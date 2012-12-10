package edu.ucsd.gwt2.modelview.client;

import java.util.HashMap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.ucsd.gwt2.modelview.shared.datamodel.Annotation;
import edu.ucsd.gwt2.modelview.shared.datamodel.DatasetAsync;

/**
 * The high-level SLASH service for the client. It wraps up many of the intricacies of the actual service
 * due to the chunking that it does.
 * @author Jeffrey Bush
 *
 */
public class ClientSlashService
{
	/** Create a remote service proxy to talk to the server-side service. */
	private static final SlashServiceAsync slashService = GWT.create(SlashService.class);

	public static void getDataset(long datasetID, long modelID, DatasetAsyncCallback callback) throws IllegalArgumentException
	{
		slashService.getDatasetAsync(datasetID, modelID, new DatasetAsyncCB(callback));
	}
	
	private static class DatasetAsyncCB implements AsyncCallback<DatasetAsync>, ScheduledCommand
	{
		private final static int ANNOTS_AT_ONCE = 500;
		private final static int GROUPS_AT_ONCE = 5;
		private boolean failed = false;
		private DatasetAsyncCallback callback;
		private DatasetAsync dataset;
		private long[][] annotation_id_groups;
		private int groups_done = 0;
		private HashMap<Long, Annotation> annotations;
		public DatasetAsyncCB(DatasetAsyncCallback callback)
		{
			this.callback = callback;
		}
		@Override
		public void onFailure(Throwable caught)
		{
			this.failed = true;
			this.callback.onFailure(caught);
		}
		@Override
		public void onSuccess(DatasetAsync dataset)
		{
			// Check for failures
			if (dataset == null)
			{
				this.failed = true;
				this.callback.onFailure(null);
			}
			
			// Setup basic information
			this.dataset = dataset;
			int len = dataset.annotationIDs.length, ngroups = (len + ANNOTS_AT_ONCE - 1) / ANNOTS_AT_ONCE, ngroups_now = Math.min(GROUPS_AT_ONCE, ngroups);
			this.annotations = new HashMap<Long, Annotation>(len);
			
			// Split annotations into groups and parallel load them
			this.annotation_id_groups = new long[ngroups][];
			for (int i = 0; i < ngroups_now; ++i)
			{
				int from = i * ANNOTS_AT_ONCE, l = Math.min((i + 1) * ANNOTS_AT_ONCE, len) - from;
				System.arraycopy(dataset.annotationIDs, from, this.annotation_id_groups[i] = new long[l], 0, l);
				slashService.getAnnotations(this.annotation_id_groups[i], new AnnotationListCB());
			}
			for (int i = ngroups_now; i < ngroups; ++i)
			{
				int from = i * ANNOTS_AT_ONCE, l = Math.min((i + 1) * ANNOTS_AT_ONCE, len) - from;
				System.arraycopy(dataset.annotationIDs, from, this.annotation_id_groups[i] = new long[l], 0, l);
			}
			
			// Update progress
			this.callback.onProgress(0, len);
		}
		
		private class AnnotationListCB implements AsyncCallback<Annotation[]>
		{
			@Override
			public void onFailure(Throwable caught)
			{
				DatasetAsyncCB.this.failed = true;
				DatasetAsyncCB.this.callback.onFailure(caught);
			}

			@Override
			public synchronized void onSuccess(Annotation[] annotations)
			{
				// Check for failures
				if (DatasetAsyncCB.this.failed) { return; }
				if (annotations == null)
				{
					DatasetAsyncCB.this.failed = true;
					DatasetAsyncCB.this.callback.onFailure(null);
				}
				
				// Copy into annotations map
				int len = annotations.length;
				for (int i = 0; i < len; ++i)
				{
					DatasetAsyncCB.this.annotations.put(annotations[i].id, annotations[i]);
				}

				// Update progress
				DatasetAsyncCB.this.callback.onProgress(DatasetAsyncCB.this.annotations.size(), DatasetAsyncCB.this.dataset.annotationIDs.length);

				// Start another request or maybe finish up
				int next = DatasetAsyncCB.GROUPS_AT_ONCE + DatasetAsyncCB.this.groups_done++, ngroups = DatasetAsyncCB.this.annotation_id_groups.length;
				if (next < ngroups)
				{
					// Request another group
					slashService.getAnnotations(DatasetAsyncCB.this.annotation_id_groups[next], new AnnotationListCB());
				}
				else if (DatasetAsyncCB.this.groups_done == ngroups)
				{
					// If all annotation groups have returned data, then lets finish up with the processing
					Scheduler.get().scheduleDeferred(DatasetAsyncCB.this);
				}
			}
		}
		
		@Override
		public void execute()
		{
	        // Build the tree
			@SuppressWarnings("unchecked")
			HashMap<Long, Annotation> roots = (HashMap<Long, Annotation>)this.annotations.clone();
	        for (Annotation a : this.annotations.values())
	        {
	        	long[] child_ids = this.dataset.annotationChildren.get(a.id);
	        	if (child_ids != null)
	        	{
	        		int len = child_ids.length;
	        		a.children = new Annotation[len];
	        		for (int i = 0; i < len; ++i)
	        		{
	        			Long id = child_ids[i];
	        			roots.remove(id);
	        			a.children[i] = this.annotations.get(id);
	        		}
	        	}
	        }
	        
	        // Set the root nodes and announce
	        this.dataset.dataset.rootAnnotations = roots.values().toArray(new Annotation[roots.size()]);
	        this.callback.onSuccess(this.dataset.dataset);
		}
	}
}

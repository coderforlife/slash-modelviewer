package edu.ucsd.gwt2.modelview.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.ucsd.gwt2.modelview.shared.datamodel.Annotation;
import edu.ucsd.gwt2.modelview.shared.datamodel.Dataset;
import edu.ucsd.gwt2.modelview.shared.datamodel.DatasetAsync;

/**
 * The async counterpart of <code>SlashService</code>.
 */
public interface SlashServiceAsync
{
	void getCompleteDataset(long datasetID, long modelID, AsyncCallback<Dataset> callback) throws IllegalArgumentException;

	void getDatasetAsync(long datasetID, long modelID, AsyncCallback<DatasetAsync> callback) throws IllegalArgumentException;

	void getAnnotations(long[] annotationIDs, AsyncCallback<Annotation[]> callback) throws IllegalArgumentException;
}

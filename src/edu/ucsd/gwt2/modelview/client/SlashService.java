package edu.ucsd.gwt2.modelview.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.ucsd.gwt2.modelview.shared.datamodel.Annotation;
import edu.ucsd.gwt2.modelview.shared.datamodel.Dataset;
import edu.ucsd.gwt2.modelview.shared.datamodel.DatasetAsync;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("slash")
public interface SlashService extends RemoteService
{
	Dataset getCompleteDataset(long datasetID, long modelID) throws Exception;

	DatasetAsync getDatasetAsync(long datasetID, long modelID) throws Exception;
	
	Annotation[] getAnnotations(long[] annotationIDs) throws Exception;
}

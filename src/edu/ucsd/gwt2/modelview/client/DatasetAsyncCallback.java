package edu.ucsd.gwt2.modelview.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.ucsd.gwt2.modelview.shared.datamodel.Dataset;

public interface DatasetAsyncCallback extends AsyncCallback<Dataset>
{
	void onProgress(int current, int total);
}

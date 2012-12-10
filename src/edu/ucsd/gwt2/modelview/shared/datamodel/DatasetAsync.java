package edu.ucsd.gwt2.modelview.shared.datamodel;

import java.io.Serializable;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The container for an asynchronous dataset retrieval.
 * When initially created, the dataset contained in this has no annotations loaded.
 * The annotations must be loaded client-side from the list of IDs and the children data.
 * @author Jeffrey Bush
 */
public class DatasetAsync implements Serializable, IsSerializable
{
	private static final long serialVersionUID = 8963700153129821590L;

	public Dataset dataset = null;
	public long[] annotationIDs = null;
	public HashMap<Long, long[]> annotationChildren = null;
}

package edu.ucsd.gwt2.modelview.shared.datamodel;

import java.io.Serializable;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Represents a Dataset from the database
 */
public class Dataset implements Serializable, IsSerializable
{
	private static final long serialVersionUID = 5258709795804834132L;

	public long id = Long.MIN_VALUE;
	
//	public String datasetName = null;
//	public int projectID = Integer.MIN_VALUE;
//	public String resourcePath = null;
//	public String organismName = null;
//	public String organismOntology = null;
//	public String anatomicalRegionName = null;
//	public String anatomicalRegionOntology = null;
//	public String atlasName = null;
//	public boolean isCurated = false;
//	public String curationUserName = null;
//	public boolean isPubliclyAvailable = false;
	
	public HashMap<String, String> applicationData = null;

	//public int annotationCount = 0;
	public Annotation[] rootAnnotations = null;
	//public BoundingBox2D boundingBox = null;

	public Quat orientation = null;
	public Point3D origin = null;
	public Point3D dimensions = null;
	public Point3D scaleFactor = null;
}

package edu.ucsd.gwt2.modelview.shared.datamodel;

import java.io.Serializable;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Represents an Annotation from the database
 */
public class Annotation implements Serializable, IsSerializable
{
	private static final long serialVersionUID = 1654522382058194958L;
	public static final int COLOR_NOT_SPECIFIED = Integer.MIN_VALUE;
	
	public long id = Long.MIN_VALUE;
	public long modelID = Long.MIN_VALUE;
	
//	public String ontologyName = null;
//	public String objectName = null;
//	public String objectOntologyURI = null;
	
	public HashMap<String, String> applicationData = null;

	public Annotation[] children = null;
	//public BoundingBox2D boundingBox = null;

	//public String geometryType = null;
	public Geometry[] geometries = null;

	public int color = Annotation.COLOR_NOT_SPECIFIED; // only solid colors are allowed, no alpha
	public Quat orientation = null;
}

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
	
	public long id = Long.MIN_VALUE;
	public long modelID = Long.MIN_VALUE;
	
//	public String ontologyName = null;
//	public String objectName = null;
//	public String objectOntologyURI = null;
	
    public HashMap<String, String> applicationData = null;

    public Annotation[] children = null;
	//public BoundingBox2D boundingBox = null;

	public String geometryType = null;
	public Geometry[] geometries = null;

    public Quat orientation = null;
}

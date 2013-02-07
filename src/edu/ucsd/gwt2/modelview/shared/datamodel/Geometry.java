package edu.ucsd.gwt2.modelview.shared.datamodel;

import java.io.Serializable;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Represents a Geometry from the database
 */
public class Geometry implements Serializable, IsSerializable
{
	private static final long serialVersionUID = 1419406625005003036L;
	
    public long id = Long.MIN_VALUE;

//	public String userName = null;
//	public long editTime = Long.MIN_VALUE;

    public HashMap<String, String> applicationData = null;
    public String type = null;
    
	public double z = Double.NaN;
    public Point2D[] traceData = null;
}

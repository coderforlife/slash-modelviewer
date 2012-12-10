package edu.ucsd.gwt2.modelview.shared.datamodel;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A quaternion.
 * @author Jeffrey Bush
 */
public class Quat implements Serializable, IsSerializable
{
	private static final long serialVersionUID = 2408945909715839307L;

	public static final Quat identity = new Quat(0, 0, 0, 1);
	
	public final double x;
	public final double y;
	public final double z;
	public final double w;
	public Quat(double x, double y, double z, double w) { this.x = x; this.y = y; this.z = z; this.w = w; }
}

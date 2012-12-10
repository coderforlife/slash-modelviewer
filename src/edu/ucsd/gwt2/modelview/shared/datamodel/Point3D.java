package edu.ucsd.gwt2.modelview.shared.datamodel;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A 3D point.
 * @author Jeffrey Bush
 */
public class Point3D implements Serializable, IsSerializable
{
	private static final long serialVersionUID = 8942846116650314973L;

	public static final Point3D origin = new Point3D(0, 0, 0);
	
	public final double x;
	public final double y;
	public final double z;
	public Point3D(double x, double y, double z) { this.x = x; this.y = y; this.z = z; }
	
	public Point3D multiply(double scalar) { return new Point3D(this.x * scalar, this.y * scalar, this.z * scalar); }
	public Point3D multiply(Point3D p) { return new Point3D(this.x * p.x, this.y * p.y, this.z * p.z); }
	public Point3D add(Point3D p) { return new Point3D(this.x + p.x, this.y + p.y, this.z + p.z); }
	
	public Point2D get2D() { return new Point2D(this.x, this.y); }
}

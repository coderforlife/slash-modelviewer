package edu.ucsd.gwt2.modelview.shared.datamodel;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A 2D bounding box.
 * @author Jeffrey Bush
 */
public class BoundingBox2D implements Serializable, IsSerializable
{
	private static final long serialVersionUID = -5886948685847720176L;

	public final Point2D min;
	public final Point2D max;
	
	public BoundingBox2D(Point2D min, Point2D max) { this.min = min; this.max = max; }
	public BoundingBox2D(double min_x, double min_y, double max_x, double max_y) { this.min = new Point2D(min_x, min_y); this.max = new Point2D(max_x, max_y); }
	
	public BoundingBox2D scale(Point2D scale) { return new BoundingBox2D(this.min.multiply(scale), this.max.multiply(scale)); }
}

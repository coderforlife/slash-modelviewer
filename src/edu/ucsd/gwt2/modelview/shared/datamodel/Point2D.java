package edu.ucsd.gwt2.modelview.shared.datamodel;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A 2D point.
 * @author Jeffrey Bush
 */
public class Point2D implements Serializable, IsSerializable, Comparable<Point2D>
{
	private static final long serialVersionUID = 5312096026918989431L;

	public static final Point2D origin = new Point2D(0, 0);
	
	public final double x;
	public final double y;
	public Point2D(double x, double y) { this.x = x; this.y = y; }

	@Override
	public String toString() { return "(" + this.x + "," + this.y + ")"; }
	@Override
	public int compareTo(Point2D p) { int c = Double.compare(this.x, p.x); return c == 0 ? Double.compare(this.y, p.y) : c; }
	public boolean equals(Point2D p) { return this.x == p.x && this.y == p.y; }
	
	public Point2D multiply(double scalar) { return new Point2D(this.x * scalar, this.y * scalar); }
	public Point2D multiply(Point2D p) { return new Point2D(this.x * p.x, this.y * p.y); }
	public Point2D add(Point2D p) { return new Point2D(this.x + p.x, this.y + p.y); }

	public double dist2(Point2D p) { double x = this.x - p.x, y = this.y - p.y; return x * x + y * y; }
}

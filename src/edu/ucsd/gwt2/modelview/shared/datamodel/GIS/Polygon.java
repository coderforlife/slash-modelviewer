package edu.ucsd.gwt2.modelview.shared.datamodel.GIS;

import edu.ucsd.gwt2.modelview.shared.datamodel.Point2D;

public class Polygon extends SpatialObject
{
	private static final long serialVersionUID = -2719648588389595386L;

	public final Point2D[][] points;
	
	public Polygon(Point2D[][] points) { this.points = points; }

	protected Polygon copy() { return new Polygon(copy(this.points, new Point2D[this.points.length][])); } // not a deep copy

	@Override
	public SpatialType type() { return SpatialType.Polygon; }
	
	@Override
	public Point2D[] getPoints()
	{
		int n = 0;
		for (int i = 0; i < this.points.length; ++i)
			n += this.points[i].length;
		Point2D[] points = new Point2D[n];
		n = 0;
		for (int i = 0; i < this.points.length; ++i)
		{
			System.arraycopy(this.points[i], 0, points, n, this.points[i].length);
			n += this.points[i].length;
		}
		return points;
	}

	@Override
	public SpatialObject reduce()
	{
		int len = this.points.length;
		Polygon poly = this;
		for (int i = 0; i < len; ++i)
		{
			Point2D[] r = reduce(this.points[i]);
			if (r != null)
			{
				if (poly == this) { poly = this.copy(); }
				poly.points[i] = r;
			}
		}
		return poly;
	}
}
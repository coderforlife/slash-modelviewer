package edu.ucsd.gwt2.modelview.shared.datamodel.GIS;

import edu.ucsd.gwt2.modelview.shared.datamodel.Point2D;

public class Point extends SpatialObject
{
	private static final long serialVersionUID = 2290968017648162846L;

	public final Point2D point;
	
	public Point(Point2D point) { this.point = point; }

	@Override
	public SpatialType type() { return SpatialType.Point; }
	
	@Override
	public Point2D[] getPoints() { return new Point2D[]{this.point}; }

	@Override
	public SpatialObject reduce() { return this; }
}

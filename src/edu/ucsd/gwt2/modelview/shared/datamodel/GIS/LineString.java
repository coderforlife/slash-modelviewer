package edu.ucsd.gwt2.modelview.shared.datamodel.GIS;

import edu.ucsd.gwt2.modelview.shared.datamodel.Point2D;

public class LineString extends SpatialObject
{
	private static final long serialVersionUID = -4358841871103172174L;

	public final Point2D[] points;
	
	public LineString(Point2D[] points) { this.points = points; }

	@Override
	public SpatialType type() { return SpatialType.LineString; }
	
	@Override
	public Point2D[] getPoints() { return this.points; }
}

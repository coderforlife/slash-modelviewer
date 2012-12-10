package edu.ucsd.gwt2.modelview.shared.datamodel.GIS;

public class MultiPoint extends Multi<Point>
{
	private static final long serialVersionUID = 6322691187213780471L;

	public MultiPoint(Point[] parts) { super(parts); }

	@Override
	public SpatialType type() { return SpatialType.MultiPoint; }

	@Override
	public SpatialType subType() { return SpatialType.Point; }
}
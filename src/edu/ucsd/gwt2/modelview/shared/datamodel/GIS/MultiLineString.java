package edu.ucsd.gwt2.modelview.shared.datamodel.GIS;

public class MultiLineString extends Multi<LineString>
{
	private static final long serialVersionUID = -2477101452088296665L;

	public MultiLineString(LineString[] parts) { super(parts); }
	
	@Override
	public SpatialType type() { return SpatialType.MultiLineString; }

	@Override
	public SpatialType subType() { return SpatialType.LineString; }
}
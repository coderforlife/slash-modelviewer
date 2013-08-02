package edu.ucsd.gwt2.modelview.shared.datamodel.GIS;

public class MultiPolygon extends Multi<Polygon>
{
	private static final long serialVersionUID = 8662992638434495422L;

	public MultiPolygon(Polygon[] parts) { super(parts); }

	@Override
	protected GeometryCollectionBase<Polygon> copy() { return new MultiPolygon(copy(super.parts, new Polygon[super.parts.length])); }

	@Override
	public SpatialType type() { return SpatialType.MultiPolygon; }

	@Override
	public SpatialType subType() { return SpatialType.Polygon; }
}
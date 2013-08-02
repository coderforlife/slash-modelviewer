package edu.ucsd.gwt2.modelview.shared.datamodel.GIS;

public class GeometryCollection extends GeometryCollectionBase<SpatialObject>
{
	private static final long serialVersionUID = -8626738641149250851L;

	public GeometryCollection(SpatialObject[] parts) { super(parts); }

	@Override
	protected GeometryCollectionBase<SpatialObject> copy() { return new GeometryCollection(copy(super.parts, new SpatialObject[super.parts.length])); }
	
	@Override
	public SpatialType type() { return SpatialType.GeometryCollection; }
}
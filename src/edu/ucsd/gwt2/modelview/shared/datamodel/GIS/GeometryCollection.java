package edu.ucsd.gwt2.modelview.shared.datamodel.GIS;

public class GeometryCollection extends GeometryCollectionBase<SpatialObject>
{
	private static final long serialVersionUID = -8626738641149250851L;

	public GeometryCollection(SpatialObject[] parts) { super(parts); }

	@Override
	public SpatialType type() { return SpatialType.GeometryCollection; }
}
package edu.ucsd.gwt2.modelview.shared.datamodel.GIS;

public abstract class Multi<T extends SpatialObject> extends GeometryCollectionBase<T>
{
	private static final long serialVersionUID = -7786409218623567583L;

	public Multi(T[] parts) { super(parts); }

	public abstract SpatialType subType();
}
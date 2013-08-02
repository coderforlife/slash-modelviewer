package edu.ucsd.gwt2.modelview.shared.datamodel.GIS;

import edu.ucsd.gwt2.modelview.shared.datamodel.Point2D;

public abstract class GeometryCollectionBase<T extends SpatialObject> extends SpatialObject
{
	private static final long serialVersionUID = 6407897564451388889L;

	public final T[] parts;
	
	public GeometryCollectionBase(T[] parts) { this.parts = parts; }

	protected abstract GeometryCollectionBase<T> copy(); // not a deep copy
	
	@Override
	public Point2D[] getPoints()
	{
		int n = 0;
		Point2D[][] parts = new Point2D[this.parts.length][];
		for (int i = 0; i < this.parts.length; ++i)
		{
			parts[i] = this.parts[i].getPoints();
			n += parts[i].length;
		}
		Point2D[] points = new Point2D[n];
		n = 0;
		for (int i = 0; i < this.parts.length; ++i)
		{
			System.arraycopy(parts[i], 0, points, n, parts[i].length);
			n += parts[i].length;
		}
		return points;
	}

	@Override
	public SpatialObject reduce()
	{
		int len = this.parts.length;
		GeometryCollectionBase<T> obj = this;
		for (int i = 0; i < len; ++i)
		{
			@SuppressWarnings("unchecked")
			T r = (T)this.parts[i].reduce();
			if (r != this.parts[i])
			{
				if (obj == this) { obj = this.copy(); }
				obj.parts[i] = (T)r;
			}
		}
		return obj;
	}
}
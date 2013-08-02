package edu.ucsd.gwt2.modelview.shared.datamodel.GIS;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;

public class MultiPoint extends Multi<Point>
{
	private static final long serialVersionUID = 6322691187213780471L;

	public MultiPoint(Point[] parts) { super(parts); }

	@Override
	protected GeometryCollectionBase<Point> copy() { return new MultiPoint(copy(super.parts, new Point[super.parts.length])); }

	@Override
	public SpatialType type() { return SpatialType.MultiPoint; }

	@Override
	public SpatialType subType() { return SpatialType.Point; }

	@Override
	public SpatialObject reduce()
	{
		// Remove coincident points
		final int len = super.parts.length;
		
		// Sort (indices only)
		final Integer[] idxs = new Integer[len];
		for (int i = 1; i < len; ++i) { idxs[i] = i; }
		Arrays.sort(idxs, new Comparator<Integer>() { @Override public int compare(final Integer i1, final Integer i2) { return parts[i1].point.compareTo(parts[i2].point); } });

		// Find duplicates
		LinkedList<Integer> remove = new LinkedList<Integer>();
		int last = idxs[0];
		for (int i = 1; i < len; ++i) { int next = idxs[i]; if (super.parts[last].point.equals(super.parts[next].point)) { remove.add(idxs[i]); } last = next; }
		
		// Return the this or the reduced object 
		return remove.size() == 0 ? this : new MultiPoint(copyWithout(super.parts, new Point[super.parts.length - remove.size()], remove));
	}
}
package edu.ucsd.gwt2.modelview.shared.datamodel.GIS;

import static java.lang.Math.sqrt;

import java.io.Serializable;
import java.util.LinkedList;

import com.google.gwt.user.client.rpc.IsSerializable;

import edu.ucsd.gwt2.modelview.shared.datamodel.Point2D;

/**
 * GIS Geometry Data Types, used in spatial databases.
 * @author Jeffrey Bush
 */
public abstract class SpatialObject implements Serializable, IsSerializable
{
	private static final long serialVersionUID = 6109479979938288162L;
	
	public abstract SpatialType type();
	public abstract Point2D[] getPoints();
	public abstract SpatialObject reduce(); // reduces the number of points in the geometry by removing near-collinear or coincident points

	private static Point2D[] reduce3(Point2D p, Point2D q, Point2D r, boolean always_return)
	{
		// The three case complicates logic elsewhere and can be short-cutted
		     if (areNearlyCollinear(p, q, r)) { return p.equals(r) ? new Point2D[] { p } : new Point2D[] { p, r }; }
		else if (areNearlyCollinear(q, r, p)) { return p.equals(q) ? new Point2D[] { q } : new Point2D[] { p, q }; }
		else if (areNearlyCollinear(r, p, q)) { return q.equals(r) ? new Point2D[] { r } : new Point2D[] { q, r }; }
		else { return always_return ? new Point2D[] { p, q, r } : null; }
	}
	
	protected static Point2D[] reduce(Point2D[] pts)
	{
		// most reduces boil down to removing near-collinear points, which is what is implemented here
		// returns null if no reduction is done, otherwise it returns a new array of points
		
		int len = pts.length;
		if (len < 2) { return null; }
		else if (len == 2) { return pts[0].equals(pts[1]) ? new Point2D[] { pts[0] } : null; }
		else if (len == 3) { return reduce3(pts[0], pts[1], pts[2], false); }
		
		LinkedList<Integer> remove = new LinkedList<Integer>();
		Point2D last = pts[0];
		for (int i = 1; i < len - 1; ++i)
		{
			if (areNearlyCollinear(last, pts[i], pts[i+1])) { remove.add(i); }
			else { last = pts[i]; }
		}
		
		// At this point there are >=2 points left, so lets check the 2 and 3 case again
		int new_len = len - remove.size();
		if (new_len == 2) { return pts[0].equals(pts[len-1]) ? new Point2D[] { pts[0] } : new Point2D[] { pts[0], pts[len-1] }; } // I know pts[0] and pts[len-1] remain
		else if (new_len == 3)  { return reduce3(pts[0], last, pts[len-1], true); } // I know pts[0], last, and pts[len-1] remain
		
		// Two positions can wrap-around and need to be checked
		if (areNearlyCollinear(last, pts[len-1], pts[0])) { remove.add(len-1); --new_len; } else { last = pts[len - 1]; }
		int next = 1;
		for (Integer i : remove) { if (next != i) { break; } ++next; }
		if(areNearlyCollinear(last, pts[0], pts[next])) { remove.addFirst(0); --new_len; }
			
		// Now we have >=2 points left
		return remove.size() == 0 ? null : copyWithout(pts, new Point2D[pts.length - remove.size()], remove);
	}
	
	private static boolean areNearlyCollinear(Point2D p, Point2D q, Point2D r)
	{
		if (p.equals(q) || q.equals(r)) { return true; }
	
		//double pq2 = p.dist2(q), qr2 = q.dist2(r), rp2 = r.dist2(p); // 3 * (2 mult, 3 add) => 6 mult, 9 add
		//double mq2 = (2 * ( pq2 + qr2 ) - rp2) / 4; // squared median of triangle coming from middle point (deemed not helpful - also calculation is problematic)
		
		// Heron's formula for area => 4 sqrt, 10 mult, 14 add
		//double pq = sqrt(p.dist2(q)), qr = sqrt(q.dist2(r)), rp = sqrt(r.dist2(p)); // 3 * (1 sqrt, 2 mult, 3 add) => 3 sqrt, 6 mult, 9 add
		//double s = (pq+qr+rp) * 0.5; // 1 mult, 2 add
		//double area = sqrt(s*(s-pq)*(s-qr)*(s-rp)); // 1 sqrt, 3 mult, 3 add
		
		// Determinant formula for area => 7 mult, 5 add  [so much better than Heron's formula! even though that one would save 1 sqrt, 2 mult, and 1 add later]
		//double area = (p.x * q.y + p.y * r.x + q.x * r.y - q.y * r.x - p.y * q.x - p.x * r.y) * 0.5; // 7 mult, 5 add
		double area_2 = p.x * q.y + p.y * r.x + q.x * r.y - q.y * r.x - p.y * q.x - p.x * r.y; // 6 mult, 5 add - actually calculate 2 times the area - easier to work with and one less mult
		if (area_2 == 0.0) { return true; }
		
		// Get base and height (altitude) of the triangle
		double base = sqrt(r.dist2(p)), alt = area_2 / base; // 1 sqrt, 1 div, 2 mult, 3 add
		
		// Ideas: (with a percentage of points removed from a sample dataset) 
		//   IMOD method:           area < 0.5               95% 
		//   More restrictive:      area < 0.1               71%
		//   Take into account alt: area < 0.1 && alt < 0.1  59% **
		//                          area < 0.5 && alt < 0.1  61%
		//                          area * alt < 0.01        67%
		
		return alt <= 0.1 && area_2 <= 0.2;
	}
	
	protected static <T> T[] copyWithout(T[] src, T[] dst, LinkedList<Integer> indxs)
	{
		// copy length n array while skipping m indices with at most n-m copies
		int src_last = 0, dst_last = 0;
		while (!indxs.isEmpty())
		{
			int i = indxs.removeFirst(), len = i - src_last;
			if (len != 0)
			{
				System.arraycopy(src, src_last, dst, dst_last, len);
				dst_last += len;
			}
			src_last = i + 1;
		}
		if (src.length != src_last) { System.arraycopy(src, src_last, dst, dst_last, src.length - src_last); }
		return dst;
	}
	
	protected static <T> T[] copy(T[] src, T[] dst) { System.arraycopy(src, 0, dst, 0, src.length); return dst; }
}

package edu.ucsd.gwt2.modelview.server;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import edu.ucsd.gwt2.modelview.shared.datamodel.Point2D;
import edu.ucsd.gwt2.modelview.shared.datamodel.GIS.SpatialObject;
import edu.ucsd.gwt2.modelview.shared.datamodel.GIS.GeometryCollection;
import edu.ucsd.gwt2.modelview.shared.datamodel.GIS.LineString;
import edu.ucsd.gwt2.modelview.shared.datamodel.GIS.MultiLineString;
import edu.ucsd.gwt2.modelview.shared.datamodel.GIS.MultiPoint;
import edu.ucsd.gwt2.modelview.shared.datamodel.GIS.MultiPolygon;
import edu.ucsd.gwt2.modelview.shared.datamodel.GIS.Point;
import edu.ucsd.gwt2.modelview.shared.datamodel.GIS.Polygon;
import edu.ucsd.gwt2.modelview.shared.datamodel.GIS.SpatialType;

/**
 * GIS Utility functions for the server.
 * @author Jeffrey Bush
 */
public class GISServerUtil
{
	private final static byte BO_XDR = 0; //Big Endian
	private final static byte BO_NDR = 1; //Little Endian
	
	private static Point2D[] readPoints(ByteBuffer b)
	{
		int n = b.getInt();
		Point2D[] points = new Point2D[n];
		for (int i = 0; i < n; ++i)
		{
			points[i] = new Point2D(b.getDouble(), b.getDouble());
		}
		return points;
	}
	@SuppressWarnings("unchecked")
	private static <T extends SpatialObject> T[] readMulti(SpatialType type, ByteBuffer b) throws GISException
	{
		int n = b.getInt();
		T[] parts = (T[])Array.newInstance(type.clazz, n);
		for (int i = 0; i < n; ++i)
		{
			parts[i] = (T)readWKB(b);
			if (!parts[i].type().equals(type)) { throw new GISException("Illegal geometry type in GIS WKB format"); }
		}
		return parts;
	}
	
	public static SpatialObject readWKB(byte[] data) throws GISException
	{
		return readWKB(ByteBuffer.wrap(data));
	}
	public static SpatialObject readWKB(ByteBuffer b) throws GISException
	{
		switch (b.get())
		{
		case BO_XDR: b.order(ByteOrder.BIG_ENDIAN);    break;
		case BO_NDR: b.order(ByteOrder.LITTLE_ENDIAN); break;
		default: throw new GISException("Illegal byte order in GIS WKB format");
		}
		
		switch (b.getInt())
		{
		
		case SpatialType.POINT:			return new Point(new Point2D(b.getDouble(), b.getDouble()));
		case SpatialType.LINE_STRING:	return new LineString(readPoints(b));
		case SpatialType.POLYGON:
			int nrings = b.getInt();
			Point2D[][] points = new Point2D[nrings][];
			for (int i = 0; i < nrings; ++i)
			{
				points[i] = readPoints(b);
			}
			return new Polygon(points);
		
		case SpatialType.MULTI_POINT:		return new MultiPoint((Point[])readMulti(SpatialType.Point, b));
		case SpatialType.MULTI_LINE_STRING:	return new MultiLineString((LineString[])readMulti(SpatialType.LineString, b));
		case SpatialType.MULTI_POLYGON:		return new MultiPolygon((Polygon[])readMulti(SpatialType.Polygon, b));
		
		case SpatialType.GEOMETRY_COLLECTION:
			int n = b.getInt();
			SpatialObject[] parts = new SpatialObject[n];
			for (int i = 0; i < n; ++i)
			{
				parts[i] = readWKB(b);
				if (parts[i].type().equals(SpatialType.GeometryCollection)) { throw new GISException("Illegal geometry type in GIS WKB format"); }
			}
			return new GeometryCollection(parts);
			
		default: throw new GISException("Unknown geometry type in GIS WKB format");
		
		}
	}
}

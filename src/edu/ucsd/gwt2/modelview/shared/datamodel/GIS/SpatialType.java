package edu.ucsd.gwt2.modelview.shared.datamodel.GIS;

public enum SpatialType
{
	Point(1, Point.class),
	LineString(2, LineString.class),
	Polygon(3, Polygon.class),
	MultiPoint(4, MultiPoint.class),
	MultiLineString(5, MultiLineString.class),
	MultiPolygon(6, MultiPolygon.class),
	GeometryCollection(7, GeometryCollection.class);
	
	public final static int POINT = 1;
	public final static int LINE_STRING = 2;
	public final static int POLYGON = 3;
	public final static int MULTI_POINT = 4;
	public final static int MULTI_LINE_STRING = 5;
	public final static int MULTI_POLYGON = 6;
	public final static int GEOMETRY_COLLECTION = 7;
	
    public final int value;
    public final Class<? extends SpatialObject> clazz;
    SpatialType(int value, Class<? extends SpatialObject> clazz) { this.value = value; this.clazz = clazz; }
}

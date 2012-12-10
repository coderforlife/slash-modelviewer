package edu.ucsd.gwt2.modelview.shared.datamodel;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

/**
 * GWT Serializer for the immutable class Point2D
 * @author Jeffrey Bush
 */
public final class Point2D_CustomFieldSerializer extends CustomFieldSerializer<Point2D>
{
	public static void serialize(SerializationStreamWriter streamWriter, Point2D p) throws SerializationException
	{
		streamWriter.writeDouble(p.x);
		streamWriter.writeDouble(p.y);
	}
	
	public static Point2D instantiate(SerializationStreamReader streamReader) throws SerializationException
	{
		return new Point2D(streamReader.readDouble(), streamReader.readDouble());
	}
	
	public static void deserialize(SerializationStreamReader streamReader, Point2D p) throws SerializationException { }

	@Override
	public void serializeInstance(SerializationStreamWriter streamWriter, Point2D p) throws SerializationException { Point2D_CustomFieldSerializer.serialize(streamWriter, p); }
	
	@Override
	public Point2D instantiateInstance(SerializationStreamReader streamReader) throws SerializationException { return Point2D_CustomFieldSerializer.instantiate(streamReader); }

	@Override
	public boolean hasCustomInstantiateInstance() { return true; }

	@Override
	public void deserializeInstance(SerializationStreamReader streamReader, Point2D p) throws SerializationException { }
}

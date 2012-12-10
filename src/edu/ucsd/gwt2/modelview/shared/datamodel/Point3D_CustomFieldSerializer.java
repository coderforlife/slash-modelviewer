package edu.ucsd.gwt2.modelview.shared.datamodel;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

/**
 * GWT Serializer for the immutable class Point3D
 * @author Jeffrey Bush
 */
public final class Point3D_CustomFieldSerializer extends CustomFieldSerializer<Point3D>
{
	public static void serialize(SerializationStreamWriter streamWriter, Point3D p) throws SerializationException
	{
		streamWriter.writeDouble(p.x);
		streamWriter.writeDouble(p.y);
		streamWriter.writeDouble(p.z);
	}
	
	public static Point3D instantiate(SerializationStreamReader streamReader) throws SerializationException
	{
		return new Point3D(streamReader.readDouble(), streamReader.readDouble(), streamReader.readDouble());
	}
	
	public static void deserialize(SerializationStreamReader streamReader, Point3D p) throws SerializationException { }
	
	@Override
	public void serializeInstance(SerializationStreamWriter streamWriter, Point3D p) throws SerializationException { Point3D_CustomFieldSerializer.serialize(streamWriter, p); }
	
	@Override
	public Point3D instantiateInstance(SerializationStreamReader streamReader) throws SerializationException { return Point3D_CustomFieldSerializer.instantiate(streamReader); }

	@Override
	public boolean hasCustomInstantiateInstance() { return true; }

	@Override
	public void deserializeInstance(SerializationStreamReader streamReader, Point3D p) throws SerializationException { }
}

package edu.ucsd.gwt2.modelview.shared.datamodel;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

/**
 * GWT Serializer for the immutable class BoundingBox2D
 * @author Jeffrey Bush
 */
public final class BoundingBox2D_CustomFieldSerializer extends CustomFieldSerializer<BoundingBox2D>
{
	public static void serialize(SerializationStreamWriter streamWriter, BoundingBox2D b) throws SerializationException
	{
		streamWriter.writeDouble(b.min.x);
		streamWriter.writeDouble(b.min.y);
		streamWriter.writeDouble(b.max.x);
		streamWriter.writeDouble(b.max.y);
	}
	
	public static BoundingBox2D instantiate(SerializationStreamReader streamReader) throws SerializationException
	{
		return new BoundingBox2D(streamReader.readDouble(), streamReader.readDouble(), streamReader.readDouble(), streamReader.readDouble());
	}

	public static void deserialize(SerializationStreamReader streamReader, BoundingBox2D b) throws SerializationException { }
	
	@Override
	public void serializeInstance(SerializationStreamWriter streamWriter, BoundingBox2D b) throws SerializationException { BoundingBox2D_CustomFieldSerializer.serialize(streamWriter, b); }
	
	@Override
	public BoundingBox2D instantiateInstance(SerializationStreamReader streamReader) throws SerializationException { return BoundingBox2D_CustomFieldSerializer.instantiate(streamReader); }

	@Override
	public boolean hasCustomInstantiateInstance() { return true; }

	@Override
	public void deserializeInstance(SerializationStreamReader streamReader, BoundingBox2D b) throws SerializationException { }
}

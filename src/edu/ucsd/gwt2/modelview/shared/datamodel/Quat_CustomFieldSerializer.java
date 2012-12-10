package edu.ucsd.gwt2.modelview.shared.datamodel;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

/**
 * GWT Serializer for the immutable class Quat
 * @author Jeffrey Bush
 */
public final class Quat_CustomFieldSerializer extends CustomFieldSerializer<Quat>
{
	public static void serialize(SerializationStreamWriter streamWriter, Quat q) throws SerializationException
	{
		streamWriter.writeDouble(q.x);
		streamWriter.writeDouble(q.y);
		streamWriter.writeDouble(q.z);
		streamWriter.writeDouble(q.w);
	}
	
	public static Quat instantiate(SerializationStreamReader streamReader) throws SerializationException
	{
		return new Quat(streamReader.readDouble(), streamReader.readDouble(), streamReader.readDouble(), streamReader.readDouble());
	}

	public static void deserialize(SerializationStreamReader streamReader, Quat q) throws SerializationException { }

	@Override
	public void serializeInstance(SerializationStreamWriter streamWriter, Quat q) throws SerializationException { Quat_CustomFieldSerializer.serialize(streamWriter, q); }
	
	@Override
	public Quat instantiateInstance(SerializationStreamReader streamReader) throws SerializationException { return Quat_CustomFieldSerializer.instantiate(streamReader); }

	@Override
	public boolean hasCustomInstantiateInstance() { return true; }

	@Override
	public void deserializeInstance(SerializationStreamReader streamReader, Quat q) throws SerializationException { }
}

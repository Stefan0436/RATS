package org.asf.rats.service.packet.entries;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.asf.rats.service.packet.PacketEntry;

public class SerializingEntry<T> implements PacketEntry<T> {

	private T val;

	SerializingEntry() {

	}

	public SerializingEntry(T value) {
		val = value;
	}

	@Override
	public int length() {
		return toArray().length;
	}

	@Override
	public long type() {
		return 1271422171532l;
	}

	@Override
	public byte[] toArray() {
		try {
			return serialize(val);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isCompatible(long type) {
		return type == type();
	}

	@Override
	@SuppressWarnings("unchecked")
	public PacketEntry<T> fromArray(byte[] data) {
		try {
			return new SerializingEntry<T>((T) deserialize(data));
		} catch (ClassNotFoundException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public T get() {
		return val;
	}

	byte[] serialize(Object obj) throws IOException {
		ByteArrayOutputStream strm = new ByteArrayOutputStream();
		ObjectOutputStream serializer = new ObjectOutputStream(strm);
		serializer.writeObject(obj);
		return strm.toByteArray();
	}

	Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
		ByteArrayInputStream strm = new ByteArrayInputStream(data);
		ObjectInputStream deserializer = new ObjectInputStream(strm);
		Object obj = deserializer.readObject();
		strm.close();
		return obj;
	}

}

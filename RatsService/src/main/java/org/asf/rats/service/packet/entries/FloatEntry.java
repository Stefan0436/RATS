package org.asf.rats.service.packet.entries;

import java.nio.ByteBuffer;

import org.asf.rats.service.packet.PacketEntry;

public class FloatEntry implements PacketEntry<Float> {

	private Float val;

	FloatEntry() {

	}

	public FloatEntry(Float value) {
		val = value;
	}

	@Override
	public int length() {
		return toArray().length;
	}

	@Override
	public long type() {
		return 11824271332l;
	}

	@Override
	public byte[] toArray() {
		return ByteBuffer.allocate(4).putFloat(val).array();
	}

	@Override
	public boolean isCompatible(long type) {
		return type == type();
	}

	@Override
	public PacketEntry<Float> fromArray(byte[] data) {
		return new FloatEntry(ByteBuffer.wrap(data).getFloat());
	}

	@Override
	public Float get() {
		return val;
	}

}

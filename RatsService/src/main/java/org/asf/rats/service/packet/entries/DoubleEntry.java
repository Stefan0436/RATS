package org.asf.rats.service.packet.entries;

import java.nio.ByteBuffer;

import org.asf.rats.service.packet.PacketEntry;

public class DoubleEntry implements PacketEntry<Double> {

	private Double val;

	DoubleEntry() {

	}

	public DoubleEntry(Double value) {
		val = value;
	}

	@Override
	public int length() {
		return toArray().length;
	}

	@Override
	public long type() {
		return 1162733142417l;
	}

	@Override
	public byte[] toArray() {
		return ByteBuffer.allocate(8).putDouble(val).array();
	}

	@Override
	public boolean isCompatible(long type) {
		return type == type();
	}

	@Override
	public PacketEntry<Double> fromArray(byte[] data) {
		return new DoubleEntry(ByteBuffer.wrap(data).getDouble());
	}

	@Override
	public Double get() {
		return val;
	}

}

package org.asf.rats.service.packet.entries;

import java.nio.ByteBuffer;

import org.asf.rats.service.packet.PacketEntry;

public class IntEntry implements PacketEntry<Integer> {

	private Integer val;

	IntEntry() {

	}

	public IntEntry(Integer value) {
		val = value;
	}

	@Override
	public int length() {
		return toArray().length;
	}

	@Override
	public long type() {
		return 1212632l;
	}

	@Override
	public byte[] toArray() {
		return ByteBuffer.allocate(4).putInt(val).array();
	}

	@Override
	public boolean isCompatible(long type) {
		return type == type();
	}

	@Override
	public PacketEntry<Integer> fromArray(byte[] data) {
		return new IntEntry(ByteBuffer.wrap(data).getInt());
	}

	@Override
	public Integer get() {
		return val;
	}

}

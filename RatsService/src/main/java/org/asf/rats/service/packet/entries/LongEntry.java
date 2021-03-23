package org.asf.rats.service.packet.entries;

import java.nio.ByteBuffer;

import org.asf.rats.service.packet.PacketEntry;

public class LongEntry implements PacketEntry<Long> {

	private Long val;

	LongEntry() {

	}

	public LongEntry(Long value) {
		val = value;
	}

	@Override
	public int length() {
		return toArray().length;
	}

	@Override
	public long type() {
		return 124272619l;
	}

	@Override
	public byte[] toArray() {
		return ByteBuffer.allocate(8).putLong(val).array();
	}

	@Override
	public boolean isCompatible(long type) {
		return type == type();
	}

	@Override
	public PacketEntry<Long> fromArray(byte[] data) {
		return new LongEntry(ByteBuffer.wrap(data).getLong());
	}

	@Override
	public Long get() {
		return val;
	}

}

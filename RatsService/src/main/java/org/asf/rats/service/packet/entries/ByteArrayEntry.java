package org.asf.rats.service.packet.entries;

import org.asf.rats.service.packet.PacketEntry;

public class ByteArrayEntry implements PacketEntry<byte[]> {

	private byte[] val;

	ByteArrayEntry() {

	}

	public ByteArrayEntry(byte[] value) {
		val = value;
	}

	@Override
	public int length() {
		return toArray().length;
	}

	@Override
	public long type() {
		return 1143732171330301337l;
	}

	@Override
	public byte[] toArray() {
		return val;
	}

	@Override
	public boolean isCompatible(long type) {
		return type == type();
	}

	@Override
	public PacketEntry<byte[]> fromArray(byte[] data) {
		return new ByteArrayEntry(data);
	}

	@Override
	public byte[] get() {
		return val;
	}

}

package org.asf.rats.service.packet.entries;

import org.asf.rats.service.packet.PacketEntry;

public class ByteEntry implements PacketEntry<Byte> {

	private Byte val;

	ByteEntry() {

	}

	public ByteEntry(Byte value) {
		val = value;
	}

	@Override
	public int length() {
		return toArray().length;
	}

	@Override
	public long type() {
		return 114373217l;
	}

	@Override
	public byte[] toArray() {
		return new byte[] { val };
	}

	@Override
	public boolean isCompatible(long type) {
		return type == type();
	}

	@Override
	public PacketEntry<Byte> fromArray(byte[] data) {
		return new ByteEntry(data[0]);
	}

	@Override
	public Byte get() {
		return val;
	}

}

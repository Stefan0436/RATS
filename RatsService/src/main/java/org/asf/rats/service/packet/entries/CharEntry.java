package org.asf.rats.service.packet.entries;

import java.nio.ByteBuffer;

import org.asf.rats.service.packet.PacketEntry;

public class CharEntry implements PacketEntry<Character> {

	private Character val;

	CharEntry() {
	}

	public CharEntry(Character value) {
		val = value;
	}

	@Override
	public int length() {
		return toArray().length;
	}

	@Override
	public long type() {
		return 115201330l;
	}

	@Override
	public byte[] toArray() {
		return ByteBuffer.allocate(2).putChar(val).array();
	}

	@Override
	public boolean isCompatible(long type) {
		return type == type();
	}

	@Override
	public PacketEntry<Character> fromArray(byte[] data) {
		return new CharEntry(ByteBuffer.wrap(data).getChar());
	}

	@Override
	public Character get() {
		return val;
	}

}

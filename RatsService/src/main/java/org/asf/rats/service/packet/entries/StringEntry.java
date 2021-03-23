package org.asf.rats.service.packet.entries;

import org.asf.rats.service.packet.PacketEntry;

public class StringEntry implements PacketEntry<String> {

	private String val;

	StringEntry() {

	}

	public StringEntry(String value) {
		val = value;
	}

	@Override
	public int length() {
		return val.getBytes().length;
	}

	@Override
	public long type() {
		return 1313230212619l;
	}

	@Override
	public byte[] toArray() {
		return val.getBytes();
	}

	@Override
	public boolean isCompatible(long type) {
		return type == type();
	}

	@Override
	public PacketEntry<String> fromArray(byte[] data) {
		return new StringEntry(new String(data));
	}

	@Override
	public String get() {
		return val;
	}

}

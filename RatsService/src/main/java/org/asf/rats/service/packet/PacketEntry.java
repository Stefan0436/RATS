package org.asf.rats.service.packet;

public interface PacketEntry<T> {
	public int length();
	public long type();
	public byte[] toArray();
	
	public boolean isCompatible(long type);
	public PacketEntry<T> fromArray(byte[] data);
	
	public T get();
}

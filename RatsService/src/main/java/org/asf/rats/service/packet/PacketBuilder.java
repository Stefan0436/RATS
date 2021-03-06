package org.asf.rats.service.packet;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.asf.aos.util.service.extra.slib.util.ArrayUtil;
import org.asf.rats.service.packet.entries.ByteArrayEntry;
import org.asf.rats.service.packet.entries.ByteEntry;
import org.asf.rats.service.packet.entries.CharEntry;
import org.asf.rats.service.packet.entries.DoubleEntry;
import org.asf.rats.service.packet.entries.FloatEntry;
import org.asf.rats.service.packet.entries.IntEntry;
import org.asf.rats.service.packet.entries.SerializingEntry;
import org.asf.rats.service.packet.entries.StringEntry;

/**
 * 
 * Packet building system, creates network packets.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public class PacketBuilder {
	protected ArrayList<PacketEntry<?>> entries = new ArrayList<PacketEntry<?>>();
	protected long version = 1l;
	
	/**
	 * Adds an entry
	 * @param entry Packet entry
	 */
	public PacketBuilder add(PacketEntry<?> entry) {
		entries.add(entry);
		return this;
	}

	/**
	 * Adds a string
	 * @param entry String entry
	 */
	public PacketBuilder add(String entry) {
		entries.add(new StringEntry(entry));
		return this;
	}

	/**
	 * Adds an integer
	 * @param entry Integer entry
	 */
	public PacketBuilder add(int entry) {
		entries.add(new IntEntry(entry));
		return this;
	}

	/**
	 * Adds a floating-point number
	 * @param entry Float entry
	 */
	public PacketBuilder add(float entry) {
		entries.add(new FloatEntry(entry));
		return this;
	}

	/**
	 * Adds a double-precision floating-point number
	 * @param entry Double entry
	 */
	public PacketBuilder add(double entry) {
		entries.add(new DoubleEntry(entry));
		return this;
	}

	/**
	 * Adds a byte number (recommended to avoid usage, use arrays instead)
	 * @param entry Byte entry
	 */
	public PacketBuilder add(byte entry) {
		entries.add(new ByteEntry(entry));
		return this;
	}

	/**
	 * Adds a character (recommended to avoid usage, use Strings instead)
	 * @param entry Char entry
	 */
	public PacketBuilder add(char entry) {
		entries.add(new CharEntry(entry));
		return this;
	}

	/**
	 * Adds a byte array
	 * @param entry Byte array entry
	 */
	public PacketBuilder add(byte[] entry) {
		entries.add(new ByteArrayEntry(entry));
		return this;
	}

	/**
	 * Adds a normal object (serializing)
	 * @param entry Object entry
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public PacketBuilder add(Object entry) {
		entries.add(new SerializingEntry(entry));
		return this;
	}

	/**
	 * Sets the version needed to parse the packet, default is one.
	 * @param version Packet version.
	 */
	public PacketBuilder setVersion(long version) {
		this.version = version;
		return this;
	}

	/**
	 * Builds the packet
	 * 
	 * @return Byte array representing the packet.
	 */
	public byte[] build() {
		ArrayList<Byte> bytes = new ArrayList<Byte>();

		for (byte b : ByteBuffer.allocate(8).putLong(version).array()) {
			bytes.add(b);
		}

		ArrayList<Byte> bytesTmp = new ArrayList<Byte>();
		for (PacketEntry<?> entry : entries) {
			for (byte b : ByteBuffer.allocate(8).putLong(entry.type()).array()) {
				bytesTmp.add(b);
			}
			for (byte b : ByteBuffer.allocate(4).putInt(entry.length()).array()) {
				bytesTmp.add(b);
			}
		}
		
		for (byte b : ByteBuffer.allocate(4).putInt(bytesTmp.size()).array()) {
			bytes.add(b);
		}
		bytes.addAll(bytesTmp);

		bytesTmp = new ArrayList<Byte>();
		for (PacketEntry<?> entry : entries) {
			for (byte b : entry.toArray()) {
				bytesTmp.add(b);
			}
		}

		for (byte b : ByteBuffer.allocate(4).putInt(bytesTmp.size()).array()) {
			bytes.add(b);
		}
		bytes.addAll(bytesTmp);

		return ArrayUtil.castWrapperArrayToPrimitive(bytes.toArray(t -> new Byte[t]), new byte[0]);
	}
}

package org.asf.rats.service.packet;

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.asf.rats.service.packet.entries.ByteArrayEntry;
import org.asf.rats.service.packet.entries.ByteEntry;
import org.asf.rats.service.packet.entries.CharEntry;
import org.asf.rats.service.packet.entries.DoubleEntry;
import org.asf.rats.service.packet.entries.FloatEntry;
import org.asf.rats.service.packet.entries.IntEntry;
import org.asf.rats.service.packet.entries.LongEntry;
import org.asf.rats.service.packet.entries.SerializingEntry;
import org.asf.rats.service.packet.entries.StringEntry;

/**
 * 
 * Packet parsing system, parses network packets created by the
 * {@link PacketBuilder PacketBuilder}.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public class PacketParser {
	protected ArrayList<PacketEntry<?>> entries = new ArrayList<PacketEntry<?>>();

	@SuppressWarnings("rawtypes")
	protected HashMap<Long, Class<? extends PacketEntry>> entryTypes = new HashMap<Long, Class<? extends PacketEntry>>();

	public PacketEntry<?>[] getEntries() {
		return entries.toArray(t -> new PacketEntry<?>[t]);
	}
	
	/**
	 * Creates a new parser instance, registers the default entry types.
	 */
	public PacketParser() {
		registerType(new StringEntry(null));
		registerType(new LongEntry(0l));
		registerType(new IntEntry(0));
		registerType(new ByteEntry((byte)0));
		registerType(new ByteArrayEntry(new byte[0]));
		registerType(new SerializingEntry<Integer>(0));
		registerType(new FloatEntry(0f));
		registerType(new DoubleEntry(0d));
		registerType(new CharEntry('\000'));
	}

	/**
	 * Registers an entry type, defaults are already registered.
	 * 
	 * @param entry Packet entry
	 */
	public void registerType(PacketEntry<?> entry) {
		entryTypes.put(entry.type(), entry.getClass());
	}

	protected long version = 1l;

	/**
	 * Sets the version needed to parse the packet, default is one.
	 * 
	 * @param version Packet version.
	 */
	public void setSupportedVersion(long version) {
		this.version = version;
	}

	/**
	 * Imports the packet bytes and constructs the entries.
	 */
	public void importArray(byte[] data) {
		long ver = ByteBuffer.wrap(Arrays.copyOfRange(data, 0, 8)).getLong();
		if (ver != version)
			throw new IllegalArgumentException("Packet version mismatch, got: " + ver + ", expected: " + version);

		int headerLength = ByteBuffer.wrap(Arrays.copyOfRange(data, 8, 12)).getInt();
		byte[] header = Arrays.copyOfRange(data, 12, 12 + headerLength);

		int contentLength = ByteBuffer.wrap(Arrays.copyOfRange(data, 12 + headerLength, 12 + headerLength + 4))
				.getInt();
		byte[] content = Arrays.copyOfRange(data, 12 + headerLength + 4, 12 + headerLength + 4 + contentLength);

		entries.clear();
		int currentHeader = 0;
		int currentContent = 0;
		while (currentHeader != header.length) {
			long entryType = ByteBuffer.wrap(Arrays.copyOfRange(header, currentHeader, currentHeader + 8)).getLong();
			currentHeader += 8;

			int entryLength = ByteBuffer.wrap(Arrays.copyOfRange(header, currentHeader, currentHeader + 4)).getInt();
			currentHeader += 4;
			
			try {
				@SuppressWarnings("rawtypes")
				Constructor<? extends PacketEntry> ctor = entryTypes.get(entryType).getDeclaredConstructor();
				ctor.setAccessible(true);
				PacketEntry<?> ent = ctor.newInstance();
				ent = ent.fromArray(Arrays.copyOfRange(content, currentContent, currentContent + entryLength));
				currentContent += entryLength;
				
				entries.add(ent);
			} catch (Exception e) {
				throw new IllegalArgumentException("Failed to create packet entry with type: " + entryType, e);
			}
		}
	}
}

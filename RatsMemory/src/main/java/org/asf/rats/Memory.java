package org.asf.rats;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * 
 * RaTs! Memory System, stores objects and information.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public class Memory {

	/**
	 * 
	 * @author Stefan0436 - AerialWorks Software Foundation
	 *
	 * @param <T>
	 */
	public class MemoryValue<T> extends Memory {
		private T value;
		private Class<T> valueClass;

		@SuppressWarnings("unchecked")
		protected MemoryValue(String name, T value) {
			this.value = value;
			this.name = name;
			valueClass = (Class<T>) value.getClass();
		}

		public boolean isAssignableFrom(Class<?> cls) {
			return valueClass.isAssignableFrom(cls);
		}

		public boolean isAssignableTo(Class<?> cls) {
			return cls.isAssignableFrom(valueClass);
		}

		public T getValue() {
			return value;
		}
	}

	private static Memory instance;

	protected String path = null;
	protected String name = null;

	protected ArrayList<Memory> subMemory = new ArrayList<Memory>();
	protected ArrayList<MemoryValue<?>> values = new ArrayList<MemoryValue<?>>();

	protected String separator = "\\.";

	/**
	 * Internal constructor
	 */
	protected Memory() {
		name = "root";
	}

	/**
	 * Assigns the Memory instance, allows for different implementations, must be
	 * assigned BEFORE components are initialized or things will malfunction, use
	 * launch wrappers to do so.
	 * 
	 * @param instance Memory instance
	 */
	protected static void setInstance(Memory instance) {
		Memory.instance = instance;
	}

	/**
	 * Retrieves the active Memory instance.
	 * 
	 * @return Memory instance.
	 */
	public static Memory getInstance() {
		if (instance == null)
			instance = new Memory();

		return instance;
	}

	protected static void initComponent() {
		if (instance == null)
			instance = new Memory();
	}

	@Override
	public String toString() {
		return (path != null ? path + separator : "") + name;
	}

	/**
	 * Retrieves the path of the entry.
	 */
	public String getPath() {
		return (path != null ? path + separator : "") + name;
	}

	/**
	 * Gets the memory instance by path
	 * 
	 * @param path Memory path
	 * @return Memory instance or null if not found.
	 */
	public Memory get(String path) {
		String[] pathEntries = path.split(separator);
		if (pathEntries.length == 0)
			return null;

		Memory mem = this;
		for (String entry : pathEntries) {
			ArrayList<Memory> members = mem.subMemory;
			Memory memOld = mem;

			for (Memory memory : members) {
				if (memory.name.equals(entry)) {
					mem = memory;
					break;
				}
			}

			if (mem == memOld)
				return null;
		}

		if (mem == this)
			return null;

		return mem;
	}

	/**
	 * Creates a memory instance. (returns existing if found)
	 * 
	 * @param path Memory path
	 * @return Memory instance
	 */
	public Memory getOrCreate(String path) {
		if (get(path) != null)
			return get(path);

		String[] pathEntries = path.split(separator);
		if (pathEntries.length == 0)
			return null;

		Memory mem = this;
		String currentPath = (this.path == null ? "" : getPath());
		for (String entry : pathEntries) {
			ArrayList<Memory> members = mem.subMemory;
			Memory memOld = mem;

			for (Memory memory : members) {
				if (memory.name.equals(entry)) {
					mem = memory;
					break;
				}
			}

			if (mem == memOld) {
				Memory memory = new Memory();
				memory.name = entry;
				memory.path = currentPath;
				mem.subMemory.add(memory);
				mem = memory;
			}

			if (!currentPath.isEmpty())
				currentPath += separator;
			currentPath += entry;
		}

		return mem;
	}

	/**
	 * Retrieves the name of the entry.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Assigns the value (clears any other values)
	 * 
	 * @param <T>   Value type
	 * @param value Value to assign
	 */
	public <T> void assign(T value) {
		assign("value", value);
	}

	/**
	 * Assigns the value (clears any other values)
	 * 
	 * @param <T>   Value type
	 * @param name  Value name
	 * @param value Value to assign
	 */
	public <T> void assign(String name, T value) {
		values.clear();
		values.add(new MemoryValue<T>(name, value));
	}

	/**
	 * Sets the value by name
	 * 
	 * @param <T>   Value type
	 * @param name  Value name
	 * @param value Value to assign
	 */
	public <T> void set(String name, T value) {
		for (MemoryValue<?> val : values) {
			if (val.name.equals(name)) {
				values.set(values.indexOf(val), new MemoryValue<T>(name, value));
				return;
			}
		}

		values.add(new MemoryValue<T>(name, value));
	}

	/**
	 * Appends a value
	 * 
	 * @param <T>   Value type
	 * @param value Value to append
	 */
	public <T> void append(T value) {
		append("value" + values.size(), value);
	}

	/**
	 * Appends a value
	 * 
	 * @param <T>   Value type
	 * @param name  Value name
	 * @param value Value to append
	 */
	public <T> void append(String name, T value) {
		values.add(new MemoryValue<T>(name, value));
	}

	/**
	 * Appends all values
	 * 
	 * @param <T>    Value type
	 * @param values Values to append
	 */
	public <T> void appendAll(T[] values) {
		for (T value : values) {
			append("value" + this.values.size(), value);
		}
	}

	/**
	 * Appends all values
	 * 
	 * @param <T>    Value type
	 * @param values Values to append
	 */
	public <T> void appendAll(Iterable<T> values) {
		for (T value : values) {
			append("value" + this.values.size(), value);
		}
	}

	/**
	 * Retrieves all sub-entries
	 */
	public Memory[] getMembers() {
		return subMemory.toArray(t -> new Memory[t]);
	}

	/**
	 * Retrieves all values of a specific type.
	 * 
	 * @param <T>  Value type
	 * @param base Value class
	 * @return Array of values of the specified class.
	 */
	@SuppressWarnings("unchecked")
	public <T> T[] getValues(Class<T> base) {
		ArrayList<T> values = new ArrayList<T>();
		for (MemoryValue<?> value : this.values) {
			if (value.isAssignableTo(base)) {
				values.add((T) value.getValue());
			}
		}
		return values.toArray(t -> (T[]) Array.newInstance(base, values.size()));
	}

	/**
	 * Retrieves the first value of a specific type
	 * 
	 * @param <T>  Value type
	 * @param base Value class
	 * @return Array of values of the specified class.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getValue(Class<T> base) {
		for (MemoryValue<?> value : this.values) {
			if (value.isAssignableTo(base)) {
				return (T) value.getValue();
			}
		}
		return null;
	}

	/**
	 * Retrieves the first value of a specific type and name
	 * 
	 * @param <T>  Value type
	 * @param name Value name
	 * @param base Value class
	 * @return Array of values of the specified class.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getValue(String name, Class<?> base) {
		for (MemoryValue<?> value : this.values) {
			if (value.isAssignableTo(base) && value.name.equals(name)) {
				return (T) value.getValue();
			}
		}
		return null;
	}

	/**
	 * Removes a value by name
	 * 
	 * @param name Value name
	 */
	public void removeValue(String name) {
		for (MemoryValue<?> value : this.values) {
			if (value.name.equals(name)) {
				values.remove(value);
				return;
			}
		}
	}
}

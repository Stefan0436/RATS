package org.asf.rats.events.internal;

import java.util.Iterator;

import org.asf.cyan.api.events.core.IEventListener;

public class ListenerList implements Iterable<IEventListener> {
	public class Cell implements Iterator<IEventListener> {
		public IEventListener listener;
		public Cell next;

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public IEventListener next() {
			Cell itm = next;
			next = next.next;
			return itm.listener;
		}
	}

	private Cell main;

	@Override
	public Iterator<IEventListener> iterator() {
		Cell cl = new Cell();
		cl.next = main;
		return cl;
	}

	public void add(IEventListener listener) {
		Cell cEntry = main;
		while (cEntry != null) {
			if (cEntry.next == null)
				break;

			cEntry = cEntry.next;
		}

		if (main == null) {
			main = new Cell();
			main.listener = listener;
			return;
		}

		cEntry.next = new Cell();
		cEntry = cEntry.next;

		cEntry.listener = listener;
	}

}

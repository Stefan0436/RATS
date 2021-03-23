package org.asf.rats;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.asf.aos.util.service.extra.slib.util.ArrayUtil;
import org.asf.rats.service.packet.PacketBuilder;
import org.asf.rats.service.packet.PacketEntry;
import org.asf.rats.service.packet.PacketParser;
import org.junit.Test;

public class PacketBuilderTest {

	Random rnd = new Random();

	public String genText() {
		Character[] chars = ArrayUtil.buildArray('0', ArrayUtil.rangingNumeric('1', '9', true, true),
				ArrayUtil.rangingNumeric('a', 'z', true, true));
		String txt = "";

		for (int i = 0; i < rnd.nextInt(100000); i++) {
			txt += chars[rnd.nextInt(chars.length)];
		}
		return txt;
	}

	@Test
	public void mainTest() {
		ArrayList<String> texts = new ArrayList<String>();
		int length = rnd.nextInt(200);
		for (int i = 0; i < length; i++) {
			texts.add(genText());
		}
		PacketBuilder builder = new PacketBuilder();
		for (int i = 0; i < length; i++) {
			builder.add(texts.get(i));
		}

		int otherpackets = rnd.nextInt(200);
		ArrayList<Object> objs = new ArrayList<Object>();
		for (int i = 0; i < otherpackets; i++) {
			int r = rnd.nextInt(5);
			if (r == 0)
				objs.add(rnd.nextDouble());
			else if (r == 1)
				objs.add(rnd.nextFloat());
			else if (r == 2)
				objs.add(rnd.nextBoolean());
			else if (r == 3)
				objs.add(rnd.nextInt());
			else if (r == 4) {
				byte[] dat = new byte[rnd.nextInt(20)];
				rnd.nextBytes(dat);
				objs.add(dat);
			} else
				objs.add(rnd.nextLong());
		}
		for (int i = 0; i < otherpackets; i++) {
			builder.add(objs.get(i));
		}
		byte[] data = builder.build();
		PacketParser parser = new PacketParser();
		parser.importArray(data);
		for (int i = 0; i < length; i++) {
			assertTrue(parser.getEntries()[i].get().toString().equals(texts.get(i)));
		}
		int i2 = 0;
		for (int i = length; i < length + otherpackets; i++) {
			PacketEntry<?> ent = parser.getEntries()[i];
			if (ent.get() instanceof byte[]) {
				assertTrue(Arrays.equals((byte[]) ent.get(), (byte[]) objs.get(i2)));
			} else if (ent.get() instanceof String) {
				assertTrue(ent.get().equals(objs.get(i2)));
			} else {
				assertEquals(ent.get(), objs.get(i2));
			}

			i2++;
		}
	}

}

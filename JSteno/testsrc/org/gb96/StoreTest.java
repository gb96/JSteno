package org.gb96;
import static org.junit.Assert.*;

import org.gb96.Store;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 */

/**
 * @author BowerinG
 *
 */
public class StoreTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
//		for (int i = 0; i < 256; i++) {
//			StringBuilder sb = new StringBuilder();
//			sb.append("i = ");
//			sb.append(i);
//			sb.append(' ');
//			byte[] b = new byte[] {(byte) i};
//			sb.append(toSigned(b[0]));
//			sb.append(" -> ");
//			Store.decrementUnsigned(b, 0);
//			sb.append(toSigned(b[0]));
//			System.out.println(sb.toString());
//		}
//		System.out.println();
	}

	/**
	 * Test method for {@link Store#makeDistinct(byte[], int)}.
	 */
	@Test
	public void testMakeDistinct() {
		testDistinct(new byte[] {0, 0, 0});
		testDistinct(new byte[] {1, 2, 3});
		testDistinct(new byte[] {0, 0, 1});
		testDistinct(new byte[] {0, 1, 1});
		testDistinct(new byte[] {(byte) 255, (byte) 255, (byte) 254});
		testDistinct(new byte[] {(byte) 255, (byte) 255, (byte) 0});
		testDistinct(new byte[] {-128, -128, -128});
		testDistinct(new byte[] {(byte) 255, (byte) 255, (byte) 255});
		
		testUnsignedAvg((byte)0, (byte)0);
		testUnsignedAvg((byte)0, (byte)1);
		testUnsignedAvg((byte)0, (byte)2);
		testUnsignedAvg((byte)2, (byte)4);
		testUnsignedAvg((byte)4, (byte)2);
		testUnsignedAvg((byte)255, (byte)255);
		testUnsignedAvg((byte)255, (byte)0);
		testUnsignedAvg((byte)0, (byte)128);
		testUnsignedAvg((byte)253, (byte)255);
	}
	
	private static void testDistinct(final byte[] arr) {
		final String original = toString(arr, 0);
		Store.makeDistinct(arr, 0);
		System.out.println(original + " -> " + toString(arr, 0));
		assertDistinct(arr, 0);
	}

	private static void testUnsignedAvg(final byte b1, final byte b2) {
		final String original = "(" + toSigned(b1) + ", " + toSigned(b2) + ")";
		byte avg = Store.unsignedAvg(b1, b2);
		System.out.println(original + " Avg-> " + toSigned(avg));
	}

	static void assertDistinct(final byte[] arr, final int i) {
		final byte d0 = arr[i];
		final byte d1 = arr[i + 1];
		final byte d2 = arr[i + 2];
		assertTrue(d0 != d1);
		assertTrue(d1 != d2);
		assertTrue(d0 != d2);
	}
	
	static String toString(final byte[] arr, final int i) {
		final byte d0 = arr[i];
		final byte d1 = arr[i + 1];
		final byte d2 = arr[i + 2];
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		sb.append(toSigned(d0));
		sb.append(", ");
		sb.append(toSigned(d1));
		sb.append(", ");
		sb.append(toSigned(d2));
		sb.append(')');
		return sb.toString();
	}
	
	static int toSigned(final byte b) {
		if (b >= 0) {
			return b;
		}
		return 256 + b;
	}
}

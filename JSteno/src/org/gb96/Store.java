package org.gb96;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Random;

import javax.imageio.ImageIO;

/**
 * 
 */

/**
 * @author BowerinG
 * 
 */
public class Store {
	static final Random RANDOM = new Random();
	
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		if (args == null || args.length != 2) {
			System.err.println("Usage: Store dataFile vesselImageFile");
			return;
		}
		
		final String imgInputFilename = args[1];
		final File imgFile = new File(imgInputFilename);

		final String dataFilename = args[0];
		
		final BufferedImage bi = ImageIO.read(imgFile);

		final RandomAccessFile fileHandle = new RandomAccessFile(dataFilename, "r");
		final FileChannel fileChannel = fileHandle.getChannel();
        final ByteBuffer buf = ByteBuffer.allocateDirect((int) fileHandle.length() + 8 + dataFilename.length() + 1);
        buf.order(ByteOrder.nativeOrder());
        fileChannel.position(0);
        // store filename and length.
        buf.put(dataFilename.getBytes()); // filename
        buf.put((byte) 0); // terminator for filename string
        buf.putLong(fileHandle.length()); // length
        
        fileChannel.read(buf);
        fileHandle.close();
        buf.rewind();
        storeMethod1(buf, bi);
		
		// Write out to file
		ImageIO.write(bi, "png", new File("out.png"));
	}

	/**
	 * Method 1 places data in a single color component for a pixel that is marked by having
	 * the other two color components matched.
	 * </br>
	 * All other pixels are modified so that no two color components match.
	 * 
	 * @param buf
	 * @param bi
	 */
	private static void storeMethod1(final ByteBuffer buf, final BufferedImage bi) {
		final WritableRaster raster = bi.getRaster();
		final DataBufferByte dataBuffer = (DataBufferByte)raster.getDataBuffer();
//		System.out.println("dataBuffer = " + dataBuffer);
		int numBanks = dataBuffer.getNumBanks();
		System.out.println("image dataBuffer NumBanks=" + numBanks);
		
		System.out.println("data buf position=" + buf.position() + " limit=" + buf.limit());

		byte[] data = dataBuffer.getData(); // first bank
		System.out.println("dataBuffer Bank1 length=" + data.length);
		
		final int messageVesselRatio = data.length / 3 / buf.capacity();
		System.out.println("messageVesselRatio=" + messageVesselRatio);
		
		if (messageVesselRatio < 5) {
			System.err.println("Vessel is too small to carry message. Vessel:Message = " + messageVesselRatio + "\n"
					+ "Please choose a vessel that is at least " + ( 5 / messageVesselRatio) + " larger.");
			// Message not stored.
			return;
		}
		
		final int encodingFrequency = 4 * messageVesselRatio / 5;
		System.out.println("encodingFrequency=" + encodingFrequency);
		
		for (int i = 0; i < data.length; i += 3) {
			final byte blue = data[i];
			final byte green = data[i + 1];
			final byte red = data[i + 2];
			
			if (buf.hasRemaining() && RANDOM.nextInt(encodingFrequency) == 0) {
				// store data this pixel
				switch (RANDOM.nextInt(3)) {
				case 0:
					// store in blue
					data[i] = buf.get();
//					System.out.println(buf.position() + " blue : " + data[i]);
					byte avgGR = unsignedAvg(green, red);
					if (avgGR == data[i]) {
						if (avgGR != 0) {
							avgGR --;
						} else {
							avgGR++;
						}
					}
					data[i + 1] = avgGR;
					data[i + 2] = avgGR;
					break;
				case 1:
					// store in green
					data[i + 1] = buf.get();
//					System.out.println(buf.position() + " green : " + data[i + 1]);
					byte avgBR = unsignedAvg(blue, red);
					if (avgBR == data[i + 1]) {
						if (avgBR != 0) {
							avgBR --;
						} else {
							avgBR++;
						}
					}
					data[i + 0] = avgBR;
					data[i + 2] = avgBR;
					break;
				case 2:
					// store in red
					data[i + 2] = buf.get();
//					System.out.println(buf.position() + " red : " + data[i + 2]);
					byte avgBG = unsignedAvg(blue, green);
					if (avgBG == data[i + 2]) {
						if (avgBG != 0) {
							avgBG --;
						} else {
							avgBG++;
						}
					}
					data[i + 0] = avgBG;
					data[i + 1] = avgBG;
					break;
				}
			} else {
				// adjust background pixels
				makeDistinct(data, i);
			}
		}
		if (buf.hasRemaining()) {
			System.err.println("Failed to store message in vessel.  Remaining bytes=" + buf.remaining());
		}
	}
	
	/**
	 * Method 2 uses the lower 3 bits of each colour component to store 8 bits of data
	 * plus one bit to flag the presence of data.
	 * </br>
	 * All other pixels have the flag bit cleared.
	 * </br>
	 * NOTE this method is not yet properly tested and has no corresponding load implementation yet.
	 * 
	 * @param buf
	 * @param bi
	 */
	private static void storeMethod2(final ByteBuffer buf, final BufferedImage bi) {
		final WritableRaster raster = bi.getRaster();
		final DataBufferByte dataBuffer = (DataBufferByte)raster.getDataBuffer();
//		System.out.println("dataBuffer = " + dataBuffer);
		int numBanks = dataBuffer.getNumBanks();
		System.out.println("image dataBuffer NumBanks=" + numBanks);
		
		System.out.println("data buf position=" + buf.position() + " limit=" + buf.limit());

		byte[] data = dataBuffer.getData(); // first bank
		System.out.println("dataBuffer Bank1 length=" + data.length);
		
		final int messageVesselRatio = data.length / 3 / buf.capacity();
		System.out.println("messageVesselRatio=" + messageVesselRatio);
		
		if (messageVesselRatio < 5) {
			System.err.println("Vessel is too small to carry message. Vessel:Message = " + messageVesselRatio + "\n"
					+ "Please choose a vessel that is at least " + ( 5 / messageVesselRatio) + " larger.");
			// Message not stored.
			return;
		}
		
		final int encodingFrequency = 4 * messageVesselRatio / 5;
		System.out.println("encodingFrequency=" + encodingFrequency);
		
		for (int i = 0; i < data.length; i += 3) {
			final byte blue = data[i];
			final byte green = data[i + 1];
			final byte red = data[i + 2];
			
			if (buf.hasRemaining() && RANDOM.nextInt(encodingFrequency) == 0) {
				// store data this pixel
				final byte d = buf.get();
				data[i] = (byte) ((blue & 0b1111_1000) | d >>> 5);
				data[i + 1] = (byte) ((green & 0b1111_1000) | (d >>> 2) & 0b0000_0111);
				data[i + 2] = (byte) ((red & 0b1111_1000) | ((d  & 0b0000_0011) << 1) | 1);
			} else {
				// adjust background pixels
				flagUnused(data, i);
			}
		}
		if (buf.hasRemaining()) {
			System.err.println("Failed to store message in vessel.  Remaining bytes=" + buf.remaining());
		}
	}


	static byte unsignedAvg(final byte b1, final byte b2) {
		final int avg = (toSigned(b1) + toSigned(b2)) / 2;
		return (byte) avg;
	}

	static void makeDistinct(final byte[] data, final int i) {
		final byte d0 = data[i];
		final byte d1 = data[i + 1];
		final byte d2 = data[i + 2];
		
		if (d0 != d1 && d0 != d2 && d1 != d2) return;
		if (d0 == d1 && d1 == d2) {
			if (d0 != 0 && d0 != 1) {
				data[i] -= 2;
				data[i + 1] --;
			} else {
				data[i] += 2;
				data[i + 1] ++;
			}
			return;
		}
		if (d0 == d1) {
			if (d0 != 0 && d0 - 1 != d2) {
				data[i] --;
			} else if (d0 != -1 && d0 + 1 != d2) {
				data[i] ++;
			} else if (d0 == -1) { // 255, 255, 254
				data[i] -= 2;
			} else { // 0, 0, 1
				data[i] += 2;
			}
			return;
		}
		if (d1 == d2) {
			if (d1 != 0 && d1 - 1 != d0) {
				data[i + 1] --;
			} else if (d1 != -1 && d1 + 1 != d0) {
				data[i + 1] ++;
			} else if (d1 == -1) {
				data[i + 1] -= 2;
			} else {
				data[i + 1] += 2;
			}
			return;
		}
		// d0 == d2
		if (d0 != 0 && d0 - 1 != d1) {
			data[i] --;
		} else if (d0 != -1 && d0 + 1 != d1) {
			data[i] ++;
		} else if (d0 == -1) {
			data[i] -= 2;
		} else {
			data[i] += 2;
		}
		
		assert data[i] != data[i + 1] && data[i] != data[i + 2] && data[i + 1] != data[i + 2];
	}

	static int toSigned(final byte b) {
		if (b >= 0) {
			return b;
		}
		return 256 + b;
	}

	private static void flagUnused(final byte[] data, final int i) {
		// clear the lowest order bit in the 3rd color component:
		data[i + 2] = (byte) (data[i + 2] & 0b1111_1110);
	}

}

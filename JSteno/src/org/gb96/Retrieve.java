package org.gb96;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import javax.imageio.ImageIO;

/**
 * 
 */

/**
 * @author BowerinG
 * 
 */
public class Retrieve {
	static final int BUF_SIZE = 200;
	
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final String imgInputFilename = "out.png";
		final File imgFile = new File(imgInputFilename);
		
		final BufferedImage bi = ImageIO.read(imgFile);
		        
        loadMethod1(bi);
	}

	private static void loadMethod1(final BufferedImage bi) throws IOException {
		final WritableRaster raster = bi.getRaster();
		final DataBufferByte dataBuffer = (DataBufferByte)raster.getDataBuffer();
		int numBanks = dataBuffer.getNumBanks();
		System.out.println("dataBuffer NumBanks=" + numBanks);
		
		byte[] data = dataBuffer.getData(); // first bank
		System.out.println("dataBuffer Bank1 length=" + data.length);
		ByteBuffer buf = ByteBuffer.allocateDirect(BUF_SIZE);
	    buf.order(ByteOrder.nativeOrder());

		String dataFilename = null;
		long dataFileLength = -1;
	    int state = 0;
	    
		for (int i = 0; i < data.length; i += 3) {
			final byte blue = data[i];
			final byte green = data[i + 1];
			final byte red = data[i + 2];
			
			byte d;
			
			if (blue == green) {
				// data stored in red
				d = red;
			} else if (green == red) {
				// data stored in blue
				d = blue;
			} else if (blue == red) {
				// data stored in green
				d = green;
			} else {
				// no data stored this pixel
				continue;
			}
			
			buf.put(d);
			if (state == 0 && d == 0) {
				// finished reading filename
				state = 1;
				buf.flip();
				byte[] filenameBytes = new byte[buf.limit() - 1]; // omit nul terminator
				buf.get(filenameBytes);
				buf.clear();
				dataFilename = new String(filenameBytes);
				while (new File(dataFilename).exists()) {
					// rename to avoid overwrite existing
					dataFilename = dataFilename + "_";
				}
				System.out.println("data filename=" + dataFilename);
			} else if (state == 1 && buf.position() == 8) {
				// finished reading file length
				state = 2;
				buf.flip();
				dataFileLength = buf.getLong();
				System.out.println("data file length=" + dataFileLength);
				buf.clear();
				// Allocate new big buffer.  Old buffer becomes garbage.
				buf = ByteBuffer.allocateDirect((int) dataFileLength);
			    buf.order(ByteOrder.nativeOrder());
			}
			
		}
		buf.flip();

		if (dataFilename == null) {
			System.err.println("Could not extract data filename");
			return;
		}
		if (dataFileLength < 0) {
			System.err.println("Could not extract data file length");
			return;
		}
		
		final FileOutputStream fos = new FileOutputStream(dataFilename);
		final FileChannel wChannel = fos.getChannel();


		final int written = wChannel.write(buf);
		System.out.println("wrote " + written + " bytes to " + dataFilename);
		fos.close();
	}
	
}

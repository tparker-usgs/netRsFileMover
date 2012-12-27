package gov.usgs.NetRSFileMover;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

public class ThrottledOutputStream extends OutputStream {

	private final long bytesPerSecond;
	private long lastWrite;
	private long bytesWriten;
	private long bytesPerWindow;
	private static long timeWindowMS;
	private OutputStream outStream;
	
	private final static Logger LOGGER = Logger.getLogger(NetRSSettings.class .getName()); 

	ThrottledOutputStream(OutputStream outStream, long bytesPerSecond) {
		
		LOGGER.finest("constructing ThrottledOutputStream");
		this.bytesPerSecond = bytesPerSecond;
		this.outStream = outStream;
		lastWrite = System.currentTimeMillis();

		timeWindowMS = 250;
		bytesPerWindow = (bytesPerSecond/1000) * timeWindowMS;
	}
	
	
	@Override
	public void write(int b) throws IOException {
		
		outStream.write(b);
		bytesWriten += 4;
		
		if (bytesWriten > bytesPerWindow)
		{
			bytesWriten = 0;
			
			long now = System.currentTimeMillis();
			long sleep = timeWindowMS - (now - lastWrite);
			lastWrite = now;
			try {
				if (sleep > 0)
					Thread.sleep(sleep);
			} catch (InterruptedException e) {}
		}
	}
}

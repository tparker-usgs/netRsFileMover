package gov.usgs.NetRSFileMover;


import gov.usgs.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;

/**
 * A class to hold a FTP connection to a NetRS
 * 
 * @author Tom Parker
 * 
 */
public class NetRSConnection {
	private final static Logger LOGGER = Log.getLogger(NetRSConnection.class
			.getName());

	private static int ONE_MINUTE = 1000 * 60;
	private static int ONE_DAY = ONE_MINUTE * 60 * 24;

	private NetRSSettings settings;
	private long pollTime;
	private final long endTime;
	private final long oldestPoll;
	private FTPClient ftp;
	private SimpleDateFormat fileNameFormat;

	public NetRSConnection(NetRSSettings settings) {

		this.settings = settings;
		
		long startTime = System.currentTimeMillis();
		endTime = startTime + (settings.duration * ONE_MINUTE);
		pollTime = startTime - (startTime % (settings.duration * ONE_MINUTE));
		oldestPoll = startTime - (settings.maxBackfill * ONE_DAY);
		
		fileNameFormat = new SimpleDateFormat(settings.fileNameFormat);
		fileNameFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		LOGGER.info("first poll at "
				+ fileNameFormat.format(new Date(pollTime)));

		ftp = new FTPClient();
		
		ftp.setConnectTimeout(settings.connectTimeout * 1000);
		if (settings.printHash)
			ftp.setCopyStreamListener(getCopyStreamListener());
	}

	private void connect() throws IOException {

		LOGGER.info("Connecting to " + settings.address);
		try {
			System.out.println("bps " + settings.bytesPerSecond);
			if (settings.bytesPerSecond > 0)
				ftp.setReceiveBufferSize(settings.bytesPerSecond/4);
			
			ftp.connect(settings.address);
			ftp.login(settings.userName, settings.password);
			ftp.enterLocalPassiveMode();
			
		} catch (IOException e) {
			if (ftp.isConnected())
				ftp.disconnect();

			throw e;
		}
	}

	/**
	 * Poll the next file from the receiver, updating pollTime
	 * 
	 * @throws IOException
	 */
	public void poll() {

		// Stop polling if I've been running too long.
		if (System.currentTimeMillis() > endTime) {
			pollTime = Long.MIN_VALUE;
			LOGGER.info("I've been running too long. I'll stop");
			return;
		}

		String filename = fileNameFormat.format(new Date(pollTime));
		LOGGER.fine("Polling " + settings.systemName + " for " + filename);

		pollTime -= settings.duration * ONE_MINUTE;
		OutputStream output = null;
		boolean result = false;
		File out = new File(settings.outputDir + File.separator
				+ settings.systemName + File.separator + filename);

		// Just return if I already have the file or have gone too far back
		if (out.exists()) {
			LOGGER.fine("I already have " + filename + " skipping it.");
			return;
		} else {
			LOGGER.fine("I don't have " + filename + " looking for it.");
		}

		try {
			connect();
			out.getParentFile().mkdirs();
			output = new FileOutputStream(out);
			if (settings.bytesPerSecond > 0)
				output = new ThrottledOutputStream(output,
						settings.bytesPerSecond);
			long now = System.currentTimeMillis();
			result = ftp.retrieveFile(filename, output);
			if (result)
				LOGGER.fine("got file in " + (System.currentTimeMillis() - now)
						+ " ms");
			else
				LOGGER.fine("didn't get file.");

		} catch (IOException e) {
			LOGGER.info(e.toString());
			LOGGER.info("Cannot retreive " + filename
					+ ". Undeterred I will continue.");
		} finally {
			if (output != null)
				try {
					output.close();
					if (!result)
						out.delete();
					ftp.disconnect();
				} catch (IOException e) {
				}
		}

		if (settings.depthFirst && !polledLast())
			poll();
	}

	/**
	 * Decide if I've checked for all files.
	 * 
	 * @return true if I've polled all files
	 */
	public boolean polledLast() {
		return pollTime < oldestPoll;
	}

	/**
	 * create a CopyStreamListener to print hash marks durring downloads. Based
	 * on FTPClient example code.
	 * 
	 * @return a hash-printing CopyStreamListener
	 */
	private static CopyStreamListener getCopyStreamListener() {
		return new CopyStreamListener() {
			private long kBytesTotal = 0;

			public void bytesTransferred(CopyStreamEvent event) {
				bytesTransferred(event.getTotalBytesTransferred(),
						event.getBytesTransferred(), event.getStreamSize());
			}

			public void bytesTransferred(long totalBytesTransferred,
					int bytesTransferred, long streamSize) {
				long kBytes = totalBytesTransferred / (1024);
				for (long l = kBytesTotal; l < kBytes; l++) {
					System.out.print("#");
				}
				kBytesTotal = kBytes;
			}
		};
	}
}

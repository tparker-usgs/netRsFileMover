package gov.usgs.NetRSFileMover;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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
	private NetRSSettings settings;
	private long pollTime;
	private final long startTime;
	private FTPClient ftp;
	private SimpleDateFormat fileNameFormat;

	public NetRSConnection(NetRSSettings settings) {

		this.settings = settings;
		startTime = System.currentTimeMillis();
		pollTime = startTime - (startTime % (settings.duration * 1000 * 60));

		
		fileNameFormat = new SimpleDateFormat(settings.fileNameFormat);
		fileNameFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		System.out.println("first poll at " + fileNameFormat.format(new Date(pollTime)));
		
		ftp = new FTPClient();
		ftp.setControlKeepAliveReplyTimeout(3 * 1000);
		ftp.setControlKeepAliveTimeout(30 * 1000);
		ftp.setConnectTimeout(settings.connectTimeout * 1000);
		if (settings.printHash)
			ftp.setCopyStreamListener(getCopyStreamListener());
	}

	private void connect() throws IOException {
		if (ftp != null && ftp.isConnected())
			return;

		System.out.println("Connecting to " + settings.address);
		try {
			ftp.connect(settings.address);
			System.out.println("trying " + settings.userName + " " + settings.password);
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
		if (System.currentTimeMillis() > startTime + settings.duration * 1000
				* 60) {
			pollTime = Long.MIN_VALUE;
			System.out.println("I've been running too long. I'll stop");
			return;
		}

		String filename = fileNameFormat.format(new Date(pollTime));
		System.out.println("Polling " + settings.systemName + " for " + filename);


		pollTime -= settings.duration * 1000 * 60;
		FileOutputStream output = null;
		boolean result = false;
		File out = new File(settings.outputDir + File.separator + filename);

		// Just return if I already have the file or have gone too far back
		if (out.exists()) {
			System.out.println("no need to pull " + filename + " skipping it.");
			return;
		}
		
		try {
			connect();
			out = new File(settings.outputDir + File.separator + filename);
			out.getParentFile().mkdirs();
			
			output = new FileOutputStream(out);
			result = ftp.retrieveFile(filename, output);
			
		} catch (IOException e) {
			System.err.println(e);
			System.err.println("Cannot retreive " + filename
					+ ". Undeterred I will continue.");
		} finally {
			if (output != null)
				try {
					output.close();
					if (!result)
						out.delete();
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
		return pollTime < startTime
				- (settings.maxBackfill * 1000 * 60 * 60 * 24);
	}

	/**
	 * create a CopyStreamListener to print hash marks durring downloads. Based
	 * on FTPClient example code.
	 * 
	 * @return a hash-printing CopyStreamListener
	 */
	private static CopyStreamListener getCopyStreamListener() {
		return new CopyStreamListener() {
			private long mBytesTotal = 0;

			public void bytesTransferred(CopyStreamEvent event) {
				bytesTransferred(event.getTotalBytesTransferred(),
						event.getBytesTransferred(), event.getStreamSize());
			}

			public void bytesTransferred(long totalBytesTransferred,
					int bytesTransferred, long streamSize) {
				long mBytes = totalBytesTransferred / (1024 * 1024);
				for (long l = mBytesTotal; l < mBytes; l++) {
					System.out.print("#");
				}
				mBytesTotal = mBytes;
			}
		};
	}
}

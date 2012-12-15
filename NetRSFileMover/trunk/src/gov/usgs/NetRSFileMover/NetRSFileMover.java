package gov.usgs.NetRSFileMover;


import gov.usgs.util.ConfigFile;
import gov.usgs.util.Util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;

/**
 * Retrieve files from a Trimble NetRS device via FTP
 * 
 * @author Tom Parker
 */
public final class NetRSFileMover
{
	
	private boolean printHash;
	private ConfigFile cf;
	private FTPClient ftp;
	private ConfigFile configDefault;
	private List<NetRSSettings> receiverSettings;
	
	public NetRSFileMover(ConfigFile cf)
	{
		configDefault = cf.getSubConfig("default");
		List<String> receivers = cf.getList("receivers");
		
		for (String receiver : receivers)
			receiverSettings.add(new NetRSSettings(receiver, configDefault.getSubConfig(receiver, true)));
		
		ftp = new FTPClient();
        if (printHash) {
            ftp.setCopyStreamListener(getCopyStreamListener());
        }
	}
		
	private void go() {
		
//		long now = (System.currentTimeMillis() / 1000 / 60) / duration;
//		long time = now;
//		while (time > (now - maxBackfill / 24 / 60))
//		{
//			getFile(time);
//			time -= duration;
//		}
	}
	
	private void getFile(long time) {
		
//		StringBuilder format = new StringBuilder();
//		format.append("/yyyyMM/");
//		
//		if (usePerDaySubdirectories)
//			format.append("dd/");
//		
//		if (usePerSessionIdSubdirectories)
//			format.append("'" + sessionId + "'/");
//		
//		format.append("'" + systemName + "'yyyyMMddhhMM'" + sessionId + "." + dataFormat);
//		
//		for (String receiver : receivers)
//		{
//			try {
//				ftp.connect(receiver);
//			} catch (IOException e) {
//		        if (ftp.isConnected())
//		        {
//		            try {
//		                ftp.disconnect();
//		            } catch (IOException f) {}
//		        }
//		        System.err.println("Could not connect to " + receiver + ".");
//			}
//			
//			int reply = ftp.getReplyCode();
//			System.out.println("Connected to " + receiver);
//			if (!FTPReply.isPositiveCompletion(reply))
//			{
//				try {
//					ftp.disconnect();
//				} catch (IOException e) {}
//				
//				System.err.println("FTP server refused connection.");
//				System.exit(1);
//			}
//		}
	}
	
	public static void main(String[] args) throws UnknownHostException {
		ConfigFile cf =  new ConfigFile(args[0]);
		NetRSFileMover arch = new NetRSFileMover(cf);
		arch.go();
    }
	
    private static CopyStreamListener getCopyStreamListener() {
        return new CopyStreamListener(){
            private long mBytesTotal = 0;
            public void bytesTransferred(CopyStreamEvent event) {
                bytesTransferred(event.getTotalBytesTransferred(), event.getBytesTransferred(), event.getStreamSize());
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


package gov.usgs.NetRSFileMover;

import gov.usgs.util.ConfigFile;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Retrieve files from a Trimble NetRS device via FTP
 * 
 * @author Tom Parker
 */
public final class NetRSFileMover {
	public static final boolean DEFAULT_BACKFILL_FIRST = true;
	public static final int DEFAULT_MAX_RUNTIME = 1440;
	public static final long ONE_DAY = 1000 * 60 * 60 * 24;

	private final static Logger LOGGER = Logger.getLogger(NetRSFileMover.class .getName()); 
	
	private List<NetRSConnection> receivers;

	/**
	 * simple constructor
	 * 
	 * @param cf
	 */
	public NetRSFileMover(ConfigFile cf) {

		List<String> receiverList = cf.getList("receiver");
		receivers = new LinkedList<NetRSConnection>();
		
		if (receiverList == null || receiverList.size() == 0)
			throw new RuntimeException("Didn't find any receiver directives.");	
		
		for (String receiver : cf.getList("receiver")) {
			
			NetRSSettings settings = new NetRSSettings(receiver,
					cf.getSubConfig(receiver, true));
			
			NetRSConnection connection = new NetRSConnection(settings);
			receivers.add(connection);
		}
	}

	/**
	 * Do the work
	 */
	private void go() {
		while (!receivers.isEmpty()) {
			Iterator<NetRSConnection> it = receivers.iterator();
			while (it.hasNext()) {
				NetRSConnection receiver = it.next();
				receiver.poll();
				
				if (receiver.polledLast())
					it.remove();
			}
		}
	}

	/**
	 * Main method. Always a good place to start.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		if (args.length != 1)
			throw new RuntimeException("Usage: NetRSFileMover <config>");

		ConfigFile cf = new ConfigFile(args[0]);
		
		Logger.getLogger("gov.usgs.NetRSFileMover");
		LogManager.getLogManager().getLogger("gov.usgs.NetRSFileMover").setLevel(Level.FINEST);
		
		if (!cf.wasSuccessfullyRead())
			throw new RuntimeException("Can't read config file " + args[0]);

		NetRSFileMover arch = new NetRSFileMover(cf);
		arch.go();
		
		LOGGER.info("Got everything I'm going to get. Exiting.");
	}
}

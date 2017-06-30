package gov.usgs.volcanoes.netRSFileMover;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.volcanoes.core.configfile.ConfigFile;

/**
 * Retrieve files from a Trimble NetRS device via FTP
 * 
 * TODO: add option to resume file transfers
 * 
 * @author Tom Parker
 */
public final class NetRSFileMover {
	public static final boolean DEFAULT_BACKFILL_FIRST = true;
	public static final int DEFAULT_MAX_RUNTIME = 60 * 60 * 24;
	public static final int ONE_DAY = 1000 * 60 * 60 * 24;

	private static final Logger LOGGER = LoggerFactory.getLogger(NetRSFileMover.class);

	private List<NetRSConnection> receivers;

	/**
	 * simple constructor
	 * 
	 * @param configFile
	 */
	public NetRSFileMover(ConfigFile configFile) {

		List<String> receiverList = configFile.getList("receiver");
		if (receiverList == null || receiverList.size() == 0) {
			System.err
					.println("I didn't find any receiver directives. Guess I'm done.");
			System.exit(1);
		}

		receivers = new LinkedList<NetRSConnection>();
		for (String receiverName : configFile.getList("receiver")) {
			NetRSSettings settings = new NetRSSettings(receiverName,
					configFile.getSubConfig(receiverName, true));

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
		
		if (args.length != 1) {
			System.err.println("Usage: NetRSFileMover <config>");
			System.exit(1);
		}

		ConfigFile cf = new ConfigFile(args[0]);
		if (!cf.wasSuccessfullyRead()) {
			System.err.print("Can't read config file " + args[0]);
			System.exit(1);
		}

		NetRSFileMover arch = new NetRSFileMover(cf);
		arch.go();

		LOGGER.info("Got everything I'm going to get. Exiting.");
	}
}

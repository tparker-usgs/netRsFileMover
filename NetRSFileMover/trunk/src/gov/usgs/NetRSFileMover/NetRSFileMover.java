package gov.usgs.NetRSFileMover;

import gov.usgs.util.ConfigFile;

import java.util.List;

/**
 * Retrieve files from a Trimble NetRS device via FTP
 * 
 * @author Tom Parker
 */
public final class NetRSFileMover {
	public static final boolean DEFAULT_BACKFILL_FIRST = true;
	public static final int DEFAULT_MAX_RUNTIME = 1440;
	public static final long ONE_DAY = 1000 * 60 * 60 * 24;

	private List<NetRSConnection> receivers;

	/**
	 * simple constructor
	 * 
	 * @param cf
	 */
	public NetRSFileMover(ConfigFile cf) {
		
		for (String receiver : cf.getList("receivers")) {
			
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
			for (NetRSConnection receiver : receivers) {
				receiver.poll();

				if (receiver.polledLast())
					receivers.remove(receiver);
			}
		}
	}

	/**
	 * Main method. Always a good place to start.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		ConfigFile cf = new ConfigFile(args[0]);
		NetRSFileMover arch = new NetRSFileMover(cf);
		arch.go();
	}
}

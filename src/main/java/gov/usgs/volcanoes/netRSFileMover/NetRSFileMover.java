package gov.usgs.volcanoes.netRSFileMover;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.martiansoftware.jsap.ParseException;

import gov.usgs.volcanoes.core.configfile.ConfigFile;
import gov.usgs.volcanoes.core.time.TimeSpan;

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
   * @throws ParseException
   */
  public NetRSFileMover(ConfigFile configFile, List<String> selectedReceivers) throws ParseException {

    List<String> receiverList = configFile.getList("receiver");
    if (receiverList == null || receiverList.size() == 0) {
      System.err.println("I didn't find any receiver directives. Guess I'm done.");
      System.exit(1);
    }

    receivers = new LinkedList<NetRSConnection>();
    System.out.println("TOMP: " + selectedReceivers);
    for (String receiverName : configFile.getList("receiver")) {
      if (selectedReceivers != null && !selectedReceivers.contains(receiverName)) {
        System.out.println("Skipping " + receiverName);
        continue;
      }
      NetRSSettings settings =
          new NetRSSettings(receiverName, configFile.getSubConfig(receiverName, true));

      NetRSConnection connection = new NetRSConnection(settings);
      receivers.add(connection);
    }
  }


  private void setTimeSpan(TimeSpan timeSpan) {
    for (NetRSConnection connection : receivers) {
      connection.setTimeSpan(timeSpan);
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
  public static void main(String[] args) throws Exception {
    final NetRSFileMoverArgs config = new NetRSFileMoverArgs(args);

    ConfigFile cf = null;
    cf = new ConfigFile(config.configFileName);
    if (!cf.wasSuccessfullyRead()) {
      LOGGER.error("Couldn't find config file " + config.configFileName
          + ". Use '-c' to create an example config.");
      System.exit(1);
    }

    NetRSFileMover arch = new NetRSFileMover(cf, config.stations);
    if (config.timeSpan != null) {
      LOGGER.debug("Setting time span = {}", config.timeSpan);
      arch.setTimeSpan(config.timeSpan);
    }

    arch.go();

    LOGGER.info("Got everything I'm going to get. Exiting.");
  }

}

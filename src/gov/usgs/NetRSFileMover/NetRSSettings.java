package gov.usgs.NetRSFileMover;

import gov.usgs.util.ConfigFile;
import gov.usgs.util.Util;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Settings required to retrieve files from a single NetRS receiver
 * 
 * @author Tom Parker
 * 
 */
public class NetRSSettings {
	public static final String DEFAULT_USER = "netrsFTP";
	public static final boolean DEFAULT_DO_DELETE = false;
	public static final int DEFAULT_DURATION = 1440;
	public static final boolean DEFAULT_USE_PER_DAY_SUBDIRECTORIES = true;
	public static final boolean DEFAULT_USE_PER_SESSION_ID_SUBDIRECTORIES = true;
	public static final int DEFAULT_MAX_BACKFILL = 7;
	public static final boolean DEFAULT_PRINT_HASH = true;
	public static final String DEFAULT_SESSION_ID = "a";
	public static final String DEFAULT_DATA_FORMAT = "T00";
	public static final String DEFAULT_OUTPUT_DIR = "output";
	public static final boolean DEFAULT_DEPTH_FIRST = false;

	public final String userName;
	public final String password;
	public final boolean doDelete;
	public final int duration;
	public final boolean usePerDaySubdirectories;
	public final boolean usePerSessionIdSubdirectories;
	public final int maxBackfill;
	public final String systemName;
	public final String sessionId;
	public final String dataFormat;
	public final String address;
	public final boolean printHash;
	public final String fileNameFormat;
	public final String outputDir;
	public final boolean depthFirst;

	public NetRSSettings(String systemName, ConfigFile cf) {
		this.systemName = systemName;
		if (cf.getString("password") != null) {
			userName = cf.getString("userName");
			password = cf.getString("password");
		} else {
			userName = "anonymous";
			String hostname = null;
			try {
				hostname = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
			}
			password = System.getProperty("user.name") + "@" + hostname;
		}

		depthFirst = Util.stringToBoolean(cf.getString("depthFirst"),
				DEFAULT_DEPTH_FIRST);
		printHash = Util.stringToBoolean(cf.getString("printHash"),
				DEFAULT_PRINT_HASH);
		doDelete = Util.stringToBoolean(cf.getString("doDelete"),
				DEFAULT_DO_DELETE);
		duration = Util.stringToInt(cf.getString("duration"), DEFAULT_DURATION);
		usePerDaySubdirectories = Util.stringToBoolean(
				cf.getString("usePerDaySubdirectories"),
				DEFAULT_USE_PER_DAY_SUBDIRECTORIES);
		usePerSessionIdSubdirectories = Util.stringToBoolean(
				cf.getString("usePerSessionIdSubdirectories"),
				DEFAULT_USE_PER_SESSION_ID_SUBDIRECTORIES);
		maxBackfill = Util.stringToInt(cf.getString("maxBackfill"),
				DEFAULT_MAX_BACKFILL);

		sessionId = Util.stringToString(cf.getString("sessionId"),
				DEFAULT_SESSION_ID);
		if (!sessionId.matches("^a-z$"))
			throw new RuntimeException(
					"sessionId must be a single lowercase letter. " + sessionId
							+ " won't work.");

		dataFormat = Util.stringToString(cf.getString("dataFormat"),
				DEFAULT_DATA_FORMAT);
		if (!dataFormat.equals("T00") && !dataFormat.equals("Binex"))
			throw new RuntimeException(
					"dataFormat must be either T00 or Binex. " + dataFormat
							+ " doesn't cut it.");

		address = cf.getString("address");

		outputDir = Util.stringToString(cf.getString("outputDir"),
				DEFAULT_OUTPUT_DIR);
		File f = new File(outputDir);
		if (!f.exists())
			throw new RuntimeException("outputDir " + outputDir
					+ " doesn't exist. Won't try to pull files from "
					+ systemName);

		fileNameFormat = getFileNameFormat();
	}

	public String getFileNameFormat() {
		StringBuilder format = new StringBuilder();
		format.append("/yyyyMM/");

		if (usePerDaySubdirectories)
			format.append("dd/");

		if (usePerSessionIdSubdirectories)
			format.append("'" + sessionId + "'/");

		format.append("'" + systemName + "'yyyyMMddhhMM'" + sessionId + "."
				+ dataFormat);

		return format.toString();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("userName = " + userName + "\n");
		sb.append("password = " + password + "\n");
		sb.append("doDelete = " + doDelete + "\n");
		sb.append("duration = " + duration + "\n");
		sb.append("usePerDaySubdirectories = " + usePerDaySubdirectories + "\n");
		sb.append("usePerSessionIdSubdirectories = "
				+ usePerSessionIdSubdirectories + "\n");
		sb.append("maxBackfill = " + maxBackfill + "\n");
		sb.append("systemName = " + systemName + "\n");
		sb.append("sessionId = " + sessionId + "\n");
		sb.append("dataFormat = " + dataFormat + "\n");
		sb.append("address = " + address + "\n");

		return sb.toString();
	}

}

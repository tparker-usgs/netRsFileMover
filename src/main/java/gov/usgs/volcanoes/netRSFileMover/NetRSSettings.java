package gov.usgs.volcanoes.netRSFileMover;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import gov.usgs.volcanoes.core.configfile.ConfigFile;
import gov.usgs.volcanoes.core.util.StringUtils;

/**
 * Settings required to retrieve files from a single NetRS receiver
 * 
 * @author Tom Parker
 * 
 */
public class NetRSSettings {
	public static final String DEFAULT_USER = "netrsFTP";
	public static final int DEFAULT_DURATION = 1440;
	public static final boolean DEFAULT_USE_PER_DAY_SUBDIRECTORIES = true;
	public static final boolean DEFAULT_USE_PER_SESSION_ID_SUBDIRECTORIES = true;
	public static final int DEFAULT_MAX_DAYS = 7;
	public static final boolean DEFAULT_PRINT_HASH = true;
	public static final String DEFAULT_SESSION_ID = "a";
	public static final String DEFAULT_DATA_FORMAT = "T00";
	public static final String DEFAULT_OUTPUT_DIR = "output";
	public static final boolean DEFAULT_DEPTH_FIRST = false;
	public static final int DEFAULT_CONNECT_TIMEOUT = 30;
	public static final boolean DEFAULT_PASSIVE_FTP = true;
	public static final int DEFAULT_WINDOW_SIZE = 0;
	public static final boolean DEFAULT_RESUME_TRANSFER = true;

	public final String userName;
	public final String password;
	public final int duration;
	public final boolean usePerDaySubdirectories;
	public final boolean usePerSessionIdSubdirectories;
	public final int maxDays;
	public final String systemName;
	public final String sessionId;
	public final String dataFormat;
	public final String address;
	public final boolean printHash;
	public final String fileNameFormat;
	public final String outputDir;
	public final boolean depthFirst;
	public final int connectTimeout;
	public final int bytesPerSecond;
	public final boolean passiveFTP;
	public final int windowSize;
	public final boolean resumeTransfer;

	/**
	 * Simple constructor.
	 * 
	 * @param systemName
	 * @param cf
	 */
	public NetRSSettings(String systemName, ConfigFile cf) {

		this.systemName = systemName;
		if (cf.getString("password") != null) {
			userName = StringUtils.stringToString(cf.getString("userName"),
					DEFAULT_USER);
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

		bytesPerSecond = StringUtils.stringToInt(cf.getString("bytesPerSecond"),
				Integer.MIN_VALUE);
		depthFirst = StringUtils.stringToBoolean(cf.getString("depthFirst"),
				DEFAULT_DEPTH_FIRST);
		printHash = StringUtils.stringToBoolean(cf.getString("printHash"),
				DEFAULT_PRINT_HASH);
		resumeTransfer = StringUtils.stringToBoolean(cf.getString("resumeTransfer"),
				DEFAULT_RESUME_TRANSFER);
		duration = StringUtils.stringToInt(cf.getString("duration"), DEFAULT_DURATION);
		usePerDaySubdirectories = StringUtils.stringToBoolean(
				cf.getString("perDaySubdirectories"),
				DEFAULT_USE_PER_DAY_SUBDIRECTORIES);
		usePerSessionIdSubdirectories = StringUtils.stringToBoolean(
				cf.getString("perSessionIdSubdirectories"),
				DEFAULT_USE_PER_SESSION_ID_SUBDIRECTORIES);
		maxDays = StringUtils.stringToInt(cf.getString("maxDays"),
				DEFAULT_MAX_DAYS);

		connectTimeout = StringUtils.stringToInt(cf.getString("connectTimeout"),
				DEFAULT_CONNECT_TIMEOUT);

		sessionId = StringUtils.stringToString(cf.getString("sessionId"),
				DEFAULT_SESSION_ID);
		if (!sessionId.matches("^[a-z]$"))
			throw new RuntimeException(
					"sessionId must be a single lowercase letter. " + sessionId
							+ " won't work.");

		dataFormat = StringUtils.stringToString(cf.getString("dataFormat"),
				DEFAULT_DATA_FORMAT);
		if (!dataFormat.equals("T00") && !dataFormat.equals("Binex"))
			throw new RuntimeException(
					"dataFormat must be either T00 or Binex. " + dataFormat
							+ " doesn't cut it.");

		address = cf.getString("address");

		passiveFTP = StringUtils.stringToBoolean(cf.getString("passiveFTP"), DEFAULT_PASSIVE_FTP);
		
		windowSize = StringUtils.stringToInt(cf.getString("windowSize"), DEFAULT_WINDOW_SIZE);
				
		outputDir = StringUtils.stringToString(cf.getString("outputDir"),
				DEFAULT_OUTPUT_DIR);
		File f = new File(outputDir);
		if (!f.exists())
			f.mkdir();

		fileNameFormat = getFileNameFormat();
	}

	/**
	 * Construct a string, suitable for passing to SimpleDateFormat,
	 * representing the path of a file on the receiver.
	 * 
	 * @return
	 */
	public String getFileNameFormat() {
		StringBuilder format = new StringBuilder();
		format.append("yyyyMM/");

		if (usePerDaySubdirectories)
			format.append("dd/");

		if (usePerSessionIdSubdirectories)
			format.append("'" + sessionId + "'/");

		format.append("'" + systemName + "'yyyyMMddHHmm'" + sessionId + "."
				+ dataFormat + "'");

		return format.toString();
	}

	/**
	 * construct a human-readable string of this objects settings.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("userName = " + userName + "\n");
		sb.append("password = " + password + "\n");
		sb.append("duration = " + duration + "\n");
		sb.append("usePerDaySubdirectories = " + usePerDaySubdirectories + "\n");
		sb.append("usePerSessionIdSubdirectories = "
				+ usePerSessionIdSubdirectories + "\n");
		sb.append("maxDays = " + maxDays + "\n");
		sb.append("systemName = " + systemName + "\n");
		sb.append("sessionId = " + sessionId + "\n");
		sb.append("dataFormat = " + dataFormat + "\n");
		sb.append("address = " + address + "\n");

		return sb.toString();
	}
}

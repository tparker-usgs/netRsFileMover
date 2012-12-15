package gov.usgs.NetRSFileMover;
import gov.usgs.util.ConfigFile;
import gov.usgs.util.Util;

import java.net.InetAddress;
import java.net.UnknownHostException;


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
	
	public NetRSSettings (String systemName, ConfigFile cf) {
		this.systemName = systemName;
		if (cf.getString("password") != null) {
			userName = cf.getString("userName");
			password = cf.getString("password");
		} else {
			userName = "anonymous";
			String hostname = null;
            try {
            	hostname = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) { }
            password = System.getProperty("user.name") + "@" + hostname;
		}
	
		doDelete = Util.stringToBoolean(cf.getString("doDelete"), DEFAULT_DO_DELETE);
		duration = Util.stringToInt(cf.getString("duration"), DEFAULT_DURATION);
		usePerDaySubdirectories = Util.stringToBoolean(cf.getString("usePerDaySubdirectories"), DEFAULT_USE_PER_DAY_SUBDIRECTORIES);
		usePerSessionIdSubdirectories = Util.stringToBoolean(cf.getString("usePerSessionIdSubdirectories"), DEFAULT_USE_PER_SESSION_ID_SUBDIRECTORIES);
		maxBackfill = Util.stringToInt(cf.getString("maxBackfill"), DEFAULT_MAX_BACKFILL);
		
		sessionId = Util.stringToString(cf.getString("sessionId"), DEFAULT_SESSION_ID);
		if (!sessionId.matches("^a-z$"))
			throw new RuntimeException("sessionId must be a single lowercase letter. " + sessionId + " won't work.");
		
		dataFormat = Util.stringToString(cf.getString("dataFormat"), DEFAULT_DATA_FORMAT);
		if (!dataFormat.equals("T00") && !dataFormat.equals("Binex"))
			throw new RuntimeException("dataFormat must be either T00 or Binex. " + dataFormat + " doesn't cut it.");
	}
	
	

}

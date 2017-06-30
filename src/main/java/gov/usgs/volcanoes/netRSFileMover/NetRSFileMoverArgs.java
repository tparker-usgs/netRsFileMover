/**
 * I waive copyright and related rights in the this work worldwide
 * through the CC0 1.0 Universal public domain dedication.
 * https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */

package gov.usgs.volcanoes.netRSFileMover;

import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;

import gov.usgs.volcanoes.core.args.Args;
import gov.usgs.volcanoes.core.args.Arguments;
import gov.usgs.volcanoes.core.args.decorator.ConfigFileArg;
import gov.usgs.volcanoes.core.args.decorator.CreateConfigArg;
import gov.usgs.volcanoes.core.args.decorator.DateRangeArg;
import gov.usgs.volcanoes.core.args.decorator.TimeSpanArg;
import gov.usgs.volcanoes.core.args.decorator.VerboseArg;
import gov.usgs.volcanoes.core.time.TimeSpan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Argument processor for Pensive.
 *
 * @author Tom Parker
 */
public class NetRSFileMoverArgs {
	private static final Logger LOGGER = LoggerFactory.getLogger(NetRSFileMoverArgs.class);

	private static final String EXAMPLE_CONFIG_FILENAME = "receivers.config";

	private static final String PROGRAM_NAME = "java -jar gov.usgs.volcanoes.netRSFileMover.NetRSFileMover";
	private static final String EXPLANATION = "I move files slowly\n";

	/** format of time on cmd line */
	public static final String INPUT_TIME_FORMAT = "yyyyMMdd";

	private static final Parameter[] PARAMETERS = new Parameter[] {};

	/** If true, log more. */
	public final boolean verbose;

	/** Date of first file to retrieve. */
	public final TimeSpan timeSpan;

	/** my config file. */
	public final String configFileName;

	/**
	 * Class constructor.
	 * 
	 * @param commandLineArgs
	 *            the command line arguments
	 * @throws Exception
	 *             when things go wrong
	 */
	public NetRSFileMoverArgs(final String[] commandLineArgs) throws Exception {
		Arguments args = null;
		args = new Args(PROGRAM_NAME, EXPLANATION, PARAMETERS);
		args = new TimeSpanArg(INPUT_TIME_FORMAT, false, args);
		args = new VerboseArg(args);
		args = new ConfigFileArg(null, args);
		args = new CreateConfigArg(EXAMPLE_CONFIG_FILENAME, args);

		JSAPResult jsapResult = null;
		jsapResult = args.parse(commandLineArgs);

		verbose = jsapResult.getBoolean("verbose");
		LOGGER.debug("Setting: verbose={}", verbose);

		timeSpan = (TimeSpan) jsapResult.getObject("timeSpan");
		if (timeSpan != null) {
			LOGGER.debug("Setting: timeSpan={}", timeSpan);
		}

		configFileName = jsapResult.getString("config-filename");
		LOGGER.debug("Setting: config-filename={}", configFileName);

		if (jsapResult.getBoolean("create-config") || jsapResult.getBoolean("help")) {
			System.exit(1);
		}
	}
}

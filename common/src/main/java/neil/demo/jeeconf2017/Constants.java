package neil.demo.jeeconf2017;

import neil.demo.jeeconf2017.domain.HistoricCurrency;

/**
 * <P>Define some constants.
 * </P>
 */
public class Constants {
	
	// Distributed Objects, all here are names of com.hazelcast.core.IMap objects
	public static final String      MAP_EXPONENTIAL_MOVING_AVERAGE = "ema";
	public static final String      MAP_HISTORIC_CURRENCY	= HistoricCurrency.class.getSimpleName();
	public static final String      MAP_SIMPLE_MOVING_AVERAGE = "sma";
	
	// Source data, the European Central Bank
	public static final String      ECB_BASE_URL = "http://www.ecb.europa.eu/stats/eurofxref";
	public static final String      ECB_90DAY_HISTORY_XML = "eurofxref-hist-90d.xml";
	public static final String      ECB_90DAY_HISTORY_XML_SAVED = "eurofxref-hist-90d-20170524.xml";
	public static final String 		XML_NAMESPACE_ECB = "http://www.ecb.int/vocabulary/2002-08-01/eurofxref";
	public static final String 		XML_NAMESPACE_GESMES = "http://www.gesmes.org/xml/2002-08-01";

}

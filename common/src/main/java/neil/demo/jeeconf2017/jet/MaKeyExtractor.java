package neil.demo.jeeconf2017.jet;

import com.hazelcast.jet.Distributed;

import neil.demo.jeeconf2017.domain.HistoricCurrency;
import neil.demo.jeeconf2017.domain.HistoricCurrencyKey;

import java.util.Map;

/**
 * <P>When the edge between processing vertexes is partition, routing is
 * involved.
 * </P>
 * <P>Re-use the routing method the IMDG calls for storing the data to
 * be the routing method used by Jet for sending the data across the
 * edges of the processing graph.
 * </P>
 * <P>This keys data local to the JVM between intermediate stages
 * of the analysis, so reducing network hops.
 * </P>
 */
@SuppressWarnings("serial")
public class MaKeyExtractor implements Distributed.Function<Map.Entry<HistoricCurrencyKey, HistoricCurrency>, String> {

	/**
	 * <P>Use the key's provided routing.
	 * </P>
	 * 
	 * @param entry An entry streamed from the source map
	 * @return The routing key of that entry, {@code source + target} currencies.
	 */
	@Override
	public String apply(Map.Entry<HistoricCurrencyKey, HistoricCurrency> entry) {
		HistoricCurrencyKey historicCurrencyKey = entry.getKey();

		return historicCurrencyKey.getPartitionKey();
	}

}

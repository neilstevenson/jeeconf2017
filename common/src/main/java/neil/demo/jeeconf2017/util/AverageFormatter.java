package neil.demo.jeeconf2017.util;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.stream.DistributedCollectors;
import com.hazelcast.jet.stream.IStreamMap;

import neil.demo.jeeconf2017.domain.Currency;
import neil.demo.jeeconf2017.domain.CurrencyAverage;
import neil.demo.jeeconf2017.domain.CurrencyPair;

/**
 * <P><B>Bonus material!</B> : Use Jet as a reformatter.
 * </P>
 * <P>Output from the calculations are stored in {@link com.hazelcast.core.IMap IMap}
 * as map entries where the key is a {@link neil.demo.jeeconf2017.domain.CurrencyPair CurrencyPair}
 * and the value is a {@link java.math.BigDecimal}. We need them as collection of
 * {@link neil.demo.jeeconf2017.domain.CurrencyAverage CurrencyAverage}, and ideally
 * sorted into order, as this will make the display on a web page easier.
 * </P>
 * <P>This is an easy enough piece of reformating. We could treat the {@link com.hazelcast.core.IMap IMap}
 * as a {@link java.util.Map Map} and stream the content and reformat. This would stream the map
 * first (from as many server JVMs as we have) and then do the reformatting and collection.
 * </P>
 * <P>Here, we use Jet's {@link com.hazelcast.jet.stream.IStreamMap} so the streaming happens in
 * a distributed way (on as many server JVMs as we have) and the initial collection is distributed too
 * (on as many servers as we have). If we were doing a filter too, to limit what is being collected,
 * it would be more efficient to filter in situ prior to transmission from server to client, rather
 * than to filter on the client.
 * </P>
 */
@Component
public class AverageFormatter {

	@Autowired
	private JetInstance jetInstance;
	
    /**
     * <P>Helper method to read from an {@link com.hazelcast.core.IMap IMap}
     * and reformat.
     * </P>
     * <P>Stream the IMap content in, format into the required class.
     * As there are no filters, in general this has the potential to
     * overflow memory. However, there is only one average per currency
     * and only so many countries in the world, so there should not
     * be that much data in this case.
     * </P>
     * 
     * @param mapName "exponential" or "simple"
     * @return A list of moving averages
     */
    public Collection<CurrencyAverage> read(String mapName) {
    	
    	IStreamMap<Currency, BigDecimal> streamMap = this.jetInstance.getMap(mapName);
    	
    	return streamMap
    			.stream()
				.map(entry -> 
					// Source currency as Euro is implied.
					new CurrencyAverage(new CurrencyPair(Currency.EUR, entry.getKey()), entry.getValue())
						)
				.collect(DistributedCollectors.toCollection(TreeSet::new));
	}

}

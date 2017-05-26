package neil.demo.jeeconf2017.jet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import com.hazelcast.jet.AbstractProcessor;
import com.hazelcast.jet.Traversers;
import neil.demo.jeeconf2017.domain.Currency;
import neil.demo.jeeconf2017.domain.HistoricCurrency;
import neil.demo.jeeconf2017.domain.HistoricCurrencyKey;

/**
 * <P>The first stage in moving average calculation, get the series
 * of figures that are to be averaged.
 * </P>
 * <P>We will be provided a stream of currencies and their prices,
 * keep the last '<I>n</I>' prices for each currency. As the
 * input is partitioned, currencies will not be spread across
 * processors.
 * </P>
 */
public class LastNProcessor<Entry, Set> extends AbstractProcessor {

	private final int last;

	/**
	 * <P>For each currency, keep an ordered list of daily prices.
	 * </P>
	 * TODO The source currency in the data feed is always the Euro
	 * so only the target currency is stored. This could/should be
	 * expanded should data from other exchanges be obtained.
	 */
	private final Map<Currency, TreeSet<ClosingPrice>> collatedPrices = new HashMap<>();
	
	public LastNProcessor(final int arg0) {
		this.last = arg0;
	}
	
	/**
	 * <P>Use a {@link java.util.TreeSet} to keep the last '<I>n</I>' items,
	 * as later processing may need them in order -- exponential moving
	 * average does, as the more recent values are given a higher weighting.
	 * </P>
	 * 
	 * @param ordinal Where this item has come from
	 * @param item The item itself, a currency pair on a date
	 * @return {@code true} for successfully processed.
	 */
    @Override
    protected boolean tryProcess(int ordinal, Object item) throws IOException {
    	
		@SuppressWarnings("unchecked")
		Map.Entry<HistoricCurrencyKey, HistoricCurrency> entry = 
				(Map.Entry<HistoricCurrencyKey, HistoricCurrency>) item;
		
		HistoricCurrencyKey historicCurrencyKey = entry.getKey();
		HistoricCurrency historicCurrency = entry.getValue();
	
		// Ensure we have a list of values for the currency received
		if (!this.collatedPrices.containsKey(historicCurrencyKey.getTo())) {
			this.collatedPrices.put(historicCurrencyKey.getTo(), new TreeSet<>());
		}
		
		TreeSet<ClosingPrice> lastNPrices = this.collatedPrices.get(historicCurrencyKey.getTo());

		// Add price to set, a TreeSet<Comparable> so goes in the corrected order
		lastNPrices.add(new ClosingPrice(historicCurrencyKey.getDate(), historicCurrency.getClose()));
		
		// Ascending order, first is oldest so not needed if have too many
		if (lastNPrices.size() > this.last) {
			lastNPrices.remove(lastNPrices.first());
		}
		
    	return true;
    }

    /**
     * <P>Send out the collated '<I>last n</I>' for each currency.
     * </P>
     * 
     * @return Should be {@code true}, streaming the collected results shouldn't fail
     */
    @Override
    public boolean complete() {
    	return super.emitCooperatively(Traversers.traverseStream(this.collatedPrices.entrySet().stream()));
    }
    
}

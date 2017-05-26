package neil.demo.jeeconf2017.jet;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import com.hazelcast.jet.AbstractProcessor;
import com.hazelcast.jet.Traversers;

import lombok.extern.slf4j.Slf4j;

import neil.demo.jeeconf2017.domain.Currency;

/**
 * <P>Calculate the <B>Simple Moving Average</B>.
 * </P>
 * <P>For a stream of input, sum them up and divide by the count.
 * </P>
 * <P>Earlier processing stages ensure we are only fed the
 * data we need to sum.
 * </P>
 */
@Slf4j
public class SmaProcessor extends AbstractProcessor {

	private Map<Currency, BigDecimal> averages = new HashMap<>();
	
	/**
	 * <P>Called once per currency, calculate the average and keep
	 * it locally.
	 * </P>
	 * 
	 * @param ordinal Where this item has come from
	 * @param item A set of prices for a currency.
	 * @return {@code false} if input set empty, otherwise {@code true}
	 */
    @Override
    protected boolean tryProcess(int ordinal, Object item) throws IOException {
    	
		@SuppressWarnings("unchecked")
		Map.Entry<Currency, TreeSet<ClosingPrice>> entry = 
				(Map.Entry<Currency, TreeSet<ClosingPrice>>) item;
		
		Currency currency = entry.getKey();
		TreeSet<ClosingPrice> prices = entry.getValue();
		
		if (prices.size() == 0) {
			// Abandon calculation
			log.warn("Empty set of prices for {}", currency);
			return false;
		}

		BigDecimal divisor = new BigDecimal(prices.size());
		BigDecimal tally = BigDecimal.ZERO;
		
		// Add all the values
		for (ClosingPrice lastNPrice : prices) {
			tally = tally.add(lastNPrice.getClose());
		}
		
		// Sum and round
		this.averages.put(currency, tally.divide(divisor, 2, RoundingMode.HALF_UP));
		
		return true;
    }

    /**
     * <P>Output the averages accumulated.
     * </P>
     * 
     * @return {@code true}, not expecting failure streaming the field
     */
    @Override
    public boolean complete() {
    	log.info("complete -> {}", this.averages.keySet());
    	return super.emitCooperatively(Traversers.traverseStream(this.averages.entrySet().stream()));
    }

}

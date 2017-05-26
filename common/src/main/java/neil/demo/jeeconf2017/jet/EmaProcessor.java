package neil.demo.jeeconf2017.jet;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import com.hazelcast.jet.AbstractProcessor;
import com.hazelcast.jet.Traversers;
import neil.demo.jeeconf2017.domain.Currency;

import lombok.extern.slf4j.Slf4j;

/**
 * <P>Calculate the <B>Exponential Moving Average</B>.
 * </P>
 * <P>There are various ways to calculate this, but the essence
 * is that more significance is given to newer prices unlike
 * the {@link SmaProcessor} which treats all prices equally.
 * </P>
 * <P>For a series of 10 prices, the method used here is
 * to compute the flat average of the first five as the
 * base. The base plus a multiple times the sixth is used
 * to compute the next. This value is then used with a multiple
 * times the seventh, and so on.
 * </P>
 */
@Slf4j
public class EmaProcessor extends AbstractProcessor {

	private Map<Currency, BigDecimal> averages = new HashMap<>();

	/**
	 * <P>Called once per currency. Calculate the average and
	 * store it temporarily until input is exhausted.
	 * </P>
	 * <P>The method here is to take the flat average of the first
	 * five terms.
	 * </P>
	 * <P>The weight for the 6<sup>th</sup> is (2/6), and the value
	 * here is ( (4/6 * the 6<sup>th</sup>) + (2/6 * the value for the first 5) ).
	 * </P>
	 * <P>The weight for the 7<sup>th</sup> is (2/7), and the value
	 * here is ( (5/7 * the 7<sup>th</sup>) + (2/7 * the value for the first 6) ).
	 * </P>
	 * <P>The weight for the 8<sup>th</sup> is (2/8), and the value
	 * here is ( (6/8 * the 7<sup>th</sup>) + (2/8 * the value for the first 7) ).
	 * </P>
	 * 
	 * @param ordinal Where this item has come from
	 * @param item The item itself, a currency pair on a date
	 * @return {@code false} if input set too small, otherwise {@code true}
	 */
    @Override
    protected boolean tryProcess(int ordinal, Object item) throws IOException {
    	
		@SuppressWarnings("unchecked")
		Map.Entry<Currency, TreeSet<ClosingPrice>> entry = 
				(Map.Entry<Currency, TreeSet<ClosingPrice>>) item;
		
		Currency currency = entry.getKey();
		TreeSet<ClosingPrice> prices = entry.getValue();

		if (prices.size() < 6) {
			// Abandon calculation
			log.warn("Only {} prices for {}", prices.size(), currency);
			return false;
		}

		ClosingPrice[] priceArray = prices.toArray(new ClosingPrice[prices.size()]);

		// Simple average for first five
		
		BigDecimal FIVE = new BigDecimal(5);
		BigDecimal tally = BigDecimal.ZERO;
		int i=0;
		for (; i<5; i++) {
			tally = tally.add(priceArray[i].getClose());
		}
		
		BigDecimal value = tally.divide(FIVE);
		BigDecimal TWO = new BigDecimal(2);

		// Exponentiate for remaining
		
		for ( ; i<priceArray.length ; i++) {
			BigDecimal divisor = new BigDecimal(i + 1);
			BigDecimal weight = TWO.divide(divisor, 2, RoundingMode.HALF_UP);
			
			BigDecimal previous = value.multiply(weight);
			BigDecimal current = priceArray[i].getClose().multiply(BigDecimal.ONE.subtract(weight));
			value = current.add(previous);
		}

		// Save result for later output
		
		this.averages.put(currency, value.setScale(2, RoundingMode.HALF_UP));
		
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

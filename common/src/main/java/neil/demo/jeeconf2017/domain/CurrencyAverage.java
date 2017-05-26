package neil.demo.jeeconf2017.domain;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * <P>A pair of currencies for trading, from one to the other.
 * </P>
 */
@AllArgsConstructor
@Data
@SuppressWarnings("serial")
public class CurrencyAverage implements Comparable<CurrencyAverage>, Serializable {
	
	private CurrencyPair	pair;
	private BigDecimal  	average;
	
	// Comparable - only on the currency pair
	@Override
	public int compareTo(CurrencyAverage that) {
		return this.pair.compareTo(that.getPair());
	}

}

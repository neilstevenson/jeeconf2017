package neil.demo.jeeconf2017.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * <P>A pair of currencies for trading, from one to the other.
 * </P>
 * <P>This class is used by the client currently, but hold in
 * shared as perhaps eventually the server may need it.
 * </P>
 */
@AllArgsConstructor
@Data
@SuppressWarnings("serial")
public class CurrencyPair implements Comparable<CurrencyPair>, Serializable {
	
	private Currency	from;
	private Currency	to;
	
	// Comparable - Source currency, then Target currency
	@Override
	public int compareTo(CurrencyPair that) {
		
		int from = this.from.compareTo(that.getFrom());
		
		return from!=0 ? from : this.getTo().compareTo(that.getTo());
	}

}

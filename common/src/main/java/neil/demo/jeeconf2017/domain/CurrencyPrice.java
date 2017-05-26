package neil.demo.jeeconf2017.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * <P>A currency pair in effect. Currencies have no absolute price,
 * only a relative price converting from another.
 * </P>
 * <P>This class is used by the client currently, but hold in
 * shared as perhaps eventually the server may need it.
 * </P>
 */
@AllArgsConstructor
@Data
@SuppressWarnings("serial")
public class CurrencyPrice implements Comparable<CurrencyPrice>, Serializable {
	
	private Currency	from;
	private Currency	to;
	private LocalDate   date;
	private Price       kind;
	private BigDecimal	rate;
	
	// Comparable - Order by date, descending
	@Override
	public int compareTo(CurrencyPrice that) {
		return -1 * this.date.compareTo(that.getDate());
	}

}

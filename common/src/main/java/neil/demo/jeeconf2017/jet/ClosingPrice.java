package neil.demo.jeeconf2017.jet;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * <P>The closing price of a currency on a specific date.
 * </P>
 */
@AllArgsConstructor
@Data
public class ClosingPrice implements Comparable<ClosingPrice> {
	private LocalDate   	date;
	private BigDecimal		close;

	/**
	 * <P>Sort by ascending sequence on the date alone.
	 * </P>
	 * 
	 * @param that Another {@link ClosingPrice}
	 * @return	Natural sort for date.
	 */
	@Override
	public int compareTo(ClosingPrice that) {
		return this.date.compareTo(that.getDate());
	}

}

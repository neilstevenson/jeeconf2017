package neil.demo.jeeconf2017.domain;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

/**
 * <P>The <I>value</I> pair of the {@link HistoryCurrencyKey} / {@link HistoryCurrency}
 * {@code key-value} pair.
 * </P>
 * <P>Here we are just storing the day's closing price.
 * </P>
 * <P>The opening price, daily high and daily low would be useful additions.
 * </P>
 */
@Data
@SuppressWarnings("serial")
public class HistoricCurrency implements Serializable {

	//private BigDecimal	open;
	private BigDecimal		close;
	//private BigDecimal	high;
	//private BigDecimal	low;

}

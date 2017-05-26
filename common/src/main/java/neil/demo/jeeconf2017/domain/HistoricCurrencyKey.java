package neil.demo.jeeconf2017.domain;

import com.hazelcast.core.PartitionAware;

import java.io.Serializable;
import java.time.LocalDate;

import lombok.Data;

/**
 * <P>The <I>value</I> pair of the {@link HistoryCurrencyKey} / {@link HistoryCurrency}
 * {@code key-value} pair.
 * </P>
 * <P>The key is formed from three fields, but for the purposes of partition routing
 * only the source and target currencies are used.
 * <UL>
 * <LI><P>By this means the keys {@code (EUR,GBP,2017-04-10)} and {@code (EUR,GBP,2017-04-11)}
 * will have the same routing ("{@code EURGBP}") and their entries will be placed in
 * the same partition for all dates. Any processing that needs to look at both days can compare them
 * on the same JVM, and won't need to consider network transmission.
 * </P>
 * </LI>
 * <LI><P>The danger with any such interference with normal behaviour is that the cluster
 * data could be unbalanced. Worse case we could map all entries to the same partition.
 * </P>
 * <P>This should not be the case here, the same currencies are traded every day.
 * </P>
 * </LI>
 * </UL>
 */
@Data
@SuppressWarnings("serial")
public class HistoricCurrencyKey implements PartitionAware<String>, Serializable {
	
	private Currency	from;
	private Currency	to;
	private LocalDate   date;

	// Partitioning - Use currency only
	
	@Override
	public String getPartitionKey() {
		return this.from.name() + this.to.name();
	}

}

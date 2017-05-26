package neil.demo.jeeconf2017.jet;

import com.hazelcast.jet.DAG;
import com.hazelcast.jet.Edge;
import com.hazelcast.jet.Processors;
import com.hazelcast.jet.Vertex;

import neil.demo.jeeconf2017.Constants;

/**
 * <P>A <B>D</B>istributed <B>A</B>cyclic <B>G</B>raph to compute <B>M</B>oving <B>A</B>verages.
 * </P>
 * <P>The moving averages are calculated on currency prices, for the last 10 prices,
 * and are calculated according to both <I>simple</I> and an <I>exponential</I> (weighted)
 * calculation scheme.
 * </P>
 * <P>In pictorial terms, the processing looks like:
 * </P>
 * <PRE>
 *                             /========================\
 *                             | Historic Currency IMap |
 *                             \========================/
 *                                         |
 *                               (from, to, date, price)
 *                                         |
 *                               +--------------------+          
 *                               | Last 'n' Processor | 
 *                               +--------------------+          
 *                                         |
 *                                    (to, price[])                                        
 *                                         |
 *                   +---------------------+---------------------+
 *                   |                                           |
 *          +----------------+                           +----------------+
 *          | SMA Calculator |                           | EMA Calculator |
 *          +----------------+                           +----------------+
 *                   |                                           |
 *              (to, price)                                 (to, price)
 *                   |                                           |
 *           /================\                          /================\
 *           | SMA Price IMap |                          | EMA Price IMap |
 *           \================/                          \================/
 * </PRE>
 * <P><B>NOTE:</B>
 * In the current data feed, the source currency is always the Euro ("{@code EUR}"), so
 * only the target currency passes through the stages. This optimisation needs to be
 * removed if the data feed expands to have other source currencies.
 * </P>
 * <P>There are a few stages to this graph. Remember there will be at least one instance
 * of this graph running on every node.
 * </P>
 * <H3>1 <B>{@link IMap} -> {@link LastNProcessor}</B></H3>
 * <P>The start of the graph uses the built-in {@link Processors.readMap()}
 * to treat an {@link IMap} as a streaming data source. The contents
 * of the source {@link IMap} as fed as input intu the {@link LastNProcessor}.
 * </P>
 * <P>The optimisation here is that should be multiple nodes in the
 * Hazelcast IMDG grid, each with a share of that {@link IMap} and
 * each also running this processing graph. Data from the parts of
 * the {@link IMap} stored on a node are fed into the instance of
 * the graph on that node. At this point data doesn't need to
 * cross JVMs, so the ingest speed is much higher.
 * </P>
 * <H3>2-A <B>{@link LastNProcessor} -> {@link SmaProcessor}</B></H3>
 * <P>For each target currency, the {@link LastNProcessor} emits
 * a set of last <I>n</I> prices for that currency.
 * </P>
 * <P>This is fed into the {@link SmaProcessor} which then only
 * has to sum each up and divide by the count to calculate the
 * result for each currency.
 * </P>
 * <H3>2-B <B>{@link LastNProcessor} -> {@link EmaProcessor}</B></H3>
 * <P>The same data output by the {@link LastNProcessor} that was
 * fed into the {@link SmaProcessor} in step <B>2-A</B> is also fed
 * into the {@link EmaProcessor}.
 * </P>
 * <P>Processing in the {@link EmaProcoessor} to calculate the
 * exponential moving average is more complicated, as the later
 * more recent prices are given a higher weighting so skew the
 * average in favour of the later results.
 * </P>
 * <H3>3-A <B>{@link SmaProcessor} -> {@link IMap}</B></H3>
 * <P>The {@link SmaProcessor} emits a stream of map entries
 * for a target currency key and average price value.
 * </P>
 * <P>Use the built-in map writer processor {@link Processors#writeMap}
 * to stream this into an {@link IMap} that holds the results.
 * </P>
 * <H3>3-B <B>{@link EmaProcessor} -> {@link IMap}</B></H3>
 * <P>The last stage of <I>Exponential Moving Average</I> follows the same style
 * as the last stage of <I>Simple Moving Average</I>, namely to stream to output
 * from the calculation in parallel into a {@link IMap} using the
 * build-in map writer processor {@link Processors#writeMap}. 
 * </P>
 */
public class MaDAG extends DAG {
	
	private static final int MAX = 10;

	// https://en.wikipedia.org/wiki/Fragile_base_class
	public MaDAG (final int last) {
		super();
		
		if (last > MAX) {
			throw new RuntimeException("Processing capped at last " + MAX + ", supplied " + last);
		}

		/* First stage, send the map content through a processor that will collate
		 * the last "N" prices for each currency.
		 */
		Vertex mapSource = this.newVertex("mapSource", Processors.readMap(Constants.MAP_HISTORIC_CURRENCY));
		Vertex lastN = this.newVertex("lastN", new LastNProcessorSupplier(last));
		this.edge(Edge.between(mapSource, lastN).partitioned(new MaKeyExtractor()));

		/* Simple Moving Average fork, sum and write to the SMA map
		 */
		Vertex sma = this.newVertex("sma", SmaProcessor::new);
		this.edge(Edge.from(lastN, 0).to(sma));
		Vertex smaMapSink = this.newVertex("smaMapSink", Processors.writeMap(Constants.MAP_SIMPLE_MOVING_AVERAGE));
		this.edge(Edge.between(sma, smaMapSink));

		/* Exponential Moving Average fork, sum and write to the EMA map
		 */
		Vertex ema = this.newVertex("ema", EmaProcessor::new);
		this.edge(Edge.from(lastN, 1).to(ema));
		Vertex emaMapSink = this.newVertex("emaMapSink", Processors.writeMap(Constants.MAP_EXPONENTIAL_MOVING_AVERAGE));
		this.edge(Edge.between(ema, emaMapSink));
	}
}

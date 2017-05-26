package neil.demo.jeeconf2017.controller;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.hazelcast.core.HazelcastInstance;

import lombok.extern.slf4j.Slf4j;
import neil.demo.jeeconf2017.Constants;
import neil.demo.jeeconf2017.domain.Currency;
import neil.demo.jeeconf2017.domain.CurrencyPair;
import neil.demo.jeeconf2017.domain.CurrencyPrice;
import neil.demo.jeeconf2017.domain.HistoricCurrency;
import neil.demo.jeeconf2017.domain.HistoricCurrencyKey;
import neil.demo.jeeconf2017.domain.Price;
import neil.demo.jeeconf2017.gesmes.GesmesLoader;
import neil.demo.jeeconf2017.util.ReflectionUtil;

/**
 * <P>A controller to handle the currency operations:
 * </P>
 * <OL>
 * <LI><P>{@code list} - Show the currencies in the historic currency map,
 * but only list the currency pair (from, to) and not the detail.
 * </P></LI>
 * <LI><P>{@code load} - Load historic currency data from the European
 * Central Bank, up to 90 days worth of closing prices for 30 currencies
 * compared to the Euro.
 * </P></LI>
 * <LI><P>{@code view} - Display the detail for a currency pair, the
 * list of closing prices.
 * </P></LI>
 */
@Controller
@RequestMapping("currency")
@Slf4j
public class CurrencyController {

	@Autowired
	private GesmesLoader gesmesLoader;
	@Autowired
	private HazelcastInstance hazelcastInstance;

	/**
	 * <P>
	 * Use a helper to find the column headers for an HTML table, and the same
	 * for the column data.
	 * </P>
	 * 
	 * @return The page to render and some objects for it
	 */
	@GetMapping("list")
	public ModelAndView list() {

		// Treat Hazelcast's IMap as Java standard map
		Map<HistoricCurrencyKey, HistoricCurrency> historicCurrencyMap
			= this.hazelcastInstance.getMap(Constants.MAP_HISTORIC_CURRENCY);
		
		// Only obtain the keys into the client from the cluster
		Stream<HistoricCurrencyKey> historicCurrencyKeys = historicCurrencyMap.keySet().stream();
		
		// Form unique pairs of source/target currency
		Collection<CurrencyPair> currencyPairs = 
				historicCurrencyKeys
				.map(historicCurrencyKey -> 
					new CurrencyPair(historicCurrencyKey.getFrom(), historicCurrencyKey.getTo())
				)
				.collect(Collectors.toCollection(TreeSet::new));
		
		ModelAndView modelAndView = new ModelAndView("currency/list");

		modelAndView.addObject("columns", ReflectionUtil.getColumns(CurrencyPair.class));
		modelAndView.addObject("data", ReflectionUtil.getData(currencyPairs, CurrencyPair.class));

		return modelAndView;
	}

	/**
	 * <P>Run the Gesmes loader, which will put data into Hazelcast
	 * and return the count of the number of objects.
	 * </P>
	 * 
	 * @param j_load A hidden object, used as a flag for page display
	 * @return The page to render and some objects for it
	 */
	@GetMapping("load")
	public ModelAndView load(@RequestParam(name="j_load", required=false) String j_load) {

		ModelAndView modelAndView = new ModelAndView("currency/load");

		if (j_load==null) {
			// First page render, show "Load" button but do not processing
		} else {
			// Second page render, assume "Load" button pressed so do processing 
			
			Instant start = Instant.now();

			int count = -1;
			try {
				count = this.gesmesLoader.load();
			} catch (Exception exception) {
				log.error("load()", exception);
			}
			
			Duration elapsed = Duration.between(start, Instant.now());

			modelAndView.addObject("j_elapsed", elapsed.toString());
			modelAndView.addObject("j_load", count);
		}
		
		return modelAndView;
	}

	/**
	 * <P>
	 * View a currency pair's prices.
	 * </P>
	 * <P>
	 * TODO: The j_view object is missing double quotes, so have to manually
	 * parse. If it was valid JSON the request parameter method argument could be
	 * changed to {@link CurrencyPair}.
	 * </P>
	 * <P><B>Note</B> Here we take a {@link com.hazelcast.core.IMap IMap} and
	 * downcast it to a {@link java.util.Map}, then take a stream from the
	 * entryset to convert into the required result.
	 * </P>
	 * <P>As the {@link com.hazelcast.core.IMap IMap} is hosted across multiple
	 * JVMs this can potentially return more data into the collection than there
	 * is room to host. It won't happen here as there only so many currency pair
	 * possibilities, but it's an area to be careful of in general.
	 * </P>
	 * <P>See also {@link AverageController#read(String)} which does a similar
	 * thing but uses a Jet {@link com.hazelcast.jet.stream.IStreamMap IStreamMap}
	 * to do the streaming more efficiently.
	 * </P>
	 * 
	 * @param request
	 *            A String, looks like JSON but isn't
	 * @return The page to render, and model attributes
	 */
	@PostMapping("view")
	public ModelAndView view(@RequestParam(name="j_view", required=true) String j_view) {

		ModelAndView modelAndView = new ModelAndView("currency/view");

		try {
			j_view = j_view.trim();
			
			int from_index = j_view.indexOf("from=");
			int to_index = j_view.indexOf("to=");
			
			String from = j_view.substring(from_index + 5, from_index + 8);
			String to = j_view.substring(to_index + 3, to_index + 6);

			// Treat Hazelcast's IMap as Java standard map
			Map<HistoricCurrencyKey, HistoricCurrency> historicCurrencyMap
				= this.hazelcastInstance.getMap(Constants.MAP_HISTORIC_CURRENCY);
			
			// See method comments on use of IStreamMap as alternative.
			Stream<Map.Entry<HistoricCurrencyKey, HistoricCurrency>> historicCurrencies
				= historicCurrencyMap.entrySet().stream();
			
			// Form list of prices for that currency
			Collection<CurrencyPrice> currencyPrices = 
					historicCurrencies
					.filter(entry -> entry.getKey().getFrom().equals(Currency.valueOf(from)))
					.filter(entry -> entry.getKey().getTo().equals(Currency.valueOf(to)))
					.map(entry -> 
							new CurrencyPrice(entry.getKey().getFrom(),
									entry.getKey().getTo(),
									entry.getKey().getDate(),
									Price.CLOSE,
									entry.getValue().getClose())
					)
					.collect(Collectors.toCollection(TreeSet::new));

			modelAndView.addObject("columns", ReflectionUtil.getColumns(CurrencyPrice.class));
			modelAndView.addObject("data", ReflectionUtil.getData(currencyPrices, CurrencyPrice.class));

		} catch (Exception exception) {
			log.error("view()", exception);
		}
		

		return modelAndView;
	}
}

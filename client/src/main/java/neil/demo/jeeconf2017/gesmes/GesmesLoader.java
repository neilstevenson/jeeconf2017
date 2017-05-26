package neil.demo.jeeconf2017.gesmes;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.time.LocalDate;

import javax.xml.transform.stream.StreamSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import lombok.extern.slf4j.Slf4j;
import neil.demo.jeeconf2017.Constants;
import neil.demo.jeeconf2017.domain.Currency;
import neil.demo.jeeconf2017.domain.HistoricCurrency;
import neil.demo.jeeconf2017.domain.HistoricCurrencyKey;
import neil.demo.jeeconf2017.gesmes.Gesmes.Cube;
import neil.demo.jeeconf2017.gesmes.Gesmes.Envelope;

/**
 * <P>Read from the European Central Bank, if possible, or from a
 * saved file. Write into a Hazelcast map.
 * </P>
 * <P>This is an ETL process. Lots of other ways to do this, for
 * example with a Kafka stream.
 * </P>
 */
@Component
@Slf4j
public class GesmesLoader {

	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private HazelcastInstance hazelcastInstance;
	@Autowired
	private Jaxb2Marshaller jaxb2Marshaller;

	/**
	 * <P>
	 * Try to read the last 90 dates exchange rates from the European Central
	 * Bank directly. If this fails, eg. if there is no network connection, then
	 * use stored results in a file.
	 * </P>
	 * <P>
	 * Use JAXB to turn the XML into a Java object, then for all the rates in
	 * this inject individually into an {@link IMap}.
	 * </P>
	 * 
	 * @return How many were loaded
	 * @throws Exception
	 *             Anything other than no internet connectivity
	 */
	public int load() throws Exception {

		/*
		 * Try the internet first, then src/main/resources second.
		 */
		Resource mainResource = this.applicationContext
				.getResource(Constants.ECB_BASE_URL + "/" + Constants.ECB_90DAY_HISTORY_XML);
		Resource fallbackResource = this.applicationContext
				.getResource("classpath:" + Constants.ECB_90DAY_HISTORY_XML_SAVED);
		Resource[] resources = new Resource[] { mainResource, fallbackResource };

		Envelope envelope = null;

		// Read rates from the available places until successful
		for (Resource resource : resources) {
			try (InputStream inputStream = resource.getInputStream();) {
				log.info("Read from '{}'", resource.getURL().toString());
				StreamSource streamSource = new StreamSource(inputStream);
				envelope = (Envelope) this.jaxb2Marshaller.unmarshal(streamSource);
				break;
			} catch (UnknownHostException unknownHostException) {
				// No network access
				log.error("Problem with '{}'", unknownHostException.getMessage());
			}
		}

		IMap<HistoricCurrencyKey, HistoricCurrency> historicCurrencyMap = this.hazelcastInstance
				.getMap(Constants.MAP_HISTORIC_CURRENCY);

		int count = 0;

		// Allow an NPE to be thrown if data incomplete.
		Cube parentCube = envelope.getCube();
		for (Cube dateCube : parentCube.getCubes()) {
			LocalDate date = LocalDate.parse(dateCube.getTime());

			for (Cube rateCube : dateCube.getCubes()) {
				HistoricCurrencyKey historicCurrencyKey = new HistoricCurrencyKey();
				// "From" currency is implied, for Eureopean Central Bank.
				historicCurrencyKey.setFrom(Currency.EUR);
				historicCurrencyKey.setTo(Currency.valueOf(rateCube.getCurrency()));
				historicCurrencyKey.setDate(date);

				HistoricCurrency historicCurrency = new HistoricCurrency();
				historicCurrency.setClose(new BigDecimal(rateCube.getRate()));

				historicCurrencyMap.set(historicCurrencyKey, historicCurrency);
				count++;
			}

		}

		if (count == 0) {
			log.error("No currencies loaded");
			return -1;
		} else {
			log.info("Loaded {} {}", count, (count == 1 ? "currency" : "currencies"));
			return count;
		}

	}
}

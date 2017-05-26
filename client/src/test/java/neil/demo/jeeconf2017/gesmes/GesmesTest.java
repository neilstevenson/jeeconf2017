package neil.demo.jeeconf2017.gesmes;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.xml.transform.stream.StreamSource;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.extern.slf4j.Slf4j;
import neil.demo.jeeconf2017.Constants;
import neil.demo.jeeconf2017.MyClientConfig;
import neil.demo.jeeconf2017.domain.Currency;

/**
 * <P>
 * Test parsing of a three tier cube. Outer cube is container, holding cubes for
 * dates, each has the same number of currency cubes inside.
 * </P>
 */
@RunWith(SpringRunner.class)
@Slf4j
@SpringBootTest(classes = { MyClientConfig.MyJaxb.class })
public class GesmesTest {

	// Don't count the Euro, as that is source currency not any of the targets.
	private static final int CURRENCIES_EXPECTED = Currency.values().length - 1;

	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private Jaxb2Marshaller jaxb2Marshaller;
	@Rule
	public TestName testName = new TestName();

	@Test
	public void test_parse() throws Exception {
		String testInput = "classpath:" + Constants.ECB_90DAY_HISTORY_XML_SAVED;

		log.info("{}: use '{}'", this.testName.getMethodName(), testInput);

		Resource resource = this.applicationContext.getResource(testInput);

		try (InputStream inputStream = resource.getInputStream();) {
			StreamSource streamSource = new StreamSource(inputStream);

			Object object = this.jaxb2Marshaller.unmarshal(streamSource);

			assertThat("Null", object, not(nullValue()));

			assertThat("Class", object, is(instanceOf(Gesmes.Envelope.class)));

			Gesmes.Envelope envelope = (Gesmes.Envelope) object;

			Gesmes.Cube outerCube = envelope.getCube();

			assertThat("OuterCube", outerCube, not(nullValue()));

			List<Gesmes.Cube> middleCubes = outerCube.getCubes();

			assertThat("MiddleCubes", middleCubes, not(nullValue()));

			/*
			 * 90 calendar days, at least 25 for weekends and public holidays.
			 * Weak test.
			 */
			assertThat("MiddleCubes, 90 calendar days of history", middleCubes.size(), Matchers.greaterThan(60));
			assertThat("MiddleCubes, 90 calender days of history", middleCubes.size(), Matchers.lessThan(65));

			// Hardcoding for specific test data extract for 24th May 2017
			int TRADING_DAYS_UPTO_2017_05_24 = 61;
			assertThat("MiddleCubes, trading days in last 90 calender days from 2017-05-24", middleCubes.size(),
					Matchers.equalTo(TRADING_DAYS_UPTO_2017_05_24));

			for (int i = 0; i < middleCubes.size(); i++) {
				Gesmes.Cube middleCube = middleCubes.get(i);

				assertThat("MiddleCube " + i, middleCube, not(nullValue()));
				String dateStr = middleCube.getTime();
				assertThat("Date for MiddleCube " + i, dateStr, not(nullValue()));
				try {
					LocalDate.parse(dateStr);
				} catch (DateTimeParseException dateTimeParseException) {
					fail("Date for MiddleCube " + i + " is '" + dateStr + "'.");
				}

				List<Gesmes.Cube> innerCubes = middleCube.getCubes();

				assertThat("InnerCubes" + i, innerCubes, not(nullValue()));
				assertThat("InnerCubes, currencies", innerCubes.size(), equalTo(CURRENCIES_EXPECTED));

				for (int j = 0; j < innerCubes.size(); j++) {
					Gesmes.Cube innerCube = innerCubes.get(j);

					assertThat("InnerCube " + i + "," + j, innerCube, not(nullValue()));

					assertThat("Cubes for InnerCube " + i + "," + j, innerCube.getCubes(), nullValue());

					String currencyStr = innerCube.getCurrency();
					assertThat("Target currency for InnerCube " + i + "," + j, currencyStr, not(nullValue()));
					try {
						Currency currency = Currency.valueOf(currencyStr);
						assertThat("Target currency for InnerCube " + i + "," + j, currency,
								not(equalTo(Currency.EUR)));
					} catch (IllegalArgumentException illegalArgumentException) {
						fail("Target currency for InnerCube " + i + "," + j + " is '" + currencyStr + "'.");
					}

					String rateStr = innerCube.getRate();
					assertThat("Rate InnerCube " + i + "," + j, rateStr, not(nullValue()));
					try {
						BigDecimal rate = new BigDecimal(rateStr);
						assertTrue("Positive rate for InnerCube " + i + "," + j + " is '" + rateStr + "'.",
								(rate.compareTo(BigDecimal.ZERO) > 0));
					} catch (NumberFormatException numberFormatException) {
						fail("Rate for InnerCube " + i + "," + j + " is '" + rateStr + "'.");
					}
				}
			}
		}

	}
}

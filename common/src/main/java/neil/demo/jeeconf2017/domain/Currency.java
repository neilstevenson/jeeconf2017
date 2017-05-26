package neil.demo.jeeconf2017.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * <P>ISO4217 currency codes for the expected currencies.
 * </P>
 */
@RequiredArgsConstructor
public enum Currency {

	 AUD ("Australian Dollar")
	,BGN ("Bulgarian Lev")
	,BRL ("Brazilian Real")
	,CAD ("Canadian Dollar")
	,CHF ("Swiss Franc")
	,CNY ("Chinese Yuan Renminbi")
	,CZK ("Czech Koruna")
	,DKK ("Danish Krone")
	,EUR ("Euro")
	,GBP ("British Pound")
	,HKD ("Hong Kong Dollar")
	,HRK ("Crotian Kuna")
	,HUF ("Hungarian Forint")
	,IDR ("Indonesian Rupiah")
	,ILS ("Israeli Sheqel")
	,INR ("Indian Rupee")
	,JPY ("Japanese Yen")
	,KRW ("South Korean Won")
	,MXN ("Mexican Peso")
	,MYR ("Malaysian Ringgit")
	,NOK ("Norwegian Krone")
	,NZD ("New Zealand Dollar")
	,PHP ("Philippines Peso")
	,PLN ("Polish Zloty")
	,RON ("Romanian New Leu")
	,RUB ("Russian Ruble")
	,SEK ("Swedish Krona")
	,SGD ("Singapore Dollar")
	,THB ("Thai Baht")
	,TRY ("Turkish Lira")
	,USD ("US Dollar")
	,ZAR ("South African Rand")
	;

	@Getter
    private final String description;
}

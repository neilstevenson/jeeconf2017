package neil.demo.jeeconf2017.gesmes;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import neil.demo.jeeconf2017.Constants;

import lombok.Data;
import lombok.ToString;

/**
 * <P>Format for the XML data received from European Central Bank.</P>
 * <P>Unhelpfully, the structure is a {@link Gesmes.Cube} with no attributes,
 * containing several {@link Gesmes.Cube} with a {@code time} attribute containing
 * several {@link Gesmes.Cube} with {@code currency} and {@code rate} attributes.
 * </P>
 * <P>Difficult to validate JSR303 style.
 * </P>
 */
public class Gesmes {
	
	/**
	 * <P>Top level, {@code subject} and {@code Sender} not used.
	 * </P>
	 */
	@Data
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name="Envelope", namespace=Constants.XML_NAMESPACE_GESMES)
	public static class Envelope {
		@XmlElement(name="subject", namespace=Constants.XML_NAMESPACE_GESMES)
		private String subject;

		@XmlElement(name="Sender", namespace=Constants.XML_NAMESPACE_GESMES)
		private Sender sender;
		
		@XmlElement(name="Cube", namespace=Constants.XML_NAMESPACE_ECB)
		private Cube cube;
	}

	/**
	 * <P>Not used, present for completeness.
	 * </P>
	 */
	@ToString
	static class Sender {
		@XmlElement(name="name", namespace=Constants.XML_NAMESPACE_GESMES)
		private String name;
	}
	
	/**
	 * <P>Cubes are in three tiers.
	 * </P>
	 * <OL>
	 * <LI>
	 * <P>The outermost has no attributes.</P>
	 * </LI>
	 * <LI>
	 * <P>The middle has {@code time}.</P>
	 * </LI>
	 * <LI>
	 * <P>The innermost has {@code currency} and {@code rate}. The currency is the target currency,
	 * the source currency (Euro for ECB) is not specified.</P>
	 * </LI>
	 * </OL>
	 */
	@Data
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Cube {
		@XmlAttribute(name="currency", required=false)
		private String currency;

		@XmlAttribute(name="rate", required=false)
		private String rate;

		@XmlAttribute(name="time", required=false)
		private String time;
		
		@XmlElement(name="Cube", namespace=Constants.XML_NAMESPACE_ECB)
		private List<Cube> cubes;
	}

}

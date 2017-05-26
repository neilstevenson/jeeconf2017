package neil.demo.jeeconf2017;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import neil.demo.jeeconf2017.gesmes.Gesmes;

/**
 * <P>Work with Spring Boot 1.5.3. Future versions may autobuild some
 * of these beans so we don't need to bother.
 * </P>
 * <P>{@code JetInstance} contains a {@code HazelcastInstance}, so make
 * this as a bean so that another {@code HazelcastInstance} isn't built.
 * </P>
 */
@Configuration
public class MyClientConfig {

    /**
     * <P>Create a marshaller {@code @Bean} though we'll only
     * use unmarshall - ({@code XML} to {@code Java}). Use
     * this to parse the XML from the European Central Bank
     * </P>
     * <P>Embed this bean in an inner class, so that {@link GesmesTest}
     * can test only the parsing without having to mock the Hazelcast
     * connectivity.
     * </P>
     * 
     * @return A {@code @Bean} configured for the expected classes
     */
	@Configuration
	public static class MyJaxb {
		
		@Bean
		public Jaxb2Marshaller jaxb2Marshaller() {
			Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
			jaxb2Marshaller.setClassesToBeBound(Gesmes.Envelope.class);
			return jaxb2Marshaller;
		}
		
	}

	/**
	 * <P>Load configuration for Hazelcast client from an XML file.
	 * </P>
	 * 
	 * @return Configuration object, built from XML
	 */
    @Bean
    public ClientConfig clientConfig() throws Exception {
    	return new XmlClientConfigBuilder("hazelcast-client.xml").build();
    }
    
	/**
	 * <P>Return a Hazelcast Jet client as a Spring bean.
	 * </P>
	 * 
	 * @param clientConfig Created above
	 * @return A Hazelcast Jet client
	 */
    @Bean
    public JetInstance jetInstance(ClientConfig clientConfig) { 
            return Jet.newJetClient(clientConfig);
    }

	/**
	 * <P>Return a Hazelcast IMDG client as a Spring bean.
	 * </P>
	 * 
	 * @param jetInstance Created above
	 * @return A Hazelcast IMDG client, enriched with Jet
	 */
    @Bean
    public HazelcastInstance hazelcastInstance(JetInstance jetInstance) {
            return jetInstance.getHazelcastInstance();
    }
}

package neil.demo.jeeconf2017;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.config.JetConfig;

/**
 * <P>Work with Spring Boot 1.5.3. Future versions may autobuild some
 * of these beans so we don't need to bother.
 * </P>
 * <P>{@code JetInstance} contains a {@code HazelcastInstance}, so make
 * this as a bean so that another {@code HazelcastInstance} isn't built.
 * </P>
 */
@Configuration
public class MyServerConfig {

	/**
	 * <P>Load configuration for Hazelcast server from an XML file.
	 * </P>
	 * 
	 * @return Configuration object, built from XML
	 */
	@Bean
	public Config config() {
		return new ClasspathXmlConfig("hazelcast.xml");
	}
	
	/**
	 * <P>Return a Hazelcast Jet server as a Spring bean.
	 * </P>
	 * 
	 * @param config Created above
	 * @return A Hazelcast Jet server
	 */
	@Bean
	public JetInstance jetInstance(Config config) {
		JetConfig jetConfig = new JetConfig();
		
		jetConfig.setHazelcastConfig(config);
		
		return Jet.newJetInstance(jetConfig);
	}

	/**
	 * <P>Return a Hazelcast IMDG server as a Spring bean.
	 * </P>
	 * 
	 * @param jetInstance Created above
	 * @return A Hazelcast IMDG server, enriched with Jet
	 */
	@Bean
	public HazelcastInstance hazelcastInstance(JetInstance jetInstance) {
		return jetInstance.getHazelcastInstance();
	}

}

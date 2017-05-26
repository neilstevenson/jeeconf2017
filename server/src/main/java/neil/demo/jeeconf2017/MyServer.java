package neil.demo.jeeconf2017;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <P>Start Spring Boot, which will find the necessary {@code @Bean}s
 * and create a Hazelcast IMDG server enriched with Jet.
 * </P>
 */
@SpringBootApplication
public class MyServer {

	public static void main(String[] args) {
		SpringApplication.run(MyServer.class, args);
	}
        
}


package neil.demo.jeeconf2017;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <P>Start Spring Boot, which will find the necessary {@code @Bean}s
 * and create a Hazelcast IMDG client enriched with Jet.
 * </P>
 * <P>This client provides a GUI available as:
 * <P>
 * <A HREF="http://localhost:8080">http://localhost:8080</A>
 * <P>As a GUI, it won't win prizes, not the kind of prizes you'd want
 * to win anyway. The point here is to show the data and the processing.
 * </P>
 */
@SpringBootApplication
public class MyClient {

	public static void main(String[] args) {
		SpringApplication.run(MyClient.class, args);
	}
        
}


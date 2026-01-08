package sry.mail.BybitCalculator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class BybitCalculatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(BybitCalculatorApplication.class, args);
	}

}

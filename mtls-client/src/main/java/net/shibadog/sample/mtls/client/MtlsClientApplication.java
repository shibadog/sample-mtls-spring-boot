package net.shibadog.sample.mtls.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class MtlsClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(MtlsClientApplication.class, args);
	}

	@RestController
	static class ClientController {
		
	}
}

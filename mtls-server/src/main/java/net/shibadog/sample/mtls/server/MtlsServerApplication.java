package net.shibadog.sample.mtls.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@SpringBootApplication
public class MtlsServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MtlsServerApplication.class, args);
	}

	@RestController
	static class ServerController {
		@GetMapping(value="/")
		public String getTest() {
			return "OK";
		}
	}
}

package net.shibadog.sample.mtls.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
public class MtlsClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(MtlsClientApplication.class, args);
	}

	@RestController
	static class ClientController {
		@Autowired
		private ClientService clientService;

		@GetMapping(value="/test")
		public String getTest() {
			return "test OK";
		}

		@GetMapping(value="/")
		public String getRequestTest() {
			return clientService.post();
		}
	}

	@Configuration
	static class RestTemplateConfig {
		@Autowired
		private RestTemplateBuilder restTemplateBuilder;
		@Autowired
		private ResourceLoader resourceLoader;

		@Bean
		RestTemplate restTemplate(
				@Value("${app.keystore.key}") String pkey,
				@Value("${app.keystore.password}") String keyStorePassword,
				@Value("${app.cacert.filepath}") String caCertPath,
				@Value("${app.cacert.alias}") String caCertAlias
		) {
			Supplier<ClientHttpRequestFactory> requestFactory = () -> {			
				try {
					SSLContext sslContext =
							createSslContext("TLSv1.3", pkey, keyStorePassword, caCertPath, caCertAlias);

					HttpClient httpClient = createHttpClient(sslContext);

					return new HttpComponentsClientHttpRequestFactory(httpClient);
				} catch (Exception e) {
					return null;
				}
			};

			return restTemplateBuilder
					.requestFactory(requestFactory)
					.build();
		}

		SSLContext createSslContext(String protocol, String pkey, String keyStorePassword, String certificatePath, String alias)
				throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {
			SSLContext sslContext = SSLContext.getInstance(protocol);

			// Create Key Manager
			KeyManager[] keyManagers = createKeyManagers(pkey, keyStorePassword);

			// Create Trust Manager
			TrustManager[] trustManagers = createTrustManagers(certificatePath, alias);

			// Init SSLContext
			sslContext.init(keyManagers, trustManagers, new SecureRandom());

			return sslContext;
		}

		KeyManager[] createKeyManagers(String pkey, String keyStorePassword)
				throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException {
			KeyManager[] keyManagers = null;

			if (pkey != null) {
				try (InputStream is = new ByteArrayInputStream(Base64.getDecoder().decode(pkey))) {
					// See https://docs.oracle.com/javase/jp/8/docs/technotes/guides/security/StandardNames.html#KeyStore
					KeyStore keyStore = KeyStore.getInstance("pkcs12");
					keyStore.load(is, keyStorePassword.toCharArray());

					KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
					kmf.init(keyStore, keyStorePassword.toCharArray());
					keyManagers = kmf.getKeyManagers();
				}
			}

			return keyManagers;
		}

		private TrustManager[] createTrustManagers(String certificatePath, String alias)
				throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
			TrustManager[] trustManagers = null;

			if (certificatePath != null) {
				KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
				trustStore.load(null);

				try (InputStream is = new BufferedInputStream(resourceLoader.getResource(certificatePath).getInputStream())) {
					// See https://docs.oracle.com/javase/jp/8/docs/technotes/guides/security/StandardNames.html#CertificateFactory
					Certificate certificate = CertificateFactory.getInstance("X.509")
							.generateCertificate(is);

					trustStore.setCertificateEntry(alias, certificate);

					TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
					tmf.init(trustStore);
					trustManagers = tmf.getTrustManagers();
				}
			}

			return trustManagers;
		}

		HttpClient createHttpClient(SSLContext sslContext) {
			String[] supportedProtocols = {"TLSv1.2", "TLSv1.3"};
			String[] supportedCipherSuites = null;
			HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;

			SSLConnectionSocketFactory socketFactory =
					new SSLConnectionSocketFactory(sslContext, supportedProtocols, supportedCipherSuites, hostnameVerifier);

			Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
					.register("http", PlainConnectionSocketFactory.getSocketFactory())
					.register("https", socketFactory)
					.build();

			PoolingHttpClientConnectionManager connectionManager =
					new PoolingHttpClientConnectionManager(registry, null, null,
					TimeValue.of(3000L, TimeUnit.MILLISECONDS));
			connectionManager.setMaxTotal(20);
			connectionManager.setDefaultMaxPerRoute(2);

			RequestConfig requestConfig = RequestConfig.custom().build();

			return HttpClientBuilder.create()
					.setConnectionManager(connectionManager)
					.setDefaultRequestConfig(requestConfig)
					.build();
		}
	}

	@Service
	static class ClientService {
		@Autowired
		private RestTemplate restTemplate;

		public String post() {
			return restTemplate.getForObject("https://localhost:8443/", String.class);
		}
	}
}

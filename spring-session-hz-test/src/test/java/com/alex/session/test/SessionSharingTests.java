package com.alex.session.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@EnableAsync
public class SessionSharingTests {

	private static final Logger LOGGER = LoggerFactory.getLogger(SessionSharingTests.class);

	@TestConfiguration
	static class EmployeeServiceImplTestContextConfiguration {

		@Value("${test.corePoolSize:50}")
		private int corePoolSize;

		@Bean(name = "threadPoolTaskExecutor")
		public Executor threadPoolTaskExecutor() {
			ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
			threadPoolTaskExecutor.setCorePoolSize(this.corePoolSize);
			threadPoolTaskExecutor.setThreadGroupName("async");
			threadPoolTaskExecutor.setThreadNamePrefix("async");
			return threadPoolTaskExecutor;
		}

		@Bean
		public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
			return restTemplateBuilder.setConnectTimeout(2000).setReadTimeout(2000).build();
		}

		/*@Bean
		public ClientHttpRequestFactory createRequestFactory() {
			PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
			connectionManager.setMaxTotal(100);
			connectionManager.setDefaultMaxPerRoute(50);
		
			RequestConfig config = RequestConfig.custom().setConnectTimeout(2000).build();
			CloseableHttpClient httpClient = HttpClientBuilder.create().setConnectionManager(connectionManager)
					.setDefaultRequestConfig(config).build();
			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
			return requestFactory;
		}
		
		@Bean
		@Qualifier("myRestService")
		public RestTemplate createRestTemplate(ClientHttpRequestFactory factory) {
			RestTemplate restTemplate = new RestTemplate(factory);
		
			return restTemplate;
		}*/
	}

	@Autowired
	RequestUtil requestUtil;

	public static final int EXECUTIONS_NO = 20;
	public static final int ITERATIONS_PER_EXECUTION_NO = 50;

	@Test
	public void executeTestNTimes() throws Exception {
		long testRaceConditions = 0;
		for (int i = 0; i < EXECUTIONS_NO; i++) {
			testRaceConditions += testRaceConditions();
		}
		LOGGER.info("ALL LASTED: {}ms", testRaceConditions / EXECUTIONS_NO);
	}

	// @Test
	public long testRaceConditions() throws Exception {
		List<Future<Boolean>> results = new ArrayList<>();
		HttpHeaders headers = requestUtil.init();

		long start = System.currentTimeMillis();

		for (int i = 0; i < ITERATIONS_PER_EXECUTION_NO; i++) {
			results.add(requestUtil.testWriteAndRead(i, headers));
		}

		for (Future<Boolean> result : results) {
			result.get();
		}

		LOGGER.info("EXECUTION LASTED: {}ms", System.currentTimeMillis() - start);

		return System.currentTimeMillis() - start;
	}
}

package com.alex.session.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RequestUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequestUtil.class);

	@Autowired
	RestTemplate restTemplate;

	@Value("${test.app1:http://localhost:8080/?attName=%s}")
	String APP_1_URL;

	@Value("${test.app2:http://localhost:9090/?attName=%s}")
	String APP_2_URL;

	static final int ITERATIONS_NO = 100;

	@Async("threadPoolTaskExecutor")
	public Future<Boolean> testWriteAndRead(int orderNo, HttpHeaders headers) {

		String attName = "attName_" + orderNo;
		String writeURL = null;
		String readURL = null;
		if (orderNo % 2 == 0) {
			writeURL = APP_1_URL;
			readURL = APP_2_URL;
		} else {
			writeURL = APP_2_URL;
			readURL = APP_1_URL;
		}

		for (int i = 0; i < ITERATIONS_NO; i++) {
			testWriteAndReadOnce(orderNo, i % 2 == 0 ? writeURL : readURL, i % 2 == 1 ? writeURL : readURL, attName,
					headers);
		}

		return new AsyncResult<Boolean>(Boolean.TRUE);
	}

	private void testWriteAndReadOnce(int orderNo, String writeURL, String readURL, String attName,
			HttpHeaders headers) {
		String attValue = String.valueOf(System.currentTimeMillis()) + ":" + orderNo;

		write(writeURL, attName, attValue, headers);
		String readValue = read(readURL, attName, headers);

		if (!attValue.equals(readValue)) {
			LOGGER.error("ERROR COMPARING RESULTS. EXPECTED: " + attValue + " BUT GOT: " + readValue);
			throw new RuntimeException("ERROR COMPARING RESULTS");
		}

		LOGGER.info("Comparation successful");
	}

	private void write(String writeURL, String attName, String attValue, HttpHeaders headers) {
		LOGGER.info("Writting to {} attName: {} attValue: {}", writeURL, attName, attValue);
		String executeURL = String.format(writeURL + "&attValue=%s", attName, attValue);

		this.restTemplate.exchange(executeURL, HttpMethod.POST, new HttpEntity<String>(headers), Void.class);
	}

	private String read(String readURL, String attName, HttpHeaders headers) {
		LOGGER.info("Reading from {} attName: {}", readURL, attName);
		String executeURL = String.format(readURL, attName);

		ResponseEntity<String> response = this.restTemplate.exchange(executeURL, HttpMethod.GET,
				new HttpEntity<String>(headers), String.class);
		return response.getBody();
	}

	public HttpHeaders init() {
		String executeURL = String.format(APP_1_URL + "&attValue=%s", "dummy", "dummy");

		// write something to a session so we make sure a session object is
		// created
		ResponseEntity<Void> response = this.restTemplate.postForEntity(executeURL, null, Void.class);
		List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
		if (cookies == null) {
			cookies = new ArrayList<>();
		}

		HttpHeaders headers = new HttpHeaders();
		String allCookies = "";
		for (String cookie : cookies) {
			allCookies += cookie.substring(0, cookie.indexOf(";") + 1) + " ";
		}
		headers.add("Cookie", allCookies);

		executeURL = String.format(APP_2_URL, "dummy");

		ResponseEntity<String> response2 = this.restTemplate.exchange(executeURL, HttpMethod.GET,
				new HttpEntity<String>(headers), String.class);

		cookies = response2.getHeaders().get(HttpHeaders.SET_COOKIE);

		if (cookies != null) {
			for (String cookie : cookies) {
				String cookieShort = cookie.substring(0, cookie.indexOf(";") + 2);
				if (!allCookies.contains(cookieShort)) {
					allCookies += cookieShort;
				}
			}
			headers.remove("Cookie");
			headers.add("Cookie", allCookies);
		}
		return headers;
	}
}

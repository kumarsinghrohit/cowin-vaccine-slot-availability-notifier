package com.example.cowin.service;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class WebClientService {

	// Set the below header, otherwise, you may get 403 error
	private static final String HEADERS = "'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 Safari/537.36'";
	private static final String COVINURL = "https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/calendarByPin";

	private final WebClient webClient;

	public WebClientService() {
		this.webClient = WebClient.builder().build();
	}

	public List<String> get(String pinCode, String date) {
		URI uri = UriComponentsBuilder.fromUriString(COVINURL).queryParam("pincode", pinCode).queryParam("date", date)
				.build().toUri();
		return webClient.get().uri(uri).header(HEADERS).retrieve().bodyToFlux(String.class)
				.onErrorResume(WebClientResponseException.class,
						ex -> ex.getStatusCode() == HttpStatus.NOT_FOUND ? Flux.empty() : Mono.error(ex))
				.collectList().block();
	}
}

package com.example.cowin.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppInventoryRestApiController {

	@GetMapping("")
	public ResponseEntity<HttpStatus> getStatus() {
		return new ResponseEntity<>(HttpStatus.OK);
	}
}

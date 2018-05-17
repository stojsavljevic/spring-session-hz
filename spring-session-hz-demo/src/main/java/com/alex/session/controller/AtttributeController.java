package com.alex.session.controller;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AtttributeController {

	@GetMapping("/")
	public String getAttribute(@RequestParam String attName, HttpSession session) {
		return (String) session.getAttribute(attName);
	}

	@PostMapping("/")
	public void setAttribute(@RequestParam String attName, @RequestParam String attValue, HttpSession session) {
		session.setAttribute(attName, attValue);
	}
}

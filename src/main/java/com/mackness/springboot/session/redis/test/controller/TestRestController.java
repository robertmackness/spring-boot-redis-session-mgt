package com.mackness.springboot.session.redis.test.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestRestController {

	@RequestMapping("/")
	public String helloTest() {
		return "hello test";
	}
}

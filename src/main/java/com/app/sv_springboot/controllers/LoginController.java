package com.app.sv_springboot.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.sv_springboot.dtos.LoginCheckDto;
import com.app.sv_springboot.services.LoginService;

@CrossOrigin(origins = "*")
@RestController
public class LoginController {

	private static Logger logger = LoggerFactory.getLogger(LoginController.class);

	@Autowired
	LoginService loginService;

	@GetMapping("/login")
	public LoginCheckDto checkLogin(@RequestParam("role") String role, @RequestParam("emailId") String emailId,
			@RequestParam("password") String password) {
		try {
			return loginService.checkLogin(role, emailId, password);
		} catch (Exception e) {
			logger.error("An error has occurred while gfetching Login Status {} ", e.getMessage(), e);
			return null;
		}
	}
	
	@PostMapping("/register")
	public LoginCheckDto registerUser(@RequestBody LoginCheckDto regSet) {
		try {
			return loginService.registerUser(regSet);
		} catch (Exception e) {
			logger.error("An error has occurred while gfetching Login Status {} ", e.getMessage(), e);
			return null;
		}
	}

}

package com.app.sv_springboot.services;

import com.app.sv_springboot.dtos.LoginCheckDto;

public interface LoginService {

	public LoginCheckDto checkLogin(String role, String emailId, String password);

	public LoginCheckDto registerUser(LoginCheckDto regSet);

}

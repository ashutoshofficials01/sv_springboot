package com.app.sv_springboot.services;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import com.app.sv_springboot.dtos.SendResponseDto;

public interface ScoreService {

	public SendResponseDto resumeUpload(MultipartFile file, String userId);

}

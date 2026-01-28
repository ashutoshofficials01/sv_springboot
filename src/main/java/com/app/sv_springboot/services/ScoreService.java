package com.app.sv_springboot.services;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

public interface ScoreService {

	public Map<String, Object> resumeUpload(MultipartFile file, String userId);

}

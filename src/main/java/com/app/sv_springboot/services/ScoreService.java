package com.app.sv_springboot.services;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import com.app.sv_springboot.dtos.SendResponseDto;

public interface ScoreService {

	public SendResponseDto resumeUploadIT(MultipartFile file, String userId);

	public SendResponseDto resumeUploadConstruction(MultipartFile file, String userId);

	public SendResponseDto resumeUploadEducation(MultipartFile file, String userId);

	public SendResponseDto resumeUploadFinance(MultipartFile file, String userId);

	public SendResponseDto resumeUploadBusiness(MultipartFile file, String userId);

	public SendResponseDto resumeUploadTourism(MultipartFile file, String userId);

	public SendResponseDto resumeUploadHealthcare(MultipartFile file, String userId);

	public SendResponseDto resumeUploadLegal(MultipartFile file, String userId);

	public SendResponseDto resumeUploadGovernment(MultipartFile file, String userId);

	public SendResponseDto resumeUploadDesign(MultipartFile file, String userId);

}

package com.app.sv_springboot.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.*;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import java.io.InputStream;

@Component
public class FileConversionUtil {

	public String extractResumeText(MultipartFile file) throws Exception {

		String fileName = file.getOriginalFilename();
		if (fileName == null) {
			return "";
		}

		String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

		switch (extension) {

		case "pdf":
			try (PDDocument document = PDDocument.load(file.getBytes())) {
				PDFTextStripper stripper = new PDFTextStripper();
				return stripper.getText(document).toLowerCase();
			}

		case "docx":
			try (XWPFDocument doc = new XWPFDocument(file.getInputStream());
					XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
				return extractor.getText().toLowerCase();
			}

		case "doc":
			try (HWPFDocument doc = new HWPFDocument(file.getInputStream())) {
				WordExtractor extractor = new WordExtractor(doc);
				return extractor.getText().toLowerCase();
			}

		case "txt":
			return new String(file.getBytes()).toLowerCase();

		default:
			throw new IllegalArgumentException("Unsupported file type: " + extension);
		}
	}

}

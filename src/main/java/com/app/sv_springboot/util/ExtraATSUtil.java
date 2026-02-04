package com.app.sv_springboot.util;

import java.util.HashSet;
import java.util.Set;

import org.apache.pdfbox.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.*;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.InputStream;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ExtraATSUtil {

	public Set<String> extractFontsFromResume(MultipartFile file) throws Exception {
		Set<String> fonts = new HashSet<>();
		try {

			String fileName = file.getOriginalFilename();
			if (fileName == null) {
				return fonts;
			}

			String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

			switch (extension) {
			case "docx":
				try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
					for (XWPFParagraph para : document.getParagraphs()) {
						for (XWPFRun run : para.getRuns()) {
							if (run.getFontFamily() != null) {
								fonts.add(run.getFontFamily().toLowerCase());
							}
						}
					}
				}
				return fonts;

			case "doc":
				try (HWPFDocument document = new HWPFDocument(file.getInputStream())) {
					Range range = document.getRange();
					for (int i = 0; i < range.numParagraphs(); i++) {
						Paragraph para = range.getParagraph(i);
						for (int j = 0; j < para.numCharacterRuns(); j++) {
							CharacterRun run = para.getCharacterRun(j);
							if (run.getFontName() != null) {
								fonts.add(run.getFontName().toLowerCase());
							}
						}
					}
				}
				return fonts;

			case "pdf":
				/*
				 * PDF font extraction is unreliable. ATS engines usually treat text-based PDFs
				 * as acceptable and scanned PDFs as poor. So we DO NOT extract fonts here.
				 */
				return fonts;

			case "txt":
				/*
				 * Plain text has no font metadata. ATS assumes default machine-readable font.
				 */
				return fonts;

			default:
				throw new IllegalArgumentException("Unsupported file type: " + extension);

			}

		} catch (Exception e) {
			System.err.println(e);
			return null;
		}

	}

}

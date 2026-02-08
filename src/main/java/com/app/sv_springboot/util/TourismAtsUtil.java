package com.app.sv_springboot.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.app.sv_springboot.dao.ATSGeneralParamRepo;
import com.app.sv_springboot.dtos.AtsGenParamDto;
import com.app.sv_springboot.dtos.AtsListDto;
import com.app.sv_springboot.entities.ATS_General_Param_Entity;

import jakarta.annotation.PostConstruct;
import tools.jackson.databind.ObjectMapper;

@Component
public class TourismAtsUtil {

//	private final AtsGenParamDto atsGenParamDto;

	private static Logger logger = LoggerFactory.getLogger(GeneralATSUtil.class);

	@Autowired
	ATSGeneralParamRepo atsGeneralParamRepo;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	FileConversionUtil fileConUtil;

	@Autowired
	ExtraATSUtil extraATSUtil;

	private List<Map<String, Object>> generalTourismATSParameterList;

	private Map<String, Map<String, Object>> generalTourismATSParameterMap;

//	@PostConstruct
//	ITATSUtil(AtsGenParamDto atsGenParamDto) {
//		this.atsGenParamDto = atsGenParamDto;
//	}

	@PostConstruct
	public void loadGeneralATSParameters() {
		try {

			Resource resource = resourceLoader.getResource("classpath:Tourism_ATSParameter.json");

			// Read JSON as Map
			Map<String, Object> jsonData = objectMapper.readValue(resource.getInputStream(), Map.class);

			// Extract array
			generalTourismATSParameterList = (List<Map<String, Object>>) jsonData.get("Tourism_ATSParameter");

			// Create fast-lookup map
			generalTourismATSParameterMap = new HashMap<>();

			for (Map<String, Object> param : generalTourismATSParameterList) {
				Number atsGeneralIdNum = (Number) param.get("atsGeneralId");
				long atsGeneralId = atsGeneralIdNum.longValue();
				String atsParamId = (String) param.get("atsParamId");

				String key = atsGeneralId + "_" + atsParamId;
				generalTourismATSParameterMap.put(key, param);
			}

			System.out.println("Loaded ATS Params Count: " + generalTourismATSParameterMap.size());

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load generalATSParameters.json", e);
		}
	}

	public List<Map<String, Object>> getAllGeneralATSParams() {
//		System.out.println("getAllGeneralATSParams() --> generalTourismATSParameterList :: " + generalTourismATSParameterList);
		return generalTourismATSParameterList;
	}

	public Map<String, Object> getGeneralTourismATSParam(long atsGeneralId, String atsParamId) {
//		System.out.println("getGeneralTourismATSParam(long " + atsGeneralId + ", String " + atsParamId
//				+ ") --> generalTourismATSParameterMap :: " + generalTourismATSParameterMap.get(atsGeneralId + "_" + atsParamId));
		return generalTourismATSParameterMap.get(atsGeneralId + "_" + atsParamId);
	}

	public AtsListDto calculateATS271(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts271 = new AtsListDto();
		AtsGenParamDto calAtsGen271 = new AtsGenParamDto();
		long ats271_points = 0;
		try {

			// 1️ Fetch JSON parameter config
			Map<String, Object> ats271ParamData = getGeneralTourismATSParam(atsGeneralId, atsParamId);
			if (ats271ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config (points / penalty)
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);
			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats271ParamData.get("logic_description");

			List<String> keywords = (List<String>) logicDescription.get("keywords");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDescription.get("matching_rules");

			boolean caseSensitive = (boolean) matchingRules.get("case_sensitive");

			long minPartial = ((Number) matchingRules.get("minimum_keyword_match_for_partial_score")).longValue();

			long minFull = ((Number) matchingRules.get("minimum_keyword_match_for_full_score")).longValue();

//	     4️ Read resume content
//			String resumeText = new String(file.getBytes());
			String resumeText = fileConUtil.extractResumeText(file);
			resumeText = resumeText.replaceAll("\\s+", " ").trim();

			if (!caseSensitive) {
				resumeText = resumeText.toLowerCase();
			}

//	     5️ Keyword matching
			long matchCount = 0;

			for (String keyword : keywords) {
				String key = caseSensitive ? keyword : keyword.toLowerCase();
				if (resumeText.contains(key)) {
					System.out.println("ATS-271 :: " + key);
					matchCount++;
				}
			}

//	     6️ Scoring logic
			if (matchCount >= minFull) {
				ats271_points = genATSParamData.getMax_points();
			} else if ((matchCount >= minPartial) && (matchCount < minFull)) {
				ats271_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if ((matchCount <= 0) && (matchCount < minPartial)) {
				ats271_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats271_points == max) {
				paramType = "positive";
			} else if (ats271_points == partial) {
				paramType = "partial";
			} else if (ats271_points == 0) {
				paramType = "negative";
			}

			calAtsGen271.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen271.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen271.setCategory(genATSParamData.getCategory());
			calAtsGen271.setDescription(genATSParamData.getDescription());
			calAtsGen271.setMax_points(genATSParamData.getMax_points());
			calAtsGen271.setParameter(genATSParamData.getParameter());
			calAtsGen271.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen271.setTotal_points(genATSParamData.getTotal_points());

			calAts271.setAtsGeneralId(atsGeneralId);
			calAts271.setAtsGeneralParamDto(calAtsGen271);
			calAts271.setAtsParamData(ats271ParamData);
			calAts271.setAtsParamId(atsParamId);
			calAts271.setAtsParamType(paramType);
			calAts271.setAtsScore(ats271_points);

			return calAts271;

		} catch (Exception e) {
			e.printStackTrace();
			return calAts271;
		}
	}

	public AtsListDto calculateATS272(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts272 = new AtsListDto();
		AtsGenParamDto calAtsGen272 = new AtsGenParamDto();
		long ats272_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats272ParamData = getGeneralTourismATSParam(atsGeneralId, atsParamId);

			if (ats272ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats272ParamData.get("logic_description");

			Map<String, List<String>> skillCategories = (Map<String, List<String>>) logicDescription
					.get("skill_categories");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDescription.get("matching_rules");

			boolean caseSensitive = (boolean) matchingRules.get("case_sensitive");

			long minPartial = ((Number) matchingRules.get("minimum_category_match_for_partial_score")).longValue();

			long minFull = ((Number) matchingRules.get("minimum_category_match_for_full_score")).longValue();

			// 4️ Read resume content
			String resumeText = fileConUtil.extractResumeText(file);
			resumeText = resumeText.replaceAll("\\s+", " ").trim();

			if (!caseSensitive) {
				resumeText = resumeText.toLowerCase();
			}

			// 5️ Category matching
			long matchedCategories = 0;

			for (Map.Entry<String, List<String>> entry : skillCategories.entrySet()) {
				for (String keyword : entry.getValue()) {

					String key = caseSensitive ? keyword : keyword.toLowerCase();

					if (resumeText.contains(key)) {
						System.out.println("ATS-272 :: " + key);
						matchedCategories++;
					}
				}
			}

			// 6️ Scoring logic
			if (matchedCategories >= minFull) {
				ats272_points = genATSParamData.getMax_points();
			} else if (matchedCategories >= minPartial && matchedCategories < minFull) {
				ats272_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else {
				ats272_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats272_points == max) {
				paramType = "positive";
			} else if (ats272_points == partial) {
				paramType = "partial";
			} else if (ats272_points == 0) {
				paramType = "negative";
			}

			calAtsGen272.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen272.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen272.setCategory(genATSParamData.getCategory());
			calAtsGen272.setDescription(genATSParamData.getDescription());
			calAtsGen272.setMax_points(genATSParamData.getMax_points());
			calAtsGen272.setParameter(genATSParamData.getParameter());
			calAtsGen272.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen272.setTotal_points(genATSParamData.getTotal_points());

			calAts272.setAtsGeneralId(atsGeneralId);
			calAts272.setAtsGeneralParamDto(calAtsGen272);
			calAts272.setAtsParamData(ats272ParamData);
			calAts272.setAtsParamId(atsParamId);
			calAts272.setAtsParamType(paramType);
			calAts272.setAtsScore(ats272_points);

			return calAts272;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts272;
		}
	}

	public AtsListDto calculateATS273(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts273 = new AtsListDto();
		AtsGenParamDto calAtsGen273 = new AtsGenParamDto();
		long ats273_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats273ParamData = getGeneralTourismATSParam(atsGeneralId, atsParamId);

			if (ats273ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats273ParamData.get("logic_description");

			Map<String, List<String>> keywordVariations = (Map<String, List<String>>) logicDescription
					.get("keyword_variations");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDescription.get("matching_rules");

			boolean caseSensitive = (boolean) matchingRules.get("case_sensitive");

			long minPartial = ((Number) matchingRules.get("minimum_variation_match_for_partial_score")).longValue();

			long minFull = ((Number) matchingRules.get("minimum_variation_match_for_full_score")).longValue();

			// 4 Read resume content
			String resumeText = fileConUtil.extractResumeText(file);
			resumeText = (resumeText.replaceAll("\\s+", " ").trim());

			if (!caseSensitive) {
				resumeText = resumeText.toLowerCase();
			}

			// 5
			long matchedVariationGroups = 0;

			for (Map.Entry<String, List<String>> entry : keywordVariations.entrySet()) {

				List<String> variants = entry.getValue();

				if (variants == null || variants.size() < 2) {
					continue;
				}

				String correctWord = caseSensitive ? variants.get(0) : variants.get(0).toLowerCase();

				boolean canonicalPresent = resumeText.contains(correctWord);

				boolean alternatePresent = false;

				for (int i = 1; i < variants.size(); i++) {
					String alternate = caseSensitive ? variants.get(i) : variants.get(i).toLowerCase();
					if (resumeText.contains(" " + alternate + " ")) {
						System.out.println("ATS-273 :: " + alternate);
						alternatePresent = true;
						break;
					}
				}

				if (alternatePresent) {
					matchedVariationGroups++;
				}
			}

			// 6
			if (matchedVariationGroups > minFull) {
				ats273_points = 0;
			} else if (matchedVariationGroups <= minFull && matchedVariationGroups > minPartial) {
				ats273_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedVariationGroups <= minPartial) {
				ats273_points = genATSParamData.getMax_points();
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats273_points == max) {
				paramType = "positive";
			} else if (ats273_points == partial) {
				paramType = "partial";
			} else if (ats273_points == 0) {
				paramType = "negative";
			}

			calAtsGen273.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen273.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen273.setCategory(genATSParamData.getCategory());
			calAtsGen273.setDescription(genATSParamData.getDescription());
			calAtsGen273.setMax_points(genATSParamData.getMax_points());
			calAtsGen273.setParameter(genATSParamData.getParameter());
			calAtsGen273.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen273.setTotal_points(genATSParamData.getTotal_points());

			calAts273.setAtsGeneralId(atsGeneralId);
			calAts273.setAtsGeneralParamDto(calAtsGen273);
			calAts273.setAtsParamData(ats273ParamData);
			calAts273.setAtsParamId(atsParamId);
			calAts273.setAtsParamType(paramType);
			calAts273.setAtsScore(ats273_points);

			return calAts273;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts273;
		}
	}

	public AtsListDto calculateATS274(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts274 = new AtsListDto();
		AtsGenParamDto calAtsGen274 = new AtsGenParamDto();
		long ats274_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats274ParamData = getGeneralTourismATSParam(atsGeneralId, atsParamId);

			if (ats274ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats274ParamData.get("logic_description");

			Map<String, List<String>> acceptedFormats = (Map<String, List<String>>) logicDescription
					.get("accepted_formats");

			List<String> fullScoreFormats = acceptedFormats.get("full_score");
			List<String> partialScoreFormats = acceptedFormats.get("partial_score");

			// 4️ Extract file extension
			if (fileName == null || !fileName.contains(".")) {
				return null;
			}

			String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

			System.out.println("ATS-274 :: " + extension);

			// 5️ Scoring logic
			if (fullScoreFormats.contains(extension)) {
				ats274_points = genATSParamData.getMax_points();
			} else if (partialScoreFormats.contains(extension)) {
				ats274_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else {
				ats274_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats274_points == max) {
				paramType = "positive";
			} else if (ats274_points == partial) {
				paramType = "partial";
			} else if (ats274_points == 0) {
				paramType = "negative";
			}

			calAtsGen274.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen274.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen274.setCategory(genATSParamData.getCategory());
			calAtsGen274.setDescription(genATSParamData.getDescription());
			calAtsGen274.setMax_points(genATSParamData.getMax_points());
			calAtsGen274.setParameter(genATSParamData.getParameter());
			calAtsGen274.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen274.setTotal_points(genATSParamData.getTotal_points());

			calAts274.setAtsGeneralId(atsGeneralId);
			calAts274.setAtsGeneralParamDto(calAtsGen274);
			calAts274.setAtsParamData(ats274ParamData);
			calAts274.setAtsParamId(atsParamId);
			calAts274.setAtsParamType(paramType);
			calAts274.setAtsScore(ats274_points);

			return calAts274;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts274;
		}
	}

	public AtsListDto calculateATS275(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts275 = new AtsListDto();
		AtsGenParamDto calAtsGen275 = new AtsGenParamDto();
		long ats275_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats275ParamData = getGeneralTourismATSParam(atsGeneralId, atsParamId);

			if (ats275ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats275ParamData.get("logic_description");

			Map<String, List<String>> layoutIndicators = (Map<String, List<String>>) logicDescription
					.get("layout_indicators");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDescription.get("matching_rules");

			long maxFull = ((Number) matchingRules.get("max_allowed_layout_violations_for_full_score")).longValue();

			long maxPartial = ((Number) matchingRules.get("max_allowed_layout_violations_for_partial_score"))
					.longValue();

			// 4️ Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);
			resumeText = resumeText.replaceAll("\\s+", " ").trim();

			// 5️ Count layout violations
			long layoutViolations = 0;

			for (List<String> indicators : layoutIndicators.values()) {
				for (String indicator : indicators) {
					if (resumeText.contains(indicator)) {
						System.out.println("ATS-275 :: " + indicator);
						layoutViolations++;
					}
				}
			}

			// 6 Scoring Logic
			if (layoutViolations <= maxFull) {
				ats275_points = genATSParamData.getMax_points();
			} else if (layoutViolations <= maxPartial) {
				ats275_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else {
				ats275_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats275_points == max) {
				paramType = "positive";
			} else if (ats275_points == partial) {
				paramType = "partial";
			} else if (ats275_points == 0) {
				paramType = "negative";
			}

			calAtsGen275.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen275.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen275.setCategory(genATSParamData.getCategory());
			calAtsGen275.setDescription(genATSParamData.getDescription());
			calAtsGen275.setMax_points(genATSParamData.getMax_points());
			calAtsGen275.setParameter(genATSParamData.getParameter());
			calAtsGen275.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen275.setTotal_points(genATSParamData.getTotal_points());

			calAts275.setAtsGeneralId(atsGeneralId);
			calAts275.setAtsGeneralParamDto(calAtsGen275);
			calAts275.setAtsParamData(ats275ParamData);
			calAts275.setAtsParamId(atsParamId);
			calAts275.setAtsParamType(paramType);
			calAts275.setAtsScore(ats275_points);

			return calAts275;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts275;
		}
	}

	public AtsListDto calculateATS276(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts276 = new AtsListDto();
		AtsGenParamDto calAtsGen276 = new AtsGenParamDto();
		long ats276_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats276ParamData = getGeneralTourismATSParam(atsGeneralId, atsParamId);

			if (ats276ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats276ParamData.get("logic_description");

			Map<String, Object> fontRules = (Map<String, Object>) logicDescription.get("font_rules");

			List<String> standardFonts = (List<String>) fontRules.get("standard_fonts");

			List<String> nonStandardFonts = (List<String>) fontRules.get("non_standard_fonts");

			// 4 Extract matching_rules
			Map<String, Object> matchingRules = (Map<String, Object>) logicDescription.get("matching_rules");

			boolean caseSensitive = (boolean) matchingRules.get("case_sensitive");

			boolean fullScoreIfStandard = (boolean) matchingRules.get("full_score_if_only_standard_fonts");

			boolean partialScoreIfMixed = (boolean) matchingRules.get("partial_score_if_mixed_fonts");

			boolean zeroScoreIfNonStandard = (boolean) matchingRules.get("zero_score_if_only_non_standard_fonts");

			long fullScore = genATSParamData.getMax_points();
			long partialScore = genATSParamData.getPenalty_points();
			long zeroScore = 0;

			// 5 Extract fonts from resume
			Set<String> detectedFonts = extraATSUtil.extractFontsFromResume(file);

			System.out.println("ATS-276 :: " + detectedFonts);

			// 6 Fonts cannot be detected
			if (detectedFonts == null || detectedFonts.isEmpty()) {
				AtsListDto calAtsNull = new AtsListDto();
				AtsGenParamDto calAtsGenNull = new AtsGenParamDto();

				calAtsGenNull.setAtsGeneralId(genATSParamData.getAtsGeneralId());
				calAtsGenNull.setAtsParamId(genATSParamData.getAtsParamId());
				calAtsGenNull.setCategory(genATSParamData.getCategory());
				calAtsGenNull.setDescription(genATSParamData.getDescription());
				calAtsGenNull.setMax_points(genATSParamData.getMax_points());
				calAtsGenNull.setParameter(genATSParamData.getParameter());
				calAtsGenNull.setPenalty_points(genATSParamData.getPenalty_points());
				calAtsGenNull.setTotal_points(genATSParamData.getTotal_points());

				calAtsNull.setAtsGeneralId(atsGeneralId);
				calAtsNull.setAtsGeneralParamDto(calAtsGenNull);
				calAtsNull.setAtsParamData(ats276ParamData);
				calAtsNull.setAtsParamId(atsParamId);
				calAtsNull.setAtsParamType("negative");
				calAtsNull.setAtsScore(0);

				return calAtsNull;
			}

			boolean hasStandard = false;
			boolean hasNonStandard = false;

			for (String font : detectedFonts) {
				String fontName = caseSensitive ? font : font.toLowerCase();

				if (standardFonts.contains(fontName)) {
					hasStandard = true;
				}

				if (nonStandardFonts.contains(fontName)) {
					hasNonStandard = true;
				}
			}

			// 7️ Scoring logic (CONFIG DRIVEN)
			if (hasNonStandard && !hasStandard && zeroScoreIfNonStandard) {
				ats276_points = 0;
			} else if (hasStandard && hasNonStandard && partialScoreIfMixed) {
				ats276_points = fullScore - partialScore;
			} else if (hasStandard && !hasNonStandard && fullScoreIfStandard) {
				ats276_points = fullScore;
			} else {
				ats276_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats276_points == max) {
				paramType = "positive";
			} else if (ats276_points == partial) {
				paramType = "partial";
			} else if (ats276_points == 0) {
				paramType = "negative";
			}

			calAtsGen276.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen276.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen276.setCategory(genATSParamData.getCategory());
			calAtsGen276.setDescription(genATSParamData.getDescription());
			calAtsGen276.setMax_points(genATSParamData.getMax_points());
			calAtsGen276.setParameter(genATSParamData.getParameter());
			calAtsGen276.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen276.setTotal_points(genATSParamData.getTotal_points());

			calAts276.setAtsGeneralId(atsGeneralId);
			calAts276.setAtsGeneralParamDto(calAtsGen276);
			calAts276.setAtsParamData(ats276ParamData);
			calAts276.setAtsParamId(atsParamId);
			calAts276.setAtsParamType(paramType);
			calAts276.setAtsScore(ats276_points);

			return calAts276;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts276;
		}
	}

	public AtsListDto calculateATS277(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts277 = new AtsListDto();
		AtsGenParamDto calAtsGen277 = new AtsGenParamDto();
		long ats277_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats277ParamData = getGeneralTourismATSParam(atsGeneralId, atsParamId);

			if (ats277ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats277ParamData.get("logic_description");

			List<String> sections = (List<String>) logicDescription.get("sections");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDescription.get("matching_rules");

			boolean caseSensitive = (boolean) matchingRules.get("case_sensitive");

			long minPartial = ((Number) matchingRules.get("minimum_sections_for_partial_score")).longValue();

			long minFull = ((Number) matchingRules.get("minimum_sections_for_full_score")).longValue();

			// 4 Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);

			if (resumeText == null || resumeText.isEmpty()) {
				return null;
			}

			if (!caseSensitive) {
				resumeText = resumeText.toLowerCase();
			}

			// 5 Detect sections
			Set<String> matchedSections = new HashSet<>();

			for (String section : sections) {
				String sectionKey = caseSensitive ? section : section.toLowerCase();

				// Match section as heading or line start
				if (resumeText.contains("\n" + sectionKey) || resumeText.startsWith(sectionKey)
						|| resumeText.contains(sectionKey + "\n") || resumeText.startsWith(" " + sectionKey + " ")
						|| resumeText.startsWith("\n" + sectionKey + "\n")) {
					System.out.println("ATS-277 :: " + sectionKey);
					matchedSections.add(sectionKey);
				}
			}

			long sectionCount = ((Number) matchedSections.size()).longValue();

			// 6 Scoring logic
			if (sectionCount >= minFull) {
				ats277_points = genATSParamData.getMax_points();
			} else if (sectionCount >= minPartial && sectionCount < minFull) {
				ats277_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (sectionCount < minPartial) {
				ats277_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats277_points == max) {
				paramType = "positive";
			} else if (ats277_points == partial) {
				paramType = "partial";
			} else if (ats277_points == 0) {
				paramType = "negative";
			}

			calAtsGen277.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen277.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen277.setCategory(genATSParamData.getCategory());
			calAtsGen277.setDescription(genATSParamData.getDescription());
			calAtsGen277.setMax_points(genATSParamData.getMax_points());
			calAtsGen277.setParameter(genATSParamData.getParameter());
			calAtsGen277.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen277.setTotal_points(genATSParamData.getTotal_points());

			calAts277.setAtsGeneralId(atsGeneralId);
			calAts277.setAtsGeneralParamDto(calAtsGen277);
			calAts277.setAtsParamData(ats277ParamData);
			calAts277.setAtsParamId(atsParamId);
			calAts277.setAtsParamType(paramType);
			calAts277.setAtsScore(ats277_points);

			return calAts277;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts277;
		}
	}

	public AtsListDto calculateATS278(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts278 = new AtsListDto();
		AtsGenParamDto calAtsGen278 = new AtsGenParamDto();
		long ats278_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats278ParamData = getGeneralTourismATSParam(atsGeneralId, atsParamId);

			if (ats278ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats278ParamData.get("logic_description");

			Map<String, List<String>> acceptedFormats = (Map<String, List<String>>) logicDescription
					.get("accepted_formats");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDescription.get("matching_rules");

			boolean caseSensitive = (boolean) matchingRules.get("case_sensitive");

			long maxFull = ((Number) matchingRules.get("max_order_violations_for_full_score")).longValue();

			long maxPartial = ((Number) matchingRules.get("max_order_violations_for_partial_score")).longValue();

			// 4 Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);
			if (resumeText == null || resumeText.isEmpty()) {
				return null;
			}

			if (!caseSensitive) {
				resumeText = resumeText.toLowerCase();
			}

			// 5 Detect section positions (ONE per section group)
			List<Long> detectedSectionPositions = new ArrayList<>();

			for (Map.Entry<String, List<String>> sectionGroup : acceptedFormats.entrySet()) {

				long earliestIndexForGroup = -1;

				for (String heading : sectionGroup.getValue()) {

					String key = caseSensitive ? heading : heading.toLowerCase();
					long index = resumeText.indexOf("\n" + key);

					// Handle heading at very beginning
					if (index == -1 && resumeText.startsWith(key)) {
						System.out.println("ATS-278 :: " + key);
						index = 0;
					}

					if (index != -1) {
						if (earliestIndexForGroup == -1 || index < earliestIndexForGroup) {
							earliestIndexForGroup = index;
						}
					}
				}

				// Add ONLY ONE position per section group
				if (earliestIndexForGroup != -1) {
					detectedSectionPositions.add(earliestIndexForGroup);
				}
			}

			// Less than 2 detected logical sections → ordering meaningless
			if (detectedSectionPositions.size() < 2) {
				return null;
			}

			// 6️ Count ordering violations
			long violations = 0;
			long previousIndex = detectedSectionPositions.get(0);

			for (int i = 1; i < detectedSectionPositions.size(); i++) {
				long currentIndex = detectedSectionPositions.get(i);

				if (currentIndex < previousIndex) {
					violations++;
				}

				previousIndex = currentIndex;
			}

			// 7️ Scoring
			if (violations <= maxFull) {
				ats278_points = genATSParamData.getMax_points();
			} else if (violations <= maxPartial) {
				ats278_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (violations > maxPartial) {
				ats278_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats278_points == max) {
				paramType = "positive";
			} else if (ats278_points == partial) {
				paramType = "partial";
			} else if (ats278_points == 0) {
				paramType = "negative";
			}

			calAtsGen278.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen278.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen278.setCategory(genATSParamData.getCategory());
			calAtsGen278.setDescription(genATSParamData.getDescription());
			calAtsGen278.setMax_points(genATSParamData.getMax_points());
			calAtsGen278.setParameter(genATSParamData.getParameter());
			calAtsGen278.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen278.setTotal_points(genATSParamData.getTotal_points());

			calAts278.setAtsGeneralId(atsGeneralId);
			calAts278.setAtsGeneralParamDto(calAtsGen278);
			calAts278.setAtsParamData(ats278ParamData);
			calAts278.setAtsParamId(atsParamId);
			calAts278.setAtsParamType(paramType);
			calAts278.setAtsScore(ats278_points);

			return calAts278;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts278;
		}
	}

	public AtsListDto calculateATS279(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts279 = new AtsListDto();
		AtsGenParamDto calAtsGen279 = new AtsGenParamDto();
		long ats279_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats279ParamData = getGeneralTourismATSParam(atsGeneralId, atsParamId);

			if (ats279ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats279ParamData.get("logic_description");

			Map<String, List<String>> experienceIndicators = (Map<String, List<String>>) logicDescription
					.get("experience_indicators");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDescription.get("matching_rules");

			boolean caseSensitive = (boolean) matchingRules.get("case_sensitive");
			long minPartial = ((Number) matchingRules.get("minimum_matches_for_partial_score")).longValue();
			long minFull = ((Number) matchingRules.get("minimum_matches_for_full_score")).longValue();

			// 4 Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);

			if (!caseSensitive) {
				resumeText = resumeText.toLowerCase();
			}

			// 5 Count experience matches
			long matchCount = 0;

			for (List<String> keywords : experienceIndicators.values()) {
				for (String keyword : keywords) {
					String key = caseSensitive ? keyword : keyword.toLowerCase();
					if (resumeText.contains(key)) {
						System.out.println("ATS-279 :: " + key);
						matchCount++;
					}
				}
			}

			// 6 Scoring
			if (matchCount >= minFull) {
				ats279_points = genATSParamData.getMax_points();
			} else if (matchCount >= minPartial && matchCount < minFull) {
				ats279_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchCount < minPartial) {
				ats279_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats279_points == max) {
				paramType = "positive";
			} else if (ats279_points == partial) {
				paramType = "partial";
			} else if (ats279_points == 0) {
				paramType = "negative";
			}

			calAtsGen279.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen279.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen279.setCategory(genATSParamData.getCategory());
			calAtsGen279.setDescription(genATSParamData.getDescription());
			calAtsGen279.setMax_points(genATSParamData.getMax_points());
			calAtsGen279.setParameter(genATSParamData.getParameter());
			calAtsGen279.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen279.setTotal_points(genATSParamData.getTotal_points());

			calAts279.setAtsGeneralId(atsGeneralId);
			calAts279.setAtsGeneralParamDto(calAtsGen279);
			calAts279.setAtsParamData(ats279ParamData);
			calAts279.setAtsParamId(atsParamId);
			calAts279.setAtsParamType(paramType);
			calAts279.setAtsScore(ats279_points);

			return calAts279;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts279;
		}
	}

	public AtsListDto calculateATS280(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts280 = new AtsListDto();
		AtsGenParamDto calAtsGen280 = new AtsGenParamDto();
		long ats280_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats280ParamData = getGeneralTourismATSParam(atsGeneralId, atsParamId);

			if (ats280ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats280ParamData.get("logic_description");

			List<String> recognizedTitles = (List<String>) logicDescription.get("recognized_titles");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDescription.get("matching_rules");

			boolean caseSensitive = (boolean) matchingRules.get("case_sensitive");
			long minPartial = ((Number) matchingRules.get("minimum_title_match_for_partial_score")).longValue();
			long minFull = ((Number) matchingRules.get("minimum_title_match_for_full_score")).longValue();

			// 4 Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);

			if (!caseSensitive) {
				resumeText = resumeText.toLowerCase();
			}

			// 5 Count distinct job title matches
			long titleMatchCount = 0;

			for (String title : recognizedTitles) {
				String key = caseSensitive ? title : title.toLowerCase();
				if (resumeText.contains(key)) {
					System.out.println("ATS-280 :: " + key);
					titleMatchCount++;
				}
			}

			// 6 Scoring
			if (titleMatchCount >= minFull) {
				ats280_points = genATSParamData.getMax_points();
			} else if (titleMatchCount >= minPartial && titleMatchCount < minFull) {
				ats280_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (titleMatchCount < minPartial) {
				ats280_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats280_points == max) {
				paramType = "positive";
			} else if (ats280_points == partial) {
				paramType = "partial";
			} else if (ats280_points == 0) {
				paramType = "negative";
			}

			calAtsGen280.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen280.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen280.setCategory(genATSParamData.getCategory());
			calAtsGen280.setDescription(genATSParamData.getDescription());
			calAtsGen280.setMax_points(genATSParamData.getMax_points());
			calAtsGen280.setParameter(genATSParamData.getParameter());
			calAtsGen280.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen280.setTotal_points(genATSParamData.getTotal_points());

			calAts280.setAtsGeneralId(atsGeneralId);
			calAts280.setAtsGeneralParamDto(calAtsGen280);
			calAts280.setAtsParamData(ats280ParamData);
			calAts280.setAtsParamId(atsParamId);
			calAts280.setAtsParamType(paramType);
			calAts280.setAtsScore(ats280_points);

			return calAts280;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts280;
		}
	}

	public AtsListDto calculateATS281(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts281 = new AtsListDto();
		AtsGenParamDto calAtsGen281 = new AtsGenParamDto();
		long ats281_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats281ParamData = getGeneralTourismATSParam(atsGeneralId, atsParamId);

			if (ats281ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats281ParamData.get("logic_description");

			List<String> datePatterns = (List<String>) logicDescription.get("date_patterns");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDescription.get("matching_rules");

			boolean caseSensitive = (boolean) matchingRules.get("case_sensitive");
			long minMatches = ((Number) matchingRules.get("minimum_date_matches_for_score")).longValue();

			// 4 Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);
			if (!caseSensitive) {
				resumeText = resumeText.toLowerCase();
			}

			// 5 Regex patterns for common date formats
			List<String> regexPatterns = Arrays.asList(
					"(jan|feb|mar|apr|may|jun|jul|aug|sep|sept|oct|nov|dec)\\s+\\d{4}", "(\\d{2}/\\d{4})",
					"(\\d{4}-\\d{2})", "\\b\\d{4}\\b", "(\\d{4}\\s*-\\s*\\d{4})", "(\\d{4}\\s*-\\s*present)",
					"((jan|feb|mar|apr|may|jun|jul|aug|sep|sept|oct|nov|dec)\\s+\\d{4}\\s*-\\s*(present|(jan|feb|mar|apr|may|jun|jul|aug|sep|sept|oct|nov|dec)\\s+\\d{4}))");

			long dateMatchCount = 0;

			for (String regex : regexPatterns) {
				if (resumeText.matches("(?s).*" + regex + ".*")) {
					System.out.println("ATS-281 :: " + regex);
					dateMatchCount++;
				}
			}

			// 6 Scoring (binary)
			if (dateMatchCount >= minMatches) {
				ats281_points = genATSParamData.getMax_points();
			} else if (dateMatchCount < minMatches) {
				ats281_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats281_points == max) {
				paramType = "positive";
			} else if (ats281_points == partial) {
				paramType = "partial";
			} else if (ats281_points == 0) {
				paramType = "negative";
			}

			calAtsGen281.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen281.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen281.setCategory(genATSParamData.getCategory());
			calAtsGen281.setDescription(genATSParamData.getDescription());
			calAtsGen281.setMax_points(genATSParamData.getMax_points());
			calAtsGen281.setParameter(genATSParamData.getParameter());
			calAtsGen281.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen281.setTotal_points(genATSParamData.getTotal_points());

			calAts281.setAtsGeneralId(atsGeneralId);
			calAts281.setAtsGeneralParamDto(calAtsGen281);
			calAts281.setAtsParamData(ats281ParamData);
			calAts281.setAtsParamId(atsParamId);
			calAts281.setAtsParamType(paramType);
			calAts281.setAtsScore(ats281_points);

			return calAts281;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts281;
		}
	}

	public AtsListDto calculateATS282(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts282 = new AtsListDto();
		AtsGenParamDto calAtsGen282 = new AtsGenParamDto();
		long ats282_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats282ParamData = getGeneralTourismATSParam(atsGeneralId, atsParamId);

			if (ats282ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats282ParamData.get("logic_description");

			Map<String, List<String>> impactIndicators = (Map<String, List<String>>) logicDescription
					.get("impact_indicators");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDescription.get("matching_rules");

			boolean caseSensitive = (boolean) matchingRules.get("case_sensitive");
			long minPartial = ((Number) matchingRules.get("minimum_matches_for_partial_score")).longValue();
			long minFull = ((Number) matchingRules.get("minimum_matches_for_full_score")).longValue();

			// 4 Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);

			if (resumeText == null || resumeText.isEmpty()) {
				return null;
			}

			if (!caseSensitive) {
				resumeText = resumeText.toLowerCase();
			}

			// 5 Count quantified impact matches
			long impactMatchCount = 0;

			for (List<String> indicators : impactIndicators.values()) {
				for (String indicator : indicators) {
					String key = caseSensitive ? indicator : indicator.toLowerCase();

					if (resumeText.contains(key)) {
						System.out.println("ATS-282 :: " + key);
						impactMatchCount++;
					}
				}
			}

			// 6 Additional numeric signal (very important)

			// Detect numbers like: 30%, 10k, 1M, 527ms, etc.
			if (resumeText.matches("(?s).*\\b\\d+(%|k|m|ms|s|x)?\\b.*")) {
				impactMatchCount++;
			}

			// 7 Scoring
			if (impactMatchCount >= minFull) {
				ats282_points = genATSParamData.getMax_points();
			} else if (impactMatchCount >= minPartial && impactMatchCount < minFull) {
				ats282_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (impactMatchCount < minPartial) {
				ats282_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats282_points == max) {
				paramType = "positive";
			} else if (ats282_points == partial) {
				paramType = "partial";
			} else if (ats282_points == 0) {
				paramType = "negative";
			}

			calAtsGen282.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen282.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen282.setCategory(genATSParamData.getCategory());
			calAtsGen282.setDescription(genATSParamData.getDescription());
			calAtsGen282.setMax_points(genATSParamData.getMax_points());
			calAtsGen282.setParameter(genATSParamData.getParameter());
			calAtsGen282.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen282.setTotal_points(genATSParamData.getTotal_points());

			calAts282.setAtsGeneralId(atsGeneralId);
			calAts282.setAtsGeneralParamDto(calAtsGen282);
			calAts282.setAtsParamData(ats282ParamData);
			calAts282.setAtsParamId(atsParamId);
			calAts282.setAtsParamType(paramType);
			calAts282.setAtsScore(ats282_points);

			return calAts282;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts282;
		}
	}

	public AtsListDto calculateATS283(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts283 = new AtsListDto();
		AtsGenParamDto calAtsGen283 = new AtsGenParamDto();
		long ats283_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats283ParamData = getGeneralTourismATSParam(atsGeneralId, atsParamId);

			if (ats283ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats283ParamData.get("logic_description");

			Map<String, List<String>> skillGroups = (Map<String, List<String>>) logicDescription.get("skill_groups");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDescription.get("matching_rules");

			boolean caseSensitive = (boolean) matchingRules.get("case_sensitive");
			long minPartial = ((Number) matchingRules.get("minimum_group_match_for_partial_score")).longValue();
			long minFull = ((Number) matchingRules.get("minimum_group_match_for_full_score")).longValue();

			// 4 Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);

			if (resumeText == null || resumeText.isEmpty()) {
				return null;
			}

			if (!caseSensitive) {
				resumeText = resumeText.toLowerCase();
			}

			// 5 Count matched skill groups (NOT individual keywords)
			long matchedGroups = 0;

			for (List<String> skills : skillGroups.values()) {
				boolean groupMatched = false;

				for (String skill : skills) {
					String key = caseSensitive ? skill : skill.toLowerCase();
					if (resumeText.contains(key)) {
						System.out.println("ATS-283 :: " + key);
						groupMatched = true;
						break; // only count this group once
					}
				}

				if (groupMatched) {
					matchedGroups++;
				}
			}

			// 6 Scoring
			if (matchedGroups >= minFull) {
				ats283_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartial && matchedGroups < minFull) {
				ats283_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartial) {
				ats283_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats283_points == max) {
				paramType = "positive";
			} else if (ats283_points == partial) {
				paramType = "partial";
			} else if (ats283_points == 0) {
				paramType = "negative";
			}

			calAtsGen283.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen283.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen283.setCategory(genATSParamData.getCategory());
			calAtsGen283.setDescription(genATSParamData.getDescription());
			calAtsGen283.setMax_points(genATSParamData.getMax_points());
			calAtsGen283.setParameter(genATSParamData.getParameter());
			calAtsGen283.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen283.setTotal_points(genATSParamData.getTotal_points());

			calAts283.setAtsGeneralId(atsGeneralId);
			calAts283.setAtsGeneralParamDto(calAtsGen283);
			calAts283.setAtsParamData(ats283ParamData);
			calAts283.setAtsParamId(atsParamId);
			calAts283.setAtsParamType(paramType);
			calAts283.setAtsScore(ats283_points);

			return calAts283;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts283;
		}
	}

	public AtsListDto calculateATS284(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts284 = new AtsListDto();
		AtsGenParamDto calAtsGen284 = new AtsGenParamDto();
		long ats284_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats284ParamData = getGeneralTourismATSParam(atsGeneralId, atsParamId);

			if (ats284ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats284ParamData.get("logic_description");

			Map<String, List<String>> practiceGroups = (Map<String, List<String>>) logicDescription
					.get("practice_groups");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDescription.get("matching_rules");

			boolean caseSensitive = (Boolean) matchingRules.getOrDefault("case_sensitive", false);

			long minPartial = ((Number) matchingRules.get("minimum_practice_group_for_partial_score")).longValue();

			long minFull = ((Number) matchingRules.get("minimum_practice_group_for_full_score")).longValue();

			// 4 Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);

			if (resumeText == null || resumeText.isEmpty()) {
				return null;
			}

			if (!caseSensitive) {
				resumeText = resumeText.toLowerCase();
			}

			// 5 Detect matched practice groups (each group counted once)
			long matchedGroups = 0;

			for (Map.Entry<String, List<String>> group : practiceGroups.entrySet()) {
				for (String keyword : group.getValue()) {
					String key = caseSensitive ? keyword : keyword.toLowerCase();
					if (resumeText.contains(key)) {
						System.out.println("ATS-284 :: " + key);
						matchedGroups++;
						break; // count group only once
					}
				}
			}

			// 6 Scoring
			if (matchedGroups >= minFull) {
				ats284_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartial && matchedGroups < minFull) {
				ats284_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartial) {
				ats284_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats284_points == max) {
				paramType = "positive";
			} else if (ats284_points == partial) {
				paramType = "partial";
			} else if (ats284_points == 0) {
				paramType = "negative";
			}

			calAtsGen284.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen284.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen284.setCategory(genATSParamData.getCategory());
			calAtsGen284.setDescription(genATSParamData.getDescription());
			calAtsGen284.setMax_points(genATSParamData.getMax_points());
			calAtsGen284.setParameter(genATSParamData.getParameter());
			calAtsGen284.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen284.setTotal_points(genATSParamData.getTotal_points());

			calAts284.setAtsGeneralId(atsGeneralId);
			calAts284.setAtsGeneralParamDto(calAtsGen284);
			calAts284.setAtsParamData(ats284ParamData);
			calAts284.setAtsParamId(atsParamId);
			calAts284.setAtsParamType(paramType);
			calAts284.setAtsScore(ats284_points);

			return calAts284;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts284;
		}
	}

	public AtsListDto calculateATS285(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts285 = new AtsListDto();
		AtsGenParamDto calAtsGen285 = new AtsGenParamDto();
		long ats285_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats285ParamData = getGeneralTourismATSParam(atsGeneralId, atsParamId);

			if (ats285ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic description
			Map<String, Object> logicDescription = (Map<String, Object>) ats285ParamData.get("logic_description");

			Map<String, List<String>> educationGroups = (Map<String, List<String>>) logicDescription
					.get("education_groups");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDescription.get("matching_rules");

			boolean caseSensitive = (boolean) matchingRules.get("case_sensitive");

			long minPartialGroups = ((Number) matchingRules.get("minimum_group_match_for_partial_score")).longValue();

			long minFullGroups = ((Number) matchingRules.get("minimum_group_match_for_full_score")).longValue();

			// 4 Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);

			if (!caseSensitive) {
				resumeText = resumeText.toLowerCase();
			}

			// 5 Count matched education groups
			long matchedGroups = 0;

			for (Map.Entry<String, List<String>> groupEntry : educationGroups.entrySet()) {
				boolean groupMatched = false;

				for (String keyword : groupEntry.getValue()) {
					String key = caseSensitive ? keyword : keyword.toLowerCase();

					if (resumeText.contains(key)) {
						System.out.println("ATS-285 :: " + key);
						groupMatched = true;
						break;
					}
				}

				if (groupMatched) {
					matchedGroups++;
				}
			}

			// 6 Scoring
			if (matchedGroups >= minFullGroups) {
				ats285_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartialGroups && matchedGroups < minFullGroups) {
				ats285_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartialGroups) {
				ats285_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats285_points == max) {
				paramType = "positive";
			} else if (ats285_points == partial) {
				paramType = "partial";
			} else if (ats285_points == 0) {
				paramType = "negative";
			}

			calAtsGen285.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen285.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen285.setCategory(genATSParamData.getCategory());
			calAtsGen285.setDescription(genATSParamData.getDescription());
			calAtsGen285.setMax_points(genATSParamData.getMax_points());
			calAtsGen285.setParameter(genATSParamData.getParameter());
			calAtsGen285.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen285.setTotal_points(genATSParamData.getTotal_points());

			calAts285.setAtsGeneralId(atsGeneralId);
			calAts285.setAtsGeneralParamDto(calAtsGen285);
			calAts285.setAtsParamData(ats285ParamData);
			calAts285.setAtsParamId(atsParamId);
			calAts285.setAtsParamType(paramType);
			calAts285.setAtsScore(ats285_points);

			return calAts285;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts285;
		}
	}

	public AtsListDto calculateATS286(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts286 = new AtsListDto();
		AtsGenParamDto calAtsGen286 = new AtsGenParamDto();
		long ats286_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats286ParamData = getGeneralTourismATSParam(atsGeneralId, atsParamId);

			if (ats286ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic description
			Map<String, Object> logicDescription = (Map<String, Object>) ats286ParamData.get("logic_description");

			Map<String, List<String>> clarityGroups = (Map<String, List<String>>) logicDescription
					.get("clarity_groups");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDescription.get("matching_rules");

			boolean caseSensitive = (boolean) matchingRules.get("case_sensitive");

			long minPartialGroups = ((Number) matchingRules.get("minimum_group_match_for_partial_score")).longValue();

			long minFullGroups = ((Number) matchingRules.get("minimum_group_match_for_full_score")).longValue();

			// 4 Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);

			if (!caseSensitive) {
				resumeText = resumeText.toLowerCase();
			}

			// 5 Count matched clarity groups
			long matchedGroups = 0;

			for (Map.Entry<String, List<String>> groupEntry : clarityGroups.entrySet()) {
				boolean groupMatched = false;

				for (String keyword : groupEntry.getValue()) {
					String key = caseSensitive ? keyword : keyword.toLowerCase();

					if (resumeText.contains(key)) {
						System.out.println("ATS-286 :: " + key);
						groupMatched = true;
						break;
					}
				}

				if (groupMatched) {
					matchedGroups++;
				}
			}

			// 6 Scoring
			if (matchedGroups >= minFullGroups) {
				ats286_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartialGroups && matchedGroups < minFullGroups) {
				ats286_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartialGroups) {
				ats286_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats286_points == max) {
				paramType = "positive";
			} else if (ats286_points == partial) {
				paramType = "partial";
			} else if (ats286_points == 0) {
				paramType = "negative";
			}

			calAtsGen286.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen286.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen286.setCategory(genATSParamData.getCategory());
			calAtsGen286.setDescription(genATSParamData.getDescription());
			calAtsGen286.setMax_points(genATSParamData.getMax_points());
			calAtsGen286.setParameter(genATSParamData.getParameter());
			calAtsGen286.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen286.setTotal_points(genATSParamData.getTotal_points());

			calAts286.setAtsGeneralId(atsGeneralId);
			calAts286.setAtsGeneralParamDto(calAtsGen286);
			calAts286.setAtsParamData(ats286ParamData);
			calAts286.setAtsParamId(atsParamId);
			calAts286.setAtsParamType(paramType);
			calAts286.setAtsScore(ats286_points);

			return calAts286;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts286;
		}
	}

	public AtsListDto calculateATS287(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts287 = new AtsListDto();
		AtsGenParamDto calAtsGen287 = new AtsGenParamDto();
		long ats287_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats287ParamData = getGeneralTourismATSParam(atsGeneralId, atsParamId);

			if (ats287ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats287ParamData.get("logic_description");

			Map<String, List<String>> actionVerbGroups = (Map<String, List<String>>) logicDescription
					.get("action_verb_groups");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDescription.get("matching_rules");

			boolean caseSensitive = Boolean.TRUE.equals(matchingRules.get("case_sensitive"));

			long minPartialGroups = ((Number) matchingRules.get("minimum_group_match_for_partial_score")).longValue();

			long minFullGroups = ((Number) matchingRules.get("minimum_group_match_for_full_score")).longValue();

			// 4 Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);

			if (resumeText == null || resumeText.trim().isEmpty()) {
				return null;
			}

			if (!caseSensitive) {
				resumeText = resumeText.toLowerCase();
			}

			// 5 Count matched action-verb groups (max 1 per group)
			long matchedGroups = 0;

			for (Map.Entry<String, List<String>> groupEntry : actionVerbGroups.entrySet()) {

				boolean groupMatched = false;

				for (String verb : groupEntry.getValue()) {
					String keyword = caseSensitive ? verb : verb.toLowerCase();

					// word-boundary safe match
					if (resumeText.matches("(?s).*\\b" + Pattern.quote(keyword) + "\\b.*")) {
						groupMatched = true;
						System.out.println("ATS-287 :: " + keyword);
						break;
					}
				}

				if (groupMatched) {
					matchedGroups++;
				}
			}

			// 6 Scoring
			if (matchedGroups >= minFullGroups) {
				ats287_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartialGroups && matchedGroups < minFullGroups) {
				ats287_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartialGroups) {
				ats287_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats287_points == max) {
				paramType = "positive";
			} else if (ats287_points == partial) {
				paramType = "partial";
			} else if (ats287_points == 0) {
				paramType = "negative";
			}

			calAtsGen287.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen287.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen287.setCategory(genATSParamData.getCategory());
			calAtsGen287.setDescription(genATSParamData.getDescription());
			calAtsGen287.setMax_points(genATSParamData.getMax_points());
			calAtsGen287.setParameter(genATSParamData.getParameter());
			calAtsGen287.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen287.setTotal_points(genATSParamData.getTotal_points());

			calAts287.setAtsGeneralId(atsGeneralId);
			calAts287.setAtsGeneralParamDto(calAtsGen287);
			calAts287.setAtsParamData(ats287ParamData);
			calAts287.setAtsParamId(atsParamId);
			calAts287.setAtsParamType(paramType);
			calAts287.setAtsScore(ats287_points);

			return calAts287;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts287;
		}
	}

	public AtsListDto calculateATS288(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts288 = new AtsListDto();
		AtsGenParamDto calAtsGen288 = new AtsGenParamDto();
		long ats288_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats288ParamData = getGeneralTourismATSParam(atsGeneralId, atsParamId);

			if (ats288ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Fetch JSON configuration
			Map<String, Object> logicDesc = (Map<String, Object>) ats288ParamData.get("logic_description");
			Map<String, Object> errorRules = (Map<String, Object>) logicDesc.get("error_detection_rules");
			Map<String, Object> scoringRules = (Map<String, Object>) logicDesc.get("scoring_rules");

			// 4 Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);
			if (resumeText == null || resumeText.isEmpty()) {
				return null;
			}

			resumeText = resumeText.toLowerCase();

			// 5 Count all error patterns
			long totalErrors = 0;

			for (Object ruleListObj : errorRules.values()) {
				List<String> ruleList = (List<String>) ruleListObj;
				for (String errorPattern : ruleList) {
					if (resumeText.contains(" " + errorPattern.toLowerCase() + " ")) {
						System.out.println("ATS-288 :: " + errorPattern.toLowerCase());
						totalErrors++;
					}
				}
			}

			// 6 Read scoring thresholds
			long fullAllowed = ((Number) scoringRules.get("max_allowed_errors_for_full_score")).longValue();
			long partialAllowed = ((Number) scoringRules.get("max_allowed_errors_for_partial_score")).longValue();
			long zeroAbove = ((Number) scoringRules.get("zero_score_if_errors_above")).longValue();

			// 7 Final scoring decision
			if (totalErrors <= fullAllowed) {
				ats288_points = genATSParamData.getMax_points();
			} else if (totalErrors <= partialAllowed) {
				ats288_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (totalErrors >= zeroAbove) {
				ats288_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats288_points == max) {
				paramType = "positive";
			} else if (ats288_points == partial) {
				paramType = "partial";
			} else if (ats288_points == 0) {
				paramType = "negative";
			}

			calAtsGen288.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen288.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen288.setCategory(genATSParamData.getCategory());
			calAtsGen288.setDescription(genATSParamData.getDescription());
			calAtsGen288.setMax_points(genATSParamData.getMax_points());
			calAtsGen288.setParameter(genATSParamData.getParameter());
			calAtsGen288.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen288.setTotal_points(genATSParamData.getTotal_points());

			calAts288.setAtsGeneralId(atsGeneralId);
			calAts288.setAtsGeneralParamDto(calAtsGen288);
			calAts288.setAtsParamData(ats288ParamData);
			calAts288.setAtsParamId(atsParamId);
			calAts288.setAtsParamType(paramType);
			calAts288.setAtsScore(ats288_points);

			return calAts288;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts288;
		}
	}

	public AtsListDto calculateATS289(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts289 = new AtsListDto();
		AtsGenParamDto calAtsGen289 = new AtsGenParamDto();
		long ats289_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats289ParamData = getGeneralTourismATSParam(atsGeneralId, atsParamId);

			if (ats289ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);

			if (resumeText == null || resumeText.isEmpty()) {
				return null;
			}

			resumeText = resumeText.toLowerCase();

			// 4 Read indicator groups
			Map<String, Object> logicDesc = (Map<String, Object>) ats289ParamData.get("logic_description");

			Map<String, Object> nonTextIndicators = (Map<String, Object>) logicDesc.get("non_text_indicators");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDesc.get("matching_rules");

			long fullScoreLimit = ((Number) matchingRules.get("max_allowed_non_text_elements_for_full_score"))
					.longValue();

			long partialScoreLimit = ((Number) matchingRules.get("max_allowed_non_text_elements_for_partial_score"))
					.longValue();

			long zeroScoreLimit = ((Number) matchingRules.get("zero_score_if_non_text_elements_above")).longValue();

			// 5 Count non-text violations

			long totalViolations = 0;

			for (Object indicatorListObj : nonTextIndicators.values()) {
				List<String> indicators = (List<String>) indicatorListObj;

				for (String indicator : indicators) {
					if (resumeText.contains(indicator.toLowerCase())) {
						System.out.println("ATS-289 :: " + indicator.toLowerCase());
						totalViolations++;
					}
				}
			}

			// 6 Scoring logic
			if (totalViolations <= fullScoreLimit) {
				ats289_points = genATSParamData.getMax_points();
			} else if (totalViolations <= partialScoreLimit) {
				ats289_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (totalViolations >= zeroScoreLimit) {
				ats289_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats289_points == max) {
				paramType = "positive";
			} else if (ats289_points == partial) {
				paramType = "partial";
			} else if (ats289_points == 0) {
				paramType = "negative";
			}

			calAtsGen289.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen289.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen289.setCategory(genATSParamData.getCategory());
			calAtsGen289.setDescription(genATSParamData.getDescription());
			calAtsGen289.setMax_points(genATSParamData.getMax_points());
			calAtsGen289.setParameter(genATSParamData.getParameter());
			calAtsGen289.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen289.setTotal_points(genATSParamData.getTotal_points());

			calAts289.setAtsGeneralId(atsGeneralId);
			calAts289.setAtsGeneralParamDto(calAtsGen289);
			calAts289.setAtsParamData(ats289ParamData);
			calAts289.setAtsParamId(atsParamId);
			calAts289.setAtsParamType(paramType);
			calAts289.setAtsScore(ats289_points);

			return calAts289;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts289;
		}
	}

	public AtsListDto calculateATS290(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts290 = new AtsListDto();
		AtsGenParamDto calAtsGen290 = new AtsGenParamDto();

		long ats290_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats290ParamData = getGeneralTourismATSParam(atsGeneralId, atsParamId);

			if (ats290ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract resume text
			String resumeText = fileConUtil.extractResumeText(file);

			if (resumeText == null || resumeText.isEmpty()) {
				return null;
			}

			resumeText = resumeText.toLowerCase();

			// 4 Read JSON logic
			Map<String, Object> logicDesc = (Map<String, Object>) ats290ParamData.get("logic_description");

			Map<String, Object> criticalInfoIndicators = (Map<String, Object>) logicDesc
					.get("critical_information_indicators");

			List<String> headerFooterIndicators = (List<String>) logicDesc.get("header_footer_indicators");

			Map<String, Object> matchingRules = (Map<String, Object>) logicDesc.get("matching_rules");

			long fullScoreLimit = ((Number) matchingRules.get("max_allowed_header_footer_violations_for_full_score"))
					.longValue();

			long partialScoreLimit = ((Number) matchingRules
					.get("max_allowed_header_footer_violations_for_partial_score")).longValue();

			long zeroScoreLimit = ((Number) matchingRules.get("zero_score_if_violations_above")).longValue();

			// 5 Detect header/footer related critical info
			long totalViolations = 0;

			for (String headerFooterKeyword : headerFooterIndicators) {
				if (resumeText.contains(headerFooterKeyword.toLowerCase())) {

					// If header/footer exists, check for critical info presence
					for (Object indicatorGroupObj : criticalInfoIndicators.values()) {
						List<String> indicators = (List<String>) indicatorGroupObj;

						for (String indicator : indicators) {
							if (resumeText.contains(indicator.toLowerCase())) {
								System.out.println("ATS-290 :: " + indicator.toLowerCase());
								totalViolations++;
								break;
							}
						}
					}
				}
			}

			// 6 Scoring
			if (totalViolations <= fullScoreLimit) {
				ats290_points = genATSParamData.getMax_points();
			} else if (totalViolations <= partialScoreLimit) {
				ats290_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (totalViolations >= zeroScoreLimit
					|| (totalViolations > partialScoreLimit && totalViolations < zeroScoreLimit)) {
				ats290_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats290_points == max) {
				paramType = "positive";
			} else if (ats290_points == partial) {
				paramType = "partial";
			} else if (ats290_points == 0) {
				paramType = "negative";
			}

			calAtsGen290.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen290.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen290.setCategory(genATSParamData.getCategory());
			calAtsGen290.setDescription(genATSParamData.getDescription());
			calAtsGen290.setMax_points(genATSParamData.getMax_points());
			calAtsGen290.setParameter(genATSParamData.getParameter());
			calAtsGen290.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen290.setTotal_points(genATSParamData.getTotal_points());

			calAts290.setAtsGeneralId(atsGeneralId);
			calAts290.setAtsGeneralParamDto(calAtsGen290);
			calAts290.setAtsParamData(ats290ParamData);
			calAts290.setAtsParamId(atsParamId);
			calAts290.setAtsParamType(paramType);
			calAts290.setAtsScore(ats290_points);

			return calAts290;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts290;
		}
	}

}

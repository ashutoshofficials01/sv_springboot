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
public class FinanceAtsUtil {

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

	private List<Map<String, Object>> generalFinanceATSParameterList;

	private Map<String, Map<String, Object>> generalFinanceATSParameterMap;

//	@PostConstruct
//	ITATSUtil(AtsGenParamDto atsGenParamDto) {
//		this.atsGenParamDto = atsGenParamDto;
//	}

	@PostConstruct
	public void loadFinance_ATSParameter() {
		try {

			Resource resource = resourceLoader.getResource("classpath:Finance_ATSParameter.json");

			// Read JSON as Map
			Map<String, Object> jsonData = objectMapper.readValue(resource.getInputStream(), Map.class);

			// Extract array
			generalFinanceATSParameterList = (List<Map<String, Object>>) jsonData.get("Finance_ATSParameter");

			// Create fast-lookup map
			generalFinanceATSParameterMap = new HashMap<>();

			for (Map<String, Object> param : generalFinanceATSParameterList) {
				Number atsGeneralIdNum = (Number) param.get("atsGeneralId");
				long atsGeneralId = atsGeneralIdNum.longValue();
				String atsParamId = (String) param.get("atsParamId");

				String key = atsGeneralId + "_" + atsParamId;
				generalFinanceATSParameterMap.put(key, param);
			}

			System.out.println("Loaded ATS Params Count: " + generalFinanceATSParameterMap.size());

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load Finance_ATSParameter.json", e);
		}
	}

	public List<Map<String, Object>> getAllGeneralATSParams() {
//		System.out.println("getAllGeneralATSParams() --> generalFinanceATSParameterList :: " + generalFinanceATSParameterList);
		return generalFinanceATSParameterList;
	}

	public Map<String, Object> getGeneralFinanceATSParam(long atsGeneralId, String atsParamId) {
//		System.out.println("getGeneralFinanceATSParam(long " + atsGeneralId + ", String " + atsParamId
//				+ ") --> generalFinanceATSParameterMap :: " + generalFinanceATSParameterMap.get(atsGeneralId + "_" + atsParamId));
		return generalFinanceATSParameterMap.get(atsGeneralId + "_" + atsParamId);
	}
	
	public AtsListDto calculateATS151(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts151 = new AtsListDto();
		AtsGenParamDto calAtsGen151 = new AtsGenParamDto();
		long ats151_points = 0;
		try {

			// 1️ Fetch JSON parameter config
			Map<String, Object> ats151ParamData = getGeneralFinanceATSParam(atsGeneralId, atsParamId);
			if (ats151ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config (points / penalty)
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);
			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats151ParamData.get("logic_description");

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
					System.out.println("ATS-151 :: " + key);
					matchCount++;
				}
			}

//	     6️ Scoring logic
			if (matchCount >= minFull) {
				ats151_points = genATSParamData.getMax_points();
			} else if ((matchCount >= minPartial) && (matchCount < minFull)) {
				ats151_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if ((matchCount <= 0) && (matchCount < minPartial)) {
				ats151_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats151_points == max) {
				paramType = "positive";
			} else if (ats151_points == partial) {
				paramType = "partial";
			} else if (ats151_points == 0) {
				paramType = "negative";
			}

			calAtsGen151.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen151.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen151.setCategory(genATSParamData.getCategory());
			calAtsGen151.setDescription(genATSParamData.getDescription());
			calAtsGen151.setMax_points(genATSParamData.getMax_points());
			calAtsGen151.setParameter(genATSParamData.getParameter());
			calAtsGen151.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen151.setTotal_points(genATSParamData.getTotal_points());

			calAts151.setAtsGeneralId(atsGeneralId);
			calAts151.setAtsGeneralParamDto(calAtsGen151);
			calAts151.setAtsParamData(ats151ParamData);
			calAts151.setAtsParamId(atsParamId);
			calAts151.setAtsParamType(paramType);
			calAts151.setAtsScore(ats151_points);

			return calAts151;

		} catch (Exception e) {
			e.printStackTrace();
			return calAts151;
		}
	}

	public AtsListDto calculateATS152(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts152 = new AtsListDto();
		AtsGenParamDto calAtsGen152 = new AtsGenParamDto();
		long ats152_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats152ParamData = getGeneralFinanceATSParam(atsGeneralId, atsParamId);

			if (ats152ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats152ParamData.get("logic_description");

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
						System.out.println("ATS-152 :: " + key);
						matchedCategories++;
					}
				}
			}

			// 6️ Scoring logic
			if (matchedCategories >= minFull) {
				ats152_points = genATSParamData.getMax_points();
			} else if (matchedCategories >= minPartial && matchedCategories < minFull) {
				ats152_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else {
				ats152_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats152_points == max) {
				paramType = "positive";
			} else if (ats152_points == partial) {
				paramType = "partial";
			} else if (ats152_points == 0) {
				paramType = "negative";
			}

			calAtsGen152.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen152.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen152.setCategory(genATSParamData.getCategory());
			calAtsGen152.setDescription(genATSParamData.getDescription());
			calAtsGen152.setMax_points(genATSParamData.getMax_points());
			calAtsGen152.setParameter(genATSParamData.getParameter());
			calAtsGen152.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen152.setTotal_points(genATSParamData.getTotal_points());

			calAts152.setAtsGeneralId(atsGeneralId);
			calAts152.setAtsGeneralParamDto(calAtsGen152);
			calAts152.setAtsParamData(ats152ParamData);
			calAts152.setAtsParamId(atsParamId);
			calAts152.setAtsParamType(paramType);
			calAts152.setAtsScore(ats152_points);

			return calAts152;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts152;
		}
	}

	public AtsListDto calculateATS153(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts153 = new AtsListDto();
		AtsGenParamDto calAtsGen153 = new AtsGenParamDto();
		long ats153_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats153ParamData = getGeneralFinanceATSParam(atsGeneralId, atsParamId);

			if (ats153ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats153ParamData.get("logic_description");

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
						System.out.println("ATS-153 :: " + alternate);
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
				ats153_points = 0;
			} else if (matchedVariationGroups <= minFull && matchedVariationGroups > minPartial) {
				ats153_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedVariationGroups <= minPartial) {
				ats153_points = genATSParamData.getMax_points();
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats153_points == max) {
				paramType = "positive";
			} else if (ats153_points == partial) {
				paramType = "partial";
			} else if (ats153_points == 0) {
				paramType = "negative";
			}

			calAtsGen153.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen153.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen153.setCategory(genATSParamData.getCategory());
			calAtsGen153.setDescription(genATSParamData.getDescription());
			calAtsGen153.setMax_points(genATSParamData.getMax_points());
			calAtsGen153.setParameter(genATSParamData.getParameter());
			calAtsGen153.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen153.setTotal_points(genATSParamData.getTotal_points());

			calAts153.setAtsGeneralId(atsGeneralId);
			calAts153.setAtsGeneralParamDto(calAtsGen153);
			calAts153.setAtsParamData(ats153ParamData);
			calAts153.setAtsParamId(atsParamId);
			calAts153.setAtsParamType(paramType);
			calAts153.setAtsScore(ats153_points);

			return calAts153;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts153;
		}
	}

	public AtsListDto calculateATS154(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts154 = new AtsListDto();
		AtsGenParamDto calAtsGen154 = new AtsGenParamDto();
		long ats154_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats154ParamData = getGeneralFinanceATSParam(atsGeneralId, atsParamId);

			if (ats154ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats154ParamData.get("logic_description");

			Map<String, List<String>> acceptedFormats = (Map<String, List<String>>) logicDescription
					.get("accepted_formats");

			List<String> fullScoreFormats = acceptedFormats.get("full_score");
			List<String> partialScoreFormats = acceptedFormats.get("partial_score");

			// 4️ Extract file extension
			if (fileName == null || !fileName.contains(".")) {
				return null;
			}

			String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

			System.out.println("ATS-154 :: " + extension);

			// 5️ Scoring logic
			if (fullScoreFormats.contains(extension)) {
				ats154_points = genATSParamData.getMax_points();
			} else if (partialScoreFormats.contains(extension)) {
				ats154_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else {
				ats154_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats154_points == max) {
				paramType = "positive";
			} else if (ats154_points == partial) {
				paramType = "partial";
			} else if (ats154_points == 0) {
				paramType = "negative";
			}

			calAtsGen154.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen154.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen154.setCategory(genATSParamData.getCategory());
			calAtsGen154.setDescription(genATSParamData.getDescription());
			calAtsGen154.setMax_points(genATSParamData.getMax_points());
			calAtsGen154.setParameter(genATSParamData.getParameter());
			calAtsGen154.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen154.setTotal_points(genATSParamData.getTotal_points());

			calAts154.setAtsGeneralId(atsGeneralId);
			calAts154.setAtsGeneralParamDto(calAtsGen154);
			calAts154.setAtsParamData(ats154ParamData);
			calAts154.setAtsParamId(atsParamId);
			calAts154.setAtsParamType(paramType);
			calAts154.setAtsScore(ats154_points);

			return calAts154;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts154;
		}
	}

	public AtsListDto calculateATS155(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts155 = new AtsListDto();
		AtsGenParamDto calAtsGen155 = new AtsGenParamDto();
		long ats155_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats155ParamData = getGeneralFinanceATSParam(atsGeneralId, atsParamId);

			if (ats155ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats155ParamData.get("logic_description");

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
						System.out.println("ATS-155 :: " + indicator);
						layoutViolations++;
					}
				}
			}

			// 6 Scoring Logic
			if (layoutViolations <= maxFull) {
				ats155_points = genATSParamData.getMax_points();
			} else if (layoutViolations <= maxPartial) {
				ats155_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else {
				ats155_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats155_points == max) {
				paramType = "positive";
			} else if (ats155_points == partial) {
				paramType = "partial";
			} else if (ats155_points == 0) {
				paramType = "negative";
			}

			calAtsGen155.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen155.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen155.setCategory(genATSParamData.getCategory());
			calAtsGen155.setDescription(genATSParamData.getDescription());
			calAtsGen155.setMax_points(genATSParamData.getMax_points());
			calAtsGen155.setParameter(genATSParamData.getParameter());
			calAtsGen155.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen155.setTotal_points(genATSParamData.getTotal_points());

			calAts155.setAtsGeneralId(atsGeneralId);
			calAts155.setAtsGeneralParamDto(calAtsGen155);
			calAts155.setAtsParamData(ats155ParamData);
			calAts155.setAtsParamId(atsParamId);
			calAts155.setAtsParamType(paramType);
			calAts155.setAtsScore(ats155_points);

			return calAts155;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts155;
		}
	}

	public AtsListDto calculateATS156(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts156 = new AtsListDto();
		AtsGenParamDto calAtsGen156 = new AtsGenParamDto();
		long ats156_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats156ParamData = getGeneralFinanceATSParam(atsGeneralId, atsParamId);

			if (ats156ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats156ParamData.get("logic_description");

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

			System.out.println("ATS-156 :: " + detectedFonts);

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
				calAtsNull.setAtsParamData(ats156ParamData);
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
				ats156_points = 0;
			} else if (hasStandard && hasNonStandard && partialScoreIfMixed) {
				ats156_points = fullScore - partialScore;
			} else if (hasStandard && !hasNonStandard && fullScoreIfStandard) {
				ats156_points = fullScore;
			} else {
				ats156_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats156_points == max) {
				paramType = "positive";
			} else if (ats156_points == partial) {
				paramType = "partial";
			} else if (ats156_points == 0) {
				paramType = "negative";
			}

			calAtsGen156.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen156.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen156.setCategory(genATSParamData.getCategory());
			calAtsGen156.setDescription(genATSParamData.getDescription());
			calAtsGen156.setMax_points(genATSParamData.getMax_points());
			calAtsGen156.setParameter(genATSParamData.getParameter());
			calAtsGen156.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen156.setTotal_points(genATSParamData.getTotal_points());

			calAts156.setAtsGeneralId(atsGeneralId);
			calAts156.setAtsGeneralParamDto(calAtsGen156);
			calAts156.setAtsParamData(ats156ParamData);
			calAts156.setAtsParamId(atsParamId);
			calAts156.setAtsParamType(paramType);
			calAts156.setAtsScore(ats156_points);

			return calAts156;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts156;
		}
	}

	public AtsListDto calculateATS157(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts157 = new AtsListDto();
		AtsGenParamDto calAtsGen157 = new AtsGenParamDto();
		long ats157_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats157ParamData = getGeneralFinanceATSParam(atsGeneralId, atsParamId);

			if (ats157ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats157ParamData.get("logic_description");

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
					System.out.println("ATS-157 :: " + sectionKey);
					matchedSections.add(sectionKey);
				}
			}

			long sectionCount = ((Number) matchedSections.size()).longValue();

			// 6 Scoring logic
			if (sectionCount >= minFull) {
				ats157_points = genATSParamData.getMax_points();
			} else if (sectionCount >= minPartial && sectionCount < minFull) {
				ats157_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (sectionCount < minPartial) {
				ats157_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats157_points == max) {
				paramType = "positive";
			} else if (ats157_points == partial) {
				paramType = "partial";
			} else if (ats157_points == 0) {
				paramType = "negative";
			}

			calAtsGen157.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen157.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen157.setCategory(genATSParamData.getCategory());
			calAtsGen157.setDescription(genATSParamData.getDescription());
			calAtsGen157.setMax_points(genATSParamData.getMax_points());
			calAtsGen157.setParameter(genATSParamData.getParameter());
			calAtsGen157.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen157.setTotal_points(genATSParamData.getTotal_points());

			calAts157.setAtsGeneralId(atsGeneralId);
			calAts157.setAtsGeneralParamDto(calAtsGen157);
			calAts157.setAtsParamData(ats157ParamData);
			calAts157.setAtsParamId(atsParamId);
			calAts157.setAtsParamType(paramType);
			calAts157.setAtsScore(ats157_points);

			return calAts157;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts157;
		}
	}

	public AtsListDto calculateATS158(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts158 = new AtsListDto();
		AtsGenParamDto calAtsGen158 = new AtsGenParamDto();
		long ats158_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats158ParamData = getGeneralFinanceATSParam(atsGeneralId, atsParamId);

			if (ats158ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats158ParamData.get("logic_description");

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
						System.out.println("ATS-158 :: " + key);
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
				ats158_points = genATSParamData.getMax_points();
			} else if (violations <= maxPartial) {
				ats158_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (violations > maxPartial) {
				ats158_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats158_points == max) {
				paramType = "positive";
			} else if (ats158_points == partial) {
				paramType = "partial";
			} else if (ats158_points == 0) {
				paramType = "negative";
			}

			calAtsGen158.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen158.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen158.setCategory(genATSParamData.getCategory());
			calAtsGen158.setDescription(genATSParamData.getDescription());
			calAtsGen158.setMax_points(genATSParamData.getMax_points());
			calAtsGen158.setParameter(genATSParamData.getParameter());
			calAtsGen158.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen158.setTotal_points(genATSParamData.getTotal_points());

			calAts158.setAtsGeneralId(atsGeneralId);
			calAts158.setAtsGeneralParamDto(calAtsGen158);
			calAts158.setAtsParamData(ats158ParamData);
			calAts158.setAtsParamId(atsParamId);
			calAts158.setAtsParamType(paramType);
			calAts158.setAtsScore(ats158_points);

			return calAts158;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts158;
		}
	}

	public AtsListDto calculateATS159(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts159 = new AtsListDto();
		AtsGenParamDto calAtsGen159 = new AtsGenParamDto();
		long ats159_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats159ParamData = getGeneralFinanceATSParam(atsGeneralId, atsParamId);

			if (ats159ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats159ParamData.get("logic_description");

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
						System.out.println("ATS-159 :: " + key);
						matchCount++;
					}
				}
			}

			// 6 Scoring
			if (matchCount >= minFull) {
				ats159_points = genATSParamData.getMax_points();
			} else if (matchCount >= minPartial && matchCount < minFull) {
				ats159_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchCount < minPartial) {
				ats159_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats159_points == max) {
				paramType = "positive";
			} else if (ats159_points == partial) {
				paramType = "partial";
			} else if (ats159_points == 0) {
				paramType = "negative";
			}

			calAtsGen159.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen159.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen159.setCategory(genATSParamData.getCategory());
			calAtsGen159.setDescription(genATSParamData.getDescription());
			calAtsGen159.setMax_points(genATSParamData.getMax_points());
			calAtsGen159.setParameter(genATSParamData.getParameter());
			calAtsGen159.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen159.setTotal_points(genATSParamData.getTotal_points());

			calAts159.setAtsGeneralId(atsGeneralId);
			calAts159.setAtsGeneralParamDto(calAtsGen159);
			calAts159.setAtsParamData(ats159ParamData);
			calAts159.setAtsParamId(atsParamId);
			calAts159.setAtsParamType(paramType);
			calAts159.setAtsScore(ats159_points);

			return calAts159;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts159;
		}
	}

	public AtsListDto calculateATS160(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts160 = new AtsListDto();
		AtsGenParamDto calAtsGen160 = new AtsGenParamDto();
		long ats160_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats160ParamData = getGeneralFinanceATSParam(atsGeneralId, atsParamId);

			if (ats160ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats160ParamData.get("logic_description");

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
					System.out.println("ATS-160 :: " + key);
					titleMatchCount++;
				}
			}

			// 6 Scoring
			if (titleMatchCount >= minFull) {
				ats160_points = genATSParamData.getMax_points();
			} else if (titleMatchCount >= minPartial && titleMatchCount < minFull) {
				ats160_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (titleMatchCount < minPartial) {
				ats160_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats160_points == max) {
				paramType = "positive";
			} else if (ats160_points == partial) {
				paramType = "partial";
			} else if (ats160_points == 0) {
				paramType = "negative";
			}

			calAtsGen160.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen160.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen160.setCategory(genATSParamData.getCategory());
			calAtsGen160.setDescription(genATSParamData.getDescription());
			calAtsGen160.setMax_points(genATSParamData.getMax_points());
			calAtsGen160.setParameter(genATSParamData.getParameter());
			calAtsGen160.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen160.setTotal_points(genATSParamData.getTotal_points());

			calAts160.setAtsGeneralId(atsGeneralId);
			calAts160.setAtsGeneralParamDto(calAtsGen160);
			calAts160.setAtsParamData(ats160ParamData);
			calAts160.setAtsParamId(atsParamId);
			calAts160.setAtsParamType(paramType);
			calAts160.setAtsScore(ats160_points);

			return calAts160;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts160;
		}
	}

	public AtsListDto calculateATS161(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts161 = new AtsListDto();
		AtsGenParamDto calAtsGen161 = new AtsGenParamDto();
		long ats161_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats161ParamData = getGeneralFinanceATSParam(atsGeneralId, atsParamId);

			if (ats161ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats161ParamData.get("logic_description");

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
					System.out.println("ATS-161 :: " + regex);
					dateMatchCount++;
				}
			}

			// 6 Scoring (binary)
			if (dateMatchCount >= minMatches) {
				ats161_points = genATSParamData.getMax_points();
			} else if (dateMatchCount < minMatches) {
				ats161_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats161_points == max) {
				paramType = "positive";
			} else if (ats161_points == partial) {
				paramType = "partial";
			} else if (ats161_points == 0) {
				paramType = "negative";
			}

			calAtsGen161.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen161.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen161.setCategory(genATSParamData.getCategory());
			calAtsGen161.setDescription(genATSParamData.getDescription());
			calAtsGen161.setMax_points(genATSParamData.getMax_points());
			calAtsGen161.setParameter(genATSParamData.getParameter());
			calAtsGen161.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen161.setTotal_points(genATSParamData.getTotal_points());

			calAts161.setAtsGeneralId(atsGeneralId);
			calAts161.setAtsGeneralParamDto(calAtsGen161);
			calAts161.setAtsParamData(ats161ParamData);
			calAts161.setAtsParamId(atsParamId);
			calAts161.setAtsParamType(paramType);
			calAts161.setAtsScore(ats161_points);

			return calAts161;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts161;
		}
	}

	public AtsListDto calculateATS162(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts162 = new AtsListDto();
		AtsGenParamDto calAtsGen162 = new AtsGenParamDto();
		long ats162_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats162ParamData = getGeneralFinanceATSParam(atsGeneralId, atsParamId);

			if (ats162ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats162ParamData.get("logic_description");

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
						System.out.println("ATS-162 :: " + key);
						impactMatchCount++;
					}
				}
			}

			// 6 Additional numeric signal (very important)

			// Detect numbers like: 30%, 10k, 1M, 515ms, etc.
			if (resumeText.matches("(?s).*\\b\\d+(%|k|m|ms|s|x)?\\b.*")) {
				impactMatchCount++;
			}

			// 7 Scoring
			if (impactMatchCount >= minFull) {
				ats162_points = genATSParamData.getMax_points();
			} else if (impactMatchCount >= minPartial && impactMatchCount < minFull) {
				ats162_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (impactMatchCount < minPartial) {
				ats162_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats162_points == max) {
				paramType = "positive";
			} else if (ats162_points == partial) {
				paramType = "partial";
			} else if (ats162_points == 0) {
				paramType = "negative";
			}

			calAtsGen162.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen162.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen162.setCategory(genATSParamData.getCategory());
			calAtsGen162.setDescription(genATSParamData.getDescription());
			calAtsGen162.setMax_points(genATSParamData.getMax_points());
			calAtsGen162.setParameter(genATSParamData.getParameter());
			calAtsGen162.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen162.setTotal_points(genATSParamData.getTotal_points());

			calAts162.setAtsGeneralId(atsGeneralId);
			calAts162.setAtsGeneralParamDto(calAtsGen162);
			calAts162.setAtsParamData(ats162ParamData);
			calAts162.setAtsParamId(atsParamId);
			calAts162.setAtsParamType(paramType);
			calAts162.setAtsScore(ats162_points);

			return calAts162;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts162;
		}
	}

	public AtsListDto calculateATS163(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts163 = new AtsListDto();
		AtsGenParamDto calAtsGen163 = new AtsGenParamDto();
		long ats163_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats163ParamData = getGeneralFinanceATSParam(atsGeneralId, atsParamId);

			if (ats163ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats163ParamData.get("logic_description");

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
						System.out.println("ATS-163 :: " + key);
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
				ats163_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartial && matchedGroups < minFull) {
				ats163_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartial) {
				ats163_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats163_points == max) {
				paramType = "positive";
			} else if (ats163_points == partial) {
				paramType = "partial";
			} else if (ats163_points == 0) {
				paramType = "negative";
			}

			calAtsGen163.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen163.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen163.setCategory(genATSParamData.getCategory());
			calAtsGen163.setDescription(genATSParamData.getDescription());
			calAtsGen163.setMax_points(genATSParamData.getMax_points());
			calAtsGen163.setParameter(genATSParamData.getParameter());
			calAtsGen163.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen163.setTotal_points(genATSParamData.getTotal_points());

			calAts163.setAtsGeneralId(atsGeneralId);
			calAts163.setAtsGeneralParamDto(calAtsGen163);
			calAts163.setAtsParamData(ats163ParamData);
			calAts163.setAtsParamId(atsParamId);
			calAts163.setAtsParamType(paramType);
			calAts163.setAtsScore(ats163_points);

			return calAts163;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts163;
		}
	}

	public AtsListDto calculateATS164(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts164 = new AtsListDto();
		AtsGenParamDto calAtsGen164 = new AtsGenParamDto();
		long ats164_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats164ParamData = getGeneralFinanceATSParam(atsGeneralId, atsParamId);

			if (ats164ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats164ParamData.get("logic_description");

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
						System.out.println("ATS-164 :: " + key);
						matchedGroups++;
						break; // count group only once
					}
				}
			}

			// 6 Scoring
			if (matchedGroups >= minFull) {
				ats164_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartial && matchedGroups < minFull) {
				ats164_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartial) {
				ats164_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats164_points == max) {
				paramType = "positive";
			} else if (ats164_points == partial) {
				paramType = "partial";
			} else if (ats164_points == 0) {
				paramType = "negative";
			}

			calAtsGen164.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen164.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen164.setCategory(genATSParamData.getCategory());
			calAtsGen164.setDescription(genATSParamData.getDescription());
			calAtsGen164.setMax_points(genATSParamData.getMax_points());
			calAtsGen164.setParameter(genATSParamData.getParameter());
			calAtsGen164.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen164.setTotal_points(genATSParamData.getTotal_points());

			calAts164.setAtsGeneralId(atsGeneralId);
			calAts164.setAtsGeneralParamDto(calAtsGen164);
			calAts164.setAtsParamData(ats164ParamData);
			calAts164.setAtsParamId(atsParamId);
			calAts164.setAtsParamType(paramType);
			calAts164.setAtsScore(ats164_points);

			return calAts164;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts164;
		}
	}

	public AtsListDto calculateATS165(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts165 = new AtsListDto();
		AtsGenParamDto calAtsGen165 = new AtsGenParamDto();
		long ats165_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats165ParamData = getGeneralFinanceATSParam(atsGeneralId, atsParamId);

			if (ats165ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic description
			Map<String, Object> logicDescription = (Map<String, Object>) ats165ParamData.get("logic_description");

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
						System.out.println("ATS-165 :: " + key);
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
				ats165_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartialGroups && matchedGroups < minFullGroups) {
				ats165_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartialGroups) {
				ats165_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats165_points == max) {
				paramType = "positive";
			} else if (ats165_points == partial) {
				paramType = "partial";
			} else if (ats165_points == 0) {
				paramType = "negative";
			}

			calAtsGen165.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen165.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen165.setCategory(genATSParamData.getCategory());
			calAtsGen165.setDescription(genATSParamData.getDescription());
			calAtsGen165.setMax_points(genATSParamData.getMax_points());
			calAtsGen165.setParameter(genATSParamData.getParameter());
			calAtsGen165.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen165.setTotal_points(genATSParamData.getTotal_points());

			calAts165.setAtsGeneralId(atsGeneralId);
			calAts165.setAtsGeneralParamDto(calAtsGen165);
			calAts165.setAtsParamData(ats165ParamData);
			calAts165.setAtsParamId(atsParamId);
			calAts165.setAtsParamType(paramType);
			calAts165.setAtsScore(ats165_points);

			return calAts165;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts165;
		}
	}

	public AtsListDto calculateATS166(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts166 = new AtsListDto();
		AtsGenParamDto calAtsGen166 = new AtsGenParamDto();
		long ats166_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats166ParamData = getGeneralFinanceATSParam(atsGeneralId, atsParamId);

			if (ats166ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic description
			Map<String, Object> logicDescription = (Map<String, Object>) ats166ParamData.get("logic_description");

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
						System.out.println("ATS-166 :: " + key);
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
				ats166_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartialGroups && matchedGroups < minFullGroups) {
				ats166_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartialGroups) {
				ats166_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats166_points == max) {
				paramType = "positive";
			} else if (ats166_points == partial) {
				paramType = "partial";
			} else if (ats166_points == 0) {
				paramType = "negative";
			}

			calAtsGen166.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen166.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen166.setCategory(genATSParamData.getCategory());
			calAtsGen166.setDescription(genATSParamData.getDescription());
			calAtsGen166.setMax_points(genATSParamData.getMax_points());
			calAtsGen166.setParameter(genATSParamData.getParameter());
			calAtsGen166.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen166.setTotal_points(genATSParamData.getTotal_points());

			calAts166.setAtsGeneralId(atsGeneralId);
			calAts166.setAtsGeneralParamDto(calAtsGen166);
			calAts166.setAtsParamData(ats166ParamData);
			calAts166.setAtsParamId(atsParamId);
			calAts166.setAtsParamType(paramType);
			calAts166.setAtsScore(ats166_points);

			return calAts166;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts166;
		}
	}

	public AtsListDto calculateATS167(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts167 = new AtsListDto();
		AtsGenParamDto calAtsGen167 = new AtsGenParamDto();
		long ats167_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats167ParamData = getGeneralFinanceATSParam(atsGeneralId, atsParamId);

			if (ats167ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats167ParamData.get("logic_description");

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
						System.out.println("ATS-167 :: " + keyword);
						break;
					}
				}

				if (groupMatched) {
					matchedGroups++;
				}
			}

			// 6 Scoring
			if (matchedGroups >= minFullGroups) {
				ats167_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartialGroups && matchedGroups < minFullGroups) {
				ats167_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartialGroups) {
				ats167_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats167_points == max) {
				paramType = "positive";
			} else if (ats167_points == partial) {
				paramType = "partial";
			} else if (ats167_points == 0) {
				paramType = "negative";
			}

			calAtsGen167.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen167.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen167.setCategory(genATSParamData.getCategory());
			calAtsGen167.setDescription(genATSParamData.getDescription());
			calAtsGen167.setMax_points(genATSParamData.getMax_points());
			calAtsGen167.setParameter(genATSParamData.getParameter());
			calAtsGen167.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen167.setTotal_points(genATSParamData.getTotal_points());

			calAts167.setAtsGeneralId(atsGeneralId);
			calAts167.setAtsGeneralParamDto(calAtsGen167);
			calAts167.setAtsParamData(ats167ParamData);
			calAts167.setAtsParamId(atsParamId);
			calAts167.setAtsParamType(paramType);
			calAts167.setAtsScore(ats167_points);

			return calAts167;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts167;
		}
	}

	public AtsListDto calculateATS168(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts168 = new AtsListDto();
		AtsGenParamDto calAtsGen168 = new AtsGenParamDto();
		long ats168_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats168ParamData = getGeneralFinanceATSParam(atsGeneralId, atsParamId);

			if (ats168ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Fetch JSON configuration
			Map<String, Object> logicDesc = (Map<String, Object>) ats168ParamData.get("logic_description");
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
						System.out.println("ATS-168 :: " + errorPattern.toLowerCase());
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
				ats168_points = genATSParamData.getMax_points();
			} else if (totalErrors <= partialAllowed) {
				ats168_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (totalErrors >= zeroAbove) {
				ats168_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats168_points == max) {
				paramType = "positive";
			} else if (ats168_points == partial) {
				paramType = "partial";
			} else if (ats168_points == 0) {
				paramType = "negative";
			}

			calAtsGen168.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen168.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen168.setCategory(genATSParamData.getCategory());
			calAtsGen168.setDescription(genATSParamData.getDescription());
			calAtsGen168.setMax_points(genATSParamData.getMax_points());
			calAtsGen168.setParameter(genATSParamData.getParameter());
			calAtsGen168.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen168.setTotal_points(genATSParamData.getTotal_points());

			calAts168.setAtsGeneralId(atsGeneralId);
			calAts168.setAtsGeneralParamDto(calAtsGen168);
			calAts168.setAtsParamData(ats168ParamData);
			calAts168.setAtsParamId(atsParamId);
			calAts168.setAtsParamType(paramType);
			calAts168.setAtsScore(ats168_points);

			return calAts168;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts168;
		}
	}

	public AtsListDto calculateATS169(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts169 = new AtsListDto();
		AtsGenParamDto calAtsGen169 = new AtsGenParamDto();
		long ats169_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats169ParamData = getGeneralFinanceATSParam(atsGeneralId, atsParamId);

			if (ats169ParamData == null) {
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
			Map<String, Object> logicDesc = (Map<String, Object>) ats169ParamData.get("logic_description");

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
						System.out.println("ATS-169 :: " + indicator.toLowerCase());
						totalViolations++;
					}
				}
			}

			// 6 Scoring logic
			if (totalViolations <= fullScoreLimit) {
				ats169_points = genATSParamData.getMax_points();
			} else if (totalViolations <= partialScoreLimit) {
				ats169_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (totalViolations >= zeroScoreLimit) {
				ats169_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats169_points == max) {
				paramType = "positive";
			} else if (ats169_points == partial) {
				paramType = "partial";
			} else if (ats169_points == 0) {
				paramType = "negative";
			}

			calAtsGen169.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen169.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen169.setCategory(genATSParamData.getCategory());
			calAtsGen169.setDescription(genATSParamData.getDescription());
			calAtsGen169.setMax_points(genATSParamData.getMax_points());
			calAtsGen169.setParameter(genATSParamData.getParameter());
			calAtsGen169.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen169.setTotal_points(genATSParamData.getTotal_points());

			calAts169.setAtsGeneralId(atsGeneralId);
			calAts169.setAtsGeneralParamDto(calAtsGen169);
			calAts169.setAtsParamData(ats169ParamData);
			calAts169.setAtsParamId(atsParamId);
			calAts169.setAtsParamType(paramType);
			calAts169.setAtsScore(ats169_points);

			return calAts169;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts169;
		}
	}

	public AtsListDto calculateATS170(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts170 = new AtsListDto();
		AtsGenParamDto calAtsGen170 = new AtsGenParamDto();

		long ats170_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats170ParamData = getGeneralFinanceATSParam(atsGeneralId, atsParamId);

			if (ats170ParamData == null) {
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
			Map<String, Object> logicDesc = (Map<String, Object>) ats170ParamData.get("logic_description");

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
								System.out.println("ATS-170 :: " + indicator.toLowerCase());
								totalViolations++;
								break;
							}
						}
					}
				}
			}

			// 6 Scoring
			if (totalViolations <= fullScoreLimit) {
				ats170_points = genATSParamData.getMax_points();
			} else if (totalViolations <= partialScoreLimit) {
				ats170_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (totalViolations >= zeroScoreLimit
					|| (totalViolations > partialScoreLimit && totalViolations < zeroScoreLimit)) {
				ats170_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats170_points == max) {
				paramType = "positive";
			} else if (ats170_points == partial) {
				paramType = "partial";
			} else if (ats170_points == 0) {
				paramType = "negative";
			}

			calAtsGen170.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen170.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen170.setCategory(genATSParamData.getCategory());
			calAtsGen170.setDescription(genATSParamData.getDescription());
			calAtsGen170.setMax_points(genATSParamData.getMax_points());
			calAtsGen170.setParameter(genATSParamData.getParameter());
			calAtsGen170.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen170.setTotal_points(genATSParamData.getTotal_points());

			calAts170.setAtsGeneralId(atsGeneralId);
			calAts170.setAtsGeneralParamDto(calAtsGen170);
			calAts170.setAtsParamData(ats170ParamData);
			calAts170.setAtsParamId(atsParamId);
			calAts170.setAtsParamType(paramType);
			calAts170.setAtsScore(ats170_points);

			return calAts170;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts170;
		}
	}

}

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
public class EducationAtsUtil {

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

	private List<Map<String, Object>> generalEducationATSParameterList;

	private Map<String, Map<String, Object>> generalEducationATSParameterMap;

//	@PostConstruct
//	ITATSUtil(AtsGenParamDto atsGenParamDto) {
//		this.atsGenParamDto = atsGenParamDto;
//	}

	@PostConstruct
	public void loadEducation_ATSParameter() {
		try {

			Resource resource = resourceLoader.getResource("classpath:Education_ATSParameter.json");

			// Read JSON as Map
			Map<String, Object> jsonData = objectMapper.readValue(resource.getInputStream(), Map.class);

			// Extract array
			generalEducationATSParameterList = (List<Map<String, Object>>) jsonData.get("Education_ATSParameter");

			// Create fast-lookup map
			generalEducationATSParameterMap = new HashMap<>();

			for (Map<String, Object> param : generalEducationATSParameterList) {
				Number atsGeneralIdNum = (Number) param.get("atsGeneralId");
				long atsGeneralId = atsGeneralIdNum.longValue();
				String atsParamId = (String) param.get("atsParamId");

				String key = atsGeneralId + "_" + atsParamId;
				generalEducationATSParameterMap.put(key, param);
			}

			System.out.println("Loaded ATS Params Count: " + generalEducationATSParameterMap.size());

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load Education_ATSParameter.json", e);
		}
	}

	public List<Map<String, Object>> getAllGeneralATSParams() {
//		System.out.println("getAllGeneralATSParams() --> generalEducationATSParameterList :: " + generalEducationATSParameterList);
		return generalEducationATSParameterList;
	}

	public Map<String, Object> getGeneralEducationATSParam(long atsGeneralId, String atsParamId) {
//		System.out.println("getGeneralEducationATSParam(long " + atsGeneralId + ", String " + atsParamId
//				+ ") --> generalEducationATSParameterMap :: " + generalEducationATSParameterMap.get(atsGeneralId + "_" + atsParamId));
		return generalEducationATSParameterMap.get(atsGeneralId + "_" + atsParamId);
	}

	public AtsListDto calculateATS121(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts121 = new AtsListDto();
		AtsGenParamDto calAtsGen121 = new AtsGenParamDto();
		long ats121_points = 0;
		try {

			// 1️ Fetch JSON parameter config
			Map<String, Object> ats121ParamData = getGeneralEducationATSParam(atsGeneralId, atsParamId);
			if (ats121ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config (points / penalty)
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);
			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats121ParamData.get("logic_description");

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
					System.out.println("ATS-121 :: " + key);
					matchCount++;
				}
			}

//	     6️ Scoring logic
			if (matchCount >= minFull) {
				ats121_points = genATSParamData.getMax_points();
			} else if ((matchCount >= minPartial) && (matchCount < minFull)) {
				ats121_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if ((matchCount <= 0) && (matchCount < minPartial)) {
				ats121_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats121_points == max) {
				paramType = "positive";
			} else if (ats121_points == partial) {
				paramType = "partial";
			} else if (ats121_points == 0) {
				paramType = "negative";
			}

			calAtsGen121.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen121.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen121.setCategory(genATSParamData.getCategory());
			calAtsGen121.setDescription(genATSParamData.getDescription());
			calAtsGen121.setMax_points(genATSParamData.getMax_points());
			calAtsGen121.setParameter(genATSParamData.getParameter());
			calAtsGen121.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen121.setTotal_points(genATSParamData.getTotal_points());

			calAts121.setAtsGeneralId(atsGeneralId);
			calAts121.setAtsGeneralParamDto(calAtsGen121);
			calAts121.setAtsParamData(ats121ParamData);
			calAts121.setAtsParamId(atsParamId);
			calAts121.setAtsParamType(paramType);
			calAts121.setAtsScore(ats121_points);

			return calAts121;

		} catch (Exception e) {
			e.printStackTrace();
			return calAts121;
		}
	}

	public AtsListDto calculateATS122(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts122 = new AtsListDto();
		AtsGenParamDto calAtsGen122 = new AtsGenParamDto();
		long ats122_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats122ParamData = getGeneralEducationATSParam(atsGeneralId, atsParamId);

			if (ats122ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats122ParamData.get("logic_description");

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
						System.out.println("ATS-122 :: " + key);
						matchedCategories++;
					}
				}
			}

			// 6️ Scoring logic
			if (matchedCategories >= minFull) {
				ats122_points = genATSParamData.getMax_points();
			} else if (matchedCategories >= minPartial && matchedCategories < minFull) {
				ats122_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else {
				ats122_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats122_points == max) {
				paramType = "positive";
			} else if (ats122_points == partial) {
				paramType = "partial";
			} else if (ats122_points == 0) {
				paramType = "negative";
			}

			calAtsGen122.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen122.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen122.setCategory(genATSParamData.getCategory());
			calAtsGen122.setDescription(genATSParamData.getDescription());
			calAtsGen122.setMax_points(genATSParamData.getMax_points());
			calAtsGen122.setParameter(genATSParamData.getParameter());
			calAtsGen122.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen122.setTotal_points(genATSParamData.getTotal_points());

			calAts122.setAtsGeneralId(atsGeneralId);
			calAts122.setAtsGeneralParamDto(calAtsGen122);
			calAts122.setAtsParamData(ats122ParamData);
			calAts122.setAtsParamId(atsParamId);
			calAts122.setAtsParamType(paramType);
			calAts122.setAtsScore(ats122_points);

			return calAts122;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts122;
		}
	}

	public AtsListDto calculateATS123(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts123 = new AtsListDto();
		AtsGenParamDto calAtsGen123 = new AtsGenParamDto();
		long ats123_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats123ParamData = getGeneralEducationATSParam(atsGeneralId, atsParamId);

			if (ats123ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats123ParamData.get("logic_description");

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
						System.out.println("ATS-123 :: " + alternate);
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
				ats123_points = 0;
			} else if (matchedVariationGroups <= minFull && matchedVariationGroups > minPartial) {
				ats123_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedVariationGroups <= minPartial) {
				ats123_points = genATSParamData.getMax_points();
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats123_points == max) {
				paramType = "positive";
			} else if (ats123_points == partial) {
				paramType = "partial";
			} else if (ats123_points == 0) {
				paramType = "negative";
			}

			calAtsGen123.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen123.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen123.setCategory(genATSParamData.getCategory());
			calAtsGen123.setDescription(genATSParamData.getDescription());
			calAtsGen123.setMax_points(genATSParamData.getMax_points());
			calAtsGen123.setParameter(genATSParamData.getParameter());
			calAtsGen123.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen123.setTotal_points(genATSParamData.getTotal_points());

			calAts123.setAtsGeneralId(atsGeneralId);
			calAts123.setAtsGeneralParamDto(calAtsGen123);
			calAts123.setAtsParamData(ats123ParamData);
			calAts123.setAtsParamId(atsParamId);
			calAts123.setAtsParamType(paramType);
			calAts123.setAtsScore(ats123_points);

			return calAts123;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts123;
		}
	}

	public AtsListDto calculateATS124(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts124 = new AtsListDto();
		AtsGenParamDto calAtsGen124 = new AtsGenParamDto();
		long ats124_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats124ParamData = getGeneralEducationATSParam(atsGeneralId, atsParamId);

			if (ats124ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats124ParamData.get("logic_description");

			Map<String, List<String>> acceptedFormats = (Map<String, List<String>>) logicDescription
					.get("accepted_formats");

			List<String> fullScoreFormats = acceptedFormats.get("full_score");
			List<String> partialScoreFormats = acceptedFormats.get("partial_score");

			// 4️ Extract file extension
			if (fileName == null || !fileName.contains(".")) {
				return null;
			}

			String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

			System.out.println("ATS-124 :: " + extension);

			// 5️ Scoring logic
			if (fullScoreFormats.contains(extension)) {
				ats124_points = genATSParamData.getMax_points();
			} else if (partialScoreFormats.contains(extension)) {
				ats124_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else {
				ats124_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats124_points == max) {
				paramType = "positive";
			} else if (ats124_points == partial) {
				paramType = "partial";
			} else if (ats124_points == 0) {
				paramType = "negative";
			}

			calAtsGen124.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen124.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen124.setCategory(genATSParamData.getCategory());
			calAtsGen124.setDescription(genATSParamData.getDescription());
			calAtsGen124.setMax_points(genATSParamData.getMax_points());
			calAtsGen124.setParameter(genATSParamData.getParameter());
			calAtsGen124.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen124.setTotal_points(genATSParamData.getTotal_points());

			calAts124.setAtsGeneralId(atsGeneralId);
			calAts124.setAtsGeneralParamDto(calAtsGen124);
			calAts124.setAtsParamData(ats124ParamData);
			calAts124.setAtsParamId(atsParamId);
			calAts124.setAtsParamType(paramType);
			calAts124.setAtsScore(ats124_points);

			return calAts124;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts124;
		}
	}

	public AtsListDto calculateATS125(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts125 = new AtsListDto();
		AtsGenParamDto calAtsGen125 = new AtsGenParamDto();
		long ats125_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats125ParamData = getGeneralEducationATSParam(atsGeneralId, atsParamId);

			if (ats125ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats125ParamData.get("logic_description");

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
						System.out.println("ATS-125 :: " + indicator);
						layoutViolations++;
					}
				}
			}

			// 6 Scoring Logic
			if (layoutViolations <= maxFull) {
				ats125_points = genATSParamData.getMax_points();
			} else if (layoutViolations <= maxPartial) {
				ats125_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else {
				ats125_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats125_points == max) {
				paramType = "positive";
			} else if (ats125_points == partial) {
				paramType = "partial";
			} else if (ats125_points == 0) {
				paramType = "negative";
			}

			calAtsGen125.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen125.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen125.setCategory(genATSParamData.getCategory());
			calAtsGen125.setDescription(genATSParamData.getDescription());
			calAtsGen125.setMax_points(genATSParamData.getMax_points());
			calAtsGen125.setParameter(genATSParamData.getParameter());
			calAtsGen125.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen125.setTotal_points(genATSParamData.getTotal_points());

			calAts125.setAtsGeneralId(atsGeneralId);
			calAts125.setAtsGeneralParamDto(calAtsGen125);
			calAts125.setAtsParamData(ats125ParamData);
			calAts125.setAtsParamId(atsParamId);
			calAts125.setAtsParamType(paramType);
			calAts125.setAtsScore(ats125_points);

			return calAts125;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts125;
		}
	}

	public AtsListDto calculateATS126(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts126 = new AtsListDto();
		AtsGenParamDto calAtsGen126 = new AtsGenParamDto();
		long ats126_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats126ParamData = getGeneralEducationATSParam(atsGeneralId, atsParamId);

			if (ats126ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats126ParamData.get("logic_description");

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

			System.out.println("ATS-126 :: " + detectedFonts);

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
				calAtsNull.setAtsParamData(ats126ParamData);
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
				ats126_points = 0;
			} else if (hasStandard && hasNonStandard && partialScoreIfMixed) {
				ats126_points = fullScore - partialScore;
			} else if (hasStandard && !hasNonStandard && fullScoreIfStandard) {
				ats126_points = fullScore;
			} else {
				ats126_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats126_points == max) {
				paramType = "positive";
			} else if (ats126_points == partial) {
				paramType = "partial";
			} else if (ats126_points == 0) {
				paramType = "negative";
			}

			calAtsGen126.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen126.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen126.setCategory(genATSParamData.getCategory());
			calAtsGen126.setDescription(genATSParamData.getDescription());
			calAtsGen126.setMax_points(genATSParamData.getMax_points());
			calAtsGen126.setParameter(genATSParamData.getParameter());
			calAtsGen126.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen126.setTotal_points(genATSParamData.getTotal_points());

			calAts126.setAtsGeneralId(atsGeneralId);
			calAts126.setAtsGeneralParamDto(calAtsGen126);
			calAts126.setAtsParamData(ats126ParamData);
			calAts126.setAtsParamId(atsParamId);
			calAts126.setAtsParamType(paramType);
			calAts126.setAtsScore(ats126_points);

			return calAts126;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts126;
		}
	}

	public AtsListDto calculateATS127(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts127 = new AtsListDto();
		AtsGenParamDto calAtsGen127 = new AtsGenParamDto();
		long ats127_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats127ParamData = getGeneralEducationATSParam(atsGeneralId, atsParamId);

			if (ats127ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats127ParamData.get("logic_description");

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
					System.out.println("ATS-127 :: " + sectionKey);
					matchedSections.add(sectionKey);
				}
			}

			long sectionCount = ((Number) matchedSections.size()).longValue();

			// 6 Scoring logic
			if (sectionCount >= minFull) {
				ats127_points = genATSParamData.getMax_points();
			} else if (sectionCount >= minPartial && sectionCount < minFull) {
				ats127_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (sectionCount < minPartial) {
				ats127_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats127_points == max) {
				paramType = "positive";
			} else if (ats127_points == partial) {
				paramType = "partial";
			} else if (ats127_points == 0) {
				paramType = "negative";
			}

			calAtsGen127.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen127.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen127.setCategory(genATSParamData.getCategory());
			calAtsGen127.setDescription(genATSParamData.getDescription());
			calAtsGen127.setMax_points(genATSParamData.getMax_points());
			calAtsGen127.setParameter(genATSParamData.getParameter());
			calAtsGen127.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen127.setTotal_points(genATSParamData.getTotal_points());

			calAts127.setAtsGeneralId(atsGeneralId);
			calAts127.setAtsGeneralParamDto(calAtsGen127);
			calAts127.setAtsParamData(ats127ParamData);
			calAts127.setAtsParamId(atsParamId);
			calAts127.setAtsParamType(paramType);
			calAts127.setAtsScore(ats127_points);

			return calAts127;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts127;
		}
	}

	public AtsListDto calculateATS128(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts128 = new AtsListDto();
		AtsGenParamDto calAtsGen128 = new AtsGenParamDto();
		long ats128_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats128ParamData = getGeneralEducationATSParam(atsGeneralId, atsParamId);

			if (ats128ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats128ParamData.get("logic_description");

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
						System.out.println("ATS-128 :: " + key);
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
				ats128_points = genATSParamData.getMax_points();
			} else if (violations <= maxPartial) {
				ats128_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (violations > maxPartial) {
				ats128_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats128_points == max) {
				paramType = "positive";
			} else if (ats128_points == partial) {
				paramType = "partial";
			} else if (ats128_points == 0) {
				paramType = "negative";
			}

			calAtsGen128.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen128.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen128.setCategory(genATSParamData.getCategory());
			calAtsGen128.setDescription(genATSParamData.getDescription());
			calAtsGen128.setMax_points(genATSParamData.getMax_points());
			calAtsGen128.setParameter(genATSParamData.getParameter());
			calAtsGen128.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen128.setTotal_points(genATSParamData.getTotal_points());

			calAts128.setAtsGeneralId(atsGeneralId);
			calAts128.setAtsGeneralParamDto(calAtsGen128);
			calAts128.setAtsParamData(ats128ParamData);
			calAts128.setAtsParamId(atsParamId);
			calAts128.setAtsParamType(paramType);
			calAts128.setAtsScore(ats128_points);

			return calAts128;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts128;
		}
	}

	public AtsListDto calculateATS129(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts129 = new AtsListDto();
		AtsGenParamDto calAtsGen129 = new AtsGenParamDto();
		long ats129_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats129ParamData = getGeneralEducationATSParam(atsGeneralId, atsParamId);

			if (ats129ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats129ParamData.get("logic_description");

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
						System.out.println("ATS-129 :: " + key);
						matchCount++;
					}
				}
			}

			// 6 Scoring
			if (matchCount >= minFull) {
				ats129_points = genATSParamData.getMax_points();
			} else if (matchCount >= minPartial && matchCount < minFull) {
				ats129_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchCount < minPartial) {
				ats129_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats129_points == max) {
				paramType = "positive";
			} else if (ats129_points == partial) {
				paramType = "partial";
			} else if (ats129_points == 0) {
				paramType = "negative";
			}

			calAtsGen129.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen129.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen129.setCategory(genATSParamData.getCategory());
			calAtsGen129.setDescription(genATSParamData.getDescription());
			calAtsGen129.setMax_points(genATSParamData.getMax_points());
			calAtsGen129.setParameter(genATSParamData.getParameter());
			calAtsGen129.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen129.setTotal_points(genATSParamData.getTotal_points());

			calAts129.setAtsGeneralId(atsGeneralId);
			calAts129.setAtsGeneralParamDto(calAtsGen129);
			calAts129.setAtsParamData(ats129ParamData);
			calAts129.setAtsParamId(atsParamId);
			calAts129.setAtsParamType(paramType);
			calAts129.setAtsScore(ats129_points);

			return calAts129;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts129;
		}
	}

	public AtsListDto calculateATS130(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts130 = new AtsListDto();
		AtsGenParamDto calAtsGen130 = new AtsGenParamDto();
		long ats130_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats130ParamData = getGeneralEducationATSParam(atsGeneralId, atsParamId);

			if (ats130ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats130ParamData.get("logic_description");

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
					System.out.println("ATS-130 :: " + key);
					titleMatchCount++;
				}
			}

			// 6 Scoring
			if (titleMatchCount >= minFull) {
				ats130_points = genATSParamData.getMax_points();
			} else if (titleMatchCount >= minPartial && titleMatchCount < minFull) {
				ats130_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (titleMatchCount < minPartial) {
				ats130_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats130_points == max) {
				paramType = "positive";
			} else if (ats130_points == partial) {
				paramType = "partial";
			} else if (ats130_points == 0) {
				paramType = "negative";
			}

			calAtsGen130.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen130.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen130.setCategory(genATSParamData.getCategory());
			calAtsGen130.setDescription(genATSParamData.getDescription());
			calAtsGen130.setMax_points(genATSParamData.getMax_points());
			calAtsGen130.setParameter(genATSParamData.getParameter());
			calAtsGen130.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen130.setTotal_points(genATSParamData.getTotal_points());

			calAts130.setAtsGeneralId(atsGeneralId);
			calAts130.setAtsGeneralParamDto(calAtsGen130);
			calAts130.setAtsParamData(ats130ParamData);
			calAts130.setAtsParamId(atsParamId);
			calAts130.setAtsParamType(paramType);
			calAts130.setAtsScore(ats130_points);

			return calAts130;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts130;
		}
	}

	public AtsListDto calculateATS131(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts131 = new AtsListDto();
		AtsGenParamDto calAtsGen131 = new AtsGenParamDto();
		long ats131_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats131ParamData = getGeneralEducationATSParam(atsGeneralId, atsParamId);

			if (ats131ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats131ParamData.get("logic_description");

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
					System.out.println("ATS-131 :: " + regex);
					dateMatchCount++;
				}
			}

			// 6 Scoring (binary)
			if (dateMatchCount >= minMatches) {
				ats131_points = genATSParamData.getMax_points();
			} else if (dateMatchCount < minMatches) {
				ats131_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats131_points == max) {
				paramType = "positive";
			} else if (ats131_points == partial) {
				paramType = "partial";
			} else if (ats131_points == 0) {
				paramType = "negative";
			}

			calAtsGen131.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen131.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen131.setCategory(genATSParamData.getCategory());
			calAtsGen131.setDescription(genATSParamData.getDescription());
			calAtsGen131.setMax_points(genATSParamData.getMax_points());
			calAtsGen131.setParameter(genATSParamData.getParameter());
			calAtsGen131.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen131.setTotal_points(genATSParamData.getTotal_points());

			calAts131.setAtsGeneralId(atsGeneralId);
			calAts131.setAtsGeneralParamDto(calAtsGen131);
			calAts131.setAtsParamData(ats131ParamData);
			calAts131.setAtsParamId(atsParamId);
			calAts131.setAtsParamType(paramType);
			calAts131.setAtsScore(ats131_points);

			return calAts131;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts131;
		}
	}

	public AtsListDto calculateATS132(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts132 = new AtsListDto();
		AtsGenParamDto calAtsGen132 = new AtsGenParamDto();
		long ats132_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats132ParamData = getGeneralEducationATSParam(atsGeneralId, atsParamId);

			if (ats132ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats132ParamData.get("logic_description");

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
						System.out.println("ATS-132 :: " + key);
						impactMatchCount++;
					}
				}
			}

			// 6 Additional numeric signal (very important)

			// Detect numbers like: 30%, 10k, 1M, 512ms, etc.
			if (resumeText.matches("(?s).*\\b\\d+(%|k|m|ms|s|x)?\\b.*")) {
				impactMatchCount++;
			}

			// 7 Scoring
			if (impactMatchCount >= minFull) {
				ats132_points = genATSParamData.getMax_points();
			} else if (impactMatchCount >= minPartial && impactMatchCount < minFull) {
				ats132_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (impactMatchCount < minPartial) {
				ats132_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats132_points == max) {
				paramType = "positive";
			} else if (ats132_points == partial) {
				paramType = "partial";
			} else if (ats132_points == 0) {
				paramType = "negative";
			}

			calAtsGen132.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen132.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen132.setCategory(genATSParamData.getCategory());
			calAtsGen132.setDescription(genATSParamData.getDescription());
			calAtsGen132.setMax_points(genATSParamData.getMax_points());
			calAtsGen132.setParameter(genATSParamData.getParameter());
			calAtsGen132.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen132.setTotal_points(genATSParamData.getTotal_points());

			calAts132.setAtsGeneralId(atsGeneralId);
			calAts132.setAtsGeneralParamDto(calAtsGen132);
			calAts132.setAtsParamData(ats132ParamData);
			calAts132.setAtsParamId(atsParamId);
			calAts132.setAtsParamType(paramType);
			calAts132.setAtsScore(ats132_points);

			return calAts132;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts132;
		}
	}

	public AtsListDto calculateATS133(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts133 = new AtsListDto();
		AtsGenParamDto calAtsGen133 = new AtsGenParamDto();
		long ats133_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats133ParamData = getGeneralEducationATSParam(atsGeneralId, atsParamId);

			if (ats133ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats133ParamData.get("logic_description");

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
						System.out.println("ATS-133 :: " + key);
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
				ats133_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartial && matchedGroups < minFull) {
				ats133_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartial) {
				ats133_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats133_points == max) {
				paramType = "positive";
			} else if (ats133_points == partial) {
				paramType = "partial";
			} else if (ats133_points == 0) {
				paramType = "negative";
			}

			calAtsGen133.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen133.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen133.setCategory(genATSParamData.getCategory());
			calAtsGen133.setDescription(genATSParamData.getDescription());
			calAtsGen133.setMax_points(genATSParamData.getMax_points());
			calAtsGen133.setParameter(genATSParamData.getParameter());
			calAtsGen133.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen133.setTotal_points(genATSParamData.getTotal_points());

			calAts133.setAtsGeneralId(atsGeneralId);
			calAts133.setAtsGeneralParamDto(calAtsGen133);
			calAts133.setAtsParamData(ats133ParamData);
			calAts133.setAtsParamId(atsParamId);
			calAts133.setAtsParamType(paramType);
			calAts133.setAtsScore(ats133_points);

			return calAts133;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts133;
		}
	}

	public AtsListDto calculateATS134(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts134 = new AtsListDto();
		AtsGenParamDto calAtsGen134 = new AtsGenParamDto();
		long ats134_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats134ParamData = getGeneralEducationATSParam(atsGeneralId, atsParamId);

			if (ats134ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats134ParamData.get("logic_description");

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
						System.out.println("ATS-134 :: " + key);
						matchedGroups++;
						break; // count group only once
					}
				}
			}

			// 6 Scoring
			if (matchedGroups >= minFull) {
				ats134_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartial && matchedGroups < minFull) {
				ats134_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartial) {
				ats134_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats134_points == max) {
				paramType = "positive";
			} else if (ats134_points == partial) {
				paramType = "partial";
			} else if (ats134_points == 0) {
				paramType = "negative";
			}

			calAtsGen134.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen134.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen134.setCategory(genATSParamData.getCategory());
			calAtsGen134.setDescription(genATSParamData.getDescription());
			calAtsGen134.setMax_points(genATSParamData.getMax_points());
			calAtsGen134.setParameter(genATSParamData.getParameter());
			calAtsGen134.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen134.setTotal_points(genATSParamData.getTotal_points());

			calAts134.setAtsGeneralId(atsGeneralId);
			calAts134.setAtsGeneralParamDto(calAtsGen134);
			calAts134.setAtsParamData(ats134ParamData);
			calAts134.setAtsParamId(atsParamId);
			calAts134.setAtsParamType(paramType);
			calAts134.setAtsScore(ats134_points);

			return calAts134;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts134;
		}
	}

	public AtsListDto calculateATS135(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts135 = new AtsListDto();
		AtsGenParamDto calAtsGen135 = new AtsGenParamDto();
		long ats135_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats135ParamData = getGeneralEducationATSParam(atsGeneralId, atsParamId);

			if (ats135ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic description
			Map<String, Object> logicDescription = (Map<String, Object>) ats135ParamData.get("logic_description");

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
						System.out.println("ATS-135 :: " + key);
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
				ats135_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartialGroups && matchedGroups < minFullGroups) {
				ats135_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartialGroups) {
				ats135_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats135_points == max) {
				paramType = "positive";
			} else if (ats135_points == partial) {
				paramType = "partial";
			} else if (ats135_points == 0) {
				paramType = "negative";
			}

			calAtsGen135.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen135.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen135.setCategory(genATSParamData.getCategory());
			calAtsGen135.setDescription(genATSParamData.getDescription());
			calAtsGen135.setMax_points(genATSParamData.getMax_points());
			calAtsGen135.setParameter(genATSParamData.getParameter());
			calAtsGen135.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen135.setTotal_points(genATSParamData.getTotal_points());

			calAts135.setAtsGeneralId(atsGeneralId);
			calAts135.setAtsGeneralParamDto(calAtsGen135);
			calAts135.setAtsParamData(ats135ParamData);
			calAts135.setAtsParamId(atsParamId);
			calAts135.setAtsParamType(paramType);
			calAts135.setAtsScore(ats135_points);

			return calAts135;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts135;
		}
	}

	public AtsListDto calculateATS136(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts136 = new AtsListDto();
		AtsGenParamDto calAtsGen136 = new AtsGenParamDto();
		long ats136_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats136ParamData = getGeneralEducationATSParam(atsGeneralId, atsParamId);

			if (ats136ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic description
			Map<String, Object> logicDescription = (Map<String, Object>) ats136ParamData.get("logic_description");

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
						System.out.println("ATS-136 :: " + key);
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
				ats136_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartialGroups && matchedGroups < minFullGroups) {
				ats136_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartialGroups) {
				ats136_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats136_points == max) {
				paramType = "positive";
			} else if (ats136_points == partial) {
				paramType = "partial";
			} else if (ats136_points == 0) {
				paramType = "negative";
			}

			calAtsGen136.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen136.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen136.setCategory(genATSParamData.getCategory());
			calAtsGen136.setDescription(genATSParamData.getDescription());
			calAtsGen136.setMax_points(genATSParamData.getMax_points());
			calAtsGen136.setParameter(genATSParamData.getParameter());
			calAtsGen136.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen136.setTotal_points(genATSParamData.getTotal_points());

			calAts136.setAtsGeneralId(atsGeneralId);
			calAts136.setAtsGeneralParamDto(calAtsGen136);
			calAts136.setAtsParamData(ats136ParamData);
			calAts136.setAtsParamId(atsParamId);
			calAts136.setAtsParamType(paramType);
			calAts136.setAtsScore(ats136_points);

			return calAts136;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts136;
		}
	}

	public AtsListDto calculateATS137(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts137 = new AtsListDto();
		AtsGenParamDto calAtsGen137 = new AtsGenParamDto();
		long ats137_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats137ParamData = getGeneralEducationATSParam(atsGeneralId, atsParamId);

			if (ats137ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats137ParamData.get("logic_description");

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
						System.out.println("ATS-137 :: " + keyword);
						break;
					}
				}

				if (groupMatched) {
					matchedGroups++;
				}
			}

			// 6 Scoring
			if (matchedGroups >= minFullGroups) {
				ats137_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartialGroups && matchedGroups < minFullGroups) {
				ats137_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartialGroups) {
				ats137_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats137_points == max) {
				paramType = "positive";
			} else if (ats137_points == partial) {
				paramType = "partial";
			} else if (ats137_points == 0) {
				paramType = "negative";
			}

			calAtsGen137.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen137.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen137.setCategory(genATSParamData.getCategory());
			calAtsGen137.setDescription(genATSParamData.getDescription());
			calAtsGen137.setMax_points(genATSParamData.getMax_points());
			calAtsGen137.setParameter(genATSParamData.getParameter());
			calAtsGen137.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen137.setTotal_points(genATSParamData.getTotal_points());

			calAts137.setAtsGeneralId(atsGeneralId);
			calAts137.setAtsGeneralParamDto(calAtsGen137);
			calAts137.setAtsParamData(ats137ParamData);
			calAts137.setAtsParamId(atsParamId);
			calAts137.setAtsParamType(paramType);
			calAts137.setAtsScore(ats137_points);

			return calAts137;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts137;
		}
	}

	public AtsListDto calculateATS138(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts138 = new AtsListDto();
		AtsGenParamDto calAtsGen138 = new AtsGenParamDto();
		long ats138_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats138ParamData = getGeneralEducationATSParam(atsGeneralId, atsParamId);

			if (ats138ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Fetch JSON configuration
			Map<String, Object> logicDesc = (Map<String, Object>) ats138ParamData.get("logic_description");
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
						System.out.println("ATS-138 :: " + errorPattern.toLowerCase());
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
				ats138_points = genATSParamData.getMax_points();
			} else if (totalErrors <= partialAllowed) {
				ats138_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (totalErrors >= zeroAbove) {
				ats138_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats138_points == max) {
				paramType = "positive";
			} else if (ats138_points == partial) {
				paramType = "partial";
			} else if (ats138_points == 0) {
				paramType = "negative";
			}

			calAtsGen138.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen138.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen138.setCategory(genATSParamData.getCategory());
			calAtsGen138.setDescription(genATSParamData.getDescription());
			calAtsGen138.setMax_points(genATSParamData.getMax_points());
			calAtsGen138.setParameter(genATSParamData.getParameter());
			calAtsGen138.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen138.setTotal_points(genATSParamData.getTotal_points());

			calAts138.setAtsGeneralId(atsGeneralId);
			calAts138.setAtsGeneralParamDto(calAtsGen138);
			calAts138.setAtsParamData(ats138ParamData);
			calAts138.setAtsParamId(atsParamId);
			calAts138.setAtsParamType(paramType);
			calAts138.setAtsScore(ats138_points);

			return calAts138;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts138;
		}
	}

	public AtsListDto calculateATS139(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts139 = new AtsListDto();
		AtsGenParamDto calAtsGen139 = new AtsGenParamDto();
		long ats139_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats139ParamData = getGeneralEducationATSParam(atsGeneralId, atsParamId);

			if (ats139ParamData == null) {
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
			Map<String, Object> logicDesc = (Map<String, Object>) ats139ParamData.get("logic_description");

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
						System.out.println("ATS-139 :: " + indicator.toLowerCase());
						totalViolations++;
					}
				}
			}

			// 6 Scoring logic
			if (totalViolations <= fullScoreLimit) {
				ats139_points = genATSParamData.getMax_points();
			} else if (totalViolations <= partialScoreLimit) {
				ats139_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (totalViolations >= zeroScoreLimit) {
				ats139_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats139_points == max) {
				paramType = "positive";
			} else if (ats139_points == partial) {
				paramType = "partial";
			} else if (ats139_points == 0) {
				paramType = "negative";
			}

			calAtsGen139.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen139.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen139.setCategory(genATSParamData.getCategory());
			calAtsGen139.setDescription(genATSParamData.getDescription());
			calAtsGen139.setMax_points(genATSParamData.getMax_points());
			calAtsGen139.setParameter(genATSParamData.getParameter());
			calAtsGen139.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen139.setTotal_points(genATSParamData.getTotal_points());

			calAts139.setAtsGeneralId(atsGeneralId);
			calAts139.setAtsGeneralParamDto(calAtsGen139);
			calAts139.setAtsParamData(ats139ParamData);
			calAts139.setAtsParamId(atsParamId);
			calAts139.setAtsParamType(paramType);
			calAts139.setAtsScore(ats139_points);

			return calAts139;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts139;
		}
	}

	public AtsListDto calculateATS140(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts140 = new AtsListDto();
		AtsGenParamDto calAtsGen140 = new AtsGenParamDto();

		long ats140_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats140ParamData = getGeneralEducationATSParam(atsGeneralId, atsParamId);

			if (ats140ParamData == null) {
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
			Map<String, Object> logicDesc = (Map<String, Object>) ats140ParamData.get("logic_description");

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
								System.out.println("ATS-140 :: " + indicator.toLowerCase());
								totalViolations++;
								break;
							}
						}
					}
				}
			}

			// 6 Scoring
			if (totalViolations <= fullScoreLimit) {
				ats140_points = genATSParamData.getMax_points();
			} else if (totalViolations <= partialScoreLimit) {
				ats140_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (totalViolations >= zeroScoreLimit
					|| (totalViolations > partialScoreLimit && totalViolations < zeroScoreLimit)) {
				ats140_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats140_points == max) {
				paramType = "positive";
			} else if (ats140_points == partial) {
				paramType = "partial";
			} else if (ats140_points == 0) {
				paramType = "negative";
			}

			calAtsGen140.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen140.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen140.setCategory(genATSParamData.getCategory());
			calAtsGen140.setDescription(genATSParamData.getDescription());
			calAtsGen140.setMax_points(genATSParamData.getMax_points());
			calAtsGen140.setParameter(genATSParamData.getParameter());
			calAtsGen140.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen140.setTotal_points(genATSParamData.getTotal_points());

			calAts140.setAtsGeneralId(atsGeneralId);
			calAts140.setAtsGeneralParamDto(calAtsGen140);
			calAts140.setAtsParamData(ats140ParamData);
			calAts140.setAtsParamId(atsParamId);
			calAts140.setAtsParamType(paramType);
			calAts140.setAtsScore(ats140_points);

			return calAts140;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts140;
		}
	}

}

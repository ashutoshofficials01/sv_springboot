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
public class DesignAtsUtil {

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

	private List<Map<String, Object>> generalDesignATSParameterList;

	private Map<String, Map<String, Object>> generalDesignATSParameterMap;

//	@PostConstruct
//	ITATSUtil(AtsGenParamDto atsGenParamDto) {
//		this.atsGenParamDto = atsGenParamDto;
//	}

	@PostConstruct
	public void loadGeneralATSParameters() {
		try {

			Resource resource = resourceLoader.getResource("classpath:Design_ATSParameter.json");

			// Read JSON as Map
			Map<String, Object> jsonData = objectMapper.readValue(resource.getInputStream(), Map.class);

			// Extract array
			generalDesignATSParameterList = (List<Map<String, Object>>) jsonData.get("Design_ATSParameter");

			// Create fast-lookup map
			generalDesignATSParameterMap = new HashMap<>();

			for (Map<String, Object> param : generalDesignATSParameterList) {
				Number atsGeneralIdNum = (Number) param.get("atsGeneralId");
				long atsGeneralId = atsGeneralIdNum.longValue();
				String atsParamId = (String) param.get("atsParamId");

				String key = atsGeneralId + "_" + atsParamId;
				generalDesignATSParameterMap.put(key, param);
			}

			System.out.println("Loaded ATS Params Count: " + generalDesignATSParameterMap.size());

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load Design_ATSParameter.json", e);
		}
	}

	public List<Map<String, Object>> getAllGeneralATSParams() {
//		System.out.println("getAllGeneralATSParams() --> generalDesignATSParameterList :: " + generalDesignATSParameterList);
		return generalDesignATSParameterList;
	}

	public Map<String, Object> getGeneralDesignATSParam(long atsGeneralId, String atsParamId) {
//		System.out.println("getGeneralDesignATSParam(long " + atsGeneralId + ", String " + atsParamId
//				+ ") --> generalDesignATSParameterMap :: " + generalDesignATSParameterMap.get(atsGeneralId + "_" + atsParamId));
		return generalDesignATSParameterMap.get(atsGeneralId + "_" + atsParamId);
	}
	
	public AtsListDto calculateATS091(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts091 = new AtsListDto();
		AtsGenParamDto calAtsGen091 = new AtsGenParamDto();
		long ats091_points = 0;
		try {

			// 1️ Fetch JSON parameter config
			Map<String, Object> ats091ParamData = getGeneralDesignATSParam(atsGeneralId, atsParamId);
			if (ats091ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config (points / penalty)
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);
			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats091ParamData.get("logic_description");

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
					System.out.println("ATS-091 :: " + key);
					matchCount++;
				}
			}

//	     6️ Scoring logic
			if (matchCount >= minFull) {
				ats091_points = genATSParamData.getMax_points();
			} else if ((matchCount >= minPartial) && (matchCount < minFull)) {
				ats091_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if ((matchCount <= 0) && (matchCount < minPartial)) {
				ats091_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats091_points == max) {
				paramType = "positive";
			} else if (ats091_points == partial) {
				paramType = "partial";
			} else if (ats091_points == 0) {
				paramType = "negative";
			}

			calAtsGen091.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen091.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen091.setCategory(genATSParamData.getCategory());
			calAtsGen091.setDescription(genATSParamData.getDescription());
			calAtsGen091.setMax_points(genATSParamData.getMax_points());
			calAtsGen091.setParameter(genATSParamData.getParameter());
			calAtsGen091.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen091.setTotal_points(genATSParamData.getTotal_points());

			calAts091.setAtsGeneralId(atsGeneralId);
			calAts091.setAtsGeneralParamDto(calAtsGen091);
			calAts091.setAtsParamData(ats091ParamData);
			calAts091.setAtsParamId(atsParamId);
			calAts091.setAtsParamType(paramType);
			calAts091.setAtsScore(ats091_points);

			return calAts091;

		} catch (Exception e) {
			e.printStackTrace();
			return calAts091;
		}
	}

	public AtsListDto calculateATS092(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts092 = new AtsListDto();
		AtsGenParamDto calAtsGen092 = new AtsGenParamDto();
		long ats092_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats092ParamData = getGeneralDesignATSParam(atsGeneralId, atsParamId);

			if (ats092ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats092ParamData.get("logic_description");

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
						System.out.println("ATS-092 :: " + key);
						matchedCategories++;
					}
				}
			}

			// 6️ Scoring logic
			if (matchedCategories >= minFull) {
				ats092_points = genATSParamData.getMax_points();
			} else if (matchedCategories >= minPartial && matchedCategories < minFull) {
				ats092_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else {
				ats092_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats092_points == max) {
				paramType = "positive";
			} else if (ats092_points == partial) {
				paramType = "partial";
			} else if (ats092_points == 0) {
				paramType = "negative";
			}

			calAtsGen092.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen092.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen092.setCategory(genATSParamData.getCategory());
			calAtsGen092.setDescription(genATSParamData.getDescription());
			calAtsGen092.setMax_points(genATSParamData.getMax_points());
			calAtsGen092.setParameter(genATSParamData.getParameter());
			calAtsGen092.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen092.setTotal_points(genATSParamData.getTotal_points());

			calAts092.setAtsGeneralId(atsGeneralId);
			calAts092.setAtsGeneralParamDto(calAtsGen092);
			calAts092.setAtsParamData(ats092ParamData);
			calAts092.setAtsParamId(atsParamId);
			calAts092.setAtsParamType(paramType);
			calAts092.setAtsScore(ats092_points);

			return calAts092;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts092;
		}
	}

	public AtsListDto calculateATS093(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts093 = new AtsListDto();
		AtsGenParamDto calAtsGen093 = new AtsGenParamDto();
		long ats093_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats093ParamData = getGeneralDesignATSParam(atsGeneralId, atsParamId);

			if (ats093ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats093ParamData.get("logic_description");

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
						System.out.println("ATS-093 :: " + alternate);
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
				ats093_points = 0;
			} else if (matchedVariationGroups <= minFull && matchedVariationGroups > minPartial) {
				ats093_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedVariationGroups <= minPartial) {
				ats093_points = genATSParamData.getMax_points();
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats093_points == max) {
				paramType = "positive";
			} else if (ats093_points == partial) {
				paramType = "partial";
			} else if (ats093_points == 0) {
				paramType = "negative";
			}

			calAtsGen093.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen093.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen093.setCategory(genATSParamData.getCategory());
			calAtsGen093.setDescription(genATSParamData.getDescription());
			calAtsGen093.setMax_points(genATSParamData.getMax_points());
			calAtsGen093.setParameter(genATSParamData.getParameter());
			calAtsGen093.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen093.setTotal_points(genATSParamData.getTotal_points());

			calAts093.setAtsGeneralId(atsGeneralId);
			calAts093.setAtsGeneralParamDto(calAtsGen093);
			calAts093.setAtsParamData(ats093ParamData);
			calAts093.setAtsParamId(atsParamId);
			calAts093.setAtsParamType(paramType);
			calAts093.setAtsScore(ats093_points);

			return calAts093;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts093;
		}
	}

	public AtsListDto calculateATS094(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts094 = new AtsListDto();
		AtsGenParamDto calAtsGen094 = new AtsGenParamDto();
		long ats094_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats094ParamData = getGeneralDesignATSParam(atsGeneralId, atsParamId);

			if (ats094ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats094ParamData.get("logic_description");

			Map<String, List<String>> acceptedFormats = (Map<String, List<String>>) logicDescription
					.get("accepted_formats");

			List<String> fullScoreFormats = acceptedFormats.get("full_score");
			List<String> partialScoreFormats = acceptedFormats.get("partial_score");

			// 4️ Extract file extension
			if (fileName == null || !fileName.contains(".")) {
				return null;
			}

			String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

			System.out.println("ATS-094 :: " + extension);

			// 5️ Scoring logic
			if (fullScoreFormats.contains(extension)) {
				ats094_points = genATSParamData.getMax_points();
			} else if (partialScoreFormats.contains(extension)) {
				ats094_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else {
				ats094_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats094_points == max) {
				paramType = "positive";
			} else if (ats094_points == partial) {
				paramType = "partial";
			} else if (ats094_points == 0) {
				paramType = "negative";
			}

			calAtsGen094.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen094.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen094.setCategory(genATSParamData.getCategory());
			calAtsGen094.setDescription(genATSParamData.getDescription());
			calAtsGen094.setMax_points(genATSParamData.getMax_points());
			calAtsGen094.setParameter(genATSParamData.getParameter());
			calAtsGen094.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen094.setTotal_points(genATSParamData.getTotal_points());

			calAts094.setAtsGeneralId(atsGeneralId);
			calAts094.setAtsGeneralParamDto(calAtsGen094);
			calAts094.setAtsParamData(ats094ParamData);
			calAts094.setAtsParamId(atsParamId);
			calAts094.setAtsParamType(paramType);
			calAts094.setAtsScore(ats094_points);

			return calAts094;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts094;
		}
	}

	public AtsListDto calculateATS095(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts095 = new AtsListDto();
		AtsGenParamDto calAtsGen095 = new AtsGenParamDto();
		long ats095_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats095ParamData = getGeneralDesignATSParam(atsGeneralId, atsParamId);

			if (ats095ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats095ParamData.get("logic_description");

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
						System.out.println("ATS-095 :: " + indicator);
						layoutViolations++;
					}
				}
			}

			// 6 Scoring Logic
			if (layoutViolations <= maxFull) {
				ats095_points = genATSParamData.getMax_points();
			} else if (layoutViolations <= maxPartial) {
				ats095_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else {
				ats095_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats095_points == max) {
				paramType = "positive";
			} else if (ats095_points == partial) {
				paramType = "partial";
			} else if (ats095_points == 0) {
				paramType = "negative";
			}

			calAtsGen095.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen095.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen095.setCategory(genATSParamData.getCategory());
			calAtsGen095.setDescription(genATSParamData.getDescription());
			calAtsGen095.setMax_points(genATSParamData.getMax_points());
			calAtsGen095.setParameter(genATSParamData.getParameter());
			calAtsGen095.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen095.setTotal_points(genATSParamData.getTotal_points());

			calAts095.setAtsGeneralId(atsGeneralId);
			calAts095.setAtsGeneralParamDto(calAtsGen095);
			calAts095.setAtsParamData(ats095ParamData);
			calAts095.setAtsParamId(atsParamId);
			calAts095.setAtsParamType(paramType);
			calAts095.setAtsScore(ats095_points);

			return calAts095;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts095;
		}
	}

	public AtsListDto calculateATS096(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts096 = new AtsListDto();
		AtsGenParamDto calAtsGen096 = new AtsGenParamDto();
		long ats096_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats096ParamData = getGeneralDesignATSParam(atsGeneralId, atsParamId);

			if (ats096ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats096ParamData.get("logic_description");

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

			System.out.println("ATS-096 :: " + detectedFonts);

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
				calAtsNull.setAtsParamData(ats096ParamData);
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
				ats096_points = 0;
			} else if (hasStandard && hasNonStandard && partialScoreIfMixed) {
				ats096_points = fullScore - partialScore;
			} else if (hasStandard && !hasNonStandard && fullScoreIfStandard) {
				ats096_points = fullScore;
			} else {
				ats096_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats096_points == max) {
				paramType = "positive";
			} else if (ats096_points == partial) {
				paramType = "partial";
			} else if (ats096_points == 0) {
				paramType = "negative";
			}

			calAtsGen096.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen096.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen096.setCategory(genATSParamData.getCategory());
			calAtsGen096.setDescription(genATSParamData.getDescription());
			calAtsGen096.setMax_points(genATSParamData.getMax_points());
			calAtsGen096.setParameter(genATSParamData.getParameter());
			calAtsGen096.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen096.setTotal_points(genATSParamData.getTotal_points());

			calAts096.setAtsGeneralId(atsGeneralId);
			calAts096.setAtsGeneralParamDto(calAtsGen096);
			calAts096.setAtsParamData(ats096ParamData);
			calAts096.setAtsParamId(atsParamId);
			calAts096.setAtsParamType(paramType);
			calAts096.setAtsScore(ats096_points);

			return calAts096;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts096;
		}
	}

	public AtsListDto calculateATS097(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts097 = new AtsListDto();
		AtsGenParamDto calAtsGen097 = new AtsGenParamDto();
		long ats097_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats097ParamData = getGeneralDesignATSParam(atsGeneralId, atsParamId);

			if (ats097ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats097ParamData.get("logic_description");

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
					System.out.println("ATS-097 :: " + sectionKey);
					matchedSections.add(sectionKey);
				}
			}

			long sectionCount = ((Number) matchedSections.size()).longValue();

			// 6 Scoring logic
			if (sectionCount >= minFull) {
				ats097_points = genATSParamData.getMax_points();
			} else if (sectionCount >= minPartial && sectionCount < minFull) {
				ats097_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (sectionCount < minPartial) {
				ats097_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats097_points == max) {
				paramType = "positive";
			} else if (ats097_points == partial) {
				paramType = "partial";
			} else if (ats097_points == 0) {
				paramType = "negative";
			}

			calAtsGen097.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen097.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen097.setCategory(genATSParamData.getCategory());
			calAtsGen097.setDescription(genATSParamData.getDescription());
			calAtsGen097.setMax_points(genATSParamData.getMax_points());
			calAtsGen097.setParameter(genATSParamData.getParameter());
			calAtsGen097.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen097.setTotal_points(genATSParamData.getTotal_points());

			calAts097.setAtsGeneralId(atsGeneralId);
			calAts097.setAtsGeneralParamDto(calAtsGen097);
			calAts097.setAtsParamData(ats097ParamData);
			calAts097.setAtsParamId(atsParamId);
			calAts097.setAtsParamType(paramType);
			calAts097.setAtsScore(ats097_points);

			return calAts097;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts097;
		}
	}

	public AtsListDto calculateATS098(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts098 = new AtsListDto();
		AtsGenParamDto calAtsGen098 = new AtsGenParamDto();
		long ats098_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats098ParamData = getGeneralDesignATSParam(atsGeneralId, atsParamId);

			if (ats098ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats098ParamData.get("logic_description");

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
						System.out.println("ATS-098 :: " + key);
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
				ats098_points = genATSParamData.getMax_points();
			} else if (violations <= maxPartial) {
				ats098_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (violations > maxPartial) {
				ats098_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats098_points == max) {
				paramType = "positive";
			} else if (ats098_points == partial) {
				paramType = "partial";
			} else if (ats098_points == 0) {
				paramType = "negative";
			}

			calAtsGen098.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen098.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen098.setCategory(genATSParamData.getCategory());
			calAtsGen098.setDescription(genATSParamData.getDescription());
			calAtsGen098.setMax_points(genATSParamData.getMax_points());
			calAtsGen098.setParameter(genATSParamData.getParameter());
			calAtsGen098.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen098.setTotal_points(genATSParamData.getTotal_points());

			calAts098.setAtsGeneralId(atsGeneralId);
			calAts098.setAtsGeneralParamDto(calAtsGen098);
			calAts098.setAtsParamData(ats098ParamData);
			calAts098.setAtsParamId(atsParamId);
			calAts098.setAtsParamType(paramType);
			calAts098.setAtsScore(ats098_points);

			return calAts098;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts098;
		}
	}

	public AtsListDto calculateATS099(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts099 = new AtsListDto();
		AtsGenParamDto calAtsGen099 = new AtsGenParamDto();
		long ats099_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats099ParamData = getGeneralDesignATSParam(atsGeneralId, atsParamId);

			if (ats099ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats099ParamData.get("logic_description");

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
						System.out.println("ATS-099 :: " + key);
						matchCount++;
					}
				}
			}

			// 6 Scoring
			if (matchCount >= minFull) {
				ats099_points = genATSParamData.getMax_points();
			} else if (matchCount >= minPartial && matchCount < minFull) {
				ats099_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchCount < minPartial) {
				ats099_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats099_points == max) {
				paramType = "positive";
			} else if (ats099_points == partial) {
				paramType = "partial";
			} else if (ats099_points == 0) {
				paramType = "negative";
			}

			calAtsGen099.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen099.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen099.setCategory(genATSParamData.getCategory());
			calAtsGen099.setDescription(genATSParamData.getDescription());
			calAtsGen099.setMax_points(genATSParamData.getMax_points());
			calAtsGen099.setParameter(genATSParamData.getParameter());
			calAtsGen099.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen099.setTotal_points(genATSParamData.getTotal_points());

			calAts099.setAtsGeneralId(atsGeneralId);
			calAts099.setAtsGeneralParamDto(calAtsGen099);
			calAts099.setAtsParamData(ats099ParamData);
			calAts099.setAtsParamId(atsParamId);
			calAts099.setAtsParamType(paramType);
			calAts099.setAtsScore(ats099_points);

			return calAts099;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts099;
		}
	}

	public AtsListDto calculateATS100(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts100 = new AtsListDto();
		AtsGenParamDto calAtsGen100 = new AtsGenParamDto();
		long ats100_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats100ParamData = getGeneralDesignATSParam(atsGeneralId, atsParamId);

			if (ats100ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats100ParamData.get("logic_description");

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
					System.out.println("ATS-100 :: " + key);
					titleMatchCount++;
				}
			}

			// 6 Scoring
			if (titleMatchCount >= minFull) {
				ats100_points = genATSParamData.getMax_points();
			} else if (titleMatchCount >= minPartial && titleMatchCount < minFull) {
				ats100_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (titleMatchCount < minPartial) {
				ats100_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats100_points == max) {
				paramType = "positive";
			} else if (ats100_points == partial) {
				paramType = "partial";
			} else if (ats100_points == 0) {
				paramType = "negative";
			}

			calAtsGen100.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen100.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen100.setCategory(genATSParamData.getCategory());
			calAtsGen100.setDescription(genATSParamData.getDescription());
			calAtsGen100.setMax_points(genATSParamData.getMax_points());
			calAtsGen100.setParameter(genATSParamData.getParameter());
			calAtsGen100.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen100.setTotal_points(genATSParamData.getTotal_points());

			calAts100.setAtsGeneralId(atsGeneralId);
			calAts100.setAtsGeneralParamDto(calAtsGen100);
			calAts100.setAtsParamData(ats100ParamData);
			calAts100.setAtsParamId(atsParamId);
			calAts100.setAtsParamType(paramType);
			calAts100.setAtsScore(ats100_points);

			return calAts100;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts100;
		}
	}

	public AtsListDto calculateATS101(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts101 = new AtsListDto();
		AtsGenParamDto calAtsGen101 = new AtsGenParamDto();
		long ats101_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats101ParamData = getGeneralDesignATSParam(atsGeneralId, atsParamId);

			if (ats101ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats101ParamData.get("logic_description");

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
					System.out.println("ATS-101 :: " + regex);
					dateMatchCount++;
				}
			}

			// 6 Scoring (binary)
			if (dateMatchCount >= minMatches) {
				ats101_points = genATSParamData.getMax_points();
			} else if (dateMatchCount < minMatches) {
				ats101_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats101_points == max) {
				paramType = "positive";
			} else if (ats101_points == partial) {
				paramType = "partial";
			} else if (ats101_points == 0) {
				paramType = "negative";
			}

			calAtsGen101.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen101.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen101.setCategory(genATSParamData.getCategory());
			calAtsGen101.setDescription(genATSParamData.getDescription());
			calAtsGen101.setMax_points(genATSParamData.getMax_points());
			calAtsGen101.setParameter(genATSParamData.getParameter());
			calAtsGen101.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen101.setTotal_points(genATSParamData.getTotal_points());

			calAts101.setAtsGeneralId(atsGeneralId);
			calAts101.setAtsGeneralParamDto(calAtsGen101);
			calAts101.setAtsParamData(ats101ParamData);
			calAts101.setAtsParamId(atsParamId);
			calAts101.setAtsParamType(paramType);
			calAts101.setAtsScore(ats101_points);

			return calAts101;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts101;
		}
	}

	public AtsListDto calculateATS111(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts111 = new AtsListDto();
		AtsGenParamDto calAtsGen111 = new AtsGenParamDto();
		long ats111_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats111ParamData = getGeneralDesignATSParam(atsGeneralId, atsParamId);

			if (ats111ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats111ParamData.get("logic_description");

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
						System.out.println("ATS-111 :: " + key);
						impactMatchCount++;
					}
				}
			}

			// 6 Additional numeric signal (very important)

			// Detect numbers like: 30%, 10k, 1M, 500ms, etc.
			if (resumeText.matches("(?s).*\\b\\d+(%|k|m|ms|s|x)?\\b.*")) {
				impactMatchCount++;
			}

			// 7 Scoring
			if (impactMatchCount >= minFull) {
				ats111_points = genATSParamData.getMax_points();
			} else if (impactMatchCount >= minPartial && impactMatchCount < minFull) {
				ats111_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (impactMatchCount < minPartial) {
				ats111_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats111_points == max) {
				paramType = "positive";
			} else if (ats111_points == partial) {
				paramType = "partial";
			} else if (ats111_points == 0) {
				paramType = "negative";
			}

			calAtsGen111.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen111.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen111.setCategory(genATSParamData.getCategory());
			calAtsGen111.setDescription(genATSParamData.getDescription());
			calAtsGen111.setMax_points(genATSParamData.getMax_points());
			calAtsGen111.setParameter(genATSParamData.getParameter());
			calAtsGen111.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen111.setTotal_points(genATSParamData.getTotal_points());

			calAts111.setAtsGeneralId(atsGeneralId);
			calAts111.setAtsGeneralParamDto(calAtsGen111);
			calAts111.setAtsParamData(ats111ParamData);
			calAts111.setAtsParamId(atsParamId);
			calAts111.setAtsParamType(paramType);
			calAts111.setAtsScore(ats111_points);

			return calAts111;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts111;
		}
	}

	public AtsListDto calculateATS103(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts103 = new AtsListDto();
		AtsGenParamDto calAtsGen103 = new AtsGenParamDto();
		long ats103_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats103ParamData = getGeneralDesignATSParam(atsGeneralId, atsParamId);

			if (ats103ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats103ParamData.get("logic_description");

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
						System.out.println("ATS-103 :: " + key);
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
				ats103_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartial && matchedGroups < minFull) {
				ats103_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartial) {
				ats103_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats103_points == max) {
				paramType = "positive";
			} else if (ats103_points == partial) {
				paramType = "partial";
			} else if (ats103_points == 0) {
				paramType = "negative";
			}

			calAtsGen103.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen103.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen103.setCategory(genATSParamData.getCategory());
			calAtsGen103.setDescription(genATSParamData.getDescription());
			calAtsGen103.setMax_points(genATSParamData.getMax_points());
			calAtsGen103.setParameter(genATSParamData.getParameter());
			calAtsGen103.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen103.setTotal_points(genATSParamData.getTotal_points());

			calAts103.setAtsGeneralId(atsGeneralId);
			calAts103.setAtsGeneralParamDto(calAtsGen103);
			calAts103.setAtsParamData(ats103ParamData);
			calAts103.setAtsParamId(atsParamId);
			calAts103.setAtsParamType(paramType);
			calAts103.setAtsScore(ats103_points);

			return calAts103;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts103;
		}
	}

	public AtsListDto calculateATS104(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts104 = new AtsListDto();
		AtsGenParamDto calAtsGen104 = new AtsGenParamDto();
		long ats104_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats104ParamData = getGeneralDesignATSParam(atsGeneralId, atsParamId);

			if (ats104ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats104ParamData.get("logic_description");

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
						System.out.println("ATS-104 :: " + key);
						matchedGroups++;
						break; // count group only once
					}
				}
			}

			// 6 Scoring
			if (matchedGroups >= minFull) {
				ats104_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartial && matchedGroups < minFull) {
				ats104_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartial) {
				ats104_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats104_points == max) {
				paramType = "positive";
			} else if (ats104_points == partial) {
				paramType = "partial";
			} else if (ats104_points == 0) {
				paramType = "negative";
			}

			calAtsGen104.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen104.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen104.setCategory(genATSParamData.getCategory());
			calAtsGen104.setDescription(genATSParamData.getDescription());
			calAtsGen104.setMax_points(genATSParamData.getMax_points());
			calAtsGen104.setParameter(genATSParamData.getParameter());
			calAtsGen104.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen104.setTotal_points(genATSParamData.getTotal_points());

			calAts104.setAtsGeneralId(atsGeneralId);
			calAts104.setAtsGeneralParamDto(calAtsGen104);
			calAts104.setAtsParamData(ats104ParamData);
			calAts104.setAtsParamId(atsParamId);
			calAts104.setAtsParamType(paramType);
			calAts104.setAtsScore(ats104_points);

			return calAts104;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts104;
		}
	}

	public AtsListDto calculateATS105(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts105 = new AtsListDto();
		AtsGenParamDto calAtsGen105 = new AtsGenParamDto();
		long ats105_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats105ParamData = getGeneralDesignATSParam(atsGeneralId, atsParamId);

			if (ats105ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic description
			Map<String, Object> logicDescription = (Map<String, Object>) ats105ParamData.get("logic_description");

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
						System.out.println("ATS-105 :: " + key);
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
				ats105_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartialGroups && matchedGroups < minFullGroups) {
				ats105_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartialGroups) {
				ats105_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats105_points == max) {
				paramType = "positive";
			} else if (ats105_points == partial) {
				paramType = "partial";
			} else if (ats105_points == 0) {
				paramType = "negative";
			}

			calAtsGen105.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen105.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen105.setCategory(genATSParamData.getCategory());
			calAtsGen105.setDescription(genATSParamData.getDescription());
			calAtsGen105.setMax_points(genATSParamData.getMax_points());
			calAtsGen105.setParameter(genATSParamData.getParameter());
			calAtsGen105.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen105.setTotal_points(genATSParamData.getTotal_points());

			calAts105.setAtsGeneralId(atsGeneralId);
			calAts105.setAtsGeneralParamDto(calAtsGen105);
			calAts105.setAtsParamData(ats105ParamData);
			calAts105.setAtsParamId(atsParamId);
			calAts105.setAtsParamType(paramType);
			calAts105.setAtsScore(ats105_points);

			return calAts105;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts105;
		}
	}

	public AtsListDto calculateATS106(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts106 = new AtsListDto();
		AtsGenParamDto calAtsGen106 = new AtsGenParamDto();
		long ats106_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats106ParamData = getGeneralDesignATSParam(atsGeneralId, atsParamId);

			if (ats106ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic description
			Map<String, Object> logicDescription = (Map<String, Object>) ats106ParamData.get("logic_description");

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
						System.out.println("ATS-106 :: " + key);
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
				ats106_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartialGroups && matchedGroups < minFullGroups) {
				ats106_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartialGroups) {
				ats106_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats106_points == max) {
				paramType = "positive";
			} else if (ats106_points == partial) {
				paramType = "partial";
			} else if (ats106_points == 0) {
				paramType = "negative";
			}

			calAtsGen106.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen106.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen106.setCategory(genATSParamData.getCategory());
			calAtsGen106.setDescription(genATSParamData.getDescription());
			calAtsGen106.setMax_points(genATSParamData.getMax_points());
			calAtsGen106.setParameter(genATSParamData.getParameter());
			calAtsGen106.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen106.setTotal_points(genATSParamData.getTotal_points());

			calAts106.setAtsGeneralId(atsGeneralId);
			calAts106.setAtsGeneralParamDto(calAtsGen106);
			calAts106.setAtsParamData(ats106ParamData);
			calAts106.setAtsParamId(atsParamId);
			calAts106.setAtsParamType(paramType);
			calAts106.setAtsScore(ats106_points);

			return calAts106;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts106;
		}
	}

	public AtsListDto calculateATS107(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts107 = new AtsListDto();
		AtsGenParamDto calAtsGen107 = new AtsGenParamDto();
		long ats107_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats107ParamData = getGeneralDesignATSParam(atsGeneralId, atsParamId);

			if (ats107ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats107ParamData.get("logic_description");

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
						System.out.println("ATS-107 :: " + keyword);
						break;
					}
				}

				if (groupMatched) {
					matchedGroups++;
				}
			}

			// 6 Scoring
			if (matchedGroups >= minFullGroups) {
				ats107_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartialGroups && matchedGroups < minFullGroups) {
				ats107_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartialGroups) {
				ats107_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats107_points == max) {
				paramType = "positive";
			} else if (ats107_points == partial) {
				paramType = "partial";
			} else if (ats107_points == 0) {
				paramType = "negative";
			}

			calAtsGen107.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen107.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen107.setCategory(genATSParamData.getCategory());
			calAtsGen107.setDescription(genATSParamData.getDescription());
			calAtsGen107.setMax_points(genATSParamData.getMax_points());
			calAtsGen107.setParameter(genATSParamData.getParameter());
			calAtsGen107.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen107.setTotal_points(genATSParamData.getTotal_points());

			calAts107.setAtsGeneralId(atsGeneralId);
			calAts107.setAtsGeneralParamDto(calAtsGen107);
			calAts107.setAtsParamData(ats107ParamData);
			calAts107.setAtsParamId(atsParamId);
			calAts107.setAtsParamType(paramType);
			calAts107.setAtsScore(ats107_points);

			return calAts107;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts107;
		}
	}

	public AtsListDto calculateATS108(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts108 = new AtsListDto();
		AtsGenParamDto calAtsGen108 = new AtsGenParamDto();
		long ats108_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats108ParamData = getGeneralDesignATSParam(atsGeneralId, atsParamId);

			if (ats108ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Fetch JSON configuration
			Map<String, Object> logicDesc = (Map<String, Object>) ats108ParamData.get("logic_description");
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
						System.out.println("ATS-108 :: " + errorPattern.toLowerCase());
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
				ats108_points = genATSParamData.getMax_points();
			} else if (totalErrors <= partialAllowed) {
				ats108_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (totalErrors >= zeroAbove) {
				ats108_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats108_points == max) {
				paramType = "positive";
			} else if (ats108_points == partial) {
				paramType = "partial";
			} else if (ats108_points == 0) {
				paramType = "negative";
			}

			calAtsGen108.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen108.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen108.setCategory(genATSParamData.getCategory());
			calAtsGen108.setDescription(genATSParamData.getDescription());
			calAtsGen108.setMax_points(genATSParamData.getMax_points());
			calAtsGen108.setParameter(genATSParamData.getParameter());
			calAtsGen108.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen108.setTotal_points(genATSParamData.getTotal_points());

			calAts108.setAtsGeneralId(atsGeneralId);
			calAts108.setAtsGeneralParamDto(calAtsGen108);
			calAts108.setAtsParamData(ats108ParamData);
			calAts108.setAtsParamId(atsParamId);
			calAts108.setAtsParamType(paramType);
			calAts108.setAtsScore(ats108_points);

			return calAts108;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts108;
		}
	}

	public AtsListDto calculateATS109(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts109 = new AtsListDto();
		AtsGenParamDto calAtsGen109 = new AtsGenParamDto();
		long ats109_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats109ParamData = getGeneralDesignATSParam(atsGeneralId, atsParamId);

			if (ats109ParamData == null) {
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
			Map<String, Object> logicDesc = (Map<String, Object>) ats109ParamData.get("logic_description");

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
						System.out.println("ATS-109 :: " + indicator.toLowerCase());
						totalViolations++;
					}
				}
			}

			// 6 Scoring logic
			if (totalViolations <= fullScoreLimit) {
				ats109_points = genATSParamData.getMax_points();
			} else if (totalViolations <= partialScoreLimit) {
				ats109_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (totalViolations >= zeroScoreLimit) {
				ats109_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats109_points == max) {
				paramType = "positive";
			} else if (ats109_points == partial) {
				paramType = "partial";
			} else if (ats109_points == 0) {
				paramType = "negative";
			}

			calAtsGen109.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen109.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen109.setCategory(genATSParamData.getCategory());
			calAtsGen109.setDescription(genATSParamData.getDescription());
			calAtsGen109.setMax_points(genATSParamData.getMax_points());
			calAtsGen109.setParameter(genATSParamData.getParameter());
			calAtsGen109.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen109.setTotal_points(genATSParamData.getTotal_points());

			calAts109.setAtsGeneralId(atsGeneralId);
			calAts109.setAtsGeneralParamDto(calAtsGen109);
			calAts109.setAtsParamData(ats109ParamData);
			calAts109.setAtsParamId(atsParamId);
			calAts109.setAtsParamType(paramType);
			calAts109.setAtsScore(ats109_points);

			return calAts109;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts109;
		}
	}

	public AtsListDto calculateATS110(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts110 = new AtsListDto();
		AtsGenParamDto calAtsGen110 = new AtsGenParamDto();

		long ats110_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats110ParamData = getGeneralDesignATSParam(atsGeneralId, atsParamId);

			if (ats110ParamData == null) {
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
			Map<String, Object> logicDesc = (Map<String, Object>) ats110ParamData.get("logic_description");

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
								System.out.println("ATS-110 :: " + indicator.toLowerCase());
								totalViolations++;
								break;
							}
						}
					}
				}
			}

			// 6 Scoring
			if (totalViolations <= fullScoreLimit) {
				ats110_points = genATSParamData.getMax_points();
			} else if (totalViolations <= partialScoreLimit) {
				ats110_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (totalViolations >= zeroScoreLimit
					|| (totalViolations > partialScoreLimit && totalViolations < zeroScoreLimit)) {
				ats110_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats110_points == max) {
				paramType = "positive";
			} else if (ats110_points == partial) {
				paramType = "partial";
			} else if (ats110_points == 0) {
				paramType = "negative";
			}

			calAtsGen110.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen110.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen110.setCategory(genATSParamData.getCategory());
			calAtsGen110.setDescription(genATSParamData.getDescription());
			calAtsGen110.setMax_points(genATSParamData.getMax_points());
			calAtsGen110.setParameter(genATSParamData.getParameter());
			calAtsGen110.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen110.setTotal_points(genATSParamData.getTotal_points());

			calAts110.setAtsGeneralId(atsGeneralId);
			calAts110.setAtsGeneralParamDto(calAtsGen110);
			calAts110.setAtsParamData(ats110ParamData);
			calAts110.setAtsParamId(atsParamId);
			calAts110.setAtsParamType(paramType);
			calAts110.setAtsScore(ats110_points);

			return calAts110;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts110;
		}
	}

}

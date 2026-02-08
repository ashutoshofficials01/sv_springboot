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
public class GovernmentAtsUtil {
	

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

	private List<Map<String, Object>> generalGovernmentATSParameterList;

	private Map<String, Map<String, Object>> generalGovernmentATSParameterMap;

//	@PostConstruct
//	ITATSUtil(AtsGenParamDto atsGenParamDto) {
//		this.atsGenParamDto = atsGenParamDto;
//	}

	@PostConstruct
	public void loadGeneralATSParameters() {
		try {

			Resource resource = resourceLoader.getResource("classpath:Government_ATSParameter.json");

			// Read JSON as Map
			Map<String, Object> jsonData = objectMapper.readValue(resource.getInputStream(), Map.class);

			// Extract array
			generalGovernmentATSParameterList = (List<Map<String, Object>>) jsonData.get("Government_ATSParameter");

			// Create fast-lookup map
			generalGovernmentATSParameterMap = new HashMap<>();

			for (Map<String, Object> param : generalGovernmentATSParameterList) {
				Number atsGeneralIdNum = (Number) param.get("atsGeneralId");
				long atsGeneralId = atsGeneralIdNum.longValue();
				String atsParamId = (String) param.get("atsParamId");

				String key = atsGeneralId + "_" + atsParamId;
				generalGovernmentATSParameterMap.put(key, param);
			}

			System.out.println("Loaded ATS Params Count: " + generalGovernmentATSParameterMap.size());

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load Government_ATSParameter.json", e);
		}
	}

	public List<Map<String, Object>> getAllGeneralATSParams() {
//		System.out.println("getAllGeneralATSParams() --> generalGovernmentATSParameterList :: " + generalGovernmentATSParameterList);
		return generalGovernmentATSParameterList;
	}

	public Map<String, Object> getGeneralGovernmentATSParam(long atsGeneralId, String atsParamId) {
//		System.out.println("getGeneralGovernmentATSParam(long " + atsGeneralId + ", String " + atsParamId
//				+ ") --> generalGovernmentATSParameterMap :: " + generalGovernmentATSParameterMap.get(atsGeneralId + "_" + atsParamId));
		return generalGovernmentATSParameterMap.get(atsGeneralId + "_" + atsParamId);
	}
	
	public AtsListDto calculateATS181(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts181 = new AtsListDto();
		AtsGenParamDto calAtsGen181 = new AtsGenParamDto();
		long ats181_points = 0;
		try {

			// 1️ Fetch JSON parameter config
			Map<String, Object> ats181ParamData = getGeneralGovernmentATSParam(atsGeneralId, atsParamId);
			if (ats181ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config (points / penalty)
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);
			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats181ParamData.get("logic_description");

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
					System.out.println("ATS-181 :: " + key);
					matchCount++;
				}
			}

//	     6️ Scoring logic
			if (matchCount >= minFull) {
				ats181_points = genATSParamData.getMax_points();
			} else if ((matchCount >= minPartial) && (matchCount < minFull)) {
				ats181_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if ((matchCount <= 0) && (matchCount < minPartial)) {
				ats181_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats181_points == max) {
				paramType = "positive";
			} else if (ats181_points == partial) {
				paramType = "partial";
			} else if (ats181_points == 0) {
				paramType = "negative";
			}

			calAtsGen181.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen181.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen181.setCategory(genATSParamData.getCategory());
			calAtsGen181.setDescription(genATSParamData.getDescription());
			calAtsGen181.setMax_points(genATSParamData.getMax_points());
			calAtsGen181.setParameter(genATSParamData.getParameter());
			calAtsGen181.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen181.setTotal_points(genATSParamData.getTotal_points());

			calAts181.setAtsGeneralId(atsGeneralId);
			calAts181.setAtsGeneralParamDto(calAtsGen181);
			calAts181.setAtsParamData(ats181ParamData);
			calAts181.setAtsParamId(atsParamId);
			calAts181.setAtsParamType(paramType);
			calAts181.setAtsScore(ats181_points);

			return calAts181;

		} catch (Exception e) {
			e.printStackTrace();
			return calAts181;
		}
	}

	public AtsListDto calculateATS182(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts182 = new AtsListDto();
		AtsGenParamDto calAtsGen182 = new AtsGenParamDto();
		long ats182_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats182ParamData = getGeneralGovernmentATSParam(atsGeneralId, atsParamId);

			if (ats182ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats182ParamData.get("logic_description");

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
						System.out.println("ATS-182 :: " + key);
						matchedCategories++;
					}
				}
			}

			// 6️ Scoring logic
			if (matchedCategories >= minFull) {
				ats182_points = genATSParamData.getMax_points();
			} else if (matchedCategories >= minPartial && matchedCategories < minFull) {
				ats182_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else {
				ats182_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats182_points == max) {
				paramType = "positive";
			} else if (ats182_points == partial) {
				paramType = "partial";
			} else if (ats182_points == 0) {
				paramType = "negative";
			}

			calAtsGen182.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen182.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen182.setCategory(genATSParamData.getCategory());
			calAtsGen182.setDescription(genATSParamData.getDescription());
			calAtsGen182.setMax_points(genATSParamData.getMax_points());
			calAtsGen182.setParameter(genATSParamData.getParameter());
			calAtsGen182.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen182.setTotal_points(genATSParamData.getTotal_points());

			calAts182.setAtsGeneralId(atsGeneralId);
			calAts182.setAtsGeneralParamDto(calAtsGen182);
			calAts182.setAtsParamData(ats182ParamData);
			calAts182.setAtsParamId(atsParamId);
			calAts182.setAtsParamType(paramType);
			calAts182.setAtsScore(ats182_points);

			return calAts182;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts182;
		}
	}

	public AtsListDto calculateATS183(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts183 = new AtsListDto();
		AtsGenParamDto calAtsGen183 = new AtsGenParamDto();
		long ats183_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats183ParamData = getGeneralGovernmentATSParam(atsGeneralId, atsParamId);

			if (ats183ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats183ParamData.get("logic_description");

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
						System.out.println("ATS-183 :: " + alternate);
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
				ats183_points = 0;
			} else if (matchedVariationGroups <= minFull && matchedVariationGroups > minPartial) {
				ats183_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedVariationGroups <= minPartial) {
				ats183_points = genATSParamData.getMax_points();
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats183_points == max) {
				paramType = "positive";
			} else if (ats183_points == partial) {
				paramType = "partial";
			} else if (ats183_points == 0) {
				paramType = "negative";
			}

			calAtsGen183.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen183.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen183.setCategory(genATSParamData.getCategory());
			calAtsGen183.setDescription(genATSParamData.getDescription());
			calAtsGen183.setMax_points(genATSParamData.getMax_points());
			calAtsGen183.setParameter(genATSParamData.getParameter());
			calAtsGen183.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen183.setTotal_points(genATSParamData.getTotal_points());

			calAts183.setAtsGeneralId(atsGeneralId);
			calAts183.setAtsGeneralParamDto(calAtsGen183);
			calAts183.setAtsParamData(ats183ParamData);
			calAts183.setAtsParamId(atsParamId);
			calAts183.setAtsParamType(paramType);
			calAts183.setAtsScore(ats183_points);

			return calAts183;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts183;
		}
	}

	public AtsListDto calculateATS184(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts184 = new AtsListDto();
		AtsGenParamDto calAtsGen184 = new AtsGenParamDto();
		long ats184_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats184ParamData = getGeneralGovernmentATSParam(atsGeneralId, atsParamId);

			if (ats184ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats184ParamData.get("logic_description");

			Map<String, List<String>> acceptedFormats = (Map<String, List<String>>) logicDescription
					.get("accepted_formats");

			List<String> fullScoreFormats = acceptedFormats.get("full_score");
			List<String> partialScoreFormats = acceptedFormats.get("partial_score");

			// 4️ Extract file extension
			if (fileName == null || !fileName.contains(".")) {
				return null;
			}

			String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

			System.out.println("ATS-184 :: " + extension);

			// 5️ Scoring logic
			if (fullScoreFormats.contains(extension)) {
				ats184_points = genATSParamData.getMax_points();
			} else if (partialScoreFormats.contains(extension)) {
				ats184_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else {
				ats184_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats184_points == max) {
				paramType = "positive";
			} else if (ats184_points == partial) {
				paramType = "partial";
			} else if (ats184_points == 0) {
				paramType = "negative";
			}

			calAtsGen184.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen184.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen184.setCategory(genATSParamData.getCategory());
			calAtsGen184.setDescription(genATSParamData.getDescription());
			calAtsGen184.setMax_points(genATSParamData.getMax_points());
			calAtsGen184.setParameter(genATSParamData.getParameter());
			calAtsGen184.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen184.setTotal_points(genATSParamData.getTotal_points());

			calAts184.setAtsGeneralId(atsGeneralId);
			calAts184.setAtsGeneralParamDto(calAtsGen184);
			calAts184.setAtsParamData(ats184ParamData);
			calAts184.setAtsParamId(atsParamId);
			calAts184.setAtsParamType(paramType);
			calAts184.setAtsScore(ats184_points);

			return calAts184;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts184;
		}
	}

	public AtsListDto calculateATS185(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts185 = new AtsListDto();
		AtsGenParamDto calAtsGen185 = new AtsGenParamDto();
		long ats185_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats185ParamData = getGeneralGovernmentATSParam(atsGeneralId, atsParamId);

			if (ats185ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats185ParamData.get("logic_description");

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
						System.out.println("ATS-185 :: " + indicator);
						layoutViolations++;
					}
				}
			}

			// 6 Scoring Logic
			if (layoutViolations <= maxFull) {
				ats185_points = genATSParamData.getMax_points();
			} else if (layoutViolations <= maxPartial) {
				ats185_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else {
				ats185_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats185_points == max) {
				paramType = "positive";
			} else if (ats185_points == partial) {
				paramType = "partial";
			} else if (ats185_points == 0) {
				paramType = "negative";
			}

			calAtsGen185.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen185.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen185.setCategory(genATSParamData.getCategory());
			calAtsGen185.setDescription(genATSParamData.getDescription());
			calAtsGen185.setMax_points(genATSParamData.getMax_points());
			calAtsGen185.setParameter(genATSParamData.getParameter());
			calAtsGen185.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen185.setTotal_points(genATSParamData.getTotal_points());

			calAts185.setAtsGeneralId(atsGeneralId);
			calAts185.setAtsGeneralParamDto(calAtsGen185);
			calAts185.setAtsParamData(ats185ParamData);
			calAts185.setAtsParamId(atsParamId);
			calAts185.setAtsParamType(paramType);
			calAts185.setAtsScore(ats185_points);

			return calAts185;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts185;
		}
	}

	public AtsListDto calculateATS186(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts186 = new AtsListDto();
		AtsGenParamDto calAtsGen186 = new AtsGenParamDto();
		long ats186_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats186ParamData = getGeneralGovernmentATSParam(atsGeneralId, atsParamId);

			if (ats186ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats186ParamData.get("logic_description");

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

			System.out.println("ATS-186 :: " + detectedFonts);

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
				calAtsNull.setAtsParamData(ats186ParamData);
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
				ats186_points = 0;
			} else if (hasStandard && hasNonStandard && partialScoreIfMixed) {
				ats186_points = fullScore - partialScore;
			} else if (hasStandard && !hasNonStandard && fullScoreIfStandard) {
				ats186_points = fullScore;
			} else {
				ats186_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats186_points == max) {
				paramType = "positive";
			} else if (ats186_points == partial) {
				paramType = "partial";
			} else if (ats186_points == 0) {
				paramType = "negative";
			}

			calAtsGen186.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen186.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen186.setCategory(genATSParamData.getCategory());
			calAtsGen186.setDescription(genATSParamData.getDescription());
			calAtsGen186.setMax_points(genATSParamData.getMax_points());
			calAtsGen186.setParameter(genATSParamData.getParameter());
			calAtsGen186.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen186.setTotal_points(genATSParamData.getTotal_points());

			calAts186.setAtsGeneralId(atsGeneralId);
			calAts186.setAtsGeneralParamDto(calAtsGen186);
			calAts186.setAtsParamData(ats186ParamData);
			calAts186.setAtsParamId(atsParamId);
			calAts186.setAtsParamType(paramType);
			calAts186.setAtsScore(ats186_points);

			return calAts186;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts186;
		}
	}

	public AtsListDto calculateATS187(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts187 = new AtsListDto();
		AtsGenParamDto calAtsGen187 = new AtsGenParamDto();
		long ats187_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats187ParamData = getGeneralGovernmentATSParam(atsGeneralId, atsParamId);

			if (ats187ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats187ParamData.get("logic_description");

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
					System.out.println("ATS-187 :: " + sectionKey);
					matchedSections.add(sectionKey);
				}
			}

			long sectionCount = ((Number) matchedSections.size()).longValue();

			// 6 Scoring logic
			if (sectionCount >= minFull) {
				ats187_points = genATSParamData.getMax_points();
			} else if (sectionCount >= minPartial && sectionCount < minFull) {
				ats187_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (sectionCount < minPartial) {
				ats187_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats187_points == max) {
				paramType = "positive";
			} else if (ats187_points == partial) {
				paramType = "partial";
			} else if (ats187_points == 0) {
				paramType = "negative";
			}

			calAtsGen187.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen187.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen187.setCategory(genATSParamData.getCategory());
			calAtsGen187.setDescription(genATSParamData.getDescription());
			calAtsGen187.setMax_points(genATSParamData.getMax_points());
			calAtsGen187.setParameter(genATSParamData.getParameter());
			calAtsGen187.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen187.setTotal_points(genATSParamData.getTotal_points());

			calAts187.setAtsGeneralId(atsGeneralId);
			calAts187.setAtsGeneralParamDto(calAtsGen187);
			calAts187.setAtsParamData(ats187ParamData);
			calAts187.setAtsParamId(atsParamId);
			calAts187.setAtsParamType(paramType);
			calAts187.setAtsScore(ats187_points);

			return calAts187;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts187;
		}
	}

	public AtsListDto calculateATS188(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts188 = new AtsListDto();
		AtsGenParamDto calAtsGen188 = new AtsGenParamDto();
		long ats188_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats188ParamData = getGeneralGovernmentATSParam(atsGeneralId, atsParamId);

			if (ats188ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats188ParamData.get("logic_description");

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
						System.out.println("ATS-188 :: " + key);
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
				ats188_points = genATSParamData.getMax_points();
			} else if (violations <= maxPartial) {
				ats188_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (violations > maxPartial) {
				ats188_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats188_points == max) {
				paramType = "positive";
			} else if (ats188_points == partial) {
				paramType = "partial";
			} else if (ats188_points == 0) {
				paramType = "negative";
			}

			calAtsGen188.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen188.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen188.setCategory(genATSParamData.getCategory());
			calAtsGen188.setDescription(genATSParamData.getDescription());
			calAtsGen188.setMax_points(genATSParamData.getMax_points());
			calAtsGen188.setParameter(genATSParamData.getParameter());
			calAtsGen188.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen188.setTotal_points(genATSParamData.getTotal_points());

			calAts188.setAtsGeneralId(atsGeneralId);
			calAts188.setAtsGeneralParamDto(calAtsGen188);
			calAts188.setAtsParamData(ats188ParamData);
			calAts188.setAtsParamId(atsParamId);
			calAts188.setAtsParamType(paramType);
			calAts188.setAtsScore(ats188_points);

			return calAts188;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts188;
		}
	}

	public AtsListDto calculateATS189(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts189 = new AtsListDto();
		AtsGenParamDto calAtsGen189 = new AtsGenParamDto();
		long ats189_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats189ParamData = getGeneralGovernmentATSParam(atsGeneralId, atsParamId);

			if (ats189ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3️ Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats189ParamData.get("logic_description");

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
						System.out.println("ATS-189 :: " + key);
						matchCount++;
					}
				}
			}

			// 6 Scoring
			if (matchCount >= minFull) {
				ats189_points = genATSParamData.getMax_points();
			} else if (matchCount >= minPartial && matchCount < minFull) {
				ats189_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchCount < minPartial) {
				ats189_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats189_points == max) {
				paramType = "positive";
			} else if (ats189_points == partial) {
				paramType = "partial";
			} else if (ats189_points == 0) {
				paramType = "negative";
			}

			calAtsGen189.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen189.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen189.setCategory(genATSParamData.getCategory());
			calAtsGen189.setDescription(genATSParamData.getDescription());
			calAtsGen189.setMax_points(genATSParamData.getMax_points());
			calAtsGen189.setParameter(genATSParamData.getParameter());
			calAtsGen189.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen189.setTotal_points(genATSParamData.getTotal_points());

			calAts189.setAtsGeneralId(atsGeneralId);
			calAts189.setAtsGeneralParamDto(calAtsGen189);
			calAts189.setAtsParamData(ats189ParamData);
			calAts189.setAtsParamId(atsParamId);
			calAts189.setAtsParamType(paramType);
			calAts189.setAtsScore(ats189_points);

			return calAts189;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts189;
		}
	}

	public AtsListDto calculateATS190(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts190 = new AtsListDto();
		AtsGenParamDto calAtsGen190 = new AtsGenParamDto();
		long ats190_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats190ParamData = getGeneralGovernmentATSParam(atsGeneralId, atsParamId);

			if (ats190ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats190ParamData.get("logic_description");

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
					System.out.println("ATS-190 :: " + key);
					titleMatchCount++;
				}
			}

			// 6 Scoring
			if (titleMatchCount >= minFull) {
				ats190_points = genATSParamData.getMax_points();
			} else if (titleMatchCount >= minPartial && titleMatchCount < minFull) {
				ats190_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (titleMatchCount < minPartial) {
				ats190_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats190_points == max) {
				paramType = "positive";
			} else if (ats190_points == partial) {
				paramType = "partial";
			} else if (ats190_points == 0) {
				paramType = "negative";
			}

			calAtsGen190.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen190.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen190.setCategory(genATSParamData.getCategory());
			calAtsGen190.setDescription(genATSParamData.getDescription());
			calAtsGen190.setMax_points(genATSParamData.getMax_points());
			calAtsGen190.setParameter(genATSParamData.getParameter());
			calAtsGen190.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen190.setTotal_points(genATSParamData.getTotal_points());

			calAts190.setAtsGeneralId(atsGeneralId);
			calAts190.setAtsGeneralParamDto(calAtsGen190);
			calAts190.setAtsParamData(ats190ParamData);
			calAts190.setAtsParamId(atsParamId);
			calAts190.setAtsParamType(paramType);
			calAts190.setAtsScore(ats190_points);

			return calAts190;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts190;
		}
	}

	public AtsListDto calculateATS191(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts191 = new AtsListDto();
		AtsGenParamDto calAtsGen191 = new AtsGenParamDto();
		long ats191_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats191ParamData = getGeneralGovernmentATSParam(atsGeneralId, atsParamId);

			if (ats191ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats191ParamData.get("logic_description");

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
					System.out.println("ATS-191 :: " + regex);
					dateMatchCount++;
				}
			}

			// 6 Scoring (binary)
			if (dateMatchCount >= minMatches) {
				ats191_points = genATSParamData.getMax_points();
			} else if (dateMatchCount < minMatches) {
				ats191_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats191_points == max) {
				paramType = "positive";
			} else if (ats191_points == partial) {
				paramType = "partial";
			} else if (ats191_points == 0) {
				paramType = "negative";
			}

			calAtsGen191.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen191.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen191.setCategory(genATSParamData.getCategory());
			calAtsGen191.setDescription(genATSParamData.getDescription());
			calAtsGen191.setMax_points(genATSParamData.getMax_points());
			calAtsGen191.setParameter(genATSParamData.getParameter());
			calAtsGen191.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen191.setTotal_points(genATSParamData.getTotal_points());

			calAts191.setAtsGeneralId(atsGeneralId);
			calAts191.setAtsGeneralParamDto(calAtsGen191);
			calAts191.setAtsParamData(ats191ParamData);
			calAts191.setAtsParamId(atsParamId);
			calAts191.setAtsParamType(paramType);
			calAts191.setAtsScore(ats191_points);

			return calAts191;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts191;
		}
	}

	public AtsListDto calculateATS192(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts192 = new AtsListDto();
		AtsGenParamDto calAtsGen192 = new AtsGenParamDto();
		long ats192_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats192ParamData = getGeneralGovernmentATSParam(atsGeneralId, atsParamId);

			if (ats192ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats192ParamData.get("logic_description");

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
						System.out.println("ATS-192 :: " + key);
						impactMatchCount++;
					}
				}
			}

			// 6 Additional numeric signal (very important)

			// Detect numbers like: 30%, 19k, 1M, 500ms, etc.
			if (resumeText.matches("(?s).*\\b\\d+(%|k|m|ms|s|x)?\\b.*")) {
				impactMatchCount++;
			}

			// 7 Scoring
			if (impactMatchCount >= minFull) {
				ats192_points = genATSParamData.getMax_points();
			} else if (impactMatchCount >= minPartial && impactMatchCount < minFull) {
				ats192_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (impactMatchCount < minPartial) {
				ats192_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats192_points == max) {
				paramType = "positive";
			} else if (ats192_points == partial) {
				paramType = "partial";
			} else if (ats192_points == 0) {
				paramType = "negative";
			}

			calAtsGen192.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen192.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen192.setCategory(genATSParamData.getCategory());
			calAtsGen192.setDescription(genATSParamData.getDescription());
			calAtsGen192.setMax_points(genATSParamData.getMax_points());
			calAtsGen192.setParameter(genATSParamData.getParameter());
			calAtsGen192.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen192.setTotal_points(genATSParamData.getTotal_points());

			calAts192.setAtsGeneralId(atsGeneralId);
			calAts192.setAtsGeneralParamDto(calAtsGen192);
			calAts192.setAtsParamData(ats192ParamData);
			calAts192.setAtsParamId(atsParamId);
			calAts192.setAtsParamType(paramType);
			calAts192.setAtsScore(ats192_points);

			return calAts192;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts192;
		}
	}

	public AtsListDto calculateATS193(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts193 = new AtsListDto();
		AtsGenParamDto calAtsGen193 = new AtsGenParamDto();
		long ats193_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats193ParamData = getGeneralGovernmentATSParam(atsGeneralId, atsParamId);

			if (ats193ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats193ParamData.get("logic_description");

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
						System.out.println("ATS-193 :: " + key);
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
				ats193_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartial && matchedGroups < minFull) {
				ats193_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartial) {
				ats193_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats193_points == max) {
				paramType = "positive";
			} else if (ats193_points == partial) {
				paramType = "partial";
			} else if (ats193_points == 0) {
				paramType = "negative";
			}

			calAtsGen193.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen193.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen193.setCategory(genATSParamData.getCategory());
			calAtsGen193.setDescription(genATSParamData.getDescription());
			calAtsGen193.setMax_points(genATSParamData.getMax_points());
			calAtsGen193.setParameter(genATSParamData.getParameter());
			calAtsGen193.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen193.setTotal_points(genATSParamData.getTotal_points());

			calAts193.setAtsGeneralId(atsGeneralId);
			calAts193.setAtsGeneralParamDto(calAtsGen193);
			calAts193.setAtsParamData(ats193ParamData);
			calAts193.setAtsParamId(atsParamId);
			calAts193.setAtsParamType(paramType);
			calAts193.setAtsScore(ats193_points);

			return calAts193;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts193;
		}
	}

	public AtsListDto calculateATS194(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts194 = new AtsListDto();
		AtsGenParamDto calAtsGen194 = new AtsGenParamDto();
		long ats194_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats194ParamData = getGeneralGovernmentATSParam(atsGeneralId, atsParamId);

			if (ats194ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats194ParamData.get("logic_description");

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
						System.out.println("ATS-194 :: " + key);
						matchedGroups++;
						break; // count group only once
					}
				}
			}

			// 6 Scoring
			if (matchedGroups >= minFull) {
				ats194_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartial && matchedGroups < minFull) {
				ats194_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartial) {
				ats194_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats194_points == max) {
				paramType = "positive";
			} else if (ats194_points == partial) {
				paramType = "partial";
			} else if (ats194_points == 0) {
				paramType = "negative";
			}

			calAtsGen194.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen194.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen194.setCategory(genATSParamData.getCategory());
			calAtsGen194.setDescription(genATSParamData.getDescription());
			calAtsGen194.setMax_points(genATSParamData.getMax_points());
			calAtsGen194.setParameter(genATSParamData.getParameter());
			calAtsGen194.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen194.setTotal_points(genATSParamData.getTotal_points());

			calAts194.setAtsGeneralId(atsGeneralId);
			calAts194.setAtsGeneralParamDto(calAtsGen194);
			calAts194.setAtsParamData(ats194ParamData);
			calAts194.setAtsParamId(atsParamId);
			calAts194.setAtsParamType(paramType);
			calAts194.setAtsScore(ats194_points);

			return calAts194;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts194;
		}
	}

	public AtsListDto calculateATS195(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts195 = new AtsListDto();
		AtsGenParamDto calAtsGen195 = new AtsGenParamDto();
		long ats195_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats195ParamData = getGeneralGovernmentATSParam(atsGeneralId, atsParamId);

			if (ats195ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic description
			Map<String, Object> logicDescription = (Map<String, Object>) ats195ParamData.get("logic_description");

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
						System.out.println("ATS-195 :: " + key);
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
				ats195_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartialGroups && matchedGroups < minFullGroups) {
				ats195_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartialGroups) {
				ats195_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats195_points == max) {
				paramType = "positive";
			} else if (ats195_points == partial) {
				paramType = "partial";
			} else if (ats195_points == 0) {
				paramType = "negative";
			}

			calAtsGen195.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen195.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen195.setCategory(genATSParamData.getCategory());
			calAtsGen195.setDescription(genATSParamData.getDescription());
			calAtsGen195.setMax_points(genATSParamData.getMax_points());
			calAtsGen195.setParameter(genATSParamData.getParameter());
			calAtsGen195.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen195.setTotal_points(genATSParamData.getTotal_points());

			calAts195.setAtsGeneralId(atsGeneralId);
			calAts195.setAtsGeneralParamDto(calAtsGen195);
			calAts195.setAtsParamData(ats195ParamData);
			calAts195.setAtsParamId(atsParamId);
			calAts195.setAtsParamType(paramType);
			calAts195.setAtsScore(ats195_points);

			return calAts195;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts195;
		}
	}

	public AtsListDto calculateATS196(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts196 = new AtsListDto();
		AtsGenParamDto calAtsGen196 = new AtsGenParamDto();
		long ats196_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats196ParamData = getGeneralGovernmentATSParam(atsGeneralId, atsParamId);

			if (ats196ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic description
			Map<String, Object> logicDescription = (Map<String, Object>) ats196ParamData.get("logic_description");

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
						System.out.println("ATS-196 :: " + key);
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
				ats196_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartialGroups && matchedGroups < minFullGroups) {
				ats196_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartialGroups) {
				ats196_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats196_points == max) {
				paramType = "positive";
			} else if (ats196_points == partial) {
				paramType = "partial";
			} else if (ats196_points == 0) {
				paramType = "negative";
			}

			calAtsGen196.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen196.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen196.setCategory(genATSParamData.getCategory());
			calAtsGen196.setDescription(genATSParamData.getDescription());
			calAtsGen196.setMax_points(genATSParamData.getMax_points());
			calAtsGen196.setParameter(genATSParamData.getParameter());
			calAtsGen196.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen196.setTotal_points(genATSParamData.getTotal_points());

			calAts196.setAtsGeneralId(atsGeneralId);
			calAts196.setAtsGeneralParamDto(calAtsGen196);
			calAts196.setAtsParamData(ats196ParamData);
			calAts196.setAtsParamId(atsParamId);
			calAts196.setAtsParamType(paramType);
			calAts196.setAtsScore(ats196_points);

			return calAts196;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts196;
		}
	}

	public AtsListDto calculateATS197(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts197 = new AtsListDto();
		AtsGenParamDto calAtsGen197 = new AtsGenParamDto();
		long ats197_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats197ParamData = getGeneralGovernmentATSParam(atsGeneralId, atsParamId);

			if (ats197ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Extract logic_description
			Map<String, Object> logicDescription = (Map<String, Object>) ats197ParamData.get("logic_description");

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
						System.out.println("ATS-197 :: " + keyword);
						break;
					}
				}

				if (groupMatched) {
					matchedGroups++;
				}
			}

			// 6 Scoring
			if (matchedGroups >= minFullGroups) {
				ats197_points = genATSParamData.getMax_points();
			} else if (matchedGroups >= minPartialGroups && matchedGroups < minFullGroups) {
				ats197_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (matchedGroups < minPartialGroups) {
				ats197_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats197_points == max) {
				paramType = "positive";
			} else if (ats197_points == partial) {
				paramType = "partial";
			} else if (ats197_points == 0) {
				paramType = "negative";
			}

			calAtsGen197.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen197.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen197.setCategory(genATSParamData.getCategory());
			calAtsGen197.setDescription(genATSParamData.getDescription());
			calAtsGen197.setMax_points(genATSParamData.getMax_points());
			calAtsGen197.setParameter(genATSParamData.getParameter());
			calAtsGen197.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen197.setTotal_points(genATSParamData.getTotal_points());

			calAts197.setAtsGeneralId(atsGeneralId);
			calAts197.setAtsGeneralParamDto(calAtsGen197);
			calAts197.setAtsParamData(ats197ParamData);
			calAts197.setAtsParamId(atsParamId);
			calAts197.setAtsParamType(paramType);
			calAts197.setAtsScore(ats197_points);

			return calAts197;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts197;
		}
	}

	public AtsListDto calculateATS198(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts198 = new AtsListDto();
		AtsGenParamDto calAtsGen198 = new AtsGenParamDto();
		long ats198_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats198ParamData = getGeneralGovernmentATSParam(atsGeneralId, atsParamId);

			if (ats198ParamData == null) {
				return null;
			}

			// 2️ Fetch DB config
			ATS_General_Param_Entity genATSParamData = atsGeneralParamRepo.findByAtsGeneralId(atsGeneralId);

			if (genATSParamData == null) {
				return null;
			}

			// 3 Fetch JSON configuration
			Map<String, Object> logicDesc = (Map<String, Object>) ats198ParamData.get("logic_description");
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
						System.out.println("ATS-198 :: " + errorPattern.toLowerCase());
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
				ats198_points = genATSParamData.getMax_points();
			} else if (totalErrors <= partialAllowed) {
				ats198_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (totalErrors >= zeroAbove) {
				ats198_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats198_points == max) {
				paramType = "positive";
			} else if (ats198_points == partial) {
				paramType = "partial";
			} else if (ats198_points == 0) {
				paramType = "negative";
			}

			calAtsGen198.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen198.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen198.setCategory(genATSParamData.getCategory());
			calAtsGen198.setDescription(genATSParamData.getDescription());
			calAtsGen198.setMax_points(genATSParamData.getMax_points());
			calAtsGen198.setParameter(genATSParamData.getParameter());
			calAtsGen198.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen198.setTotal_points(genATSParamData.getTotal_points());

			calAts198.setAtsGeneralId(atsGeneralId);
			calAts198.setAtsGeneralParamDto(calAtsGen198);
			calAts198.setAtsParamData(ats198ParamData);
			calAts198.setAtsParamId(atsParamId);
			calAts198.setAtsParamType(paramType);
			calAts198.setAtsScore(ats198_points);

			return calAts198;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts198;
		}
	}

	public AtsListDto calculateATS199(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts199 = new AtsListDto();
		AtsGenParamDto calAtsGen199 = new AtsGenParamDto();
		long ats199_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats199ParamData = getGeneralGovernmentATSParam(atsGeneralId, atsParamId);

			if (ats199ParamData == null) {
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
			Map<String, Object> logicDesc = (Map<String, Object>) ats199ParamData.get("logic_description");

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
						System.out.println("ATS-199 :: " + indicator.toLowerCase());
						totalViolations++;
					}
				}
			}

			// 6 Scoring logic
			if (totalViolations <= fullScoreLimit) {
				ats199_points = genATSParamData.getMax_points();
			} else if (totalViolations <= partialScoreLimit) {
				ats199_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (totalViolations >= zeroScoreLimit) {
				ats199_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats199_points == max) {
				paramType = "positive";
			} else if (ats199_points == partial) {
				paramType = "partial";
			} else if (ats199_points == 0) {
				paramType = "negative";
			}

			calAtsGen199.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen199.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen199.setCategory(genATSParamData.getCategory());
			calAtsGen199.setDescription(genATSParamData.getDescription());
			calAtsGen199.setMax_points(genATSParamData.getMax_points());
			calAtsGen199.setParameter(genATSParamData.getParameter());
			calAtsGen199.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen199.setTotal_points(genATSParamData.getTotal_points());

			calAts199.setAtsGeneralId(atsGeneralId);
			calAts199.setAtsGeneralParamDto(calAtsGen199);
			calAts199.setAtsParamData(ats199ParamData);
			calAts199.setAtsParamId(atsParamId);
			calAts199.setAtsParamType(paramType);
			calAts199.setAtsScore(ats199_points);

			return calAts199;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts199;
		}
	}

	public AtsListDto calculateATS200(String atsParamId, long atsGeneralId, String fileName, MultipartFile file) {
		AtsListDto calAts200 = new AtsListDto();
		AtsGenParamDto calAtsGen200 = new AtsGenParamDto();

		long ats200_points = 0;
		try {
			// 1️ Fetch JSON config
			Map<String, Object> ats200ParamData = getGeneralGovernmentATSParam(atsGeneralId, atsParamId);

			if (ats200ParamData == null) {
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
			Map<String, Object> logicDesc = (Map<String, Object>) ats200ParamData.get("logic_description");

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
								System.out.println("ATS-200 :: " + indicator.toLowerCase());
								totalViolations++;
								break;
							}
						}
					}
				}
			}

			// 6 Scoring
			if (totalViolations <= fullScoreLimit) {
				ats200_points = genATSParamData.getMax_points();
			} else if (totalViolations <= partialScoreLimit) {
				ats200_points = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			} else if (totalViolations >= zeroScoreLimit
					|| (totalViolations > partialScoreLimit && totalViolations < zeroScoreLimit)) {
				ats200_points = 0;
			}

			// 7 Storing Data
			long max = genATSParamData.getMax_points();
			long partial = genATSParamData.getMax_points() - genATSParamData.getPenalty_points();
			String paramType = "";

			if (ats200_points == max) {
				paramType = "positive";
			} else if (ats200_points == partial) {
				paramType = "partial";
			} else if (ats200_points == 0) {
				paramType = "negative";
			}

			calAtsGen200.setAtsGeneralId(genATSParamData.getAtsGeneralId());
			calAtsGen200.setAtsParamId(genATSParamData.getAtsParamId());
			calAtsGen200.setCategory(genATSParamData.getCategory());
			calAtsGen200.setDescription(genATSParamData.getDescription());
			calAtsGen200.setMax_points(genATSParamData.getMax_points());
			calAtsGen200.setParameter(genATSParamData.getParameter());
			calAtsGen200.setPenalty_points(genATSParamData.getPenalty_points());
			calAtsGen200.setTotal_points(genATSParamData.getTotal_points());

			calAts200.setAtsGeneralId(atsGeneralId);
			calAts200.setAtsGeneralParamDto(calAtsGen200);
			calAts200.setAtsParamData(ats200ParamData);
			calAts200.setAtsParamId(atsParamId);
			calAts200.setAtsParamType(paramType);
			calAts200.setAtsScore(ats200_points);

			return calAts200;
		} catch (Exception e) {
			e.printStackTrace();
			return calAts200;
		}
	}

}

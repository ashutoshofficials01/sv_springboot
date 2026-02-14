package com.app.sv_springboot.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.json.*;
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
public class GeneralATSUtil {

	private final AtsGenParamDto atsGenParamDto;
	private final ITAtsUtil iTAtsUtil;
	private final BusinessAtsUtil businessAtsUtil;
	private final ConstructionAtsUtil constructionAtsUtil;
	private final DesignAtsUtil designAtsUtil;
	private final EducationAtsUtil educationAtsUtil;
	private final FinanceAtsUtil financeAtsUtil;
	private final GovernmentAtsUtil governmentAtsUtil;
	private final HealthcareAtsUtil healthcareAtsUtil;
	private final LegalAtsUtil legalAtsUtil;
	private final TourismAtsUtil tourismAtsUtil;

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

	GeneralATSUtil(AtsGenParamDto atsGenParamDto, ITAtsUtil iTAtsUtil, BusinessAtsUtil businessAtsUtil,
			ConstructionAtsUtil constructionAtsUtil, DesignAtsUtil designAtsUtil, EducationAtsUtil educationAtsUtil,
			FinanceAtsUtil financeAtsUtil, GovernmentAtsUtil governmentAtsUtil, HealthcareAtsUtil healthcareAtsUtil,
			LegalAtsUtil legalAtsUtil, TourismAtsUtil tourismAtsUtil) {
		this.atsGenParamDto = atsGenParamDto;
		this.iTAtsUtil = iTAtsUtil;
		this.businessAtsUtil = businessAtsUtil;
		this.constructionAtsUtil = constructionAtsUtil;
		this.designAtsUtil = designAtsUtil;
		this.educationAtsUtil = educationAtsUtil;
		this.financeAtsUtil = financeAtsUtil;
		this.governmentAtsUtil = governmentAtsUtil;
		this.healthcareAtsUtil = healthcareAtsUtil;
		this.legalAtsUtil = legalAtsUtil;
		this.tourismAtsUtil = tourismAtsUtil;
	}

	/* IT Resume => => */

	private List<Map<String, Object>> generalITATSParameterList;

	private Map<String, Map<String, Object>> generalITATSParameterMap;

	@PostConstruct
	public void loadITATSParameters() {
		try {

			Resource resource = resourceLoader.getResource("classpath:IT_ATSParameter.json");

			// Read JSON as Map
			Map<String, Object> jsonData = objectMapper.readValue(resource.getInputStream(), Map.class);

			// Extract array
			generalITATSParameterList = (List<Map<String, Object>>) jsonData.get("IT_ATSParameter");

			// Create fast-lookup map
			generalITATSParameterMap = new HashMap<>();

			for (Map<String, Object> param : generalITATSParameterList) {
				Number atsGeneralIdNum = (Number) param.get("atsGeneralId");
				long atsGeneralId = atsGeneralIdNum.longValue();
				String atsParamId = (String) param.get("atsParamId");

				String key = atsGeneralId + "_" + atsParamId;
				generalITATSParameterMap.put(key, param);
			}

			System.out.println("Loaded ATS Params Count IT : " + generalITATSParameterMap.size());

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load IT_ATSParameter.json", e);
		}
	}

	public List<Map<String, Object>> getAllGeneralATSParams() {
		return generalITATSParameterList;
	}

	public Map<String, Object> getGeneralATSParam(long atsGeneralId, String atsParamId) {
		return generalITATSParameterMap.get(atsGeneralId + "_" + atsParamId);
	}

	/* <= <= IT Resume */

	/* Construction Resume => => */

	private List<Map<String, Object>> generalConstructionATSParameterList;

	private Map<String, Map<String, Object>> generalConstructionATSParameterMap;

	@PostConstruct
	public void loadConstructionATSParameters() {
		try {

			Resource resource = resourceLoader.getResource("classpath:Construction_ATSParameter.json");

			// Read JSON as Map
			Map<String, Object> jsonData = objectMapper.readValue(resource.getInputStream(), Map.class);

			// Extract array
			generalConstructionATSParameterList = (List<Map<String, Object>>) jsonData.get("Construction_ATSParameter");

			// Create fast-lookup map
			generalConstructionATSParameterMap = new HashMap<>();

			for (Map<String, Object> param : generalConstructionATSParameterList) {
				Number atsGeneralIdNum = (Number) param.get("atsGeneralId");
				long atsGeneralId = atsGeneralIdNum.longValue();
				String atsParamId = (String) param.get("atsParamId");

				String key = atsGeneralId + "_" + atsParamId;
				generalConstructionATSParameterMap.put(key, param);
			}

			System.out.println("Loaded ATS Params Count Construction : " + generalConstructionATSParameterMap.size());

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load Construction_ATSParameter.json", e);
		}
	}

	public List<Map<String, Object>> getAllConstructionGeneralATSParams() {
		return generalConstructionATSParameterList;
	}

	public Map<String, Object> getGeneralConstructionATSParam(long atsGeneralId, String atsParamId) {
		return generalConstructionATSParameterMap.get(atsGeneralId + "_" + atsParamId);
	}

	/* <= <= Construction Resume */

	/* Business Resume => => */

	private List<Map<String, Object>> generalBusinessATSParameterList;

	private Map<String, Map<String, Object>> generalBusinessATSParameterMap;

	@PostConstruct
	public void loadBusinessATSParameters() {
		try {

			Resource resource = resourceLoader.getResource("classpath:Business_ATSParameter.json");

			// Read JSON as Map
			Map<String, Object> jsonData = objectMapper.readValue(resource.getInputStream(), Map.class);

			// Extract array
			generalBusinessATSParameterList = (List<Map<String, Object>>) jsonData.get("Business_ATSParameter");

			// Create fast-lookup map
			generalBusinessATSParameterMap = new HashMap<>();

			for (Map<String, Object> param : generalBusinessATSParameterList) {
				Number atsGeneralIdNum = (Number) param.get("atsGeneralId");
				long atsGeneralId = atsGeneralIdNum.longValue();
				String atsParamId = (String) param.get("atsParamId");

				String key = atsGeneralId + "_" + atsParamId;
				generalBusinessATSParameterMap.put(key, param);
			}

			System.out.println("Loaded ATS Params Count Business : " + generalBusinessATSParameterMap.size());

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load Business_ATSParameter.json", e);
		}
	}

	public List<Map<String, Object>> getAllBusinessGeneralATSParams() {
		return generalBusinessATSParameterList;
	}

	public Map<String, Object> getGeneralBusinessATSParam(long atsGeneralId, String atsParamId) {
		return generalBusinessATSParameterMap.get(atsGeneralId + "_" + atsParamId);
	}

	/* <= <= Business Resume */

	/* Design Resume => => */

	private List<Map<String, Object>> generalDesignATSParameterList;

	private Map<String, Map<String, Object>> generalDesignATSParameterMap;

	@PostConstruct
	public void loadDesignATSParameters() {
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

			System.out.println("Loaded ATS Params Count Design : " + generalDesignATSParameterMap.size());

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load Design_ATSParameter.json", e);
		}
	}

	public List<Map<String, Object>> getAllDesignGeneralATSParams() {
		return generalDesignATSParameterList;
	}

	public Map<String, Object> getGeneralDesignATSParam(long atsGeneralId, String atsParamId) {
		return generalDesignATSParameterMap.get(atsGeneralId + "_" + atsParamId);
	}

	/* <= <= Design Resume */

	/* Education Resume => => */

	private List<Map<String, Object>> generalEducationATSParameterList;

	private Map<String, Map<String, Object>> generalEducationATSParameterMap;

	@PostConstruct
	public void loadEducationATSParameters() {
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

			System.out.println("Loaded ATS Params Count Education : " + generalEducationATSParameterMap.size());

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load Education_ATSParameter.json", e);
		}
	}

	public List<Map<String, Object>> getAllEducationGeneralATSParams() {
		return generalEducationATSParameterList;
	}

	public Map<String, Object> getGeneralEducationATSParam(long atsGeneralId, String atsParamId) {
		return generalEducationATSParameterMap.get(atsGeneralId + "_" + atsParamId);
	}

	/* <= <= Education Resume */

	/* Finance Resume => => */

	private List<Map<String, Object>> generalFinanceATSParameterList;

	private Map<String, Map<String, Object>> generalFinanceATSParameterMap;

	@PostConstruct
	public void loadFinanceATSParameters() {
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

			System.out.println("Loaded ATS Params Count Finance : " + generalFinanceATSParameterMap.size());

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load Finance_ATSParameter.json", e);
		}
	}

	public List<Map<String, Object>> getAllFinanceGeneralATSParams() {
		return generalFinanceATSParameterList;
	}

	public Map<String, Object> getGeneralFinanceATSParam(long atsGeneralId, String atsParamId) {
		return generalFinanceATSParameterMap.get(atsGeneralId + "_" + atsParamId);
	}

	/* <= <= Finance Resume */

	/* Government Resume => => */

	private List<Map<String, Object>> generalGovernmentATSParameterList;

	private Map<String, Map<String, Object>> generalGovernmentATSParameterMap;

	@PostConstruct
	public void loadGovernmentATSParameters() {
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

			System.out.println("Loaded ATS Params Count Government : " + generalGovernmentATSParameterMap.size());

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load Government_ATSParameter.json", e);
		}
	}

	public List<Map<String, Object>> getAllGovernmentGeneralATSParams() {
		return generalGovernmentATSParameterList;
	}

	public Map<String, Object> getGeneralGovernmentATSParam(long atsGeneralId, String atsParamId) {
		return generalGovernmentATSParameterMap.get(atsGeneralId + "_" + atsParamId);
	}

	/* <= <= Government Resume */

	/* Healthcare Resume => => */

	private List<Map<String, Object>> generalHealthcareATSParameterList;

	private Map<String, Map<String, Object>> generalHealthcareATSParameterMap;

	@PostConstruct
	public void loadHealthcareATSParameters() {
		try {

			Resource resource = resourceLoader.getResource("classpath:Healthcare_ATSParameter.json");

			// Read JSON as Map
			Map<String, Object> jsonData = objectMapper.readValue(resource.getInputStream(), Map.class);

			// Extract array
			generalHealthcareATSParameterList = (List<Map<String, Object>>) jsonData.get("Healthcare_ATSParameter");

			// Create fast-lookup map
			generalHealthcareATSParameterMap = new HashMap<>();

			for (Map<String, Object> param : generalHealthcareATSParameterList) {
				Number atsGeneralIdNum = (Number) param.get("atsGeneralId");
				long atsGeneralId = atsGeneralIdNum.longValue();
				String atsParamId = (String) param.get("atsParamId");

				String key = atsGeneralId + "_" + atsParamId;
				generalHealthcareATSParameterMap.put(key, param);
			}

			System.out.println("Loaded ATS Params Count Healthcare : " + generalHealthcareATSParameterMap.size());

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load Healthcare_ATSParameter.json", e);
		}
	}

	public List<Map<String, Object>> getAllHealthcareGeneralATSParams() {
		return generalHealthcareATSParameterList;
	}

	public Map<String, Object> getGeneralHealthcareATSParam(long atsGeneralId, String atsParamId) {
		return generalHealthcareATSParameterMap.get(atsGeneralId + "_" + atsParamId);
	}

	/* <= <= Healthcare Resume */

	/* Legal Resume => => */

	private List<Map<String, Object>> generalLegalATSParameterList;

	private Map<String, Map<String, Object>> generalLegalATSParameterMap;

	@PostConstruct
	public void loadLegalATSParameters() {
		try {

			Resource resource = resourceLoader.getResource("classpath:Legal_ATSParameter.json");

			// Read JSON as Map
			Map<String, Object> jsonData = objectMapper.readValue(resource.getInputStream(), Map.class);

			// Extract array
			generalLegalATSParameterList = (List<Map<String, Object>>) jsonData.get("Legal_ATSParameter");

			// Create fast-lookup map
			generalLegalATSParameterMap = new HashMap<>();

			for (Map<String, Object> param : generalLegalATSParameterList) {
				Number atsGeneralIdNum = (Number) param.get("atsGeneralId");
				long atsGeneralId = atsGeneralIdNum.longValue();
				String atsParamId = (String) param.get("atsParamId");

				String key = atsGeneralId + "_" + atsParamId;
				generalLegalATSParameterMap.put(key, param);
			}

			System.out.println("Loaded ATS Params Count Legal : " + generalLegalATSParameterMap.size());

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load Legal_ATSParameter.json", e);
		}
	}

	public List<Map<String, Object>> getAllLegalGeneralATSParams() {
		return generalLegalATSParameterList;
	}

	public Map<String, Object> getGeneralLegalATSParam(long atsGeneralId, String atsParamId) {
		return generalLegalATSParameterMap.get(atsGeneralId + "_" + atsParamId);
	}

	/* <= <= Legal Resume */

	/* Tourism Resume => => */

	private List<Map<String, Object>> generalTourismATSParameterList;

	private Map<String, Map<String, Object>> generalTourismATSParameterMap;

	@PostConstruct
	public void loadTourismATSParameters() {
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

			System.out.println("Loaded ATS Params Count Tourism : " + generalTourismATSParameterMap.size());

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load Tourism_ATSParameter.json", e);
		}
	}

	public List<Map<String, Object>> getAllTourismGeneralATSParams() {
		return generalTourismATSParameterList;
	}

	public Map<String, Object> getGeneralTourismATSParam(long atsGeneralId, String atsParamId) {
		return generalTourismATSParameterMap.get(atsGeneralId + "_" + atsParamId);
	}

	/* <= <= Tourism Resume */

	public List<AtsListDto> calculateIT_ATSScore(String fileName, MultipartFile file) {

		long ats_Combined_Score = 0;

		long ats_001_score = 0;
		long ats_002_score = 0;
		long ats_003_score = 0;
		long ats_004_score = 0;
		long ats_005_score = 0;
		long ats_006_score = 0;
		long ats_007_score = 0;
		long ats_008_score = 0;
		long ats_009_score = 0;
		long ats_010_score = 0;
		long ats_011_score = 0;
		long ats_012_score = 0;
		long ats_013_score = 0;
		long ats_014_score = 0;
		long ats_015_score = 0;
		long ats_016_score = 0;
		long ats_017_score = 0;
		long ats_018_score = 0;
		long ats_019_score = 0;
		long ats_020_score = 0;

		List<AtsListDto> storeJSONs = new ArrayList<>();

		AtsListDto combinedDto = new AtsListDto();
		AtsGenParamDto combinedGenDto = new AtsGenParamDto();

		List<Map<String, Object>> generalAllATSParams = getAllGeneralATSParams();
		List<ATS_General_Param_Entity> genATSParamAll = atsGeneralParamRepo.findAllByResumeId(100L);
//		Map<String, Object> generalATSParam = getGeneralATSParam();

		for (Map<String, Object> genATS : generalAllATSParams) {
			for (ATS_General_Param_Entity agp : genATSParamAll) {

				String atsParamId = String.valueOf(agp.getAtsParamId());
//				System.out.println("atsParamId :: " + atsParamId);

				String genATSParamId = String.valueOf(genATS.get("atsParamId"));

				if (atsParamId.equalsIgnoreCase(genATSParamId) || atsParamId == genATSParamId) {
					// System.out.println("atsParamId :: " + atsParamId + " <!> genATSParamId :: " +
					// genATSParamId);

					switch (atsParamId) {

					case "ATS-001":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_001_obj = iTAtsUtil.calculateATS001(atsParamId, generalATSId, fileName,
									file);

							if (ats_001_obj != null) {
								ats_001_score = ats_001_obj.getAtsScore();
								storeJSONs.add(ats_001_obj);
							}
//						JSONObject ats_001_obj = calculateATS001(atsParamId, generalATSId, fileName, file);
//						ats_001_score = ats_001_obj.getLong("ats001_points");
//						storeJSONs.add(ats_001_obj);

//						ats_001_score = calculateATS001(atsParamId, generalATSId, fileName, file);

						}
						break;

					case "ATS-002":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_002_obj = iTAtsUtil.calculateATS002(atsParamId, generalATSId, fileName,
									file);

							if (ats_002_obj != null) {
								ats_002_score = ats_002_obj.getAtsScore();
								storeJSONs.add(ats_002_obj);
							}
//						ats_002_score = calculateATS002(atsParamId, generalATSId, fileName, file);

//						JSONObject ats_002_obj = calculateATS002(atsParamId, generalATSId, fileName, file);
//
//						ats_002_score = ats_002_obj.getLong("ats002_points");
//
//						storeJSONs.add(ats_002_obj);

						}

						break;

					case "ATS-003":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_003_obj = iTAtsUtil.calculateATS003(atsParamId, generalATSId, fileName,
									file);

							if (ats_003_obj != null) {
								ats_003_score = ats_003_obj.getAtsScore();
								storeJSONs.add(ats_003_obj);
							}
//						ats_003_score = calculateATS003(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_003_obj = calculateATS003(atsParamId, generalATSId, fileName, file);
//
//						ats_003_score = ats_003_obj.getLong("ats003_points");
//
//						storeJSONs.add(ats_003_obj);
						}

						break;

					case "ATS-004":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_004_obj = iTAtsUtil.calculateATS004(atsParamId, generalATSId, fileName,
									file);

							if (ats_004_obj != null) {
								ats_004_score = ats_004_obj.getAtsScore();
								storeJSONs.add(ats_004_obj);
							}
//						ats_004_score = calculateATS004(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_004_obj = calculateATS004(atsParamId, generalATSId, fileName, file);
//
//						ats_004_score = ats_004_obj.getLong("ats004_points");
//
//						storeJSONs.add(ats_004_obj);

						}
						break;

					case "ATS-005":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_005_obj = iTAtsUtil.calculateATS005(atsParamId, generalATSId, fileName,
									file);

							if (ats_005_obj != null) {
								ats_005_score = ats_005_obj.getAtsScore();
								storeJSONs.add(ats_005_obj);
							}
//						ats_005_score = calculateATS005(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_005_obj = calculateATS005(atsParamId, generalATSId, fileName, file);
//
//						ats_005_score = ats_005_obj.getLong("ats005_points");
//
//						storeJSONs.add(ats_005_obj);

						}

						break;

					case "ATS-006":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_006_obj = iTAtsUtil.calculateATS006(atsParamId, generalATSId, fileName,
									file);

							if (ats_006_obj != null) {
								ats_006_score = ats_006_obj.getAtsScore();
								storeJSONs.add(ats_006_obj);
							}

//						ats_006_score = calculateATS006(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_006_obj = calculateATS006(atsParamId, generalATSId, fileName, file);
//
//						ats_006_score = ats_006_obj.getLong("ats006_points");
//
//						storeJSONs.add(ats_006_obj);

						}

						break;

					case "ATS-007":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_007_obj = iTAtsUtil.calculateATS007(atsParamId, generalATSId, fileName,
									file);

							if (ats_007_obj != null) {
								ats_007_score = ats_007_obj.getAtsScore();
								storeJSONs.add(ats_007_obj);
							}
//						ats_007_score = calculateATS007(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_007_obj = calculateATS007(atsParamId, generalATSId, fileName, file);
//
//						ats_007_score = ats_007_obj.getLong("ats007_points");
//
//						storeJSONs.add(ats_007_obj);

						}

						break;

					case "ATS-008":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_008_obj = iTAtsUtil.calculateATS008(atsParamId, generalATSId, fileName,
									file);

							if (ats_008_obj != null) {
								ats_008_score = ats_008_obj.getAtsScore();
								storeJSONs.add(ats_008_obj);
							}
//						ats_008_score = calculateATS008(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_008_obj = calculateATS008(atsParamId, generalATSId, fileName, file);
//
//						ats_008_score = ats_008_obj.getLong("ats008_points");
//
//						storeJSONs.add(ats_008_obj);

						}
						break;

					case "ATS-009":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_009_obj = iTAtsUtil.calculateATS009(atsParamId, generalATSId, fileName,
									file);

							if (ats_009_obj != null) {
								ats_009_score = ats_009_obj.getAtsScore();
								storeJSONs.add(ats_009_obj);
							}
//						ats_009_score = calculateATS009(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_009_obj = calculateATS009(atsParamId, generalATSId, fileName, file);
//
//						ats_009_score = ats_009_obj.getLong("ats009_points");
//
//						storeJSONs.add(ats_009_obj);

						}

						break;

					case "ATS-010":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_010_obj = iTAtsUtil.calculateATS010(atsParamId, generalATSId, fileName,
									file);
							if (ats_010_obj != null) {
								ats_010_score = ats_010_obj.getAtsScore();
								storeJSONs.add(ats_010_obj);
							}
//						ats_010_score = calculateATS010(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_010_obj = calculateATS010(atsParamId, generalATSId, fileName, file);
//
//						ats_010_score = ats_010_obj.getLong("ats010_points");
//
//						storeJSONs.add(ats_010_obj);

						}

						break;

					case "ATS-011":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_011_obj = iTAtsUtil.calculateATS011(atsParamId, generalATSId, fileName,
									file);
							if (ats_011_obj != null) {
								ats_011_score = ats_011_obj.getAtsScore();
								storeJSONs.add(ats_011_obj);
							}
//						ats_011_score = calculateATS011(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_011_obj = calculateATS011(atsParamId, generalATSId, fileName, file);
//
//						ats_011_score = ats_011_obj.getLong("ats011_points");
//
//						storeJSONs.add(ats_011_obj);

						}
						break;

					case "ATS-012":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_012_obj = iTAtsUtil.calculateATS012(atsParamId, generalATSId, fileName,
									file);

							if (ats_012_obj != null) {
								ats_012_score = ats_012_obj.getAtsScore();
								storeJSONs.add(ats_012_obj);
							}
//						ats_012_score = calculateATS012(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_012_obj = calculateATS012(atsParamId, generalATSId, fileName, file);
//
//						ats_012_score = ats_012_obj.getLong("ats012_points");
//
//						storeJSONs.add(ats_012_obj);

						}

						break;

					case "ATS-013":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_013_obj = iTAtsUtil.calculateATS013(atsParamId, generalATSId, fileName,
									file);

							if (ats_013_obj != null) {
								ats_013_score = ats_013_obj.getAtsScore();
								storeJSONs.add(ats_013_obj);
							}
//						ats_013_score = calculateATS013(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_013_obj = calculateATS013(atsParamId, generalATSId, fileName, file);
//
//						ats_013_score = ats_013_obj.getLong("ats013_points");
//
//						storeJSONs.add(ats_013_obj);

						}

						break;

					case "ATS-014":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_014_obj = iTAtsUtil.calculateATS014(atsParamId, generalATSId, fileName,
									file);

							if (ats_014_obj != null) {
								ats_014_score = ats_014_obj.getAtsScore();
								storeJSONs.add(ats_014_obj);
							}
//						ats_014_score = calculateATS014(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_014_obj = calculateATS014(atsParamId, generalATSId, fileName, file);
//
//						ats_014_score = ats_014_obj.getLong("ats014_points");
//
//						storeJSONs.add(ats_014_obj);

						}
						break;

					case "ATS-015":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_015_obj = iTAtsUtil.calculateATS015(atsParamId, generalATSId, fileName,
									file);

							if (ats_015_obj != null) {
								ats_015_score = ats_015_obj.getAtsScore();
								storeJSONs.add(ats_015_obj);
							}
//						ats_015_score = calculateATS015(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_015_obj = calculateATS015(atsParamId, generalATSId, fileName, file);
//
//						ats_015_score = ats_015_obj.getLong("ats015_points");
//
//						storeJSONs.add(ats_015_obj);

						}

						break;

					case "ATS-016":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_016_obj = iTAtsUtil.calculateATS016(atsParamId, generalATSId, fileName,
									file);

							if (ats_016_obj != null) {
								ats_016_score = ats_016_obj.getAtsScore();
								storeJSONs.add(ats_016_obj);
							}
//						ats_016_score = calculateATS016(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_016_obj = calculateATS016(atsParamId, generalATSId, fileName, file);
//
//						ats_016_score = ats_016_obj.getLong("ats016_points");
//
//						storeJSONs.add(ats_016_obj);

						}

						break;

					case "ATS-017":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_017_obj = iTAtsUtil.calculateATS017(atsParamId, generalATSId, fileName,
									file);

							if (ats_017_obj != null) {
								ats_017_score = ats_017_obj.getAtsScore();
								storeJSONs.add(ats_017_obj);
							}
//						ats_017_score = calculateATS017(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_017_obj = calculateATS017(atsParamId, generalATSId, fileName, file);
//
//						ats_017_score = ats_017_obj.getLong("ats017_points");
//
//						storeJSONs.add(ats_017_obj);

						}

						break;

					case "ATS-018":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_018_obj = iTAtsUtil.calculateATS018(atsParamId, generalATSId, fileName,
									file);

							if (ats_018_obj != null) {
								ats_018_score = ats_018_obj.getAtsScore();
								storeJSONs.add(ats_018_obj);
							}
//						ats_018_score = calculateATS018(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_018_obj = calculateATS018(atsParamId, generalATSId, fileName, file);
//
//						ats_018_score = ats_018_obj.getLong("ats018_points");
//
//						storeJSONs.add(ats_018_obj);

						}
						break;

					case "ATS-019":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_019_obj = iTAtsUtil.calculateATS019(atsParamId, generalATSId, fileName,
									file);

							if (ats_019_obj != null) {
								ats_019_score = ats_019_obj.getAtsScore();
								storeJSONs.add(ats_019_obj);
							}
//						ats_019_score = calculateATS019(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_019_obj = calculateATS019(atsParamId, generalATSId, fileName, file);
//
//						ats_019_score = ats_019_obj.getLong("ats019_points");
//
//						storeJSONs.add(ats_019_obj);

						}

						break;

					case "ATS-020":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_020_obj = iTAtsUtil.calculateATS020(atsParamId, generalATSId, fileName,
									file);

							if (ats_020_obj != null) {
								ats_020_score = ats_020_obj.getAtsScore();
								storeJSONs.add(ats_020_obj);
							}
//						ats_020_score = calculateATS020(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_020_obj = calculateATS020(atsParamId, generalATSId, fileName, file);
//
//						ats_020_score = ats_020_obj.getLong("ats020_points");
//
//						storeJSONs.add(ats_020_obj);

						}

						break;

					default:
						System.out.println("Unhandled ATS Param: " + agp.getAtsParamId());
					}
				}
			}
		}

		System.out.println(" ATS Combined Score : " + "\nATS-001 => " + ats_001_score + "\nATS-002 => " + ats_002_score
				+ "\nATS-003 => " + ats_003_score + "\nATS-004 => " + ats_004_score + "\nATS-005 => " + ats_005_score
				+ "\nATS-006 => " + ats_006_score + "\nATS-007 => " + ats_007_score + "\nATS-008 => " + ats_008_score
				+ "\nATS-009 => " + ats_009_score + "\nATS-010 => " + ats_010_score + "\nATS-011 => " + ats_011_score
				+ "\nATS-012 => " + ats_012_score + "\nATS-013 => " + ats_013_score + "\nATS-014 => " + ats_014_score
				+ "\nATS-015 => " + ats_015_score + "\nATS-016 => " + ats_016_score + "\nATS-017 => " + ats_017_score
				+ "\nATS-018 => " + ats_018_score + "\nATS-019 => " + ats_019_score + "\nATS-020 => " + ats_020_score);

		ats_Combined_Score = ats_001_score + ats_002_score + ats_003_score + ats_004_score + ats_005_score
				+ ats_006_score + ats_007_score + ats_008_score + ats_009_score + ats_010_score + ats_011_score
				+ ats_012_score + ats_013_score + ats_014_score + ats_015_score + ats_016_score + ats_017_score
				+ ats_018_score + ats_019_score + ats_020_score;

//		combinedDto = new AtsListDto();
//		combinedGenDto = new AtsGenParamDto();

		long atsGenCombined = 21;
		ATS_General_Param_Entity genATSCombined = atsGeneralParamRepo.findByAtsGeneralId(atsGenCombined);

		combinedGenDto.setAtsGeneralId(genATSCombined.getAtsGeneralId());
		combinedGenDto.setAtsParamId(genATSCombined.getAtsParamId());
		combinedGenDto.setCategory(genATSCombined.getCategory());
		combinedGenDto.setDescription(genATSCombined.getDescription());
		combinedGenDto.setMax_points(genATSCombined.getMax_points());
		combinedGenDto.setParameter(genATSCombined.getParameter());
		combinedGenDto.setPenalty_points(genATSCombined.getPenalty_points());
		combinedGenDto.setTotal_points(genATSCombined.getTotal_points());

		combinedDto.setAtsGeneralId(genATSCombined.getAtsGeneralId());
		combinedDto.setAtsGeneralParamDto(combinedGenDto);
		combinedDto.setAtsParamData(null);
		combinedDto.setAtsParamId(genATSCombined.getAtsParamId());
		combinedDto.setAtsParamType("Total");
		combinedDto.setAtsScore(ats_Combined_Score);

		storeJSONs.add(combinedDto);

		System.out.println("storeJSONs :: " + storeJSONs);

		return storeJSONs;

	}

	public List<AtsListDto> calculateConstruction_ATSScore(String fileName, MultipartFile file) {

		long ats_Combined_Score = 0;

		long ats_061_score = 0;
		long ats_062_score = 0;
		long ats_063_score = 0;
		long ats_064_score = 0;
		long ats_065_score = 0;
		long ats_066_score = 0;
		long ats_067_score = 0;
		long ats_068_score = 0;
		long ats_069_score = 0;
		long ats_070_score = 0;
		long ats_071_score = 0;
		long ats_072_score = 0;
		long ats_073_score = 0;
		long ats_074_score = 0;
		long ats_075_score = 0;
		long ats_076_score = 0;
		long ats_077_score = 0;
		long ats_078_score = 0;
		long ats_079_score = 0;
		long ats_080_score = 0;

		List<AtsListDto> storeJSONs = new ArrayList<>();

		AtsListDto combinedDto = new AtsListDto();
		AtsGenParamDto combinedGenDto = new AtsGenParamDto();

		List<Map<String, Object>> generalAllConstructionATSParams = getAllConstructionGeneralATSParams();
		List<ATS_General_Param_Entity> genATSParamAll = atsGeneralParamRepo.findAllByResumeId(102L);
//		Map<String, Object> generalATSParam = getGeneralATSParam();

		for (Map<String, Object> genATS : generalAllConstructionATSParams) {
			for (ATS_General_Param_Entity agp : genATSParamAll) {

				String atsParamId = String.valueOf(agp.getAtsParamId());
//				System.out.println("atsParamId :: " + atsParamId);

				String genATSParamId = String.valueOf(genATS.get("atsParamId"));
//				// System.out.println("atsParamId :: " + atsParamId + " <!> genATSParamId :: " + genATSParamId);
				if (atsParamId.equalsIgnoreCase(genATSParamId) || atsParamId == genATSParamId) {
					// System.out.println("atsParamId :: " + atsParamId + " <!> genATSParamId :: " +
					// genATSParamId);

					switch (atsParamId) {

					case "ATS-061":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_061_obj = constructionAtsUtil.calculateATS061(atsParamId, generalATSId,
									fileName, file);

							if (ats_061_obj != null) {
								ats_061_score = ats_061_obj.getAtsScore();
								storeJSONs.add(ats_061_obj);
							}
//						JSONObject ats_061_obj = calculateATS061(atsParamId, generalATSId, fileName, file);
//						ats_061_score = ats_061_obj.getLong("ats061_points");
//						storeJSONs.add(ats_061_obj);

//						ats_061_score = calculateATS061(atsParamId, generalATSId, fileName, file);

						}
						break;

					case "ATS-062":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_062_obj = constructionAtsUtil.calculateATS062(atsParamId, generalATSId,
									fileName, file);

							if (ats_062_obj != null) {
								ats_062_score = ats_062_obj.getAtsScore();
								storeJSONs.add(ats_062_obj);
							}
//						ats_062_score = calculateATS062(atsParamId, generalATSId, fileName, file);

//						JSONObject ats_062_obj = calculateATS062(atsParamId, generalATSId, fileName, file);
//
//						ats_062_score = ats_062_obj.getLong("ats062_points");
//
//						storeJSONs.add(ats_062_obj);

						}

						break;

					case "ATS-063":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_063_obj = constructionAtsUtil.calculateATS063(atsParamId, generalATSId,
									fileName, file);

							if (ats_063_obj != null) {
								ats_063_score = ats_063_obj.getAtsScore();
								storeJSONs.add(ats_063_obj);
							}
//						ats_063_score = calculateATS063(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_063_obj = calculateATS063(atsParamId, generalATSId, fileName, file);
//
//						ats_063_score = ats_063_obj.getLong("ats063_points");
//
//						storeJSONs.add(ats_063_obj);
						}

						break;

					case "ATS-064":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_064_obj = constructionAtsUtil.calculateATS064(atsParamId, generalATSId,
									fileName, file);

							if (ats_064_obj != null) {
								ats_064_score = ats_064_obj.getAtsScore();
								storeJSONs.add(ats_064_obj);
							}
//						ats_064_score = calculateATS064(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_064_obj = calculateATS064(atsParamId, generalATSId, fileName, file);
//
//						ats_064_score = ats_064_obj.getLong("ats064_points");
//
//						storeJSONs.add(ats_064_obj);

						}
						break;

					case "ATS-065":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_065_obj = constructionAtsUtil.calculateATS065(atsParamId, generalATSId,
									fileName, file);

							if (ats_065_obj != null) {
								ats_065_score = ats_065_obj.getAtsScore();
								storeJSONs.add(ats_065_obj);
							}
//						ats_065_score = calculateATS065(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_065_obj = calculateATS065(atsParamId, generalATSId, fileName, file);
//
//						ats_065_score = ats_065_obj.getLong("ats065_points");
//
//						storeJSONs.add(ats_065_obj);

						}

						break;

					case "ATS-066":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_066_obj = constructionAtsUtil.calculateATS066(atsParamId, generalATSId,
									fileName, file);

							if (ats_066_obj != null) {
								ats_066_score = ats_066_obj.getAtsScore();
								storeJSONs.add(ats_066_obj);
							}

//						ats_066_score = calculateATS066(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_066_obj = calculateATS066(atsParamId, generalATSId, fileName, file);
//
//						ats_066_score = ats_066_obj.getLong("ats066_points");
//
//						storeJSONs.add(ats_066_obj);

						}

						break;

					case "ATS-067":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_067_obj = constructionAtsUtil.calculateATS067(atsParamId, generalATSId,
									fileName, file);

							if (ats_067_obj != null) {
								ats_067_score = ats_067_obj.getAtsScore();
								storeJSONs.add(ats_067_obj);
							}
//						ats_067_score = calculateATS067(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_067_obj = calculateATS067(atsParamId, generalATSId, fileName, file);
//
//						ats_067_score = ats_067_obj.getLong("ats067_points");
//
//						storeJSONs.add(ats_067_obj);

						}

						break;

					case "ATS-068":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_068_obj = constructionAtsUtil.calculateATS068(atsParamId, generalATSId,
									fileName, file);

							if (ats_068_obj != null) {
								ats_068_score = ats_068_obj.getAtsScore();
								storeJSONs.add(ats_068_obj);
							}
//						ats_068_score = calculateATS068(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_068_obj = calculateATS068(atsParamId, generalATSId, fileName, file);
//
//						ats_068_score = ats_068_obj.getLong("ats068_points");
//
//						storeJSONs.add(ats_068_obj);

						}
						break;

					case "ATS-069":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_069_obj = constructionAtsUtil.calculateATS069(atsParamId, generalATSId,
									fileName, file);

							if (ats_069_obj != null) {
								ats_069_score = ats_069_obj.getAtsScore();
								storeJSONs.add(ats_069_obj);
							}
//						ats_069_score = calculateATS069(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_069_obj = calculateATS069(atsParamId, generalATSId, fileName, file);
//
//						ats_069_score = ats_069_obj.getLong("ats069_points");
//
//						storeJSONs.add(ats_069_obj);

						}

						break;

					case "ATS-070":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_070_obj = constructionAtsUtil.calculateATS070(atsParamId, generalATSId,
									fileName, file);
							if (ats_070_obj != null) {
								ats_070_score = ats_070_obj.getAtsScore();
								storeJSONs.add(ats_070_obj);
							}
//						ats_070_score = calculateATS070(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_070_obj = calculateATS070(atsParamId, generalATSId, fileName, file);
//
//						ats_070_score = ats_070_obj.getLong("ats070_points");
//
//						storeJSONs.add(ats_070_obj);

						}

						break;

					case "ATS-071":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_071_obj = constructionAtsUtil.calculateATS071(atsParamId, generalATSId,
									fileName, file);
							if (ats_071_obj != null) {
								ats_071_score = ats_071_obj.getAtsScore();
								storeJSONs.add(ats_071_obj);
							}
//						ats_071_score = calculateATS071(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_071_obj = calculateATS071(atsParamId, generalATSId, fileName, file);
//
//						ats_071_score = ats_071_obj.getLong("ats071_points");
//
//						storeJSONs.add(ats_071_obj);

						}
						break;

					case "ATS-072":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_072_obj = constructionAtsUtil.calculateATS072(atsParamId, generalATSId,
									fileName, file);

							if (ats_072_obj != null) {
								ats_072_score = ats_072_obj.getAtsScore();
								storeJSONs.add(ats_072_obj);
							}
//						ats_072_score = calculateATS072(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_072_obj = calculateATS072(atsParamId, generalATSId, fileName, file);
//
//						ats_072_score = ats_072_obj.getLong("ats072_points");
//
//						storeJSONs.add(ats_072_obj);

						}

						break;

					case "ATS-073":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_073_obj = constructionAtsUtil.calculateATS073(atsParamId, generalATSId,
									fileName, file);

							if (ats_073_obj != null) {
								ats_073_score = ats_073_obj.getAtsScore();
								storeJSONs.add(ats_073_obj);
							}
//						ats_073_score = calculateATS073(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_073_obj = calculateATS073(atsParamId, generalATSId, fileName, file);
//
//						ats_073_score = ats_073_obj.getLong("ats073_points");
//
//						storeJSONs.add(ats_073_obj);

						}

						break;

					case "ATS-074":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_074_obj = constructionAtsUtil.calculateATS074(atsParamId, generalATSId,
									fileName, file);

							if (ats_074_obj != null) {
								ats_074_score = ats_074_obj.getAtsScore();
								storeJSONs.add(ats_074_obj);
							}
//						ats_074_score = calculateATS074(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_074_obj = calculateATS074(atsParamId, generalATSId, fileName, file);
//
//						ats_074_score = ats_074_obj.getLong("ats074_points");
//
//						storeJSONs.add(ats_074_obj);

						}
						break;

					case "ATS-075":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_075_obj = constructionAtsUtil.calculateATS075(atsParamId, generalATSId,
									fileName, file);

							if (ats_075_obj != null) {
								ats_075_score = ats_075_obj.getAtsScore();
								storeJSONs.add(ats_075_obj);
							}
//						ats_075_score = calculateATS075(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_075_obj = calculateATS075(atsParamId, generalATSId, fileName, file);
//
//						ats_075_score = ats_075_obj.getLong("ats075_points");
//
//						storeJSONs.add(ats_075_obj);

						}

						break;

					case "ATS-076":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_076_obj = constructionAtsUtil.calculateATS076(atsParamId, generalATSId,
									fileName, file);

							if (ats_076_obj != null) {
								ats_076_score = ats_076_obj.getAtsScore();
								storeJSONs.add(ats_076_obj);
							}
//						ats_076_score = calculateATS076(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_076_obj = calculateATS076(atsParamId, generalATSId, fileName, file);
//
//						ats_076_score = ats_076_obj.getLong("ats076_points");
//
//						storeJSONs.add(ats_076_obj);

						}

						break;

					case "ATS-077":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_077_obj = constructionAtsUtil.calculateATS077(atsParamId, generalATSId,
									fileName, file);

							if (ats_077_obj != null) {
								ats_077_score = ats_077_obj.getAtsScore();
								storeJSONs.add(ats_077_obj);
							}
//						ats_077_score = calculateATS077(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_077_obj = calculateATS077(atsParamId, generalATSId, fileName, file);
//
//						ats_077_score = ats_077_obj.getLong("ats077_points");
//
//						storeJSONs.add(ats_077_obj);

						}

						break;

					case "ATS-078":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_078_obj = constructionAtsUtil.calculateATS078(atsParamId, generalATSId,
									fileName, file);

							if (ats_078_obj != null) {
								ats_078_score = ats_078_obj.getAtsScore();
								storeJSONs.add(ats_078_obj);
							}
//						ats_078_score = calculateATS078(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_078_obj = calculateATS078(atsParamId, generalATSId, fileName, file);
//
//						ats_078_score = ats_078_obj.getLong("ats078_points");
//
//						storeJSONs.add(ats_078_obj);

						}
						break;

					case "ATS-079":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_079_obj = constructionAtsUtil.calculateATS079(atsParamId, generalATSId,
									fileName, file);

							if (ats_079_obj != null) {
								ats_079_score = ats_079_obj.getAtsScore();
								storeJSONs.add(ats_079_obj);
							}
//						ats_079_score = calculateATS079(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_079_obj = calculateATS079(atsParamId, generalATSId, fileName, file);
//
//						ats_079_score = ats_079_obj.getLong("ats079_points");
//
//						storeJSONs.add(ats_079_obj);

						}

						break;

					case "ATS-080":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_080_obj = constructionAtsUtil.calculateATS080(atsParamId, generalATSId,
									fileName, file);

							if (ats_080_obj != null) {
								ats_080_score = ats_080_obj.getAtsScore();
								storeJSONs.add(ats_080_obj);
							}
//						ats_080_score = calculateATS080(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_080_obj = calculateATS080(atsParamId, generalATSId, fileName, file);
//
//						ats_080_score = ats_080_obj.getLong("ats080_points");
//
//						storeJSONs.add(ats_080_obj);

						}

						break;

					default:
						System.out.println("Unhandled ATS Param: " + agp.getAtsParamId());
					}
				}
			}
		}

		System.out.println(" ATS Combined Score : " + "\nATS-061 => " + ats_061_score + "\nATS-062 => " + ats_062_score
				+ "\nATS-063 => " + ats_063_score + "\nATS-064 => " + ats_064_score + "\nATS-065 => " + ats_065_score
				+ "\nATS-066 => " + ats_066_score + "\nATS-067 => " + ats_067_score + "\nATS-068 => " + ats_068_score
				+ "\nATS-069 => " + ats_069_score + "\nATS-070 => " + ats_070_score + "\nATS-071 => " + ats_071_score
				+ "\nATS-072 => " + ats_072_score + "\nATS-073 => " + ats_073_score + "\nATS-074 => " + ats_074_score
				+ "\nATS-075 => " + ats_075_score + "\nATS-076 => " + ats_076_score + "\nATS-077 => " + ats_077_score
				+ "\nATS-078 => " + ats_078_score + "\nATS-079 => " + ats_079_score + "\nATS-080 => " + ats_080_score);

		ats_Combined_Score = ats_061_score + ats_062_score + ats_063_score + ats_064_score + ats_065_score
				+ ats_066_score + ats_067_score + ats_068_score + ats_069_score + ats_070_score + ats_071_score
				+ ats_072_score + ats_073_score + ats_074_score + ats_075_score + ats_076_score + ats_077_score
				+ ats_078_score + ats_079_score + ats_080_score;

//		combinedDto = new AtsListDto();
//		combinedGenDto = new AtsGenParamDto();

		long atsGenCombined = 81;
		ATS_General_Param_Entity genATSCombined = atsGeneralParamRepo.findByAtsGeneralId(atsGenCombined);

		combinedGenDto.setAtsGeneralId(genATSCombined.getAtsGeneralId());
		combinedGenDto.setAtsParamId(genATSCombined.getAtsParamId());
		combinedGenDto.setCategory(genATSCombined.getCategory());
		combinedGenDto.setDescription(genATSCombined.getDescription());
		combinedGenDto.setMax_points(genATSCombined.getMax_points());
		combinedGenDto.setParameter(genATSCombined.getParameter());
		combinedGenDto.setPenalty_points(genATSCombined.getPenalty_points());
		combinedGenDto.setTotal_points(genATSCombined.getTotal_points());

		combinedDto.setAtsGeneralId(genATSCombined.getAtsGeneralId());
		combinedDto.setAtsGeneralParamDto(combinedGenDto);
		combinedDto.setAtsParamData(null);
		combinedDto.setAtsParamId(genATSCombined.getAtsParamId());
		combinedDto.setAtsParamType("Total");
		combinedDto.setAtsScore(ats_Combined_Score);

		storeJSONs.add(combinedDto);

		System.out.println("storeJSONs :: " + storeJSONs);

		return storeJSONs;
	}

	public List<AtsListDto> calculateEducation_ATSScore(String fileName, MultipartFile file) {

		long ats_Combined_Score = 0;

		long ats_121_score = 0;
		long ats_122_score = 0;
		long ats_123_score = 0;
		long ats_124_score = 0;
		long ats_125_score = 0;
		long ats_126_score = 0;
		long ats_127_score = 0;
		long ats_128_score = 0;
		long ats_129_score = 0;
		long ats_130_score = 0;
		long ats_131_score = 0;
		long ats_132_score = 0;
		long ats_133_score = 0;
		long ats_134_score = 0;
		long ats_135_score = 0;
		long ats_136_score = 0;
		long ats_137_score = 0;
		long ats_138_score = 0;
		long ats_139_score = 0;
		long ats_140_score = 0;

		List<AtsListDto> storeJSONs = new ArrayList<>();

		AtsListDto combinedDto = new AtsListDto();
		AtsGenParamDto combinedGenDto = new AtsGenParamDto();

		List<Map<String, Object>> generalAllEducationATSParams = getAllEducationGeneralATSParams();
		List<ATS_General_Param_Entity> genATSParamAll = atsGeneralParamRepo.findAllByResumeId(104L);
//		Map<String, Object> generalATSParam = getGeneralATSParam();

		for (Map<String, Object> genATS : generalAllEducationATSParams) {
			for (ATS_General_Param_Entity agp : genATSParamAll) {

				String atsParamId = String.valueOf(agp.getAtsParamId());
//				System.out.println("atsParamId :: " + atsParamId);

				String genATSParamId = String.valueOf(genATS.get("atsParamId"));
//				// System.out.println("atsParamId :: " + atsParamId + " <!> genATSParamId :: " + genATSParamId);

				if (atsParamId.equalsIgnoreCase(genATSParamId) || atsParamId == genATSParamId) {
					// System.out.println("atsParamId :: " + atsParamId + " <!> genATSParamId :: " +
					// genATSParamId);

					switch (atsParamId) {

					case "ATS-121":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_121_obj = educationAtsUtil.calculateATS121(atsParamId, generalATSId,
									fileName, file);

							if (ats_121_obj != null) {
								ats_121_score = ats_121_obj.getAtsScore();
								storeJSONs.add(ats_121_obj);
							}
//						JSONObject ats_121_obj = calculateATS121(atsParamId, generalATSId, fileName, file);
//						ats_121_score = ats_121_obj.getLong("ats121_points");
//						storeJSONs.add(ats_121_obj);

//						ats_121_score = calculateATS121(atsParamId, generalATSId, fileName, file);

						}
						break;

					case "ATS-122":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_122_obj = educationAtsUtil.calculateATS122(atsParamId, generalATSId,
									fileName, file);

							if (ats_122_obj != null) {
								ats_122_score = ats_122_obj.getAtsScore();
								storeJSONs.add(ats_122_obj);
							}
//						ats_122_score = calculateATS122(atsParamId, generalATSId, fileName, file);

//						JSONObject ats_122_obj = calculateATS122(atsParamId, generalATSId, fileName, file);
//
//						ats_122_score = ats_122_obj.getLong("ats122_points");
//
//						storeJSONs.add(ats_122_obj);

						}

						break;

					case "ATS-123":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_123_obj = educationAtsUtil.calculateATS123(atsParamId, generalATSId,
									fileName, file);

							if (ats_123_obj != null) {
								ats_123_score = ats_123_obj.getAtsScore();
								storeJSONs.add(ats_123_obj);
							}
//						ats_123_score = calculateATS123(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_123_obj = calculateATS123(atsParamId, generalATSId, fileName, file);
//
//						ats_123_score = ats_123_obj.getLong("ats123_points");
//
//						storeJSONs.add(ats_123_obj);
						}

						break;

					case "ATS-124":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_124_obj = educationAtsUtil.calculateATS124(atsParamId, generalATSId,
									fileName, file);

							if (ats_124_obj != null) {
								ats_124_score = ats_124_obj.getAtsScore();
								storeJSONs.add(ats_124_obj);
							}
//						ats_124_score = calculateATS124(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_124_obj = calculateATS124(atsParamId, generalATSId, fileName, file);
//
//						ats_124_score = ats_124_obj.getLong("ats124_points");
//
//						storeJSONs.add(ats_124_obj);

						}
						break;

					case "ATS-125":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_125_obj = educationAtsUtil.calculateATS125(atsParamId, generalATSId,
									fileName, file);

							if (ats_125_obj != null) {
								ats_125_score = ats_125_obj.getAtsScore();
								storeJSONs.add(ats_125_obj);
							}
//						ats_125_score = calculateATS125(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_125_obj = calculateATS125(atsParamId, generalATSId, fileName, file);
//
//						ats_125_score = ats_125_obj.getLong("ats125_points");
//
//						storeJSONs.add(ats_125_obj);

						}

						break;

					case "ATS-126":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_126_obj = educationAtsUtil.calculateATS126(atsParamId, generalATSId,
									fileName, file);

							if (ats_126_obj != null) {
								ats_126_score = ats_126_obj.getAtsScore();
								storeJSONs.add(ats_126_obj);
							}

//						ats_126_score = calculateATS126(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_126_obj = calculateATS126(atsParamId, generalATSId, fileName, file);
//
//						ats_126_score = ats_126_obj.getLong("ats126_points");
//
//						storeJSONs.add(ats_126_obj);

						}

						break;

					case "ATS-127":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_127_obj = educationAtsUtil.calculateATS127(atsParamId, generalATSId,
									fileName, file);

							if (ats_127_obj != null) {
								ats_127_score = ats_127_obj.getAtsScore();
								storeJSONs.add(ats_127_obj);
							}
//						ats_127_score = calculateATS127(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_127_obj = calculateATS127(atsParamId, generalATSId, fileName, file);
//
//						ats_127_score = ats_127_obj.getLong("ats127_points");
//
//						storeJSONs.add(ats_127_obj);

						}

						break;

					case "ATS-128":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_128_obj = educationAtsUtil.calculateATS128(atsParamId, generalATSId,
									fileName, file);

							if (ats_128_obj != null) {
								ats_128_score = ats_128_obj.getAtsScore();
								storeJSONs.add(ats_128_obj);
							}
//						ats_128_score = calculateATS128(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_128_obj = calculateATS128(atsParamId, generalATSId, fileName, file);
//
//						ats_128_score = ats_128_obj.getLong("ats128_points");
//
//						storeJSONs.add(ats_128_obj);

						}
						break;

					case "ATS-129":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_129_obj = educationAtsUtil.calculateATS129(atsParamId, generalATSId,
									fileName, file);

							if (ats_129_obj != null) {
								ats_129_score = ats_129_obj.getAtsScore();
								storeJSONs.add(ats_129_obj);
							}
//						ats_129_score = calculateATS129(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_129_obj = calculateATS129(atsParamId, generalATSId, fileName, file);
//
//						ats_129_score = ats_129_obj.getLong("ats129_points");
//
//						storeJSONs.add(ats_129_obj);

						}

						break;

					case "ATS-130":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_130_obj = educationAtsUtil.calculateATS130(atsParamId, generalATSId,
									fileName, file);
							if (ats_130_obj != null) {
								ats_130_score = ats_130_obj.getAtsScore();
								storeJSONs.add(ats_130_obj);
							}
//						ats_130_score = calculateATS130(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_130_obj = calculateATS130(atsParamId, generalATSId, fileName, file);
//
//						ats_130_score = ats_130_obj.getLong("ats130_points");
//
//						storeJSONs.add(ats_130_obj);

						}

						break;

					case "ATS-131":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_131_obj = educationAtsUtil.calculateATS131(atsParamId, generalATSId,
									fileName, file);
							if (ats_131_obj != null) {
								ats_131_score = ats_131_obj.getAtsScore();
								storeJSONs.add(ats_131_obj);
							}
//						ats_131_score = calculateATS131(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_131_obj = calculateATS131(atsParamId, generalATSId, fileName, file);
//
//						ats_131_score = ats_131_obj.getLong("ats131_points");
//
//						storeJSONs.add(ats_131_obj);

						}
						break;

					case "ATS-132":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_132_obj = educationAtsUtil.calculateATS132(atsParamId, generalATSId,
									fileName, file);

							if (ats_132_obj != null) {
								ats_132_score = ats_132_obj.getAtsScore();
								storeJSONs.add(ats_132_obj);
							}
//						ats_132_score = calculateATS132(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_132_obj = calculateATS132(atsParamId, generalATSId, fileName, file);
//
//						ats_132_score = ats_132_obj.getLong("ats132_points");
//
//						storeJSONs.add(ats_132_obj);

						}

						break;

					case "ATS-133":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_133_obj = educationAtsUtil.calculateATS133(atsParamId, generalATSId,
									fileName, file);

							if (ats_133_obj != null) {
								ats_133_score = ats_133_obj.getAtsScore();
								storeJSONs.add(ats_133_obj);
							}
//						ats_133_score = calculateATS133(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_133_obj = calculateATS133(atsParamId, generalATSId, fileName, file);
//
//						ats_133_score = ats_133_obj.getLong("ats133_points");
//
//						storeJSONs.add(ats_133_obj);

						}

						break;

					case "ATS-134":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_134_obj = educationAtsUtil.calculateATS134(atsParamId, generalATSId,
									fileName, file);

							if (ats_134_obj != null) {
								ats_134_score = ats_134_obj.getAtsScore();
								storeJSONs.add(ats_134_obj);
							}
//						ats_134_score = calculateATS134(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_134_obj = calculateATS134(atsParamId, generalATSId, fileName, file);
//
//						ats_134_score = ats_134_obj.getLong("ats134_points");
//
//						storeJSONs.add(ats_134_obj);

						}
						break;

					case "ATS-135":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_135_obj = educationAtsUtil.calculateATS135(atsParamId, generalATSId,
									fileName, file);

							if (ats_135_obj != null) {
								ats_135_score = ats_135_obj.getAtsScore();
								storeJSONs.add(ats_135_obj);
							}
//						ats_135_score = calculateATS135(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_135_obj = calculateATS135(atsParamId, generalATSId, fileName, file);
//
//						ats_135_score = ats_135_obj.getLong("ats135_points");
//
//						storeJSONs.add(ats_135_obj);

						}

						break;

					case "ATS-136":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_136_obj = educationAtsUtil.calculateATS136(atsParamId, generalATSId,
									fileName, file);

							if (ats_136_obj != null) {
								ats_136_score = ats_136_obj.getAtsScore();
								storeJSONs.add(ats_136_obj);
							}
//						ats_136_score = calculateATS136(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_136_obj = calculateATS136(atsParamId, generalATSId, fileName, file);
//
//						ats_136_score = ats_136_obj.getLong("ats136_points");
//
//						storeJSONs.add(ats_136_obj);

						}

						break;

					case "ATS-137":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_137_obj = educationAtsUtil.calculateATS137(atsParamId, generalATSId,
									fileName, file);

							if (ats_137_obj != null) {
								ats_137_score = ats_137_obj.getAtsScore();
								storeJSONs.add(ats_137_obj);
							}
//						ats_137_score = calculateATS137(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_137_obj = calculateATS137(atsParamId, generalATSId, fileName, file);
//
//						ats_137_score = ats_137_obj.getLong("ats137_points");
//
//						storeJSONs.add(ats_137_obj);

						}

						break;

					case "ATS-138":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_138_obj = educationAtsUtil.calculateATS138(atsParamId, generalATSId,
									fileName, file);

							if (ats_138_obj != null) {
								ats_138_score = ats_138_obj.getAtsScore();
								storeJSONs.add(ats_138_obj);
							}
//						ats_138_score = calculateATS138(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_138_obj = calculateATS138(atsParamId, generalATSId, fileName, file);
//
//						ats_138_score = ats_138_obj.getLong("ats138_points");
//
//						storeJSONs.add(ats_138_obj);

						}
						break;

					case "ATS-139":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_139_obj = educationAtsUtil.calculateATS139(atsParamId, generalATSId,
									fileName, file);

							if (ats_139_obj != null) {
								ats_139_score = ats_139_obj.getAtsScore();
								storeJSONs.add(ats_139_obj);
							}
//						ats_139_score = calculateATS139(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_139_obj = calculateATS139(atsParamId, generalATSId, fileName, file);
//
//						ats_139_score = ats_139_obj.getLong("ats139_points");
//
//						storeJSONs.add(ats_139_obj);

						}

						break;

					case "ATS-140":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_140_obj = educationAtsUtil.calculateATS140(atsParamId, generalATSId,
									fileName, file);

							if (ats_140_obj != null) {
								ats_140_score = ats_140_obj.getAtsScore();
								storeJSONs.add(ats_140_obj);
							}
//						ats_140_score = calculateATS140(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_140_obj = calculateATS140(atsParamId, generalATSId, fileName, file);
//
//						ats_140_score = ats_140_obj.getLong("ats140_points");
//
//						storeJSONs.add(ats_140_obj);

						}

						break;

					default:
						System.out.println("Unhandled ATS Param: " + agp.getAtsParamId());
					}
				}
			}
		}

		System.out.println(" ATS Combined Score : " + "\nATS-121 => " + ats_121_score + "\nATS-122 => " + ats_122_score
				+ "\nATS-123 => " + ats_123_score + "\nATS-124 => " + ats_124_score + "\nATS-125 => " + ats_125_score
				+ "\nATS-126 => " + ats_126_score + "\nATS-127 => " + ats_127_score + "\nATS-128 => " + ats_128_score
				+ "\nATS-129 => " + ats_129_score + "\nATS-130 => " + ats_130_score + "\nATS-131 => " + ats_131_score
				+ "\nATS-132 => " + ats_132_score + "\nATS-133 => " + ats_133_score + "\nATS-134 => " + ats_134_score
				+ "\nATS-135 => " + ats_135_score + "\nATS-136 => " + ats_136_score + "\nATS-137 => " + ats_137_score
				+ "\nATS-138 => " + ats_138_score + "\nATS-139 => " + ats_139_score + "\nATS-140 => " + ats_140_score);

		ats_Combined_Score = ats_121_score + ats_122_score + ats_123_score + ats_124_score + ats_125_score
				+ ats_126_score + ats_127_score + ats_128_score + ats_129_score + ats_130_score + ats_131_score
				+ ats_132_score + ats_133_score + ats_134_score + ats_135_score + ats_136_score + ats_137_score
				+ ats_138_score + ats_139_score + ats_140_score;

//		combinedDto = new AtsListDto();
//		combinedGenDto = new AtsGenParamDto();

		long atsGenCombined = 141;
		ATS_General_Param_Entity genATSCombined = atsGeneralParamRepo.findByAtsGeneralId(atsGenCombined);

		combinedGenDto.setAtsGeneralId(genATSCombined.getAtsGeneralId());
		combinedGenDto.setAtsParamId(genATSCombined.getAtsParamId());
		combinedGenDto.setCategory(genATSCombined.getCategory());
		combinedGenDto.setDescription(genATSCombined.getDescription());
		combinedGenDto.setMax_points(genATSCombined.getMax_points());
		combinedGenDto.setParameter(genATSCombined.getParameter());
		combinedGenDto.setPenalty_points(genATSCombined.getPenalty_points());
		combinedGenDto.setTotal_points(genATSCombined.getTotal_points());

		combinedDto.setAtsGeneralId(genATSCombined.getAtsGeneralId());
		combinedDto.setAtsGeneralParamDto(combinedGenDto);
		combinedDto.setAtsParamData(null);
		combinedDto.setAtsParamId(genATSCombined.getAtsParamId());
		combinedDto.setAtsParamType("Total");
		combinedDto.setAtsScore(ats_Combined_Score);

		storeJSONs.add(combinedDto);

		System.out.println("storeJSONs :: " + storeJSONs);

		return storeJSONs;
	}

	public List<AtsListDto> calculateFinance_ATSScore(String fileName, MultipartFile file) {

		long ats_Combined_Score = 0;

		long ats_151_score = 0;
		long ats_152_score = 0;
		long ats_153_score = 0;
		long ats_154_score = 0;
		long ats_155_score = 0;
		long ats_156_score = 0;
		long ats_157_score = 0;
		long ats_158_score = 0;
		long ats_159_score = 0;
		long ats_160_score = 0;
		long ats_161_score = 0;
		long ats_162_score = 0;
		long ats_163_score = 0;
		long ats_164_score = 0;
		long ats_165_score = 0;
		long ats_166_score = 0;
		long ats_167_score = 0;
		long ats_168_score = 0;
		long ats_169_score = 0;
		long ats_170_score = 0;

		List<AtsListDto> storeJSONs = new ArrayList<>();

		AtsListDto combinedDto = new AtsListDto();
		AtsGenParamDto combinedGenDto = new AtsGenParamDto();

		List<Map<String, Object>> generalAllFinanceATSParams = getAllFinanceGeneralATSParams();
		List<ATS_General_Param_Entity> genATSParamAll = atsGeneralParamRepo.findAllByResumeId(101L);
//		Map<String, Object> generalATSParam = getGeneralATSParam();

		for (Map<String, Object> genATS : generalAllFinanceATSParams) {
			for (ATS_General_Param_Entity agp : genATSParamAll) {

				String atsParamId = String.valueOf(agp.getAtsParamId());
//				System.out.println("atsParamId :: " + atsParamId);

				String genATSParamId = String.valueOf(genATS.get("atsParamId"));
//				// System.out.println("atsParamId :: " + atsParamId + " <!> genATSParamId :: " + genATSParamId);

				if (atsParamId.equalsIgnoreCase(genATSParamId) || atsParamId == genATSParamId) {
					// System.out.println("atsParamId :: " + atsParamId + " <!> genATSParamId :: " +
					// genATSParamId);

					switch (atsParamId) {

					case "ATS-151":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_151_obj = financeAtsUtil.calculateATS151(atsParamId, generalATSId, fileName,
									file);

							if (ats_151_obj != null) {
								ats_151_score = ats_151_obj.getAtsScore();
								storeJSONs.add(ats_151_obj);
							}
//						JSONObject ats_151_obj = calculateATS151(atsParamId, generalATSId, fileName, file);
//						ats_151_score = ats_151_obj.getLong("ats151_points");
//						storeJSONs.add(ats_151_obj);

//						ats_151_score = calculateATS151(atsParamId, generalATSId, fileName, file);

						}
						break;

					case "ATS-152":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_152_obj = financeAtsUtil.calculateATS152(atsParamId, generalATSId, fileName,
									file);

							if (ats_152_obj != null) {
								ats_152_score = ats_152_obj.getAtsScore();
								storeJSONs.add(ats_152_obj);
							}
//						ats_152_score = calculateATS152(atsParamId, generalATSId, fileName, file);

//						JSONObject ats_152_obj = calculateATS152(atsParamId, generalATSId, fileName, file);
//
//						ats_152_score = ats_152_obj.getLong("ats152_points");
//
//						storeJSONs.add(ats_152_obj);

						}

						break;

					case "ATS-153":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_153_obj = financeAtsUtil.calculateATS153(atsParamId, generalATSId, fileName,
									file);

							if (ats_153_obj != null) {
								ats_153_score = ats_153_obj.getAtsScore();
								storeJSONs.add(ats_153_obj);
							}
//						ats_153_score = calculateATS153(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_153_obj = calculateATS153(atsParamId, generalATSId, fileName, file);
//
//						ats_153_score = ats_153_obj.getLong("ats153_points");
//
//						storeJSONs.add(ats_153_obj);
						}

						break;

					case "ATS-154":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_154_obj = financeAtsUtil.calculateATS154(atsParamId, generalATSId, fileName,
									file);

							if (ats_154_obj != null) {
								ats_154_score = ats_154_obj.getAtsScore();
								storeJSONs.add(ats_154_obj);
							}
//						ats_154_score = calculateATS154(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_154_obj = calculateATS154(atsParamId, generalATSId, fileName, file);
//
//						ats_154_score = ats_154_obj.getLong("ats154_points");
//
//						storeJSONs.add(ats_154_obj);

						}
						break;

					case "ATS-155":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_155_obj = financeAtsUtil.calculateATS155(atsParamId, generalATSId, fileName,
									file);

							if (ats_155_obj != null) {
								ats_155_score = ats_155_obj.getAtsScore();
								storeJSONs.add(ats_155_obj);
							}
//						ats_155_score = calculateATS155(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_155_obj = calculateATS155(atsParamId, generalATSId, fileName, file);
//
//						ats_155_score = ats_155_obj.getLong("ats155_points");
//
//						storeJSONs.add(ats_155_obj);

						}

						break;

					case "ATS-156":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_156_obj = financeAtsUtil.calculateATS156(atsParamId, generalATSId, fileName,
									file);

							if (ats_156_obj != null) {
								ats_156_score = ats_156_obj.getAtsScore();
								storeJSONs.add(ats_156_obj);
							}

//						ats_156_score = calculateATS156(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_156_obj = calculateATS156(atsParamId, generalATSId, fileName, file);
//
//						ats_156_score = ats_156_obj.getLong("ats156_points");
//
//						storeJSONs.add(ats_156_obj);

						}

						break;

					case "ATS-157":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_157_obj = financeAtsUtil.calculateATS157(atsParamId, generalATSId, fileName,
									file);

							if (ats_157_obj != null) {
								ats_157_score = ats_157_obj.getAtsScore();
								storeJSONs.add(ats_157_obj);
							}
//						ats_157_score = calculateATS157(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_157_obj = calculateATS157(atsParamId, generalATSId, fileName, file);
//
//						ats_157_score = ats_157_obj.getLong("ats157_points");
//
//						storeJSONs.add(ats_157_obj);

						}

						break;

					case "ATS-158":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_158_obj = financeAtsUtil.calculateATS158(atsParamId, generalATSId, fileName,
									file);

							if (ats_158_obj != null) {
								ats_158_score = ats_158_obj.getAtsScore();
								storeJSONs.add(ats_158_obj);
							}
//						ats_158_score = calculateATS158(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_158_obj = calculateATS158(atsParamId, generalATSId, fileName, file);
//
//						ats_158_score = ats_158_obj.getLong("ats158_points");
//
//						storeJSONs.add(ats_158_obj);

						}
						break;

					case "ATS-159":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_159_obj = financeAtsUtil.calculateATS159(atsParamId, generalATSId, fileName,
									file);

							if (ats_159_obj != null) {
								ats_159_score = ats_159_obj.getAtsScore();
								storeJSONs.add(ats_159_obj);
							}
//						ats_159_score = calculateATS159(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_159_obj = calculateATS159(atsParamId, generalATSId, fileName, file);
//
//						ats_159_score = ats_159_obj.getLong("ats159_points");
//
//						storeJSONs.add(ats_159_obj);

						}

						break;

					case "ATS-160":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_160_obj = financeAtsUtil.calculateATS160(atsParamId, generalATSId, fileName,
									file);
							if (ats_160_obj != null) {
								ats_160_score = ats_160_obj.getAtsScore();
								storeJSONs.add(ats_160_obj);
							}
//						ats_160_score = calculateATS160(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_160_obj = calculateATS160(atsParamId, generalATSId, fileName, file);
//
//						ats_160_score = ats_160_obj.getLong("ats160_points");
//
//						storeJSONs.add(ats_160_obj);

						}

						break;

					case "ATS-161":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_161_obj = financeAtsUtil.calculateATS161(atsParamId, generalATSId, fileName,
									file);
							if (ats_161_obj != null) {
								ats_161_score = ats_161_obj.getAtsScore();
								storeJSONs.add(ats_161_obj);
							}
//						ats_161_score = calculateATS161(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_161_obj = calculateATS161(atsParamId, generalATSId, fileName, file);
//
//						ats_161_score = ats_161_obj.getLong("ats161_points");
//
//						storeJSONs.add(ats_161_obj);

						}
						break;

					case "ATS-162":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_162_obj = financeAtsUtil.calculateATS162(atsParamId, generalATSId, fileName,
									file);

							if (ats_162_obj != null) {
								ats_162_score = ats_162_obj.getAtsScore();
								storeJSONs.add(ats_162_obj);
							}
//						ats_162_score = calculateATS162(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_162_obj = calculateATS162(atsParamId, generalATSId, fileName, file);
//
//						ats_162_score = ats_162_obj.getLong("ats162_points");
//
//						storeJSONs.add(ats_162_obj);

						}

						break;

					case "ATS-163":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_163_obj = financeAtsUtil.calculateATS163(atsParamId, generalATSId, fileName,
									file);

							if (ats_163_obj != null) {
								ats_163_score = ats_163_obj.getAtsScore();
								storeJSONs.add(ats_163_obj);
							}
//						ats_163_score = calculateATS163(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_163_obj = calculateATS163(atsParamId, generalATSId, fileName, file);
//
//						ats_163_score = ats_163_obj.getLong("ats163_points");
//
//						storeJSONs.add(ats_163_obj);

						}

						break;

					case "ATS-164":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_164_obj = financeAtsUtil.calculateATS164(atsParamId, generalATSId, fileName,
									file);

							if (ats_164_obj != null) {
								ats_164_score = ats_164_obj.getAtsScore();
								storeJSONs.add(ats_164_obj);
							}
//						ats_164_score = calculateATS164(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_164_obj = calculateATS164(atsParamId, generalATSId, fileName, file);
//
//						ats_164_score = ats_164_obj.getLong("ats164_points");
//
//						storeJSONs.add(ats_164_obj);

						}
						break;

					case "ATS-165":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_165_obj = financeAtsUtil.calculateATS165(atsParamId, generalATSId, fileName,
									file);

							if (ats_165_obj != null) {
								ats_165_score = ats_165_obj.getAtsScore();
								storeJSONs.add(ats_165_obj);
							}
//						ats_165_score = calculateATS165(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_165_obj = calculateATS165(atsParamId, generalATSId, fileName, file);
//
//						ats_165_score = ats_165_obj.getLong("ats165_points");
//
//						storeJSONs.add(ats_165_obj);

						}

						break;

					case "ATS-166":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_166_obj = financeAtsUtil.calculateATS166(atsParamId, generalATSId, fileName,
									file);

							if (ats_166_obj != null) {
								ats_166_score = ats_166_obj.getAtsScore();
								storeJSONs.add(ats_166_obj);
							}
//						ats_166_score = calculateATS166(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_166_obj = calculateATS166(atsParamId, generalATSId, fileName, file);
//
//						ats_166_score = ats_166_obj.getLong("ats166_points");
//
//						storeJSONs.add(ats_166_obj);

						}

						break;

					case "ATS-167":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_167_obj = financeAtsUtil.calculateATS167(atsParamId, generalATSId, fileName,
									file);

							if (ats_167_obj != null) {
								ats_167_score = ats_167_obj.getAtsScore();
								storeJSONs.add(ats_167_obj);
							}
//						ats_167_score = calculateATS167(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_167_obj = calculateATS167(atsParamId, generalATSId, fileName, file);
//
//						ats_167_score = ats_167_obj.getLong("ats167_points");
//
//						storeJSONs.add(ats_167_obj);

						}

						break;

					case "ATS-168":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_168_obj = financeAtsUtil.calculateATS168(atsParamId, generalATSId, fileName,
									file);

							if (ats_168_obj != null) {
								ats_168_score = ats_168_obj.getAtsScore();
								storeJSONs.add(ats_168_obj);
							}
//						ats_168_score = calculateATS168(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_168_obj = calculateATS168(atsParamId, generalATSId, fileName, file);
//
//						ats_168_score = ats_168_obj.getLong("ats168_points");
//
//						storeJSONs.add(ats_168_obj);

						}
						break;

					case "ATS-169":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_169_obj = financeAtsUtil.calculateATS169(atsParamId, generalATSId, fileName,
									file);

							if (ats_169_obj != null) {
								ats_169_score = ats_169_obj.getAtsScore();
								storeJSONs.add(ats_169_obj);
							}
//						ats_169_score = calculateATS169(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_169_obj = calculateATS169(atsParamId, generalATSId, fileName, file);
//
//						ats_169_score = ats_169_obj.getLong("ats169_points");
//
//						storeJSONs.add(ats_169_obj);

						}

						break;

					case "ATS-170":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_170_obj = financeAtsUtil.calculateATS170(atsParamId, generalATSId, fileName,
									file);

							if (ats_170_obj != null) {
								ats_170_score = ats_170_obj.getAtsScore();
								storeJSONs.add(ats_170_obj);
							}
//						ats_170_score = calculateATS170(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_170_obj = calculateATS170(atsParamId, generalATSId, fileName, file);
//
//						ats_170_score = ats_170_obj.getLong("ats170_points");
//
//						storeJSONs.add(ats_170_obj);

						}

						break;

					default:
						System.out.println("Unhandled ATS Param: " + agp.getAtsParamId());
					}
				}
			}
		}

		System.out.println(" ATS Combined Score : " + "\nATS-151 => " + ats_151_score + "\nATS-152 => " + ats_152_score
				+ "\nATS-153 => " + ats_153_score + "\nATS-154 => " + ats_154_score + "\nATS-155 => " + ats_155_score
				+ "\nATS-156 => " + ats_156_score + "\nATS-157 => " + ats_157_score + "\nATS-158 => " + ats_158_score
				+ "\nATS-159 => " + ats_159_score + "\nATS-160 => " + ats_160_score + "\nATS-161 => " + ats_161_score
				+ "\nATS-162 => " + ats_162_score + "\nATS-163 => " + ats_163_score + "\nATS-164 => " + ats_164_score
				+ "\nATS-165 => " + ats_165_score + "\nATS-166 => " + ats_166_score + "\nATS-167 => " + ats_167_score
				+ "\nATS-168 => " + ats_168_score + "\nATS-169 => " + ats_169_score + "\nATS-170 => " + ats_170_score);

		ats_Combined_Score = ats_151_score + ats_152_score + ats_153_score + ats_154_score + ats_155_score
				+ ats_156_score + ats_157_score + ats_158_score + ats_159_score + ats_160_score + ats_161_score
				+ ats_162_score + ats_163_score + ats_164_score + ats_165_score + ats_166_score + ats_167_score
				+ ats_168_score + ats_169_score + ats_170_score;

//		combinedDto = new AtsListDto();
//		combinedGenDto = new AtsGenParamDto();

		long atsGenCombined = 171;
		ATS_General_Param_Entity genATSCombined = atsGeneralParamRepo.findByAtsGeneralId(atsGenCombined);

		combinedGenDto.setAtsGeneralId(genATSCombined.getAtsGeneralId());
		combinedGenDto.setAtsParamId(genATSCombined.getAtsParamId());
		combinedGenDto.setCategory(genATSCombined.getCategory());
		combinedGenDto.setDescription(genATSCombined.getDescription());
		combinedGenDto.setMax_points(genATSCombined.getMax_points());
		combinedGenDto.setParameter(genATSCombined.getParameter());
		combinedGenDto.setPenalty_points(genATSCombined.getPenalty_points());
		combinedGenDto.setTotal_points(genATSCombined.getTotal_points());

		combinedDto.setAtsGeneralId(genATSCombined.getAtsGeneralId());
		combinedDto.setAtsGeneralParamDto(combinedGenDto);
		combinedDto.setAtsParamData(null);
		combinedDto.setAtsParamId(genATSCombined.getAtsParamId());
		combinedDto.setAtsParamType("Total");
		combinedDto.setAtsScore(ats_Combined_Score);

		storeJSONs.add(combinedDto);

		System.out.println("storeJSONs :: " + storeJSONs);

		return storeJSONs;
	}

	public List<AtsListDto> calculateBusiness_ATSScore(String fileName, MultipartFile file) {

		long ats_Combined_Score = 0;

		long ats_031_score = 0;
		long ats_032_score = 0;
		long ats_033_score = 0;
		long ats_034_score = 0;
		long ats_035_score = 0;
		long ats_036_score = 0;
		long ats_037_score = 0;
		long ats_038_score = 0;
		long ats_039_score = 0;
		long ats_040_score = 0;
		long ats_041_score = 0;
		long ats_042_score = 0;
		long ats_043_score = 0;
		long ats_044_score = 0;
		long ats_045_score = 0;
		long ats_046_score = 0;
		long ats_047_score = 0;
		long ats_048_score = 0;
		long ats_049_score = 0;
		long ats_050_score = 0;

		List<AtsListDto> storeJSONs = new ArrayList<>();

		AtsListDto combinedDto = new AtsListDto();
		AtsGenParamDto combinedGenDto = new AtsGenParamDto();

		List<Map<String, Object>> generalAllBusinessATSParams = getAllBusinessGeneralATSParams();
		List<ATS_General_Param_Entity> genATSParamAll = atsGeneralParamRepo.findAllByResumeId(105L);
//		Map<String, Object> generalATSParam = getGeneralATSParam();

		for (Map<String, Object> genATS : generalAllBusinessATSParams) {
			for (ATS_General_Param_Entity agp : genATSParamAll) {

				String atsParamId = String.valueOf(agp.getAtsParamId());
//				System.out.println("atsParamId :: " + atsParamId);

				String genATSParamId = String.valueOf(genATS.get("atsParamId"));
//				// System.out.println("atsParamId :: " + atsParamId + " <!> genATSParamId :: " + genATSParamId);

				if (atsParamId.equalsIgnoreCase(genATSParamId) || atsParamId == genATSParamId) {
					// System.out.println("atsParamId :: " + atsParamId + " <!> genATSParamId :: " +
					// genATSParamId);

					switch (atsParamId) {

					case "ATS-031":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_031_obj = businessAtsUtil.calculateATS031(atsParamId, generalATSId, fileName,
									file);

							if (ats_031_obj != null) {
								ats_031_score = ats_031_obj.getAtsScore();
								storeJSONs.add(ats_031_obj);
							}
//						JSONObject ats_031_obj = calculateATS031(atsParamId, generalATSId, fileName, file);
//						ats_031_score = ats_031_obj.getLong("ats031_points");
//						storeJSONs.add(ats_031_obj);

//						ats_031_score = calculateATS031(atsParamId, generalATSId, fileName, file);

						}
						break;

					case "ATS-032":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_032_obj = businessAtsUtil.calculateATS032(atsParamId, generalATSId, fileName,
									file);

							if (ats_032_obj != null) {
								ats_032_score = ats_032_obj.getAtsScore();
								storeJSONs.add(ats_032_obj);
							}
//						ats_032_score = calculateATS032(atsParamId, generalATSId, fileName, file);

//						JSONObject ats_032_obj = calculateATS032(atsParamId, generalATSId, fileName, file);
//
//						ats_032_score = ats_032_obj.getLong("ats032_points");
//
//						storeJSONs.add(ats_032_obj);

						}

						break;

					case "ATS-033":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_033_obj = businessAtsUtil.calculateATS033(atsParamId, generalATSId, fileName,
									file);

							if (ats_033_obj != null) {
								ats_033_score = ats_033_obj.getAtsScore();
								storeJSONs.add(ats_033_obj);
							}
//						ats_033_score = calculateATS033(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_033_obj = calculateATS033(atsParamId, generalATSId, fileName, file);
//
//						ats_033_score = ats_033_obj.getLong("ats033_points");
//
//						storeJSONs.add(ats_033_obj);
						}

						break;

					case "ATS-034":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_034_obj = businessAtsUtil.calculateATS034(atsParamId, generalATSId, fileName,
									file);

							if (ats_034_obj != null) {
								ats_034_score = ats_034_obj.getAtsScore();
								storeJSONs.add(ats_034_obj);
							}
//						ats_034_score = calculateATS034(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_034_obj = calculateATS034(atsParamId, generalATSId, fileName, file);
//
//						ats_034_score = ats_034_obj.getLong("ats034_points");
//
//						storeJSONs.add(ats_034_obj);

						}
						break;

					case "ATS-035":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_035_obj = businessAtsUtil.calculateATS035(atsParamId, generalATSId, fileName,
									file);

							if (ats_035_obj != null) {
								ats_035_score = ats_035_obj.getAtsScore();
								storeJSONs.add(ats_035_obj);
							}
//						ats_035_score = calculateATS035(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_035_obj = calculateATS035(atsParamId, generalATSId, fileName, file);
//
//						ats_035_score = ats_035_obj.getLong("ats035_points");
//
//						storeJSONs.add(ats_035_obj);

						}

						break;

					case "ATS-036":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_036_obj = businessAtsUtil.calculateATS036(atsParamId, generalATSId, fileName,
									file);

							if (ats_036_obj != null) {
								ats_036_score = ats_036_obj.getAtsScore();
								storeJSONs.add(ats_036_obj);
							}

//						ats_036_score = calculateATS036(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_036_obj = calculateATS036(atsParamId, generalATSId, fileName, file);
//
//						ats_036_score = ats_036_obj.getLong("ats036_points");
//
//						storeJSONs.add(ats_036_obj);

						}

						break;

					case "ATS-037":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_037_obj = businessAtsUtil.calculateATS037(atsParamId, generalATSId, fileName,
									file);

							if (ats_037_obj != null) {
								ats_037_score = ats_037_obj.getAtsScore();
								storeJSONs.add(ats_037_obj);
							}
//						ats_037_score = calculateATS037(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_037_obj = calculateATS037(atsParamId, generalATSId, fileName, file);
//
//						ats_037_score = ats_037_obj.getLong("ats037_points");
//
//						storeJSONs.add(ats_037_obj);

						}

						break;

					case "ATS-038":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_038_obj = businessAtsUtil.calculateATS038(atsParamId, generalATSId, fileName,
									file);

							if (ats_038_obj != null) {
								ats_038_score = ats_038_obj.getAtsScore();
								storeJSONs.add(ats_038_obj);
							}
//						ats_038_score = calculateATS038(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_038_obj = calculateATS038(atsParamId, generalATSId, fileName, file);
//
//						ats_038_score = ats_038_obj.getLong("ats038_points");
//
//						storeJSONs.add(ats_038_obj);

						}
						break;

					case "ATS-039":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_039_obj = businessAtsUtil.calculateATS039(atsParamId, generalATSId, fileName,
									file);

							if (ats_039_obj != null) {
								ats_039_score = ats_039_obj.getAtsScore();
								storeJSONs.add(ats_039_obj);
							}
//						ats_039_score = calculateATS039(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_039_obj = calculateATS039(atsParamId, generalATSId, fileName, file);
//
//						ats_039_score = ats_039_obj.getLong("ats039_points");
//
//						storeJSONs.add(ats_039_obj);

						}

						break;

					case "ATS-040":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_040_obj = businessAtsUtil.calculateATS040(atsParamId, generalATSId, fileName,
									file);
							if (ats_040_obj != null) {
								ats_040_score = ats_040_obj.getAtsScore();
								storeJSONs.add(ats_040_obj);
							}
//						ats_040_score = calculateATS040(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_040_obj = calculateATS040(atsParamId, generalATSId, fileName, file);
//
//						ats_040_score = ats_040_obj.getLong("ats040_points");
//
//						storeJSONs.add(ats_040_obj);

						}

						break;

					case "ATS-041":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_041_obj = businessAtsUtil.calculateATS041(atsParamId, generalATSId, fileName,
									file);
							if (ats_041_obj != null) {
								ats_041_score = ats_041_obj.getAtsScore();
								storeJSONs.add(ats_041_obj);
							}
//						ats_041_score = calculateATS041(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_041_obj = calculateATS041(atsParamId, generalATSId, fileName, file);
//
//						ats_041_score = ats_041_obj.getLong("ats041_points");
//
//						storeJSONs.add(ats_041_obj);

						}
						break;

					case "ATS-042":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_042_obj = businessAtsUtil.calculateATS042(atsParamId, generalATSId, fileName,
									file);

							if (ats_042_obj != null) {
								ats_042_score = ats_042_obj.getAtsScore();
								storeJSONs.add(ats_042_obj);
							}
//						ats_042_score = calculateATS042(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_042_obj = calculateATS042(atsParamId, generalATSId, fileName, file);
//
//						ats_042_score = ats_042_obj.getLong("ats042_points");
//
//						storeJSONs.add(ats_042_obj);

						}

						break;

					case "ATS-043":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_043_obj = businessAtsUtil.calculateATS043(atsParamId, generalATSId, fileName,
									file);

							if (ats_043_obj != null) {
								ats_043_score = ats_043_obj.getAtsScore();
								storeJSONs.add(ats_043_obj);
							}
//						ats_043_score = calculateATS043(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_043_obj = calculateATS043(atsParamId, generalATSId, fileName, file);
//
//						ats_043_score = ats_043_obj.getLong("ats043_points");
//
//						storeJSONs.add(ats_043_obj);

						}

						break;

					case "ATS-044":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_044_obj = businessAtsUtil.calculateATS044(atsParamId, generalATSId, fileName,
									file);

							if (ats_044_obj != null) {
								ats_044_score = ats_044_obj.getAtsScore();
								storeJSONs.add(ats_044_obj);
							}
//						ats_044_score = calculateATS044(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_044_obj = calculateATS044(atsParamId, generalATSId, fileName, file);
//
//						ats_044_score = ats_044_obj.getLong("ats044_points");
//
//						storeJSONs.add(ats_044_obj);

						}
						break;

					case "ATS-045":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_045_obj = businessAtsUtil.calculateATS045(atsParamId, generalATSId, fileName,
									file);

							if (ats_045_obj != null) {
								ats_045_score = ats_045_obj.getAtsScore();
								storeJSONs.add(ats_045_obj);
							}
//						ats_045_score = calculateATS045(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_045_obj = calculateATS045(atsParamId, generalATSId, fileName, file);
//
//						ats_045_score = ats_045_obj.getLong("ats045_points");
//
//						storeJSONs.add(ats_045_obj);

						}

						break;

					case "ATS-046":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_046_obj = businessAtsUtil.calculateATS046(atsParamId, generalATSId, fileName,
									file);

							if (ats_046_obj != null) {
								ats_046_score = ats_046_obj.getAtsScore();
								storeJSONs.add(ats_046_obj);
							}
//						ats_046_score = calculateATS046(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_046_obj = calculateATS046(atsParamId, generalATSId, fileName, file);
//
//						ats_046_score = ats_046_obj.getLong("ats046_points");
//
//						storeJSONs.add(ats_046_obj);

						}

						break;

					case "ATS-047":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_047_obj = businessAtsUtil.calculateATS047(atsParamId, generalATSId, fileName,
									file);

							if (ats_047_obj != null) {
								ats_047_score = ats_047_obj.getAtsScore();
								storeJSONs.add(ats_047_obj);
							}
//						ats_047_score = calculateATS047(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_047_obj = calculateATS047(atsParamId, generalATSId, fileName, file);
//
//						ats_047_score = ats_047_obj.getLong("ats047_points");
//
//						storeJSONs.add(ats_047_obj);

						}

						break;

					case "ATS-048":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_048_obj = businessAtsUtil.calculateATS048(atsParamId, generalATSId, fileName,
									file);

							if (ats_048_obj != null) {
								ats_048_score = ats_048_obj.getAtsScore();
								storeJSONs.add(ats_048_obj);
							}
//						ats_048_score = calculateATS048(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_048_obj = calculateATS048(atsParamId, generalATSId, fileName, file);
//
//						ats_048_score = ats_048_obj.getLong("ats048_points");
//
//						storeJSONs.add(ats_048_obj);

						}
						break;

					case "ATS-049":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_049_obj = businessAtsUtil.calculateATS049(atsParamId, generalATSId, fileName,
									file);

							if (ats_049_obj != null) {
								ats_049_score = ats_049_obj.getAtsScore();
								storeJSONs.add(ats_049_obj);
							}
//						ats_049_score = calculateATS049(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_049_obj = calculateATS049(atsParamId, generalATSId, fileName, file);
//
//						ats_049_score = ats_049_obj.getLong("ats049_points");
//
//						storeJSONs.add(ats_049_obj);

						}

						break;

					case "ATS-050":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_050_obj = businessAtsUtil.calculateATS050(atsParamId, generalATSId, fileName,
									file);

							if (ats_050_obj != null) {
								ats_050_score = ats_050_obj.getAtsScore();
								storeJSONs.add(ats_050_obj);
							}
//						ats_050_score = calculateATS050(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_050_obj = calculateATS050(atsParamId, generalATSId, fileName, file);
//
//						ats_050_score = ats_050_obj.getLong("ats050_points");
//
//						storeJSONs.add(ats_050_obj);

						}

						break;

					default:
						System.out.println("Unhandled ATS Param: " + agp.getAtsParamId());
					}
				}
			}
		}

		System.out.println(" ATS Combined Score : " + "\nATS-031 => " + ats_031_score + "\nATS-032 => " + ats_032_score
				+ "\nATS-033 => " + ats_033_score + "\nATS-034 => " + ats_034_score + "\nATS-035 => " + ats_035_score
				+ "\nATS-036 => " + ats_036_score + "\nATS-037 => " + ats_037_score + "\nATS-038 => " + ats_038_score
				+ "\nATS-039 => " + ats_039_score + "\nATS-040 => " + ats_040_score + "\nATS-041 => " + ats_041_score
				+ "\nATS-042 => " + ats_042_score + "\nATS-043 => " + ats_043_score + "\nATS-044 => " + ats_044_score
				+ "\nATS-045 => " + ats_045_score + "\nATS-046 => " + ats_046_score + "\nATS-047 => " + ats_047_score
				+ "\nATS-048 => " + ats_048_score + "\nATS-049 => " + ats_049_score + "\nATS-050 => " + ats_050_score);

		ats_Combined_Score = ats_031_score + ats_032_score + ats_033_score + ats_034_score + ats_035_score
				+ ats_036_score + ats_037_score + ats_038_score + ats_039_score + ats_040_score + ats_041_score
				+ ats_042_score + ats_043_score + ats_044_score + ats_045_score + ats_046_score + ats_047_score
				+ ats_048_score + ats_049_score + ats_050_score;

//		combinedDto = new AtsListDto();
//		combinedGenDto = new AtsGenParamDto();

		long atsGenCombined = 51;
		ATS_General_Param_Entity genATSCombined = atsGeneralParamRepo.findByAtsGeneralId(atsGenCombined);

		combinedGenDto.setAtsGeneralId(genATSCombined.getAtsGeneralId());
		combinedGenDto.setAtsParamId(genATSCombined.getAtsParamId());
		combinedGenDto.setCategory(genATSCombined.getCategory());
		combinedGenDto.setDescription(genATSCombined.getDescription());
		combinedGenDto.setMax_points(genATSCombined.getMax_points());
		combinedGenDto.setParameter(genATSCombined.getParameter());
		combinedGenDto.setPenalty_points(genATSCombined.getPenalty_points());
		combinedGenDto.setTotal_points(genATSCombined.getTotal_points());

		combinedDto.setAtsGeneralId(genATSCombined.getAtsGeneralId());
		combinedDto.setAtsGeneralParamDto(combinedGenDto);
		combinedDto.setAtsParamData(null);
		combinedDto.setAtsParamId(genATSCombined.getAtsParamId());
		combinedDto.setAtsParamType("Total");
		combinedDto.setAtsScore(ats_Combined_Score);

		storeJSONs.add(combinedDto);

		System.out.println("storeJSONs :: " + storeJSONs);

		return storeJSONs;
	}

	public List<AtsListDto> calculateTourism_ATSScore(String fileName, MultipartFile file) {

		long ats_Combined_Score = 0;

		long ats_271_score = 0;
		long ats_272_score = 0;
		long ats_273_score = 0;
		long ats_274_score = 0;
		long ats_275_score = 0;
		long ats_276_score = 0;
		long ats_277_score = 0;
		long ats_278_score = 0;
		long ats_279_score = 0;
		long ats_280_score = 0;
		long ats_281_score = 0;
		long ats_282_score = 0;
		long ats_283_score = 0;
		long ats_284_score = 0;
		long ats_285_score = 0;
		long ats_286_score = 0;
		long ats_287_score = 0;
		long ats_288_score = 0;
		long ats_289_score = 0;
		long ats_290_score = 0;

		List<AtsListDto> storeJSONs = new ArrayList<>();

		AtsListDto combinedDto = new AtsListDto();
		AtsGenParamDto combinedGenDto = new AtsGenParamDto();

		List<Map<String, Object>> generalAllTourismATSParams = getAllTourismGeneralATSParams();
		List<ATS_General_Param_Entity> genATSParamAll = atsGeneralParamRepo.findAllByResumeId(108L);
//		Map<String, Object> generalATSParam = getGeneralATSParam();

		for (Map<String, Object> genATS : generalAllTourismATSParams) {
			for (ATS_General_Param_Entity agp : genATSParamAll) {

				String atsParamId = String.valueOf(agp.getAtsParamId());
//				System.out.println("atsParamId :: " + atsParamId);

				String genATSParamId = String.valueOf(genATS.get("atsParamId"));
//				// System.out.println("atsParamId :: " + atsParamId + " <!> genATSParamId :: " + genATSParamId);4

				if (atsParamId.equalsIgnoreCase(genATSParamId) || atsParamId == genATSParamId) {
					// System.out.println("atsParamId :: " + atsParamId + " <!> genATSParamId :: " +
					// genATSParamId);

					switch (atsParamId) {

					case "ATS-271":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_271_obj = tourismAtsUtil.calculateATS271(atsParamId, generalATSId, fileName,
									file);

							if (ats_271_obj != null) {
								ats_271_score = ats_271_obj.getAtsScore();
								storeJSONs.add(ats_271_obj);
							}
//						JSONObject ats_271_obj = calculateATS271(atsParamId, generalATSId, fileName, file);
//						ats_271_score = ats_271_obj.getLong("ats271_points");
//						storeJSONs.add(ats_271_obj);

//						ats_271_score = calculateATS271(atsParamId, generalATSId, fileName, file);

						}
						break;

					case "ATS-272":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_272_obj = tourismAtsUtil.calculateATS272(atsParamId, generalATSId, fileName,
									file);

							if (ats_272_obj != null) {
								ats_272_score = ats_272_obj.getAtsScore();
								storeJSONs.add(ats_272_obj);
							}
//						ats_272_score = calculateATS272(atsParamId, generalATSId, fileName, file);

//						JSONObject ats_272_obj = calculateATS272(atsParamId, generalATSId, fileName, file);
//
//						ats_272_score = ats_272_obj.getLong("ats272_points");
//
//						storeJSONs.add(ats_272_obj);

						}

						break;

					case "ATS-273":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_273_obj = tourismAtsUtil.calculateATS273(atsParamId, generalATSId, fileName,
									file);

							if (ats_273_obj != null) {
								ats_273_score = ats_273_obj.getAtsScore();
								storeJSONs.add(ats_273_obj);
							}
//						ats_273_score = calculateATS273(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_273_obj = calculateATS273(atsParamId, generalATSId, fileName, file);
//
//						ats_273_score = ats_273_obj.getLong("ats273_points");
//
//						storeJSONs.add(ats_273_obj);
						}

						break;

					case "ATS-274":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_274_obj = tourismAtsUtil.calculateATS274(atsParamId, generalATSId, fileName,
									file);

							if (ats_274_obj != null) {
								ats_274_score = ats_274_obj.getAtsScore();
								storeJSONs.add(ats_274_obj);
							}
//						ats_274_score = calculateATS274(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_274_obj = calculateATS274(atsParamId, generalATSId, fileName, file);
//
//						ats_274_score = ats_274_obj.getLong("ats274_points");
//
//						storeJSONs.add(ats_274_obj);

						}
						break;

					case "ATS-275":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_275_obj = tourismAtsUtil.calculateATS275(atsParamId, generalATSId, fileName,
									file);

							if (ats_275_obj != null) {
								ats_275_score = ats_275_obj.getAtsScore();
								storeJSONs.add(ats_275_obj);
							}
//						ats_275_score = calculateATS275(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_275_obj = calculateATS275(atsParamId, generalATSId, fileName, file);
//
//						ats_275_score = ats_275_obj.getLong("ats275_points");
//
//						storeJSONs.add(ats_275_obj);

						}

						break;

					case "ATS-276":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_276_obj = tourismAtsUtil.calculateATS276(atsParamId, generalATSId, fileName,
									file);

							if (ats_276_obj != null) {
								ats_276_score = ats_276_obj.getAtsScore();
								storeJSONs.add(ats_276_obj);
							}

//						ats_276_score = calculateATS276(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_276_obj = calculateATS276(atsParamId, generalATSId, fileName, file);
//
//						ats_276_score = ats_276_obj.getLong("ats276_points");
//
//						storeJSONs.add(ats_276_obj);

						}

						break;

					case "ATS-277":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_277_obj = tourismAtsUtil.calculateATS277(atsParamId, generalATSId, fileName,
									file);

							if (ats_277_obj != null) {
								ats_277_score = ats_277_obj.getAtsScore();
								storeJSONs.add(ats_277_obj);
							}
//						ats_277_score = calculateATS277(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_277_obj = calculateATS277(atsParamId, generalATSId, fileName, file);
//
//						ats_277_score = ats_277_obj.getLong("ats277_points");
//
//						storeJSONs.add(ats_277_obj);

						}

						break;

					case "ATS-278":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_278_obj = tourismAtsUtil.calculateATS278(atsParamId, generalATSId, fileName,
									file);

							if (ats_278_obj != null) {
								ats_278_score = ats_278_obj.getAtsScore();
								storeJSONs.add(ats_278_obj);
							}
//						ats_278_score = calculateATS278(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_278_obj = calculateATS278(atsParamId, generalATSId, fileName, file);
//
//						ats_278_score = ats_278_obj.getLong("ats278_points");
//
//						storeJSONs.add(ats_278_obj);

						}
						break;

					case "ATS-279":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_279_obj = tourismAtsUtil.calculateATS279(atsParamId, generalATSId, fileName,
									file);

							if (ats_279_obj != null) {
								ats_279_score = ats_279_obj.getAtsScore();
								storeJSONs.add(ats_279_obj);
							}
//						ats_279_score = calculateATS279(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_279_obj = calculateATS279(atsParamId, generalATSId, fileName, file);
//
//						ats_279_score = ats_279_obj.getLong("ats279_points");
//
//						storeJSONs.add(ats_279_obj);

						}

						break;

					case "ATS-280":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_280_obj = tourismAtsUtil.calculateATS280(atsParamId, generalATSId, fileName,
									file);
							if (ats_280_obj != null) {
								ats_280_score = ats_280_obj.getAtsScore();
								storeJSONs.add(ats_280_obj);
							}
//						ats_280_score = calculateATS280(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_280_obj = calculateATS280(atsParamId, generalATSId, fileName, file);
//
//						ats_280_score = ats_280_obj.getLong("ats280_points");
//
//						storeJSONs.add(ats_280_obj);

						}

						break;

					case "ATS-281":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_281_obj = tourismAtsUtil.calculateATS281(atsParamId, generalATSId, fileName,
									file);
							if (ats_281_obj != null) {
								ats_281_score = ats_281_obj.getAtsScore();
								storeJSONs.add(ats_281_obj);
							}
//						ats_281_score = calculateATS281(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_281_obj = calculateATS281(atsParamId, generalATSId, fileName, file);
//
//						ats_281_score = ats_281_obj.getLong("ats281_points");
//
//						storeJSONs.add(ats_281_obj);

						}
						break;

					case "ATS-282":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_282_obj = tourismAtsUtil.calculateATS282(atsParamId, generalATSId, fileName,
									file);

							if (ats_282_obj != null) {
								ats_282_score = ats_282_obj.getAtsScore();
								storeJSONs.add(ats_282_obj);
							}
//						ats_282_score = calculateATS282(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_282_obj = calculateATS282(atsParamId, generalATSId, fileName, file);
//
//						ats_282_score = ats_282_obj.getLong("ats282_points");
//
//						storeJSONs.add(ats_282_obj);

						}

						break;

					case "ATS-283":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_283_obj = tourismAtsUtil.calculateATS283(atsParamId, generalATSId, fileName,
									file);

							if (ats_283_obj != null) {
								ats_283_score = ats_283_obj.getAtsScore();
								storeJSONs.add(ats_283_obj);
							}
//						ats_283_score = calculateATS283(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_283_obj = calculateATS283(atsParamId, generalATSId, fileName, file);
//
//						ats_283_score = ats_283_obj.getLong("ats283_points");
//
//						storeJSONs.add(ats_283_obj);

						}

						break;

					case "ATS-284":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_284_obj = tourismAtsUtil.calculateATS284(atsParamId, generalATSId, fileName,
									file);

							if (ats_284_obj != null) {
								ats_284_score = ats_284_obj.getAtsScore();
								storeJSONs.add(ats_284_obj);
							}
//						ats_284_score = calculateATS284(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_284_obj = calculateATS284(atsParamId, generalATSId, fileName, file);
//
//						ats_284_score = ats_284_obj.getLong("ats284_points");
//
//						storeJSONs.add(ats_284_obj);

						}
						break;

					case "ATS-285":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_285_obj = tourismAtsUtil.calculateATS285(atsParamId, generalATSId, fileName,
									file);

							if (ats_285_obj != null) {
								ats_285_score = ats_285_obj.getAtsScore();
								storeJSONs.add(ats_285_obj);
							}
//						ats_285_score = calculateATS285(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_285_obj = calculateATS285(atsParamId, generalATSId, fileName, file);
//
//						ats_285_score = ats_285_obj.getLong("ats285_points");
//
//						storeJSONs.add(ats_285_obj);

						}

						break;

					case "ATS-286":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_286_obj = tourismAtsUtil.calculateATS286(atsParamId, generalATSId, fileName,
									file);

							if (ats_286_obj != null) {
								ats_286_score = ats_286_obj.getAtsScore();
								storeJSONs.add(ats_286_obj);
							}
//						ats_286_score = calculateATS286(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_286_obj = calculateATS286(atsParamId, generalATSId, fileName, file);
//
//						ats_286_score = ats_286_obj.getLong("ats286_points");
//
//						storeJSONs.add(ats_286_obj);

						}

						break;

					case "ATS-287":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_287_obj = tourismAtsUtil.calculateATS287(atsParamId, generalATSId, fileName,
									file);

							if (ats_287_obj != null) {
								ats_287_score = ats_287_obj.getAtsScore();
								storeJSONs.add(ats_287_obj);
							}
//						ats_287_score = calculateATS287(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_287_obj = calculateATS287(atsParamId, generalATSId, fileName, file);
//
//						ats_287_score = ats_287_obj.getLong("ats287_points");
//
//						storeJSONs.add(ats_287_obj);

						}

						break;

					case "ATS-288":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_288_obj = tourismAtsUtil.calculateATS288(atsParamId, generalATSId, fileName,
									file);

							if (ats_288_obj != null) {
								ats_288_score = ats_288_obj.getAtsScore();
								storeJSONs.add(ats_288_obj);
							}
//						ats_288_score = calculateATS288(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_288_obj = calculateATS288(atsParamId, generalATSId, fileName, file);
//
//						ats_288_score = ats_288_obj.getLong("ats288_points");
//
//						storeJSONs.add(ats_288_obj);

						}
						break;

					case "ATS-289":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_289_obj = tourismAtsUtil.calculateATS289(atsParamId, generalATSId, fileName,
									file);

							if (ats_289_obj != null) {
								ats_289_score = ats_289_obj.getAtsScore();
								storeJSONs.add(ats_289_obj);
							}
//						ats_289_score = calculateATS289(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_289_obj = calculateATS289(atsParamId, generalATSId, fileName, file);
//
//						ats_289_score = ats_289_obj.getLong("ats289_points");
//
//						storeJSONs.add(ats_289_obj);

						}

						break;

					case "ATS-290":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_290_obj = tourismAtsUtil.calculateATS290(atsParamId, generalATSId, fileName,
									file);

							if (ats_290_obj != null) {
								ats_290_score = ats_290_obj.getAtsScore();
								storeJSONs.add(ats_290_obj);
							}
//						ats_290_score = calculateATS290(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_290_obj = calculateATS290(atsParamId, generalATSId, fileName, file);
//
//						ats_290_score = ats_290_obj.getLong("ats290_points");
//
//						storeJSONs.add(ats_290_obj);

						}

						break;

					default:
						System.out.println("Unhandled ATS Param: " + agp.getAtsParamId());
					}
				}
			}
		}

		System.out.println(" ATS Combined Score : " + "\nATS-271 => " + ats_271_score + "\nATS-272 => " + ats_272_score
				+ "\nATS-273 => " + ats_273_score + "\nATS-274 => " + ats_274_score + "\nATS-275 => " + ats_275_score
				+ "\nATS-276 => " + ats_276_score + "\nATS-277 => " + ats_277_score + "\nATS-278 => " + ats_278_score
				+ "\nATS-279 => " + ats_279_score + "\nATS-280 => " + ats_280_score + "\nATS-281 => " + ats_281_score
				+ "\nATS-282 => " + ats_282_score + "\nATS-283 => " + ats_283_score + "\nATS-284 => " + ats_284_score
				+ "\nATS-285 => " + ats_285_score + "\nATS-286 => " + ats_286_score + "\nATS-287 => " + ats_287_score
				+ "\nATS-288 => " + ats_288_score + "\nATS-289 => " + ats_289_score + "\nATS-290 => " + ats_290_score);

		ats_Combined_Score = ats_271_score + ats_272_score + ats_273_score + ats_274_score + ats_275_score
				+ ats_276_score + ats_277_score + ats_278_score + ats_279_score + ats_280_score + ats_281_score
				+ ats_282_score + ats_283_score + ats_284_score + ats_285_score + ats_286_score + ats_287_score
				+ ats_288_score + ats_289_score + ats_290_score;

//		combinedDto = new AtsListDto();
//		combinedGenDto = new AtsGenParamDto();

		long atsGenCombined = 291;
		ATS_General_Param_Entity genATSCombined = atsGeneralParamRepo.findByAtsGeneralId(atsGenCombined);

		combinedGenDto.setAtsGeneralId(genATSCombined.getAtsGeneralId());
		combinedGenDto.setAtsParamId(genATSCombined.getAtsParamId());
		combinedGenDto.setCategory(genATSCombined.getCategory());
		combinedGenDto.setDescription(genATSCombined.getDescription());
		combinedGenDto.setMax_points(genATSCombined.getMax_points());
		combinedGenDto.setParameter(genATSCombined.getParameter());
		combinedGenDto.setPenalty_points(genATSCombined.getPenalty_points());
		combinedGenDto.setTotal_points(genATSCombined.getTotal_points());

		combinedDto.setAtsGeneralId(genATSCombined.getAtsGeneralId());
		combinedDto.setAtsGeneralParamDto(combinedGenDto);
		combinedDto.setAtsParamData(null);
		combinedDto.setAtsParamId(genATSCombined.getAtsParamId());
		combinedDto.setAtsParamType("Total");
		combinedDto.setAtsScore(ats_Combined_Score);

		storeJSONs.add(combinedDto);

		System.out.println("storeJSONs :: " + storeJSONs);

		return storeJSONs;
	}

	public List<AtsListDto> calculateHealthcare_ATSScore(String fileName, MultipartFile file) {

		long ats_Combined_Score = 0;

		long ats_211_score = 0;
		long ats_212_score = 0;
		long ats_213_score = 0;
		long ats_214_score = 0;
		long ats_215_score = 0;
		long ats_216_score = 0;
		long ats_217_score = 0;
		long ats_218_score = 0;
		long ats_219_score = 0;
		long ats_220_score = 0;
		long ats_221_score = 0;
		long ats_222_score = 0;
		long ats_223_score = 0;
		long ats_224_score = 0;
		long ats_225_score = 0;
		long ats_226_score = 0;
		long ats_227_score = 0;
		long ats_228_score = 0;
		long ats_229_score = 0;
		long ats_230_score = 0;

		List<AtsListDto> storeJSONs = new ArrayList<>();

		AtsListDto combinedDto = new AtsListDto();
		AtsGenParamDto combinedGenDto = new AtsGenParamDto();

		List<Map<String, Object>> generalAllHealthcareATSParams = getAllHealthcareGeneralATSParams();
		List<ATS_General_Param_Entity> genATSParamAll = atsGeneralParamRepo.findAllByResumeId(103L);
//		Map<String, Object> generalATSParam = getGeneralATSParam();

		for (Map<String, Object> genATS : generalAllHealthcareATSParams) {
			for (ATS_General_Param_Entity agp : genATSParamAll) {

				String atsParamId = String.valueOf(agp.getAtsParamId());
//				System.out.println("atsParamId :: " + atsParamId);

				String genATSParamId = String.valueOf(genATS.get("atsParamId"));
//				// System.out.println("atsParamId :: " + atsParamId + " <!> genATSParamId :: " + genATSParamId);

				if (atsParamId.equalsIgnoreCase(genATSParamId) || atsParamId == genATSParamId) {
					// System.out.println("atsParamId :: " + atsParamId + " <!> genATSParamId :: " +
					// genATSParamId);

					switch (atsParamId) {

					case "ATS-211":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_211_obj = healthcareAtsUtil.calculateATS211(atsParamId, generalATSId,
									fileName, file);

							if (ats_211_obj != null) {
								ats_211_score = ats_211_obj.getAtsScore();
								storeJSONs.add(ats_211_obj);
							}
//						JSONObject ats_211_obj = calculateATS211(atsParamId, generalATSId, fileName, file);
//						ats_211_score = ats_211_obj.getLong("ats211_points");
//						storeJSONs.add(ats_211_obj);

//						ats_211_score = calculateATS211(atsParamId, generalATSId, fileName, file);

						}
						break;

					case "ATS-212":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_212_obj = healthcareAtsUtil.calculateATS212(atsParamId, generalATSId,
									fileName, file);

							if (ats_212_obj != null) {
								ats_212_score = ats_212_obj.getAtsScore();
								storeJSONs.add(ats_212_obj);
							}
//						ats_212_score = calculateATS212(atsParamId, generalATSId, fileName, file);

//						JSONObject ats_212_obj = calculateATS212(atsParamId, generalATSId, fileName, file);
//
//						ats_212_score = ats_212_obj.getLong("ats212_points");
//
//						storeJSONs.add(ats_212_obj);

						}

						break;

					case "ATS-213":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_213_obj = healthcareAtsUtil.calculateATS213(atsParamId, generalATSId,
									fileName, file);

							if (ats_213_obj != null) {
								ats_213_score = ats_213_obj.getAtsScore();
								storeJSONs.add(ats_213_obj);
							}
//						ats_213_score = calculateATS213(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_213_obj = calculateATS213(atsParamId, generalATSId, fileName, file);
//
//						ats_213_score = ats_213_obj.getLong("ats213_points");
//
//						storeJSONs.add(ats_213_obj);
						}

						break;

					case "ATS-214":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_214_obj = healthcareAtsUtil.calculateATS214(atsParamId, generalATSId,
									fileName, file);

							if (ats_214_obj != null) {
								ats_214_score = ats_214_obj.getAtsScore();
								storeJSONs.add(ats_214_obj);
							}
//						ats_214_score = calculateATS214(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_214_obj = calculateATS214(atsParamId, generalATSId, fileName, file);
//
//						ats_214_score = ats_214_obj.getLong("ats214_points");
//
//						storeJSONs.add(ats_214_obj);

						}
						break;

					case "ATS-215":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_215_obj = healthcareAtsUtil.calculateATS215(atsParamId, generalATSId,
									fileName, file);

							if (ats_215_obj != null) {
								ats_215_score = ats_215_obj.getAtsScore();
								storeJSONs.add(ats_215_obj);
							}
//						ats_215_score = calculateATS215(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_215_obj = calculateATS215(atsParamId, generalATSId, fileName, file);
//
//						ats_215_score = ats_215_obj.getLong("ats215_points");
//
//						storeJSONs.add(ats_215_obj);

						}

						break;

					case "ATS-216":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_216_obj = healthcareAtsUtil.calculateATS216(atsParamId, generalATSId,
									fileName, file);

							if (ats_216_obj != null) {
								ats_216_score = ats_216_obj.getAtsScore();
								storeJSONs.add(ats_216_obj);
							}

//						ats_216_score = calculateATS216(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_216_obj = calculateATS216(atsParamId, generalATSId, fileName, file);
//
//						ats_216_score = ats_216_obj.getLong("ats216_points");
//
//						storeJSONs.add(ats_216_obj);

						}

						break;

					case "ATS-217":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_217_obj = healthcareAtsUtil.calculateATS217(atsParamId, generalATSId,
									fileName, file);

							if (ats_217_obj != null) {
								ats_217_score = ats_217_obj.getAtsScore();
								storeJSONs.add(ats_217_obj);
							}
//						ats_217_score = calculateATS217(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_217_obj = calculateATS217(atsParamId, generalATSId, fileName, file);
//
//						ats_217_score = ats_217_obj.getLong("ats217_points");
//
//						storeJSONs.add(ats_217_obj);

						}

						break;

					case "ATS-218":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_218_obj = healthcareAtsUtil.calculateATS218(atsParamId, generalATSId,
									fileName, file);

							if (ats_218_obj != null) {
								ats_218_score = ats_218_obj.getAtsScore();
								storeJSONs.add(ats_218_obj);
							}
//						ats_218_score = calculateATS218(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_218_obj = calculateATS218(atsParamId, generalATSId, fileName, file);
//
//						ats_218_score = ats_218_obj.getLong("ats218_points");
//
//						storeJSONs.add(ats_218_obj);

						}
						break;

					case "ATS-219":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_219_obj = healthcareAtsUtil.calculateATS219(atsParamId, generalATSId,
									fileName, file);

							if (ats_219_obj != null) {
								ats_219_score = ats_219_obj.getAtsScore();
								storeJSONs.add(ats_219_obj);
							}
//						ats_219_score = calculateATS219(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_219_obj = calculateATS219(atsParamId, generalATSId, fileName, file);
//
//						ats_219_score = ats_219_obj.getLong("ats219_points");
//
//						storeJSONs.add(ats_219_obj);

						}

						break;

					case "ATS-220":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_220_obj = healthcareAtsUtil.calculateATS220(atsParamId, generalATSId,
									fileName, file);
							if (ats_220_obj != null) {
								ats_220_score = ats_220_obj.getAtsScore();
								storeJSONs.add(ats_220_obj);
							}
//						ats_220_score = calculateATS220(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_220_obj = calculateATS220(atsParamId, generalATSId, fileName, file);
//
//						ats_220_score = ats_220_obj.getLong("ats220_points");
//
//						storeJSONs.add(ats_220_obj);

						}

						break;

					case "ATS-221":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_221_obj = healthcareAtsUtil.calculateATS221(atsParamId, generalATSId,
									fileName, file);
							if (ats_221_obj != null) {
								ats_221_score = ats_221_obj.getAtsScore();
								storeJSONs.add(ats_221_obj);
							}
//						ats_221_score = calculateATS221(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_221_obj = calculateATS221(atsParamId, generalATSId, fileName, file);
//
//						ats_221_score = ats_221_obj.getLong("ats221_points");
//
//						storeJSONs.add(ats_221_obj);

						}
						break;

					case "ATS-222":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_222_obj = healthcareAtsUtil.calculateATS222(atsParamId, generalATSId,
									fileName, file);

							if (ats_222_obj != null) {
								ats_222_score = ats_222_obj.getAtsScore();
								storeJSONs.add(ats_222_obj);
							}
//						ats_222_score = calculateATS222(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_222_obj = calculateATS222(atsParamId, generalATSId, fileName, file);
//
//						ats_222_score = ats_222_obj.getLong("ats222_points");
//
//						storeJSONs.add(ats_222_obj);

						}

						break;

					case "ATS-223":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_223_obj = healthcareAtsUtil.calculateATS223(atsParamId, generalATSId,
									fileName, file);

							if (ats_223_obj != null) {
								ats_223_score = ats_223_obj.getAtsScore();
								storeJSONs.add(ats_223_obj);
							}
//						ats_223_score = calculateATS223(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_223_obj = calculateATS223(atsParamId, generalATSId, fileName, file);
//
//						ats_223_score = ats_223_obj.getLong("ats223_points");
//
//						storeJSONs.add(ats_223_obj);

						}

						break;

					case "ATS-224":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_224_obj = healthcareAtsUtil.calculateATS224(atsParamId, generalATSId,
									fileName, file);

							if (ats_224_obj != null) {
								ats_224_score = ats_224_obj.getAtsScore();
								storeJSONs.add(ats_224_obj);
							}
//						ats_224_score = calculateATS224(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_224_obj = calculateATS224(atsParamId, generalATSId, fileName, file);
//
//						ats_224_score = ats_224_obj.getLong("ats224_points");
//
//						storeJSONs.add(ats_224_obj);

						}
						break;

					case "ATS-225":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_225_obj = healthcareAtsUtil.calculateATS225(atsParamId, generalATSId,
									fileName, file);

							if (ats_225_obj != null) {
								ats_225_score = ats_225_obj.getAtsScore();
								storeJSONs.add(ats_225_obj);
							}
//						ats_225_score = calculateATS225(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_225_obj = calculateATS225(atsParamId, generalATSId, fileName, file);
//
//						ats_225_score = ats_225_obj.getLong("ats225_points");
//
//						storeJSONs.add(ats_225_obj);

						}

						break;

					case "ATS-226":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_226_obj = healthcareAtsUtil.calculateATS226(atsParamId, generalATSId,
									fileName, file);

							if (ats_226_obj != null) {
								ats_226_score = ats_226_obj.getAtsScore();
								storeJSONs.add(ats_226_obj);
							}
//						ats_226_score = calculateATS226(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_226_obj = calculateATS226(atsParamId, generalATSId, fileName, file);
//
//						ats_226_score = ats_226_obj.getLong("ats226_points");
//
//						storeJSONs.add(ats_226_obj);

						}

						break;

					case "ATS-227":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_227_obj = healthcareAtsUtil.calculateATS227(atsParamId, generalATSId,
									fileName, file);

							if (ats_227_obj != null) {
								ats_227_score = ats_227_obj.getAtsScore();
								storeJSONs.add(ats_227_obj);
							}
//						ats_227_score = calculateATS227(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_227_obj = calculateATS227(atsParamId, generalATSId, fileName, file);
//
//						ats_227_score = ats_227_obj.getLong("ats227_points");
//
//						storeJSONs.add(ats_227_obj);

						}

						break;

					case "ATS-228":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_228_obj = healthcareAtsUtil.calculateATS228(atsParamId, generalATSId,
									fileName, file);

							if (ats_228_obj != null) {
								ats_228_score = ats_228_obj.getAtsScore();
								storeJSONs.add(ats_228_obj);
							}
//						ats_228_score = calculateATS228(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_228_obj = calculateATS228(atsParamId, generalATSId, fileName, file);
//
//						ats_228_score = ats_228_obj.getLong("ats228_points");
//
//						storeJSONs.add(ats_228_obj);

						}
						break;

					case "ATS-229":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_229_obj = healthcareAtsUtil.calculateATS229(atsParamId, generalATSId,
									fileName, file);

							if (ats_229_obj != null) {
								ats_229_score = ats_229_obj.getAtsScore();
								storeJSONs.add(ats_229_obj);
							}
//						ats_229_score = calculateATS229(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_229_obj = calculateATS229(atsParamId, generalATSId, fileName, file);
//
//						ats_229_score = ats_229_obj.getLong("ats229_points");
//
//						storeJSONs.add(ats_229_obj);

						}

						break;

					case "ATS-230":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_230_obj = healthcareAtsUtil.calculateATS230(atsParamId, generalATSId,
									fileName, file);

							if (ats_230_obj != null) {
								ats_230_score = ats_230_obj.getAtsScore();
								storeJSONs.add(ats_230_obj);
							}
//						ats_230_score = calculateATS230(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_230_obj = calculateATS230(atsParamId, generalATSId, fileName, file);
//
//						ats_230_score = ats_230_obj.getLong("ats230_points");
//
//						storeJSONs.add(ats_230_obj);

						}

						break;

					default:
						System.out.println("Unhandled ATS Param: " + agp.getAtsParamId());
					}
				}
			}
		}

		System.out.println(" ATS Combined Score : " + "\nATS-211 => " + ats_211_score + "\nATS-212 => " + ats_212_score
				+ "\nATS-213 => " + ats_213_score + "\nATS-214 => " + ats_214_score + "\nATS-215 => " + ats_215_score
				+ "\nATS-216 => " + ats_216_score + "\nATS-217 => " + ats_217_score + "\nATS-218 => " + ats_218_score
				+ "\nATS-219 => " + ats_219_score + "\nATS-220 => " + ats_220_score + "\nATS-221 => " + ats_221_score
				+ "\nATS-222 => " + ats_222_score + "\nATS-223 => " + ats_223_score + "\nATS-224 => " + ats_224_score
				+ "\nATS-225 => " + ats_225_score + "\nATS-226 => " + ats_226_score + "\nATS-227 => " + ats_227_score
				+ "\nATS-228 => " + ats_228_score + "\nATS-229 => " + ats_229_score + "\nATS-230 => " + ats_230_score);

		ats_Combined_Score = ats_211_score + ats_212_score + ats_213_score + ats_214_score + ats_215_score
				+ ats_216_score + ats_217_score + ats_218_score + ats_219_score + ats_220_score + ats_221_score
				+ ats_222_score + ats_223_score + ats_224_score + ats_225_score + ats_226_score + ats_227_score
				+ ats_228_score + ats_229_score + ats_230_score;

//		combinedDto = new AtsListDto();
//		combinedGenDto = new AtsGenParamDto();

		long atsGenCombined = 231;
		ATS_General_Param_Entity genATSCombined = atsGeneralParamRepo.findByAtsGeneralId(atsGenCombined);

		combinedGenDto.setAtsGeneralId(genATSCombined.getAtsGeneralId());
		combinedGenDto.setAtsParamId(genATSCombined.getAtsParamId());
		combinedGenDto.setCategory(genATSCombined.getCategory());
		combinedGenDto.setDescription(genATSCombined.getDescription());
		combinedGenDto.setMax_points(genATSCombined.getMax_points());
		combinedGenDto.setParameter(genATSCombined.getParameter());
		combinedGenDto.setPenalty_points(genATSCombined.getPenalty_points());
		combinedGenDto.setTotal_points(genATSCombined.getTotal_points());

		combinedDto.setAtsGeneralId(genATSCombined.getAtsGeneralId());
		combinedDto.setAtsGeneralParamDto(combinedGenDto);
		combinedDto.setAtsParamData(null);
		combinedDto.setAtsParamId(genATSCombined.getAtsParamId());
		combinedDto.setAtsParamType("Total");
		combinedDto.setAtsScore(ats_Combined_Score);

		storeJSONs.add(combinedDto);

		System.out.println("storeJSONs :: " + storeJSONs);

		return storeJSONs;
	}

	public List<AtsListDto> calculateLegal_ATSScore(String fileName, MultipartFile file) {

		long ats_Combined_Score = 0;

		long ats_241_score = 0;
		long ats_242_score = 0;
		long ats_243_score = 0;
		long ats_244_score = 0;
		long ats_245_score = 0;
		long ats_246_score = 0;
		long ats_247_score = 0;
		long ats_248_score = 0;
		long ats_249_score = 0;
		long ats_250_score = 0;
		long ats_251_score = 0;
		long ats_252_score = 0;
		long ats_253_score = 0;
		long ats_254_score = 0;
		long ats_255_score = 0;
		long ats_256_score = 0;
		long ats_257_score = 0;
		long ats_258_score = 0;
		long ats_259_score = 0;
		long ats_260_score = 0;

		List<AtsListDto> storeJSONs = new ArrayList<>();

		AtsListDto combinedDto = new AtsListDto();
		AtsGenParamDto combinedGenDto = new AtsGenParamDto();

		List<Map<String, Object>> generalAllLegalATSParams = getAllLegalGeneralATSParams();
		List<ATS_General_Param_Entity> genATSParamAll = atsGeneralParamRepo.findAllByResumeId(109L);
//		Map<String, Object> generalATSParam = getGeneralATSParam();

		for (Map<String, Object> genATS : generalAllLegalATSParams) {
			for (ATS_General_Param_Entity agp : genATSParamAll) {

				String atsParamId = String.valueOf(agp.getAtsParamId());
//				System.out.println("atsParamId :: " + atsParamId);

				String genATSParamId = String.valueOf(genATS.get("atsParamId"));
//				// System.out.println("atsParamId :: " + atsParamId + " <!> genATSParamId :: " + genATSParamId);

				if (atsParamId.equalsIgnoreCase(genATSParamId) || atsParamId == genATSParamId) {
					// System.out.println("atsParamId :: " + atsParamId + " <!> genATSParamId :: " +
					// genATSParamId);

					switch (atsParamId) {

					case "ATS-241":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_241_obj = legalAtsUtil.calculateATS241(atsParamId, generalATSId, fileName,
									file);

							if (ats_241_obj != null) {
								ats_241_score = ats_241_obj.getAtsScore();
								storeJSONs.add(ats_241_obj);
							}
//						JSONObject ats_241_obj = calculateATS241(atsParamId, generalATSId, fileName, file);
//						ats_241_score = ats_241_obj.getLong("ats241_points");
//						storeJSONs.add(ats_241_obj);

//						ats_241_score = calculateATS241(atsParamId, generalATSId, fileName, file);

						}
						break;

					case "ATS-242":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_242_obj = legalAtsUtil.calculateATS242(atsParamId, generalATSId, fileName,
									file);

							if (ats_242_obj != null) {
								ats_242_score = ats_242_obj.getAtsScore();
								storeJSONs.add(ats_242_obj);
							}
//						ats_242_score = calculateATS242(atsParamId, generalATSId, fileName, file);

//						JSONObject ats_242_obj = calculateATS242(atsParamId, generalATSId, fileName, file);
//
//						ats_242_score = ats_242_obj.getLong("ats242_points");
//
//						storeJSONs.add(ats_242_obj);

						}

						break;

					case "ATS-243":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_243_obj = legalAtsUtil.calculateATS243(atsParamId, generalATSId, fileName,
									file);

							if (ats_243_obj != null) {
								ats_243_score = ats_243_obj.getAtsScore();
								storeJSONs.add(ats_243_obj);
							}
//						ats_243_score = calculateATS243(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_243_obj = calculateATS243(atsParamId, generalATSId, fileName, file);
//
//						ats_243_score = ats_243_obj.getLong("ats243_points");
//
//						storeJSONs.add(ats_243_obj);
						}

						break;

					case "ATS-244":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_244_obj = legalAtsUtil.calculateATS244(atsParamId, generalATSId, fileName,
									file);

							if (ats_244_obj != null) {
								ats_244_score = ats_244_obj.getAtsScore();
								storeJSONs.add(ats_244_obj);
							}
//						ats_244_score = calculateATS244(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_244_obj = calculateATS244(atsParamId, generalATSId, fileName, file);
//
//						ats_244_score = ats_244_obj.getLong("ats244_points");
//
//						storeJSONs.add(ats_244_obj);

						}
						break;

					case "ATS-245":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_245_obj = legalAtsUtil.calculateATS245(atsParamId, generalATSId, fileName,
									file);

							if (ats_245_obj != null) {
								ats_245_score = ats_245_obj.getAtsScore();
								storeJSONs.add(ats_245_obj);
							}
//						ats_245_score = calculateATS245(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_245_obj = calculateATS245(atsParamId, generalATSId, fileName, file);
//
//						ats_245_score = ats_245_obj.getLong("ats245_points");
//
//						storeJSONs.add(ats_245_obj);

						}

						break;

					case "ATS-246":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_246_obj = legalAtsUtil.calculateATS246(atsParamId, generalATSId, fileName,
									file);

							if (ats_246_obj != null) {
								ats_246_score = ats_246_obj.getAtsScore();
								storeJSONs.add(ats_246_obj);
							}

//						ats_246_score = calculateATS246(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_246_obj = calculateATS246(atsParamId, generalATSId, fileName, file);
//
//						ats_246_score = ats_246_obj.getLong("ats246_points");
//
//						storeJSONs.add(ats_246_obj);

						}

						break;

					case "ATS-247":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_247_obj = legalAtsUtil.calculateATS247(atsParamId, generalATSId, fileName,
									file);

							if (ats_247_obj != null) {
								ats_247_score = ats_247_obj.getAtsScore();
								storeJSONs.add(ats_247_obj);
							}
//						ats_247_score = calculateATS247(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_247_obj = calculateATS247(atsParamId, generalATSId, fileName, file);
//
//						ats_247_score = ats_247_obj.getLong("ats247_points");
//
//						storeJSONs.add(ats_247_obj);

						}

						break;

					case "ATS-248":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_248_obj = legalAtsUtil.calculateATS248(atsParamId, generalATSId, fileName,
									file);

							if (ats_248_obj != null) {
								ats_248_score = ats_248_obj.getAtsScore();
								storeJSONs.add(ats_248_obj);
							}
//						ats_248_score = calculateATS248(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_248_obj = calculateATS248(atsParamId, generalATSId, fileName, file);
//
//						ats_248_score = ats_248_obj.getLong("ats248_points");
//
//						storeJSONs.add(ats_248_obj);

						}
						break;

					case "ATS-249":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_249_obj = legalAtsUtil.calculateATS249(atsParamId, generalATSId, fileName,
									file);

							if (ats_249_obj != null) {
								ats_249_score = ats_249_obj.getAtsScore();
								storeJSONs.add(ats_249_obj);
							}
//						ats_249_score = calculateATS249(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_249_obj = calculateATS249(atsParamId, generalATSId, fileName, file);
//
//						ats_249_score = ats_249_obj.getLong("ats249_points");
//
//						storeJSONs.add(ats_249_obj);

						}

						break;

					case "ATS-250":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_250_obj = legalAtsUtil.calculateATS250(atsParamId, generalATSId, fileName,
									file);
							if (ats_250_obj != null) {
								ats_250_score = ats_250_obj.getAtsScore();
								storeJSONs.add(ats_250_obj);
							}
//						ats_250_score = calculateATS250(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_250_obj = calculateATS250(atsParamId, generalATSId, fileName, file);
//
//						ats_250_score = ats_250_obj.getLong("ats250_points");
//
//						storeJSONs.add(ats_250_obj);

						}

						break;

					case "ATS-251":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_251_obj = legalAtsUtil.calculateATS251(atsParamId, generalATSId, fileName,
									file);
							if (ats_251_obj != null) {
								ats_251_score = ats_251_obj.getAtsScore();
								storeJSONs.add(ats_251_obj);
							}
//						ats_251_score = calculateATS251(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_251_obj = calculateATS251(atsParamId, generalATSId, fileName, file);
//
//						ats_251_score = ats_251_obj.getLong("ats251_points");
//
//						storeJSONs.add(ats_251_obj);

						}
						break;

					case "ATS-252":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_252_obj = legalAtsUtil.calculateATS252(atsParamId, generalATSId, fileName,
									file);

							if (ats_252_obj != null) {
								ats_252_score = ats_252_obj.getAtsScore();
								storeJSONs.add(ats_252_obj);
							}
//						ats_252_score = calculateATS252(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_252_obj = calculateATS252(atsParamId, generalATSId, fileName, file);
//
//						ats_252_score = ats_252_obj.getLong("ats252_points");
//
//						storeJSONs.add(ats_252_obj);

						}

						break;

					case "ATS-253":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_253_obj = legalAtsUtil.calculateATS253(atsParamId, generalATSId, fileName,
									file);

							if (ats_253_obj != null) {
								ats_253_score = ats_253_obj.getAtsScore();
								storeJSONs.add(ats_253_obj);
							}
//						ats_253_score = calculateATS253(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_253_obj = calculateATS253(atsParamId, generalATSId, fileName, file);
//
//						ats_253_score = ats_253_obj.getLong("ats253_points");
//
//						storeJSONs.add(ats_253_obj);

						}

						break;

					case "ATS-254":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_254_obj = legalAtsUtil.calculateATS254(atsParamId, generalATSId, fileName,
									file);

							if (ats_254_obj != null) {
								ats_254_score = ats_254_obj.getAtsScore();
								storeJSONs.add(ats_254_obj);
							}
//						ats_254_score = calculateATS254(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_254_obj = calculateATS254(atsParamId, generalATSId, fileName, file);
//
//						ats_254_score = ats_254_obj.getLong("ats254_points");
//
//						storeJSONs.add(ats_254_obj);

						}
						break;

					case "ATS-255":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_255_obj = legalAtsUtil.calculateATS255(atsParamId, generalATSId, fileName,
									file);

							if (ats_255_obj != null) {
								ats_255_score = ats_255_obj.getAtsScore();
								storeJSONs.add(ats_255_obj);
							}
//						ats_255_score = calculateATS255(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_255_obj = calculateATS255(atsParamId, generalATSId, fileName, file);
//
//						ats_255_score = ats_255_obj.getLong("ats255_points");
//
//						storeJSONs.add(ats_255_obj);

						}

						break;

					case "ATS-256":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_256_obj = legalAtsUtil.calculateATS256(atsParamId, generalATSId, fileName,
									file);

							if (ats_256_obj != null) {
								ats_256_score = ats_256_obj.getAtsScore();
								storeJSONs.add(ats_256_obj);
							}
//						ats_256_score = calculateATS256(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_256_obj = calculateATS256(atsParamId, generalATSId, fileName, file);
//
//						ats_256_score = ats_256_obj.getLong("ats256_points");
//
//						storeJSONs.add(ats_256_obj);

						}

						break;

					case "ATS-257":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_257_obj = legalAtsUtil.calculateATS257(atsParamId, generalATSId, fileName,
									file);

							if (ats_257_obj != null) {
								ats_257_score = ats_257_obj.getAtsScore();
								storeJSONs.add(ats_257_obj);
							}
//						ats_257_score = calculateATS257(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_257_obj = calculateATS257(atsParamId, generalATSId, fileName, file);
//
//						ats_257_score = ats_257_obj.getLong("ats257_points");
//
//						storeJSONs.add(ats_257_obj);

						}

						break;

					case "ATS-258":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_258_obj = legalAtsUtil.calculateATS258(atsParamId, generalATSId, fileName,
									file);

							if (ats_258_obj != null) {
								ats_258_score = ats_258_obj.getAtsScore();
								storeJSONs.add(ats_258_obj);
							}
//						ats_258_score = calculateATS258(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_258_obj = calculateATS258(atsParamId, generalATSId, fileName, file);
//
//						ats_258_score = ats_258_obj.getLong("ats258_points");
//
//						storeJSONs.add(ats_258_obj);

						}
						break;

					case "ATS-259":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_259_obj = legalAtsUtil.calculateATS259(atsParamId, generalATSId, fileName,
									file);

							if (ats_259_obj != null) {
								ats_259_score = ats_259_obj.getAtsScore();
								storeJSONs.add(ats_259_obj);
							}
//						ats_259_score = calculateATS259(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_259_obj = calculateATS259(atsParamId, generalATSId, fileName, file);
//
//						ats_259_score = ats_259_obj.getLong("ats259_points");
//
//						storeJSONs.add(ats_259_obj);

						}

						break;

					case "ATS-260":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_260_obj = legalAtsUtil.calculateATS260(atsParamId, generalATSId, fileName,
									file);

							if (ats_260_obj != null) {
								ats_260_score = ats_260_obj.getAtsScore();
								storeJSONs.add(ats_260_obj);
							}
//						ats_260_score = calculateATS260(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_260_obj = calculateATS260(atsParamId, generalATSId, fileName, file);
//
//						ats_260_score = ats_260_obj.getLong("ats260_points");
//
//						storeJSONs.add(ats_260_obj);

						}

						break;

					default:
						System.out.println("Unhandled ATS Param: " + agp.getAtsParamId());
					}
				}
			}
		}

		System.out.println(" ATS Combined Score : " + "\nATS-241 => " + ats_241_score + "\nATS-242 => " + ats_242_score
				+ "\nATS-243 => " + ats_243_score + "\nATS-244 => " + ats_244_score + "\nATS-245 => " + ats_245_score
				+ "\nATS-246 => " + ats_246_score + "\nATS-247 => " + ats_247_score + "\nATS-248 => " + ats_248_score
				+ "\nATS-249 => " + ats_249_score + "\nATS-250 => " + ats_250_score + "\nATS-251 => " + ats_251_score
				+ "\nATS-252 => " + ats_252_score + "\nATS-253 => " + ats_253_score + "\nATS-254 => " + ats_254_score
				+ "\nATS-255 => " + ats_255_score + "\nATS-256 => " + ats_256_score + "\nATS-257 => " + ats_257_score
				+ "\nATS-258 => " + ats_258_score + "\nATS-259 => " + ats_259_score + "\nATS-260 => " + ats_260_score);

		ats_Combined_Score = ats_241_score + ats_242_score + ats_243_score + ats_244_score + ats_245_score
				+ ats_246_score + ats_247_score + ats_248_score + ats_249_score + ats_250_score + ats_251_score
				+ ats_252_score + ats_253_score + ats_254_score + ats_255_score + ats_256_score + ats_257_score
				+ ats_258_score + ats_259_score + ats_260_score;

//		combinedDto = new AtsListDto();
//		combinedGenDto = new AtsGenParamDto();

		long atsGenCombined = 261;
		ATS_General_Param_Entity genATSCombined = atsGeneralParamRepo.findByAtsGeneralId(atsGenCombined);

		combinedGenDto.setAtsGeneralId(genATSCombined.getAtsGeneralId());
		combinedGenDto.setAtsParamId(genATSCombined.getAtsParamId());
		combinedGenDto.setCategory(genATSCombined.getCategory());
		combinedGenDto.setDescription(genATSCombined.getDescription());
		combinedGenDto.setMax_points(genATSCombined.getMax_points());
		combinedGenDto.setParameter(genATSCombined.getParameter());
		combinedGenDto.setPenalty_points(genATSCombined.getPenalty_points());
		combinedGenDto.setTotal_points(genATSCombined.getTotal_points());

		combinedDto.setAtsGeneralId(genATSCombined.getAtsGeneralId());
		combinedDto.setAtsGeneralParamDto(combinedGenDto);
		combinedDto.setAtsParamData(null);
		combinedDto.setAtsParamId(genATSCombined.getAtsParamId());
		combinedDto.setAtsParamType("Total");
		combinedDto.setAtsScore(ats_Combined_Score);

		storeJSONs.add(combinedDto);

		System.out.println("storeJSONs :: " + storeJSONs);

		return storeJSONs;
	}

	public List<AtsListDto> calculateGovernment_ATSScore(String fileName, MultipartFile file) {

		long ats_Combined_Score = 0;

		long ats_181_score = 0;
		long ats_182_score = 0;
		long ats_183_score = 0;
		long ats_184_score = 0;
		long ats_185_score = 0;
		long ats_186_score = 0;
		long ats_187_score = 0;
		long ats_188_score = 0;
		long ats_189_score = 0;
		long ats_190_score = 0;
		long ats_191_score = 0;
		long ats_192_score = 0;
		long ats_193_score = 0;
		long ats_194_score = 0;
		long ats_195_score = 0;
		long ats_196_score = 0;
		long ats_197_score = 0;
		long ats_198_score = 0;
		long ats_199_score = 0;
		long ats_200_score = 0;

		List<AtsListDto> storeJSONs = new ArrayList<>();

		AtsListDto combinedDto = new AtsListDto();
		AtsGenParamDto combinedGenDto = new AtsGenParamDto();

		List<Map<String, Object>> generalAllGovernmentATSParams = getAllGovernmentGeneralATSParams();
		List<ATS_General_Param_Entity> genATSParamAll = atsGeneralParamRepo.findAllByResumeId(107L);
//		Map<String, Object> generalATSParam = getGeneralATSParam();

		for (Map<String, Object> genATS : generalAllGovernmentATSParams) {
			for (ATS_General_Param_Entity agp : genATSParamAll) {

				String atsParamId = String.valueOf(agp.getAtsParamId());
//				System.out.println("atsParamId :: " + atsParamId);

				String genATSParamId = String.valueOf(genATS.get("atsParamId"));
//				// System.out.println("atsParamId :: " + atsParamId + " <!> genATSParamId :: " + genATSParamId);

				if (atsParamId.equalsIgnoreCase(genATSParamId) || atsParamId == genATSParamId) {
					// System.out.println("atsParamId :: " + atsParamId + " <!> genATSParamId :: " +
					// genATSParamId);

					switch (atsParamId) {

					case "ATS-181":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_181_obj = governmentAtsUtil.calculateATS181(atsParamId, generalATSId,
									fileName, file);

							if (ats_181_obj != null) {
								ats_181_score = ats_181_obj.getAtsScore();
								storeJSONs.add(ats_181_obj);
							}
//						JSONObject ats_181_obj = calculateATS181(atsParamId, generalATSId, fileName, file);
//						ats_181_score = ats_181_obj.getLong("ats181_points");
//						storeJSONs.add(ats_181_obj);

//						ats_181_score = calculateATS181(atsParamId, generalATSId, fileName, file);

						}
						break;

					case "ATS-182":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_182_obj = governmentAtsUtil.calculateATS182(atsParamId, generalATSId,
									fileName, file);

							if (ats_182_obj != null) {
								ats_182_score = ats_182_obj.getAtsScore();
								storeJSONs.add(ats_182_obj);
							}
//						ats_182_score = calculateATS182(atsParamId, generalATSId, fileName, file);

//						JSONObject ats_182_obj = calculateATS182(atsParamId, generalATSId, fileName, file);
//
//						ats_182_score = ats_182_obj.getLong("ats182_points");
//
//						storeJSONs.add(ats_182_obj);

						}

						break;

					case "ATS-183":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_183_obj = governmentAtsUtil.calculateATS183(atsParamId, generalATSId,
									fileName, file);

							if (ats_183_obj != null) {
								ats_183_score = ats_183_obj.getAtsScore();
								storeJSONs.add(ats_183_obj);
							}
//						ats_183_score = calculateATS183(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_183_obj = calculateATS183(atsParamId, generalATSId, fileName, file);
//
//						ats_183_score = ats_183_obj.getLong("ats183_points");
//
//						storeJSONs.add(ats_183_obj);
						}

						break;

					case "ATS-184":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_184_obj = governmentAtsUtil.calculateATS184(atsParamId, generalATSId,
									fileName, file);

							if (ats_184_obj != null) {
								ats_184_score = ats_184_obj.getAtsScore();
								storeJSONs.add(ats_184_obj);
							}
//						ats_184_score = calculateATS184(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_184_obj = calculateATS184(atsParamId, generalATSId, fileName, file);
//
//						ats_184_score = ats_184_obj.getLong("ats184_points");
//
//						storeJSONs.add(ats_184_obj);

						}
						break;

					case "ATS-185":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_185_obj = governmentAtsUtil.calculateATS185(atsParamId, generalATSId,
									fileName, file);

							if (ats_185_obj != null) {
								ats_185_score = ats_185_obj.getAtsScore();
								storeJSONs.add(ats_185_obj);
							}
//						ats_185_score = calculateATS185(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_185_obj = calculateATS185(atsParamId, generalATSId, fileName, file);
//
//						ats_185_score = ats_185_obj.getLong("ats185_points");
//
//						storeJSONs.add(ats_185_obj);

						}

						break;

					case "ATS-186":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_186_obj = governmentAtsUtil.calculateATS186(atsParamId, generalATSId,
									fileName, file);

							if (ats_186_obj != null) {
								ats_186_score = ats_186_obj.getAtsScore();
								storeJSONs.add(ats_186_obj);
							}

//						ats_186_score = calculateATS186(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_186_obj = calculateATS186(atsParamId, generalATSId, fileName, file);
//
//						ats_186_score = ats_186_obj.getLong("ats186_points");
//
//						storeJSONs.add(ats_186_obj);

						}

						break;

					case "ATS-187":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_187_obj = governmentAtsUtil.calculateATS187(atsParamId, generalATSId,
									fileName, file);

							if (ats_187_obj != null) {
								ats_187_score = ats_187_obj.getAtsScore();
								storeJSONs.add(ats_187_obj);
							}
//						ats_187_score = calculateATS187(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_187_obj = calculateATS187(atsParamId, generalATSId, fileName, file);
//
//						ats_187_score = ats_187_obj.getLong("ats187_points");
//
//						storeJSONs.add(ats_187_obj);

						}

						break;

					case "ATS-188":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_188_obj = governmentAtsUtil.calculateATS188(atsParamId, generalATSId,
									fileName, file);

							if (ats_188_obj != null) {
								ats_188_score = ats_188_obj.getAtsScore();
								storeJSONs.add(ats_188_obj);
							}
//						ats_188_score = calculateATS188(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_188_obj = calculateATS188(atsParamId, generalATSId, fileName, file);
//
//						ats_188_score = ats_188_obj.getLong("ats188_points");
//
//						storeJSONs.add(ats_188_obj);

						}
						break;

					case "ATS-189":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_189_obj = governmentAtsUtil.calculateATS189(atsParamId, generalATSId,
									fileName, file);

							if (ats_189_obj != null) {
								ats_189_score = ats_189_obj.getAtsScore();
								storeJSONs.add(ats_189_obj);
							}
//						ats_189_score = calculateATS189(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_189_obj = calculateATS189(atsParamId, generalATSId, fileName, file);
//
//						ats_189_score = ats_189_obj.getLong("ats189_points");
//
//						storeJSONs.add(ats_189_obj);

						}

						break;

					case "ATS-190":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_190_obj = governmentAtsUtil.calculateATS190(atsParamId, generalATSId,
									fileName, file);
							if (ats_190_obj != null) {
								ats_190_score = ats_190_obj.getAtsScore();
								storeJSONs.add(ats_190_obj);
							}
//						ats_190_score = calculateATS190(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_190_obj = calculateATS190(atsParamId, generalATSId, fileName, file);
//
//						ats_190_score = ats_190_obj.getLong("ats190_points");
//
//						storeJSONs.add(ats_190_obj);

						}

						break;

					case "ATS-191":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_191_obj = governmentAtsUtil.calculateATS191(atsParamId, generalATSId,
									fileName, file);
							if (ats_191_obj != null) {
								ats_191_score = ats_191_obj.getAtsScore();
								storeJSONs.add(ats_191_obj);
							}
//						ats_191_score = calculateATS191(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_191_obj = calculateATS191(atsParamId, generalATSId, fileName, file);
//
//						ats_191_score = ats_191_obj.getLong("ats191_points");
//
//						storeJSONs.add(ats_191_obj);

						}
						break;

					case "ATS-192":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_192_obj = governmentAtsUtil.calculateATS192(atsParamId, generalATSId,
									fileName, file);

							if (ats_192_obj != null) {
								ats_192_score = ats_192_obj.getAtsScore();
								storeJSONs.add(ats_192_obj);
							}
//						ats_192_score = calculateATS192(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_192_obj = calculateATS192(atsParamId, generalATSId, fileName, file);
//
//						ats_192_score = ats_192_obj.getLong("ats192_points");
//
//						storeJSONs.add(ats_192_obj);

						}

						break;

					case "ATS-193":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_193_obj = governmentAtsUtil.calculateATS193(atsParamId, generalATSId,
									fileName, file);

							if (ats_193_obj != null) {
								ats_193_score = ats_193_obj.getAtsScore();
								storeJSONs.add(ats_193_obj);
							}
//						ats_193_score = calculateATS193(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_193_obj = calculateATS193(atsParamId, generalATSId, fileName, file);
//
//						ats_193_score = ats_193_obj.getLong("ats193_points");
//
//						storeJSONs.add(ats_193_obj);

						}

						break;

					case "ATS-194":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_194_obj = governmentAtsUtil.calculateATS194(atsParamId, generalATSId,
									fileName, file);

							if (ats_194_obj != null) {
								ats_194_score = ats_194_obj.getAtsScore();
								storeJSONs.add(ats_194_obj);
							}
//						ats_194_score = calculateATS194(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_194_obj = calculateATS194(atsParamId, generalATSId, fileName, file);
//
//						ats_194_score = ats_194_obj.getLong("ats194_points");
//
//						storeJSONs.add(ats_194_obj);

						}
						break;

					case "ATS-195":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_195_obj = governmentAtsUtil.calculateATS195(atsParamId, generalATSId,
									fileName, file);

							if (ats_195_obj != null) {
								ats_195_score = ats_195_obj.getAtsScore();
								storeJSONs.add(ats_195_obj);
							}
//						ats_195_score = calculateATS195(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_195_obj = calculateATS195(atsParamId, generalATSId, fileName, file);
//
//						ats_195_score = ats_195_obj.getLong("ats195_points");
//
//						storeJSONs.add(ats_195_obj);

						}

						break;

					case "ATS-196":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_196_obj = governmentAtsUtil.calculateATS196(atsParamId, generalATSId,
									fileName, file);

							if (ats_196_obj != null) {
								ats_196_score = ats_196_obj.getAtsScore();
								storeJSONs.add(ats_196_obj);
							}
//						ats_196_score = calculateATS196(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_196_obj = calculateATS196(atsParamId, generalATSId, fileName, file);
//
//						ats_196_score = ats_196_obj.getLong("ats196_points");
//
//						storeJSONs.add(ats_196_obj);

						}

						break;

					case "ATS-197":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_197_obj = governmentAtsUtil.calculateATS197(atsParamId, generalATSId,
									fileName, file);

							if (ats_197_obj != null) {
								ats_197_score = ats_197_obj.getAtsScore();
								storeJSONs.add(ats_197_obj);
							}
//						ats_197_score = calculateATS197(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_197_obj = calculateATS197(atsParamId, generalATSId, fileName, file);
//
//						ats_197_score = ats_197_obj.getLong("ats197_points");
//
//						storeJSONs.add(ats_197_obj);

						}

						break;

					case "ATS-198":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_198_obj = governmentAtsUtil.calculateATS198(atsParamId, generalATSId,
									fileName, file);

							if (ats_198_obj != null) {
								ats_198_score = ats_198_obj.getAtsScore();
								storeJSONs.add(ats_198_obj);
							}
//						ats_198_score = calculateATS198(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_198_obj = calculateATS198(atsParamId, generalATSId, fileName, file);
//
//						ats_198_score = ats_198_obj.getLong("ats198_points");
//
//						storeJSONs.add(ats_198_obj);

						}
						break;

					case "ATS-199":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_199_obj = governmentAtsUtil.calculateATS199(atsParamId, generalATSId,
									fileName, file);

							if (ats_199_obj != null) {
								ats_199_score = ats_199_obj.getAtsScore();
								storeJSONs.add(ats_199_obj);
							}
//						ats_199_score = calculateATS199(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_199_obj = calculateATS199(atsParamId, generalATSId, fileName, file);
//
//						ats_199_score = ats_199_obj.getLong("ats199_points");
//
//						storeJSONs.add(ats_199_obj);

						}

						break;

					case "ATS-200":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_200_obj = governmentAtsUtil.calculateATS200(atsParamId, generalATSId,
									fileName, file);

							if (ats_200_obj != null) {
								ats_200_score = ats_200_obj.getAtsScore();
								storeJSONs.add(ats_200_obj);
							}
//						ats_200_score = calculateATS200(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_200_obj = calculateATS200(atsParamId, generalATSId, fileName, file);
//
//						ats_200_score = ats_200_obj.getLong("ats200_points");
//
//						storeJSONs.add(ats_200_obj);

						}

						break;

					default:
						System.out.println("Unhandled ATS Param: " + agp.getAtsParamId());
					}
				}
			}
		}

		System.out.println(" ATS Combined Score : " + "\nATS-181 => " + ats_181_score + "\nATS-182 => " + ats_182_score
				+ "\nATS-183 => " + ats_183_score + "\nATS-184 => " + ats_184_score + "\nATS-185 => " + ats_185_score
				+ "\nATS-186 => " + ats_186_score + "\nATS-187 => " + ats_187_score + "\nATS-188 => " + ats_188_score
				+ "\nATS-189 => " + ats_189_score + "\nATS-190 => " + ats_190_score + "\nATS-191 => " + ats_191_score
				+ "\nATS-192 => " + ats_192_score + "\nATS-193 => " + ats_193_score + "\nATS-194 => " + ats_194_score
				+ "\nATS-195 => " + ats_195_score + "\nATS-196 => " + ats_196_score + "\nATS-197 => " + ats_197_score
				+ "\nATS-198 => " + ats_198_score + "\nATS-199 => " + ats_199_score + "\nATS-200 => " + ats_200_score);

		ats_Combined_Score = ats_181_score + ats_182_score + ats_183_score + ats_184_score + ats_185_score
				+ ats_186_score + ats_187_score + ats_188_score + ats_189_score + ats_190_score + ats_191_score
				+ ats_192_score + ats_193_score + ats_194_score + ats_195_score + ats_196_score + ats_197_score
				+ ats_198_score + ats_199_score + ats_200_score;

//		combinedDto = new AtsListDto();
//		combinedGenDto = new AtsGenParamDto();

		long atsGenCombined = 201;
		ATS_General_Param_Entity genATSCombined = atsGeneralParamRepo.findByAtsGeneralId(atsGenCombined);

		combinedGenDto.setAtsGeneralId(genATSCombined.getAtsGeneralId());
		combinedGenDto.setAtsParamId(genATSCombined.getAtsParamId());
		combinedGenDto.setCategory(genATSCombined.getCategory());
		combinedGenDto.setDescription(genATSCombined.getDescription());
		combinedGenDto.setMax_points(genATSCombined.getMax_points());
		combinedGenDto.setParameter(genATSCombined.getParameter());
		combinedGenDto.setPenalty_points(genATSCombined.getPenalty_points());
		combinedGenDto.setTotal_points(genATSCombined.getTotal_points());

		combinedDto.setAtsGeneralId(genATSCombined.getAtsGeneralId());
		combinedDto.setAtsGeneralParamDto(combinedGenDto);
		combinedDto.setAtsParamData(null);
		combinedDto.setAtsParamId(genATSCombined.getAtsParamId());
		combinedDto.setAtsParamType("Total");
		combinedDto.setAtsScore(ats_Combined_Score);

		storeJSONs.add(combinedDto);

		System.out.println("storeJSONs :: " + storeJSONs);

		return storeJSONs;
	}

	public List<AtsListDto> calculateDesign_ATSScore(String fileName, MultipartFile file) {

		long ats_Combined_Score = 0;

		long ats_091_score = 0;
		long ats_092_score = 0;
		long ats_093_score = 0;
		long ats_094_score = 0;
		long ats_095_score = 0;
		long ats_096_score = 0;
		long ats_097_score = 0;
		long ats_098_score = 0;
		long ats_099_score = 0;
		long ats_100_score = 0;
		long ats_101_score = 0;
		long ats_111_score = 0;
		long ats_103_score = 0;
		long ats_104_score = 0;
		long ats_105_score = 0;
		long ats_106_score = 0;
		long ats_107_score = 0;
		long ats_108_score = 0;
		long ats_109_score = 0;
		long ats_110_score = 0;

		List<AtsListDto> storeJSONs = new ArrayList<>();

		AtsListDto combinedDto = new AtsListDto();
		AtsGenParamDto combinedGenDto = new AtsGenParamDto();

		List<Map<String, Object>> generalAllDesignATSParams = getAllDesignGeneralATSParams();
		List<ATS_General_Param_Entity> genATSParamAll = atsGeneralParamRepo.findAllByResumeId(106L);
//		Map<String, Object> generalATSParam = getGeneralATSParam();

		for (Map<String, Object> genATS : generalAllDesignATSParams) {
			for (ATS_General_Param_Entity agp : genATSParamAll) {

				String atsParamId = String.valueOf(agp.getAtsParamId());
//				System.out.println("atsParamId :: " + atsParamId);

				String genATSParamId = String.valueOf(genATS.get("atsParamId"));
//				// System.out.println("atsParamId :: " + atsParamId + " <!> genATSParamId :: " + genATSParamId);

				if (atsParamId.equalsIgnoreCase(genATSParamId) || atsParamId == genATSParamId) {
					// System.out.println("atsParamId :: " + atsParamId + " <!> genATSParamId :: " +
					// genATSParamId);

					switch (atsParamId) {

					case "ATS-091":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_091_obj = designAtsUtil.calculateATS091(atsParamId, generalATSId, fileName,
									file);

							if (ats_091_obj != null) {
								ats_091_score = ats_091_obj.getAtsScore();
								storeJSONs.add(ats_091_obj);
							}
//						JSONObject ats_091_obj = calculateATS091(atsParamId, generalATSId, fileName, file);
//						ats_091_score = ats_091_obj.getLong("ats091_points");
//						storeJSONs.add(ats_091_obj);

//						ats_091_score = calculateATS091(atsParamId, generalATSId, fileName, file);

						}
						break;

					case "ATS-092":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_092_obj = designAtsUtil.calculateATS092(atsParamId, generalATSId, fileName,
									file);

							if (ats_092_obj != null) {
								ats_092_score = ats_092_obj.getAtsScore();
								storeJSONs.add(ats_092_obj);
							}
//						ats_092_score = calculateATS092(atsParamId, generalATSId, fileName, file);

//						JSONObject ats_092_obj = calculateATS092(atsParamId, generalATSId, fileName, file);
//
//						ats_092_score = ats_092_obj.getLong("ats092_points");
//
//						storeJSONs.add(ats_092_obj);

						}

						break;

					case "ATS-093":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_093_obj = designAtsUtil.calculateATS093(atsParamId, generalATSId, fileName,
									file);

							if (ats_093_obj != null) {
								ats_093_score = ats_093_obj.getAtsScore();
								storeJSONs.add(ats_093_obj);
							}
//						ats_093_score = calculateATS093(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_093_obj = calculateATS093(atsParamId, generalATSId, fileName, file);
//
//						ats_093_score = ats_093_obj.getLong("ats093_points");
//
//						storeJSONs.add(ats_093_obj);
						}

						break;

					case "ATS-094":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_094_obj = designAtsUtil.calculateATS094(atsParamId, generalATSId, fileName,
									file);

							if (ats_094_obj != null) {
								ats_094_score = ats_094_obj.getAtsScore();
								storeJSONs.add(ats_094_obj);
							}
//						ats_094_score = calculateATS094(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_094_obj = calculateATS094(atsParamId, generalATSId, fileName, file);
//
//						ats_094_score = ats_094_obj.getLong("ats094_points");
//
//						storeJSONs.add(ats_094_obj);

						}
						break;

					case "ATS-095":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_095_obj = designAtsUtil.calculateATS095(atsParamId, generalATSId, fileName,
									file);

							if (ats_095_obj != null) {
								ats_095_score = ats_095_obj.getAtsScore();
								storeJSONs.add(ats_095_obj);
							}
//						ats_095_score = calculateATS095(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_095_obj = calculateATS095(atsParamId, generalATSId, fileName, file);
//
//						ats_095_score = ats_095_obj.getLong("ats095_points");
//
//						storeJSONs.add(ats_095_obj);

						}

						break;

					case "ATS-096":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_096_obj = designAtsUtil.calculateATS096(atsParamId, generalATSId, fileName,
									file);

							if (ats_096_obj != null) {
								ats_096_score = ats_096_obj.getAtsScore();
								storeJSONs.add(ats_096_obj);
							}

//						ats_096_score = calculateATS096(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_096_obj = calculateATS096(atsParamId, generalATSId, fileName, file);
//
//						ats_096_score = ats_096_obj.getLong("ats096_points");
//
//						storeJSONs.add(ats_096_obj);

						}

						break;

					case "ATS-097":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_097_obj = designAtsUtil.calculateATS097(atsParamId, generalATSId, fileName,
									file);

							if (ats_097_obj != null) {
								ats_097_score = ats_097_obj.getAtsScore();
								storeJSONs.add(ats_097_obj);
							}
//						ats_097_score = calculateATS097(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_097_obj = calculateATS097(atsParamId, generalATSId, fileName, file);
//
//						ats_097_score = ats_097_obj.getLong("ats097_points");
//
//						storeJSONs.add(ats_097_obj);

						}

						break;

					case "ATS-098":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_098_obj = designAtsUtil.calculateATS098(atsParamId, generalATSId, fileName,
									file);

							if (ats_098_obj != null) {
								ats_098_score = ats_098_obj.getAtsScore();
								storeJSONs.add(ats_098_obj);
							}
//						ats_098_score = calculateATS098(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_098_obj = calculateATS098(atsParamId, generalATSId, fileName, file);
//
//						ats_098_score = ats_098_obj.getLong("ats098_points");
//
//						storeJSONs.add(ats_098_obj);

						}
						break;

					case "ATS-099":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_099_obj = designAtsUtil.calculateATS099(atsParamId, generalATSId, fileName,
									file);

							if (ats_099_obj != null) {
								ats_099_score = ats_099_obj.getAtsScore();
								storeJSONs.add(ats_099_obj);
							}
//						ats_099_score = calculateATS099(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_099_obj = calculateATS099(atsParamId, generalATSId, fileName, file);
//
//						ats_099_score = ats_099_obj.getLong("ats099_points");
//
//						storeJSONs.add(ats_099_obj);

						}

						break;

					case "ATS-100":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_100_obj = designAtsUtil.calculateATS100(atsParamId, generalATSId, fileName,
									file);
							if (ats_100_obj != null) {
								ats_100_score = ats_100_obj.getAtsScore();
								storeJSONs.add(ats_100_obj);
							}
//						ats_100_score = calculateATS100(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_100_obj = calculateATS100(atsParamId, generalATSId, fileName, file);
//
//						ats_100_score = ats_100_obj.getLong("ats100_points");
//
//						storeJSONs.add(ats_100_obj);

						}

						break;

					case "ATS-101":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_101_obj = designAtsUtil.calculateATS101(atsParamId, generalATSId, fileName,
									file);
							if (ats_101_obj != null) {
								ats_101_score = ats_101_obj.getAtsScore();
								storeJSONs.add(ats_101_obj);
							}
//						ats_101_score = calculateATS101(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_101_obj = calculateATS101(atsParamId, generalATSId, fileName, file);
//
//						ats_101_score = ats_101_obj.getLong("ats101_points");
//
//						storeJSONs.add(ats_101_obj);

						}
						break;

					case "ATS-111":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_111_obj = designAtsUtil.calculateATS111(atsParamId, generalATSId, fileName,
									file);

							if (ats_111_obj != null) {
								ats_111_score = ats_111_obj.getAtsScore();
								storeJSONs.add(ats_111_obj);
							}
//						ats_111_score = calculateATS111(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_111_obj = calculateATS111(atsParamId, generalATSId, fileName, file);
//
//						ats_111_score = ats_111_obj.getLong("ats111_points");
//
//						storeJSONs.add(ats_111_obj);

						}

						break;

					case "ATS-103":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_103_obj = designAtsUtil.calculateATS103(atsParamId, generalATSId, fileName,
									file);

							if (ats_103_obj != null) {
								ats_103_score = ats_103_obj.getAtsScore();
								storeJSONs.add(ats_103_obj);
							}
//						ats_103_score = calculateATS103(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_103_obj = calculateATS103(atsParamId, generalATSId, fileName, file);
//
//						ats_103_score = ats_103_obj.getLong("ats103_points");
//
//						storeJSONs.add(ats_103_obj);

						}

						break;

					case "ATS-104":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_104_obj = designAtsUtil.calculateATS104(atsParamId, generalATSId, fileName,
									file);

							if (ats_104_obj != null) {
								ats_104_score = ats_104_obj.getAtsScore();
								storeJSONs.add(ats_104_obj);
							}
//						ats_104_score = calculateATS104(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_104_obj = calculateATS104(atsParamId, generalATSId, fileName, file);
//
//						ats_104_score = ats_104_obj.getLong("ats104_points");
//
//						storeJSONs.add(ats_104_obj);

						}
						break;

					case "ATS-105":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_105_obj = designAtsUtil.calculateATS105(atsParamId, generalATSId, fileName,
									file);

							if (ats_105_obj != null) {
								ats_105_score = ats_105_obj.getAtsScore();
								storeJSONs.add(ats_105_obj);
							}
//						ats_105_score = calculateATS105(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_105_obj = calculateATS105(atsParamId, generalATSId, fileName, file);
//
//						ats_105_score = ats_105_obj.getLong("ats105_points");
//
//						storeJSONs.add(ats_105_obj);

						}

						break;

					case "ATS-106":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_106_obj = designAtsUtil.calculateATS106(atsParamId, generalATSId, fileName,
									file);

							if (ats_106_obj != null) {
								ats_106_score = ats_106_obj.getAtsScore();
								storeJSONs.add(ats_106_obj);
							}
//						ats_106_score = calculateATS106(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_106_obj = calculateATS106(atsParamId, generalATSId, fileName, file);
//
//						ats_106_score = ats_106_obj.getLong("ats106_points");
//
//						storeJSONs.add(ats_106_obj);

						}

						break;

					case "ATS-107":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_107_obj = designAtsUtil.calculateATS107(atsParamId, generalATSId, fileName,
									file);

							if (ats_107_obj != null) {
								ats_107_score = ats_107_obj.getAtsScore();
								storeJSONs.add(ats_107_obj);
							}
//						ats_107_score = calculateATS107(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_107_obj = calculateATS107(atsParamId, generalATSId, fileName, file);
//
//						ats_107_score = ats_107_obj.getLong("ats107_points");
//
//						storeJSONs.add(ats_107_obj);

						}

						break;

					case "ATS-108":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_108_obj = designAtsUtil.calculateATS108(atsParamId, generalATSId, fileName,
									file);

							if (ats_108_obj != null) {
								ats_108_score = ats_108_obj.getAtsScore();
								storeJSONs.add(ats_108_obj);
							}
//						ats_108_score = calculateATS108(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_108_obj = calculateATS108(atsParamId, generalATSId, fileName, file);
//
//						ats_108_score = ats_108_obj.getLong("ats108_points");
//
//						storeJSONs.add(ats_108_obj);

						}
						break;

					case "ATS-109":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_109_obj = designAtsUtil.calculateATS109(atsParamId, generalATSId, fileName,
									file);

							if (ats_109_obj != null) {
								ats_109_score = ats_109_obj.getAtsScore();
								storeJSONs.add(ats_109_obj);
							}
//						ats_109_score = calculateATS109(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_109_obj = calculateATS109(atsParamId, generalATSId, fileName, file);
//
//						ats_109_score = ats_109_obj.getLong("ats109_points");
//
//						storeJSONs.add(ats_109_obj);

						}

						break;

					case "ATS-110":
						if (atsParamId.equalsIgnoreCase(genATSParamId)) {

							long generalATSId = agp.getAtsGeneralId();

							AtsListDto ats_110_obj = designAtsUtil.calculateATS110(atsParamId, generalATSId, fileName,
									file);

							if (ats_110_obj != null) {
								ats_110_score = ats_110_obj.getAtsScore();
								storeJSONs.add(ats_110_obj);
							}
//						ats_110_score = calculateATS110(atsParamId, generalATSId, fileName, file);
//
//						JSONObject ats_110_obj = calculateATS110(atsParamId, generalATSId, fileName, file);
//
//						ats_110_score = ats_110_obj.getLong("ats110_points");
//
//						storeJSONs.add(ats_110_obj);

						}

						break;

					default:
						System.out.println("Unhandled ATS Param: " + agp.getAtsParamId());
					}
				}
			}
		}

		System.out.println(" ATS Combined Score : " + "\nATS-091 => " + ats_091_score + "\nATS-092 => " + ats_092_score
				+ "\nATS-093 => " + ats_093_score + "\nATS-094 => " + ats_094_score + "\nATS-095 => " + ats_095_score
				+ "\nATS-096 => " + ats_096_score + "\nATS-097 => " + ats_097_score + "\nATS-098 => " + ats_098_score
				+ "\nATS-099 => " + ats_099_score + "\nATS-100 => " + ats_100_score + "\nATS-101 => " + ats_101_score
				+ "\nATS-111 => " + ats_111_score + "\nATS-103 => " + ats_103_score + "\nATS-104 => " + ats_104_score
				+ "\nATS-105 => " + ats_105_score + "\nATS-106 => " + ats_106_score + "\nATS-107 => " + ats_107_score
				+ "\nATS-108 => " + ats_108_score + "\nATS-109 => " + ats_109_score + "\nATS-110 => " + ats_110_score);

		ats_Combined_Score = ats_091_score + ats_092_score + ats_093_score + ats_094_score + ats_095_score
				+ ats_096_score + ats_097_score + ats_098_score + ats_099_score + ats_100_score + ats_101_score
				+ ats_111_score + ats_103_score + ats_104_score + ats_105_score + ats_106_score + ats_107_score
				+ ats_108_score + ats_109_score + ats_110_score;

//		combinedDto = new AtsListDto();
//		combinedGenDto = new AtsGenParamDto();

		long atsGenCombined = 111;
		ATS_General_Param_Entity genATSCombined = atsGeneralParamRepo.findByAtsGeneralId(atsGenCombined);

		combinedGenDto.setAtsGeneralId(genATSCombined.getAtsGeneralId());
		combinedGenDto.setAtsParamId(genATSCombined.getAtsParamId());
		combinedGenDto.setCategory(genATSCombined.getCategory());
		combinedGenDto.setDescription(genATSCombined.getDescription());
		combinedGenDto.setMax_points(genATSCombined.getMax_points());
		combinedGenDto.setParameter(genATSCombined.getParameter());
		combinedGenDto.setPenalty_points(genATSCombined.getPenalty_points());
		combinedGenDto.setTotal_points(genATSCombined.getTotal_points());

		combinedDto.setAtsGeneralId(genATSCombined.getAtsGeneralId());
		combinedDto.setAtsGeneralParamDto(combinedGenDto);
		combinedDto.setAtsParamData(null);
		combinedDto.setAtsParamId(genATSCombined.getAtsParamId());
		combinedDto.setAtsParamType("Total");
		combinedDto.setAtsScore(ats_Combined_Score);

		storeJSONs.add(combinedDto);

		System.out.println("storeJSONs :: " + storeJSONs);

		return storeJSONs;
	}

}
public List<ElasticSearchResponseDto> mapJsonToElasticSearchResponse(JsonNode jsonNode,
			boolean isSuggestionRequired, boolean isMultiSearch, String companyNameUrlSuffix) throws Exception, BussinessException {

		logger.info("Parsing response for elastic search for isSuggestionRequired :" + isSuggestionRequired
				+ " isMultiSearch :" + isMultiSearch);
		if (jsonNode == null) {
			return Collections.emptyList();
		}
		
		List<MedicineMaster> medicineMasterList = null;
		Map<String, MedicineMaster> medicineMastreMap = new HashMap<>();
		List<String> productCodeList = new ArrayList<>();
		JsonNode responses = jsonNode.get("hits");
		if (responses == null) {
			responses = jsonNode.get("responses");
			if (responses.isArray() && responses.size() > 0) {
				if (isMultiSearch) {
					responses = responses.get(1).path("hits");
				} else {
					responses = responses.get(0).path("hits");
				}
			}
			
		}
		for (JsonNode response : responses.path("hits")) {
			JsonNode sourceJson = response.path("_source");
			if (sourceJson != null) {
				String productCode = getStringValue(sourceJson, "subs_product_code");
				productCodeList.add(productCode);
			}
		}

		if (!CollectionUtils.isEmpty(productCodeList)) {
			medicineMasterList = medicineMasterRepository.findByProductCodeIn(productCodeList);
			if (!CollectionUtils.isEmpty(medicineMasterList)) {
				for (MedicineMaster medicineMaster : medicineMasterList) {
					medicineMastreMap.put(medicineMaster.getProductCode(), medicineMaster);
				}
			}
		}


		DecimalFormat df2 = new DecimalFormat("#.##");
		List<ElasticSearchResponseDto> responseDto = new ArrayList<>();
		
		boolean unitPriceCalcEligible = false;
		
		long customerId = utilities.getUserId();
		if(customerId==0) {
			CustomerCategory customerCategoryForCalc = customerCategoryRepository
		            .findByCustomerIdAndCategoryTypeAndActiveAndDeviceId(customerId, CxCategoryEnum.UNIT_PRICE_CALCULATION.getCategoryName(), true,"DEVICE_XYZ");
			 customerId = customerCategoryForCalc.getCustomerId();

		}
		if(customerId!=0) {
			List<CustomerCategory> customerCategory = customerCategoryRepository.findByCustomerIdAndCategoryTypeAndActive(
					customerId, CxCategoryEnum.UNIT_PRICE_CALCULATION.getCategoryName(), true);
			
			if(customerCategory.size()>0) {
				CustomerCategory  customerCategory2= customerCategory.stream().filter(t->t.getCategory().equalsIgnoreCase("B")).findFirst().orElse(null);
				if(customerCategory2!=null) {
					unitPriceCalcEligible=true;
				}
			
				
			}
		}
		
				
		for (JsonNode response : responses.path("hits")) {
			try {
				ElasticSearchResponseDto dto = new ElasticSearchResponseDto();
				
				JsonNode sourceJson = response.path("_source");
				if (sourceJson != null) {
					ElasticSearchProductDto productSearchDto = new ElasticSearchProductDto();
					StringBuilder pdpLinkBuilder = new StringBuilder(pdpLink);
					productSearchDto.setSkuName(getStringValue(sourceJson, "original_sku_name"));
					productSearchDto.setProductCode(getStringValue(sourceJson, "original_product_code"));
					productSearchDto.setAvailabilityStatus(getStringValue(sourceJson, "availability_status"));
					productSearchDto.setAvailable(getBooleanValue(sourceJson, "original_available"));
					productSearchDto.setSuppliedByTm(getBooleanValue(sourceJson, "original_supplied_bytm"));
					productSearchDto.setManufacturerName(getStringValue(sourceJson, "original_company_nm"));
					productSearchDto.setMaxCappedQty(getLongValue(sourceJson, "max_capped_qty"));
					productSearchDto.setMrp(Double.parseDouble(df2.format(getDoubleValue(sourceJson, "original_mrp"))));
					productSearchDto.setPackSize(getStringValue(sourceJson, "original_pack"));
					productSearchDto.setUnit(getStringValue(sourceJson, "original_unit"));
					productSearchDto.setPackForm(getStringValue(sourceJson, "original_pack_form"));
					productSearchDto.setPdpDeepLink(pdpLinkBuilder.append(getStringValue(sourceJson, "original_product_code")).toString());
					
					if (StringUtils.isBlank(productSearchDto.getPackForm())) {
						productSearchDto.setPackForm("Pack of " + productSearchDto.getPackSize() + " Units");
					}
					//productSearchDto.setProductImageUrl(getStringValue(sourceJson, "product_image_urls"));
					productSearchDto.setMotherBrand(getStringValue(sourceJson, "original_mother_brand"));
					productSearchDto.setOtc(getBooleanValue(sourceJson, "is_otc"));
					productSearchDto.setChronic(getBooleanValue(sourceJson, "is_chronic"));
					productSearchDto.setMotherBrand(getStringValue(sourceJson, "original_mother_brand"));
					productSearchDto.setComposition(getStringValue(sourceJson, "original_composition"));
					if (!StringUtils.isBlank(productSearchDto.getComposition())) {
						productSearchDto
								.setComposition(utilities.convertToCamelCase(productSearchDto.getComposition()));
					}
					productSearchDto.setSubsFound(getBooleanValue(sourceJson, "subs_found"));
					Long subs_taken_count = getLongValue(sourceJson, "subs_taken_count");
					if (subs_taken_count != null) {
						if (subs_taken_count < 1000) {
							if (subs_taken_count > 100) {
								subs_taken_count = subs_taken_count / 100;
								subs_taken_count = subs_taken_count * 100;
							} else {
								subs_taken_count = 100L;
							}
							productSearchDto
									.setCustomerAlsoBoughtMsg(subs_taken_count + " + users bought this instead");
						} else {
							productSearchDto.setCustomerAlsoBoughtMsg(subs_taken_count + " users bought this instead");

						}
					}
					if (productSearchDto.getAvailabilityStatus() != null
							&& productSearchDto.getAvailabilityStatus().equals("Not for Sale")) {
						productSearchDto.setAvailabilityMessage("Sale is prohibited due to government restrictions");
					}
					
					if (productSearchDto.getUnit() != null && productSearchDto.getPackSize() != null
							&& Double.parseDouble(productSearchDto.getPackSize()) > 0) {
							if(unitPriceCalcEligible){
								productSearchDto
									.setPricePerUnitLabel("₹"
										+ df2.format(
												productSearchDto.getSellingPrice()
														/ Double.parseDouble(productSearchDto.getPackSize()))
										+ "/" + productSearchDto.getUnit());
							}
						productSearchDto
								.setPricePerUnitLabel("₹"+ df2.format(
										productSearchDto.getMrp()
										/ Double.parseDouble(productSearchDto.getPackSize()))
						+ "/" + productSearchDto.getUnit());
	}
					
					
					String orgCountryOfOrigin = null;
					orgCountryOfOrigin = getStringValue(sourceJson, "original_country_nm");
					if (!StringUtils.isBlank(orgCountryOfOrigin)) {
						productSearchDto.setManufacturerAddr(getStringValue(sourceJson, "original_company_addr") + "<br><br>Country of origin: " + orgCountryOfOrigin);
					} else {
						productSearchDto.setManufacturerAddr(getStringValue(sourceJson, "original_company_addr"));
					}
					
					productSearchDto.setRxRequired(getBooleanValue(sourceJson, "original_rx_required"));
					productSearchDto.setColdStorage(getBooleanValue(sourceJson, "org_cold_storage"));
					productSearchDto.setReturnable(getBooleanValue(sourceJson, "org_is_returnable"));
					productSearchDto.setProductUrlSuffix(getStringValue(sourceJson, "original_product_url_suffix"));
					productSearchDto.setGenericBranded(getStringValue(sourceJson, "original_generic_branded"));
					productSearchDto.setStrength(getStringValue(sourceJson, "strength"));
					productSearchDto.setSaltUrlSuffix(getStringValue(sourceJson, "salt_url_suffix"));
					productSearchDto.setDisease1(getStringValue(sourceJson, "disease1"));
					productSearchDto.setDisease2(getStringValue(sourceJson, "disease2"));
					productSearchDto.setDisease3(getStringValue(sourceJson, "disease3"));
					productSearchDto.setCategory1(getStringValue(sourceJson, "category1"));
					productSearchDto.setCategory2(getStringValue(sourceJson, "category2"));
					productSearchDto.setCategory3(getStringValue(sourceJson, "category3"));
					productSearchDto.setRouteOfAdministrator(getStringValue(sourceJson, "route_of_administrator"));
					productSearchDto.setDrugType(getStringValue(sourceJson, "original_drug_type"));
					productSearchDto.setCanonicalUrl(getStringValue(sourceJson, "original_product_canonical_url"));
					productSearchDto.setModifiedOn(getStringValue(sourceJson, "modified_on"));
					
					//Subs Data
					
					if (productSearchDto.isSubsFound() && productSearchDto.getProductCode()
							.equals(getStringValue(sourceJson, "subs_product_code"))) {
						productSearchDto.setDiscount(
								Double.valueOf(getStringValue(sourceJson, "savings_percentage").replace("%", "")));
						productSearchDto.setSellingPrice(
								Double.parseDouble(df2.format((getDoubleValue(sourceJson, "subs_selling_price"))
										/ (getLongValue(sourceJson, "sub_recommended_qty")))));

						if(unitPriceCalcEligible){
							if (StringUtils.isNotBlank(sellingPriceStr)) {
					        sellingPriceStr = sellingPriceStr.replaceAll("[^0-9.]", "");  // Remove ₹ and unit text
					        if (!sellingPriceStr.isEmpty()) {
					            sellingPricePerUnit = Double.parseDouble(sellingPriceStr);
					        }
					    }

					    double subsSellingPrice = getDoubleValue(sourceJson, "subs_selling_price");
					    long subRecommendedQty = getLongValue(sourceJson, "sub_recommended_qty");

					    double subsPack = 1.0;
String subsPackStr = getStringValue(sourceJson, "subs_pack");
					    if (StringUtils.isNotBlank(subsPackStr)) {
					        subsPack = Double.parseDouble(subsPackStr);
					    }
					    double savingsValue = (sellingPricePerUnit - ((subsSellingPrice / subRecommendedQty) / subsPack)) 
					                        * (subRecommendedQty * subsPack);

					    double calculatedSavingsPercentage = (savingsValue / (sellingPricePerUnit * (subRecommendedQty * subsPack))) * 100;

					    productSearchDto.setSubsSavingPercentage(df2.format(calculatedSavingsPercentage) + "%");
						
						} else {
							String savingsPercentage = getStingValue(sourceJson, "savings_percentage");
							if (StringUtils.isNotBlank(savingsPercentage) && savingsPercentage.contains("%")) {
								savingsPercentage = savingsPercentage.replace("%", "");
								productSearchDto
										.setSubsSavingPercentage(df2.format(Double.parseDouble(savingsPercentage)) + "%");
								} else {
									productSearchDto.setSubsSavingPercentage(df2.format(Double.parseDouble(savingsPercentage)));
								}
						
						}
						 else {
								productSearchDto.setDiscount(
										Double.parseDouble(df2.format((getDoubleValue(sourceJson, "original_base_discount")))));
								productSearchDto.setSellingPrice(Double.parseDouble(df2.format(productSearchDto.getMrp()
										- ((productSearchDto.getMrp() * productSearchDto.getDiscount()) / 100))));
							}
					if (productSearchDto.isSubsFound() && isSuggestionRequired && !productSearchDto.getProductCode()
							.equals(getStringValue(sourceJson, "subs_product_code"))) {
						ElasticSearchProductDto subsProductDto = new ElasticSearchProductDto();
						  pdpLinkBuilder = new StringBuilder(pdpLink);

						subsProductDto.setSkuName(getStringValue(sourceJson, "subs_sku_name"));
						subsProductDto.setProductCode(getStringValue(sourceJson, "subs_product_code"));

						String savingsPercentage = getStringValue(sourceJson, "savings_percentage");
						if (StringUtils.isNotBlank(savingsPercentage) && savingsPercentage.contains("%")) {
							savingsPercentage = savingsPercentage.replace("%", "");
							productSearchDto
									.setSubsSavingPercentage(df2.format(Double.parseDouble(savingsPercentage)) + "%");
						} else {
							productSearchDto.setSubsSavingPercentage(df2.format(Double.parseDouble(savingsPercentage)));
						}
						
						subsProductDto.setMrp(Double.parseDouble(df2.format(getDoubleValue(sourceJson, "subs_mrp"))));
						Long sub_recommended_qty = getLongValue(sourceJson, "sub_recommended_qty");
						if (sub_recommended_qty != null && sub_recommended_qty > 0) {
							subsProductDto.setSellingPrice(
									Double.parseDouble(df2.format((getDoubleValue(sourceJson, "subs_selling_price"))
											/ (getLongValue(sourceJson, "sub_recommended_qty")))));
							subsProductDto.setDiscount(Double.parseDouble(
									df2.format(((subsProductDto.getMrp() - subsProductDto.getSellingPrice())
											/ subsProductDto.getMrp()) * 100)));

						}
						subsProductDto.setProductUrlSuffix(getStringValue(sourceJson, "subs_product_url_suffix"));
						subsProductDto.setMotherBrand(getStringValue(sourceJson, "subs_mother_brand"));
						subsProductDto.setManufacturerName(getStringValue(sourceJson, "subs_company_nm"));
						subsProductDto.setAvailabilityStatus(getStringValue(sourceJson, "subs_availability_status"));
						subsProductDto.setAvailable(getBooleanValue(sourceJson, "subs_available"));
						subsProductDto.setSuppliedByTm(getBooleanValue(sourceJson, "subs_supplied_bytm"));
						subsProductDto.setPackForm(getStringValue(sourceJson, "subs_pack_form"));
						//subsProductDto.setProductImageUrl(getStringValue(sourceJson, "subs_product_image_urls"));
						
						subsProductDto.setMaxCappedQty(getLongValue(sourceJson, "subs_max_capped_qty"));
						if (subsProductDto.getMaxCappedQty() == null) {
							subsProductDto.setMaxCappedQty(getLongValue(sourceJson, "max_capped_qty"));
						}
						subs_taken_count = null;
						subs_taken_count = getLongValue(sourceJson, "substitute_taken_count");
						if (subs_taken_count == null) {
							subs_taken_count = getLongValue(sourceJson, "subs_taken_count");
						}
						if (subs_taken_count != null) {
							if (subs_taken_count < 1000) {
								if (subs_taken_count > 100) {
									subs_taken_count = subs_taken_count / 100;
									subs_taken_count = subs_taken_count * 100;
								} else {
									subs_taken_count = 100L;
								}
								subsProductDto
										.setCustomerAlsoBoughtMsg(subs_taken_count + " + users bought this instead");
							} else {
								subsProductDto
										.setCustomerAlsoBoughtMsg(subs_taken_count + " users bought this instead");

							}
						}
						if (subsProductDto.getAvailabilityStatus() != null
								&& subsProductDto.getAvailabilityStatus().equals("Not for Sale")) {
							subsProductDto.setAvailabilityMessage("Sale is prohibited due to government restrictions");
						}
						subsProductDto.setPackSize(getStringValue(sourceJson, "subs_pack"));
						if (productSearchDto.getUnit() != null && subsProductDto.getPackSize() != null
								&& Double.parseDouble(subsProductDto.getPackSize()) > 0) {
							subsProductDto.setPricePerUnitLabel("₹"
									+ df2.format(
											subsProductDto.getSellingPrice()
													/ Double.parseDouble(subsProductDto.getPackSize()))
									+ "/" + productSearchDto.getUnit());
						}
						if (StringUtils.isBlank(subsProductDto.getPackForm())) {
							subsProductDto.setPackForm("Pack of " + subsProductDto.getPackSize() + " Units");
						}
						
						String subsCountryOfOrigin = null;
						subsCountryOfOrigin = getStringValue(sourceJson, "subs_country_nm");
						if (!StringUtils.isBlank(subsCountryOfOrigin)) {
							subsProductDto.setManufacturerAddr(getStringValue(sourceJson, "subs_company_addr") + "<br><br>Country of origin: " + subsCountryOfOrigin);
						} else {
							subsProductDto.setManufacturerAddr(getStringValue(sourceJson, "subs_company_addr"));
						}
						
						MedicineMaster suggestionMedicineMaster = medicineMastreMap
								.get(subsProductDto.getProductCode());
						if (suggestionMedicineMaster != null) {
							if (suggestionMedicineMaster.getAcuteChronic() != null
									&& suggestionMedicineMaster.getAcuteChronic().equalsIgnoreCase("CHRONIC")) {
								subsProductDto.setChronic(true);
							}
							if (suggestionMedicineMaster.getScheduleDrugs() != null
									&& suggestionMedicineMaster.getScheduleDrugs().contentEquals("OTC")) {
								subsProductDto.setOtc(true);
							}
						}
						String productCode = getStringValue(sourceJson, "subs_product_code");
						List<ProductImageMaster> productImageMasterList = cacheService.findByProductCode(productCode);
						if (!CollectionUtils.isEmpty(productImageMasterList)) {
							List<String> productImageList = new ArrayList<>();
							String productUrl = null;
							for (ProductImageMaster imageMaster : productImageMasterList) {
								if (imageMaster.getProductImageUrl() != null) {
									productImageList.add(imageMaster.getProductImageUrl());
									if (productUrl != null) {
										productUrl = productUrl + ","
												+ imageMaster.getProductImageUrl();
									} else {
										productUrl = imageMaster.getProductImageUrl();
									}
								}
							}
							if (!CollectionUtils.isEmpty(productImageList) && !StringUtils.isEmpty(productUrl)) {
								subsProductDto.setProductUrlArray(productImageList);
								subsProductDto.setProductImageUrl(productUrl);
							} else {
								subsProductDto.setProductUrlArray(new ArrayList<>());
								subsProductDto.setProductImageUrl("");

							}
						} else {
							subsProductDto.setProductUrlArray(new ArrayList<>());
							subsProductDto.setProductImageUrl("");

						}
						if (StringUtils.isBlank(subsProductDto.getProductImageUrl())) {
							subsProductDto.setProductImageUrl(getStringValue(sourceJson, "original_drug_type"));
						}
						String appender = pdpLinkBuilder.append(getStringValue(sourceJson, "subs_product_code")).toString();
						subsProductDto.setPdpDeepLink(
								appender + "&ogPcId=" + getStringValue(sourceJson, "original_product_code"));
						dto.setSuggestion(subsProductDto);
					} else {
						productSearchDto.setSubsFound(false);
					}
					String productCode = getStringValue(sourceJson, "original_product_code");
					List<ProductImageMaster> productImageMasterList = cacheService.findByProductCode(productCode);
					if (!CollectionUtils.isEmpty(productImageMasterList)) {
						List<String> productImageList = new ArrayList<>();
						List<String> encodedProductImageList = new ArrayList<>();
						String productUrl = null;
						for (ProductImageMaster imageMaster : productImageMasterList) {
							if (imageMaster.getProductImageUrl() != null) {
								productImageList.add(imageMaster.getProductImageUrl());
								if (productUrl != null) {
									productUrl = productUrl + ","
											+ imageMaster.getProductImageUrl();
								} else {
									productUrl = imageMaster.getProductImageUrl();
								}	
								encodedProductImageList.add(encodeUrl(imageMaster.getProductImageUrl()));
							}
							
						}
						
						if (!CollectionUtils.isEmpty(encodedProductImageList) && !StringUtils.isEmpty(productUrl)) {
							productSearchDto.setProductUrlArray(encodedProductImageList);
							productSearchDto.setProductImageUrl(productUrl);
						} else {
							productSearchDto.setProductUrlArray(new ArrayList<>());
							productSearchDto.setProductImageUrl("");

						}
					} else {
						productSearchDto.setProductUrlArray(new ArrayList<>());
						productSearchDto.setProductImageUrl("");

					}
					if (StringUtils.isBlank(productSearchDto.getProductImageUrl())) {
						productSearchDto.setProductImageUrl(getStringValue(sourceJson, "original_drug_type"));
					}
					productSearchDto.setCompanyNameUrlSuffix(companyNameUrlSuffix);
					dto.setProduct(productSearchDto);
					responseDto.add(dto);
				} else {
					logger.error("No data found inside the _source for isSuggestionRequired : " + isSuggestionRequired
							+ " isMultiSearch :" + isMultiSearch);
				}
			} catch (Exception e) {
				logger.error("Error while parsing elastic search response into dto" + ExceptionUtils.getStackTrace(e));

			}
		}
		logger.info("Successfully Parsing response for elastic search for isSuggestionRequired :" + isSuggestionRequired
				+ " isMultiSearch :" + isMultiSearch);
		return responseDto;
	}

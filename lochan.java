List<MedicineMaster> medicineMasterList = null;
		Map<String, MedicineMaster> medicineMastreMap = new HashMap<>();
		List<String> productCodeList = new ArrayList<>();
		boolean unitPriceCalcEligible = true;
		//check if user logged In
		Long customerId = utilities.getUserId();
		if (customerId==null){
			if(StringUtils.isNotEmpty(deviceId)){
				CustomerCategory customerCategoryForCalc = customerCategoryRepository
						.findByCustomerIdAndCategoryTypeAndActiveAndDeviceId(customerId,
								CxCategoryEnum.UNIT_PRICE_CALCULATION.getCategoryName(), true, deviceId);
				customerId = customerCategoryForCalc.getCustomerId();
			}
		}
		List<CustomerCategory> customerCategory = customerCategoryRepository
				.findByCustomerIdAndCategoryTypeAndActive(customerId,
						CxCategoryEnum.UNIT_PRICE_CALCULATION.getCategoryName(), true);
		if (customerCategory.size() > 0) {
			CustomerCategory customerCategoryData = customerCategory.stream()
					.filter(t -> t.getCategory().equalsIgnoreCase("B")).findFirst().orElse(null);
			if (customerCategoryData != null) {
				unitPriceCalcEligible = false;
			}
		}
		JsonNode responses = jsonNode.get("hits");

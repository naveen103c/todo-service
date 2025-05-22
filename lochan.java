List<MedicineMaster> medicineMasterList = null;
		Map<String, MedicineMaster> medicineMastreMap = new HashMap<>();
		List<String> productCodeList = new ArrayList<>();
		boolean unitPriceCalcEligible = false;
		Long customerId = utilities.getUserId();
		if (customerId != null) {
			if (customerId == 0) {
				CustomerCategory customerCategoryForCalc = customerCategoryRepository
						.findByCustomerIdAndCategoryTypeAndActiveAndDeviceId(customerId,
								CxCategoryEnum.UNIT_PRICE_CALCULATION.getCategoryName(), true, deviceId);
				customerId = customerCategoryForCalc.getCustomerId();

			} else {
				List<CustomerCategory> customerCategory = customerCategoryRepository
						.findByCustomerIdAndCategoryTypeAndActive(customerId,
								CxCategoryEnum.UNIT_PRICE_CALCULATION.getCategoryName(), true);
				if (customerCategory.size() > 0) {
					CustomerCategory customerCategoryData = customerCategory.stream()
							.filter(t -> t.getCategory().equalsIgnoreCase("A")).findFirst().orElse(null);
					if (customerCategoryData != null) {
						unitPriceCalcEligible = true;
					}
				}
			}
		}
		JsonNode responses = jsonNode.get("hits");

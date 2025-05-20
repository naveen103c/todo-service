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
					    double savings Value = (sellingPricePerUnit - ((subsSellingPrice / subRecommendedQty) / subsPack)) 
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
					} else {
						productSearchDto.setDiscount(
								Double.parseDouble(df2.format((getDoubleValue(sourceJson, "original_base_discount")))));
						productSearchDto.setSellingPrice(Double.parseDouble(df2.format(productSearchDto.getMrp()
								- ((productSearchDto.getMrp() * productSearchDto.getDiscount()) / 100))));
					}
					

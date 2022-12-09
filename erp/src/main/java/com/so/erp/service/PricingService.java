package com.so.erp.service;

import java.util.List;

import com.so.erp.model.Buyer;
import com.so.erp.model.Pricing;
import com.so.erp.model.Product;

public interface PricingService {

	List<Pricing> pricingList(Pricing pricing);

	int getTotal(Pricing pricing);

	void pricingInsert(Pricing pricing);

	int pricingDelete(Pricing pricing);

	int pricingUpdate(Pricing pricing);

	int getPrice(Pricing pricing);
	
	List<Pricing> search(Pricing pricing);

	int pricingRestore(Pricing pricing);

	int overlapCheck(Pricing pricing);

	Pricing listForExcel(Pricing pricing);

	List<Product> getProductList(Pricing pricing);


}

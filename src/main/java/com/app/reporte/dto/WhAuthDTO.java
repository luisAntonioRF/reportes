package com.app.reporte.dto;

import java.math.BigDecimal;

public class WhAuthDTO {

	 private BigDecimal billingAmt;
	 
	 private String otype;

	public BigDecimal getBillingAmt() {
		return billingAmt;
	}

	public void setBillingAmt(BigDecimal billingAmt) {
		this.billingAmt = billingAmt;
	}

	public String getOtype() {
		return otype;
	}

	public void setOtype(String otype) {
		this.otype = otype;
	}
	 
	 
	
}

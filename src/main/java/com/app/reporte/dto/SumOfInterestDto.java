package com.app.reporte.dto;

import java.math.BigDecimal;

public class SumOfInterestDto {

	private  Integer fact_account_id;
	private BigDecimal interest_revolving;
	private BigDecimal interest_fixed;
	private BigDecimal interest_total;
	
	
	
	
	public SumOfInterestDto() {
		
	}
	
	public Integer getFact_account_id() {
		return fact_account_id;
	}
	public void setFact_account_id(Integer fact_account_id) {
		this.fact_account_id = fact_account_id;
	}
	public BigDecimal getInterest_revolving() {
		return interest_revolving;
	}
	public void setInterest_revolving(BigDecimal interest_revolving) {
		this.interest_revolving = interest_revolving;
	}
	public BigDecimal getInterest_fixed() {
		return interest_fixed;
	}
	public void setInterest_fixed(BigDecimal interest_fixed) {
		this.interest_fixed = interest_fixed;
	}
	public BigDecimal getInterest_total() {
		return interest_total;
	}
	public void setInterest_total(BigDecimal interest_total) {
		this.interest_total = interest_total;
	}
	
	
}

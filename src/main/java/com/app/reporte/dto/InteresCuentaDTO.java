package com.app.reporte.dto;

import java.math.BigDecimal;

public class InteresCuentaDTO {

	private Integer id;
    private String prn;
    private Integer factAccountId;
    private BigDecimal interesCuentaBalance;
    private BigDecimal interesCuentaOrden;
    
    
    
	public InteresCuentaDTO() {
		
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getPrn() {
		return prn;
	}
	public void setPrn(String prn) {
		this.prn = prn;
	}
	public Integer getFactAccountId() {
		return factAccountId;
	}
	public void setFactAccountId(Integer factAccountId) {
		this.factAccountId = factAccountId;
	}
	public BigDecimal getInteresCuentaBalance() {
		return interesCuentaBalance;
	}
	public void setInteresCuentaBalance(BigDecimal interesCuentaBalance) {
		this.interesCuentaBalance = interesCuentaBalance;
	}
	public BigDecimal getInteresCuentaOrden() {
		return interesCuentaOrden;
	}
	public void setInteresCuentaOrden(BigDecimal interesCuentaOrden) {
		this.interesCuentaOrden = interesCuentaOrden;
	}
	@Override
	public String toString() {
		return "InteresCuentaDTO [id=" + id + ", prn=" + prn + ", factAccountId=" + factAccountId
				+ ", interesCuentaBalance=" + interesCuentaBalance + ", interesCuentaOrden=" + interesCuentaOrden + "]";
	}
    
	
	
    
}

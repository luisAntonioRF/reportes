package com.app.reporte.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountReportWalletDTO {

	private Integer accountId;           // account_id
    private String fecha;             // fecha
    private String card;
    private String prn;               // prn
    private String numCliente;        // num_cliente
    private String cuentaAsociada;    // cuenta_asociada
    private String producto;          // producto
    private BigDecimal lineaCredito;      // linea_credito
    private String status;            // status
    private Integer diasVencido;      // dias_vencido
    private Integer etapa;            // etapa
    private LocalDateTime fechaPago;  // fecha_pago
    private BigDecimal pagoExigible;      // pago_exigible
    private BigDecimal capitalNoExigible; // capital_no_exigible
    private BigDecimal capitalExigible;   // capital_exigible
    private BigDecimal interesExigible;   // interes_exigible
    private BigDecimal interesesCuentasBalances;
    private BigDecimal InteresesCuentasOrden;
    private BigDecimal interesesMoratorios; // intereses_moratorios
    
    
	public String getCard() {
		return card;
	}
	public void setCard(String card) {
		this.card = card;
	}

	public Integer getAccountId() {
		return accountId;
	}
	public void setAccountId(Integer accountId) {
		this.accountId = accountId;
	}
	public String getFecha() {
		return fecha;
	}
	public void setFecha(String fecha) {
		this.fecha = fecha;
	}
	public String getPrn() {
		return prn;
	}
	public void setPrn(String prn) {
		this.prn = prn;
	}
	public String getNumCliente() {
		return numCliente;
	}
	public void setNumCliente(String numCliente) {
		this.numCliente = numCliente;
	}
	public String getCuentaAsociada() {
		return cuentaAsociada;
	}
	public void setCuentaAsociada(String cuentaAsociada) {
		this.cuentaAsociada = cuentaAsociada;
	}
	public String getProducto() {
		return producto;
	}
	public void setProducto(String producto) {
		this.producto = producto;
	}
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Integer getDiasVencido() {
		return diasVencido;
	}
	public void setDiasVencido(Integer diasVencido) {
		this.diasVencido = diasVencido;
	}
	public Integer getEtapa() {
		return etapa;
	}
	public void setEtapa(Integer etapa) {
		this.etapa = etapa;
	}

	public LocalDateTime getFechaPago() {
		return fechaPago;
	}
	public void setFechaPago(LocalDateTime fechaPago) {
		this.fechaPago = fechaPago;
	}

	
	public BigDecimal getLineaCredito() {
		return lineaCredito;
	}
	public void setLineaCredito(BigDecimal lineaCredito) {
		this.lineaCredito = lineaCredito;
	}
	public BigDecimal getPagoExigible() {
		return pagoExigible;
	}
	public void setPagoExigible(BigDecimal pagoExigible) {
		this.pagoExigible = pagoExigible;
	}
	public BigDecimal getCapitalNoExigible() {
		return capitalNoExigible;
	}
	public void setCapitalNoExigible(BigDecimal capitalNoExigible) {
		this.capitalNoExigible = capitalNoExigible;
	}
	public BigDecimal getCapitalExigible() {
		return capitalExigible;
	}
	public void setCapitalExigible(BigDecimal capitalExigible) {
		this.capitalExigible = capitalExigible;
	}
	public BigDecimal getInteresExigible() {
		return interesExigible;
	}
	public void setInteresExigible(BigDecimal interesExigible) {
		this.interesExigible = interesExigible;
	}
	public BigDecimal getInteresesCuentasBalances() {
		return interesesCuentasBalances;
	}
	public void setInteresesCuentasBalances(BigDecimal interesesCuentasBalances) {
		this.interesesCuentasBalances = interesesCuentasBalances;
	}
	public BigDecimal getInteresesCuentasOrden() {
		return InteresesCuentasOrden;
	}
	public void setInteresesCuentasOrden(BigDecimal interesesCuentasOrden) {
		InteresesCuentasOrden = interesesCuentasOrden;
	}
	public BigDecimal getInteresesMoratorios() {
		return interesesMoratorios;
	}
	public void setInteresesMoratorios(BigDecimal interesesMoratorios) {
		this.interesesMoratorios = interesesMoratorios;
	}
	@Override
	public String toString() {
		return "AccountReportWalletDTO [accountId=" + accountId + ", fecha=" + fecha + ", prn=" + prn + ", numCliente="
				+ numCliente + ", cuentaAsociada=" + cuentaAsociada + ", producto=" + producto + ", lineaCredito="
				+ lineaCredito + ", status=" + status + ", diasVencido=" + diasVencido + ", etapa=" + etapa
				+ ", fechaPago=" + fechaPago + ", pagoExigible=" + pagoExigible + ", capitalNoExigible="
				+ capitalNoExigible + ", capitalExigible=" + capitalExigible + ", interesExigible=" + interesExigible
				+ ", interesesCuentasBalances=" + interesesCuentasBalances + ", InteresesCuentasOrden="
				+ InteresesCuentasOrden + ", interesesMoratorios=" + interesesMoratorios + "]";
	}
    
	
	
    
}

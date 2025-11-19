package com.app.reporte.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TarjetaDTO {

	  private String prn;
	  private String marca;
	  private String tipo;
	  private String producto;
	  private BigDecimal lineaCredito;
	  private String transaccion;
	  private String preloanExternaId;
	  private LocalDateTime fecha;
	  private BigDecimal montoDisposicion;
	  private String tipoDisposicion;
	  private String canalDisposicion;
	  private String preloanSuffix;
	 
	  
	  
	public String getPreloanSuffix() {
		return preloanSuffix;
	}
	public void setPreloanSuffix(String preloanSuffix) {
		this.preloanSuffix = preloanSuffix;
	}
	public LocalDateTime getFecha() {
		return fecha;
	}
	public void setFecha(LocalDateTime fecha) {
		this.fecha = fecha;
	}
	public String getPreloanExternaId() {
		return preloanExternaId;
	}
	public void setPreloanExternaId(String preloanExternaId) {
		this.preloanExternaId = preloanExternaId;
	}

	public String getTipoDisposicion() {
		return tipoDisposicion;
	}
	public void setTipoDisposicion(String tipoDisposicion) {
		this.tipoDisposicion = tipoDisposicion;
	}
	public String getCanalDisposicion() {
		return canalDisposicion;
	}
	public void setCanalDisposicion(String canalDisposicion) {
		this.canalDisposicion = canalDisposicion;
	}
	public String getPrn() {
		return prn;
	}
	public void setPrn(String prn) {
		this.prn = prn;
	}
	public String getMarca() {
		return marca;
	}
	public void setMarca(String marca) {
		this.marca = marca;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public String getProducto() {
		return producto;
	}
	public void setProducto(String producto) {
		this.producto = producto;
	}
	public BigDecimal getLineaCredito() {
		return lineaCredito;
	}
	public void setLineaCredito(BigDecimal lineaCredito) {
		this.lineaCredito = lineaCredito;
	}
	public String getTransaccion() {
		return transaccion;
	}
	public void setTransaccion(String transaccion) {
		this.transaccion = transaccion;
	}
	public BigDecimal getMontoDisposicion() {
		return montoDisposicion;
	}
	public void setMontoDisposicion(BigDecimal montoDisposicion) {
		this.montoDisposicion = montoDisposicion;
	}
	@Override
	public String toString() {
		return "TarjetaDTO [prn=" + prn + ", marca=" + marca + ", tipo=" + tipo + ", producto=" + producto
				+ ", lineaCredito=" + lineaCredito + ", transaccion=" + transaccion + ", preloanExternaId="
				+ preloanExternaId + ", fecha=" + fecha + ", montoDisposicion=" + montoDisposicion
				+ ", tipoDisposicion=" + tipoDisposicion + ", canalDisposicion=" + canalDisposicion + ", preloanSuffix="
				+ preloanSuffix + "]";
	}

	  
}

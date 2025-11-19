package com.app.reporte.dto;

public class TarjetaColocacionDTO extends TarjetaDTO{

	private String idCliente;

	private String nombre;
		
	private String numeroCliente;
	
	private String cuentaAsociada;
	
	private String tarjeta;
	

	public String getTarjeta() {
		return tarjeta;
	}

	public void setTarjeta(String tarjeta) {
		this.tarjeta = tarjeta;
	}

	

	public String getIdCliente() {
		return idCliente;
	}

	public void setIdCliente(String idCliente) {
		this.idCliente = idCliente;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getNumeroCliente() {
		return numeroCliente;
	}

	public void setNumeroCliente(String numeroCliente) {
		this.numeroCliente = numeroCliente;
	}

	public String getCuentaAsociada() {
		return cuentaAsociada;
	}

	public void setCuentaAsociada(String cuentaAsociada) {
		this.cuentaAsociada = cuentaAsociada;
	}

	@Override
	public String toString() {
	    return "TarjetaColocacionDTO [idCliente=" + idCliente
	            + ", nombre=" + nombre
	            + ", numeroCliente=" + numeroCliente
	            + ", cuentaAsociada=" + cuentaAsociada
	            + "] " + super.toString(); 
	}
	
	
	
}

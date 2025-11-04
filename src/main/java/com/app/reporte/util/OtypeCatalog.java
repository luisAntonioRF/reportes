package com.app.reporte.util;

public enum OtypeCatalog {

	
	 A("Adelanto en efectivo", "ATM"),
	 C("Compra", "POS"),
	 E("E-commerce", " compras en E-commerce"),
	 D("Disposición en cajero", "ATM"),
	 O("Otro", "Sucursal");

	    private final String tipoDisposicion;
	    private final String canalDisposicion;

	    OtypeCatalog(String tipoDisposicion, String canalDisposicion) {
	        this.tipoDisposicion = tipoDisposicion;
	        this.canalDisposicion = canalDisposicion;
	    }

	    public String getTipoDisposicion() { return tipoDisposicion; }
	    public String getCanalDisposicion() { return canalDisposicion; }

	    public static OtypeCatalog fromCode(String code) {
	        for (OtypeCatalog c : values()) {
	            if (c.name().equalsIgnoreCase(code)) return c;
	        }
	        return null;
	    }
	    
}

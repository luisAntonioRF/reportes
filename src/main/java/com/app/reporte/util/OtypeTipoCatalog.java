package com.app.reporte.util;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public enum OtypeTipoCatalog {

	  MASTERCARD_RETAIL_PURCHASE("5", "Compra en línea o en tienda física."),
	    MASTERCARD_CASH_DISBURSEMENT("7", "Retiro de efectivo."),
	    MASTERCARD_WALLET_SETTLEMENT("8", "Pago con Apple Pay u otra app vinculada"),
	    MASTERCARD_COMPLETION_NO_PREAUTH("11", "Finalización de una compra sin autorización previa."),
	    AUTHORIZATION("A", "Compra presencial con PIN."),
	    ALLPOINT_CHARGEBACK("ad", "Contracargo por disputa de retiro en ATM Allpoint."),
	    ALLPOINT_CARD_LOAD("AO", "Recarga de tarjeta Kapital en cajero Allpoint."),
	    ALLPOINT_CARD_LOAD_REVERSAL("ao", "Cancelación de recarga en cajero Allpoint."),
	    ALLPOINT_SECOND_PRESENTMENT("ap", "Nueva presentación de transacción rechazada."),
	    ALLPOINT_EXCEPTION("as", "Error técnico o sospecha de fraude en Allpoint."),
	    BALANCE_INQUIRY("B", "Consulta de saldo desde ATM."),
	    COMPLETION("C", "Finalización de una compra previamente iniciada."),
	    MASTERCARD_CREDIT_REVERSAL("D", "Reembolso por parte del comercio."),
	    DISCOVER_CHARGEBACK("DC", "Disputa de cliente por compra con Discover."),
	    DISCOVER_EXCEPTION("DE", "Error en una transacción Discover."),
	    MAESTRO_CHARGEBACK("dh", "Disputa de compra con Maestro."),
	    MAESTRO_SECOND_PRESENTMENT("dj", "Reintento de débito Maestro."),
	    MAESTRO_EXCEPTION("dk", "Error técnico en transacción Maestro."),
	    DISCOVER_SECOND_PRESENTMENT("ds", "Segundo intento de débito Discover rechazado."),
	    FEE_COLLECTION("F", "Comisiones aplicadas por Kapital."),
	    MASTERCARD_CHARGEBACK("H", "Disputa de cliente por compra con Mastercard."),
	    MASTERCARD_SECOND_PRESENTMENT("I", "Reintento de débito o revisión de comisión de ATM."),
	    MASTERCARD_ADJUSTMENT("J", "Ajuste por decisión de arbitraje."),
	    PREPAID_LOAD("K", "Recarga o cancelación de recarga de tarjeta Kapital."),
	    PREAUTH_NOT_COMPLETED("L", "Preautorización que no se completó correctamente."),
	    DISCOVER_LOAD("LC", "Carga de tarjeta prepagada Discover."),
	    DISCOVER_LOAD_REVERSAL("lc", "Cancelación de carga Discover."),
	    FORCE_POST("M", "Transacción fuera de línea sin autorización previa."),
	    MASTERCARD_LOAD("ML", "Carga directa a la tarjeta Kapital."),
	    MAESTRO_LOAD_REVERSAL("MX", "Carga o cancelación vía Maestro."),
	    PREAUTH_COMPLETED("P", "Compra completada tras preautorización."),
	    PULSE_CHARGEBACK("pd", "Disputa de cliente sobre transacción Pulse."),
	    PULSE_SECOND_PRESENTMENT("pe", "Reintento de débito Pulse."),
	    PULSE_EXCEPTION("pz", "Error técnico en transacción Pulse."),
	    TOKENIZATION_REQUEST("Q", "Token para tarjeta virtual Kapital."),
	    MASTERCARD_LOAD_REVERSAL("q", "Cancelación de recarga Mastercard."),
	    AUTH_REVERSAL("R", "Anulación de autorización errónea o duplicada."),
	    STAR_SECOND_PRESENTMENT("SC", "Reintento vía red STAR."),
	    STAR_EXCEPTION("SS", "Error técnico en STAR."),
	    STAR_CHARGEBACK("se", "Disputa de cliente por compra vía STAR."),
	    TRANSFER_OR_CARD_LOAD("T", "Transferencia entre cuentas Kapital o carga de tarjeta."),
	    TEMPORARY_CREDIT("tc", "Crédito provisional por disputa en proceso."),
	    REVERSAL_DEBITING_BALANCE("V", "Corrección de saldo por error o rechazo."),
	    ATM_WITHDRAWAL_CASH("W", "Retiro de efectivo con crédito Kapital."),
	    ATM_WITHDRAWAL_CREDIT("Y", "Retiro en efectivo tipo “cash advance”."),
	    MERCHANT_CREDIT("Z", "Reembolso de compra por parte del comercio.");

	    private final String code;
	    private final String description;

	    OtypeTipoCatalog(String code, String description) {
	        this.code = code;
	        this.description = description;
	    }

	    public String getCode() { return code; }
	    public String getDescription() { return description; }

	    private static final Map<String, OtypeTipoCatalog> MAP =
	        Arrays.stream(values()).collect(Collectors.toMap(OtypeTipoCatalog::getCode, e -> e));

	    public static Optional<OtypeTipoCatalog> fromCode(String code) {
	        return Optional.ofNullable(MAP.get(code));
	    }
	
}

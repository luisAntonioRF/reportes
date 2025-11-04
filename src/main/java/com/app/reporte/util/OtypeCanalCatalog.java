package com.app.reporte.util;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public enum OtypeCanalCatalog {

	MASTERCARD_RETAIL_PURCHASE("5", "TPV/Ecommerce"),
    MASTERCARD_CASH_DISBURSEMENT("7", "ATM"),
    MASTERCARD_WALLET_SETTLEMENT("8", "Ecommerce/TPV"),
    MASTERCARD_COMPLETION_NO_PREAUTH("11", "TPV/Ecommerce"),
    AUTHORIZATION("A", "TPV"),
    ALLPOINT_CHARGEBACK("ad", "Otro"),
    ALLPOINT_CARD_LOAD("AO", "ATM"),
    ALLPOINT_CARD_LOAD_REVERSAL("ao", "ATM"),
    ALLPOINT_SECOND_PRESENTMENT("ap", "ATM"),
    ALLPOINT_EXCEPTION("as", "ATM"),
    BALANCE_INQUIRY("B", "ATM"),
    COMPLETION("C", "TPV/Ecommerce"),
    MASTERCARD_CREDIT_REVERSAL("D", "TPV/Ecommerce"),
    DISCOVER_CHARGEBACK("DC", "Otro"),
    DISCOVER_EXCEPTION("DE", "Otro"),
    MAESTRO_CHARGEBACK("dh", "TPV"),
    MAESTRO_SECOND_PRESENTMENT("dj", "TPV"),
    MAESTRO_EXCEPTION("dk", "TPV"),
    DISCOVER_SECOND_PRESENTMENT("ds", "TPV/Otro"),
    FEE_COLLECTION("F", "Otro"),
    MASTERCARD_CHARGEBACK("H", "TPV/Ecommerce"),
    MASTERCARD_SECOND_PRESENTMENT("I", "ATM"),
    MASTERCARD_ADJUSTMENT("J", "Otro"),
    PREPAID_LOAD("K", "ATM"),
    PREAUTH_NOT_COMPLETED("L", "TPV/Ecommerce"),
    DISCOVER_LOAD("LC", "ATM/Otro"),
    DISCOVER_LOAD_REVERSAL("lc", "ATM/Otro"),
    FORCE_POST("M", "TPV/Ecommerce"),
    MASTERCARD_LOAD("ML", "TPV/Otro"),
    MAESTRO_LOAD_REVERSAL("MX", "TPV/Otro"),
    PREAUTH_COMPLETED("P", "TPV/Ecommerce"),
    PULSE_CHARGEBACK("pd", "Otro"),
    PULSE_SECOND_PRESENTMENT("pe", "TPV"),
    PULSE_EXCEPTION("pz", "Otro"),
    TOKENIZATION_REQUEST("Q", "Otro"),
    MASTERCARD_LOAD_REVERSAL("q", "TPV"),
    AUTH_REVERSAL("R", "TPV/Ecommerce"),
    STAR_SECOND_PRESENTMENT("SC", "TPV"),
    STAR_EXCEPTION("SS", "TPV"),
    STAR_CHARGEBACK("se", "TPV/Ecommerce"),
    TRANSFER_OR_CARD_LOAD("T", "Ecommerce/Otro"),
    TEMPORARY_CREDIT("tc", "Otro"),
    REVERSAL_DEBITING_BALANCE("V", "Otro"),
    ATM_WITHDRAWAL_CASH("W", "ATM"),
    ATM_WITHDRAWAL_CREDIT("Y", "ATM"),
    MERCHANT_CREDIT("Z", "TPV/Ecommerce");

    private final String code;
    private final String canal;

    OtypeCanalCatalog(String code, String canal) {
        this.code = code;
        this.canal = canal;
    }

    public String getCode() { return code; }
    public String getCanal() { return canal; }

    private static final Map<String, OtypeCanalCatalog> MAP =
        Arrays.stream(values()).collect(Collectors.toMap(OtypeCanalCatalog::getCode, e -> e));

    public static Optional<OtypeCanalCatalog> fromCode(String code) {
        return Optional.ofNullable(MAP.get(code));
    }
}

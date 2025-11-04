package com.app.reporte.util;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ReporteUtil {

	
	   /**
     * Filtra una lista y además asigna al objeto el valor después de la 'M'
     * en el preloanId, si coincide con el PRN.
     */
    public static <T> List<T> filtrarPorPreloanId(
            List<T> list,
            Function<T, String> prnExtractor,
            Function<T, String> preloanIdExtractor,
            BiConsumer<T, String> suffixSetter) {
    	
    	if (list == null || list.isEmpty()) return Collections.emptyList();

        return list.stream()
                .filter(item -> {
                    String prn = prnExtractor.apply(item);
                    String preloanId = preloanIdExtractor.apply(item);

                    if (prn == null || preloanId == null) return false;
                    
                    if (prn == null || prn.isBlank() || preloanId == null || preloanId.isBlank()) return false;
                    
                    int indexM = preloanId.indexOf('M');
                    if (indexM == -1) return false;

                    String antesDeM = preloanId.substring(0, indexM);
                    if (!antesDeM.equals(prn)) return false;

                  
                    String despuesDeM = preloanId.substring(indexM + 1);
                    suffixSetter.accept(item, despuesDeM);

                    return true;
                })
                .collect(Collectors.toList());
    }
    
    
    
    public static final String QUERY_INICIAL = """
			SELECT
				  c.external_id        AS prn,
				  m.brand              AS marca,
				  m.card_type          AS tipo,
				  p.name               AS producto,
				  crl.amount           AS linea_credito,
				  lo.loan_id           AS transaccion,
				  pl.external_id       AS preloan_externa_id,
				  lo.created_dt        AS fact_loans_created_dt
				FROM datamart.credit_card c
				JOIN mo.cliente_tarjetas      m   ON m.card_external_id = c.external_id
				JOIN datamart.fact_account    acc ON acc.ide = c.fact_account_id
				JOIN datamart.dim_product     p   ON p.product_id = acc.product_id
				JOIN datamart.credit_line     crl ON crl.fact_account_id = acc.ide
				JOIN datamart.fact_loans      lo  ON lo.card_account_id = acc.card_account_id
				JOIN datamart.preloan         pl  ON pl.pre_loan_id  = lo.pre_loan_id
				WHERE  lo.created_dt >= :startDate
				  AND lo.created_dt <  :endDate
											    	""";
    
    public static final String QUERY_SECUNDARIO = """
    		 SELECT billing_amt, otype
	        FROM wh_event.public.wh_auth
	        WHERE auth_id = ?
	        ORDER BY id   -- usa created_dt si existe; evita ordenar por monto
    		""";
    public static final int COL_TRANSACCION=8;
}

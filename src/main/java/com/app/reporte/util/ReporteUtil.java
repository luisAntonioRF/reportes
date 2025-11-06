package com.app.reporte.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.app.reporte.dto.TarjetaDTO;

public class ReporteUtil {
	
	private static final Logger log = LoggerFactory.getLogger(ReporteUtil.class);
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
    
    public static Path generarExcel(
            List<TarjetaDTO> tarjetas,
            String baseDir,
            String rutaPlantilla,   
            String prefix           
    ) {
        ZoneId MX = ZoneId.of("America/Mexico_City");
        ZonedDateTime now = ZonedDateTime.now(MX);

        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());
        String ts = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(now);

        Path dir = Paths.get(baseDir, year, month);
        String fileName = prefix + ts + ".xlsx";          
        Path destino = dir.resolve(fileName);

        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
        	log.error("No se pudo crear el directorio: {}", dir, e);
        }

       
        try (InputStream plantillaStream = ReporteUtil.class.getResourceAsStream(rutaPlantilla)) {
            if (plantillaStream == null) {
                throw new IllegalStateException("No se encontró la plantilla en el classpath: " + rutaPlantilla);
            }

            try (Workbook workbook = new XSSFWorkbook(plantillaStream);
                 FileOutputStream fileOut = new FileOutputStream(destino.toFile())) {

                Sheet sheet = workbook.getSheetAt(0);
                int rowNum = 4;

                for (TarjetaDTO t : tarjetas) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(nz(t.getPrn()));
                    row.createCell(1).setCellValue(nz(t.getMarca()));
                    row.createCell(2).setCellValue(nz(t.getTipo()));
                    row.createCell(3).setCellValue(nz(t.getProducto()));
                    row.createCell(4).setCellValue(nz(t.getLineaCredito()).doubleValue());
                    row.createCell(5).setCellValue(nz(t.getMontoDisposicion()).doubleValue());
                    row.createCell(6).setCellValue(nz(t.getTipoDisposicion()));
                    row.createCell(7).setCellValue(nz(t.getCanalDisposicion()));
                    row.createCell(ReporteUtil.COL_TRANSACCION).setCellValue(nz(t.getTransaccion()));
                }
                workbook.write(fileOut);
                log.info("Excel generado correctamente en: {}", destino);
            }
        } catch (IOException e) {
        	log.error("Error generando Excel en {}", destino, e);
          
        }

        return destino;
    }
    
	private static String nz(String v) {
	    return v == null ? "" : v; 
	}

	private static BigDecimal nz(BigDecimal v) {
	    return v == null ? BigDecimal.ZERO : v; 
	}
    
    
    public static final String QUERY_INICIAL = """
			 SELECT
				  c.external_id        AS prn,
				  p.name               AS producto,
				  crl.amount           AS linea_credito,
				  lo.loan_id           AS transaccion,
				  pl.external_id       AS preloan_externa_id,
				  lo.created_dt        AS fact_loans_created_dt
				FROM db_datamart.credit_card c
				JOIN db_datamart.fact_account    acc ON acc.ide = c.fact_account_id
				JOIN db_datamart.dim_product     p   ON p.product_id = acc.product_id
				JOIN db_datamart.credit_line     crl ON crl.fact_account_id = acc.ide
				JOIN db_datamart.fact_loans      lo  ON lo.card_account_id = acc.card_account_id
				JOIN db_datamart.preloan         pl  ON pl.pre_loan_id  = lo.pre_loan_id
				WHERE  lo.created_dt >= :startDate
				  AND lo.created_dt <   :endDate
											    	""";
    
	public static final String QUERY_SECUNDARIO = """
				SELECT card_external_id AS prn,
			       brand AS marca,
			       card_type AS tipo
			FROM mo.cliente_tarjetas
			WHERE card_external_id IN (:prns)
				""";
    
    
    public static final String QUERY_FINAL = """
    		 SELECT billing_amt, otype
	        FROM eventos_mo.public.wh_auth
	        WHERE auth_id = ?
	        ORDER BY id  
    		""";
    public static final int COL_TRANSACCION=8;
}

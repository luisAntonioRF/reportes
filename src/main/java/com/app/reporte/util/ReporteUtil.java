package com.app.reporte.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDate;
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
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import com.app.reporte.dto.TarjetaColocacionDTO;
import com.app.reporte.dto.TarjetaDTO;

public class ReporteUtil {
	
	private static final Logger log = LoggerFactory.getLogger(ReporteUtil.class);
	
	private static final ZoneId MX = ZoneId.of("America/Mexico_City");
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
	
	private static Integer nz(Integer v) {
	    return v == null ? 0 : v;
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
    public static final String QUERY_INICIAL_V2 = """
			 SELECT
				  c.external_id        AS prn,
				  cus.id               AS id_cliente,
				  cus.display_name     AS nombre,
				  cus.manage_external_id AS numero_cliente,
				  acc.card_account_id  AS cuenta_asociada,
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
				JOIN db_datamart.dim_customer    cus ON acc.customer_id  = cus.customer_id
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
    public static final int COL_TRANSACCION_V2=12;
    
    
    
    /**
     * Genera un rango de fechas entre el inicio del día anterior (T-1)
     * y el inicio del día actual, útil para consultas de datos del día anterior.
     *
     * @return MapSqlParameterSource con "startDate" y "endDate"
     */
    public static MapSqlParameterSource getYesterdayRangeParams() {
        // Día T-1 y día actual
        final LocalDate today = LocalDate.now(MX);
        final LocalDate tMinus1 = today.minusDays(1);

        // Inicio de T-1 y de T
        final ZonedDateTime startZdt = tMinus1.atStartOfDay(MX);
        final ZonedDateTime endZdt = today.atStartOfDay(MX);

        // Convertir a Timestamp
        final Timestamp start = Timestamp.from(startZdt.toInstant());
        final Timestamp end = Timestamp.from(endZdt.toInstant());

        log.info("Rango de fechas generado: start={}, end={}", start, end);

        // Parámetros SQL
        return new MapSqlParameterSource()
                .addValue("startDate", start, java.sql.Types.TIMESTAMP)
                .addValue("endDate", end, java.sql.Types.TIMESTAMP);
    }
    
    public static Path generarExcelV2(
            List<TarjetaColocacionDTO> tarjetas,
            String baseDir,
            String rutaPlantilla2,   
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

       
        try (InputStream plantillaStream = ReporteUtil.class.getResourceAsStream(rutaPlantilla2)) {
            if (plantillaStream == null) {
                throw new IllegalStateException("No se encontró la plantilla en el classpath: " + rutaPlantilla2);
            }

            try (Workbook workbook = new XSSFWorkbook(plantillaStream);
                 FileOutputStream fileOut = new FileOutputStream(destino.toFile())) {

                Sheet sheet = workbook.getSheetAt(0);
                int rowNum = 4;

                for (TarjetaColocacionDTO t : tarjetas) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(nz(t.getTarjeta()));
                    row.createCell(1).setCellValue(nz(t.getMarca()));
                    row.createCell(2).setCellValue(nz(t.getTipo()));
                    row.createCell(3).setCellValue(nz(t.getProducto()));
                    row.createCell(4).setCellValue(nz(t.getPrn()));
                    row.createCell(5).setCellValue(nz(t.getIdCliente()));
                    row.createCell(6).setCellValue(nz(t.getNombre()));
                    row.createCell(7).setCellValue(nz(t.getNumeroCliente()));
                    row.createCell(8).setCellValue(nz(t.getCuentaAsociada()));
                    row.createCell(9).setCellValue(nz(t.getMontoDisposicion()).doubleValue());
                    row.createCell(10).setCellValue(nz(t.getTipoDisposicion()));
                    row.createCell(11).setCellValue(nz(t.getCanalDisposicion()));
                    row.createCell(ReporteUtil.COL_TRANSACCION_V2).setCellValue(nz(t.getTransaccion()));
                    row.createCell(13).setCellValue(nz(t.getLineaCredito()).doubleValue());
                    
                }
                
                workbook.write(fileOut);
                log.info("Excel generado correctamente en: {}", destino);
            }
        } catch (IOException e) {
        	log.error("Error generando Excel en {}", destino, e);
          
        }

        return destino;
    }
    
 // ------- helpers -------
 	public static int safeParseInt(String s) {
 		try {
 			return Integer.parseInt(s);
 		} catch (Exception e) {
 			return Integer.MAX_VALUE;
 		}
 	}

 	public static String nullToEmpty(String s) {
 		return s == null ? "" : s;
 	}

 	public static BigDecimal asBigDecimal(Object o) {
 		if (o == null)
 			return null;
 		if (o instanceof BigDecimal bd)
 			return bd;
 		if (o instanceof Number n)
 			return BigDecimal.valueOf(n.doubleValue());
 		try {
 			return new BigDecimal(o.toString());
 		} catch (Exception e) {
 			return null;
 		}
 	}

 	public static String asString(Object o) {
 		return o == null ? null : o.toString();
 	}

 	public static TarjetaDTO copyOf(TarjetaDTO src) {
 		TarjetaDTO t = new TarjetaDTO();
 		t.setPrn(src.getPrn());
 		t.setMarca(src.getMarca());
 		t.setTipo(src.getTipo());
 		t.setProducto(src.getProducto());
 		t.setLineaCredito(src.getLineaCredito());
 		t.setTransaccion(src.getTransaccion());
 		t.setMontoDisposicion(src.getMontoDisposicion());
 		t.setTipoDisposicion(src.getTipoDisposicion());
 		t.setCanalDisposicion(src.getCanalDisposicion());
 		return t;
 	}

 	public static TarjetaColocacionDTO copyOfV2(TarjetaColocacionDTO src) {
 		TarjetaColocacionDTO t = new TarjetaColocacionDTO();
 		t.setPrn(src.getPrn());
 		t.setMarca(src.getMarca());
 		t.setTipo(src.getTipo());
 		t.setNombre(src.getNombre());
 		t.setIdCliente(src.getIdCliente());
 		t.setNumeroCliente(src.getNumeroCliente());
 		t.setCuentaAsociada(src.getCuentaAsociada());
 		t.setProducto(src.getProducto());
 		t.setLineaCredito(src.getLineaCredito());
 		t.setTransaccion(src.getTransaccion());
 		t.setMontoDisposicion(src.getMontoDisposicion());
 		t.setTipoDisposicion(src.getTipoDisposicion());
 		t.setCanalDisposicion(src.getCanalDisposicion());
 		return t;
 	}
}

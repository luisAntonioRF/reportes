package com.app.reporte.repository;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import com.app.reporte.dto.TarjetaDTO;
import com.app.reporte.mapper.TarjetaRowMapper;
import com.app.reporte.util.OtypeCanalCatalog;
import com.app.reporte.util.OtypeTipoCatalog;
import com.app.reporte.util.ReporteUtil;

@Repository
public class ReporteRepository implements IReporteRepository{
	
    private static final Logger log = LoggerFactory.getLogger(ReporteRepository.class);

	 private final NamedParameterJdbcTemplate datamartJdbcTemplate;
	 private final JdbcTemplate whJdbcTemplate;
	 private final NamedParameterJdbcTemplate moJdbcTemplate;


	    public ReporteRepository(
	        @Qualifier("datamartNamedJdbc") NamedParameterJdbcTemplate datamartJdbcTemplate,
	        @Qualifier("whJdbcTemplate") JdbcTemplate whJdbcTemplate,
	        @Qualifier("moNamedJdbc") NamedParameterJdbcTemplate moJdbcTemplate
	    ) {
	        this.datamartJdbcTemplate = datamartJdbcTemplate;
	        this.whJdbcTemplate = whJdbcTemplate;
	        this.moJdbcTemplate=moJdbcTemplate;
	    }
    
	    @Value("${excel.output.base-dir}")
	    private String baseDir;

	    @Value("${excel.template.path}")
	    private String rutaPlantilla;

	    @Value("${excel.output.filename-prefix:reporte_}")
	    private String prefix;
    
	@Override
	public void obtainReporte() {
		
		List<Map<String, Object>> resultadoQuerySecundario = new ArrayList<>();
		
		List<TarjetaDTO> resultadoQueryInicial=null;
		
		final ZoneId MX = ZoneId.of("America/Mexico_City");

	    // Día T-1
	    final LocalDate today = LocalDate.now(MX);
	    final LocalDate tMinus1 = today.minusDays(1);

	    final ZonedDateTime startZdt = tMinus1.atStartOfDay(MX);
	    final ZonedDateTime endZdt   = today.atStartOfDay(MX);

	    
	    final Timestamp start = Timestamp.from(startZdt.toInstant());
	    final Timestamp end   = Timestamp.from(endZdt.toInstant());

	    log.info("Rango de fechas: start={}, end={}", start, end);
	    
	    final MapSqlParameterSource params = new MapSqlParameterSource()
	            .addValue("startDate", start, java.sql.Types.TIMESTAMP)
	            .addValue("endDate",   end,   java.sql.Types.TIMESTAMP);

	  try {
		   resultadoQueryInicial = datamartJdbcTemplate.query(ReporteUtil.QUERY_INICIAL,params,new TarjetaRowMapper());
	} catch (Exception e) {
		 log.error("Error consultando QUERY_INICIAL. Rango {} a {}.", startZdt, endZdt, e);
	      return;
	}
	  
	  if (resultadoQueryInicial == null || resultadoQueryInicial.isEmpty()) {
	        log.info("Sin datos para ejecutar el segundo query {} a {}.", startZdt, endZdt);
	        return;
	    }
	  
	  log.info("Filas obtenidas: {}", resultadoQueryInicial.size());
	  
	
	  try {
		    resultadoQuerySecundario = moJdbcTemplate.queryForList(
		    		ReporteUtil.QUERY_SECUNDARIO ,
		        Map.of("prns", resultadoQueryInicial.stream()
		                .map(TarjetaDTO::getPrn)
		                .collect(Collectors.toList()))
		    );
		    
		    Map<String, Map<String, Object>> porPrn = resultadoQuerySecundario.stream()
		  	      .collect(Collectors.toMap(
		  	          r -> (String) r.get("prn"),
		  	          r -> r
		  	      ));
		    
		    resultadoQueryInicial.forEach(t -> {
			      Map<String, Object> datos = porPrn.get(t.getPrn());
			      if (datos != null) {
			          t.setMarca((String) datos.get("marca"));
			          t.setTipo((String) datos.get("tipo"));
			      }
			  });
		    
		} catch (Exception e) {
		    log.error("Error ejecutando consulta secundaria de marcas/tipos", e);
		}
	  
	 
	  List<TarjetaDTO> filtradas = ReporteUtil.filtrarPorPreloanId(resultadoQueryInicial, TarjetaDTO::getPrn, TarjetaDTO::getPreloanExternaId,TarjetaDTO::setPreloanSuffix);
	    
	  List<TarjetaDTO> complemento =  this.complementData(filtradas);
	  
	  this.generarExcel(complemento);
	 
	}
	
	 public Path generarExcel(List<TarjetaDTO> complemento) {
	        return ReporteUtil.generarExcel(complemento, baseDir, rutaPlantilla, prefix);
	    }
	

	public List<TarjetaDTO> complementData(List<TarjetaDTO> tarjetas) {
		
	    Map<String, List<TarjetaDTO>> porPrnYTxn = tarjetas.stream()
	        .sorted(Comparator
	            .comparing(TarjetaDTO::getPrn, Comparator.nullsFirst(String::compareTo))
	            .thenComparing(t -> safeParseInt(t.getTransaccion())))
	        .collect(Collectors.groupingBy(
	            t -> t.getPrn() + "|" + nullToEmpty(t.getTransaccion()),
	            LinkedHashMap::new,
	            Collectors.toList()
	        ));

	   

	    List<TarjetaDTO> out = new ArrayList<>();
	    List<Map<String,Object>> wh =null;
	    
	    for (var entry : porPrnYTxn.entrySet()) {
	        List<TarjetaDTO> baseGroup = entry.getValue();
	        TarjetaDTO plantilla = baseGroup.get(0);  
	        String prn = plantilla.getPreloanSuffix();
	        Integer prnNew = Integer.parseInt(prn);

	        if (prnNew < 0) {
	            log.warn("preloanSuffix no numérico o vacío para clave {}. Se deja baseGroup sin complementar.", entry.getKey());
	            out.addAll(baseGroup);
	            continue;
	        }
	        
	       try {
	    	  wh = whJdbcTemplate.queryForList(ReporteUtil.QUERY_FINAL, prnNew);
		} catch (Exception e) {
			log.error("Error consultando QUERY_SECUNDARIO. a {}.", e);
		     
		}

	        if (wh.isEmpty()) {
	            out.addAll(baseGroup);
	            continue;
	        }
	        
	        for (Map<String,Object> fila : wh) {
	            TarjetaDTO t = copyOf(plantilla);     
	            BigDecimal monto = asBigDecimal(fila.get("billing_amt"));
	            if (monto != null) t.setMontoDisposicion(monto);

	            String otype = asString(fila.get("otype"));
	            if (otype != null && !otype.isBlank()) {
	            	 //Obtener descripción del tipo
	                OtypeTipoCatalog.fromCode(otype).ifPresent(tipo ->
	                    t.setTipoDisposicion(tipo.getDescription())
	                );

	                //Obtener canal correspondiente
	                OtypeCanalCatalog.fromCode(otype).ifPresent(canal ->
	                    t.setCanalDisposicion(canal.getCanal())
	                );
	            }
	         
	            out.add(t);
	        }
	    }

	    return out;
	}
	
	// ------- helpers -------
	private static int safeParseInt(String s){ try { return Integer.parseInt(s); } catch(Exception e){ return Integer.MAX_VALUE; } }
	private static String nullToEmpty(String s){ return s == null ? "" : s; }
	private static BigDecimal asBigDecimal(Object o){
	    if (o == null) return null;
	    if (o instanceof BigDecimal bd) return bd;
	    if (o instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
	    try { return new BigDecimal(o.toString()); } catch(Exception e){ return null; }
	}
	private static String asString(Object o){ return o == null ? null : o.toString(); }

	private static TarjetaDTO copyOf(TarjetaDTO src) {
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
	
	
}

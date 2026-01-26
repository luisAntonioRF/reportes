package com.app.reporte.repository;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import com.app.reporte.dto.AccountReportWalletDTO;
import com.app.reporte.dto.InteresCuentaDTO;
import com.app.reporte.dto.SumOfInterestDto;
import com.app.reporte.dto.TarjetaColocacionDTO;
import com.app.reporte.dto.TarjetaDTO;
import com.app.reporte.mapper.TarjetaColocacionRowMapper;
import com.app.reporte.mapper.TarjetaRowMapper;
import com.app.reporte.mapper.WalletReportRowMapper;
import com.app.reporte.util.OtypeCanalCatalog;
import com.app.reporte.util.OtypeTipoCatalog;
import com.app.reporte.util.ReporteUtil;

@Repository
public class ReporteRepository implements IReporteRepository {

	private static final Logger log = LoggerFactory.getLogger(ReporteRepository.class);

	private final NamedParameterJdbcTemplate datamartJdbcTemplate;
	private final JdbcTemplate whJdbcTemplate;
	private final NamedParameterJdbcTemplate moJdbcTemplate;

	public ReporteRepository(@Qualifier("datamartNamedJdbc") NamedParameterJdbcTemplate datamartJdbcTemplate,
			@Qualifier("whJdbcTemplate") JdbcTemplate whJdbcTemplate,
			@Qualifier("moNamedJdbc") NamedParameterJdbcTemplate moJdbcTemplate) {
		this.datamartJdbcTemplate = datamartJdbcTemplate;
		this.whJdbcTemplate = whJdbcTemplate;
		this.moJdbcTemplate = moJdbcTemplate;
	}

	@Value("${excel.output.base-dir}")
	private String baseDir;

	@Value("${excel.template.path}")
	private String rutaPlantilla;
	
	@Value("${excel.template.path2}")
	private String rutaPlantilla2;
	
	@Value("${excel.template.path3}")
	private String rutaPlantilla3;

	@Value("${excel.output.filename-prefix}")
	private String prefix;
	
	@Value("${excel.output.filename-prefix2}")
	private String prefix2;
	
	@Value("${excel.output.filename-prefix3}")
	private String prefix3;

	@Override
	public void obtainReporte() {

		List<Map<String, Object>> resultadoQuerySecundario = new ArrayList<>();

		List<TarjetaDTO> resultadoQueryInicial = null;

		MapSqlParameterSource params = ReporteUtil.getYesterdayRangeParams();

		try {
			resultadoQueryInicial = datamartJdbcTemplate.query(ReporteUtil.QUERY_INICIAL, params,
					new TarjetaRowMapper());
		} catch (Exception e) {
			log.error("Error consultando QUERY_INICIAL con parámetros: startDate={}, endDate={}. Causa: {}",
					params.getValue("startDate"), params.getValue("endDate"), e.getMessage(), e);

		}

		if (resultadoQueryInicial == null || resultadoQueryInicial.isEmpty()) {
			log.error("Sin datos para ejecutar el primer query. {}");
			return;
		}

		log.info("Filas obtenidas: {}", resultadoQueryInicial.size());

		try {
			resultadoQuerySecundario = moJdbcTemplate.queryForList(ReporteUtil.QUERY_SECUNDARIO, Map.of("prns",
					resultadoQueryInicial.stream().map(TarjetaDTO::getPrn).collect(Collectors.toList())));

			Map<String, Map<String, Object>> porPrn = resultadoQuerySecundario.stream()
					.collect(Collectors.toMap(r -> (String) r.get("prn"), r -> r));

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

		List<TarjetaDTO> filtradas = ReporteUtil.filtrarPorPreloanId(resultadoQueryInicial, TarjetaDTO::getPrn,
				TarjetaDTO::getPreloanExternaId, TarjetaDTO::setPreloanSuffix);

		List<TarjetaDTO> complemento = this.complementData(filtradas);

		this.generarExcel(complemento);

	}
	
	
	public Path generarExcel(List<TarjetaDTO> complemento) {
		return ReporteUtil.generarExcel(complemento, baseDir, rutaPlantilla, prefix);
	}

	public List<TarjetaDTO> complementData(List<TarjetaDTO> tarjetas) {

		Map<String, List<TarjetaDTO>> porPrnYTxn = tarjetas.stream()
				.sorted(Comparator.comparing(TarjetaDTO::getPrn, Comparator.nullsFirst(String::compareTo))
						.thenComparing(t -> ReporteUtil.safeParseInt(t.getTransaccion())))
				.collect(Collectors.groupingBy(t -> t.getPrn() + "|" + ReporteUtil.nullToEmpty(t.getTransaccion()),
						LinkedHashMap::new, Collectors.toList()));

		List<TarjetaDTO> out = new ArrayList<>();
		List<Map<String, Object>> wh = null;

		for (var entry : porPrnYTxn.entrySet()) {
			List<TarjetaDTO> baseGroup = entry.getValue();
			TarjetaDTO plantilla = baseGroup.get(0);
			String prn = plantilla.getPreloanSuffix();
			Integer prnNew = Integer.parseInt(prn);

			if (prnNew < 0) {
				log.warn("preloanSuffix no numérico o vacío para clave {}. Se deja baseGroup sin complementar.",
						entry.getKey());
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

			for (Map<String, Object> fila : wh) {
				TarjetaDTO t = ReporteUtil.copyOf(plantilla);
				BigDecimal monto = ReporteUtil.asBigDecimal(fila.get("amount"));
				if (monto != null)
					t.setMontoDisposicion(monto);

				String otype = ReporteUtil.asString(fila.get("otype"));
				if (otype != null && !otype.isBlank()) {
					// Obtener descripción del tipo
					OtypeTipoCatalog.fromCode(otype).ifPresent(tipo -> t.setTipoDisposicion(tipo.getDescription()));

					// Obtener canal correspondiente
					OtypeCanalCatalog.fromCode(otype).ifPresent(canal -> t.setCanalDisposicion(canal.getCanal()));
				}

				out.add(t);
			}
		}

		return out;
	}

	/*
	 * 
	 * INICIA EL REPORTE #2
	 * */
	@Override
	public void obtainReporteColocacionRepository() {

		List<Map<String, Object>> resultadoQuerySecundario = new ArrayList<>();

		MapSqlParameterSource params = ReporteUtil.getYesterdayRangeParams();

		List<TarjetaColocacionDTO> resultadoQueryInicialV2 = null;

		try {
			resultadoQueryInicialV2 = datamartJdbcTemplate.query(ReporteUtil.QUERY_INICIAL_V2, params,
					new TarjetaColocacionRowMapper());
		} catch (Exception e) {
			log.error("Error consultando QUERY_INICIAL con parámetros: startDate={}, endDate={}. Causa: {}",
					params.getValue("startDate"), params.getValue("endDate"), e.getMessage(), e);

		}

		if (resultadoQueryInicialV2 == null || resultadoQueryInicialV2.isEmpty()) {
			log.error("Sin datos para ejecutar el primer query. {}");
			return;
		}

		log.info("Filas obtenidas: {}", resultadoQueryInicialV2.size());

		try {
			resultadoQuerySecundario = moJdbcTemplate.queryForList(ReporteUtil.QUERY_SECUNDARIO_V2, Map.of("prns",
					resultadoQueryInicialV2.stream().map(TarjetaDTO::getPrn).collect(Collectors.toList())));

			Map<String, Map<String, Object>> porPrn = resultadoQuerySecundario.stream()
					.collect(Collectors.toMap(r -> (String) r.get("prn"), r -> r));

			resultadoQueryInicialV2.forEach(t -> {
				Map<String, Object> datos = porPrn.get(t.getPrn());
				if (datos != null) {
					t.setMarca((String) datos.get("marca"));
					t.setTipo((String) datos.get("tipo"));
					t.setTarjeta((String) datos.get("tarjeta"));
				}
			});

		} catch (Exception e) {
			log.error("Error ejecutando consulta secundaria de marcas/tipos", e);
		}

		List<TarjetaColocacionDTO> filtradas = ReporteUtil.<TarjetaColocacionDTO>filtrarPorPreloanId(
				resultadoQueryInicialV2, 
				TarjetaColocacionDTO::getPrn, 
				TarjetaColocacionDTO::getPreloanExternaId, 
				(t, s) -> t.setPreloanSuffix(s) 
		);

		List<TarjetaColocacionDTO> respV2 = this.complementoColocacionData(filtradas);

		this.generarExcelV2(respV2);
	}

	public Path generarExcelV2(List<TarjetaColocacionDTO> complemento) {
		return ReporteUtil.generarExcelV2(complemento, baseDir, rutaPlantilla2, prefix2);
	}

	public List<TarjetaColocacionDTO> complementoColocacionData(List<TarjetaColocacionDTO> tarjetas) {

		Map<String, List<TarjetaColocacionDTO>> porPrnYTxn = tarjetas.stream()
				.sorted(Comparator.comparing(TarjetaColocacionDTO::getPrn, Comparator.nullsFirst(String::compareTo))
						.thenComparing(t -> ReporteUtil.safeParseInt(t.getTransaccion())))
				.collect(Collectors.groupingBy(t -> t.getPrn() + "|" + ReporteUtil.nullToEmpty(t.getTransaccion()),
						LinkedHashMap::new, Collectors.toList()));

		List<TarjetaColocacionDTO> out = new ArrayList<>();
		List<Map<String, Object>> wh = null;

		for (var entry : porPrnYTxn.entrySet()) {
			List<TarjetaColocacionDTO> baseGroup = entry.getValue();
			TarjetaColocacionDTO plantilla = baseGroup.get(0);
			String prn = plantilla.getPreloanSuffix();
			Integer prnNew = Integer.parseInt(prn);

			if (prnNew < 0) {
				log.warn("preloanSuffix no numérico o vacío para clave {}. Se deja baseGroup sin complementar.",
						entry.getKey());
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

			for (Map<String, Object> fila : wh) {
				TarjetaColocacionDTO t = ReporteUtil.copyOfV2(plantilla);
				BigDecimal monto = ReporteUtil.asBigDecimal(fila.get("amount"));
				if (monto != null)
					t.setMontoDisposicion(monto);

				String otype = ReporteUtil.asString(fila.get("otype"));
				if (otype != null && !otype.isBlank()) {
					// Obtener descripción del tipo
					OtypeTipoCatalog.fromCode(otype).ifPresent(tipo -> t.setTipoDisposicion(tipo.getDescription()));

					// Obtener canal correspondiente
					OtypeCanalCatalog.fromCode(otype).ifPresent(canal -> t.setCanalDisposicion(canal.getCanal()));
				}

				out.add(t);
			}
		}

		return out;
	}

	/*
	 * REPORTE #3
	 * */

	@Override
	public void obtainReporteCarteraRepository() {
		
	
		List<AccountReportWalletDTO> resultadoQueryInicial = null;
		
		List<AccountReportWalletDTO> resultadoV2 = new ArrayList<AccountReportWalletDTO>();
		
		MapSqlParameterSource params = ReporteUtil.getYesterdayRangeParams();
		
		try {
			resultadoQueryInicial = datamartJdbcTemplate.query(ReporteUtil.QUERY_INICIAL_V3,new WalletReportRowMapper());
		} catch (Exception e) {
			log.error("Error consultando QUERY_INICIAL_Wallet con parámetros: startDate={}, endDate={}. Causa: {}",
					params.getValue("startDate"), params.getValue("endDate"), e.getMessage(), e);

		}
		
		if (resultadoQueryInicial == null || resultadoQueryInicial.isEmpty()) {
			log.error("Sin datos para ejecutar el primer query Wallet. {}");
			return;
		}
		
		log.info("Filas obtenidas: {}", resultadoQueryInicial.size());
		
		
		try {
			
			
		    for (AccountReportWalletDTO item : resultadoQueryInicial) {
		    	
		    	/*
		    	 * Obtiene la tarjeta
		    	 * */
		    	String cardNumber = getCard(item.getPrn());
		    	 item.setCard(cardNumber);
		    	 
		    	 /*
		    	  *Se complementa el campo: intereses moratorios 
		    	  * */
		    	 
		    	 BigDecimal totalDefaultInterest = obtainInteresMoratorio(item.getAccountId());
		    	 item.setInteresesMoratorios(totalDefaultInterest);
		    	 
		    	
		    	 /*
		    	  * Se obtine el interes exigible
		    	  * */
		    	 
		    	SumOfInterestDto response = obtainInterest(item.getAccountId());
		    	item.setInteresExigible(response.getInterest_total());
		    	
		    	
		    	if(item.getDiasVencido()>90 && item.getDiasVencido()<92) {
		    		item.setInteresesCuentasBalances(item.getInteresExigible());
		    		
		    		MapSqlParameterSource paramsInsert = new MapSqlParameterSource()
		    			    .addValue("prn", item.getPrn())
		    			    .addValue("factAccountId", item.getAccountId())
		    			    .addValue("interesCuentaBalance", item.getInteresExigible())
		    			    .addValue("interesCuentaOrden", 0);

		    		datamartJdbcTemplate.update(ReporteUtil.QUERY_INSERT_INTERES_CUENTA, paramsInsert);
		    		
		    	}
		    	
		    	if (item.getDiasVencido() > 92) {
		    	    try {
		    	    	SumOfInterestDto response2 = obtainInterest(item.getAccountId());
		    	    	
		    	        MapSqlParameterSource paramsInterest = new MapSqlParameterSource("factAccountId", item.getAccountId());

		    	        List<Map<String, Object>> rows = datamartJdbcTemplate.queryForList(ReporteUtil.QUERY_INTERES_CUENTA,paramsInterest);

		    	        for (Map<String, Object> row : rows) {
		    	        	
		    	            BigDecimal interesBalance = new BigDecimal(row.get("interes_cuenta_balance").toString());

		    	            BigDecimal interesOrden = response2.getInterest_total().subtract(interesBalance);
		    	            
		    	            MapSqlParameterSource paramsUpdate = new MapSqlParameterSource()
			    	        	    .addValue("interesOrden", interesOrden)
			    	        	    .addValue("factAccountId", item.getAccountId());
		    	            
		    	            int rowsAffected = datamartJdbcTemplate.update(ReporteUtil.UPDATE_INTERES_CUENTA, paramsUpdate);
		    	            
		    	            if(rowsAffected>0) {
		    	            	System.out.println("Cambios aplicados.");
		    	            }
		    	            
		    	            item.setInteresesCuentasOrden(interesOrden);
		    	        }
		    	        
		    	       
		    	        
		    	    } catch (Exception e) {
		    	        e.printStackTrace();
		    	    }
		    	}
		    	
		    	/*
		    	 * 
		    	 * CASO #1 Cliente recibe el pag0
		    	 * */
		    	BigDecimal interesExigibleTemp =  item.getInteresExigible();
		    	
		    	InteresCuentaDTO  respInteresCuenta = obtainNewTableInteresCuenta(item.getAccountId());
		    	
		    	BigDecimal balanceMasOrden = respInteresCuenta.getInteresCuentaBalance().add(respInteresCuenta.getInteresCuentaOrden());
		    	
		    	//interesExigible < balanceMasOrden
		    	if (interesExigibleTemp.compareTo(balanceMasOrden) < 0) {
		    		
		    		// balanceMasOrden - interesExigibleTemp
		    		BigDecimal interesExigibleFinal = safe(interesExigibleTemp).subtract(safe(balanceMasOrden));
		    		
		    		//interes_cuenta_balance - interesExigibleFinal
		    		BigDecimal interesRemanente = safe(item.getInteresesCuentasBalances()).subtract(safe(interesExigibleFinal));
		    		
		    		 MapSqlParameterSource paramsUpdate = new MapSqlParameterSource()
		    	        	    .addValue("interesBalance", interesRemanente)
		    	        	    .addValue("factAccountId", item.getAccountId());
	    	            
	    	             datamartJdbcTemplate.update(ReporteUtil.UPDATE_INTERES_CUENTA_BALANCE, paramsUpdate);
	    	             
	    	             item.setInteresesCuentasBalances(interesRemanente);
		    	}
		    	
		    	
		    	/*
		    	 * Caso #2 Liquidado
		    	 * */
		    	if (interesExigibleTemp.compareTo(BigDecimal.ZERO) == 0 && item.getDiasVencido() == 0) {
		    		
		    		MapSqlParameterSource paramsDelete = new MapSqlParameterSource("id", respInteresCuenta.getId());
		    		datamartJdbcTemplate.update(ReporteUtil.DELETE_INTERES_CUENTA_BY_ID, paramsDelete);

		    	}
		    		
		    	resultadoV2.add(item);
		    }
		    
		   

		    
		} catch (Exception e) {
		    e.printStackTrace();
		}
		
		ReporteUtil.generarExcelWallet(resultadoV2, baseDir, rutaPlantilla3, prefix3);
	}
	
	
	public BigDecimal obtainInteresMoratorio(Integer accountId) {
		
		MapSqlParameterSource params = new MapSqlParameterSource("accountId", accountId);

	    return datamartJdbcTemplate.queryForObject(
	            ReporteUtil.INTERES_MORATORIO_SUM,
	            params,
	            BigDecimal.class
	    );
		
	}

	public InteresCuentaDTO obtainNewTableInteresCuenta(Integer accountId) {

		MapSqlParameterSource paramsInterest = new MapSqlParameterSource("factAccountId", accountId);

		List<Map<String, Object>> rows = datamartJdbcTemplate.queryForList(ReporteUtil.QUERY_INTERES_CUENTA,paramsInterest);

		if (rows.isEmpty()) {
	        InteresCuentaDTO dto = new InteresCuentaDTO();
	        dto.setId(null); 
	        dto.setPrn(null);
	        dto.setFactAccountId(accountId); 
	        dto.setInteresCuentaBalance(BigDecimal.ZERO);
	        dto.setInteresCuentaOrden(BigDecimal.ZERO);
	        return dto;
	    }

		Map<String, Object> row = rows.get(0);
		
		Integer id = ((Number) row.get("id")).intValue();
		
		String card = ((String) row.get("prn"));
		
		Integer factAccountId = ((Number) row.get("fact_account_id")).intValue();

		BigDecimal interesBalance = row.get("interes_cuenta_balance") == null ? BigDecimal.ZERO
				: (BigDecimal) row.get("interes_cuenta_balance");

		BigDecimal interesOrden = row.get("interes_cuenta_orden") == null ? BigDecimal.ZERO
				: (BigDecimal) row.get("interes_cuenta_orden");

		InteresCuentaDTO dto = new InteresCuentaDTO();

		dto.setId(id);
		dto.setPrn(card);
		dto.setFactAccountId(factAccountId);
		dto.setInteresCuentaBalance(interesBalance);
		dto.setInteresCuentaOrden(interesOrden);

		return dto;
	}
	
	
	private BigDecimal safe(BigDecimal v) {
	    return v == null ? BigDecimal.ZERO : v;
	}
	
	/*
	 * Consultamos MO para complementar con la tarjeta
	 * Obtain card
	 * 
	 * */
	
	
	public String getCard(String prn){
		
		
		String sql = """
			    SELECT card_number
			    FROM cliente_tarjetas
			    WHERE card_external_id = :prn
			    LIMIT 1
			""";
    	
    	
   	 List<String> cards = moJdbcTemplate.query(
   		        sql,
   		        Map.of("prn", prn),
   		        (rs, rowNum) -> rs.getString("card_number")
   		    );
   	 
   	    String cardNumber = cards.isEmpty() ? null : cards.get(0);
   	    
		return cardNumber;
	}
	
	/*
	 * Se obtine el interes exigible
	 * interest_total = interest_revolving + interest_fixed
	 * */

	public  SumOfInterestDto obtainInterest(Integer inFactAccountId) {
		
		      SumOfInterestDto sumOfInterestDto =  new SumOfInterestDto();
		try {
			MapSqlParameterSource paramsInterest = new MapSqlParameterSource("factAccountId", inFactAccountId);

			Map<String, Object> row = datamartJdbcTemplate.queryForMap(ReporteUtil.QUERY_CONSULTA_INTEREST, paramsInterest);
			Integer factAccountId = ((Number) row.get("fact_account_id")).intValue();
			BigDecimal interestRevolving = (BigDecimal) row.get("interest_revolving");
			BigDecimal interestFixed     =  (BigDecimal) row.get("interest_fixed");
			BigDecimal interestTotal     = (BigDecimal) row.get("interest_total");
			
			sumOfInterestDto.setFact_account_id(factAccountId);
			sumOfInterestDto.setInterest_revolving(interestRevolving);
			sumOfInterestDto.setInterest_fixed(interestFixed);
			sumOfInterestDto.setInterest_total(interestTotal);
			
			return sumOfInterestDto;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}

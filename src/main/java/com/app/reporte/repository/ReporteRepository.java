package com.app.reporte.repository;

import java.math.BigDecimal;
import java.nio.file.Path;
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

import com.app.reporte.dto.TarjetaColocacionDTO;
import com.app.reporte.dto.TarjetaDTO;
import com.app.reporte.mapper.TarjetaRowMapper;
import com.app.reporte.mapper.TarjetaColocacionRowMapper;
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

	@Value("${excel.output.filename-prefix:reporte_}")
	private String prefix;

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
				BigDecimal monto = ReporteUtil.asBigDecimal(fila.get("billing_amt"));
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
		return ReporteUtil.generarExcelV2(complemento, baseDir, rutaPlantilla2, prefix);
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
				BigDecimal monto = ReporteUtil.asBigDecimal(fila.get("billing_amt"));
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


}

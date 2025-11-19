package com.app.reporte.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import org.springframework.jdbc.core.RowMapper;
import com.app.reporte.dto.TarjetaColocacionDTO;

public class TarjetaColocacionRowMapper implements RowMapper<TarjetaColocacionDTO>{

	@Override
	public TarjetaColocacionDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		TarjetaColocacionDTO dto = new TarjetaColocacionDTO();
		 dto.setProducto(rs.getString("producto"));
		 dto.setPrn(rs.getString("prn"));
		 dto.setIdCliente(rs.getString("id_cliente"));
		 dto.setNombre(rs.getString("nombre"));
		 dto.setNumeroCliente(rs.getString("numero_cliente"));
		 dto.setCuentaAsociada(rs.getString("cuenta_asociada"));
		 dto.setTransaccion(rs.getString("transaccion"));
		 dto.setLineaCredito(rs.getBigDecimal("linea_credito"));
		 dto.setPreloanExternaId(rs.getString("preloan_externa_id"));
	     dto.setFecha(rs.getObject("fact_loans_created_dt", LocalDateTime.class));
		 
		return dto;
	}

}

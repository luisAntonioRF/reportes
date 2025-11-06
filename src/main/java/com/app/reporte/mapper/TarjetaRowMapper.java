package com.app.reporte.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import org.springframework.jdbc.core.RowMapper;
import com.app.reporte.dto.TarjetaDTO;

public class TarjetaRowMapper implements RowMapper<TarjetaDTO>{

	@Override
	public TarjetaDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		TarjetaDTO dto = new TarjetaDTO();
        dto.setPrn(rs.getString("prn"));
        //dto.setMarca(rs.getString("marca"));
        //dto.setTipo(rs.getString("tipo"));
        dto.setProducto(rs.getString("producto"));
        dto.setLineaCredito(rs.getBigDecimal("linea_credito"));
        dto.setTransaccion(rs.getString("transaccion"));
        dto.setPreloanExternaId(rs.getString("preloan_externa_id"));
        dto.setFecha(rs.getObject("fact_loans_created_dt", LocalDateTime.class));
        return dto;
	}

}

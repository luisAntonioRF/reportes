package com.app.reporte.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import org.springframework.jdbc.core.RowMapper;
import com.app.reporte.dto.AccountReportWalletDTO;

public class WalletReportRowMapper implements RowMapper<AccountReportWalletDTO>{

	@Override
	public AccountReportWalletDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		AccountReportWalletDTO dto= new AccountReportWalletDTO();
		dto.setPrn(rs.getString("prn"));
		dto.setNumCliente(rs.getString("num_cliente"));
		dto.setCuentaAsociada(rs.getString("cuenta_asociada"));
		dto.setProducto(rs.getString("producto"));
		dto.setLineaCredito(rs.getBigDecimal("linea_credito"));
		dto.setStatus(rs.getString("status"));
		dto.setDiasVencido(rs.getInt("dias_vencido"));
		dto.setEtapa(rs.getInt("etapa"));
		dto.setFechaPago(rs.getObject("fecha_pago",LocalDateTime.class));
		dto.setPagoExigible(rs.getBigDecimal("pago_exigible"));
		dto.setCapitalNoExigible(rs.getBigDecimal("capital_no_exigible"));
		dto.setCapitalExigible(rs.getBigDecimal("capital_exigible"));
		dto.setInteresExigible(rs.getBigDecimal("interes_exigible"));
		dto.setInteresesMoratorios(rs.getBigDecimal("intereses_moratorios"));
		return dto;
	}

}

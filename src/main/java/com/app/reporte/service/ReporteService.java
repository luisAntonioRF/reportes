package com.app.reporte.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.reporte.repository.IReporteRepository;

@Service
public class ReporteService implements IReporteService{
	
	@Autowired
	IReporteRepository reporteRepository;

	@Override
	public void obtainReport() {
		
		reporteRepository.obtainReporte();
	}

	@Override
	public void obtainReporteColocacion() {
		reporteRepository.obtainReporteColocacionRepository();
		
	}

}

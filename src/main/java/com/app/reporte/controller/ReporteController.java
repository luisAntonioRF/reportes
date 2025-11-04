package com.app.reporte.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.reporte.service.IReporteService;

@RestController
public class ReporteController {

	@Autowired
	IReporteService reporteService;
	
	/*@GetMapping("/get-reporte")
	public ResponseEntity<?>obtainReporte(){
		reporteService.obtainReport();
		return null;
	}*/
	
	@Scheduled(cron = "${job.cron.expression}")
	public void obtainReporte(){
		reporteService.obtainReport();
		
	}
}

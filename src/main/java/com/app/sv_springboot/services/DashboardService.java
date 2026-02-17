package com.app.sv_springboot.services;

import java.util.List;

import com.app.sv_springboot.dtos.ATSDataListDto;
import com.app.sv_springboot.entities.User_Data_Entity;

public interface DashboardService {

	public ATSDataListDto fetchATSData();

}

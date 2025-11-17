package com.stroll.www.aop;

import org.aspectj.lang.annotation.Pointcut;

public class Pointcuts {
	@Pointcut("execution(* com.stroll.www.controller.PlaceService.get*(..))")
	public void getPlace() {}
}

package com.stroll.www.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Service;

@Service //advice bean 객체 생성
@Aspect //advice bean의 id는 클래스 이름을 따라감.
public class Advice {
	@AfterReturning("Pointcuts.getPlace()")
	public void roundStar(JoinPoint jp) {}
}

package com.example.demo;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class PrintExecutionTimeAspect {
	@Around("@annotation(PrintExceptionTime)")
	public Object printExceptionTime(ProceedingJoinPoint joinPoint) throws Throwable {
		long start = System.currentTimeMillis();
		var object = joinPoint.proceed();
		long excutionTime = System.currentTimeMillis() - start;
		System.out.println("executed " + joinPoint.toShortString() + " with " + joinPoint.getArgs().length + " ars in " + excutionTime + "ms.");
		return object;
	}

	@Before("@annotation(PrintExceptionTime)")
	public void beforePrintExceptionTime(JoinPoint joinPoint) {
		System.out.println("before " + joinPoint.toShortString() + " with " + joinPoint.getArgs().length + " args.");
	}
	
	@After("@annotation(PrintExecutionTime)")
	public void afterPrintExceptionTime(JoinPoint joinPoint) {
		System.out.println("after " + joinPoint.toShortString() + " with " + joinPoint.getArgs().length + "args.");
		
		
	}

}

package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.pi.enabled", havingValue = "true")
public class PiAsyncApplication implements ApplicationRunner {

	@Autowired
	private Pi pi;
	
	@Override
	public void run(ApplicationArguments args) throws Exception {
		var future = pi.calculateAsync(100000000);
		
		future.thenAccept(result -> System.out.println("Async PI = " + result));
		
		System.out.println("Continue my jobs after kicking off pi calculation");
		
		for (int i = 0; i < 5; i++) {
			System.out.println("working..." + i);
			Thread.sleep(500);
		}
		
		System.out.println("Application Runner has finished");
	}
}
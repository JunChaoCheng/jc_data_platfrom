package com.jc.gateway;

import com.jc.gateway.util.SpringContextUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * springboot启动类
 * @author Jc
 *
 */
@SpringBootApplication
@Import({SpringContextUtil.class})
public class GateWayApplication {
 
	public static void main(String[] args) {
		SpringApplication.run(GateWayApplication.class, args);
	}
}
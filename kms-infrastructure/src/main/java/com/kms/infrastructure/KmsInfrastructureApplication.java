package com.kms.infrastructure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * KMS基础设施层应用启动类
 * 
 * @author kms
 * @version 1.0
 * @since Java 21
 */
@SpringBootApplication(scanBasePackages = "com.kms")
@EnableFeignClients(basePackages = "com.kms.infrastructure.adapter.external")
public class KmsInfrastructureApplication {

    /**
     * 应用启动入口
     *
     * @param args 命令行参数
     */
    public static void main(final String[] args) {
        SpringApplication.run(KmsInfrastructureApplication.class, args);
    }
}
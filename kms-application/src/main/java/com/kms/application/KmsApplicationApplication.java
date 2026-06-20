package com.kms.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * KMS应用程序主入口类
 * 负责启动整个应用程序
 */
@SpringBootApplication
public class KmsApplicationApplication {

    public static void main(String[] args) {
        SpringApplication.run(KmsApplicationApplication.class, args);
    }
}

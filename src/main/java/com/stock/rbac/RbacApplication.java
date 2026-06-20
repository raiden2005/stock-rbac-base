package com.stock.rbac;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@EnableConfigurationProperties(com.stock.rbac.util.RbacConfigUtil.class)
@MapperScan(basePackages = "com.stock.rbac.mapper")
@ComponentScan(
        basePackages = {
                "com.stock.rbac",
                "com.kms.domain",
                "com.kms.infrastructure",
                "com.kms.application"
        },
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.REGEX,
                        pattern = "com.kms.*Application"
                )
        }
)
public class RbacApplication {

    public static void main(String[] args) {
        SpringApplication.run(RbacApplication.class, args);
    }
}

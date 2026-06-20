package com.stock.rbac.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * KMS模块集成配置类
 * <p>
 * 用于在RBAC系统中集成DDD KMS模块，统一管理KMS相关Bean的创建与注入
 * 确保KMS服务与现有RBAC系统的认证、权限机制无缝对接
 *
 * @author stock-rbac-integration
 * @version 1.0
 * @since Java 17
 */
@Configuration
@ComponentScan(basePackages = {"com.stock.rbac.bridge"})
public class KmsIntegrationConfig {

    /**
     * 该配置类通过 @ComponentScan 自动扫描 bridge 包下的 Bean
     * Bridge 类通过 @Service 注解自动注册：
     * - DirectoryRightDomainServiceBridge
     * - DirectoryDomainServiceBridge
     * - DirectoryTagDomainServiceBridge
     * 
     * 这些桥接类实现了 com.kms.domain 模块定义的领域服务接口，
     * 同时复用现有 RBAC 系统的 Mapper、Service 和认证机制
     */
}

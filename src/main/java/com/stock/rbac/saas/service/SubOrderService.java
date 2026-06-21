package com.stock.rbac.saas.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stock.rbac.entity.SysSubBill;
import com.stock.rbac.entity.SysSubOrder;
import com.stock.rbac.entity.SysTenant;
import com.stock.rbac.entity.SysPlanConfig;
import com.stock.rbac.mapper.SysSubBillMapper;
import com.stock.rbac.mapper.SysSubOrderMapper;
import com.stock.rbac.mapper.SysTenantMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 订阅订单服务（年费订单）
 * 支付成功后自动顺延租户有效期
 */
@Service
public class SubOrderService {

    private final SysSubOrderMapper subOrderMapper;
    private final SysSubBillMapper subBillMapper;
    private final SysTenantMapper tenantMapper;
    private final PlanConfigService planConfigService;

    public SubOrderService(SysSubOrderMapper subOrderMapper,
                           SysSubBillMapper subBillMapper,
                           SysTenantMapper tenantMapper,
                           PlanConfigService planConfigService) {
        this.subOrderMapper = subOrderMapper;
        this.subBillMapper = subBillMapper;
        this.tenantMapper = tenantMapper;
        this.planConfigService = planConfigService;
    }

    /**
     * 创建订阅订单
     * @param tenantId 租户ID
     * @param subscribeYear 订阅年数（默认1）
     */
    @Transactional(rollbackFor = Exception.class)
    public SysSubOrder createOrder(String tenantId, Integer subscribeYear) {
        if (tenantId == null || tenantId.isEmpty()) {
            throw new IllegalArgumentException("租户ID不能为空");
        }
        int years = (subscribeYear == null || subscribeYear <= 0) ? 1 : subscribeYear;

        SysPlanConfig config = planConfigService.getDefaultConfig();
        BigDecimal unitPrice = config.getYearSubPrice() != null
                ? config.getYearSubPrice() : BigDecimal.valueOf(500);

        SysTenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) {
            throw new IllegalArgumentException("租户不存在");
        }

        SysSubOrder order = new SysSubOrder();
        String orderNo = "SO" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4);
        order.setOrderNo(orderNo);
        order.setTenantId(tenantId);
        order.setPlanType(config.getPlanType());
        order.setSubscribeYear(years);
        order.setUnitPrice(unitPrice);
        order.setTotalAmount(unitPrice.multiply(BigDecimal.valueOf(years)));
        order.setOrderStatus(0);
        order.setOrderCategory(1); // 1=订阅
        subOrderMapper.insert(order);
        return order;
    }

    /**
     * 支付订阅订单（幂等）：支付成功自动顺延租户有效期、自动激活
     */
    @Transactional(rollbackFor = Exception.class)
    public SysSubOrder payOrder(String orderId, String tenantIdCheck) {
        if (orderId == null || orderId.isEmpty()) {
            throw new IllegalArgumentException("订单ID不能为空");
        }
        SysSubOrder order = subOrderMapper.selectById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (tenantIdCheck != null && !tenantIdCheck.isEmpty()
                && !tenantIdCheck.equals(order.getTenantId())) {
            throw new SecurityException("越权操作：不能支付其他租户订单");
        }
        if (order.getOrderStatus() != null && order.getOrderStatus() == 1) {
            return order;
        }
        if (order.getOrderStatus() != null && order.getOrderStatus() != 0) {
            throw new IllegalStateException("订单状态不允许支付");
        }

        order.setOrderStatus(1);
        order.setPayTime(LocalDateTime.now());
        order.setPayType("MANUAL");
        subOrderMapper.updateById(order);

        // 自动顺延租户有效期、同时激活
        SysTenant tenant = tenantMapper.selectById(order.getTenantId());
        if (tenant != null) {
            LocalDateTime base = (tenant.getValidEnd() != null && tenant.getValidEnd().isAfter(LocalDateTime.now()))
                    ? tenant.getValidEnd() : LocalDateTime.now();
            int years = order.getSubscribeYear() == null ? 1 : order.getSubscribeYear();
            tenant.setValidEnd(base.plusYears(years));
            tenant.setTenantStatus(1); // 自动激活
            tenantMapper.updateById(tenant);
        }

        // 生成账单
        String statMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        SysSubBill bill = new SysSubBill();
        bill.setBillNo("BL" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4));
        bill.setTenantId(order.getTenantId());
        bill.setOrderId(order.getId());
        bill.setBillCategory(1); // 1=订阅
        bill.setBillAmount(order.getTotalAmount());
        bill.setBillStatus(1);
        bill.setBillPeriod(statMonth);
        subBillMapper.insert(bill);
        return order;
    }

    /** 取消订单 */
    @Transactional(rollbackFor = Exception.class)
    public SysSubOrder cancelOrder(String orderId, String tenantIdCheck) {
        SysSubOrder order = subOrderMapper.selectById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (tenantIdCheck != null && !tenantIdCheck.isEmpty()
                && !tenantIdCheck.equals(order.getTenantId())) {
            throw new SecurityException("越权操作：不能取消其他租户订单");
        }
        if (order.getOrderStatus() != null && order.getOrderStatus() != 0) {
            throw new IllegalStateException("仅待支付订单可取消");
        }
        order.setOrderStatus(2);
        subOrderMapper.updateById(order);
        return order;
    }
}

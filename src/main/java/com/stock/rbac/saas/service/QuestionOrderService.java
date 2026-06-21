package com.stock.rbac.saas.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stock.rbac.entity.SysPlanConfig;
import com.stock.rbac.entity.SysQuestionOrder;
import com.stock.rbac.entity.SysSubBill;
import com.stock.rbac.entity.SysTenantQuestionStat;
import com.stock.rbac.mapper.SysQuestionOrderMapper;
import com.stock.rbac.mapper.SysSubBillMapper;
import com.stock.rbac.mapper.SysTenantQuestionStatMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 增值提问订单服务
 * 状态：0 待支付 / 1 已支付 / 2 取消 / 3 过期
 */
@Service
public class QuestionOrderService {

    private final SysQuestionOrderMapper questionOrderMapper;
    private final SysTenantQuestionStatMapper tenantQuestionStatMapper;
    private final SysSubBillMapper subBillMapper;

    public QuestionOrderService(SysQuestionOrderMapper questionOrderMapper,
                                SysTenantQuestionStatMapper tenantQuestionStatMapper,
                                SysSubBillMapper subBillMapper) {
        this.questionOrderMapper = questionOrderMapper;
        this.tenantQuestionStatMapper = tenantQuestionStatMapper;
        this.subBillMapper = subBillMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public SysQuestionOrder createOrder(String tenantId, int buyNum, SysPlanConfig config) {
        if (tenantId == null || tenantId.isEmpty()) {
            throw new IllegalArgumentException("租户ID不能为空");
        }
        if (buyNum <= 0) {
            throw new IllegalArgumentException("购买数量必须大于0");
        }

        BigDecimal unitPrice = config != null && config.getOverQuestionUnitPrice() != null
                ? config.getOverQuestionUnitPrice() : BigDecimal.valueOf(20);

        SysQuestionOrder order = new SysQuestionOrder();
        String orderNo = "QO" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4);
        order.setOrderNo(orderNo);
        order.setTenantId(tenantId);
        order.setBuyQuestionNum(buyNum);
        order.setUnitPrice(unitPrice);
        order.setTotalAmount(unitPrice.multiply(BigDecimal.valueOf(buyNum)));
        order.setOrderStatus(0); // 待支付
        questionOrderMapper.insert(order);
        return order;
    }

    /**
     * 支付订单 - 幂等处理：重复支付直接返回原订单，避免重复累加存量
     */
    @Transactional(rollbackFor = Exception.class)
    public SysQuestionOrder payOrder(String orderId) {
        if (orderId == null || orderId.isEmpty()) {
            throw new IllegalArgumentException("订单ID不能为空");
        }
        SysQuestionOrder order = questionOrderMapper.selectOne(
                new LambdaQueryWrapper<SysQuestionOrder>().eq(SysQuestionOrder::getId, orderId));
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        // 幂等：已支付不重复处理
        if (order.getOrderStatus() != null && order.getOrderStatus() == 1) {
            return order;
        }
        if (order.getOrderStatus() != null && order.getOrderStatus() != 0) {
            throw new IllegalStateException("订单状态不允许支付");
        }

        order.setOrderStatus(1);
        order.setPayTime(LocalDateTime.now());
        order.setPayType("MANUAL");
        questionOrderMapper.updateById(order);

        // 累加付费存量
        String statMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        SysTenantQuestionStat stat = tenantQuestionStatMapper.selectOne(
                new LambdaQueryWrapper<SysTenantQuestionStat>()
                        .eq(SysTenantQuestionStat::getTenantId, order.getTenantId())
                        .eq(SysTenantQuestionStat::getStatMonth, statMonth)
                        .last("LIMIT 1"));
        if (stat == null) {
            stat = new SysTenantQuestionStat();
            stat.setTenantId(order.getTenantId());
            stat.setStatMonth(statMonth);
            stat.setFreeUseNum(0);
            stat.setPayUseNum(0);
            stat.setSurplusPayQuestion(0);
            tenantQuestionStatMapper.insert(stat);
        }
        int currentSurplus = stat.getSurplusPayQuestion() == null ? 0 : stat.getSurplusPayQuestion();
        int buyNum = order.getBuyQuestionNum() == null ? 0 : order.getBuyQuestionNum();
        stat.setSurplusPayQuestion(currentSurplus + buyNum);
        tenantQuestionStatMapper.updateById(stat);

        // 生成账单
        SysSubBill bill = new SysSubBill();
        bill.setBillNo("BL" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4));
        bill.setTenantId(order.getTenantId());
        bill.setOrderId(order.getId());
        bill.setBillCategory(2); // 2=增值提问
        bill.setBillAmount(order.getTotalAmount());
        bill.setBillStatus(1);
        bill.setBillPeriod(statMonth);
        subBillMapper.insert(bill);
        return order;
    }

    /**
     * 取消订单（租户自助或定时任务关闭）
     */
    @Transactional(rollbackFor = Exception.class)
    public SysQuestionOrder cancelOrder(String orderId, String tenantIdCheck) {
        SysQuestionOrder order = questionOrderMapper.selectById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        // 跨租户隔离：必须是本租户的订单才可取消
        if (tenantIdCheck != null && !tenantIdCheck.isEmpty()
                && !tenantIdCheck.equals(order.getTenantId())) {
            throw new SecurityException("越权操作：不能取消其他租户订单");
        }
        // 仅待支付可取消
        if (order.getOrderStatus() != null && order.getOrderStatus() != 0) {
            throw new IllegalStateException("仅待支付订单可取消");
        }
        order.setOrderStatus(2);
        questionOrderMapper.updateById(order);
        return order;
    }

    public Page<SysQuestionOrder> listByTenant(String tenantId, int pageNum, int pageSize) {
        LambdaQueryWrapper<SysQuestionOrder> query = new LambdaQueryWrapper<>();
        query.eq(SysQuestionOrder::getTenantId, tenantId);
        query.orderByDesc(SysQuestionOrder::getCreateTime);
        return questionOrderMapper.selectPage(new Page<>(pageNum, pageSize), query);
    }

    public SysQuestionOrder getOrderById(String id) {
        return questionOrderMapper.selectById(id);
    }
}

package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.example.config.RabbitMQConfig;
import org.example.enums.BizCodeEnum;
import org.example.enums.CouponStateEnum;
import org.example.enums.ProductOrderStateEnum;
import org.example.enums.StockTaskStateEnum;
import org.example.exception.BizException;
import org.example.feign.ProductOrderFeignService;
import org.example.mapper.ProductTaskMapper;
import org.example.model.ProductDO;
import org.example.mapper.ProductMapper;
import org.example.model.ProductRecordMessage;
import org.example.model.ProductTaskDO;
import org.example.request.LockProductRequest;
import org.example.request.OrderItemRequest;
import org.example.service.ProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.utils.JsonData;
import org.example.vo.ProductVO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author gaokd
 * @since 2023-03-30
 */
@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductTaskMapper productTaskMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    @Autowired
    private ProductOrderFeignService orderFeignService;

    @Override
    public Map<String, Object> pageProductActivity(int page, int size) {
        Page<ProductDO> pageInfo = new Page<>(page, size);
        IPage<ProductDO> productDOIPage = productMapper.selectPage(pageInfo, null);
        Map<String, Object> pageMap = new HashMap<>(3);
        pageMap.put("total_record", productDOIPage.getTotal());
        pageMap.put("total_page", productDOIPage.getPages());
        pageMap.put("current_data", productDOIPage.getRecords().stream().map(
                obj -> beanProcess(obj)).collect(Collectors.toList()));

        return pageMap;
    }

    /**
     * 根据商品id查找
     *
     * @param productId
     * @return
     */
    @Override
    public ProductVO findDetailById(long productId) {
        ProductDO productDO = productMapper.selectById(productId);
        return beanProcess(productDO);
    }

    /**
     * 批量查询商品
     *
     * @param productIdList
     * @return
     */
    @Override
    public List<ProductVO> findProductByIdBatch(List<Long> productIdList) {
        //根据购物车里的所有id进行查询，查找所有的商品的价格
        List<ProductDO> productDOList = productMapper.selectList(new QueryWrapper<ProductDO>().in("id", productIdList));
        List<ProductVO> voList = productDOList.stream().map(
                obj -> beanProcess(obj)
        ).collect(Collectors.toList());
        return voList;
    }

    /**
     * 锁定商品库存
     * 1)遍历商品，锁定每个商品数量
     * 2)每次锁定，都要发送延迟消息
     *
     * @param lockProductRequest
     * @return
     */
    @Override
    public JsonData lockProductStock(LockProductRequest lockProductRequest) {
        String orderOutTradeNo = lockProductRequest.getOrderOutTradeNo();
        List<OrderItemRequest> orderItemList = lockProductRequest.getOrderItemList();
        //遍历商品,一次遍历，一次查询
        //拿到所有商品ID并放到集合里
        List<Long> collectIds = orderItemList.stream().map(OrderItemRequest::getProductId).collect(Collectors.toList());
        //批量查询
        List<ProductVO> productVOList = this.findProductByIdBatch(collectIds);
        //分组
        Map<Long, ProductVO> productMap = productVOList.stream().collect(Collectors.toMap(ProductVO::getId, Function.identity()));

        for (OrderItemRequest item : orderItemList) {
            int rows = productMapper.lockProductStock(item.getProductId(), item.getBuyNum());
            if (rows != 1) {
                throw new BizException(BizCodeEnum.ORDER_CONFIRM_LOCK_PRODUCT_FAIL);
            } else {
                ProductVO productVO = productMap.get(item.getProductId());

                ProductTaskDO productTaskDO = new ProductTaskDO();
                productTaskDO.setProductId(item.getProductId());
                productTaskDO.setBuyNum(item.getBuyNum());
                productTaskDO.setLockState(StockTaskStateEnum.LOCK.name());
                productTaskDO.setProductName(productVO.getTitle());
                productTaskDO.setCreateTime(new Date());
                productTaskDO.setOutTradeNo(orderOutTradeNo);
                productTaskMapper.insert(productTaskDO);

                //todo 发送延迟队列
                ProductRecordMessage productRecordMessage = new ProductRecordMessage();
                productRecordMessage.setOutTradeNo(orderOutTradeNo);
                productRecordMessage.setTaskId(productTaskDO.getId());

                rabbitTemplate.convertAndSend(rabbitMQConfig.getEventExchange(), rabbitMQConfig.getStockReleaseDelayRoutingKey(), productRecordMessage);
                log.info("商品库存锁定延迟消息发送成功:{}", productRecordMessage);

            }

        }

        return JsonData.buildSuccess();
    }

    /**
     * 释放商品库存
     *
     * @param recordMessage
     * @return
     */
    @Override
    public boolean releaseProductStock(ProductRecordMessage recordMessage) {
        ProductTaskDO productTaskDO = productTaskMapper.selectOne(new QueryWrapper<ProductTaskDO>().eq("id", recordMessage.getTaskId()));
        if (productTaskDO == null) {
            log.warn("工作单不存在");
            return true;
        }
        //状态为lock才会处理
        if (productTaskDO.getLockState().equalsIgnoreCase(StockTaskStateEnum.LOCK.name())) {
            //根据订单号查询状态
            JsonData jsonData = orderFeignService.queryProductOrderState(recordMessage.getOutTradeNo());
            String state = jsonData.getData().toString();
            if (ProductOrderStateEnum.UNPAID.name().equalsIgnoreCase(state)) {
                //状态是UNPAID状态，则返回给消息队列，重新投递
                log.warn("订单状态是UNPAID，返回给消息队列，重新投递:{}", recordMessage);
                return false;
            }
            //如果已经支付
            if (ProductOrderStateEnum.PAID.name().equalsIgnoreCase(state)) {
                //修改状态为finish
                productTaskDO.setLockState(StockTaskStateEnum.FINISH.name());
                productTaskMapper.update(productTaskDO, new QueryWrapper<ProductTaskDO>().eq("id", recordMessage.getTaskId()));
                log.info("订单已经支付，修改库存锁定工作单FINISH状态:{}", recordMessage);
                return true;
            }

            //订单不存在，或者订单被取消，确认消息，修改task状态为CANCEL
            log.warn("订单不存在");
            //修改订单工单为CNACEL
            productTaskDO.setLockState(StockTaskStateEnum.CANCEL.name());
            productTaskMapper.update(productTaskDO, new QueryWrapper<ProductTaskDO>().eq("id", recordMessage.getTaskId()));
            //恢复商品库存表，即锁定库存的值减去当前购买的值
            productMapper.unLockProductStock(productTaskDO.getProductId(), productTaskDO.getBuyNum());

            return true;

        } else {
            //非lock
            log.warn("工作单状态不是LOCK，state={},消息题={}", productTaskDO.getLockState(), recordMessage);
            return true;
        }
    }


    private ProductVO beanProcess(ProductDO obj) {
        ProductVO productVO = new ProductVO();
        BeanUtils.copyProperties(obj, productVO);
        //剩余库存 - 锁定库存
        productVO.setStock(productVO.getStock() - obj.getLockStock());
        return productVO;
    }
}

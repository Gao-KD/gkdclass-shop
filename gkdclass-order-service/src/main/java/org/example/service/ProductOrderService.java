package org.example.service;

import org.example.model.ProductOrderDO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.request.ConfirmOrderRequest;
import org.example.utils.JsonData;
import org.example.vo.OrderItemVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author gaokd
 * @since 2023-04-04
 */
public interface ProductOrderService {

    /**
     * 创建订单
     * @param confirmOrderRequest
     * @return
     */
    JsonData confirmOrder(ConfirmOrderRequest confirmOrderRequest);

    String queryProductOrderState(String outTradeNo);

    List<ProductOrderDO> queryOrderByUserId(Long id);
}

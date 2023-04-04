package org.example.service.impl;

import org.example.model.ProductOrderDO;
import org.example.mapper.ProductOrderMapper;
import org.example.request.ConfirmOrderRequest;
import org.example.service.ProductOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.utils.JsonData;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author gaokd
 * @since 2023-04-04
 */
@Service
public class ProductOrderServiceImpl  implements ProductOrderService {

    @Override
    public JsonData confirmOrder(ConfirmOrderRequest confirmOrderRequest) {
        return null;
    }
}

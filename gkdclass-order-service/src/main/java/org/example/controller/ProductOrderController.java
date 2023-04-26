package org.example.controller;


import com.mysql.cj.util.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.example.enums.BizCodeEnum;
import org.example.enums.ClientTypeEnum;
import org.example.enums.ProductOrderPayTypeEnum;
import org.example.request.ConfirmOrderRequest;
import org.example.service.ProductOrderService;
import org.example.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author gaokd
 * @since 2023-04-04
 */
@Api("订单模块")
@RestController
@RequestMapping("/api/order/v1")
@Slf4j
public class ProductOrderController {
    @Autowired
    private ProductOrderService orderService;

    /**
     * 查询呢订单状态
     *
     * 此接口没有登陆拦截，可以增加一个密钥进行rpc通讯
     * @param outTradeNo
     * @return
     */
    @ApiOperation("查询订单状态")
    @GetMapping("query_state")
    public JsonData queryProductOrderState(@RequestParam("out_trade_no")String outTradeNo){
        String state = orderService.queryProductOrderState(outTradeNo);
        return StringUtils.isNullOrEmpty(state)?JsonData.buildResult(BizCodeEnum.ORDER_CONFIRM_NOT_EXIST):JsonData.buildSuccess(state);
    }

    @ApiOperation("提交订单")
    @PostMapping("confim")
    public void confirmOrder(
            @ApiParam("订单对象")
            @RequestBody ConfirmOrderRequest confirmOrderRequest,
            HttpServletResponse response){
        JsonData jsonData = orderService.confirmOrder(confirmOrderRequest);
        if (jsonData.getCode() == 0){

            String client = confirmOrderRequest.getClientType();
            String payType = confirmOrderRequest.getPayType();

            //支付宝网页支付，跳转
            if (payType.equalsIgnoreCase(ProductOrderPayTypeEnum.ALIPAY.name())){
                log.info("创建支付宝订单成功:{}",confirmOrderRequest.toString());
                if (client.equalsIgnoreCase(ClientTypeEnum.H5.name())){
                    //把支付宝回调的流写出
                    writeData(response,jsonData);
                }else if (client.equalsIgnoreCase(ClientTypeEnum.APP.name())){
                    //App sdk支付 TODO

                }

            }else if (payType.equalsIgnoreCase(ProductOrderPayTypeEnum.WECHAT.name())){
                //微信支付 TODO
            }


        }else {
            log.error("创建订单失败{}",jsonData.toString());
        }
    }

    private void writeData(HttpServletResponse response, JsonData jsonData) {
        try {
            //传入的是html流格式
            response.setContentType("text/html;charset=UTF8");
            //把jsondata写出
            response.getWriter().write(jsonData.getData().toString());
            response.getWriter().flush();
            response.getWriter().close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }
}


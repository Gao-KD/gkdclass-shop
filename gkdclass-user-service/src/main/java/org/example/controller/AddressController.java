package org.example.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.example.enums.BizCodeEnum;
import org.example.exception.BizException;
import org.example.model.AddressDO;
import org.example.service.AddressService;
import org.example.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 电商-公司收发货地址表 前端控制器
 * </p>
 *
 * @author Gaokd
 * @since 2023-03-16
 */
@Api(tags = "收货地址")
@RestController
@RequestMapping("/api/address/v1")
public class    AddressController {
    @Autowired
    private AddressService addressService;

    @ApiOperation("根据id查找地址详情")
    @GetMapping("detail")
    public Object test(    @ApiParam(value = "地址id",required = true)
                           @RequestParam(value = "address_id")long address_id){
        AddressDO addressDO = addressService.detail(address_id);
        
        /***
         * 自定义异常测试
         */
//        if (address_id == 1){
//            throw new  BizException(BizCodeEnum.CODE_TO_ERROR.getCode(),BizCodeEnum.CODE_ERROR.getMessage());
//        }

        return JsonData.buildSuccess(addressDO);
    }

}


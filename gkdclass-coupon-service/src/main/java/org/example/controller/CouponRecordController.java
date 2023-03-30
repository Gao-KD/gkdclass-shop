package org.example.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.example.enums.BizCodeEnum;
import org.example.service.CouponRecordService;
import org.example.utils.JsonData;
import org.example.vo.CouponRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author gaokd
 * @since 2023-03-28
 */
@RestController
@RequestMapping("/api/coupon_record/v1")
public class CouponRecordController {

    @Autowired
    private CouponRecordService couponRecordService;

    @ApiOperation("分页查询个人优惠券记录")
    @GetMapping("page")
    public JsonData page(
            @ApiParam(value = "当前页") @RequestParam(value = "page", defaultValue = "1") int page,
            @ApiParam(value = "每页显示多少条") @RequestParam(value = "size", defaultValue = "20") int size
    ){
        Map<String, Object> pageMap = couponRecordService.pageCouponRecordActivity(page, size);
        return JsonData.buildSuccess(pageMap);
    }

    @ApiOperation("查询优惠券记录详情")
    @GetMapping("/detail/{record_id}")
    public JsonData getCouponRecordDetail(@ApiParam(value = "优惠券记录id") @PathVariable("record_id") long recordId){
        CouponRecordVO couponRecordVO = couponRecordService.findByRecordId(recordId);

        return couponRecordVO == null ? JsonData.buildResult(BizCodeEnum.COUPON_NO_EXIST) : JsonData.buildSuccess(couponRecordVO);
    }


}


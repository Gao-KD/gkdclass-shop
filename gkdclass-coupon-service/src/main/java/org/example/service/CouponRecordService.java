package org.example.service;


import org.example.vo.CouponRecordVO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author gaokd
 * @since 2023-03-28
 */
public interface CouponRecordService {
    //分页查询个人优惠券记录
    Map<String,Object> pageCouponRecordActivity(int page, int size);

    //根据优惠券id查询
    List<CouponRecordVO> findByRecordId(long recordId);
}

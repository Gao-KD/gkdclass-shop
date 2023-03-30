package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.Interceptor.LoginInterceptor;
import org.example.mapper.CouponRecordMapper;
import org.example.model.CouponDO;
import org.example.model.CouponRecordDO;
import org.example.model.LoginUser;
import org.example.service.CouponRecordService;
import org.example.vo.CouponRecordVO;
import org.example.vo.CouponVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author gaokd
 * @since 2023-03-28
 */
@Service
public class CouponRecordServiceImpl implements CouponRecordService {
    @Autowired
    private CouponRecordMapper couponRecordMapper;

    /**
     * 用户优惠券记录分页
     * @param page
     * @param size
     * @return
     */
    @Override
    public Map<String, Object> pageCouponRecordActivity(int page, int size) {

        //获取用户的信息
        LoginUser loginUser = LoginInterceptor.threadLocal.get();


        //封装分页信息
        Page<CouponRecordDO > pageInfo = new Page<>(page, size);
        //查找对应的优惠券信息,根据pageInfo信息查找
        IPage<CouponRecordDO> iPage = couponRecordMapper.selectPage(pageInfo,new QueryWrapper<CouponRecordDO>()
                .eq("user_id", loginUser.getId())
                .orderByDesc("create_time" ));
        Map<String,Object> pageMap = new HashMap<>(3);
        pageMap.put("total_record", iPage.getTotal());
        pageMap.put("total_page", iPage.getPages());
        pageMap.put("current_data", iPage.getRecords().stream().map(obj->beanProcess(obj)).collect(Collectors.toList()));

        return pageMap;
    }

    @Override
    public CouponRecordVO findByRecordId(long recordId) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        CouponRecordDO couponRecordDO = couponRecordMapper.selectOne(new QueryWrapper<CouponRecordDO>()
                .eq("coupon_id", recordId)
                .eq("user_id", loginUser.getId()));
        if (couponRecordDO == null){
            return null;
        }
        return beanProcess(couponRecordDO);
    }


    private CouponRecordVO beanProcess(CouponRecordDO couponRecordDO) {
        CouponRecordVO couponRecordVO = new CouponRecordVO();
        BeanUtils.copyProperties(couponRecordDO, couponRecordVO);
        return couponRecordVO;
    }
}

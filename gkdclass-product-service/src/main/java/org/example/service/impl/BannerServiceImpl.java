package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.model.BannerDO;
import org.example.mapper.BannerMapper;
import org.example.service.BannerService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.vo.BannerVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author gaokd
 * @since 2023-03-30
 */
@Service
@Slf4j
public class BannerServiceImpl implements BannerService {
    @Autowired
    private BannerMapper bannerMapper;

    @Override
    public List<BannerVO> getList() {
        List<BannerDO> bannerDOList = bannerMapper.selectList(new QueryWrapper<BannerDO>().orderByAsc("weight"));
        List<BannerVO> bannerVOList = bannerDOList.stream().map(
                        obj -> {
                            BannerVO bannerDO = new BannerVO();
                            BeanUtils.copyProperties(obj, bannerDO);
                            return bannerDO;
                        })
                .collect(Collectors.toList());
        return bannerVOList;
    }
}

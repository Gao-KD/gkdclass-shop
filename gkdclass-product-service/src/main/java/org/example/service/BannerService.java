package org.example.service;

import org.example.model.BannerDO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.vo.BannerVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author gaokd
 * @since 2023-03-30
 */
public interface BannerService {

    List<BannerVO> getList();
}

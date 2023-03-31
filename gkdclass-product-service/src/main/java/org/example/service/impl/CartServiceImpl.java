package org.example.service.impl;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.Interceptor.LoginInterceptor;
import org.example.constant.CacheKey;
import org.example.enums.BizCodeEnum;
import org.example.exception.BizException;
import org.example.model.LoginUser;
import org.example.request.CartItemRequest;
import org.example.service.CartService;
import org.example.service.ProductService;
import org.example.vo.CartItemVO;
import org.example.vo.CartVO;
import org.example.vo.ProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ProductService productService;


    /**
     * 添加购物车
     * @param cartItemRequest
     */
    @Override
    public void addToCart(CartItemRequest cartItemRequest) {
        long productId = cartItemRequest.getProductId();
        int buyNum = cartItemRequest.getBuyNum();
        //获取购物车缓存的key
        BoundHashOperations<String, Object, Object> myCartOps = getMyCartOps();

        Object cacheObj = myCartOps.get(String.valueOf(productId));
        String result = "";


        if (cacheObj != null){
            //购物车中商品的信息
             result  = (String) cacheObj;
        }
        if (StringUtils.isBlank(result)){

            //不存在，则新建一个商品
            CartItemVO cartItemVO = new CartItemVO();
            //根据商品id查找
            ProductVO productVO = productService.findDetailById(productId);
            if (productVO == null){
                //找不到该商品，抛出异常
                throw new BizException(BizCodeEnum.CART_FAIL);
            }else {
                //设置号商品信息
                cartItemVO.setPrice(productVO.getPrice());
                cartItemVO.setBuyNum(buyNum);
                cartItemVO.setProductId(productId);
                cartItemVO.setProductImg(productVO.getCoverImg());
                cartItemVO.setProductTitle(productVO.getTitle());

                //Object -> String
                //存入hash中，用的是alibaba cloud中fastjson自带的object转换为json
                myCartOps.put(String.valueOf(productId), JSON.toJSONString(cartItemVO));
                log.info("myCart:{}",myCartOps.values());
            }

        }else {
            //存在，修改数量
            //Object -> String
            //result存在 则将result转换为object
            CartItemVO cartItemVO = JSON.parseObject(result, CartItemVO.class);
            cartItemVO.setBuyNum(cartItemVO.getBuyNum() + buyNum);

            //存入hash
            myCartOps.put(String.valueOf(productId),JSON.toJSONString(cartItemVO));
        }
    }

    /**
     * 清空购物车
     */
    @Override
    public void clearCart() {
        stringRedisTemplate.delete(getCartKey());
    }

    /**
     * 查看购物车
     * @return
     */

    @Override
    public CartVO myCart() {
        //获取全部购物项，获取最新的价格,先设置一个false
        List<CartItemVO> cartItemVOList = buildCartItem(false);

        //封装成CartVO
        CartVO cartVO = new CartVO();
        cartVO.setCartItemLists(cartItemVOList);

        return cartVO;
    }

    /**
     * 返回购物车信息，如果有最新的价格，还需要修改
     * @param latestPrice
     * @return
     */
    private List<CartItemVO> buildCartItem(boolean latestPrice) {
        //获取我的购物车
        BoundHashOperations<String, Object, Object> myCart = getMyCartOps();


        //获取我购物车的每一个购物项
        List<Object> itemList = myCart.values();

        List<CartItemVO> cartItemVOList = new ArrayList<>();

        //拼接id列表查询最新价格
        List<Long> productIdList = new ArrayList<>();

        //把每一个购物项封装到cartItemVOList
        for (Object item : itemList){
            CartItemVO cartItemVO = JSON.parseObject((String) item, CartItemVO.class);
            cartItemVOList.add(cartItemVO);
        }

        /**
         * 查询最新的商品价格
         */
        if (latestPrice){
            setProductLatestPrice(cartItemVOList,productIdList);
        }

        return cartItemVOList;
    }

    /**
     * 设置商品最新价格
     * @param cartItemVOList
     * @param productIdList
     */
    private void setProductLatestPrice(List<CartItemVO> cartItemVOList, List<Long> productIdList) {
        //根据购物所有的商品id去查询商品的属性
        List<ProductVO> productVOList = productService.findProductByIdBatch(productIdList);

       //把查询出来的商品列表转换为一个流，然后根据商品id进行分组排序                              //根据id分组
        Map<Long, ProductVO> maps = productVOList.stream().collect(Collectors.toMap(ProductVO::getId, Function.identity()));
        cartItemVOList.stream().forEach(item->{

            ProductVO productVO = maps.get(item.getProductId());
            item.setPrice(productVO.getPrice());
            item.setProductTitle(productVO.getTitle());
            item.setProductImg(productVO.getCoverImg());

        });
    }

    /**
     * 抽取购物车，通用方法
     * @return
     */
    public BoundHashOperations<String,Object,Object> getMyCartOps(){
        //cartKey = "cart:userId"
        String cartKey = getCartKey();

        //返回的是第二层里面的hash结果，即里面存放的key是商品的id，value是商品的信息
        return stringRedisTemplate.boundHashOps(cartKey);

    }


    private String getCartKey(){
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        String cartKey = String.format(CacheKey.CART_KEY,loginUser.getId().toString());

        return cartKey;
    }
}

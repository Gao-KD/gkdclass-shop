package org.example.Interceptor;

import com.mysql.cj.util.StringUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.example.enums.BizCodeEnum;
import org.example.model.LoginUser;
import org.example.utils.CommonUtil;
import org.example.utils.JsonData;
import org.example.utils.JwtUtil;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    public static ThreadLocal<LoginUser> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /**
         * 获取token
         * 1：判断header里面有没有token，如果没有，从parameter里面查找
         * 2：查到，就解密token
         */
        try {
            String accseeToken = request.getHeader("token");
            if (accseeToken == null){
                accseeToken = request.getParameter("token");
            }
            if (!StringUtils.isNullOrEmpty(accseeToken)){
                Claims claims = JwtUtil.checkJWT(accseeToken, request);

                if (claims == null){
                    CommonUtil.sendJsonMessage(response, JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN));
                    return false;
                }
                Long userId = Long.valueOf(claims.get("id").toString());
                String headImg = (String) claims.get("head_img");
                String mail = (String) claims.get("mail");
                String name = (String) claims.get("name");

                LoginUser loginUser = new LoginUser();
                loginUser.setId(userId);
                loginUser.setMail(mail);
                loginUser.setName(name);
                loginUser.setHeadImg(headImg);

                //todo 把从token中解析出来的用户信息传递给前端

                //1.request.setAttribute("loginUser",loginUser);
                //2.用threadLocal传递,在controller可以通过LoginInterceptor.threadlocal.get()得到
                threadLocal.set(loginUser);
                return true;

            }
        }catch (Exception e){
            log.error("拦截器错误:"+e);
        }

        CommonUtil.sendJsonMessage(response, JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN));
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}

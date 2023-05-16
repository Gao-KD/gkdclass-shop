package org.example.config;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.example.Interceptor.CorsIntercept;
import org.example.Interceptor.LoginInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;


@Configuration
@Slf4j
public class InterceptorConfig implements WebMvcConfigurer {

    @Bean
    CorsIntercept corsIntercept(){return new CorsIntercept();}

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new LoginInterceptor())
                //拦截的路径
                .addPathPatterns("/api/order/*/**","/api/order/v1/query_order")

                //排查不拦截的路径 支付回调
                .excludePathPatterns("/api/callback/*/**", "/api/order/*/query_state");

    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null){
                HttpServletRequest request = attributes.getRequest();
                if (request == null){
                    return;
                }else {
                    String token = request.getHeader("token");
                    requestTemplate.header("token", token);

                }
            }
        };
    }
}

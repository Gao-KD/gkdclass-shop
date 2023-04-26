package org.example.config;

import lombok.extern.slf4j.Slf4j;
import org.example.Interceptor.CorsIntercept;
import org.example.Interceptor.LoginInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;



@Configuration
@Slf4j
public class InterceptorConfig  implements WebMvcConfigurer {

    @Bean
    CorsIntercept corsIntercept(){return new CorsIntercept();}

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(corsIntercept())
                .addPathPatterns("/**");

        registry.addInterceptor(new LoginInterceptor())
                //拦截的路径
                .addPathPatterns("/api/user/*/**","/api/address/*/**")

                //排查不拦截的路径
                .excludePathPatterns("/api/user/*/send_login_code","/api/user/*/send_register_code","/api/user/*/kaptcha",
                        "/api/user/*/register","/api/user/*/login","/api/user/*/upload");

    }
}

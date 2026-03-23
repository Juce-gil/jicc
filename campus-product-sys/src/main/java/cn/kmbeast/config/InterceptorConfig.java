package cn.kmbeast.config;

import cn.kmbeast.Interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * API拦截器配置
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Value("${my-server.api-context-path}")
    private String API;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 拦截器注册
        registry.addInterceptor(new JwtInterceptor())
                .addPathPatterns("/**")
                // 放行无需登录的请求
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/file/**",
                        "/error",
                        "/**/query"
                );
    }
}

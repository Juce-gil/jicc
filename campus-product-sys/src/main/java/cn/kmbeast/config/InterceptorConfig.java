package cn.kmbeast.config;

import cn.kmbeast.Interceptor.JwtInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Global JWT interceptor configuration.
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JwtInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/error",
                        "/user/login",
                        "/user/register",
                        "/file/getFile",
                        "/category/query",
                        "/product/query",
                        "/product/queryProductList/*"
                );
    }
}

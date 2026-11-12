
package com.myworld.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final MemberStatusInterceptor memberStatusInterceptor;

    public WebMvcConfig(
            MemberStatusInterceptor memberStatusInterceptor
    ) {
        this.memberStatusInterceptor = memberStatusInterceptor;
    }

    @Override
    public void addInterceptors(
            InterceptorRegistry registry
    ) {

        registry.addInterceptor(memberStatusInterceptor)

                .addPathPatterns(
                        "/workspace",
                        "/workspace/**",
                        "/messenger",
                        "/messenger/**",
                        "/admin/**",
                        "/api",
                        "/api/**"
                );
    }
}

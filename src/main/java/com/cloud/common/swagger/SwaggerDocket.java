package com.cloud.common.swagger;

import com.google.common.base.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@EnableConfigurationProperties(SwaggerProperties.class)
public class SwaggerDocket {

    @Autowired
    private SwaggerProperties swaggerProperties;

    @Bean
    public Docket createDocket() {
        //API基本信息
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title(swaggerProperties.getTitle())
                .description(swaggerProperties.getDescription())
                .version(swaggerProperties.getVersion())
                .build();

        //扫描路径
        Predicate<String> paths = PathSelectors.none();
        if (swaggerProperties.isEnabled()) {
            paths = PathSelectors.any();
        }

        //创建Docket对象
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo)
                .select().apis(RequestHandlerSelectors.basePackage(swaggerProperties.getBasePackage()))
                .paths(paths)
                .build();
    }
}

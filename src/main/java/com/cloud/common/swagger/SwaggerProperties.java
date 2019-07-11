package com.cloud.common.swagger;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "swagger")
public class SwaggerProperties {

    private String version = "";
    private String title = "";
    private String description = "";
    private String basePackage = "";
    private boolean enabled = false;
}

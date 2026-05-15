package com.cxy.travelaiagent.tools;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "aliyun.oss")
public class AliyunOssConfig {
    private String endpoint;
    private String bucketName;
    private String region;
}
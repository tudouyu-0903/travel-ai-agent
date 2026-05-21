package com.cxy.travelaiagent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.ai.autoconfigure.vectorstore.pgvector.PgVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(exclude = PgVectorStoreAutoConfiguration.class)
@ConfigurationPropertiesScan
@MapperScan("com.cxy.travelaiagent.mapper")
public class TravelAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(TravelAiAgentApplication.class, args);
    }

}

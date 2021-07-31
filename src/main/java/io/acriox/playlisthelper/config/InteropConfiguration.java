package io.acriox.playlisthelper.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class InteropConfiguration {

    @Bean
    public RestTemplate defaultRestTemplate() {
        return new RestTemplate();
    }
}

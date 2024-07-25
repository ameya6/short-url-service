package org.url.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.url.http.DUIDHttp;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.url.model.ShortURL;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Configuration
public class ApplicationConfiguration {

    @Value("${duid.server}")
    private String duidServer;

    @Bean
    public RestClient.Builder restClient() {
        return RestClient.builder().baseUrl(duidServer);
    }

    @Bean
    public DUIDHttp duidHttp() {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter
                        .create(restClient()
                                .build()))
                .build();
        return factory.createClient(DUIDHttp.class);
    }

    @Bean
    public Map<String, ShortURL> shortURLMap() {
        return new HashMap<>();
    }

    @Bean
    public Map<String, String> aliasMap() {
        return new HashMap<>();
    }

    @Bean
    public Random random() {
        return new Random();
    }

    @Bean
    public AtomicLong counter() {
        return new AtomicLong(1);
    }
}

package org.url.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.url.http.DUIDHttp;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.url.http.ShortURLJpaHttp;
import org.url.http.ShortURLRedisHttp;
import org.url.utils.LocalDateTimeAdapter;
import java.time.LocalDateTime;
import java.util.Random;

@Configuration
public class ApplicationConfiguration {

    @Value("${server-url.duid}")
    private String duidServer;

    @Value("${server-url.jpa}")
    private String shortUrlJpaServer;

    @Value("${server-url.redis}")
    private String shortUrlRedisServer;

    @Bean
    public RestClient.Builder duidRestClient() {
        return RestClient.builder().baseUrl(duidServer);
    }

    @Bean
    public DUIDHttp duidHttp() {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter
                        .create(duidRestClient()
                                .build()))
                .build();
        return factory.createClient(DUIDHttp.class);
    }

    @Bean
    public RestClient.Builder shortUrlJpaRestClient() {
        return RestClient.builder().baseUrl(shortUrlJpaServer);
    }

    @Bean
    public ShortURLJpaHttp shortUrlJpaHttp() {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter
                        .create(shortUrlJpaRestClient()
                                .build()))
                .build();
        return factory.createClient(ShortURLJpaHttp.class);
    }

    @Bean
    public RestClient.Builder shortUrlRedisRestClient() {
        return RestClient.builder().baseUrl(shortUrlRedisServer);
    }

    @Bean
    public ShortURLRedisHttp shortUrlRedisHttp() {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter
                        .create(shortUrlRedisRestClient()
                                .build()))
                .build();
        return factory.createClient(ShortURLRedisHttp.class);
    }

    @Bean
    public Random random() {
        return new Random();
    }

    @Bean
    public Gson gson() {
        return new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
    }
}

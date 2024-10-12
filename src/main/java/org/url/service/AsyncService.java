package org.url.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.url.api.model.ShortURLDTO;
import org.url.http.ShortURLJpaHttp;
import org.url.http.ShortURLRedisHttp;

import java.util.concurrent.CompletableFuture;

@Service
@Log4j2
public class AsyncService {

    @Autowired
    private ShortURLJpaHttp shortURLJpaHttp;

    @Autowired
    private ShortURLRedisHttp shortURLRedisHttp;

    @Async
    public CompletableFuture<ShortURLDTO> redisSave(ShortURLDTO shortURLDTO) {
        try {
            log.info("Saving to redis id: " + shortURLDTO.getId());
            return CompletableFuture.completedFuture(shortURLRedisHttp.create(shortURLDTO));
        } catch (Exception e) {
            log.error("Exception while saving to redis: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async
    public CompletableFuture<ShortURLDTO> jpaSave(ShortURLDTO shortURLDTO) {
        try {
            log.info("Saving to DB id: " + shortURLDTO.getId());
            return CompletableFuture.completedFuture(shortURLJpaHttp.create(shortURLDTO));
        } catch (Exception e) {
            log.error("Exception while saving to DB: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async
    public CompletableFuture<ShortURLDTO> jpaFind(String alias) {
        try {
            log.info("Finding by alias: {}", alias);
            return CompletableFuture.completedFuture(shortURLJpaHttp.findByAlias(alias));
        } catch (Exception e) {
            log.error("Exception while saving to DB: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async
    public CompletableFuture<ShortURLDTO> getRandom() {
        try {
            return CompletableFuture.completedFuture(shortURLRedisHttp.getRandom());
        } catch (Exception e) {
            log.error("Exception while saving to DB: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }
}

package org.url.service;

import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.url.exceptions.InvalidShortURLException;
import org.url.http.DUIDHttp;
import org.url.http.ShortURLJpaHttp;
import org.url.http.ShortURLRedisHttp;
import org.url.model.DUIDResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.url.model.ShortURL;
import org.url.model.ShortURLRequest;
import org.url.model.ShortURLResponse;
import org.url.records.ShortURLAlias;
import org.url.repository.ShortURLRepositoryRedis;
import org.url.repository.ShortURLRepositoryJpa;

import javax.naming.directory.InvalidAttributesException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Log4j2
public class ShortURLService {

    @Autowired
    private DUIDHttp duidHttp;

    @Autowired
    private ShortURLJpaHttp shortURLJpaHttp;

    @Autowired
    private ShortURLRedisHttp shortURLRedisHttp;

    @Autowired
    private RandomGeneratorService randomGeneratorService;

    @Autowired
    private ShortURLRepositoryRedis shortURLRedisRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private Gson gson;

    private final static String PROTOCOL = "http://";
    private final static String DOMAIN = "shorty.cm/";
    private final static String ALIAS = "alias";
    private final static int ALIAS_LENGTH = 7;
    private final static int BASE_62 = 62;

    public ShortURLResponse shortenURL(ShortURLRequest request) throws Exception {
        long start = System.currentTimeMillis();
        log.info("Short URL Request : " + request);
        final ShortURLAlias alias = createAlias(request);
        ShortURL shortURL = shortURL(alias, request);
        log.info("Saving : " + shortURL.getAlias());
        //shortURL = shortURLJpaRepository.save(shortURL);
        shortURL = jpaSave(shortURL);
        CompletableFuture<ShortURL> redisSave = redisSave(shortURL);
        CompletableFuture<SendResult<String, Object>> result = kafkaProduce(shortURL);
        CompletableFuture.allOf(redisSave).join();
        log.info("Pushed to kafka : {}", result.get().getProducerRecord() );
        log.info("Short URL save " + shortURL + "Total time ms : {}", System.currentTimeMillis() - start);
        return shortURLResponse(shortURL);
    }

    private ShortURL jpaSave(ShortURL shortURL) {
        return shortURLJpaHttp.create(shortURL);
    }

    public List<ShortURL> get() {
        List<ShortURL> urls = shortURLJpaHttp.get();
        return urls;
    }

    private CompletableFuture<SendResult<String, Object>> kafkaProduce(ShortURL shortURL) {
        String shortURLJson = gson.toJson(shortURL);
        return kafkaTemplate.send("short-url-topic", shortURL.getId().toString(), shortURLJson);
    }

    @Async
    public CompletableFuture<ShortURL> redisSave(ShortURL shortURL) {
        try {
            log.info("Saving to redis id: " + shortURL.getId());
            return CompletableFuture.completedFuture(shortURLRedisHttp.create(shortURL));
        } catch (Exception e) {
            log.error("Exception while saving to redis: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async
    public CompletableFuture<Long> saveRedisAlias(String alias) throws InterruptedException {
        return CompletableFuture.completedFuture(redisTemplate.opsForSet().add(ALIAS, alias));
    }

    public ShortURLResponse getLongURLByAlias(String alias) throws InvalidAttributesException, InvalidShortURLException {
        Assert.isTrue(alias.length() == ALIAS_LENGTH, "Invalid Alias length");
        ShortURL urlData = shortURLRedisHttp.get(alias);
        if(urlData == null)
            throw new InvalidAttributesException("No Information found");
        if(LocalDateTime.now().isAfter(urlData.getExpiry()))
            throw new InvalidShortURLException("Entry expired");
        return shortURLResponse(urlData);
    }

    public ShortURLResponse shortenURLWriteTest() throws Exception {
        log.info("Short URL Test");
        ShortURLRequest request = ShortURLRequest.builder().longURL(randomGeneratorService.getRandomURL()).build();
        final ShortURLAlias alias = createAlias(request);
        ShortURL shortURL = shortURL(alias, request);
        log.info("Saving : " + shortURL);
        CompletableFuture<ShortURL> redisSave = redisSave(shortURL);
        CompletableFuture<Long> saveRedisAlias = saveRedisAlias(shortURL.getAlias());
        CompletableFuture.allOf(redisSave, saveRedisAlias).join();
        return shortURLResponse(shortURL);
    }

    public ShortURLResponse getLongURLByAliasTest() throws InvalidAttributesException, InvalidShortURLException {
        String alias = redisTemplate.opsForSet().randomMember(ALIAS);
        return shortURLResponse(shortURLRedisRepository.findByAlias(alias));
    }

    private ShortURL shortURL(ShortURLAlias alias, ShortURLRequest request) {
        LocalDateTime expiry = validateExpiry(request.getExpiry()) ? request.getExpiry() : generateExpiry();
        return ShortURL.builder()
                .longURL(request.getLongURL())
                .alias(alias.alias())
                .createdAt(LocalDateTime.now())
                .distributedId(alias.duid())
                .expiry(expiry)
                .build();
    }

    private boolean validateExpiry(LocalDateTime expiry) {
        return expiry != null && expiry.isAfter(LocalDateTime.now());
    }

    private ShortURLResponse shortURLResponse(ShortURL shortURL) {
        return ShortURLResponse.builder()
                .longURL(shortURL.getLongURL())
                .expiry(shortURL.getExpiry())
                .shortURL(PROTOCOL + DOMAIN + shortURL.getAlias())
                .build();
    }

    private LocalDateTime generateExpiry() {
        return LocalDateTime.now().plusDays(randomGeneratorService.getRandomInt(1,365));
    }

    private ShortURLAlias createAlias(ShortURLRequest request) throws Exception {
        if(StringUtils.hasText(request.getAlias())) {
            return validateAlias(request.getAlias());
        }
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        int count = ALIAS_LENGTH;
        long duid = duid().getDuid();
        long tempDuid = duid;
        StringBuilder alias = new StringBuilder();
        while (tempDuid > 0 && count != 0) {
            int index = Math.floorMod(tempDuid, BASE_62);
            alias.append(chars.charAt(index));
            tempDuid /= BASE_62;
            count--;
        }
        return ShortURLAlias.of(duid, alias.toString());
    }

    private ShortURLAlias validateAlias(String alias) throws InvalidAttributesException {
        Assert.isTrue(alias.length() == ALIAS_LENGTH, "Alias length incorrect, should be of length " + ALIAS_LENGTH);
        if(shortURLRedisHttp.get(alias) != null)
            throw new InvalidAttributesException("Alias already exists");
        return ShortURLAlias.of(null, alias);
    }

    private DUIDResponse duid() {
        return duidHttp.generate();
    }
}

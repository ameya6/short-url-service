package org.url.service;

import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import org.duid.model.DUIDResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.url.api.model.ShortURLDTO;
import org.url.api.model.ShortURLRequest;
import org.url.api.model.ShortURLResponse;
import org.url.exceptions.InvalidShortURLException;
import org.url.http.DUIDHttp;
import org.url.http.ShortURLJpaHttp;
import org.url.http.ShortURLRedisHttp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.url.model.ShortURL;
import org.url.records.ShortURLAlias;
import javax.naming.directory.InvalidAttributesException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@Log4j2
public class ShortURLService {

    @Autowired
    private DUIDHttp duidHttp;

    @Autowired
    private AsyncService asyncService;

    @Autowired
    private ShortURLRedisHttp shortURLRedisHttp;

    @Autowired
    private ShortURLJpaHttp shortURLJpaHttp;

    @Autowired
    private RandomGeneratorService randomGeneratorService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private Gson gson;

    @Value("${url-info.protocol}")
    private String protocol;

    @Value("${url-info.domain}")
    private String domain;

    @Value("${url-info.base-62}")
    private Integer base62;

    @Value("${url-info.alias-length}")
    private Integer aliasLength;


    public ShortURLResponse shortenURL(ShortURLRequest request) throws Exception {
        long start = System.currentTimeMillis();
        log.info("Short URL Request : {}", request);
        final ShortURLAlias alias = createAlias(request);
        ShortURL shortURL = shortURL(alias, request);
        ShortURLDTO shortURLDTO = save(shortURL);
        return shortURLResponse(shortURLDTO);
    }

    /*private CompletableFuture<SendResult<String, Object>> kafkaProduce(ShortURL shortURL) {
        String shortURLJson = gson.toJson(shortURL);
        return kafkaTemplate.send("short-url-topic", shortURL.getId().toString(), shortURLJson);
    }*/

    public ShortURLResponse findByAlias(String alias) throws InvalidAttributesException, InvalidShortURLException {
        Assert.isTrue(alias.length() == aliasLength, "Invalid Alias length");
        ShortURLDTO urlData = shortURLRedisHttp.get(alias);
        if(urlData == null)
            throw new InvalidAttributesException("No Information found");
        if(urlData.getId() == null) {
            urlData = shortURLJpaHttp.findByAlias(urlData.getAlias());
        }
        if(LocalDateTime.now().isAfter(urlData.getExpiry()))
            throw new InvalidShortURLException("Entry expired");
        return shortURLResponse(urlData);
    }

    public ShortURLResponse shortenURLWriteTest() throws Exception {
        log.info("Short URL Test");
        ShortURLRequest request = createRequest();
        final ShortURLAlias alias = createAlias(request);
        log.info("DUID : {} Alias : {}", alias.duid(), alias.alias());
        ShortURL shortURL = shortURL(alias, request);
        ShortURLDTO shortURLDTO = save(shortURL);
        return shortURLResponse(shortURLDTO);
    }

    public ShortURLResponse getRandomURLInfo() throws ExecutionException, InterruptedException {
        ShortURLDTO shortURLDTO = shortURLRedisHttp.getRandom();
        if(shortURLDTO.getId() == null) {
            shortURLDTO = shortURLJpaHttp.findByAlias(shortURLDTO.getAlias());
        }
        log.info("Random data, alias : {}, id : {}", shortURLDTO.getAlias(), shortURLDTO.getId());
        return shortURLResponse(shortURLDTO);
    }

    private ShortURLDTO save(ShortURL shortURL) throws ExecutionException, InterruptedException {
        log.info("Saving : " + shortURL);
        ShortURLDTO shortURLDTO = toShortURLDTO(shortURL);
        CompletableFuture<ShortURLDTO> redisSave = asyncService.redisSave(shortURLDTO);
        CompletableFuture<ShortURLDTO> jpaSave = asyncService.jpaSave(shortURLDTO);
        CompletableFuture.allOf(redisSave, jpaSave).join();
        return redisSave.get();
    }

    private ShortURL shortURL(ShortURLAlias alias, ShortURLRequest request) {
        LocalDateTime expiry = validateExpiry(request.getExpiry()) ? request.getExpiry() : generateExpiry();
        return ShortURL.builder()
                .id(UUID.randomUUID())
                .longURL(request.getLongURL())
                .alias(alias.alias())
                .createdAt(LocalDateTime.now())
                .distributedId(alias.duid())
                .expiry(expiry)
                .build();
    }

    private ShortURLDTO toShortURLDTO(ShortURL shortURL) {
        return ShortURLDTO.builder()
                .id(shortURL.getId())
                .longURL(shortURL.getLongURL())
                .alias(shortURL.getAlias())
                .createdAt(LocalDateTime.now())
                .distributedId(shortURL.getDistributedId())
                .expiry(shortURL.getExpiry())
                .build();
    }

    private ShortURLRequest createRequest() {
        return ShortURLRequest.builder()
                .longURL(randomGeneratorService.getRandomURL())
                .build();
    }

    private boolean validateExpiry(LocalDateTime expiry) {
        return expiry != null && expiry.isAfter(LocalDateTime.now());
    }

    private ShortURLResponse shortURLResponse(ShortURLDTO shortURLDTO) {
        return ShortURLResponse.builder()
                .id(shortURLDTO.getId())
                .longURL(shortURLDTO.getLongURL())
                .expiry(shortURLDTO.getExpiry())
                .shortURL(protocol + domain + shortURLDTO.getAlias())
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
        int count = aliasLength;
        long duid = duid().getDuid();
        long tempDuid = duid;
        StringBuilder alias = new StringBuilder();
        while (tempDuid > 0 && count != 0) {
            int index = Math.floorMod(tempDuid, base62);
            alias.append(chars.charAt(index));
            tempDuid /= base62;
            count--;
        }
        return ShortURLAlias.of(duid, alias.toString());
    }

    private ShortURLAlias validateAlias(String alias) throws InvalidAttributesException {
        Assert.isTrue(alias.length() == aliasLength, "Alias length incorrect, should be of length " + aliasLength);
        if(shortURLRedisHttp.get(alias) != null)
            throw new InvalidAttributesException("Alias already exists");
        return ShortURLAlias.of(null, alias);
    }

    private DUIDResponse duid() {
        return duidHttp.generate();
    }
}

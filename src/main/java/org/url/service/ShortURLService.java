package org.url.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.url.exceptions.InvalidShortURLException;
import org.url.http.DUIDHttp;
import org.url.model.DUIDResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.url.model.ShortURL;
import org.url.model.ShortURLRequest;
import org.url.model.ShortURLResponse;
import org.url.records.ShortURLAlias;
import org.url.repository.ShortURLRepository;
import org.url.repository.ShortURLTestRepository;

import javax.naming.directory.InvalidAttributesException;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Log4j2
public class ShortURLService {

    @Autowired
    private DUIDHttp duidHttp;

    @Autowired
    private ShortURLRepository shortURLRepository;

    @Autowired
    private ShortURLTestRepository shortURLTestRepository;

    @Autowired
    private RandomGeneratorService randomGeneratorService;

    @Autowired
    private AtomicLong counter;

    @Value("${test.url.origin}")
    private long randomOrigin;

    @Value("${test.url.bound}")
    private long randomBound;

    private final static String PROTOCOL = "http://";
    private final static String DOMAIN = "shorty.cm/";
    private final static int ALIAS_LENGTH = 7;
    private final static int BASE_62 = 62;

    public ShortURLResponse shortenURL(ShortURLRequest request) throws Exception {
        log.info("Short URL Request : " + request);
        final ShortURLAlias alias = createAlias(request);
        ShortURL shortURL = shortURL(alias, request);
        log.info("Saving : " + shortURL);
        shortURLRepository.save(shortURL);
        return shortURLResponse(shortURL);
    }

    public ShortURLResponse getLongURLByAlias(String alias) throws InvalidAttributesException, InvalidShortURLException {
        Assert.isTrue(alias.length() != ALIAS_LENGTH, "Invalid Alias length");
        ShortURL urlData = shortURLRepository.findByAlias(alias);
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
        shortURLRepository.save(shortURL);
        shortURLTestRepository.save(counter.incrementAndGet(), shortURL.getAlias());
        return shortURLResponse(shortURL);
    }

    public ShortURLResponse getLongURLByAliasTest() throws InvalidAttributesException, InvalidShortURLException {
        String alias = shortURLTestRepository.find(randomGeneratorService.getRandomLong(1, 1_000_000));
        while(alias == null) {
            alias = shortURLTestRepository.find(randomGeneratorService.getRandomLong(1, 1_000_000));
        }
        return shortURLResponse(shortURLRepository.findByAlias(alias));
    }

    private ShortURL shortURL(ShortURLAlias alias, ShortURLRequest request) {
        LocalDateTime expiry = validateExpiry(request.getExpiry()) ? request.getExpiry() : generateExpiry();
        return ShortURL.builder()
                .longURL(request.getLongURL())
                .alias(alias.alias())
                .id(UUID.randomUUID())
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
        Long duid = duid().getDuid();
        StringBuilder alias = new StringBuilder();
        while (duid > 0 && count != 0) {
            int index = Math.floorMod(duid, BASE_62);
            alias.append(chars.charAt(index));
            duid /= BASE_62;
            count--;
        }
        return ShortURLAlias.of(duid, alias.toString());
    }

    private ShortURLAlias validateAlias(String alias) throws InvalidAttributesException {
        if(shortURLRepository.findByAlias(alias) != null)
            throw new InvalidAttributesException("Alias already exists");
        if(alias.length() != ALIAS_LENGTH)
            throw new InvalidAttributesException("Alias length incorrect, should be of length " + ALIAS_LENGTH);
        return ShortURLAlias.of(null, alias);
    }
    private DUIDResponse duid() {
        return duidHttp.generate();
    }
}

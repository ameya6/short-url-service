package org.url.http;


import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.url.model.ShortURL;

import java.util.List;

@HttpExchange("/api/v1/redis/url")
public interface ShortURLRedisHttp {

    @GetExchange("/{alias}")
    ShortURL get(@PathVariable String alias);

    @PostExchange("/create")
    ShortURL create(@RequestBody ShortURL shortURL);
}

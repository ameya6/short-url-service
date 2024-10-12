package org.url.http;


import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.url.api.model.ShortURLDTO;

@HttpExchange("/redis/url")
public interface ShortURLRedisHttp {

    @PostExchange("/create")
    ShortURLDTO create(@RequestBody ShortURLDTO shortURLDTO);

    @GetExchange("/{alias}")
    ShortURLDTO get(@PathVariable String alias);

    @GetExchange("/random")
    ShortURLDTO getRandom();
}

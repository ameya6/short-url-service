package org.url.http;


import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.url.model.DUIDResponse;
import org.url.model.ShortURL;

import java.util.List;

@HttpExchange("/api/v1/jpa/url")
public interface ShortURLJpaHttp {

    @GetExchange("/")
    List<ShortURL> get();

    @PostExchange("/create")
    ShortURL create(@RequestBody ShortURL shortURL);
}

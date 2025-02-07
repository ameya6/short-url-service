package org.url.http;


import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.url.api.model.ShortURLDTO;
import org.url.model.ShortURL;

import java.util.List;

@HttpExchange("/jpa/url")
public interface ShortURLJpaHttp {

    @GetExchange("/")
    List<ShortURL> get();

    @PostExchange("/create")
    ShortURLDTO create(@RequestBody ShortURLDTO shortURLDTO);

    @PostExchange("/fetch/${alias}")
    ShortURLDTO findByAlias(@PathVariable String alias);
}

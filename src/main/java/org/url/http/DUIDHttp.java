package org.url.http;


import org.duid.model.DUIDResponse;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/duid")
public interface DUIDHttp {

    @GetExchange("/generate")
    DUIDResponse generate();
}

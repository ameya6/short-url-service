package org.url.http;


import org.url.model.DUIDResponse;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/duid-service/api/v1/duid")
public interface DUIDHttp {

    @GetExchange("/generate")
    DUIDResponse generate();
}

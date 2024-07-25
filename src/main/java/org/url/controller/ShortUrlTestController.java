package org.url.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.url.model.ShortURLRequest;
import org.url.model.ShortURLResponse;
import org.url.service.ShortURLService;

@RestController
@Log4j2
@RequestMapping("/api/v1/test/url")
public class ShortUrlTestController {

    @Autowired
    private ShortURLService shortURLService;

    @PostMapping("/write")
    public ResponseEntity<ShortURLResponse> writeTest() {
        try {
            return ResponseEntity.ok(shortURLService.shortenURLWriteTest());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ShortURLResponse.builder().exception(e.getMessage()).build());
        }
    }

    @GetMapping("/read")
    public ResponseEntity<ShortURLResponse> readTest() {
        try {
            return ResponseEntity.ok(shortURLService.getLongURLByAliasTest());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ShortURLResponse.builder().exception(e.getMessage()).build());
        }
    }
}

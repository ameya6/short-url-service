package org.url.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.url.model.ShortURL;
import org.url.model.ShortURLRequest;
import org.url.model.ShortURLResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.url.service.ShortURLService;

import java.util.List;

@RestController
@Log4j2
@RequestMapping("/api/v1/url")
public class ShortUrlController {

    @Autowired
    private ShortURLService shortURLService;

    @PostMapping("/create")
    public ResponseEntity<ShortURLResponse> create(@RequestBody ShortURLRequest shortURLRequest) {
        try {
            return ResponseEntity.ok(shortURLService.shortenURL(shortURLRequest));
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ShortURLResponse.builder().exception(e.getMessage()).build());
        }
    }

    @GetMapping("/")
    public ResponseEntity<List<ShortURL>> get() {
        return ResponseEntity.ok(shortURLService.get());
    }

    @GetMapping("/fetch/{alias}")
    public ResponseEntity<ShortURLResponse> getURLByAlias(@PathVariable String alias) {
        try {
            return ResponseEntity.ok(shortURLService.getLongURLByAlias(alias));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ShortURLResponse.builder().exception(e.getMessage()).build());
        }
    }
}

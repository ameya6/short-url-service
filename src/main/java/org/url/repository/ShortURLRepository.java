package org.url.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.url.model.ShortURL;

import java.util.Map;

@Repository
public class ShortURLRepository {

    @Autowired
    private Map<String, ShortURL> shortURLMap;

    public ShortURL save(ShortURL shortURL) {
        return shortURLMap.put(shortURL.getAlias(), shortURL);
    }

    public ShortURL findByAlias(String alias) {
        return shortURLMap.get(alias);
    }
}

package org.url.repository;

import org.springframework.data.repository.CrudRepository;
import org.url.model.ShortURL;
public interface ShortURLRepositoryRedis extends CrudRepository<ShortURL, String> {
    ShortURL findByAlias(String alias);

}

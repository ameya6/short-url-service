package org.url.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.url.model.ShortURL;

@Repository
public interface ShortURLRedisRepository extends CrudRepository<ShortURL, String> {
    ShortURL findByAlias(String alias);

}

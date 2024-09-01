package org.url.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.url.model.ShortURL;

import java.util.UUID;

public interface ShortURLRepositoryJpa extends JpaRepository<ShortURL, UUID> {
    ShortURL findByAlias(String alias);
}

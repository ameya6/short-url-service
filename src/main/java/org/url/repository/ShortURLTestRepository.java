package org.url.repository;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
@Log4j2
public class ShortURLTestRepository {

    private final static String ALIAS = "alias:";

    @Autowired
    private Map<String, String> aliasMap;

    public void save(Long counter, String alias) {
        aliasMap.put("alias:"+counter, alias);
    }

    public String find(Long counter) {
        return aliasMap.get(ALIAS + counter);
    }
}

package org.url.records;

public record ShortURLAlias(Long duid, String alias) {

    public static ShortURLAlias of(Long duid, String alias) {
        return new ShortURLAlias(duid, alias);
    }
}

package org.url.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ShortURL {
    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime expiry;
    private Long distributedId;
    private String longURL;
    private String alias;
}

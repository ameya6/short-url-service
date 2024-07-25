package org.url.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShortURL {
    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime expiry;
    private Long distributedId;
    private String longURL;
    private String alias;

    @Override
    public String toString() {
        return "ShortURL{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", expiry=" + expiry +
                ", distributedId=" + distributedId +
                ", longURL='" + longURL + '\'' +
                ", alias='" + alias + '\'' +
                '}';
    }
}

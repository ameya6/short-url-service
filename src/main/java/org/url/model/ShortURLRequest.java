package org.url.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShortURLRequest {
    private String longURL;
    private LocalDateTime expiry;
    private String alias;

    @Override
    public String toString() {
        return "ShortURLRequest{" +
                "longURL='" + longURL + '\'' +
                ", expiry=" + expiry +
                ", alias='" + alias + '\'' +
                '}';
    }
}

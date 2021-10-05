package io.bosler.passport.library.models;

import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity @Data @NoArgsConstructor @AllArgsConstructor @Getter
@Setter
public class TokenLongLived {
    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    private UUID userId;
    private String name;
    private Date expiration;

}
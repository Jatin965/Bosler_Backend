package io.bosler.fractal.library.models;

import lombok.*;

import javax.persistence.Entity;
import java.util.UUID;


@Data
@AllArgsConstructor
public class GitLog {

    private String id;
    private String username;
    private String email;
    private String body;
    private String message;
}

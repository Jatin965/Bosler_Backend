package io.bosler.fractal.library.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GitCommit {

    private String email;
    private String username;
    private String message;
    private String id;
}

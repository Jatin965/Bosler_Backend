package io.bosler.fractal.library.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Files {

    private String fileName;
    private boolean folder;
    private String path;
}

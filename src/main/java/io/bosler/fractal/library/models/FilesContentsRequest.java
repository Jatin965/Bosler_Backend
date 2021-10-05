package io.bosler.fractal.library.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilesContentsRequest {
//    private String fileName;
    private String filePath;
//    private String refs;
}

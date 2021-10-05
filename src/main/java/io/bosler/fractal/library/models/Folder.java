package io.bosler.fractal.library.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Folder {

        private String name;
        private String path;
        private int depth;
        private String mode;
        private String type;
        private boolean isLeaf;
        private boolean isHidden;
        private String key;
}


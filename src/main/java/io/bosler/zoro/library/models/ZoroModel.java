package io.bosler.zoro.library.models;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "folder")
public class ZoroModel {

    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    @NotEmpty(message = "Name is mandatory")
    @Size(min=2, max=100)
    @Pattern(regexp = "[a-zA-z0-9\\s\\-_]*", message = "Name has to be alphanumeric with no special characters, spaces minus( - ) underscore( _ ) are allowed.")
    public String name;
    public String description;
    @NotEmpty(message = "Type is mandatory")
    public String type;
    public UUID parent;
    public String status;
    @CreatedDate
    public Date created_at;
    @LastModifiedDate
    public Date updated_at;
    public String created_by;
    public String updated_by;
}

package io.bosler.kitab.library.models;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import javax.validation.Valid;
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
@Table(name = "dataset")
public class DatasetModel {

    // Declare the fields for each record - these are all the properties of a pet

    public @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    @NotEmpty(message = "Name is mandatory")
    @Size(min = 2, max = 100)
    @Pattern(regexp = "[a-zA-z0-9\\s\\-_]*", message = "Name has to be alphanumeric with no special characters, spaces minus( - ) underscore( _ ) are allowed.")
    public String name;
    public String type;
    @CreationTimestamp
    public Date created_at = new Date();
    @LastModifiedDate
    public Date updated_at = new Date();
    public String created_by = "Bosler";
    public String updated_by = "Bosler";

}

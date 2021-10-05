package io.bosler.passport.library.models;

import lombok.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Groups {

    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    @NotEmpty(message = "Name is mandatory")
    @Size(min=2, max=100)
    @Pattern(regexp = "[a-zA-z0-9\\s\\-_]*", message = "Name has to be alphanumeric with no special characters, spaces minus( - ) underscore( _ ) are allowed.")
    public String name;
    public String description;
    @OneToMany
    @LazyCollection(LazyCollectionOption.FALSE)
    private Collection<Users> groups_owners = new ArrayList<>();
    @OneToMany
    @LazyCollection(LazyCollectionOption.FALSE)
    private Collection<Users> groups_administrators = new ArrayList<>();
    @OneToMany
    @LazyCollection(LazyCollectionOption.FALSE)
    private Collection<Users> groups_members = new ArrayList<>();
    public String status;
    @CreatedDate
    public Date created_at;
    @LastModifiedDate
    public Date updated_at;
    public String created_by;
    public String updated_by;


}

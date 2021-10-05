package io.bosler.fractal.library.models;

import lombok.*;

//import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FractalModel {

    // Declare the fields for each record - these are all the properties of a pet

    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    public String name;
    public LocalDate dateOfBirth;
    public String color;

}

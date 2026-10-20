package sn.smtg.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "utilisateurs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nom;
    private String prenom;

    @Column(unique = true)
    private String email;

    private String motDePasse;

    private LocalDateTime dateInscription;

    @PrePersist
    public void prePersist() {
        this.dateInscription = LocalDateTime.now();
    }
}

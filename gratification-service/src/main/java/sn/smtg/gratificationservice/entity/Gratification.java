package sn.smtg.gratificationservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "gratifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Gratification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer utilisateurId;
    private Integer quizId;
    private Integer score;
    private Integer points;

    private LocalDateTime dateObtention;

    @PrePersist
    public void prePersist() {
        this.dateObtention = LocalDateTime.now();
    }
}

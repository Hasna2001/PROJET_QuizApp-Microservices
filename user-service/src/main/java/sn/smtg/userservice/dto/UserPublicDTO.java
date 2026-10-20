package sn.smtg.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Vue publique d'un utilisateur : volontairement SANS le mot de passe (hash BCrypt). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPublicDTO {
    private Integer id;
    private String nom;
    private String prenom;
    private String email;
    private LocalDateTime dateInscription;
}

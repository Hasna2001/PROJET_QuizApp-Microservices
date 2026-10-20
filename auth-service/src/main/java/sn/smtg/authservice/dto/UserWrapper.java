package sn.smtg.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserWrapper {
    private Integer id;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;   // hash BCrypt, nécessaire à la vérification
}

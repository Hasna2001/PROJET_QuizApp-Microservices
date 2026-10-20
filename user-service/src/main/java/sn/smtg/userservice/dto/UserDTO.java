package sn.smtg.userservice.dto;

import lombok.Data;

@Data
public class UserDTO {
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
}

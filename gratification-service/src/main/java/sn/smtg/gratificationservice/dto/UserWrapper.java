package sn.smtg.gratificationservice.dto;

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
}

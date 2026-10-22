package sn.smtg.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthDTO {
    private Integer id;
    private String email;
    private String motDePasse;
}

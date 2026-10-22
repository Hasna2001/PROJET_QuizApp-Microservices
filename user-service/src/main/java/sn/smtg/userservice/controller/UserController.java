package sn.smtg.userservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.smtg.userservice.dto.UserAuthDTO;
import sn.smtg.userservice.dto.UserDTO;
import sn.smtg.userservice.dto.UserPublicDTO;
import sn.smtg.userservice.service.UserService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    UserService userService;

    @Value("${user.service.internal-token:}")
    private String internalServiceToken;

    @PostMapping("inscrire")
    public ResponseEntity<String> inscrire(@RequestBody UserDTO userDTO) {
        return userService.inscrire(userDTO);
    }

    @GetMapping("{id}")
    public ResponseEntity<UserPublicDTO> getUserById(@PathVariable Integer id) {
        return userService.getUserById(id);
    }

    @GetMapping("email/{email}")
    public ResponseEntity<UserPublicDTO> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email);
    }

    @GetMapping("internal/email/{email}")
    public ResponseEntity<UserAuthDTO> getUserForAuthentication(
            @PathVariable String email,
            @RequestHeader(value = "X-Internal-Service-Token", required = false) String serviceToken) {
        if (internalServiceToken.isBlank() || serviceToken == null
                || !MessageDigest.isEqual(
                        internalServiceToken.getBytes(StandardCharsets.UTF_8),
                        serviceToken.getBytes(StandardCharsets.UTF_8))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return userService.getUserForAuthenticationByEmail(email);
    }

    @GetMapping("all")
    public ResponseEntity<List<UserPublicDTO>> getAllUsers() {
        return userService.getAllUsers();
    }

    @PutMapping("modifier/{id}")
    public ResponseEntity<String> modifierUser(@PathVariable Integer id, @RequestBody UserDTO userDTO) {
        return userService.modifierUser(id, userDTO);
    }

    @DeleteMapping("supprimer/{id}")
    public ResponseEntity<String> supprimerUser(@PathVariable Integer id) {
        return userService.supprimerUser(id);
    }
}

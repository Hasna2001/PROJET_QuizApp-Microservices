package sn.smtg.userservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.smtg.userservice.dto.UserDTO;
import sn.smtg.userservice.dto.UserPublicDTO;
import sn.smtg.userservice.entity.User;
import sn.smtg.userservice.service.UserService;

import java.util.List;

@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("inscrire")
    public ResponseEntity<String> inscrire(@RequestBody UserDTO userDTO) {
        return userService.inscrire(userDTO);
    }

    @GetMapping("{id}")
    public ResponseEntity<UserPublicDTO> getUserById(@PathVariable Integer id) {
        return userService.getUserById(id);
    }

    @GetMapping("email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email);
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

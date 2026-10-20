package sn.smtg.userservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import sn.smtg.userservice.dto.UserDTO;
import sn.smtg.userservice.dto.UserPublicDTO;
import sn.smtg.userservice.entity.User;
import sn.smtg.userservice.repository.UserRepo;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    UserRepo userRepo;

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    public ResponseEntity<String> inscrire(UserDTO userDTO) {
        if (userDTO.getMotDePasse() == null || userDTO.getMotDePasse().isBlank()) {
            return new ResponseEntity<>("Mot de passe requis", HttpStatus.BAD_REQUEST);
        }
        if (userDTO.getEmail() == null || userDTO.getEmail().isBlank()) {
            return new ResponseEntity<>("Email requis", HttpStatus.BAD_REQUEST);
        }
        if (userRepo.findByEmail(userDTO.getEmail()).isPresent()) {
            return new ResponseEntity<>("Email déjà utilisé", HttpStatus.CONFLICT);
        }
        User user = new User();
        user.setNom(userDTO.getNom());
        user.setPrenom(userDTO.getPrenom());
        user.setEmail(userDTO.getEmail());
        user.setMotDePasse(passwordEncoder.encode(userDTO.getMotDePasse()));
        userRepo.save(user);
        return new ResponseEntity<>("Inscription réussie", HttpStatus.CREATED);
    }

    private UserPublicDTO toPublic(User u) {
        return new UserPublicDTO(u.getId(), u.getNom(), u.getPrenom(), u.getEmail(), u.getDateInscription());
    }

    public ResponseEntity<UserPublicDTO> getUserById(Integer id) {
        return userRepo.findById(id)
                .map(u -> new ResponseEntity<>(toPublic(u), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    public ResponseEntity<User> getUserByEmail(String email) {
        return userRepo.findByEmail(email)
                .map(u -> new ResponseEntity<>(u, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    public ResponseEntity<List<UserPublicDTO>> getAllUsers() {
        return new ResponseEntity<>(userRepo.findAll().stream().map(this::toPublic).toList(), HttpStatus.OK);
    }

    public ResponseEntity<String> modifierUser(Integer id, UserDTO dto) {
        Optional<User> opt = userRepo.findById(id);
        if (opt.isEmpty()) {
            return new ResponseEntity<>("Utilisateur introuvable", HttpStatus.NOT_FOUND);
        }
        User user = opt.get();
        if (dto.getNom() != null) user.setNom(dto.getNom());
        if (dto.getPrenom() != null) user.setPrenom(dto.getPrenom());
        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            if (userRepo.findByEmail(dto.getEmail()).isPresent()) {
                return new ResponseEntity<>("Email déjà utilisé", HttpStatus.CONFLICT);
            }
            user.setEmail(dto.getEmail());
        }
        if (dto.getMotDePasse() != null && !dto.getMotDePasse().isBlank()) {
            user.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));
        }
        userRepo.save(user);
        return new ResponseEntity<>("Utilisateur modifié", HttpStatus.OK);
    }

    public ResponseEntity<String> supprimerUser(Integer id) {
        if (!userRepo.existsById(id)) {
            return new ResponseEntity<>("Utilisateur introuvable", HttpStatus.NOT_FOUND);
        }
        userRepo.deleteById(id);
        return new ResponseEntity<>("Utilisateur supprimé", HttpStatus.OK);
    }
}

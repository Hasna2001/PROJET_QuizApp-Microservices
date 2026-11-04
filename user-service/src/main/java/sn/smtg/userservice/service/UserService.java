package sn.smtg.userservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.smtg.userservice.dto.PasswordResetCompletionRequest;
import sn.smtg.userservice.dto.PasswordResetTokenRequest;
import sn.smtg.userservice.dto.UserAuthDTO;
import sn.smtg.userservice.dto.UserDTO;
import sn.smtg.userservice.dto.UserPublicDTO;
import sn.smtg.userservice.entity.PasswordResetToken;
import sn.smtg.userservice.entity.User;
import sn.smtg.userservice.repository.PasswordResetTokenRepo;
import sn.smtg.userservice.repository.UserRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    UserRepo userRepo;

    @Autowired
    PasswordResetTokenRepo passwordResetTokenRepo;

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

    public ResponseEntity<UserPublicDTO> getUserByEmail(String email) {
        return userRepo.findByEmail(email)
                .map(u -> new ResponseEntity<>(toPublic(u), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    public ResponseEntity<UserAuthDTO> getUserForAuthenticationByEmail(String email) {
        return userRepo.findByEmail(email)
                .map(u -> new ResponseEntity<>(
                        new UserAuthDTO(u.getId(), u.getEmail(), u.getMotDePasse()), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public ResponseEntity<String> enregistrerJetonReinitialisation(PasswordResetTokenRequest request) {
        if (request == null || request.email() == null || request.email().isBlank()
                || request.tokenHash() == null || !request.tokenHash().matches("[a-f0-9]{64}")
                || request.expiresAt() == null
                || !request.expiresAt().isAfter(LocalDateTime.now())
                || request.expiresAt().isAfter(LocalDateTime.now().plusMinutes(31))) {
            return ResponseEntity.badRequest().body("Demande de réinitialisation invalide.");
        }
        if (!userRepo.existsByEmail(request.email())) {
            return ResponseEntity.notFound().build();
        }

        passwordResetTokenRepo.deleteByExpiresAtBefore(LocalDateTime.now());
        passwordResetTokenRepo.deleteByEmail(request.email());
        passwordResetTokenRepo.save(new PasswordResetToken(
                null, request.email(), request.tokenHash(), request.expiresAt()));
        return ResponseEntity.ok("Jeton de réinitialisation enregistré.");
    }

    @Transactional
    public ResponseEntity<String> reinitialiserMotDePasse(PasswordResetCompletionRequest request) {
        if (request == null || request.tokenHash() == null || !request.tokenHash().matches("[a-f0-9]{64}")
                || !motDePasseValide(request.motDePasse())) {
            return ResponseEntity.badRequest().body("Jeton ou mot de passe invalide.");
        }

        Optional<PasswordResetToken> tokenResult = passwordResetTokenRepo.findByTokenHash(request.tokenHash());
        if (tokenResult.isEmpty()) {
            return ResponseEntity.badRequest().body("Jeton invalide ou expiré.");
        }

        PasswordResetToken resetToken = tokenResult.get();
        if (!resetToken.getExpiresAt().isAfter(LocalDateTime.now())) {
            passwordResetTokenRepo.delete(resetToken);
            return ResponseEntity.badRequest().body("Jeton invalide ou expiré.");
        }

        Optional<User> userResult = userRepo.findByEmail(resetToken.getEmail());
        if (userResult.isEmpty()) {
            passwordResetTokenRepo.delete(resetToken);
            return ResponseEntity.badRequest().body("Jeton invalide ou expiré.");
        }

        User user = userResult.get();
        user.setMotDePasse(passwordEncoder.encode(request.motDePasse()));
        userRepo.save(user);
        passwordResetTokenRepo.delete(resetToken);
        return ResponseEntity.ok("Mot de passe réinitialisé.");
    }

    private boolean motDePasseValide(String motDePasse) {
        return motDePasse != null && motDePasse.length() >= 8
                && motDePasse.getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= 72;
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

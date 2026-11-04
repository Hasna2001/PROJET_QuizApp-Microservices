package sn.smtg.authservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.smtg.authservice.dto.AuthRequest;
import sn.smtg.authservice.dto.AuthResponse;
import sn.smtg.authservice.dto.PasswordResetRequest;
import sn.smtg.authservice.dto.PasswordResetCompletionRequest;
import sn.smtg.authservice.service.AuthService;

@RestController
@RequestMapping("auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        return authService.login(authRequest);
    }

    @PostMapping("mot-de-passe-oublie")
    public ResponseEntity<String> demanderReinitialisation(@RequestBody PasswordResetRequest request) {
        return authService.demanderReinitialisation(request);
    }

    @PostMapping("reinitialiser-mot-de-passe")
    public ResponseEntity<String> reinitialiserMotDePasse(@RequestBody PasswordResetCompletionRequest request) {
        return authService.reinitialiserMotDePasse(request);
    }

    @GetMapping("valider")
    public ResponseEntity<Boolean> validerToken(@RequestParam String token) {
        return authService.validerToken(token);
    }
}

package sn.smtg.authservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.smtg.authservice.dto.AuthRequest;
import sn.smtg.authservice.dto.AuthResponse;
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

    @GetMapping("valider")
    public ResponseEntity<Boolean> validerToken(@RequestParam String token) {
        return authService.validerToken(token);
    }
}

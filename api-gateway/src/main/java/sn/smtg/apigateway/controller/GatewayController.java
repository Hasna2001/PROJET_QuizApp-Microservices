package sn.smtg.apigateway.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class GatewayController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Resource> accueil() {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(new ClassPathResource("static/index.html"));
    }

    @GetMapping("/api")
    public Map<String, Object> informationsApi() {
        return Map.of(
                "service", "QuizApp API Gateway",
                "message", "La Gateway est en ligne. Utilisez les routes API ci-dessous.",
                "endpointsPublics", List.of(
                        "POST /user-service/user/inscrire",
                        "POST /auth-service/auth/login"
                ),
                "documentation", "Consultez le README du projet pour la liste des routes."
        );
    }
}

package sn.smtg.apigateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class GatewayController {

    @GetMapping("/")
    public Map<String, Object> accueil() {
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

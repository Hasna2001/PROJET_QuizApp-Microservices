package sn.smtg.leaderboardservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.smtg.leaderboardservice.dto.ClassementEntry;
import sn.smtg.leaderboardservice.service.LeaderboardService;

import java.util.List;

@RestController
@RequestMapping("leaderboard")
public class LeaderboardController {

    @Autowired
    LeaderboardService leaderboardService;

    @GetMapping("classement")
    public ResponseEntity<List<ClassementEntry>> getClassement() {
        return leaderboardService.getClassement();
    }

    @GetMapping("top/{n}")
    public ResponseEntity<List<ClassementEntry>> getTop(@PathVariable Integer n) {
        return leaderboardService.getTop(n);
    }

    @GetMapping("rang/{utilisateurId}")
    public ResponseEntity<ClassementEntry> getRangUtilisateur(@PathVariable Integer utilisateurId) {
        return leaderboardService.getRangUtilisateur(utilisateurId);
    }
}

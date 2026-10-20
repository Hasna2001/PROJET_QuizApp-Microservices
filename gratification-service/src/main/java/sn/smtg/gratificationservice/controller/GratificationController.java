package sn.smtg.gratificationservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.smtg.gratificationservice.dto.GratificationDTO;
import sn.smtg.gratificationservice.entity.Gratification;
import sn.smtg.gratificationservice.service.GratificationService;

import java.util.List;

@RestController
@RequestMapping("gratification")
public class GratificationController {

    @Autowired
    GratificationService gratificationService;

    @PostMapping("ajouter")
    public ResponseEntity<String> ajouter(@RequestBody GratificationDTO dto) {
        return gratificationService.ajouterGratification(dto);
    }

    @GetMapping("user/{utilisateurId}")
    public ResponseEntity<List<Gratification>> getByUtilisateur(@PathVariable Integer utilisateurId) {
        return gratificationService.getByUtilisateur(utilisateurId);
    }

    @GetMapping("all")
    public ResponseEntity<List<Gratification>> getAll() {
        return gratificationService.getAll();
    }

    @GetMapping("{id}")
    public ResponseEntity<Gratification> getById(@PathVariable Integer id) {
        return gratificationService.getById(id);
    }

    @GetMapping("total/{utilisateurId}")
    public ResponseEntity<Integer> total(@PathVariable Integer utilisateurId) {
        return gratificationService.totalPoints(utilisateurId);
    }
}

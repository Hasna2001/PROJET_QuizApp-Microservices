package sn.smtg.leaderboardservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import sn.smtg.leaderboardservice.dto.ClassementEntry;
import sn.smtg.leaderboardservice.dto.GratificationWrapper;
import sn.smtg.leaderboardservice.dto.UserWrapper;
import sn.smtg.leaderboardservice.feign.GratificationInterface;
import sn.smtg.leaderboardservice.feign.UserInterface;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class LeaderboardService {

    @Autowired
    GratificationInterface gratificationInterface;

    @Autowired
    UserInterface userInterface;

    private List<ClassementEntry> construireClassement() {
        List<GratificationWrapper> gratifications = gratificationInterface.getAllGratifications().getBody();
        List<UserWrapper> users = userInterface.getAllUsers().getBody();
        if (gratifications == null) gratifications = List.of();
        if (users == null) users = List.of();

        Map<Integer, Integer> totalPointsParUser = gratifications.stream()
                .filter(g -> g.getUtilisateurId() != null)
                .collect(Collectors.groupingBy(GratificationWrapper::getUtilisateurId,
                        Collectors.summingInt(g -> g.getPoints() != null ? g.getPoints() : 0)));

        Map<Integer, Long> nombreQuizParUser = gratifications.stream()
                .filter(g -> g.getUtilisateurId() != null)
                .collect(Collectors.groupingBy(GratificationWrapper::getUtilisateurId, Collectors.counting()));

        Map<Integer, UserWrapper> usersParId = users.stream()
                .filter(u -> u.getId() != null)
                .collect(Collectors.toMap(UserWrapper::getId, u -> u, (a, b) -> a));

        List<ClassementEntry> classement = totalPointsParUser.entrySet().stream()
                .filter(entry -> usersParId.containsKey(entry.getKey()))
                .sorted(Map.Entry.<Integer, Integer>comparingByValue(Comparator.reverseOrder()))
                .map(entry -> {
                    UserWrapper user = usersParId.get(entry.getKey());
                    return new ClassementEntry(0, entry.getKey(), user.getNom(), user.getPrenom(),
                            user.getEmail(), entry.getValue(),
                            nombreQuizParUser.getOrDefault(entry.getKey(), 0L).intValue());
                })
                .collect(Collectors.toList());

        // Attribution des rangs, avec gestion des ex-aequo
        IntStream.range(0, classement.size()).forEach(i -> {
            ClassementEntry entry = classement.get(i);
            if (i == 0 || !classement.get(i - 1).getTotalPoints().equals(entry.getTotalPoints())) {
                entry.setRang(i + 1);
            } else {
                entry.setRang(classement.get(i - 1).getRang());
            }
        });
        return classement;
    }

    public ResponseEntity<List<ClassementEntry>> getClassement() {
        return new ResponseEntity<>(construireClassement(), HttpStatus.OK);
    }

    public ResponseEntity<List<ClassementEntry>> getTop(Integer n) {
        List<ClassementEntry> top = construireClassement().stream()
                .limit(Math.max(n, 0))
                .collect(Collectors.toList());
        return new ResponseEntity<>(top, HttpStatus.OK);
    }

    public ResponseEntity<ClassementEntry> getRangUtilisateur(Integer utilisateurId) {
        return construireClassement().stream()
                .filter(e -> e.getUtilisateurId().equals(utilisateurId))
                .findFirst()
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}

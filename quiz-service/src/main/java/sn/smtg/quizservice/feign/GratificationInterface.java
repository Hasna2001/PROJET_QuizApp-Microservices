package sn.smtg.quizservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import sn.smtg.quizservice.dto.GratificationDTO;

@FeignClient(name = "GRATIFICATION-SERVICE")
public interface GratificationInterface {

    @PostMapping("gratification/ajouter")
    ResponseEntity<String> ajouterGratification(@RequestBody GratificationDTO gratificationDTO);
}

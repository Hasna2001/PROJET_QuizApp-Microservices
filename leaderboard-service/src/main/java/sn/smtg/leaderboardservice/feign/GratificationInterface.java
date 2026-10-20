package sn.smtg.leaderboardservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import sn.smtg.leaderboardservice.dto.GratificationWrapper;

import java.util.List;

@FeignClient(name = "GRATIFICATION-SERVICE")
public interface GratificationInterface {

    @GetMapping("gratification/all")
    ResponseEntity<List<GratificationWrapper>> getAllGratifications();
}

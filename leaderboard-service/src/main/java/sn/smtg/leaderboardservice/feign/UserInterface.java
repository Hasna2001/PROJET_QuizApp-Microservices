package sn.smtg.leaderboardservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import sn.smtg.leaderboardservice.dto.UserWrapper;

import java.util.List;

@FeignClient(name = "USER-SERVICE")
public interface UserInterface {

    @GetMapping("user/all")
    ResponseEntity<List<UserWrapper>> getAllUsers();
}

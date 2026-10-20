package sn.smtg.authservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import sn.smtg.authservice.dto.UserWrapper;

@FeignClient(name = "USER-SERVICE")
public interface UserInterface {

    @GetMapping("user/email/{email}")
    ResponseEntity<UserWrapper> getUserByEmail(@PathVariable("email") String email);
}

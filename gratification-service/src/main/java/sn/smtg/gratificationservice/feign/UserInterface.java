package sn.smtg.gratificationservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import sn.smtg.gratificationservice.dto.UserWrapper;

@FeignClient(name = "USER-SERVICE")
public interface UserInterface {

    @GetMapping("user/{id}")
    ResponseEntity<UserWrapper> getUserById(@PathVariable("id") Integer id);
}

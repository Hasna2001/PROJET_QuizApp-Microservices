package sn.smtg.gratificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class GratificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GratificationServiceApplication.class, args);
    }
}

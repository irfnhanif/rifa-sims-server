package io.github.irfnhanif.rifasims;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RifaSimsServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RifaSimsServerApplication.class, args);
    }

}

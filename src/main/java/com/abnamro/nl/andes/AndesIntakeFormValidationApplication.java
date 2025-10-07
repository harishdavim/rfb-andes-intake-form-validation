package com.abnamro.nl.andes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@Slf4j
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class AndesIntakeFormValidationApplication
        extends SpringBootServletInitializer {

    public static void main(String[] args) {
        log.info("inside main method of AndesIntakeFormValidationApplication ");
        SpringApplication.run(com.abnamro.nl.andes.AndesIntakeFormValidationApplication.class, args);
    }


    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(com.abnamro.nl.andes.AndesIntakeFormValidationApplication.class);
    }
}

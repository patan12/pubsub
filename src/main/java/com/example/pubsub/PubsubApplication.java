package com.example.pubsub;

import com.example.pubsub.service.PublisherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@Slf4j
@EnableAsync
public class PubsubApplication implements CommandLineRunner {

  @Autowired
  PublisherService publisherService;

  public static void main(String[] args) {
    SpringApplication.run(PubsubApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    publisherService.publishAll();
    log.info("Triggered publishing");
  }
}

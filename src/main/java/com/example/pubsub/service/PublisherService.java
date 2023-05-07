package com.example.pubsub.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PublisherService {

  @Value("${publishThreads:10}")
  private Integer publishThreads;

  @Autowired
  private MessagePublisherService messagePublisherService;

  @Autowired
  ObjectMapper objectMapper;

  @Value("classpath:large.json")
  private Resource resource;

  @Async
  public void publishAll() {
    fetchAllStagedAndPublish();
    log.info("Done publishing");
  }

  private void fetchAllStagedAndPublish() {

    long start = System.currentTimeMillis();
    List<String> allItems = IntStream.rangeClosed(1, 100000).mapToObj(String::valueOf).collect(
        Collectors.toList());

    AtomicInteger failCounter = new AtomicInteger(0);

    partition(allItems, 1000).forEach(a -> {
      long bs = System.currentTimeMillis();
      fetchAndPublish(a, failCounter);
      log.info("1000 done time[{}]ms", (System.currentTimeMillis()-bs));
    });

    log.info("Publishing Activity completed {} in [{}]ms", allItems.size(),
        System.currentTimeMillis() - start);

  }

  private static <T> List<List<T>> partition(List<T> list, int size) {
    return Stream.iterate(0, i -> i <= list.size(), i -> i + size)
        .map(i -> list.subList(i, Math.min(i + size, list.size())))
        .filter(Predicate.not(List::isEmpty))
        .collect(Collectors.toList());
  }

  private void fetchAndPublish(List<String> list, AtomicInteger failedCounter) {
    int threads = publishThreads < list.size() ? publishThreads : list.size();
    ExecutorService es = Executors.newFixedThreadPool(threads);
    CompletableFuture<?>[] futures = list.stream()
        .map(s -> new PublishRunnable(s, failedCounter))
        .map(task -> CompletableFuture.runAsync(task, es))
        .toArray(CompletableFuture[]::new);
    CompletableFuture.allOf(futures).join();
    es.shutdown();
  }

  public class PublishRunnable implements Runnable {

    private final String id;
    private final AtomicInteger failCounter;

    public PublishRunnable(String id, AtomicInteger failCounter) {
      this.id = id;
      this.failCounter = failCounter;
    }

    @Override
    public void run() {
      try {
        messagePublisherService.publish(
            objectMapper.readValue(resource.getInputStream(), Object.class));
      } catch (Exception exception) {
        log.error("Failed ", exception);
        failCounter.getAndIncrement();
      }
    }
  }
}

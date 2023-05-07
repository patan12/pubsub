/*
 * Copyright (c) 2022, Ford Motor Company and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 */

package com.example.pubsub.service;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * ProducerPublisherService.
 */
@Component
@Slf4j
public class MessagePublisherService {

  @Value("${publish.destination:myProducer-out-0}")
  private String destination;

  @Autowired
  private StreamBridge streamBridge;

  public void publish(Object payload) {
    Message<Object> message = MessageBuilder.withPayload(payload)
        .copyHeaders(Map.of("k1", "v1"))
        .build();
    streamBridge.send(destination, message);
  }

}

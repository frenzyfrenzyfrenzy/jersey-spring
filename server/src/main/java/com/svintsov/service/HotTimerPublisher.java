package com.svintsov.service;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Component
public class HotTimerPublisher implements Publisher<String>, InitializingBean {

    private final Set<Subscriber<? super String>> subscribers = new HashSet<>();

    @Autowired
    private ThreadPoolExecutor requestExecutor;

    @Override
    public void subscribe(Subscriber<? super String> s) {
        log.info("Adding one more subscriber to the timer publisher");
        subscribers.add(s);
    }

    public Flux<String> asFlux() {
        return Flux.from(this);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Creating and starting timer publisher");
        requestExecutor.submit(() -> {
            while (true) {
                String currentTime = LocalDateTime.now().toString();
                subscribers.forEach(stringSubscriber -> stringSubscriber.onNext(currentTime));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}

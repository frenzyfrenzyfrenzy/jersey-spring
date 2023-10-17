package com.svintsov.rest;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class AccumulatingReadListener implements ReadListener {

    private final ServletInputStream inputStream;
    private final byte[] buffer = new byte[1024];
    private final StringBuilder stringBuilder = new StringBuilder();

    private final Set<MonoSink<String>> requestSinks = new HashSet<>();

    @Override
    public void onDataAvailable() throws IOException {
        while (inputStream.isReady()) {
            int actualBytesRead = inputStream.read(buffer);
            String dataReadAsString = new String(buffer, 0, actualBytesRead);
            log.info("Portion of data has bean read: {}", dataReadAsString);
            stringBuilder.append(dataReadAsString);
        }
    }

    @Override
    public void onAllDataRead() throws IOException {
        log.info("Finished reading data. Final data: {}", stringBuilder);
        requestSinks.forEach(stringMonoSink -> stringMonoSink.success(stringBuilder.toString()));
    }

    @Override
    public void onError(Throwable t) {

    }

    public Mono<String> getRequestPromise() {
        log.info("New request listener registered");
        return Mono.create(requestSinks::add);
    }

}

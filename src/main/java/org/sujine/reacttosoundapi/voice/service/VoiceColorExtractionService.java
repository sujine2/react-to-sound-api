package org.sujine.reacttosoundapi.voice.service;

import lombok.Getter;
import org.springframework.stereotype.Service;
import org.sujine.reacttosoundapi.voice.dto.AudioStreamData;
import org.sujine.reacttosoundapi.voice.dto.ResponseRGB;

import javax.annotation.PreDestroy;
import java.util.concurrent.*;

@Service
@Getter
public class VoiceColorExtractionService {
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    public ResponseRGB[] getColorsWithThread(AudioStreamData streamData) throws IllegalArgumentException, ExecutionException, InterruptedException {
        ColorExtraction colorExtraction = new ColorExtraction(streamData);
        Future<ResponseRGB[]> responseRGBs = executorService.submit(colorExtraction);
        return responseRGBs.get();
    }

    @PreDestroy
    public void shutdown() {
        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

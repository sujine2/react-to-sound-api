package org.sujine.reacttosoundapi.voiceColor.service;

import lombok.Getter;
import org.springframework.stereotype.Service;
import org.sujine.reacttosoundapi.voiceColor.dto.RequestAudioStreamData;
import org.sujine.reacttosoundapi.voiceColor.dto.ResponseRGB;

import javax.annotation.PreDestroy;
import java.util.concurrent.*;

@Service
@Getter
public class VoiceColorsExtractionService {
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    public ResponseRGB[] getColorsWithThread(RequestAudioStreamData streamData) throws IllegalArgumentException, ExecutionException, InterruptedException {
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

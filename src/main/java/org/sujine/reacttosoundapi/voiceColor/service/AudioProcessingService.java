package org.sujine.reacttosoundapi.voiceColor.service;

import lombok.Getter;
import org.springframework.stereotype.Service;
import org.sujine.reacttosoundapi.voiceColor.dto.RequestAudioStreamData;
import org.sujine.reacttosoundapi.voiceColor.dto.ResponseRGB;

import javax.annotation.PreDestroy;
import java.util.concurrent.*;

@Service
@Getter
public class AudioProcessingService {
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    public  ResponseRGB[] extractMainVoiceColor(RequestAudioStreamData streamData) throws IllegalArgumentException, ExecutionException, InterruptedException {
        ColorExtractionService colorExtractionService = new ColorExtractionService(streamData);
        Future<ResponseRGB[]> responseRGBs = executorService.submit(colorExtractionService);
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

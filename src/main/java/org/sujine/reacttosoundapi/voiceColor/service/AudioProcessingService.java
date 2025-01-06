package org.sujine.reacttosoundapi.voiceColor.service;

import lombok.Getter;
import org.sujine.reacttosoundapi.voiceColor.dto.RequestAudioStreamData;
import org.sujine.reacttosoundapi.voiceColor.dto.ResponseRGB;

import java.util.ArrayList;
import java.util.concurrent.*;

@Getter
public class AudioProcessingService {
    public ArrayList<ExecutorService> executors = new ArrayList<>();

    public ResponseRGB[] extractMainVoiceColor(RequestAudioStreamData streamData) throws IllegalArgumentException, ExecutionException, InterruptedException {
        ColorExtractionService colorExtractionService = new ColorExtractionService(streamData);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        this.executors.add(executorService);
        Future<ResponseRGB[]> responseRGBs = executorService.submit(colorExtractionService);
        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        return responseRGBs.get();
    }
}

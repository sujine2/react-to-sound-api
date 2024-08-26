package org.sujine.reacttosoundapi.voiceColor.service;

import lombok.Getter;
import org.sujine.reacttosoundapi.voiceColor.domain.Voice;
import org.sujine.reacttosoundapi.voiceColor.dto.RequestAudioStreamData;
import org.sujine.reacttosoundapi.voiceColor.dto.ResponseRGB;
import org.sujine.reacttosoundapi.voiceColor.utils.AudioStreamFormatter;

import java.util.ArrayList;
import java.util.concurrent.*;

@Getter
public class VoiceService {
    public ArrayList<ExecutorService> executors = new ArrayList<>();

    public ResponseRGB[] getMainVoiceColor(RequestAudioStreamData streamData) throws IllegalArgumentException, ExecutionException, InterruptedException {
        VoiceColorTask voiceColorTask = new VoiceColorTask(streamData);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        this.executors.add(executorService);
        Future<ResponseRGB[]> responseRGBs = executorService.submit(voiceColorTask);
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

    public static byte[] getVad(RequestAudioStreamData streamData) throws IllegalArgumentException {
        Voice voice = new Voice(streamData.getRawStream(), streamData.getSampleRate());
        return AudioStreamFormatter.convertDoubleToByteArray(
                voice.getStream(),
                streamData.getSampleSize(),
                streamData.isBigEndian()
        );
    }
}

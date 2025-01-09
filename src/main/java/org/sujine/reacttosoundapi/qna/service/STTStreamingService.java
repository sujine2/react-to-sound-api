package org.sujine.reacttosoundapi.qna.service;

import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;


@Service
@Scope("prototype")
public class STTStreamingService {
    private final ClientStream<StreamingRecognizeRequest> requestObserver;

    @Autowired
    public STTStreamingService(ResponseObserver<StreamingRecognizeResponse> responseObserver) throws Exception {
        SpeechClient speechClient = SpeechClient.create();
        this.requestObserver = speechClient.streamingRecognizeCallable().splitCall(responseObserver);
    }

    public void initialize(int sampleRate) {
        RecognitionConfig recognitionConfig = RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                .setSampleRateHertz(sampleRate)
                .setLanguageCode("ko-KR")
                .build();
        StreamingRecognitionConfig streamingConfig = StreamingRecognitionConfig.newBuilder()
                .setConfig(recognitionConfig)
                .setInterimResults(true)
                .build();
        StreamingRecognizeRequest initialRequest = StreamingRecognizeRequest.newBuilder()
                .setStreamingConfig(streamingConfig)
                .build();

        this.requestObserver.send(initialRequest);
    }

    public void sendAudioData(byte[] audioData, boolean isFinal) {
        if (this.requestObserver != null) {
            StreamingRecognizeRequest request = StreamingRecognizeRequest.newBuilder()
                    .setAudioContent(ByteString.copyFrom(audioData))
                    .build();
            this.requestObserver.send(request);
//            System.out.println("Audio data sent.");
        } else {
            System.err.println("Audio stream is not initialized.");
        }

        if (isFinal) {
            try {
                Thread.sleep(300);
                System.out.println("Closing stream.");
                this.requestObserver.closeSend();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
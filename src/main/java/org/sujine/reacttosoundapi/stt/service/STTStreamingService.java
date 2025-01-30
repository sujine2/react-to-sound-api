package org.sujine.reacttosoundapi.stt.service;

import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;


@Service
@Scope("prototype")
public class STTStreamingService {
    private ClientStream<StreamingRecognizeRequest> requestObserver;
    private final ResponseObserver<StreamingRecognizeResponse>  responseObserver;
    private final SpeechClient speechClient;
    private StreamingRecognizeRequest initialRequest;

    @Autowired
    public STTStreamingService(ResponseObserver<StreamingRecognizeResponse> responseObserver) throws Exception {
        this.speechClient = SpeechClient.create();
        this.requestObserver = speechClient.streamingRecognizeCallable().splitCall(responseObserver);
        this.responseObserver = responseObserver;
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
        this.initialRequest = StreamingRecognizeRequest.newBuilder()
                .setStreamingConfig(streamingConfig)
                .build();

        this.requestObserver.send(this.initialRequest);
    }

    public void sendAudioData(byte[] audioData, boolean isFinal)throws Exception {
        if (this.requestObserver == null) {
            restartStreaming();
        }
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
                this.requestObserver = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void closeStreaming() {
        if (this.requestObserver != null) {
            this.requestObserver.closeSend();
            this.requestObserver = null;
        }

        if (this.responseObserver != null) {
            this.responseObserver.onComplete();
        }
    }

    private void restartStreaming() throws Exception {
        System.out.println("🔄 Restarting gRPC streaming...");
        this.requestObserver = speechClient.streamingRecognizeCallable().splitCall(responseObserver);
        this.requestObserver.send(this.initialRequest);
        System.out.println("✅ New requestObserver created. Streaming restarted.");
    }

    @PreDestroy
     void shutdown() {
        if (this.speechClient != null) {
            this.speechClient.shutdown();
        }
    }

}
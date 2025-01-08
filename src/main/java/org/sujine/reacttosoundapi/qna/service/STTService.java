package org.sujine.reacttosoundapi.qna.service;

import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.StreamingRecognitionConfig;
import com.google.cloud.speech.v1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import com.google.cloud.speech.v1.StreamingRecognitionResult;
import com.google.protobuf.ByteString;

import jakarta.websocket.Session;
import org.springframework.stereotype.Service;
import org.sujine.reacttosoundapi.qna.dto.ResponseText;

public class SpeechToTextService {
    private final ClientStream<StreamingRecognizeRequest> requestObserver;

    public SpeechToTextService(int sampleRate, ResponseObserver<StreamingRecognizeResponse> responseObserver) throws Exception {
        SpeechClient speechClient = SpeechClient.create();
        RecognitionConfig recognitionConfig = RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                .setSampleRateHertz(sampleRate)
                .setLanguageCode("ko-KR")
                .build();

        StreamingRecognitionConfig streamingConfig = StreamingRecognitionConfig.newBuilder()
                .setConfig(recognitionConfig)
                .setInterimResults(true)
                .build();

        requestObserver = speechClient.streamingRecognizeCallable().splitCall(responseObserver);

        // 첫 요청에 StreamingConfig를 보내야 함
        StreamingRecognizeRequest initialRequest = StreamingRecognizeRequest.newBuilder()
                .setStreamingConfig(streamingConfig)
                .build();
        requestObserver.send(initialRequest);
    }

    public void sendAudioData(byte[] audioData, boolean isFinal) {
        if (requestObserver != null) {
            StreamingRecognizeRequest request = StreamingRecognizeRequest.newBuilder()
                    .setAudioContent(ByteString.copyFrom(audioData))
                    .build();
            requestObserver.send(request);
//            System.out.println("Audio data sent.");
        } else {
            System.err.println("Audio stream is not initialized.");
        }

        if (isFinal) {
            try {
                Thread.sleep(300);
                System.out.println("Closing stream.");
                requestObserver.closeSend();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ResponseObserver
    public static class ResponseObserverSend implements ResponseObserver<StreamingRecognizeResponse> {
        private Session session;
        private static final OpenAIService openAIService = new OpenAIService();
        private static final StringBuilder finalTranscript = new StringBuilder();  // final text

        public ResponseObserverSend(Session session) {
            this.session = session;
        }

        @Override
        public void onStart(StreamController controller) {
            System.out.println("Streaming started.");
        }

        @Override
        public void onResponse(StreamingRecognizeResponse response) {
            try {
//                System.out.println("Streaming response received.");
                if (!response.getResultsList().isEmpty()) {
                    StreamingRecognitionResult result = response.getResultsList().get(0);
                    String transcript = result.getAlternativesList().get(0).getTranscript();

                    if (result.getIsFinal()) {
                        finalTranscript.append(transcript);
//                       System.out.println(new ResponseAudioText(transcript, true).toString());
                        this.session.getBasicRemote().sendObject(new ResponseText(transcript, false, true));
                        String answer = openAIService.askGpt(transcript);
                        this.session.getBasicRemote().sendObject(new ResponseText(answer, true, false));
                    } else {
//                        System.out.println(new ResponseAudioText(transcript, false).toString());
                        this.session.getBasicRemote().sendObject(new ResponseText(transcript, false,false));
                    }
                }
            } catch (Exception e) {
                System.err.println("Error during speech response: " + e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void onError(Throwable t) {
            String errorMessage = t.getMessage();
            System.err.println("Error during speech recognition: " + errorMessage);

            t.printStackTrace();
        }

        @Override
        public void onComplete() {
            System.out.println("Speech recognition completed.");
        }

        public String getFinalTranscript() {
            return finalTranscript.toString();
        }
    }

    public static class ResponseObserverNotSend implements ResponseObserver<StreamingRecognizeResponse> {
        private final OpenAIService openAIService = new OpenAIService();
        private static final StringBuilder finalTranscript = new StringBuilder();

        @Override
        public void onStart(StreamController controller) {
            System.out.println("Streaming started.");
        }

        @Override
        public void onResponse(StreamingRecognizeResponse response) {
            try {
                if (!response.getResultsList().isEmpty()) {
                    StreamingRecognitionResult result = response.getResultsList().get(0);
                    String transcript = result.getAlternativesList().get(0).getTranscript();

                    if (result.getIsFinal()) {
                        finalTranscript.append(transcript);
                        System.out.println(new ResponseText(transcript, true,true).toString());
                        String answer = openAIService.askGpt(transcript);
                        System.out.println("answer:" + new ResponseText(answer, true, false));
                    } else {
                        System.out.println(new ResponseText(transcript, true,false).toString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(Throwable t) {
            // 에러 발생 시 호출됩니다.
            String errorMessage = t.getMessage();
            System.err.println("Error during speech recognition: " + errorMessage);

            // 타임아웃 오류인지 확인
            if (errorMessage != null && errorMessage.contains("OUT_OF_RANGE: Audio Timeout Error")) {
                // 타임아웃 오류가 발생하면 startSilenceTimer를 호출
//                startSilenceTimer(this.clientStream);
            }
            t.printStackTrace();
        }

        @Override
        public void onComplete() {
            System.out.println("Speech recognition completed.");
        }
    }

}

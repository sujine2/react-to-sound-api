package org.sujine.reacttosoundapi.speechToText.service;

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
import org.sujine.reacttosoundapi.speechToText.dto.ResponseAudioText;

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

    // 음성 데이터를 전송하는 메서드
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

    // 응답 처리 및 서버로부터의 응답을 받는 ResponseObserver
    public static class ResponseObserverSend implements ResponseObserver<StreamingRecognizeResponse> {
        private Session session;
        private OpenAIService openAIService = new OpenAIService();
        private StringBuilder finalTranscript = new StringBuilder();  // 최종 텍스트를 저장할 버퍼

        public ResponseObserverSend(Session session) {
            this.session = session;
        }

        @Override
        public void onStart(StreamController controller) {
            // 스트림이 시작될 때 호출
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
//                        System.out.println(new ResponseAudioText(transcript, true).toString());
                        this.session.getBasicRemote().sendObject(new ResponseAudioText(transcript, true));
                        String answer = openAIService.askGpt(transcript);
                        this.session.getBasicRemote().sendText(answer);
                    } else {
                        // 중간 결과일 경우
//                        System.out.println(new ResponseAudioText(transcript, false).toString());
                        this.session.getBasicRemote().sendObject(new ResponseAudioText(transcript, false));
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

        // 최종 텍스트 반환 메서드
        public String getFinalTranscript() {
            return finalTranscript.toString();
        }
    }

    public static class ResponseObserverNotSend implements ResponseObserver<StreamingRecognizeResponse> {
        private OpenAIService openAIService = new OpenAIService();
        private StringBuilder finalTranscript = new StringBuilder();  // 최종 텍스트를 저장할 버퍼

        @Override
        public void onStart(StreamController controller) {
            // 스트림이 시작될 때 호출됩니다. 초기화 작업을 이곳에 작성할 수 있습니다.
            System.out.println("Streaming started.");
        }

        @Override
        public void onResponse(StreamingRecognizeResponse response) {
            // 응답이 올 때마다 호출됩니다.
            try {
                if (!response.getResultsList().isEmpty()) {
                    StreamingRecognitionResult result = response.getResultsList().get(0);
                    String transcript = result.getAlternativesList().get(0).getTranscript();

                    if (result.getIsFinal()) {
                        finalTranscript.append(transcript);
                        System.out.println(new ResponseAudioText(transcript, true).toString());
                        String answer = openAIService.askGpt(transcript);
                        System.out.println("answer:" + answer);
                    } else {
                        System.out.println(new ResponseAudioText(transcript, false).toString());
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

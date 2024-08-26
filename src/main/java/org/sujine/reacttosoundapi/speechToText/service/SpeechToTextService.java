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
import org.sujine.reacttosoundapi.speechToText.dto.RequestAudioStreamData;
import org.sujine.reacttosoundapi.speechToText.dto.ResponseAudioText;

import java.util.Timer;
import java.util.TimerTask;

public class SpeechToTextService {
    private Timer silenceTimer;
    private final int SILENCE_THRESHOLD = 5000;  // 5초 동안 음성이 없으면 변환 중단

    public ClientStream<StreamingRecognizeRequest> streamRecognize(RequestAudioStreamData streamData, ResponseObserver<StreamingRecognizeResponse> responseObserver) throws Exception {
        try (SpeechClient speechClient = SpeechClient.create()) {

            // 음성 인식 요청 설정
            RecognitionConfig recognitionConfig = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                    .setSampleRateHertz((int)streamData.getSampleRate())
                    .setLanguageCode("ko-KR")
                    .build();
            // 실시간 음성 인식 설정 (중간 결과 포함)
            StreamingRecognitionConfig streamingConfig = StreamingRecognitionConfig.newBuilder()
                    .setConfig(recognitionConfig)
                    .setInterimResults(true)
                    .build();

            // 음성 인식 스트리밍 요청 초기화
            ClientStream<StreamingRecognizeRequest> clientStream = speechClient.streamingRecognizeCallable().splitCall(responseObserver);

            StreamingRecognizeRequest initialRequest = StreamingRecognizeRequest.newBuilder()
                    .setStreamingConfig(streamingConfig)
                    .build();
            clientStream.send(initialRequest);  // 초기 설정 요청 전송

            // 음성 데이터를 Google Cloud로 전송
            ByteString audioBytes = ByteString.copyFrom(streamData.getRawStream());
            if (audioBytes.isEmpty()) {
                System.err.println("Audio stream data is empty.");
            } else {
                StreamingRecognizeRequest request = StreamingRecognizeRequest.newBuilder()
                        .setAudioContent(audioBytes)
                        .build();
                clientStream.send(request);  // 음성 데이터 요청 전송
            }
            return clientStream;
        }
    }

    public static class ResponseObserverSend implements ResponseObserver<StreamingRecognizeResponse>  {
        private final Session session;
        private StringBuilder finalTranscript = new StringBuilder();  // 최종 텍스트를 저장할 버퍼

        public ResponseObserverSend(Session session) {
            this.session = session;
        }

        @Override
        public void onStart(StreamController controller) {
            // 스트림이 시작될 때 호출됩니다. 초기화 작업을 이곳에 작성할 수 있습니다.
            System.out.println("Streaming started.");
        }

        @Override
        public void onResponse(StreamingRecognizeResponse response) {
            try {
                if (!response.getResultsList().isEmpty()) {
                    StreamingRecognitionResult result = response.getResultsList().get(0);
                    String transcript = result.getAlternativesList().get(0).getTranscript();

                    if (result.getIsFinal()) {
                        // 최종 텍스트일 경우, 버퍼에 저장
                        finalTranscript.append(transcript);

                        session.getBasicRemote().sendObject(new ResponseAudioText(transcript, true));
                    } else {
                        // 중간 결과일 경우
                        session.getBasicRemote().sendObject(new ResponseAudioText(transcript, false));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(Throwable t) {
            // 에러 발생 시 호출됩니다.
            System.err.println("Error during speech recognition: " + t.getMessage());
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
            System.err.println("Error during speech recognition: " + t.getMessage());
            t.printStackTrace();
        }

        @Override
        public void onComplete() {
            System.out.println("Speech recognition completed.");
        }
    }

    // n초 동안 텍스트 변환이 없으면 변환을 중단하고 최종 결과를 반환
    public void startSilenceTimer(Session session, ClientStream<StreamingRecognizeRequest> clientStream) {
        silenceTimer = new Timer();
        silenceTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    // 타이머가 만료되면 세션 종료 및 최종 텍스트 반환
                    clientStream.closeSend();
                    session.getBasicRemote().sendText("변환 종료: 음성 감지 안 됨");
                    session.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, SILENCE_THRESHOLD);
    }

    public void startSilenceTimer(ClientStream<StreamingRecognizeRequest> clientStream) {
        silenceTimer = new Timer();
        silenceTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    // 타이머가 만료되면 세션 종료 및 최종 텍스트 반환
                    clientStream.closeSend();
                    System.out.println("변환 종료: 음성 감지 안 됨");
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        }, SILENCE_THRESHOLD);
    }
}

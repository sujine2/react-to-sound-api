package org.sujine.reacttosoundapi.qna.service;

import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import jakarta.websocket.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.sujine.reacttosoundapi.qna.dto.ResponseText;;


public class STTService {
    private final ClientStream<StreamingRecognizeRequest> requestObserver;

    public STTService(int sampleRate, ResponseObserver<StreamingRecognizeResponse> responseObserver) throws Exception {
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

        this.requestObserver = speechClient.streamingRecognizeCallable().splitCall(responseObserver);
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

    public static class STTResponseObserver implements ResponseObserver<StreamingRecognizeResponse> {
        private Session session;
        private static final OpenAIService openAIService = new OpenAIService();
        private static final StringBuilder finalTranscript =  new StringBuilder();;  // final text

        public STTResponseObserver(Session session){
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
                        // question
                        this.finalTranscript.append(transcript);
                        this.session.getBasicRemote().sendObject(new ResponseText(transcript, false, true));
                        // answer
                        String answer = this.openAIService.askGpt(transcript);
                        this.session.getBasicRemote().sendObject(new ResponseText(answer, true, false));
                    } else {
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


}
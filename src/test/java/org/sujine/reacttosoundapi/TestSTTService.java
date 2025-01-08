package org.sujine.reacttosoundapi;

import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1.StreamingRecognitionResult;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import org.sujine.reacttosoundapi.qna.dto.ResponseText;
import org.sujine.reacttosoundapi.qna.service.OpenAIService;
import org.sujine.reacttosoundapi.qna.service.STTService;

public class TestSTTService extends STTService{
    public TestSTTService(int sampleRate, ResponseObserver<StreamingRecognizeResponse> responseObserver) throws Exception{
        super(sampleRate, responseObserver);
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
            String errorMessage = t.getMessage();
            System.err.println("Error during speech recognition: " + errorMessage);

            if (errorMessage != null && errorMessage.contains("OUT_OF_RANGE: Audio Timeout Error")) {
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

package org.sujine.reacttosoundapi.qna.controller.formatter;

import jakarta.json.Json;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import org.sujine.reacttosoundapi.qna.dto.Answer;

public class ResponseAudioTextEncoder implements Encoder.Text<Answer> {
    @Override
    public String encode(Answer answer) throws EncodeException {
        return Json.createObjectBuilder()
                .add("result", answer.getResult())
                .add("response", answer.isResponse())
                .add("bigEndian", answer.isFinal())
                .build()
                .toString();
    }
}

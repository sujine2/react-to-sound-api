package org.sujine.reacttosoundapi.speechToText.controller.formatter;

import jakarta.json.Json;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import org.sujine.reacttosoundapi.speechToText.dto.ResponseText;

public class ResponseAudioTextEncoder implements Encoder.Text<ResponseText> {
    @Override
    public String encode(ResponseText responseText) throws EncodeException {
        return Json.createObjectBuilder()
                .add("result", responseText.getResult())
                .add("response", responseText.isResponse())
                .add("bigEndian", responseText.isFinal())
                .build()
                .toString();
    }
}

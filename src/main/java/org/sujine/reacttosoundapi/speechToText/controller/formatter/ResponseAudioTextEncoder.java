package org.sujine.reacttosoundapi.speechToText.controller.formatter;

import jakarta.json.Json;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import org.sujine.reacttosoundapi.speechToText.dto.ResponseAudioText;

public class ResponseAudioTextEncoder implements Encoder.Text<ResponseAudioText> {
    @Override
    public String encode(ResponseAudioText responseAudioText) throws EncodeException {
        return Json.createObjectBuilder()
                .add("result", responseAudioText.getResult())
                .add("bigEndian", responseAudioText.isFinal())
                .toString();
    }
}

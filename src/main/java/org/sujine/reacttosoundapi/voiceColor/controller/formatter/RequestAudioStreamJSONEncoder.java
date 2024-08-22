package org.sujine.reacttosoundapi.voiceColor.controller.formatter;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import org.sujine.reacttosoundapi.voiceColor.dto.RequestAudioStreamData;


public class RequestAudioStreamJSONEncoder implements Encoder.Text<RequestAudioStreamData> {
    @Override
    public String encode(RequestAudioStreamData audioStreamData) throws EncodeException {
        JsonArrayBuilder rawStreamBuilder = Json.createArrayBuilder();
        for (double value : audioStreamData.getRawStream()) {
            rawStreamBuilder.add(value);
        }

        return Json.createObjectBuilder()
                .add("rawStream", rawStreamBuilder)
                .add("sampleRate", audioStreamData.getSampleRate())
                .add("sampleSize", audioStreamData.getSampleSize())
                .add("channel", audioStreamData.getChannel())
                .add("bigEndian", audioStreamData.isBigEndian())
                .toString();
    }
}

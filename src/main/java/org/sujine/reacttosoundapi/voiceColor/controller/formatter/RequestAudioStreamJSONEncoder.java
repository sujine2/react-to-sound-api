package org.sujine.reacttosoundapi.voiceColor.controller.formatter;

import jakarta.json.Json;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import org.sujine.reacttosoundapi.voiceColor.dto.RequestAudioStreamData;


public class RequestAudioStreamJSONEncoder implements Encoder.Text<RequestAudioStreamData> {
    @Override
    public String encode(RequestAudioStreamData audioStreamData) throws EncodeException {
        return Json.createObjectBuilder()
                .add("rawStream", new String(audioStreamData.getRawStream()))
                .add("sampleRate", audioStreamData.getSampleRate())
                .add("sampleSize", audioStreamData.getSampleSize())
                .add("channel", audioStreamData.getChannel())
                .add("bigEndian", audioStreamData.isBigEndian())
                .toString();
    }
}

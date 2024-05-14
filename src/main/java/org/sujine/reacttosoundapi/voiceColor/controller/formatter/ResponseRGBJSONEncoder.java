package org.sujine.reacttosoundapi.voiceColor.controller;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import org.sujine.reacttosoundapi.voiceColor.dto.ResponseRGB;

import java.util.List;

public class ResponseRGBJSONEncoder implements Encoder.Text<List<ResponseRGB>> {
    @Override
    public String encode(List<ResponseRGB> ResponseRGBObject) throws EncodeException {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (ResponseRGB responseRGB : ResponseRGBObject) {
            arrayBuilder.add(Json.createObjectBuilder()
                    .add("red", responseRGB.getRed())
                    .add("green", responseRGB.getGreen())
                    .add("blue", responseRGB.getBlue())
            );
        }
        return arrayBuilder.build().toString();
    }
}

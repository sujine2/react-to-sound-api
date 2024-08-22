package org.sujine.reacttosoundapi.voiceColor.controller.formatter;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import jakarta.websocket.EndpointConfig;
import org.sujine.reacttosoundapi.voiceColor.dto.ResponseRGB;

import java.util.List;

public class  ResponseRGBJSONEncoder implements Encoder.Text<ResponseRGB[]> {
    @Override
    public String encode(ResponseRGB[] responseRGBArray) throws EncodeException {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (ResponseRGB responseRGB : responseRGBArray) {
            arrayBuilder.add(Json.createObjectBuilder()
                    .add("red", responseRGB.getRed())
                    .add("green", responseRGB.getGreen())
                    .add("blue", responseRGB.getBlue())
                    .add("magnitude", responseRGB.getMagnitude())
            );
        }
        return arrayBuilder.build().toString();
    }
    @Override
    public void init(EndpointConfig ec) {
        System.out.println("Initializing message encoder");
    }

    @Override
    public void destroy() {
        System.out.println("Destroying encoder...");
    }
}

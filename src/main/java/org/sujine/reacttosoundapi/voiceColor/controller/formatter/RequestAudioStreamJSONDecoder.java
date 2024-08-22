package org.sujine.reacttosoundapi.voiceColor.controller.formatter;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.websocket.Decoder;
import org.sujine.reacttosoundapi.voiceColor.dto.RequestAudioStreamData;

import java.io.StringReader;
import java.util.Base64;

public class RequestAudioStreamJSONDecoder implements Decoder.Text<RequestAudioStreamData> {
    @Override
    public RequestAudioStreamData decode(String jsonObjectMsg) {
        JsonObject jsonObject = Json.createReader(new StringReader(jsonObjectMsg)).readObject();
        JsonArray rawStreamJsonArray = jsonObject.getJsonArray("rawStream");

        double[] rawStream = new double[rawStreamJsonArray.size()];
        for (int i = 0; i < rawStreamJsonArray.size(); i++) {
            rawStream[i] = rawStreamJsonArray.getJsonNumber(i).doubleValue();
        }

        return new RequestAudioStreamData(
                rawStream,
                (float) jsonObject.getJsonNumber("sampleRate").doubleValue(),
                jsonObject.getInt("sampleSize"),
                jsonObject.getInt("channel"),
                jsonObject.getBoolean("bigEndian")
        );
    }

    @Override
    public boolean willDecode(String jsonObjectMsg) {
        try {
            Json.createReader(new StringReader(jsonObjectMsg)).readObject();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

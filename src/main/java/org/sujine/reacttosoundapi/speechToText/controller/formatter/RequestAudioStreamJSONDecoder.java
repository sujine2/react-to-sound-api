package org.sujine.reacttosoundapi.speechToText.controller.formatter;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.websocket.Decoder;
import org.sujine.reacttosoundapi.speechToText.dto.RequestAudioStreamData;

import java.io.StringReader;

public class RequestAudioStreamJSONDecoder implements Decoder.Text<RequestAudioStreamData> {
    @Override
    public RequestAudioStreamData decode(String jsonObjectMsg) {
        JsonObject jsonObject = Json.createReader(new StringReader(jsonObjectMsg)).readObject();
        JsonArray rawStreamJsonArray = jsonObject.getJsonArray("rawStream");

        byte[] rawStream = new byte[rawStreamJsonArray.size()];
        for (int i = 0; i < rawStreamJsonArray.size(); i++) {
            rawStream[i] = (byte) rawStreamJsonArray.getInt(i);
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

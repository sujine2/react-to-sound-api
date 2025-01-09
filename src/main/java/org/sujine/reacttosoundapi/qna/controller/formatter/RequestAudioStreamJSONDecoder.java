package org.sujine.reacttosoundapi.qna.controller.formatter;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.websocket.Decoder;
import org.sujine.reacttosoundapi.qna.dto.QuestionAudioStream;

import java.io.StringReader;

public class RequestAudioStreamJSONDecoder implements Decoder.Text<QuestionAudioStream> {
    @Override
    public QuestionAudioStream decode(String jsonObjectMsg) {
        JsonObject jsonObject = Json.createReader(new StringReader(jsonObjectMsg)).readObject();
        JsonArray rawStreamJsonArray = jsonObject.getJsonArray("rawStream");

        int arraySize = rawStreamJsonArray.size();
        byte[] byteArray = new byte[arraySize];

        for (int i = 0; i < arraySize; i++) {
            int value = rawStreamJsonArray.getInt(i);  // JsonArray의 값을 int로 가져옴 (0~255)
            byteArray[i] = (byte) value;  // int 값을 byte로 캐스팅 (0~127은 그대로, 128~255는 음수로 변환)
        }
        return new QuestionAudioStream(
                byteArray,
                (float) jsonObject.getJsonNumber("sampleRate").doubleValue(),
                jsonObject.getInt("sampleSize"),
                jsonObject.getInt("channel"),
                jsonObject.getBoolean("bigEndian"),
                jsonObject.getBoolean("isFinal")
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

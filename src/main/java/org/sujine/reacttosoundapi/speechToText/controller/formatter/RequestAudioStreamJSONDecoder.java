package org.sujine.reacttosoundapi.speechToText.controller.formatter;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.websocket.Decoder;
import org.sujine.reacttosoundapi.speechToText.dto.RequestAudioStreamData;

import java.io.StringReader;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class RequestAudioStreamJSONDecoder implements Decoder.Text<RequestAudioStreamData> {
    @Override
    public RequestAudioStreamData decode(String jsonObjectMsg) {
        JsonObject jsonObject = Json.createReader(new StringReader(jsonObjectMsg)).readObject();
        JsonArray rawStreamJsonArray = jsonObject.getJsonArray("rawStream");

//        System.out.println(jsonObjectMsg);
        int arraySize = rawStreamJsonArray.size();
        byte[] byteArray = new byte[arraySize];

        // JsonArray의 각 요소를 byte로 변환
        for (int i = 0; i < arraySize; i++) {
            int value = rawStreamJsonArray.getInt(i);  // JsonArray의 값을 int로 가져옴 (0~255)
            byteArray[i] = (byte) value;  // int 값을 byte로 캐스팅 (0~127은 그대로, 128~255는 음수로 변환)
        }
//        System.out.println(Arrays.toString(rawStream));
//        double[] rawStream = new double[rawStreamJsonArray.size()];
//        for (int i = 0; i < rawStreamJsonArray.size(); i++) {
//            rawStream[i] = rawStreamJsonArray.getJsonNumber(i).doubleValue();
//        }

        return new RequestAudioStreamData(
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

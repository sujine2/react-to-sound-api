package org.sujine.reacttosoundapi.voiceColor.controller.formatter;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.websocket.Decoder;
import org.sujine.reacttosoundapi.voiceColor.dto.ResponseRGB;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class ResponseRGBJOSNDecoder implements Decoder.Text<List<ResponseRGB>> {
    @Override
    public List<ResponseRGB> decode(String jsonObjectMsg) {
        JsonArray jsonArray = Json.createReader(new StringReader(jsonObjectMsg)).readArray();
        List<ResponseRGB> RGBList = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.getJsonObject(i);
            RGBList.add(new ResponseRGB(
                    jsonObject.getInt("red"),
                    jsonObject.getInt("green"),
                    jsonObject.getInt("blue")
            ));
        }
        return RGBList;
    }

    @Override
    public boolean willDecode(String jsonObjectMsg) {
        try {
            Json.createReader(new StringReader(jsonObjectMsg)).readArray();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}

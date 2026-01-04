package com.example.fridgeapp;

import okhttp3.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;

public class ChatGPTClient {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final String apiKey;
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    public ChatGPTClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public interface ChatCallback {
        void onSuccess(String reply);
        void onError(String error);
    }

    public void sendMessage(String message, ChatCallback callback) {

        JsonObject messageObj = new JsonObject();
        messageObj.addProperty("role", "user");
        messageObj.addProperty("content", message);

        JsonObject bodyObj = new JsonObject();
        bodyObj.addProperty("model", "gpt-4.1-mini"); // cheap & fast
        bodyObj.add("messages", gson.toJsonTree(new JsonObject[]{ messageObj }));

        RequestBody requestBody = RequestBody.create(
                bodyObj.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(API_URL)
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("HTTP Error: " + response.code());
                    return;
                }

                String json = response.body().string();
                JsonObject jsonObj = gson.fromJson(json, JsonObject.class);

                String reply = jsonObj
                        .getAsJsonArray("choices")
                        .get(0)
                        .getAsJsonObject()
                        .getAsJsonObject("message")
                        .get("content")
                        .getAsString();

                callback.onSuccess(reply);
            }
        });
    }
}
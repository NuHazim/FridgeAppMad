package com.example.fridgeapp.client;

import okhttp3.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ChatGPTClient {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final String apiKey;
    private final OkHttpClient client;
    private final Gson gson = new Gson();

    public ChatGPTClient(String apiKey) {
        this.apiKey = apiKey;
        // Increase timeout to 60 seconds
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public interface ChatCallback {
        void onSuccess(String reply);
        void onError(String error);
    }

    public void sendMessage(String message, ChatCallback callback) {

        JsonObject messageObj = new JsonObject();
        messageObj.addProperty("role", "user");
        messageObj.addProperty("content", message);

        JsonArray messagesArray = new JsonArray();
        messagesArray.add(messageObj);

        JsonObject bodyObj = new JsonObject();
        bodyObj.addProperty("model", "gpt-4o-mini"); // Changed to correct model name
        bodyObj.add("messages", messagesArray);
        bodyObj.addProperty("max_tokens", 4000); // Increased for longer responses

        RequestBody requestBody = RequestBody.create(
                bodyObj.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(API_URL)
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("HTTP Error: " + response.code() + " - " + response.message());
                    return;
                }

                String json = response.body().string();

                try {
                    JsonObject jsonObj = gson.fromJson(json, JsonObject.class);

                    String reply = jsonObj
                            .getAsJsonArray("choices")
                            .get(0)
                            .getAsJsonObject()
                            .getAsJsonObject("message")
                            .get("content")
                            .getAsString();

                    callback.onSuccess(reply);
                } catch (Exception e) {
                    callback.onError("Parse error: " + e.getMessage());
                }
            }
        });
    }
}
package com.example.fridgeapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class recipesFragment extends Fragment {
    EditText input;
    TextView output;
    Button send;

    ChatGPTClient chatGPT;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.recipesfrag, container, false);

        input = view.findViewById(R.id.inputText);
        output = view.findViewById(R.id.outputText);
        send = view.findViewById(R.id.sendBtn);

        chatGPT = new ChatGPTClient("");

        send.setOnClickListener(v -> {
            String userMsg = input.getText().toString();

            chatGPT.sendMessage(userMsg, new ChatGPTClient.ChatCallback() {
                @Override
                public void onSuccess(String reply) {
                    requireActivity().runOnUiThread(() ->
                            output.setText(reply)
                    );
                }

                @Override
                public void onError(String error) {
                    requireActivity().runOnUiThread(() ->
                            output.setText("Error: " + error)
                    );
                }
            });
        });

        return view;
    }
}
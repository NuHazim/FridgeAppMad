package com.example.fridgeapp.inventory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fridgeapp.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AddItemBottomSheet extends BottomSheetDialogFragment {

    public interface AddItemListener {
        void onScanSelected();
        void onManualSelected();
    }

    private AddItemListener listener;

    public void setListener(AddItemListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bottom_sheet_add_item, container, false);

        view.findViewById(R.id.optionScan).setOnClickListener(v -> {
            if (listener != null) listener.onScanSelected();
            dismiss();
        });

        view.findViewById(R.id.optionManual).setOnClickListener(v -> {
            if (listener != null) listener.onManualSelected();
            dismiss();
        });

        return view;
    }
}


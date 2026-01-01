package com.example.fridgeapp.inventory;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.fridgeapp.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ConfirmItemDialogFragment extends DialogFragment {

    public interface OnItemConfirmedListener {
        void onItemConfirmed(String name, String category, String expiry, int quantity, String unit);
    }

    private OnItemConfirmedListener listener;

    public void setListener(OnItemConfirmedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_confirm_item, null);

        EditText etName = view.findViewById(R.id.etName);
        EditText etQuantity = view.findViewById(R.id.etQuantity);
        Spinner spinnerCategory = view.findViewById(R.id.spinnerCategory);
        Spinner spinnerExpiry = view.findViewById(R.id.spinnerExpiry);
        Spinner spinnerUnit = view.findViewById(R.id.spinnerUnit);


        // Receive scanned data
        Bundle args = getArguments();
        if (args != null) {
            etName.setText(args.getString("PRODUCT_NAME", ""));
        }

        return new AlertDialog.Builder(requireActivity())
                .setTitle("Confirm Item")
                .setView(view)
                .setPositiveButton("Save", (dialog, which) -> {

                    String expirySelection =
                            spinnerExpiry.getSelectedItem().toString();

                    String expiryDate =
                            calculateExpiryFromSelection(expirySelection);

                    String unit = spinnerUnit.getSelectedItem().toString();

                    if (listener != null) {
                        listener.onItemConfirmed(
                                etName.getText().toString(),
                                spinnerCategory.getSelectedItem().toString(),
                                expiryDate,
                                Integer.parseInt(etQuantity.getText().toString()),
                                unit
                        );
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
    }

    private String calculateExpiryFromSelection(String selection) {
        int days = 7; // default

        if (selection.contains("3")) days = 3;
        else if (selection.contains("7")) days = 7;
        else if (selection.contains("14")) days = 14;
        else if (selection.contains("30")) days = 30;
        else if (selection.contains("90")) days = 90;

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, days);

        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(cal.getTime());
    }
}


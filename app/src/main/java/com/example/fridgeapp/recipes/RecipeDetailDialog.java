package com.example.fridgeapp.recipes;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.fridgeapp.R;

import java.util.Map;

public class RecipeDetailDialog extends DialogFragment {

    private Recipe recipe;
    private OnDoneCookingListener listener;

    public interface OnDoneCookingListener {
        void onDoneCooking(Recipe recipe);
    }

    public static RecipeDetailDialog newInstance(Recipe recipe) {
        RecipeDetailDialog dialog = new RecipeDetailDialog();
        Bundle args = new Bundle();
        args.putString("docId", recipe.getDocId());
        args.putString("name", recipe.getName());
        args.putString("difficulty", recipe.getDifficulty());
        args.putString("time", recipe.getEstimatedTime());
        args.putString("calories", recipe.getCalories());
        dialog.setArguments(args);
        dialog.recipe = recipe;
        return dialog;
    }

    public void setOnDoneCookingListener(OnDoneCookingListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_recipe_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvDetailRecipeName = view.findViewById(R.id.tvDetailRecipeName);
        TextView tvDetailDifficulty = view.findViewById(R.id.tvDetailDifficulty);
        TextView tvDetailTime = view.findViewById(R.id.tvDetailTime);
        TextView tvDetailCalories = view.findViewById(R.id.tvDetailCalories);
        LinearLayout ingredientsContainer = view.findViewById(R.id.ingredientsContainer);
        LinearLayout missingIngredientsSection = view.findViewById(R.id.missingIngredientsSection);
        LinearLayout missingIngredientsContainer = view.findViewById(R.id.missingIngredientsContainer);
        LinearLayout stepsContainer = view.findViewById(R.id.stepsContainer);
        Button btnDoneCooking = view.findViewById(R.id.btnDoneCooking);

        // Set recipe details
        tvDetailRecipeName.setText(recipe.getName());
        tvDetailDifficulty.setText(recipe.getDifficulty());
        tvDetailTime.setText(recipe.getEstimatedTime());
        tvDetailCalories.setText(recipe.getCalories());

        // Add ingredients
        if (recipe.getIngredients() != null) {
            for (Map.Entry<String, String> entry : recipe.getIngredients().entrySet()) {
                TextView ingredientView = new TextView(getContext());
                ingredientView.setText("• " + entry.getKey() + ": " + entry.getValue());
                ingredientView.setTextSize(14);
                ingredientView.setTextColor(0xFF4CAF50); // Green - available
                ingredientView.setPadding(0, 8, 0, 8);
                ingredientsContainer.addView(ingredientView);
            }
        }

        // Add missing ingredients if any
        if (recipe.getMissingIngredients() != null && !recipe.getMissingIngredients().isEmpty()) {
            missingIngredientsSection.setVisibility(View.VISIBLE);
            for (String ingredient : recipe.getMissingIngredients()) {
                TextView missingView = new TextView(getContext());
                missingView.setText("• " + ingredient);
                missingView.setTextSize(14);
                missingView.setTextColor(0xFFFF5722); // Red - missing
                missingView.setPadding(0, 8, 0, 8);
                missingIngredientsContainer.addView(missingView);
            }
        }

        // Add steps
        if (recipe.getSteps() != null) {
            for (int i = 0; i < recipe.getSteps().size(); i++) {
                TextView stepView = new TextView(getContext());
                stepView.setText((i + 1) + ". " + recipe.getSteps().get(i));
                stepView.setTextSize(14);
                stepView.setTextColor(0xFF333333);
                stepView.setPadding(0, 12, 0, 12);
                stepsContainer.addView(stepView);
            }
        }

        // Done cooking button
        btnDoneCooking.setOnClickListener(v -> {
            if (recipe.hasAllIngredients()) {
                if (listener != null) {
                    listener.onDoneCooking(recipe);
                }
                dismiss();
            } else {
                Toast.makeText(getContext(),
                        "You don't have all ingredients needed for this recipe!",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}
package com.example.fridgeapp.recipes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fridgeapp.R;

import java.util.ArrayList;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private ArrayList<Recipe> recipeList;
    private OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
        void onFavoriteClick(Recipe recipe, int position);
    }

    public RecipeAdapter(ArrayList<Recipe> recipeList) {
        this.recipeList = recipeList;
    }

    public void setOnRecipeClickListener(OnRecipeClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);

        holder.tvRecipeName.setText(recipe.getName());
        holder.tvDifficulty.setText(recipe.getDifficulty());
        holder.tvTime.setText(recipe.getEstimatedTime());

        // Set favorite icon
        if (recipe.isFavorite()) {
            holder.btnFavorite.setImageResource(android.R.drawable.star_big_on);
        } else {
            holder.btnFavorite.setImageResource(android.R.drawable.star_big_off);
        }

        // Show ingredient status
        if (recipe.hasAllIngredients()) {
            holder.tvIngredientStatus.setVisibility(View.VISIBLE);
            holder.tvIngredientStatus.setText("✓ All ingredients available");
            holder.tvIngredientStatus.setTextColor(0xFF4CAF50); // Green
        } else if (recipe.getMissingIngredients() != null && !recipe.getMissingIngredients().isEmpty()) {
            holder.tvIngredientStatus.setVisibility(View.VISIBLE);
            holder.tvIngredientStatus.setText("⚠ Missing some ingredients");
            holder.tvIngredientStatus.setTextColor(0xFFFF9800); // Orange
        } else {
            holder.tvIngredientStatus.setVisibility(View.GONE);
        }

        // Click listeners
        holder.cardRecipe.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecipeClick(recipe);
            }
        });

        holder.btnFavorite.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFavoriteClick(recipe, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        CardView cardRecipe;
        TextView tvRecipeName, tvDifficulty, tvTime, tvIngredientStatus;
        ImageButton btnFavorite;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRecipe = itemView.findViewById(R.id.cardRecipe);
            tvRecipeName = itemView.findViewById(R.id.tvRecipeName);
            tvDifficulty = itemView.findViewById(R.id.tvDifficulty);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvIngredientStatus = itemView.findViewById(R.id.tvIngredientStatus);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}
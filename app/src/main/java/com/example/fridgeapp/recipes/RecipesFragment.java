package com.example.fridgeapp.recipes;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fridgeapp.ChatGPTClient;
import com.example.fridgeapp.R;
import com.example.fridgeapp.inventory.FridgeItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipesFragment extends Fragment {

    private static final String TAG = "RecipesFragment";

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ChatGPTClient chatGPTClient;
    private ListenerRegistration recipeListener;

    private Button btnGenerateRecipes;
    private TextView tabAllRecipes, tabFavorites, tvLoadingText;
    private RecyclerView recyclerViewRecipes;
    private ProgressBar loadingIndicator;
    private LinearLayout emptyStateLayout;

    private RecipeAdapter adapter;
    private ArrayList<Recipe> allRecipes = new ArrayList<>();
    private ArrayList<Recipe> favoriteRecipes = new ArrayList<>();
    private boolean showingFavorites = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.recipesfrag, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize ChatGPT Client - REPLACE WITH YOUR ACTUAL API KEY
        chatGPTClient = new ChatGPTClient("");

        // Initialize views
        btnGenerateRecipes = view.findViewById(R.id.btnGenerateRecipes);
        tabAllRecipes = view.findViewById(R.id.tabAllRecipes);
        tabFavorites = view.findViewById(R.id.tabFavorites);
        recyclerViewRecipes = view.findViewById(R.id.recyclerViewRecipes);
        loadingIndicator = view.findViewById(R.id.loadingIndicator);
        tvLoadingText = view.findViewById(R.id.tvLoadingText);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);

        // Setup RecyclerView
        adapter = new RecipeAdapter(allRecipes);
        recyclerViewRecipes.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewRecipes.setAdapter(adapter);

        // Setup adapter listener
        adapter.setOnRecipeClickListener(new RecipeAdapter.OnRecipeClickListener() {
            @Override
            public void onRecipeClick(Recipe recipe) {
                showRecipeDetail(recipe);
            }

            @Override
            public void onFavoriteClick(Recipe recipe, int position) {
                toggleFavorite(recipe, position);
            }
        });

        // Tab click listeners
        tabAllRecipes.setOnClickListener(v -> switchToAllRecipes());
        tabFavorites.setOnClickListener(v -> switchToFavorites());

        // Generate recipes button
        btnGenerateRecipes.setOnClickListener(v -> generateRecipes());

        // Setup real-time listener
        setupRealtimeListener();
    }

    private void setupRealtimeListener() {
        String userId = auth.getCurrentUser().getUid();

        // Remove old listener if exists
        if (recipeListener != null) {
            recipeListener.remove();
        }

        // Setup real-time listener
        recipeListener = db.collection("users")
                .document(userId)
                .collection("recipes")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed: " + error.getMessage());
                        return;
                    }

                    if (snapshots != null && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            allRecipes.clear();
                            favoriteRecipes.clear();

                            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                Recipe recipe = doc.toObject(Recipe.class);
                                if (recipe != null) {
                                    recipe.setDocId(doc.getId());
                                    allRecipes.add(recipe);

                                    if (recipe.isFavorite()) {
                                        favoriteRecipes.add(recipe);
                                    }
                                }
                            }

                            Log.d(TAG, "Loaded " + allRecipes.size() + " recipes");

                            // Hide loading when recipes are loaded
                            showLoading(false);

                            updateUI();
                        });
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (recipeListener != null) {
            recipeListener.remove();
        }
    }

    private void generateRecipes() {
        // Show loading
        showLoading(true);

        // Get ingredients from database
        String userId = auth.getCurrentUser().getUid();
        db.collection("users")
                .document(userId)
                .collection("items")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        showLoading(false);
                        Toast.makeText(getContext(),
                                "No ingredients found! Add items to your inventory first.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Build ingredient list
                    StringBuilder ingredientList = new StringBuilder();
                    List<FridgeItem> userIngredients = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        FridgeItem item = doc.toObject(FridgeItem.class);
                        if (item != null) {
                            userIngredients.add(item);
                            ingredientList.append("- ")
                                    .append(item.getName())
                                    .append(": ")
                                    .append(item.getQuantity())
                                    .append(" ")
                                    .append(item.getUnit())
                                    .append("\n");
                        }
                    }

                    // Create prompt for ChatGPT
                    String prompt = createRecipePrompt(ingredientList.toString());

                    // Call ChatGPT API
                    chatGPTClient.sendMessage(prompt, new ChatGPTClient.ChatCallback() {
                        @Override
                        public void onSuccess(String reply) {
                            Log.d(TAG, "ChatGPT response received");
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    parseAndSaveRecipes(reply, userIngredients);
                                });
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "ChatGPT error: " + error);
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    showLoading(false);
                                    Toast.makeText(getContext(),
                                            "Failed to generate recipes: " + error,
                                            Toast.LENGTH_LONG).show();
                                });
                            }
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(getContext(),
                            "Failed to load ingredients",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private String createRecipePrompt(String ingredients) {
        return "You are a helpful cooking assistant. I have the following ingredients in my fridge:\n\n" +
                ingredients + "\n\n" +
                "Please generate exactly 10 different recipes. For each recipe:\n" +
                "1. Use ingredients I have, OR suggest recipes where I have at least 50% of the ingredients\n" +
                "2. If a recipe requires ingredients I don't have, list them separately\n\n" +
                "Respond ONLY with a valid JSON array in this exact format (no extra text):\n" +
                "[\n" +
                "  {\n" +
                "    \"name\": \"Recipe Name\",\n" +
                "    \"difficulty\": \"Easy/Medium/Hard\",\n" +
                "    \"estimatedTime\": \"30 min\",\n" +
                "    \"calories\": \"450 kcal\",\n" +
                "    \"ingredients\": {\n" +
                "      \"Ingredient1\": \"2 cups\",\n" +
                "      \"Ingredient2\": \"1 tbsp\"\n" +
                "    },\n" +
                "    \"missingIngredients\": [\"Ingredient3\", \"Ingredient4\"],\n" +
                "    \"steps\": [\n" +
                "      \"Step 1 description\",\n" +
                "      \"Step 2 description\"\n" +
                "    ]\n" +
                "  }\n" +
                "]\n\n" +
                "Make sure the JSON is valid and properly formatted.";
    }

    private void parseAndSaveRecipes(String jsonResponse, List<FridgeItem> userIngredients) {
        try {
            // Clean the response
            String cleanJson = jsonResponse.trim();
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7);
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            }
            cleanJson = cleanJson.trim();

            Gson gson = new Gson();
            JsonArray recipesArray = gson.fromJson(cleanJson, JsonArray.class);

            String userId = auth.getCurrentUser().getUid();

            // Delete old non-favorite recipes first
            db.collection("users")
                    .document(userId)
                    .collection("recipes")
                    .whereEqualTo("favorite", false)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            doc.getReference().delete();
                        }

                        // Now save new recipes
                        saveRecipesToDb(recipesArray, userId, userIngredients);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to delete old recipes: " + e.getMessage());
                        // Try to save anyway
                        saveRecipesToDb(recipesArray, userId, userIngredients);
                    });

        } catch (Exception e) {
            Log.e(TAG, "Parse error: " + e.getMessage());
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(getContext(),
                            "Failed to parse recipes: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }
    }

    private void saveRecipesToDb(JsonArray recipesArray, String userId,
                                 List<FridgeItem> userIngredients) {
        int totalRecipes = recipesArray.size();
        int[] savedCount = {0};

        Log.d(TAG, "Saving " + totalRecipes + " recipes");

        for (int i = 0; i < recipesArray.size(); i++) {
            JsonObject recipeJson = recipesArray.get(i).getAsJsonObject();

            // Parse ingredients
            Map<String, String> ingredients = new HashMap<>();
            JsonObject ingredientsObj = recipeJson.getAsJsonObject("ingredients");
            for (String key : ingredientsObj.keySet()) {
                ingredients.put(key, ingredientsObj.get(key).getAsString());
            }

            // Parse steps
            List<String> steps = new ArrayList<>();
            JsonArray stepsArray = recipeJson.getAsJsonArray("steps");
            for (int j = 0; j < stepsArray.size(); j++) {
                steps.add(stepsArray.get(j).getAsString());
            }

            // Parse missing ingredients
            List<String> missingIngredients = new ArrayList<>();
            if (recipeJson.has("missingIngredients")) {
                JsonArray missingArray = recipeJson.getAsJsonArray("missingIngredients");
                for (int j = 0; j < missingArray.size(); j++) {
                    missingIngredients.add(missingArray.get(j).getAsString());
                }
            }

            Recipe recipe = new Recipe(
                    null,
                    recipeJson.get("name").getAsString(),
                    recipeJson.get("difficulty").getAsString(),
                    recipeJson.get("estimatedTime").getAsString(),
                    recipeJson.get("calories").getAsString(),
                    ingredients,
                    steps,
                    false,
                    missingIngredients
            );

            // Save to Firestore
            db.collection("users")
                    .document(userId)
                    .collection("recipes")
                    .add(recipe)
                    .addOnSuccessListener(documentReference -> {
                        savedCount[0]++;
                        Log.d(TAG, "Saved recipe " + savedCount[0] + "/" + totalRecipes);

                        if (savedCount[0] == totalRecipes) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    showLoading(false);
                                    Toast.makeText(getContext(),
                                            "Successfully generated " + totalRecipes + " recipes!",
                                            Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to save recipe: " + e.getMessage());
                        savedCount[0]++;

                        if (savedCount[0] == totalRecipes) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    showLoading(false);
                                });
                            }
                        }
                    });
        }
    }

    private void showRecipeDetail(Recipe recipe) {
        RecipeDetailDialog dialog = RecipeDetailDialog.newInstance(recipe);
        dialog.setOnDoneCookingListener(this::handleDoneCooking);
        dialog.show(getParentFragmentManager(), "RecipeDetailDialog");
    }

    private void handleDoneCooking(Recipe recipe) {
        String userId = auth.getCurrentUser().getUid();

        // Get user's current ingredients
        db.collection("users")
                .document(userId)
                .collection("items")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String, FridgeItem> userItems = new HashMap<>();
                    Map<String, String> userItemDocIds = new HashMap<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        FridgeItem item = doc.toObject(FridgeItem.class);
                        if (item != null) {
                            userItems.put(item.getName().toLowerCase(), item);
                            userItemDocIds.put(item.getName().toLowerCase(), doc.getId());
                        }
                    }

                    // Deduct ingredients
                    for (Map.Entry<String, String> entry : recipe.getIngredients().entrySet()) {
                        String ingredientName = entry.getKey().toLowerCase();
                        String quantityStr = entry.getValue();

                        if (userItems.containsKey(ingredientName)) {
                            FridgeItem userItem = userItems.get(ingredientName);
                            String docId = userItemDocIds.get(ingredientName);

                            // Parse recipe quantity
                            int recipeQty = parseQuantity(quantityStr);
                            int newQuantity = userItem.getQuantity() - recipeQty;

                            if (newQuantity <= 0) {
                                // Delete item
                                db.collection("users")
                                        .document(userId)
                                        .collection("items")
                                        .document(docId)
                                        .delete();
                            } else {
                                // Update quantity
                                db.collection("users")
                                        .document(userId)
                                        .collection("items")
                                        .document(docId)
                                        .update("quantity", newQuantity);
                            }
                        }
                    }

                    Toast.makeText(getContext(),
                            "Ingredients updated! Enjoy your meal! 🍽️",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private int parseQuantity(String quantityStr) {
        try {
            // Extract number from string like "2 cups", "1 tbsp", etc.
            String[] parts = quantityStr.split(" ");
            if (parts.length > 0) {
                return Integer.parseInt(parts[0]);
            }
        } catch (NumberFormatException e) {
            // If parsing fails, return 1
        }
        return 1;
    }

    private void toggleFavorite(Recipe recipe, int position) {
        boolean newFavoriteState = !recipe.isFavorite();
        recipe.setFavorite(newFavoriteState);

        // Update UI immediately for better UX
        adapter.notifyItemChanged(position);

        String userId = auth.getCurrentUser().getUid();
        db.collection("users")
                .document(userId)
                .collection("recipes")
                .document(recipe.getDocId())
                .update("favorite", newFavoriteState)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Favorite updated successfully");

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            String message = newFavoriteState ?
                                    "Added to favorites" : "Removed from favorites";
                            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update favorite: " + e.getMessage());

                    // Revert the change on failure
                    recipe.setFavorite(!newFavoriteState);
                    adapter.notifyItemChanged(position);

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Failed to update favorite",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    private void switchToAllRecipes() {
        showingFavorites = false;
        tabAllRecipes.setTextColor(0xFFFF9800); // Orange
        tabAllRecipes.setBackgroundColor(0xFFFFFFFF); // White
        tabFavorites.setTextColor(0xFF999999); // Gray
        tabFavorites.setBackgroundColor(0xFFFFFFFF); // White

        adapter = new RecipeAdapter(allRecipes);
        adapter.setOnRecipeClickListener(new RecipeAdapter.OnRecipeClickListener() {
            @Override
            public void onRecipeClick(Recipe recipe) {
                showRecipeDetail(recipe);
            }

            @Override
            public void onFavoriteClick(Recipe recipe, int position) {
                toggleFavorite(recipe, position);
            }
        });
        recyclerViewRecipes.setAdapter(adapter);
        updateUI();
    }

    private void switchToFavorites() {
        showingFavorites = true;
        tabFavorites.setTextColor(0xFFFF9800); // Orange
        tabFavorites.setBackgroundColor(0xFFFFFFFF); // White
        tabAllRecipes.setTextColor(0xFF999999); // Gray
        tabAllRecipes.setBackgroundColor(0xFFFFFFFF); // White

        adapter = new RecipeAdapter(favoriteRecipes);
        adapter.setOnRecipeClickListener(new RecipeAdapter.OnRecipeClickListener() {
            @Override
            public void onRecipeClick(Recipe recipe) {
                showRecipeDetail(recipe);
            }

            @Override
            public void onFavoriteClick(Recipe recipe, int position) {
                toggleFavorite(recipe, position);
            }
        });
        recyclerViewRecipes.setAdapter(adapter);
        updateUI();
    }

    private void updateUI() {
        ArrayList<Recipe> currentList = showingFavorites ? favoriteRecipes : allRecipes;

        if (currentList.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerViewRecipes.setVisibility(View.GONE);

            TextView tvEmptyMessage = emptyStateLayout.findViewById(R.id.tvEmptyMessage);
            if (showingFavorites) {
                tvEmptyMessage.setText("No favorite recipes yet!\nMark recipes as favorites to see them here");
            } else {
                tvEmptyMessage.setText("No recipes yet!\nClick 'Generate Recipes' to start");
            }
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            recyclerViewRecipes.setVisibility(View.VISIBLE);
        }

        adapter.notifyDataSetChanged();
    }

    private void showLoading(boolean show) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                loadingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
                tvLoadingText.setVisibility(show ? View.VISIBLE : View.GONE);
                emptyStateLayout.setVisibility(View.GONE); // Hide empty state when loading
                recyclerViewRecipes.setVisibility(show ? View.GONE : View.VISIBLE);
                btnGenerateRecipes.setEnabled(!show);
            });
        }
    }
}
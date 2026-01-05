package com.example.fridgeapp.recipes;

import java.util.List;
import java.util.Map;

public class Recipe {
    private String docId;
    private String name;
    private String difficulty;
    private String estimatedTime;
    private String calories;
    private Map<String, String> ingredients; // ingredient name -> quantity with unit
    private List<String> steps;
    private boolean isFavorite;
    private List<String> missingIngredients; // ingredients user doesn't have

    public Recipe() {
        // Required empty constructor for Firestore
    }

    public Recipe(String docId, String name, String difficulty, String estimatedTime,
                  String calories, Map<String, String> ingredients, List<String> steps,
                  boolean isFavorite, List<String> missingIngredients) {
        this.docId = docId;
        this.name = name;
        this.difficulty = difficulty;
        this.estimatedTime = estimatedTime;
        this.calories = calories;
        this.ingredients = ingredients;
        this.steps = steps;
        this.isFavorite = isFavorite;
        this.missingIngredients = missingIngredients;
    }

    // Getters and Setters
    public String getDocId() { return docId; }
    public void setDocId(String docId) { this.docId = docId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(String estimatedTime) { this.estimatedTime = estimatedTime; }

    public String getCalories() { return calories; }
    public void setCalories(String calories) { this.calories = calories; }

    public Map<String, String> getIngredients() { return ingredients; }
    public void setIngredients(Map<String, String> ingredients) { this.ingredients = ingredients; }

    public List<String> getSteps() { return steps; }
    public void setSteps(List<String> steps) { this.steps = steps; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public List<String> getMissingIngredients() { return missingIngredients; }
    public void setMissingIngredients(List<String> missingIngredients) {
        this.missingIngredients = missingIngredients;
    }

    public boolean hasAllIngredients() {
        return missingIngredients == null || missingIngredients.isEmpty();
    }
}
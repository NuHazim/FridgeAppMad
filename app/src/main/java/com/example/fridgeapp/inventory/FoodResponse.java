package com.example.fridgeapp.inventory;

public class FoodResponse {
    public int status;
    public Product product;

    public static class Product {
        public String product_name;
        public String[] categories_tags;
    }
}

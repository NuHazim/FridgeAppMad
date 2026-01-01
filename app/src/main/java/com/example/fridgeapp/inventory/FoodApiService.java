package com.example.fridgeapp.inventory;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface FoodApiService {

    @GET("api/v0/product/{barcode}.json")
    Call<FoodResponse> getProduct(
            @Path("barcode") String barcode
    );
}

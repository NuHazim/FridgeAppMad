package com.example.fridgeapp.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.fridgeapp.R;
import com.example.fridgeapp.inventory.FoodApiService;
import com.example.fridgeapp.inventory.FoodResponse;
import com.example.fridgeapp.inventory.RetrofitClient;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BarcodeActivity extends AppCompatActivity {

    private PreviewView previewView;
    private BarcodeScanner barcodeScanner;
    private boolean scanned = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);

        previewView = findViewById(R.id.previewView);

        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                                Barcode.FORMAT_EAN_13,
                                Barcode.FORMAT_EAN_8,
                                Barcode.FORMAT_UPC_A,
                                Barcode.FORMAT_UPC_E)
                        .build();

        barcodeScanner = BarcodeScanning.getClient(options);
        startCamera();
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis =
                        new ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build();

                imageAnalysis.setAnalyzer(
                        ContextCompat.getMainExecutor(this),
                        imageProxy -> {
                            if (scanned || imageProxy.getImage() == null) {
                                imageProxy.close();
                                return;
                            }

                            InputImage image = InputImage.fromMediaImage(
                                    imageProxy.getImage(),
                                    imageProxy.getImageInfo().getRotationDegrees()
                            );

                            barcodeScanner.process(image)
                                    .addOnSuccessListener(barcodes -> {
                                        for (Barcode barcode : barcodes) {
                                            String rawValue = barcode.getRawValue();

                                            if (rawValue != null) {
                                                scanned = true;

                                                // ✅ Call API with the barcode
                                                fetchProductInfo(rawValue);

                                                imageProxy.close();
                                                return;
                                            }
                                        }
                                        imageProxy.close();
                                    })
                                    .addOnFailureListener(e -> imageProxy.close());
                        });

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                );

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void fetchProductInfo(String barcode) {
        FoodApiService apiService = RetrofitClient.getInstance().create(FoodApiService.class);
        apiService.getProduct(barcode).enqueue(new Callback<FoodResponse>() {
            @Override
            public void onResponse(Call<FoodResponse> call, Response<FoodResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().product != null) {
                    String productName = response.body().product.product_name; // check @SerializedName
                    String category = response.body().product.categories_tags[0];

                    Intent result = new Intent();
                    result.putExtra("PRODUCT_NAME", productName != null ? productName : "");
                    result.putExtra("CATEGORY", category != null ? category : "");
                    setResult(RESULT_OK, result);
                    finish();
                } else {
                    Toast.makeText(BarcodeActivity.this, "Product not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<FoodResponse> call, Throwable t) {
                Toast.makeText(BarcodeActivity.this, "API call failed", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}

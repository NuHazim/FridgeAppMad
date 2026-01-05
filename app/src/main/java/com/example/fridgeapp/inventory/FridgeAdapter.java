package com.example.fridgeapp.inventory;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fridgeapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class FridgeAdapter extends RecyclerView.Adapter<FridgeAdapter.ViewHolder> {

    private final List<FridgeItem> itemList;
    FridgeAdapter adapter;

    public FridgeAdapter(List<FridgeItem> itemList ) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fridge, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FridgeItem item = itemList.get(position);

        holder.tvName.setText(item.name);
        holder.tvCategory.setText("Category: " + item.category);
        holder.tvQuantity.setText(item.quantity + " " + item.unit);

        long daysLeft = item.getDaysLeft();
        holder.tvExpiry.setText(daysLeft + " days left");
        if(daysLeft < 0) {
            holder.tvExpiry.setText("Expired");
            holder.tvExpiry.setBackgroundResource(R.drawable.bg_grey);;
        }
        else if (daysLeft <= 3) {
            holder.tvExpiry.setBackgroundResource(R.drawable.bg_red);
        } else if (daysLeft <= 7) {
            holder.tvExpiry.setBackgroundResource(R.drawable.bg_yellow);
        } else {
            holder.tvExpiry.setBackgroundResource(R.drawable.bg_green);
        }



        holder.btnDelete.setOnClickListener(v -> {
            deleteItem(holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvCategory, tvExpiry, tvQuantity, tvUnit; // add tvUnit
        ImageButton btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvExpiry = itemView.findViewById(R.id.tvExpiry);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvUnit = itemView.findViewById(R.id.tvUnit); // initialize it
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    private void deleteItem(int position) {

        FridgeItem item = itemList.get(position);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("items")
                .document(item.docId)
                .delete()
                .addOnSuccessListener(aVoid -> {

                    itemList.remove(position);
                    adapter.notifyItemRemoved(position);

                });
    }


}


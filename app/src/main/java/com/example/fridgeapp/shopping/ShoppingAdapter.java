package com.example.fridgeapp.shopping;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fridgeapp.R;
import com.example.fridgeapp.model.ShoppingItem;

import java.util.List;

public class ShoppingAdapter extends RecyclerView.Adapter<ShoppingAdapter.ViewHolder> {

    private List<ShoppingItem> items;
    private OnItemCheckedListener checkedListener;
    private OnItemDeleteListener deleteListener;

    public interface OnItemCheckedListener {
        void onItemChecked(ShoppingItem item, boolean isChecked);
    }

    public interface OnItemDeleteListener {
        void onItemDelete(ShoppingItem item);
    }

    public ShoppingAdapter(List<ShoppingItem> items,
                           OnItemCheckedListener checkedListener,
                           OnItemDeleteListener deleteListener) {
        this.items = items;
        this.checkedListener = checkedListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shopping, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShoppingItem item = items.get(position);

        holder.tvItemName.setText(item.getName());
        holder.tvCategory.setText(item.getCategory());
        holder.tvQuantity.setText(item.getQuantityNumber() + " " + item.getUnit());

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(item.isCompleted());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (checkedListener != null) {
                checkedListener.onItemChecked(item, isChecked);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onItemDelete(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView tvItemName, tvCategory, tvQuantity;
        ImageButton btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
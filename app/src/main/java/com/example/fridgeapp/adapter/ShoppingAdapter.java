package com.example.fridgeapp.adapter;

import android.graphics.Paint;
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

public class ShoppingAdapter extends RecyclerView.Adapter<ShoppingAdapter.ShoppingViewHolder> {

    private List<ShoppingItem> items;
    private OnItemCheckedListener onItemCheckedListener;
    private OnItemDeleteListener onItemDeleteListener;

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
        this.onItemCheckedListener = checkedListener;
        this.onItemDeleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ShoppingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shopping, parent, false);
        return new ShoppingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingViewHolder holder, int position) {
        ShoppingItem item = items.get(position);

        holder.tvItemName.setText(item.getName());
        holder.tvQuantity.setText(item.getQuantity());
        holder.checkBox.setChecked(item.isCompleted());

        if (item.isCompleted()) {
            holder.tvItemName.setPaintFlags(holder.tvItemName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvQuantity.setPaintFlags(holder.tvQuantity.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvItemName.setPaintFlags(holder.tvItemName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvQuantity.setPaintFlags(holder.tvQuantity.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setCompleted(isChecked);
            if (onItemCheckedListener != null) {
                onItemCheckedListener.onItemChecked(item, isChecked);
            }
            notifyItemChanged(position);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (onItemDeleteListener != null) {
                onItemDeleteListener.onItemDelete(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateItems(List<ShoppingItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void removeItem(ShoppingItem item) {
        int position = items.indexOf(item);
        if (position != -1) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class ShoppingViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView tvItemName;
        TextView tvQuantity;
        ImageButton btnDelete;

        public ShoppingViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkboxCompleted);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
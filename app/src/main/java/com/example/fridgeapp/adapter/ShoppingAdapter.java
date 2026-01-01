package com.example.fridgeapp.adapter;

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

    public interface OnItemCheckListener {
        void onItemCheck(ShoppingItem item, boolean isChecked);
    }

    public interface OnItemDeleteListener {
        void onItemDelete(ShoppingItem item);
    }

    private List<ShoppingItem> itemList;
    private OnItemCheckListener checkListener;
    private OnItemDeleteListener deleteListener;

    public ShoppingAdapter(List<ShoppingItem> itemList,
                           OnItemCheckListener checkListener,
                           OnItemDeleteListener deleteListener) {

        this.itemList = itemList;
        this.checkListener = checkListener;
        this.deleteListener = deleteListener;
        setHasStableIds(true);  // prevents RecyclerView crash
    }

    @Override
    public long getItemId(int position) {
        return itemList.get(position).getId().hashCode();
    }

    @NonNull
    @Override
    public ShoppingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shopping, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingAdapter.ViewHolder holder, int position) {
        ShoppingItem item = itemList.get(position);

        holder.tvName.setText(item.getName());
        holder.tvQuantity.setText(item.getQuantity());

        holder.cbCompleted.setOnCheckedChangeListener(null);
        holder.cbCompleted.setChecked(item.isCompleted());

        holder.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (checkListener != null) {
                checkListener.onItemCheck(item, isChecked);
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
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity;
        CheckBox cbCompleted;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvItemName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            cbCompleted = itemView.findViewById(R.id.checkboxCompleted);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}

package com.inventory.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inventory.R;
import com.inventory.listener.ItemClick;
import com.inventory.model.ProductsModel;

public class ProductsViewHolder extends RecyclerView.ViewHolder {

    private final TextView tvProductId;
    private final TextView tvProductName;
    private final TextView tvProductCategory;
    private final TextView tvProductPrice;
    private final TextView tvProductQuantity;

    private ProductsModel productsModel;

    public ProductsViewHolder(@NonNull View itemView, ItemClick listener) {
        super(itemView);
        tvProductId = itemView.findViewById(R.id.tvProductId);
        tvProductName = itemView.findViewById(R.id.tvProduct);
        tvProductCategory = itemView.findViewById(R.id.tvCategory);
        tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
        tvProductQuantity = itemView.findViewById(R.id.tvProductQuantity);

        itemView.setOnClickListener(v -> listener.onClick(itemView, getAdapterPosition(), productsModel));

        itemView.setOnLongClickListener(v -> {
            listener.onLongClick(itemView, getAdapterPosition(), productsModel);
            return false;
        });
    }

    // Initiates product listing view and set data to respective fields
    public void init(ProductsModel productsModel) {
        this.productsModel = productsModel;

        tvProductId.setText(String.format(itemView.getContext().getString(R.string.product_id), productsModel.getProductId()));
        tvProductName.setText(String.format(itemView.getContext().getString(R.string.product_name), productsModel.getProductName()));
        tvProductCategory.setText(String.format(itemView.getContext().getString(R.string.category), productsModel.getProductCategory()));
        tvProductQuantity.setText(String.format(itemView.getContext().getString(R.string.quantity), productsModel.getProductQuantity()));
        tvProductPrice.setText(String.format(itemView.getContext().getString(R.string.price), productsModel.getProductPrice()));
    }
}

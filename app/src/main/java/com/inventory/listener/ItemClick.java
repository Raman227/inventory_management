package com.inventory.listener;

import android.view.View;

import com.inventory.model.ProductsModel;

public interface ItemClick {
    // activates when user click on any product from products listing
    void onClick(View view, int position, ProductsModel productsModel);

    // activates when user long press on any product from products listing
    void onLongClick(View view, int position, ProductsModel productsModel);
}

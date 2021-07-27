package com.inventory.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inventory.R;
import com.inventory.listener.ItemClick;
import com.inventory.model.ProductsModel;
import com.inventory.viewholder.ProductsViewHolder;

import java.util.ArrayList;

public class ProductsAdapter extends RecyclerView.Adapter<ProductsViewHolder> implements Filterable {

    private final ArrayList<ProductsModel> products;
    private final ArrayList<ProductsModel> filtered;
    private final ItemClick listener;

    public ProductsAdapter(ItemClick listener) {
        this.listener = listener;
        products = new ArrayList<>();
        filtered = new ArrayList<>();
    }

    public void update(ArrayList<ProductsModel> products) {
        this.products.addAll(products);
        this.filtered.addAll(products);
        notifyDataSetChanged();
    }

    public void addProducts(ProductsModel productsModel) {
        products.add(productsModel);
        filtered.add(productsModel);
        notifyDataSetChanged();
    }

    public ArrayList<ProductsModel> getProducts() {
        return products;
    }

    public void clearAll() {
        products.clear();
        filtered.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProductsViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory_products, parent, false), listener
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ProductsViewHolder holder, int position) {
        holder.init(filtered.get(position));
    }

    @Override
    public int getItemCount() {
        return filtered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                ArrayList<ProductsModel> filteredList = new ArrayList<>();
                if ((null == constraint) || (constraint.length() == 0)) {
                    filteredList.addAll(products);
                } else {
                    String search = "" + constraint;

                    for (ProductsModel productModel : products) {
                        if (productModel.getProductName().toLowerCase().trim().contains(search.toLowerCase().trim())) {
                            filteredList.add(productModel);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filtered.clear();
                //noinspection unchecked
                filtered.addAll((ArrayList<ProductsModel>) results.values);
                notifyDataSetChanged();
            }
        };
    }
}

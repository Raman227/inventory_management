package com.inventory.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.inventory.R;
import com.inventory.adapters.ProductsAdapter;
import com.inventory.constants.Constants;
import com.inventory.listener.ItemClick;
import com.inventory.model.ProductsModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;

    private View loading;
    private ConstraintLayout parent;

    private ConstraintLayout productsDetails;
    private RecyclerView rvProducts;
    private TextView tvNoProduct;
    private EditText etSearch;

    private ProductsAdapter productsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loading = findViewById(R.id.loading);
        parent = findViewById(R.id.mainParent);

        productsDetails = findViewById(R.id.productsDetails);
        rvProducts = findViewById(R.id.rvProducts);
        tvNoProduct = findViewById(R.id.tvNoProduct);
        etSearch = findViewById(R.id.etSearch);

        setupAdapter();
        listener();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference(Constants.Products);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchProducts();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            logout();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return true;
        } else if (item.getItemId() == R.id.add) {
            addProduct();
            return true;
        } else if (item.getItemId() == R.id.refresh) {
            loading.setVisibility(View.VISIBLE);
            fetchProducts();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private final ItemClick listener = new ItemClick() {
        @Override
        public void onClick(View view, int position, ProductsModel productsModel) {
            hideKeyboard();
        }

        @Override
        public void onLongClick(View view, int position, ProductsModel productsModel) {
            hideKeyboard();
            options(view, position);
        }
    };

    // setup recyclerview adapter for products listing
    private void setupAdapter() {
        productsAdapter = new ProductsAdapter(listener);
        rvProducts.setAdapter(productsAdapter);
    }

    // listener to fetch text enter by user into search field
    private void listener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                productsAdapter.getFilter().filter("" + s);
            }
        });

        parent.setOnClickListener(v -> hideKeyboard());
    }

    // fetch products listing from firebase database and update UI accordingly
    private void fetchProducts() {
        databaseReference.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                updateUi();
                Snackbar.make(parent, String.format(getString(R.string.error), task.getException()), Snackbar.LENGTH_LONG).show();
            } else {
                //noinspection ConstantConditions
                if (task.getResult().exists()) {
                    Log.i("MainActivity", "Count is : " + task.getResult().getChildrenCount());
                    productsAdapter.clearAll();
                    for (DataSnapshot ds : task.getResult().getChildren()) {
                        ProductsModel productsModel = ds.getValue(ProductsModel.class);
                        productsAdapter.addProducts(productsModel);
                    }
                }
                updateUi();
            }
            loading.setVisibility(View.GONE);
        });
    }

    // update UI according to item count from database. show product listing if itemcount > 0
    // else show no product found
    private void updateUi() {
        if (productsAdapter.getItemCount() > 0) {
            productsDetails.setVisibility(View.VISIBLE);
            tvNoProduct.setVisibility(View.GONE);
        } else {
            productsDetails.setVisibility(View.GONE);
            tvNoProduct.setVisibility(View.VISIBLE);
        }
    }

    // switch to screen where user can add new products to database
    private void addProduct() {
        String data = new Gson().toJson(productsAdapter.getProducts());
        startActivity(new Intent(MainActivity.this, AddProductActivity.class)
                .putExtra(Constants.Products, data));
    }

    // switch to screen where user can edit existing product details
    private void editProduct(int position) {
        String data = new Gson().toJson(productsAdapter.getProducts());
        startActivity(new Intent(MainActivity.this, AddProductActivity.class)
                .putExtra(Constants.Products, data)
                .putExtra(Constants.Edit, true)
                .putExtra(Constants.Position, position));
    }

    // show alert to user and ask if they really want to delete that product.
    // If user clicks delete, Delete that particular product.
    // If user click cancel. Simply dismiss dialog.
    private void deleteProduct(int position) {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setMessage(getString(R.string.message_delete));
        alert.setPositiveButton(getString(R.string.text_delete), (dialog, which) -> delete(position));
        alert.setNegativeButton(getString(R.string.text_cancel), (dialog, which) -> dialog.dismiss());

        alert.show();
    }

    // delete product from database
    private void delete(int position) {
        loading.setVisibility(View.VISIBLE);
        databaseReference.child("" + position).removeValue();
        fetchProducts();
    }

    // Show options to user when they long press on any product from listing
    // #1 Edit product
    // #2 Delete product
    private void options(View view, int position) {
        PopupMenu menu = new PopupMenu(MainActivity.this, view);
        menu.getMenuInflater().inflate(R.menu.popup_menu, menu.getMenu());

        menu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.edit) {
                editProduct(position);
                return true;
            } else if (item.getItemId() == R.id.delete) {
                deleteProduct(position);
                return true;
            } else {
                return false;
            }
        });

        menu.show();
    }

    // Update users login status into firebase database
    private void logout() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(Constants.Profile);
        ref.child(Constants.Login).setValue(Constants.False);
    }


    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
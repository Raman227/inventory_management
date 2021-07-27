package com.inventory.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.inventory.R;
import com.inventory.constants.Constants;
import com.inventory.model.ProductsModel;
import com.inventory.utils.GenerateRandomString;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class AddProductActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;

    private View loading;
    private ConstraintLayout parent;

    private TextView productId;
    private EditText productName;
    private EditText productCategory;
    private EditText productQuantity;
    private EditText productPrice;

    private ProductsModel model = new ProductsModel();
    private ArrayList<ProductsModel> products;
    private boolean edit = false;
    private int position = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getIntentData();
        setContentView(R.layout.activity_add_product);
        setupActionBar();

        loading = findViewById(R.id.loading);
        parent = findViewById(R.id.productParent);

        productId = findViewById(R.id.productId);
        productName = findViewById(R.id.etName);
        productCategory = findViewById(R.id.etCategory);
        productQuantity = findViewById(R.id.etQuantity);
        productPrice = findViewById(R.id.etPrice);


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference(Constants.Products);
        loading.setVisibility(View.GONE);

        if (edit)
            setProductData();
        else
            generateProductId();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.save) {
            saveProduct();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getIntentData() {
        if (getIntent().hasExtra(Constants.Products)) {
            String data = getIntent().getStringExtra(Constants.Products);

            Type type = new TypeToken<ArrayList<ProductsModel>>() {
            }.getType();
            products = new Gson().fromJson(data, type);
        }

        if (getIntent().hasExtra(Constants.Edit)) {
            edit = getIntent().getBooleanExtra(Constants.Edit, false);
            position = getIntent().getIntExtra(Constants.Position, -1);
        }
    }

    private void setupActionBar() {
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getActionBarTitle());
    }

    // Set action bar title according to user request either user edit or add product here
    private String getActionBarTitle() {
        if (edit) {
            return getString(R.string.title_edit_product);
        }
        return getString(R.string.title_add_product);
    }

    // Randomly generate product id for every new product user add. (length=6)
    private void generateProductId() {
        model.setProductId(GenerateRandomString.randomString(6));
        productId.setText(model.getProductId());
    }

    // Update UI and fill product data in fields if user wants to edit product.
    private void setProductData() {
        if (position > -1) {
            model = products.get(position);
            setData();
        } else {
            generateProductId();
        }
    }

    // Update UI and fill product data in fields if user wants to edit product.
    private void setData() {
        productId.setText(model.getProductId());
        productName.setText(model.getProductName());
        productCategory.setText(model.getProductCategory());
        productQuantity.setText(model.getProductQuantity());
        productPrice.setText(model.getProductPrice());
    }

    // Save/Edit product to database after validation
    private void saveProduct() {
        loading.setVisibility(View.VISIBLE);
        hideKeyboard();
        if (validateProduct()) {
            if (null == products)
                products = new ArrayList<>();

            if (edit)
                products.set(position, model);
            else
                products.add(model);

            databaseReference.setValue(products).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Snackbar.make(parent, getMessage(), Snackbar.LENGTH_LONG).show();
                    finish();
                }
                loading.setVisibility(View.GONE);
            });
        }
    }

    private String getMessage() {
        if (edit)
            return getString(R.string.product_edited);
        else
            return getString(R.string.product_added);
    }

    // Validate fields before saving/editing into database
    private Boolean validateProduct() {
        String name = "" + productName.getText();
        String category = "" + productCategory.getText();
        String quantity = "" + productQuantity.getText();
        String price = "" + productPrice.getText();
        boolean validate = true;

        if (name.isEmpty()) {
            showMessage(getString(R.string.empty_name));
            validate = false;
        } else if (category.isEmpty()) {
            showMessage(getString(R.string.empty_category));
            validate = false;
        } else if (quantity.isEmpty()) {
            showMessage(getString(R.string.empty_quantity));
            validate = false;
        } else if (Integer.parseInt(quantity) <= 0) {
            showMessage(getString(R.string.less_than_zero_quantity));
            validate = false;
        } else if (price.isEmpty()) {
            showMessage(getString(R.string.empty_price));
            validate = false;
        } else if (Integer.parseInt(price) <= 0) {
            showMessage(getString(R.string.less_than_zero_category));
            validate = false;
        }

        if (validate) {
            model.setProductName(name);
            model.setProductCategory(category);
            model.setProductQuantity(quantity);
            model.setProductPrice(price);
        }
        return validate;
    }

    private void showMessage(String message) {
        Snackbar.make(parent, message, Snackbar.LENGTH_LONG).show();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}

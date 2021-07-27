package com.inventory.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.inventory.R;
import com.inventory.constants.Constants;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;

    private View loading;
    private ConstraintLayout parent;
    private Button submit;
    private EditText etUsername;
    private EditText etPassword;

    private String username = Constants.NoCredentials;
    private String password = Constants.NoCredentials;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loading = findViewById(R.id.loading);
        parent = findViewById(R.id.parentLayout);

        submit = findViewById(R.id.btnSubmit);
        etUsername = findViewById(R.id.etUserName);
        etPassword = findViewById(R.id.etPassword);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference(Constants.Credentials);
        fetchCredentials();
        clickListener();
    }

    private void clickListener() {

        parent.setOnClickListener(v -> hideKeyboard());
        submit.setOnClickListener(v -> {
            loading.setVisibility(View.VISIBLE);
            hideKeyboard();
            validateCredentials();
        });
    }

    // Fetch users login credentials from Firebase database
    private void fetchCredentials() {
        databaseReference.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Snackbar.make(parent, String.format(getString(R.string.error), task.getException()), Snackbar.LENGTH_LONG).show();
            } else {
                //noinspection ConstantConditions
                if (task.getResult().exists()) {
                    username = "" + task.getResult().child(Constants.Username).getValue();
                    password = "" + task.getResult().child(Constants.Password).getValue();

                    Log.i("LoginActivity", "Username is : " + username + " Password is : " + password);
                } else {
                    Snackbar.make(parent, getString(R.string.no_credentials), Snackbar.LENGTH_LONG).show();
                }
            }
            loading.setVisibility(View.GONE);
        });
    }

    // Validate users credentials whether they entered correct details or not.
    private void validateCredentials() {
        String user = "" + etUsername.getText();
        String pass = "" + etPassword.getText();
        if (user.isEmpty()) {
            Snackbar.make(parent, getString(R.string.empty_username), Snackbar.LENGTH_LONG).show();
        } else if (pass.isEmpty()) {
            Snackbar.make(parent, getString(R.string.empty_password), Snackbar.LENGTH_LONG).show();
        } else if ((!user.equals(username)) || (!pass.equals(password))) {
            Snackbar.make(parent, getString(R.string.no_credentials), Snackbar.LENGTH_LONG).show();
        } else {
            login();
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
        loading.setVisibility(View.GONE);
    }

    // Update users login status into firebase database
    private void login() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(Constants.Profile);
        ref.child(Constants.Login).setValue(Constants.True);
    }

    // hide keyboard when user click submit button or clicks outside the fields
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}

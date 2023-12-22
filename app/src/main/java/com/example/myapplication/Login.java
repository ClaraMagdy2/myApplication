package com.example.myapplication;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        TextView signUpTextView = findViewById(R.id.textViewSignUp);
        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSignUp();
            }
        });
        dbHelper = new DBHelper(this);

        usernameEditText = findViewById(R.id.editTextUsernameLogin);
        passwordEditText = findViewById(R.id.editTextPasswordLogin);
        loginButton = findViewById(R.id.buttonLogin);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }

    private void login() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        // Query the database to check if the user exists
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DBHelper.COLUMN_USERNAME + " = ? AND " + DBHelper.COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};

        Cursor cursor = db.query(
                DBHelper.TABLE_USERS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            // User exists, login successful
            showToast("Login successful");
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("key_username", username);
            startActivity(intent);
            finish();
             // Close the current login activity
            // You may navigate to another activity after successful login
            // For example, you can start a new activity:
            // Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            // startActivity(intent);

        } else {
            // User does not exist or invalid credentials
            showToast("Invalid username or password");
        }

        // Close cursor and database
        if (cursor != null) {
            cursor.close();
        }
        db.close();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    public void goToSignUp() {
        Intent intent = new Intent(this, SignUp.class);
        startActivity(intent);
    }
}


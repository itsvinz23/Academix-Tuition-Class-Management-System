package com.academix.academix;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AdminProfile extends AppCompatActivity {

    DBHelper dbHelper;
    int adminId = -1;

    TextView currentUsernameValue;
    EditText newUsernameEditText, newPasswordEditText, confirmPasswordEditText, currentPasswordEditText;
    Button updateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);

        dbHelper = new DBHelper(this);

        // Bind views
        currentUsernameValue = findViewById(R.id.currentUsernameValue);
        newUsernameEditText = findViewById(R.id.newUsernameEditText);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        currentPasswordEditText = findViewById(R.id.currentPasswordEditText);
        updateButton = findViewById(R.id.updateButton);

        // Load admin details
        loadAdminDetails();

        // Handle update button click
        updateButton.setOnClickListener(view -> updateAdminProfile());
    }

    private void loadAdminDetails() {
        Cursor cursor = dbHelper.getFirstAdmin();
        if (cursor.moveToFirst()) {
            adminId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
            currentUsernameValue.setText(username);
        }
        cursor.close();
    }

    private void updateAdminProfile() {
        String newUsername = newUsernameEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String currentPassword = currentPasswordEditText.getText().toString().trim();

        if (newUsername.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty() || currentPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Recheck current password
        Cursor cursor = dbHelper.getFirstAdmin();
        if (cursor.moveToFirst()) {
            String actualCurrentPassword = cursor.getString(cursor.getColumnIndexOrThrow("password"));
            if (!actualCurrentPassword.equals(currentPassword)) {
                Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                cursor.close();
                return;
            }
        }
        cursor.close();

        // Update
        boolean success = dbHelper.updateAdmin(adminId, newUsername, newPassword);
        if (success) {
            Toast.makeText(this, "Admin profile updated", Toast.LENGTH_SHORT).show();
            currentUsernameValue.setText(newUsername);
        } else {
            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
        }
    }
}

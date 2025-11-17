package com.academix.academix;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdminTeachers extends AppCompatActivity {

    private EditText firstNameEditText, lastNameEditText, contactEditText,
            subjectEditText, usernameEditText, passwordEditText;
    private Button registerButton;

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_teachers);

        // Set padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize DBHelper
        dbHelper = new DBHelper(this);

        // Initialize UI elements
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        contactEditText = findViewById(R.id.contactEditText);
        subjectEditText = findViewById(R.id.txtSubject);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        registerButton = findViewById(R.id.registerButton);

        // Register button click
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String fname = firstNameEditText.getText().toString().trim();
                String lname = lastNameEditText.getText().toString().trim();
                String contact = contactEditText.getText().toString().trim();
                String subject = subjectEditText.getText().toString().trim();
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (fname.isEmpty() || lname.isEmpty() || contact.isEmpty() || subject.isEmpty()
                        || username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(AdminTeachers.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean success = dbHelper.addTeacher(fname, lname, contact, subject, username, password);
                if (success) {
                    Toast.makeText(AdminTeachers.this, "Teacher registered successfully", Toast.LENGTH_SHORT).show();
                    clearInputs();
                } else {
                    Toast.makeText(AdminTeachers.this, "Username already exists", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void clearInputs() {
        firstNameEditText.setText("");
        lastNameEditText.setText("");
        contactEditText.setText("");
        subjectEditText.setText("");
        usernameEditText.setText("");
        passwordEditText.setText("");
    }
}

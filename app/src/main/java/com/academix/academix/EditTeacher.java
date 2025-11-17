package com.academix.academix;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditTeacher extends AppCompatActivity {

    private EditText firstNameEditText, lastNameEditText, contactEditText, txtSubject, usernameEditText, passwordEditText;
    private Button updateButton, deleteButton;
    private int teacherId;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_teacher);

        dbHelper = new DBHelper(this);

        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        contactEditText = findViewById(R.id.contactEditText);
        txtSubject = findViewById(R.id.txtSubject);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        updateButton = findViewById(R.id.updateButton);
        deleteButton = findViewById(R.id.deleteButton);

        // Get data from intent
        Intent intent = getIntent();
        teacherId = intent.getIntExtra("teacher_id", -1);

        firstNameEditText.setText(intent.getStringExtra("fname"));
        lastNameEditText.setText(intent.getStringExtra("lname"));
        contactEditText.setText(intent.getStringExtra("contact"));
        txtSubject.setText(intent.getStringExtra("subjectname"));
        usernameEditText.setText(intent.getStringExtra("username"));
        passwordEditText.setText(intent.getStringExtra("password"));

        // Update teacher
        updateButton.setOnClickListener(v -> {
            String fname = firstNameEditText.getText().toString().trim();
            String lname = lastNameEditText.getText().toString().trim();
            String contact = contactEditText.getText().toString().trim();
            String subject = txtSubject.getText().toString().trim();
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            boolean updated = dbHelper.updateTeacher(teacherId, fname, lname, contact, subject, username, password);
            if (updated) {
                Toast.makeText(this, "Teacher updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
            }
        });

        // Delete teacher
        deleteButton.setOnClickListener(v -> {
            boolean deleted = dbHelper.deleteTeacher(teacherId);
            if (deleted) {
                Toast.makeText(this, "Teacher deleted successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

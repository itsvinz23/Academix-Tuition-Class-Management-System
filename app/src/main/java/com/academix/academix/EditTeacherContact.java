package com.academix.academix;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EditTeacherContact extends AppCompatActivity {

    private EditText firstNameEditText, lastNameEditText, contactEditText, subjectEditText, usernameEditText, passwordEditText;
    private Button updateButton;

    private DBHelper dbHelper;
    private int teacherId;
    private String fname, lname, subject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_teacher_contact); // Your XML layout file name

        // Handle system window insets padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DBHelper(this);

        // Bind views
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        contactEditText = findViewById(R.id.contactEditText);
        subjectEditText = findViewById(R.id.subjectEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        updateButton = findViewById(R.id.updateButton);

        // Get teacher ID from intent extras
        Intent intent = getIntent();
        teacherId = intent.getIntExtra("teacher_id", -1);

        if (teacherId == -1) {
            Toast.makeText(this, "Invalid teacher ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load teacher data from DB
        Cursor cursor = dbHelper.getTeacherById(teacherId);
        if (cursor != null && cursor.moveToFirst()) {
            fname = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TEACHER_FNAME));
            lname = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TEACHER_LNAME));
            String contact = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TEACHER_CONTACT));
            subject = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TEACHER_SUBJECT));
            String username = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TEACHER_USERNAME));
            String password = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TEACHER_PASSWORD));

            // Set values to UI
            firstNameEditText.setText(fname);
            lastNameEditText.setText(lname);
            contactEditText.setText(contact);
            subjectEditText.setText(subject);
            usernameEditText.setText(username);
            passwordEditText.setText(password);

            cursor.close();
        } else {
            Toast.makeText(this, "Teacher not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Disable all except contact EditText
        firstNameEditText.setEnabled(false);
        lastNameEditText.setEnabled(false);
        subjectEditText.setEnabled(false);
        usernameEditText.setEnabled(false);
        passwordEditText.setEnabled(false);

        // Update contact on button click
        updateButton.setOnClickListener(v -> {
            String newContact = contactEditText.getText().toString().trim();

            if (newContact.isEmpty()) {
                Toast.makeText(this, "Contact cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = dbHelper.updateTeacherContactOnly(teacherId, newContact);

            if (success) {
                Toast.makeText(this, "Contact updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

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

public class EditStudentContact extends AppCompatActivity {

    private EditText fullNameEditText, contactEditText, usernameEditText, passwordEditText,courseEditText,idEditText;
    private Button updateContactButton;

    private DBHelper dbHelper;
    private int studentId;
    private String fname, lname, course;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_student_contact);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DBHelper(this);

        // View bindings
        idEditText = findViewById(R.id.idEditText);
        fullNameEditText = findViewById(R.id.fullNameEditText);
        contactEditText = findViewById(R.id.contactEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        updateContactButton = findViewById(R.id.updateContactButton);
        courseEditText = findViewById(R.id.courseEditText);

        // Get student ID from intent
        Intent intent = getIntent();
        studentId = intent.getIntExtra("student_id", -1);

        if (studentId == -1) {
            Toast.makeText(this, "Invalid student ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load student data from DB
        Cursor cursor = dbHelper.getStudentById(studentId);
        if (cursor != null && cursor.moveToFirst()) {
            fname = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_FNAME));
            lname = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_LNAME));
            String contact = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_CONTACT));
            course = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_COURSE));
            String username = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_USERNAME));
            String password = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_PASSWORD));

            // Set values to UI
            idEditText.setText(String.valueOf(studentId));
            fullNameEditText.setText(fname + " " + lname);
            contactEditText.setText(contact);
            usernameEditText.setText(username);
            passwordEditText.setText(password);
            courseEditText.setText(course);

            cursor.close();
        } else {
            Toast.makeText(this, "Student not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Update all details
        updateContactButton.setOnClickListener(v -> {
            String fullName = fullNameEditText.getText().toString().trim();
            String contact = contactEditText.getText().toString().trim();
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (fullName.isEmpty() || contact.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Split full name
            String[] nameParts = fullName.split(" ", 2);
            String updatedFname = nameParts.length > 0 ? nameParts[0] : "";
            String updatedLname = nameParts.length > 1 ? nameParts[1] : "";

            boolean success = dbHelper.updateStudent(
                    studentId,
                    updatedFname,
                    updatedLname,
                    contact,
                    course,
                    username,
                    password
            );

            if (success) {
                Toast.makeText(this, "Student updated successfully", Toast.LENGTH_SHORT).show();
                finish(); // go back
            } else {
                Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

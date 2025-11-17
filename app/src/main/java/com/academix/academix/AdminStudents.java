package com.academix.academix;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.cursoradapter.widget.SimpleCursorAdapter;

public class AdminStudents extends AppCompatActivity {

    private Spinner courseSpinner;
    private EditText firstNameEditText, lastNameEditText, contactEditText, usernameEditText, passwordEditText;
    private Button registerButton;
    private DBHelper dbHelper;
    private Cursor courseCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_students);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Initialize DBHelper FIRST
        dbHelper = new DBHelper(this);


        courseSpinner = findViewById(R.id.courseSpinner);


        // UI References
        courseSpinner = findViewById(R.id.courseSpinner);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        contactEditText = findViewById(R.id.contactEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        registerButton = findViewById(R.id.registerButton);

        // Load course names into spinner
        loadCourseSpinnerData();

        // Register button click
        registerButton.setOnClickListener(v -> registerStudent());
    }

    private void loadCourseSpinnerData() {
        courseCursor = dbHelper.getCourseNames(); // SELECT rowid AS _id, coursename FROM courses

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_spinner_item,
                courseCursor,
                new String[]{DBHelper.COL_COURSE_NAME},
                new int[]{android.R.id.text1},
                0
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseSpinner.setAdapter(adapter);

        courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(Color.WHITE); // Set spinner item color
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void registerStudent() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String contact = contactEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Get selected course name
        Cursor selectedCursor = (Cursor) courseSpinner.getSelectedItem();
        String selectedCourse = selectedCursor.getString(selectedCursor.getColumnIndexOrThrow(DBHelper.COL_COURSE_NAME));

        // Validation
        if (firstName.isEmpty() || lastName.isEmpty() || contact.isEmpty()
                || selectedCourse.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Insert into DB
        boolean success = dbHelper.addStudent(firstName, lastName, contact, selectedCourse, username, password);
        if (success) {
            Toast.makeText(this, "Student registered successfully", Toast.LENGTH_SHORT).show();
            clearFields();
        } else {
            Toast.makeText(this, "Registration failed. Try a different username.", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFields() {
        firstNameEditText.setText("");
        lastNameEditText.setText("");
        contactEditText.setText("");
        usernameEditText.setText("");
        passwordEditText.setText("");
        if (courseSpinner.getCount() > 0) courseSpinner.setSelection(0);
    }

    @Override
    protected void onDestroy() {
        if (courseCursor != null && !courseCursor.isClosed()) {
            courseCursor.close();
        }
        dbHelper.close();
        super.onDestroy();
    }
}

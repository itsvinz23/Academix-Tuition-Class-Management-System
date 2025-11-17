package com.academix.academix;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class EditStudent extends AppCompatActivity {

    private EditText firstNameEditText, lastNameEditText, contactEditText, usernameEditText, passwordEditText;
    private Spinner courseSpinner;
    private Button updateButton, deleteButton;

    private DBHelper dbHelper;
    private int studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_student);

        dbHelper = new DBHelper(this);

        // Find views
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        contactEditText = findViewById(R.id.contactEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        courseSpinner = findViewById(R.id.courseSpinner);
        updateButton = findViewById(R.id.updateButton);
        deleteButton = findViewById(R.id.deleteButton);

        // Get data from intent
        Intent intent = getIntent();
        studentId = intent.getIntExtra("student_id", -1);
        String fname = intent.getStringExtra("fname");
        String lname = intent.getStringExtra("lname");
        String contact = intent.getStringExtra("contact");
        String course = intent.getStringExtra("coursename");
        String username = intent.getStringExtra("username");
        String password = intent.getStringExtra("password");

        // Set data to views
        firstNameEditText.setText(fname);
        lastNameEditText.setText(lname);
        contactEditText.setText(contact);
        usernameEditText.setText(username);
        passwordEditText.setText(password);

        // Load course spinner with course names from DB
        loadCourses(course);

        // Update button click listener
        updateButton.setOnClickListener(v -> {
            String newContact = contactEditText.getText().toString().trim();
            String newCourse = courseSpinner.getSelectedItem().toString();
            String newUsername = usernameEditText.getText().toString().trim();
            String newPassword = passwordEditText.getText().toString().trim();

            if(newContact.isEmpty() || newUsername.isEmpty() || newPassword.isEmpty()){
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean updated = dbHelper.updateStudent(studentId, fname, lname, newContact, newCourse, newUsername, newPassword);
            if(updated){
                Toast.makeText(this, "Student updated successfully", Toast.LENGTH_SHORT).show();
                finish();  // close this activity and return
            } else {
                Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
            }
        });

        // Delete button click listener with confirmation dialog
        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Student")
                    .setMessage("Are you sure you want to delete this student?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        boolean deleted = dbHelper.deleteStudent(studentId);
                        if(deleted){
                            Toast.makeText(this, "Student deleted", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void loadCourses(String selectedCourse) {
        // Get courses from DB helper
        Cursor cursor = dbHelper.getAllCourses();
        if(cursor == null) return;

        ArrayAdapter<String> adapter;
        ArrayList<String> courseList = new ArrayList<>();

        while(cursor.moveToNext()){
            String courseName = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_COURSE_NAME));
            courseList.add(courseName);
        }
        cursor.close();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courseList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseSpinner.setAdapter(adapter);

        // Set spinner to the selected course
        int position = courseList.indexOf(selectedCourse);
        if(position >= 0){
            courseSpinner.setSelection(position);
        }
    }
}

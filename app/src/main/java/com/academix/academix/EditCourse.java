package com.academix.academix;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditCourse extends AppCompatActivity {

    EditText courseNameEditText, durationEditText, studentsEditText;
    Button updateButton, deleteButton;
    DBHelper dbHelper;
    int courseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_course);

        dbHelper = new DBHelper(this);

        // Initialize views
        courseNameEditText = findViewById(R.id.courseNameEditText);
        durationEditText = findViewById(R.id.durationEditText);
        studentsEditText = findViewById(R.id.studentsEditText);
        updateButton = findViewById(R.id.updateButton);
        deleteButton = findViewById(R.id.deleteButton);

        // Get data from intent
        Intent intent = getIntent();
        courseId = intent.getIntExtra("course_id", -1);
        String courseName = intent.getStringExtra("coursename");
        int duration = intent.getIntExtra("duration", 0);
        int maxStudents = intent.getIntExtra("max_students", 0);

        // Set values to input fields
        courseNameEditText.setText(courseName);
        durationEditText.setText(String.valueOf(duration));
        studentsEditText.setText(String.valueOf(maxStudents));

        // Update button logic
        updateButton.setOnClickListener(v -> {
            String updatedName = courseNameEditText.getText().toString().trim();
            int updatedDuration = Integer.parseInt(durationEditText.getText().toString().trim());
            int updatedMaxStudents = Integer.parseInt(studentsEditText.getText().toString().trim());

            boolean success = dbHelper.updateCourse(courseId, updatedName, updatedDuration, updatedMaxStudents);
            if (success) {
                Toast.makeText(EditCourse.this, "Course updated successfully", Toast.LENGTH_SHORT).show();
                finish(); // go back to AllCourses
            } else {
                Toast.makeText(EditCourse.this, "Failed to update course", Toast.LENGTH_SHORT).show();
            }
        });

        // Delete button logic
        deleteButton.setOnClickListener(v -> {
            boolean success = dbHelper.deleteCourse(courseId);
            if (success) {
                Toast.makeText(EditCourse.this, "Course deleted successfully", Toast.LENGTH_SHORT).show();
                finish(); // go back to AllCourses
            } else {
                Toast.makeText(EditCourse.this, "Failed to delete course", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

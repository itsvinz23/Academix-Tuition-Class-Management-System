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

public class AdminCourses extends AppCompatActivity {

    private EditText courseNameEditText, durationEditText, studentsEditText;
    private Button registerButton;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_courses);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize DBHelper
        dbHelper = new DBHelper(this);

        // Link UI elements
        courseNameEditText = findViewById(R.id.courseNameEditText);
        durationEditText = findViewById(R.id.durationEditText);
        studentsEditText = findViewById(R.id.studentsEditText);
        registerButton = findViewById(R.id.registerButton);

        // Set onClickListener for the register button
        registerButton.setOnClickListener(v -> {
            String courseName = courseNameEditText.getText().toString().trim();
            String durationStr = durationEditText.getText().toString().trim();
            String maxStudentsStr = studentsEditText.getText().toString().trim();

            if (courseName.isEmpty() || durationStr.isEmpty() || maxStudentsStr.isEmpty()) {
                Toast.makeText(AdminCourses.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int duration, maxStudents;

            try {
                duration = Integer.parseInt(durationStr);
                maxStudents = Integer.parseInt(maxStudentsStr);
            } catch (NumberFormatException e) {
                Toast.makeText(AdminCourses.this, "Invalid number input", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = dbHelper.addCourse(courseName, duration, maxStudents);

            if (success) {
                Toast.makeText(AdminCourses.this, "Course registered successfully", Toast.LENGTH_SHORT).show();
                clearFields();
            } else {
                Toast.makeText(AdminCourses.this, "Course name already exists", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearFields() {
        courseNameEditText.setText("");
        durationEditText.setText("");
        studentsEditText.setText("");
    }
}

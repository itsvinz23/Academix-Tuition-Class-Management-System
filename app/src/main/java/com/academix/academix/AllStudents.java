package com.academix.academix;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class AllStudents extends AppCompatActivity {

    private LinearLayout studentsContainer;
    private DBHelper dbHelper;
    private EditText searchEditText;
    private ImageView searchButton;
    private CardView addStudentCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_students);

        studentsContainer = findViewById(R.id.studentsContainer);
        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        addStudentCard = findViewById(R.id.addStudentCard);
        dbHelper = new DBHelper(this);

        loadStudents("");

        // Setup search button with empty check
        searchButton.setOnClickListener(v -> {
            String searchQuery = searchEditText.getText().toString().trim();
            loadStudents(searchQuery);
        });

        // Setup add student button
        addStudentCard.setOnClickListener(v -> {
            startActivity(new Intent(AllStudents.this, AdminStudents.class));
        });
    }

    private void loadStudents(String searchQuery) {
        studentsContainer.removeAllViews();

        // Get students from database based on search query
        Cursor cursor = dbHelper.searchStudents(searchQuery);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                View studentRow = getLayoutInflater().inflate(R.layout.student_row, studentsContainer, false);

                TextView idView = studentRow.findViewById(R.id.studentId);
                TextView nameView = studentRow.findViewById(R.id.studentName);
                TextView usernameView = studentRow.findViewById(R.id.studentUsername);
                TextView courseView = studentRow.findViewById(R.id.studentCourse);

                // Get all student data from cursor
                final int studentId = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_ID));
                final String fname = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_FNAME));
                final String lname = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_LNAME));
                final String contact = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_CONTACT));
                final String coursename = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_COURSE));
                final String username = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_USERNAME));
                final String password = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_PASSWORD));

                idView.setText(String.valueOf(studentId));
                nameView.setText(fname + " " + lname);
                usernameView.setText(username);
                courseView.setText(coursename);

                // Set click listener to pass data to EditStudent activity
                studentRow.setOnClickListener(v -> {
                    Intent intent = new Intent(AllStudents.this, EditStudent.class);
                    intent.putExtra("student_id", studentId);
                    intent.putExtra("fname", fname);
                    intent.putExtra("lname", lname);
                    intent.putExtra("contact", contact);
                    intent.putExtra("coursename", coursename);
                    intent.putExtra("username", username);
                    intent.putExtra("password", password);
                    startActivity(intent);
                });

                studentsContainer.addView(studentRow);

            } while (cursor.moveToNext());

            cursor.close();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning from EditStudent or AddStudentActivity
        loadStudents("");
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}

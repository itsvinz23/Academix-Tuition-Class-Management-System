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

public class AllCourses extends AppCompatActivity {

    private LinearLayout coursesContainer;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_courses);

        coursesContainer = findViewById(R.id.coursesContainer);
        dbHelper = new DBHelper(this);

        EditText searchEditText = findViewById(R.id.searchEditText);
        ImageView searchButton = findViewById(R.id.searchButton);

        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                searchCourses(query);
            } else {
                loadCourses(); // Load all courses if search is empty
            }
        });

    }

    private void searchCourses(String query) {
        coursesContainer.removeAllViews();

        Cursor cursor = dbHelper.searchCourses(query);
        if (cursor.moveToFirst()) {
            do {
                View courseRow = getLayoutInflater().inflate(R.layout.course_row, coursesContainer, false);

                TextView idView = courseRow.findViewById(R.id.courseId);
                TextView nameView = courseRow.findViewById(R.id.courseName);
                TextView durationView = courseRow.findViewById(R.id.courseDuration);
                TextView maxStudentsView = courseRow.findViewById(R.id.courseMaxStudents);

                String id = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_COURSE_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_COURSE_NAME));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_COURSE_DURATION));
                String maxStudents = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_COURSE_MAX_STUDENTS));

                idView.setText(id);
                nameView.setText(name);
                durationView.setText(duration + " months");
                maxStudentsView.setText(maxStudents);

                courseRow.setOnClickListener(v -> {
                    Intent intent = new Intent(AllCourses.this, EditCourse.class);
                    intent.putExtra("course_id", Integer.parseInt(id));
                    intent.putExtra("coursename", name);
                    intent.putExtra("duration", Integer.parseInt(duration));
                    intent.putExtra("max_students", Integer.parseInt(maxStudents));
                    startActivity(intent);
                });

                coursesContainer.addView(courseRow);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void loadCourses() {
        coursesContainer.removeAllViews();

        Cursor cursor = dbHelper.getAllCourses();
        if (cursor.moveToFirst()) {
            do {
                View courseRow = getLayoutInflater().inflate(R.layout.course_row, coursesContainer, false);

                TextView idView = courseRow.findViewById(R.id.courseId);
                TextView nameView = courseRow.findViewById(R.id.courseName);
                TextView durationView = courseRow.findViewById(R.id.courseDuration);
                TextView maxStudentsView = courseRow.findViewById(R.id.courseMaxStudents);

                String id = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_COURSE_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_COURSE_NAME));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_COURSE_DURATION));
                String maxStudents = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_COURSE_MAX_STUDENTS));

                idView.setText(id);
                nameView.setText(name);
                durationView.setText(duration + " months");
                maxStudentsView.setText(maxStudents);

                courseRow.setOnClickListener(v -> {
                    Intent intent = new Intent(AllCourses.this, EditCourse.class);
                    intent.putExtra("course_id", Integer.parseInt(id));
                    intent.putExtra("coursename", name);
                    intent.putExtra("duration", Integer.parseInt(duration));
                    intent.putExtra("max_students", Integer.parseInt(maxStudents));
                    startActivity(intent);
                });

                coursesContainer.addView(courseRow);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCourses();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    public void setAddCourseCard(View v){
        CardView addCourseCard = findViewById(R.id.addCourseCard);
        Intent intent = new Intent(AllCourses.this, AdminCourses.class);
        startActivity(intent);
    }
}

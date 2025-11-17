package com.academix.academix;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import java.util.ArrayList;

public class AllStudentResults extends AppCompatActivity {

    private Spinner batchSpinner, assignmentSpinner;
    private Button showResultsButton;
    private LinearLayout resultsRowsContainer;

    private DBHelper dbHelper;
    private TeacherDBHelper teacherDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_student_results);

        // EdgeToEdge padding (adjusts padding for system bars)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        batchSpinner = findViewById(R.id.batchSpinner);
        assignmentSpinner = findViewById(R.id.assignmentSpinner);
        showResultsButton = findViewById(R.id.searchMarksButton);
        resultsRowsContainer = findViewById(R.id.marksRowsContainer);

        dbHelper = new DBHelper(this);
        teacherDBHelper = new TeacherDBHelper(this);

        loadBatches();
        loadAssignments();

        showResultsButton.setOnClickListener(v -> loadStudentGrades());
    }

    private void loadBatches() {
        ArrayList<String> batchList = new ArrayList<>();
        Cursor cursor = dbHelper.getDistinctCourses();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String courseName = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_COURSE));
                batchList.add(courseName);
            }
            cursor.close();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, batchList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        batchSpinner.setAdapter(adapter);
    }

    private void loadAssignments() {
        ArrayList<String> assignmentList = new ArrayList<>();
        Cursor cursor = teacherDBHelper.getAllAssignments();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String assignmentName = cursor.getString(cursor.getColumnIndexOrThrow("assignment_name"));
                assignmentList.add(assignmentName);
            }
            cursor.close();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, assignmentList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assignmentSpinner.setAdapter(adapter);
    }

    private void loadStudentGrades() {
        // Clear previous results
        resultsRowsContainer.removeAllViews();

        // Show report section (make visible)
        findViewById(R.id.reportSection).setVisibility(View.VISIBLE);

        String selectedBatch = (String) batchSpinner.getSelectedItem();
        Log.d("DEBUG_BATCH", "Selected course: " + selectedBatch);

        String selectedAssignmentName = (String) assignmentSpinner.getSelectedItem();

        if (selectedBatch == null || selectedAssignmentName == null) {
            return; // nothing selected
        }

        // Get assignment ID by name
        int assignmentId = getAssignmentIdByName(selectedAssignmentName);

        Log.d("DEBUG_ASSIGNMENT", "Selected assignment: " + selectedAssignmentName + ", ID: " + assignmentId);

        if (assignmentId == -1) {
            return; // assignment not found
        }

        Cursor cursor = dbHelper.getStudentGradesByCourseAndAssignment(selectedBatch, assignmentId);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int studentId = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_ID));
                String fname = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_FNAME));
                String lname = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_LNAME));
                String username = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_USERNAME));
                int marksIndex = cursor.getColumnIndexOrThrow(TeacherDBHelper.COL_GRADE_MARKS);
                int marks = cursor.isNull(marksIndex) ? -1 : cursor.getInt(marksIndex);
                Log.d("DEBUG_GRADE_DATA", "StudentId: " + studentId + ", Name: " + fname + " " + lname + ", Username: " + username + ", Marks: " + marks);

                addResultRow(studentId, fname, lname, username, marks);
            }
            cursor.close();
        }
    }


    private int getAssignmentIdByName(String assignmentName) {
        Cursor cursor = teacherDBHelper.getAllAssignments();
        int id = -1;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("assignment_name"));
                if (assignmentName.equals(name)) {
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("assignment_id"));
                    break;
                }
            }
            cursor.close();
        }
        return id;
    }

    private void addResultRow(int studentId, String fname, String lname, String username, int marks) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(12, 8, 12, 8);

        TextView nameView = new TextView(this);
        nameView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2));
        nameView.setText(fname + " " + lname);
        nameView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        nameView.setTextColor(0xFF000000);

        TextView usernameView = new TextView(this);
        usernameView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        usernameView.setText(username);
        usernameView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        usernameView.setTextColor(0xFF000000);

        TextView idView = new TextView(this);
        idView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        idView.setText(String.valueOf(studentId));
        idView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        idView.setTextColor(0xFF000000);

        TextView marksView = new TextView(this);
        marksView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        marksView.setText(marks >= 0 ? String.valueOf(marks) : "N/A");
        marksView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        marksView.setTextColor(0xFF000000);

        row.addView(nameView);
        row.addView(usernameView);
        row.addView(idView);
        row.addView(marksView);

        resultsRowsContainer.addView(row);
    }

}

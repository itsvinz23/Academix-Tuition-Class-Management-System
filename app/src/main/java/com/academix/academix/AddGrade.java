package com.academix.academix;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;

public class AddGrade extends AppCompatActivity {

    TextView studentIdText, pdfLinkText, timeRemainingText;
    EditText gradeEditText, noteEditText;
    Button gradeButton;

    int studentId, assignmentId;
    TeacherDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_grade);

        // Inset padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            );
            return insets;
        });

        // ==== Initialize Views ====
        studentIdText = findViewById(R.id.studentIdText);
        pdfLinkText = findViewById(R.id.pdfLinkText);
        timeRemainingText = findViewById(R.id.timeRemainingText);
        gradeEditText = findViewById(R.id.gradeEditText);
        noteEditText = findViewById(R.id.noteEditText);
        gradeButton = findViewById(R.id.gradeButton);

        // ==== Get data from intent ====
        studentId = getIntent().getIntExtra("student_id", -1);
        assignmentId = getIntent().getIntExtra("assignment_id", -1);
        String timeRemaining = getIntent().getStringExtra("time_remaining");

        dbHelper = new TeacherDBHelper(this);

        // ==== Set initial data ====
        studentIdText.setText(String.valueOf(studentId));
        timeRemainingText.setText(timeRemaining != null ? timeRemaining : "--");

        // ==== Pre-fill grade if exists ====
        Cursor gradeCursor = dbHelper.getGrade(studentId, assignmentId);
        if (gradeCursor != null && gradeCursor.moveToFirst()) {
            int existingMarks = gradeCursor.getInt(gradeCursor.getColumnIndexOrThrow("marks"));
            String existingNote = gradeCursor.getString(gradeCursor.getColumnIndexOrThrow("note"));

            gradeEditText.setText(String.valueOf(existingMarks));
            noteEditText.setText(existingNote);
            gradeButton.setText("Update Grade");

            gradeCursor.close();
        }

        // ==== PDF download logic ====
        pdfLinkText.setOnClickListener(v -> {
            Cursor pdfCursor = dbHelper.getReadableDatabase().rawQuery(
                    "SELECT submitted_pdf FROM submitted_assignments WHERE student_id=? AND assignment_id=?",
                    new String[]{String.valueOf(studentId), String.valueOf(assignmentId)}
            );

            if (pdfCursor != null && pdfCursor.moveToFirst()) {
                byte[] pdfBytes = pdfCursor.getBlob(0);
                if (pdfBytes != null) {
                    try {
                        File pdfFile = new File(getExternalFilesDir(null), "submission_" + studentId + "_" + assignmentId + ".pdf");
                        FileOutputStream fos = new FileOutputStream(pdfFile);
                        fos.write(pdfBytes);
                        fos.close();

                        Uri pdfUri = FileProvider.getUriForFile(
                                this,
                                getPackageName() + ".provider",
                                pdfFile
                        );

                        Intent openPdfIntent = new Intent(Intent.ACTION_VIEW);
                        openPdfIntent.setDataAndType(pdfUri, "application/pdf");
                        openPdfIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(openPdfIntent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error opening PDF", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "No PDF found for this submission", Toast.LENGTH_SHORT).show();
                }
                pdfCursor.close();
            } else {
                Toast.makeText(this, "No PDF found for this submission", Toast.LENGTH_SHORT).show();
            }
        });

        // ==== Handle Grade Submit ====
        gradeButton.setOnClickListener(v -> {
            String marksText = gradeEditText.getText().toString().trim();
            String noteText = noteEditText.getText().toString().trim();

            if (marksText.isEmpty()) {
                Toast.makeText(this, "Please enter a mark", Toast.LENGTH_SHORT).show();
                return;
            }

            int marks = Integer.parseInt(marksText);
            if (marks < 0 || marks > 100) {
                Toast.makeText(this, "Marks should be between 0 and 100", Toast.LENGTH_SHORT).show();
                return;
            }

            if (noteText.isEmpty()) {
                noteText = "No comment";
            }

            // ==== Smart insert or update ====
            boolean insertedOrUpdated;

            Cursor check = dbHelper.getGrade(studentId, assignmentId);
            if (check != null && check.moveToFirst()) {
                insertedOrUpdated = dbHelper.updateGrade(studentId, assignmentId, marks, noteText);
                check.close();
            } else {
                insertedOrUpdated = dbHelper.addGrade(studentId, assignmentId, marks, noteText);
            }

            if (insertedOrUpdated) {
                Toast.makeText(this, "Grade submitted successfully", Toast.LENGTH_LONG).show();

                // Redirect back to ViewSubmissions
                Intent intent = new Intent(AddGrade.this, ViewSubmissions.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to submit grade", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

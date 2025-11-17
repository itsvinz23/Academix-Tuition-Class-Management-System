package com.academix.academix;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SubmitAssignment extends AppCompatActivity {

    private int studentId;
    private static final int PICK_PDF_REQUEST = 102;
    private TextView fileNameText;
    private byte[] pdfBytes = null;

    private String assignmentName, dueDate, dueTime;
    private int assignmentId;

    private TextView assignmentNameView, dueDateView, gradeView, submissionView;
    private Button uploadBtn, submitBtn;

    private TeacherDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_submit_assignment);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get data from intent
        Intent intent = getIntent();
        assignmentId = intent.getIntExtra("assignment_id", -1);
        assignmentName = intent.getStringExtra("assignment_name");
        dueDate = intent.getStringExtra("due_date");
        dueTime = intent.getStringExtra("due_time");
        studentId = intent.getIntExtra("student_id", -1);

        dbHelper = new TeacherDBHelper(this);

        // Initialize views
        assignmentNameView = findViewById(R.id.assignmentName);
        dueDateView = findViewById(R.id.assignmentDueDate);
        gradeView = findViewById(R.id.assignmentGrade);
        submissionView = findViewById(R.id.assignmentSubmission);
        uploadBtn = findViewById(R.id.uploadButton);
        submitBtn = findViewById(R.id.submitButton);
        fileNameText = findViewById(R.id.fileTypeText);

        // Set assignment name and due date/time
        assignmentNameView.setText(assignmentName);
        dueDateView.setText(dueDate + " " + dueTime);

        if (isDueExpired(dueDate)) {
            dueDateView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            dueDateView.setTextColor(getResources().getColor(android.R.color.black));
        }

        // Get grade from DB
        Integer marks = dbHelper.getGradeMarks(studentId, assignmentId);
        if (marks != null) {
            gradeView.setText(String.valueOf(marks));
        } else {
            gradeView.setText("Not Graded");
        }

        // Check if student already submitted
        boolean alreadySubmitted = dbHelper.hasSubmittedAssignment(studentId, assignmentId);

        if (alreadySubmitted) {
            submissionView.setText("Submission already uploaded");

            // Hide buttons if past due or already graded
            if (isDueExpired(dueDate) || marks != null) {
                uploadBtn.setVisibility(Button.GONE);
                submitBtn.setVisibility(Button.GONE);
            } else {
                submitBtn.setText("Edit Submission");
            }

        } else {
            submissionView.setText("No file submitted");
            uploadBtn.setVisibility(Button.VISIBLE);
            submitBtn.setVisibility(Button.VISIBLE);
            submitBtn.setText("Submit");
        }

        // Upload PDF
        uploadBtn.setOnClickListener(v -> {
            Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
            fileIntent.setType("application/pdf");
            startActivityForResult(Intent.createChooser(fileIntent, "Select PDF"), PICK_PDF_REQUEST);
        });

        // Submit PDF
        submitBtn.setOnClickListener(v -> {
            if (pdfBytes == null) {
                Toast.makeText(this, "Please upload a PDF file", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

            boolean success = dbHelper.addSubmittedAssignment(String.valueOf(studentId), assignmentId, pdfBytes, currentDate, currentTime);

            if (success) {
                Toast.makeText(this, "Submission uploaded successfully!", Toast.LENGTH_SHORT).show();
                submissionView.setText("Submission already uploaded");
                finish();
            } else {
                Toast.makeText(this, "Failed to submit assignment", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isDueExpired(String dueDateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date due = sdf.parse(dueDateString);
            return due != null && due.before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri pdfUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(pdfUri);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                byte[] tmp = new byte[4096];
                int nRead;
                while ((nRead = inputStream.read(tmp)) != -1) {
                    buffer.write(tmp, 0, nRead);
                }

                pdfBytes = buffer.toByteArray();
                inputStream.close();
                buffer.close();

                String fileName = getFileName(pdfUri);
                fileNameText.setText("Uploaded: " + fileName);
                submissionView.setText("PDF ready to submit");

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to read file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getFileName(Uri uri) {
        String name = "Selected PDF";
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (nameIndex != -1 && cursor.moveToFirst()) {
                name = cursor.getString(nameIndex);
            }
            cursor.close();
        }
        return name;
    }
}

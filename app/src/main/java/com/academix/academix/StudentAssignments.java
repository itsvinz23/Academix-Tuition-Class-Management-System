package com.academix.academix;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;

public class StudentAssignments extends AppCompatActivity {

    private int studentId;

    private LinearLayout assignmentsContainer;
    private TeacherDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_assignments);

        studentId = getIntent().getIntExtra("student_id", -1);

        assignmentsContainer = findViewById(R.id.assignmentsContainer);
        dbHelper = new TeacherDBHelper(this);

        loadAssignments();
    }

    private void loadAssignments() {
        assignmentsContainer.removeAllViews();
        Cursor cursor = dbHelper.getAllAssignments();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                View row = getLayoutInflater().inflate(R.layout.assignment_row, assignmentsContainer, false);

                TextView idView = row.findViewById(R.id.assignmentId);
                TextView nameView = row.findViewById(R.id.assignmentName);
                TextView dueDateTimeView = row.findViewById(R.id.assignmentDueDateTime);
                TextView downloadBtn = row.findViewById(R.id.assignmentDownload);

                int id = cursor.getInt(cursor.getColumnIndexOrThrow("assignment_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("assignment_name"));
                String dueDate = cursor.getString(cursor.getColumnIndexOrThrow("due_date"));
                String dueTime = cursor.getString(cursor.getColumnIndexOrThrow("due_time"));

                idView.setText(String.valueOf(id));
                nameView.setText(name);
                dueDateTimeView.setText(dueDate + " - " + dueTime);

                CardView cardView = row.findViewById(R.id.assignmentCard);
                // Check if due date is expired
                boolean isExpired = isAssignmentExpired(dueDate, dueTime);
                if (isExpired) {

                    cardView.setCardBackgroundColor(android.graphics.Color.parseColor("#ffc0c0"));
                }
                else{
                    cardView.setCardBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"));
                }

                downloadBtn.setOnClickListener(v -> {
                    Cursor pdfCursor = dbHelper.getAssignmentById(id);
                    if (pdfCursor != null && pdfCursor.moveToFirst()) {
                        byte[] pdfBytes = pdfCursor.getBlob(pdfCursor.getColumnIndexOrThrow("assignment_pdf"));
                        if (pdfBytes != null && pdfBytes.length > 0) {
                            try {
                                File pdfFile = new File(getExternalFilesDir(null), name + "_A" + id + ".pdf");
                                FileOutputStream fos = new FileOutputStream(pdfFile);
                                fos.write(pdfBytes);
                                fos.close();

                                Uri pdfUri = FileProvider.getUriForFile(
                                        StudentAssignments.this,
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
                            Toast.makeText(this, "No PDF available for this assignment", Toast.LENGTH_SHORT).show();
                        }
                        pdfCursor.close();
                    }
                });

                row.setOnClickListener(v -> {
                    Intent intent = new Intent(StudentAssignments.this, SubmitAssignment.class);
                    intent.putExtra("assignment_id", id);
                    intent.putExtra("assignment_name", name);
                    intent.putExtra("due_date", dueDate);
                    intent.putExtra("due_time", dueTime);
                    intent.putExtra("student_id", studentId);
                    startActivity(intent);
                });

                assignmentsContainer.addView(row);
            } while (cursor.moveToNext());

            cursor.close();
        } else {
            Toast.makeText(this, "No assignments found", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isAssignmentExpired(String dueDate, String dueTime) {
        try {
            String dueDateTimeString = dueDate + " " + dueTime; // format: yyyy-MM-dd HH:mm
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date dueDateTime = sdf.parse(dueDateTimeString);
            Date currentTime = new Date();

            return currentTime.after(dueDateTime);
        } catch (Exception e) {
            e.printStackTrace();
            return false; // if parsing fails, assume not expired
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadAssignments();
    }
}

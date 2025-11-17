package com.academix.academix;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ViewSubmissions extends AppCompatActivity {

    private LinearLayout submissionsContainer;
    private EditText searchEditText;
    private ImageView searchButton;
    private TeacherDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_submissions);

        submissionsContainer = findViewById(R.id.submissionsContainer);
        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);

        dbHelper = new TeacherDBHelper(this);

        loadSubmissions("");

        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString().trim();
            loadSubmissions(query);
        });
    }

    private void loadSubmissions(String query) {
        submissionsContainer.removeAllViews();

        String sql = "SELECT sa.submit_id, sa.student_id, sa.submit_date, sa.submit_time, " +
                "a.assignment_id, a.assignment_name, a.due_date, a.due_time " +
                "FROM submitted_assignments sa " +
                "JOIN assignments a ON sa.assignment_id = a.assignment_id";

        if (!query.isEmpty()) {
            sql += " WHERE sa.student_id LIKE '%" + query + "%' OR sa.submit_date LIKE '%" + query + "%'";
        }

        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(sql, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                View row = getLayoutInflater().inflate(R.layout.submission_row, submissionsContainer, false);

                TextView idView = row.findViewById(R.id.submissionStudentId);
                TextView assignmentView = row.findViewById(R.id.submissionAssignment);
                TextView timeView = row.findViewById(R.id.submissionTime);
                TextView marksView = row.findViewById(R.id.submissionMarks); // ✅ new
                Button gradeBtn = row.findViewById(R.id.gradeButton);

                String studentId = cursor.getString(cursor.getColumnIndexOrThrow("student_id"));
                String assignmentName = cursor.getString(cursor.getColumnIndexOrThrow("assignment_name"));
                int assignmentId = cursor.getInt(cursor.getColumnIndexOrThrow("assignment_id"));
                String submitDate = cursor.getString(cursor.getColumnIndexOrThrow("submit_date"));
                String submitTime = cursor.getString(cursor.getColumnIndexOrThrow("submit_time"));
                String dueDate = cursor.getString(cursor.getColumnIndexOrThrow("due_date"));
                String dueTime = cursor.getString(cursor.getColumnIndexOrThrow("due_time"));

                idView.setText(studentId);
                assignmentView.setText(assignmentName);

                // Time display logic
                String submitDateTime = submitDate + " " + submitTime;
                String dueDateTime = dueDate + " " + dueTime;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

                try {
                    Date submit = sdf.parse(submitDateTime);
                    Date due = sdf.parse(dueDateTime);

                    if (submit != null && due != null) {
                        long diffMillis = submit.getTime() - due.getTime();
                        long diffDays = Math.abs(TimeUnit.MILLISECONDS.toDays(diffMillis));
                        long diffMinutes = Math.abs(TimeUnit.MILLISECONDS.toMinutes(diffMillis)) % 60;

                        String timeMsg = diffDays + " day" + diffMinutes + " min ";
                        if (submit.after(due)) {
                            timeMsg += "late";
                            timeView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        } else {
                            timeMsg += "early";
                            timeView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        }

                        timeView.setText(timeMsg);
                    }
                } catch (Exception e) {
                    timeView.setText("Invalid time");
                }

                // Get marks and update button + text
                Integer marks = dbHelper.getGradeMarks(Integer.parseInt(studentId), assignmentId);
                if (marks != null) {
                    marksView.setText(String.valueOf(marks));
                    gradeBtn.setText("Edit");
                } else {
                    marksView.setText("--");
                    gradeBtn.setText("Grade");
                }

                // Open PDF
                assignmentView.setOnClickListener(v -> {
                    Cursor pdfCursor = dbHelper.getReadableDatabase().rawQuery(
                            "SELECT submitted_pdf FROM submitted_assignments WHERE student_id=? AND assignment_id=?",
                            new String[]{studentId, String.valueOf(assignmentId)}
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
                                Toast.makeText(this, "Error opening PDF", Toast.LENGTH_SHORT).show();
                            }
                        }
                        pdfCursor.close();
                    }
                });

                // Open grade/edit screen
                gradeBtn.setOnClickListener(v -> {
                    String timeRemaining = calculateTimeRemaining(dueDate, dueTime);

                    Intent intent = new Intent(ViewSubmissions.this, AddGrade.class);
                    intent.putExtra("student_id", Integer.parseInt(studentId));
                    intent.putExtra("assignment_id", assignmentId);
                    intent.putExtra("time_remaining", timeRemaining);
                    startActivity(intent);
                });

                submissionsContainer.addView(row);
            } while (cursor.moveToNext());

            cursor.close();
        }
    }

    private String calculateTimeRemaining(String dueDate, String dueTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            Date now = new Date();
            Date due = sdf.parse(dueDate + " " + dueTime);
            if (due == null) return "--";

            long diffMillis = due.getTime() - now.getTime();
            if (diffMillis <= 0) return "Deadline passed";

            long days = TimeUnit.MILLISECONDS.toDays(diffMillis);
            long hours = TimeUnit.MILLISECONDS.toHours(diffMillis) % 24;
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis) % 60;

            return String.format("%d days %d hours %d minutes", days, hours, minutes);
        } catch (Exception e) {
            return "--";
        }
    }
}

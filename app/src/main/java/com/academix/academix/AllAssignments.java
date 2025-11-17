package com.academix.academix;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AllAssignments extends AppCompatActivity {

    private LinearLayout assignmentsContainer;
    private TeacherDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_assignments);

        assignmentsContainer = findViewById(R.id.assignmentsContainer);
        dbHelper = new TeacherDBHelper(this);

        loadAssignments();

        // Navigate to AddAssignments screen
        CardView addCard = findViewById(R.id.addAssignmentCard);
        addCard.setOnClickListener(v -> {
            Intent intent = new Intent(AllAssignments.this, AddAssignments.class);
            startActivity(intent);
        });
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
                CardView cardView = row.findViewById(R.id.assignmentCard);

                int id = cursor.getInt(cursor.getColumnIndexOrThrow("assignment_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("assignment_name"));
                String dueDate = cursor.getString(cursor.getColumnIndexOrThrow("due_date"));
                String dueTime = cursor.getString(cursor.getColumnIndexOrThrow("due_time"));

                idView.setText(String.valueOf(id));
                nameView.setText(name);
                dueDateTimeView.setText(dueDate + " - " + dueTime);

                // Set card background color based on expiry
                if (isAssignmentExpired(dueDate, dueTime)) {
                    cardView.setCardBackgroundColor(android.graphics.Color.parseColor("#ffc0c0")); // dull red for expired
                } else {
                    cardView.setCardBackgroundColor(android.graphics.Color.parseColor("#FFFFFF")); // white
                }

                // Download PDF button logic
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
                                        AllAssignments.this,
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

                // Navigate to EditAssignment on card click with all details passed
                cardView.setOnClickListener(v -> {
                    Intent editIntent = new Intent(AllAssignments.this, EditAssignment.class);
                    editIntent.putExtra("assignment_id", id);
                    editIntent.putExtra("assignment_name", name);
                    editIntent.putExtra("due_date", dueDate);
                    editIntent.putExtra("due_time", dueTime);
                    startActivity(editIntent);
                });

                assignmentsContainer.addView(row);
            } while (cursor.moveToNext());

            cursor.close();
        }
    }


    private boolean isAssignmentExpired(String dueDate, String dueTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date dueDateTime = sdf.parse(dueDate + " " + dueTime);
            Date now = new Date();
            return now.after(dueDateTime);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAssignments();
    }
}

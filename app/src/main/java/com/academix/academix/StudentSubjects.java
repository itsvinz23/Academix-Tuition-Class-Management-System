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
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;

public class StudentSubjects extends AppCompatActivity {

    private LinearLayout subjectsContainer;
    private TeacherDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_subjects);

        subjectsContainer = findViewById(R.id.subjectsContainer);
        dbHelper = new TeacherDBHelper(this);

        loadStudyMaterials();
    }

    private void loadStudyMaterials() {
        subjectsContainer.removeAllViews();
        Cursor cursor = dbHelper.getAllSubjects();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                View subjectRow = getLayoutInflater().inflate(R.layout.subject_row, subjectsContainer, false);

                TextView idView = subjectRow.findViewById(R.id.subjectId);
                TextView nameView = subjectRow.findViewById(R.id.subjectName);
                TextView downloadText = subjectRow.findViewById(R.id.subjectDownload);

                int id = cursor.getInt(cursor.getColumnIndexOrThrow("subject_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("subject_name"));

                idView.setText(String.valueOf(id));
                nameView.setText(name);

                downloadText.setOnClickListener(v -> {
                    Cursor pdfCursor = dbHelper.getSubjectById(id);
                    if (pdfCursor != null && pdfCursor.moveToFirst()) {
                        byte[] pdfBytes = pdfCursor.getBlob(pdfCursor.getColumnIndexOrThrow("course_material"));

                        if (pdfBytes != null && pdfBytes.length > 0) {
                            try {
                                File pdfFile = new File(getExternalFilesDir(null), name + "_" + id + ".pdf");
                                FileOutputStream fos = new FileOutputStream(pdfFile);
                                fos.write(pdfBytes);
                                fos.close();

                                Uri pdfUri = FileProvider.getUriForFile(
                                        StudentSubjects.this,
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
                            Toast.makeText(this, "No PDF available for this subject", Toast.LENGTH_SHORT).show();
                        }

                        pdfCursor.close();
                    }
                });

                subjectsContainer.addView(subjectRow);
            } while (cursor.moveToNext());

            cursor.close();
        } else {
            Toast.makeText(this, "No subjects found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStudyMaterials();
    }
}

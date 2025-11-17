package com.academix.academix;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ResultsReport extends AppCompatActivity {

    private Spinner batchSpinner, studentSpinner;
    private Button showResultsButton;
    private LinearLayout reportSection, resultsRowsContainer;

    private DBHelper dbHelper;
    private TeacherDBHelper teacherDBHelper;

    private ArrayList<String> courseList = new ArrayList<>();
    private ArrayList<String> studentNames = new ArrayList<>();
    private ArrayList<Integer> studentIds = new ArrayList<>();

    private int selectedStudentId = -1;
    private String selectedStudentName = "";
    private static final int CREATE_PDF_REQUEST_CODE = 1010;
    private List<String> pdfLines;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results_report);

        dbHelper = new DBHelper(this);
        teacherDBHelper = new TeacherDBHelper(this);

        batchSpinner = findViewById(R.id.batchSpinner);
        studentSpinner = findViewById(R.id.studentSpinner);
        showResultsButton = findViewById(R.id.showResultsButton);
        reportSection = findViewById(R.id.reportSection);
        resultsRowsContainer = findViewById(R.id.resultsRowsContainer);

        loadCourses();

        batchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedCourse = courseList.get(i);
                loadStudentsForCourse(selectedCourse);
            }
            @Override public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        studentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedStudentId = studentIds.get(i);
                selectedStudentName = studentNames.get(i);
            }
            @Override public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        showResultsButton.setOnClickListener(v -> {
            if (selectedStudentId != -1) {
                displayResults(selectedStudentId);
            } else {
                Toast.makeText(this, "Please select a student.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCourses() {
        Cursor cursor = dbHelper.getDistinctCourses();
        courseList.clear();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                courseList.add(cursor.getString(0));
            } while (cursor.moveToNext());
            cursor.close();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, courseList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        batchSpinner.setAdapter(adapter);
    }

    private void loadStudentsForCourse(String course) {
        Cursor cursor = dbHelper.getStudentsByCourse(course);
        studentNames.clear();
        studentIds.clear();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("student_id"));
                String fname = cursor.getString(cursor.getColumnIndexOrThrow("fname"));
                String lname = cursor.getString(cursor.getColumnIndexOrThrow("lname"));
                studentIds.add(id);
                studentNames.add(fname + " " + lname);
            } while (cursor.moveToNext());
            cursor.close();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, studentNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        studentSpinner.setAdapter(adapter);
    }

    private void displayResults(int studentId) {
        resultsRowsContainer.removeAllViews();
        Cursor cursor = teacherDBHelper.getGradesWithAssignmentsForStudent(studentId);

        pdfLines = new ArrayList<>();
        pdfLines.add("Student Name: " + selectedStudentName);
        pdfLines.add("Course: " + batchSpinner.getSelectedItem().toString());
        pdfLines.add("Results Report");
        pdfLines.add("");

        if (cursor != null && cursor.moveToFirst()) {
            reportSection.setVisibility(View.VISIBLE);
            do {
                String subject = cursor.getString(0);
                int marks = cursor.getInt(1);

                pdfLines.add(subject + " - " + marks);

                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(12, 8, 12, 8);

                TextView subjectText = new TextView(this);
                subjectText.setText(subject);
                subjectText.setTextColor(Color.BLACK);
                subjectText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                subjectText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                TextView gradeText = new TextView(this);
                gradeText.setText(String.valueOf(marks));
                gradeText.setTextColor(Color.BLACK);
                gradeText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                gradeText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                row.addView(subjectText);
                row.addView(gradeText);
                resultsRowsContainer.addView(row);
            } while (cursor.moveToNext());
            cursor.close();

            new AlertDialog.Builder(this)
                    .setTitle("Generate Report?")
                    .setMessage("Would you like to download this results report as PDF?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                        intent.setType("application/pdf");
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.putExtra(Intent.EXTRA_TITLE, "Results_" + selectedStudentName.replace(" ", "_") + ".pdf");
                        startActivityForResult(intent, CREATE_PDF_REQUEST_CODE);
                    })
                    .setNegativeButton("No", null)
                    .show();

        } else {
            reportSection.setVisibility(View.GONE);
            Toast.makeText(this, "No grades found for this student.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_PDF_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri == null || pdfLines == null) {
                Toast.makeText(this, "Invalid file path", Toast.LENGTH_SHORT).show();
                return;
            }

            PdfDocument document = new PdfDocument();
            int pageWidth = 595;
            int pageHeight = 842;
            int margin = 40;
            int y = margin + 40;

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(6);
            paint.setColor(Color.parseColor("#4D9BE6"));
            canvas.drawRect(margin, margin, pageWidth - margin, pageHeight - margin, paint);

            paint.setStyle(Paint.Style.FILL);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD));
            paint.setTextSize(22f);
            paint.setColor(Color.parseColor("#0F1A3A"));
            canvas.drawText("Results Report", centerTextX("Results Report", paint, pageWidth), y, paint);
            y += 50;

            Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.academix_login);
            Bitmap scaledLogo = Bitmap.createScaledBitmap(logo, 100, 100, true);
            canvas.drawBitmap(scaledLogo, (pageWidth / 2) - 50, y, null);
            y += 120;

            paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
            paint.setTextSize(14f);
            paint.setColor(Color.BLACK);
            for (int i = 0; i < 3 && i < pdfLines.size(); i++) {
                canvas.drawText(pdfLines.get(i), margin + 20, y, paint);
                y += 25;
            }

            y += 30;

            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setTextSize(16f);
            paint.setColor(Color.WHITE);
            canvas.drawRect(margin + 20, y - 25, pageWidth - margin - 20, y + 15, paint);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setColor(Color.parseColor("#0F1A3A"));
            canvas.drawText("Subject", pageWidth / 3, y, paint);
            canvas.drawText("Marks", 2 * pageWidth / 3, y, paint);
            y += 30;

            paint.setTypeface(Typeface.DEFAULT);
            paint.setTextSize(14f);
            paint.setTextAlign(Paint.Align.CENTER);

            for (int i = 4; i < pdfLines.size(); i++) {
                String[] parts = pdfLines.get(i).split(" - ");
                if (parts.length >= 2) {
                    String subject = parts[0].trim();
                    String marks = parts[1].trim();

                    canvas.drawText(subject, pageWidth / 3, y, paint);
                    canvas.drawText(marks, 2 * pageWidth / 3, y, paint);
                    y += 25;
                }
            }

            document.finishPage(page);

            try (FileOutputStream fos = (FileOutputStream) getContentResolver().openOutputStream(uri)) {
                document.writeTo(fos);
                Toast.makeText(this, "PDF saved successfully!", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(this, "Error saving PDF", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } finally {
                document.close();
            }
        }
    }

    private int centerTextX(String text, Paint paint, int pageWidth) {
        float textWidth = paint.measureText(text);
        return (int) ((pageWidth - textWidth) / 2);
    }
}

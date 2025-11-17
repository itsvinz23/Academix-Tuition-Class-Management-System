package com.academix.academix;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.widget.LinearLayout.LayoutParams;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.net.Uri;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class AttendanceReport extends AppCompatActivity {

    Spinner batchSpinner, studentSpinner, monthSpinner;
    Button showHistoryButton;
    LinearLayout reportSection, attendanceRowsContainer;
    ImageView reportImage;
    TextView reportTitle;

    DBHelper dbHelper;
    TeacherDBHelper teacherDBHelper;

    Map<String, Integer> monthMap = new LinkedHashMap<>();
    List<String> batchList = new ArrayList<>();
    List<StudentModel> currentStudents = new ArrayList<>();

    // PDF saving fields
    private static final int CREATE_PDF_REQUEST_CODE = 1001;
    private List<String> pdfLines;
    private StudentModel currentStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_report);

        batchSpinner = findViewById(R.id.batchSpinner);
        studentSpinner = findViewById(R.id.studentSpinner);
        monthSpinner = findViewById(R.id.monthSpinner);
        showHistoryButton = findViewById(R.id.showHistoryButton);
        reportSection = findViewById(R.id.reportSection);
        attendanceRowsContainer = findViewById(R.id.attendanceRowsContainer);
        reportImage = findViewById(R.id.reportImage);
        reportTitle = findViewById(R.id.reportTitle);

        dbHelper = new DBHelper(this);
        teacherDBHelper = new TeacherDBHelper(this);

        setupMonthSpinner();
        loadBatches();

        batchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadStudents(batchList.get(position));
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        showHistoryButton.setOnClickListener(v -> displayReport());
    }

    private void setupMonthSpinner() {
        monthMap.put("January", 1);
        monthMap.put("February", 2);
        monthMap.put("March", 3);
        monthMap.put("April", 4);
        monthMap.put("May", 5);
        monthMap.put("June", 6);
        monthMap.put("July", 7);
        monthMap.put("August", 8);
        monthMap.put("September", 9);
        monthMap.put("October", 10);
        monthMap.put("November", 11);
        monthMap.put("December", 12);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, new ArrayList<>(monthMap.keySet()));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(adapter);
    }

    private void loadBatches() {
        batchList.clear();
        Cursor cursor = dbHelper.getDistinctCourses();
        while (cursor.moveToNext()) {
            batchList.add(cursor.getString(0));
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, batchList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        batchSpinner.setAdapter(adapter);
    }

    private void loadStudents(String selectedCourse) {
        currentStudents.clear();
        Cursor cursor = dbHelper.getStudentsByCourse(selectedCourse);
        List<String> studentNames = new ArrayList<>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_ID));
            String fname = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_FNAME));
            String lname = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_LNAME));
            String username = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_USERNAME));
            String contact = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_CONTACT));
            String course = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_COURSE));

            StudentModel s = new StudentModel(id, fname + " " + lname, username, contact, course);
            currentStudents.add(s);
            studentNames.add(s.name + " (" + s.username + ")");
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, studentNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        studentSpinner.setAdapter(adapter);
    }

    private void displayReport() {
        if (currentStudents.isEmpty()) {
            Toast.makeText(this, "No student in selected batch", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedIndex = studentSpinner.getSelectedItemPosition();
        if (selectedIndex < 0 || selectedIndex >= currentStudents.size()) {
            Toast.makeText(this, "Select a valid student", Toast.LENGTH_SHORT).show();
            return;
        }

        StudentModel student = currentStudents.get(selectedIndex);
        String selectedMonth = monthSpinner.getSelectedItem().toString();
        int selectedMonthNum = monthMap.get(selectedMonth);
        int year = Calendar.getInstance().get(Calendar.YEAR);

        Calendar cal = Calendar.getInstance();
        cal.set(year, selectedMonthNum - 1, 1);
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        attendanceRowsContainer.removeAllViews();
        reportSection.setVisibility(View.VISIBLE);
        reportTitle.setText("Attendance for " + selectedMonth + " " + year);

        List<String> reportLines = new ArrayList<>();
        reportLines.add("Full Name - " + student.name);
        reportLines.add("Username - " + student.username);
        reportLines.add("Course - " + student.course);
        reportLines.add("Contact -  " + student.contact);
        reportLines.add("Attendance Report - " + selectedMonth + " " + year);
        reportLines.add("");

        for (int day = 1; day <= daysInMonth; day++) {
            cal.set(Calendar.DAY_OF_MONTH, day);
            String dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, selectedMonthNum, day);
            boolean present = teacherDBHelper.hasAnyAttendanceForDate(student.id, dateStr);
            String status = present ? "Present" : "Absent";
            reportLines.add(dateStr + " - " + status);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(12, 8, 12, 8);
            row.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            TextView dateView = new TextView(this);
            dateView.setText(dateStr);
            dateView.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
            dateView.setTextColor(Color.BLACK);
            dateView.setGravity(Gravity.CENTER);

            TextView statusView = new TextView(this);
            statusView.setText(status);
            statusView.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
            statusView.setGravity(Gravity.CENTER);
            statusView.setTypeface(null, Typeface.BOLD);
            statusView.setTextColor(status.equals("Present") ? Color.parseColor("#007E33") : Color.parseColor("#D50000"));

            row.addView(dateView);
            row.addView(statusView);

            attendanceRowsContainer.addView(row);
        }

        // PDF download confirmation
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Generate Report?");
        builder.setMessage("Would you like to download this attendance as PDF?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            pdfLines = reportLines;
            currentStudent = student;

            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.setType("application/pdf");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_TITLE, "Attendance_" + student.username + ".pdf");
            startActivityForResult(intent, CREATE_PDF_REQUEST_CODE);
        });
        builder.setNegativeButton("No", null);
        builder.show();
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
            int pageWidth = 595; // A4 size width approx
            int margin = 40;
            int pageHeight = 850 + pdfLines.size() * 25;

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);

            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();

            // ===== Draw thick border box =====
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(6); // thick border
            paint.setColor(Color.parseColor("#4D9BE6")); // blue border color
            canvas.drawRect(margin, margin, pageWidth - margin, pageHeight - margin, paint);

            int y = margin + 40;

            // ===== Header: Title =====
            paint.setStyle(Paint.Style.FILL);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD));
            paint.setTextSize(22f);
            paint.setColor(Color.parseColor("#0F1A3A")); // dark blue
            canvas.drawText("Attendance History", centerTextX("Attendance History", paint, pageWidth), y, paint);

            y += 50;

            // ===== Logo Image (ic_login) =====
            Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.academix_login);
            Bitmap scaledLogo = Bitmap.createScaledBitmap(logo, 100, 100, true);
            canvas.drawBitmap(scaledLogo, (pageWidth / 2) - 50, y, null);

            y += 120;

            // ===== Student details =====
            paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
            paint.setTextSize(14f);
            paint.setColor(Color.BLACK);
            for (int i = 0; i < 5 && i < pdfLines.size(); i++) {
                canvas.drawText(pdfLines.get(i), margin + 20, y, paint);
                y += 25;
            }

            y += 30;

            // ===== Table Header =====
            paint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD));
            paint.setTextSize(16f);
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);

            int tableLeft = margin + 20;
            int tableRight = pageWidth - margin - 20;
            int tableHeaderHeight = 40;
            // Draw header background rectangle
            canvas.drawRect(tableLeft, y - 30, tableRight, y + tableHeaderHeight - 30, paint);

            // Draw header text centered
            paint.setColor(Color.parseColor("#0F1A3A"));
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Date", tableLeft + (tableRight - tableLeft) / 4, y, paint);
            canvas.drawText("Status", tableLeft + 3 * (tableRight - tableLeft) / 4, y, paint);

            y += 35;

            // ===== Table Rows =====
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(14f);

            int rowHeight = 30;
            int col1X = tableLeft + (tableRight - tableLeft) / 4;
            int col2X = tableLeft + 3 * (tableRight - tableLeft) / 4;

            for (int i = 5; i < pdfLines.size(); i++) {
                String line = pdfLines.get(i);
                String[] parts = line.split(" - ");
                String date = parts[0].trim();
                String status = parts.length > 1 ? parts[1].trim() : "";

                // Alternate row background color
                if ((i - 5) % 2 == 0) {
                    paint.setColor(Color.parseColor("#F0F0F0"));
                    canvas.drawRect(tableLeft, y - 20, tableRight, y + rowHeight - 20, paint);
                }

                paint.setColor(Color.BLACK);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(date, col1X, y, paint);

                if ("Present".equalsIgnoreCase(status)) {
                    paint.setColor(Color.parseColor("#007E33"));
                } else if ("Absent".equalsIgnoreCase(status)) {
                    paint.setColor(Color.parseColor("#D50000"));
                } else {
                    paint.setColor(Color.BLACK);
                }
                canvas.drawText(status, col2X, y, paint);

                y += rowHeight;
            }

            y += 40;

            // ===== Signature line at bottom left =====
            paint.setColor(Color.BLACK);
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTextSize(14f);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1);
            int sigLineStartX = margin + 20;
            int sigLineEndX = margin + 200;
            int sigLineY = y;
            canvas.drawLine(sigLineStartX, sigLineY, sigLineEndX, sigLineY, paint);

            paint.setStyle(Paint.Style.FILL);
            canvas.drawText("Signature", sigLineStartX, sigLineY + 20, paint);

            // ===== Page number at bottom right =====
            paint.setTextSize(12f);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
            paint.setColor(Color.DKGRAY);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText("Page 1", pageWidth - margin, pageHeight - 20, paint);

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

    // Helper method to center text horizontally on the page
    private int centerTextX(String text, Paint paint, int pageWidth) {
        float textWidth = paint.measureText(text);
        return (int) ((pageWidth - textWidth) / 2);
    }

    static class StudentModel {
        int id;
        String name;
        String username;
        String contact;
        String course;

        StudentModel(int id, String name, String username, String contact, String course) {
            this.id = id;
            this.name = name;
            this.username = username;
            this.contact = contact;
            this.course = course;
        }
    }
}

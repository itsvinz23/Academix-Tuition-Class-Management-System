package com.academix.academix;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TeacherAttendance extends AppCompatActivity {

    private ImageView qrCodeImage;
    private Button generateQRBtn;
    private LinearLayout attendanceContainer;
    private TeacherDBHelper dbHelper;
    private int teacherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_teacher_attendance);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent intent = getIntent();
        teacherId = intent.getIntExtra("teacher_id", -1);

        if (teacherId == -1) {
            Toast.makeText(this, "Teacher ID missing!", Toast.LENGTH_LONG).show();
            finish(); // Prevent app crash
            return;
        }
        // === Initialize views and DB ===
        qrCodeImage = findViewById(R.id.qrCodeImage);
        generateQRBtn = findViewById(R.id.generateQRBtn);
        attendanceContainer = findViewById(R.id.attendanceContainer);
        dbHelper = new TeacherDBHelper(this);

        // === Load QR code if saved and valid for today ===
        loadSavedQRCode();

        // === Generate QR Code on Button Click ===
        generateQRBtn.setOnClickListener(view -> generateQRCode());

        // === Load today's attendance ===
        loadTodayAttendance();
    }
    private void loadSavedQRCode() {
        SharedPreferences prefs = getSharedPreferences("qr_prefs", MODE_PRIVATE);
        String savedQrBase64 = prefs.getString("qr_base64", null);
        String savedDate = prefs.getString("qr_date", null);
        String today = getTodayDate();

        if (savedQrBase64 != null && savedDate != null && savedDate.equals(today)) {
            try {
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.encodeBitmap(
                        savedQrBase64,
                        BarcodeFormat.QR_CODE,
                        400,
                        400
                );
                qrCodeImage.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
    }
    private void generateQRCode() {
        String today = getTodayDate();
        String rawData = "attendance|" + teacherId + "|" + today;
        String encodedData = Base64.encodeToString(rawData.getBytes(), Base64.NO_WRAP);

        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(
                    encodedData,
                    BarcodeFormat.QR_CODE,
                    400,
                    400
            );
            qrCodeImage.setImageBitmap(bitmap);

            // Save QR code data and date in SharedPreferences
            SharedPreferences prefs = getSharedPreferences("qr_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("qr_base64", encodedData);
            editor.putString("qr_date", today);
            editor.apply();

            Toast.makeText(this, "QR Code generated for today", Toast.LENGTH_SHORT).show();

        } catch (WriterException e) {
            Toast.makeText(this, "Error generating QR code", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    private void loadTodayAttendance() {
        attendanceContainer.removeAllViews();

        String today = getTodayDate();
        Cursor cursor = dbHelper.getTodayAttendance(today, teacherId);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                View row = getLayoutInflater().inflate(R.layout.attendance_row, attendanceContainer, false);

                TextView idText = row.findViewById(R.id.textStudentId);
                TextView timeText = row.findViewById(R.id.textTimestamp);

                String studentId = cursor.getString(cursor.getColumnIndexOrThrow("student_id"));
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"));

                idText.setText(studentId);
                timeText.setText(timestamp);

                attendanceContainer.addView(row);
            } while (cursor.moveToNext());

            cursor.close();
        } else {
            Toast.makeText(this, "No attendance records for today", Toast.LENGTH_SHORT).show();
        }
    }
    private String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
}
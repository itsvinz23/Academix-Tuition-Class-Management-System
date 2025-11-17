package com.academix.academix;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StudentAttendance extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 100;

    private Button scanBtn;
    private TextView statusText;

    private TeacherDBHelper dbHelper;
    private int studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_attendance);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views and db helper
        scanBtn = findViewById(R.id.scanBtn);
        statusText = findViewById(R.id.statusText);
        dbHelper = new TeacherDBHelper(this);

        // TODO: get studentId from Intent or SharedPreferences
        studentId = getIntent().getIntExtra("student_id", -1);
        if (studentId == -1) {
            Toast.makeText(this, "Student ID not found!", Toast.LENGTH_LONG).show();
            finish();
        }

        scanBtn.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
            } else {
                startQRScanner();
            }
        });
    }

    private void startQRScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Scan teacher's attendance QR code");
        integrator.setBeepEnabled(true);
        integrator.setOrientationLocked(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    // Handle permission result for camera
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startQRScanner();
            } else {
                Toast.makeText(this, "Camera permission is required to scan QR code", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // Handle QR scan result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
                statusText.setText("Scan cancelled");
                statusText.setVisibility(TextView.VISIBLE);
            } else {
                // Process scanned QR code
                handleScannedQRCode(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleScannedQRCode(String scannedData) {
        // QR code should be Base64 encoded string of "attendance|teacherId|yyyy-MM-dd"
        try {
            byte[] decodedBytes = android.util.Base64.decode(scannedData, android.util.Base64.NO_WRAP);
            String decodedString = new String(decodedBytes);

            // Expected format: attendance|teacherId|date
            String[] parts = decodedString.split("\\|");
            if (parts.length != 3 || !parts[0].equals("attendance")) {
                statusText.setText("Invalid QR code");
                statusText.setVisibility(TextView.VISIBLE);
                return;
            }

            int teacherId = Integer.parseInt(parts[1]);
            String date = parts[2];

            String today = getTodayDate();

            if (!date.equals(today)) {
                statusText.setText("QR code expired");
                statusText.setVisibility(TextView.VISIBLE);
                return;
            }

            // Check if attendance already marked
            if (dbHelper.hasAttendanceForDate(studentId, teacherId, today)) {
                statusText.setText("Your attendance is already marked for today.");
                statusText.setVisibility(TextView.VISIBLE);
                return;
            }

            // Add attendance to DB
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            boolean added = dbHelper.addAttendance(studentId, teacherId, timestamp);
            if (added) {
                statusText.setText("Attendance marked successfully!");
                statusText.setVisibility(TextView.VISIBLE);
            } else {
                statusText.setText("Failed to mark attendance. Try again.");
                statusText.setVisibility(TextView.VISIBLE);
            }

        } catch (IllegalArgumentException e) {
            statusText.setText("Invalid QR code format");
            statusText.setVisibility(TextView.VISIBLE);
        } catch (Exception e) {
            statusText.setText("Error processing QR code");
            statusText.setVisibility(TextView.VISIBLE);
            e.printStackTrace();
        }
    }

    private String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
}

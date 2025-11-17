package com.academix.academix;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Calendar;

public class EditAssignment extends AppCompatActivity {

    private static final int PICK_PDF_REQUEST = 101;

    private EditText assignmentNameEditText, dueDateEditText, dueTimeEditText;
    private TextView pdfFileNameText;
    private Button uploadPdfButton, editAssignmentButton;

    private byte[] pdfBytes = null;
    private TeacherDBHelper dbHelper;
    private int assignmentId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_assignment);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        assignmentNameEditText = findViewById(R.id.assignmentNameEditText);
        dueDateEditText = findViewById(R.id.dueDateEditText);
        dueTimeEditText = findViewById(R.id.dueTimeEditText);
        pdfFileNameText = findViewById(R.id.pdfFileNameText);

        uploadPdfButton = findViewById(R.id.uploadPdfButton);
        editAssignmentButton = findViewById(R.id.editAssignmentButton);

        dbHelper = new TeacherDBHelper(this);

        // Get assignmentId from Intent extras
        assignmentId = getIntent().getIntExtra("assignment_id", -1);
        if (assignmentId == -1) {
            Toast.makeText(this, "Invalid assignment ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadAssignmentDetails(assignmentId);

        dueDateEditText.setOnClickListener(v -> showDatePicker());
        dueTimeEditText.setOnClickListener(v -> showTimePicker());
        uploadPdfButton.setOnClickListener(v -> openFilePicker());
        editAssignmentButton.setOnClickListener(v -> updateAssignment());
    }

    private void loadAssignmentDetails(int id) {
        Cursor cursor = dbHelper.getAssignmentById(id);
        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("assignment_name"));
            String dueDate = cursor.getString(cursor.getColumnIndexOrThrow("due_date"));
            String dueTime = cursor.getString(cursor.getColumnIndexOrThrow("due_time"));
            byte[] pdfData = cursor.getBlob(cursor.getColumnIndexOrThrow("assignment_pdf"));

            assignmentNameEditText.setText(name);
            dueDateEditText.setText(dueDate);
            dueTimeEditText.setText(dueTime);

            if (pdfData != null && pdfData.length > 0) {
                pdfBytes = pdfData;
                pdfFileNameText.setText("Existing PDF file");
            } else {
                pdfFileNameText.setText("No file selected");
            }

            cursor.close();
        } else {
            Toast.makeText(this, "Assignment not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    dueDateEditText.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    String time = String.format("%02d:%02d", hourOfDay, minute);
                    dueTimeEditText.setText(time);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        ).show();
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), PICK_PDF_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri pdfUri = data.getData();

            try {
                InputStream inputStream = getContentResolver().openInputStream(pdfUri);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                int nRead;
                byte[] tmp = new byte[4096];
                while ((nRead = inputStream.read(tmp)) != -1) {
                    buffer.write(tmp, 0, nRead);
                }
                pdfBytes = buffer.toByteArray();

                inputStream.close();
                buffer.close();

                String fileName = getFileName(pdfUri);
                pdfFileNameText.setText(fileName);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to read selected file", Toast.LENGTH_SHORT).show();
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

    private void updateAssignment() {
        String name = assignmentNameEditText.getText().toString().trim();
        String dueDate = dueDateEditText.getText().toString().trim();
        String dueTime = dueTimeEditText.getText().toString().trim();

        if (name.isEmpty() || dueDate.isEmpty() || dueTime.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pdfBytes == null) {
            Toast.makeText(this, "Please upload a PDF file", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = dbHelper.updateAssignment(assignmentId, name, dueDate, dueTime, pdfBytes);

        if (success) {
            Toast.makeText(this, "Assignment updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to update assignment", Toast.LENGTH_SHORT).show();
        }
    }
}

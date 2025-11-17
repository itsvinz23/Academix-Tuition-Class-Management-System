package com.academix.academix;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class AddSubject extends AppCompatActivity {

    private EditText subjectNameEditText;
    private TextView pdfFileNameText;
    private Button uploadPdfButton, registerButton;

    private Uri selectedPdfUri;
    private byte[] pdfBytes;

    private TeacherDBHelper teacherDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_subject);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        teacherDBHelper = new TeacherDBHelper(this);

        subjectNameEditText = findViewById(R.id.subjectNameEditText);
        pdfFileNameText = findViewById(R.id.pdfFileNameText);
        uploadPdfButton = findViewById(R.id.uploadPdfButton);
        registerButton = findViewById(R.id.registerButton);

        uploadPdfButton.setOnClickListener(v -> openPdfPicker());
        registerButton.setOnClickListener(v -> saveSubject());
    }

    // Register launcher for picking PDF file
    private final ActivityResultLauncher<String> pdfPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedPdfUri = uri;
                    String filename = getFileName(uri);
                    pdfFileNameText.setText(filename != null ? filename : "Selected PDF");
                    readPdfBytes(uri);
                }
            }
    );

    private void openPdfPicker() {
        pdfPickerLauncher.launch("application/pdf");
    }

    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                Log.e("AddSubject", "Error getting file name", e);
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    private void readPdfBytes(Uri uri) {
        try {
            ContentResolver resolver = getContentResolver();
            InputStream inputStream = resolver.openInputStream(uri);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            if (inputStream != null) {
                int nRead;
                byte[] data = new byte[16384];
                while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                pdfBytes = buffer.toByteArray();
                inputStream.close();
            } else {
                pdfBytes = null;
                Toast.makeText(this, "Unable to read selected file", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("AddSubject", "Error reading PDF bytes", e);
            pdfBytes = null;
            Toast.makeText(this, "Failed to read PDF file", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveSubject() {
        String subjectName = subjectNameEditText.getText().toString().trim();

        if (subjectName.isEmpty()) {
            Toast.makeText(this, "Please enter subject name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pdfBytes == null) {
            Toast.makeText(this, "Please select a PDF file", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean inserted = teacherDBHelper.addSubject(subjectName, pdfBytes);
        if (inserted) {
            Toast.makeText(this, "Subject added successfully!", Toast.LENGTH_SHORT).show();
            subjectNameEditText.setText("");
            pdfFileNameText.setText("No file selected");
            pdfBytes = null;
            selectedPdfUri = null;
        } else {
            Toast.makeText(this, "Failed to add subject. It might already exist.", Toast.LENGTH_LONG).show();
        }
    }
}

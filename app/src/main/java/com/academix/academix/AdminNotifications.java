package com.academix.academix;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;

public class AdminNotifications extends AppCompatActivity {

    private Spinner recipientSpinner;
    private EditText messageEditText;
    private Button sendButton, shareButton;

    private DBHelper dbHelper;

    private static final int SMS_PERMISSION_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notifications);

        recipientSpinner = findViewById(R.id.recipientSpinner);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        shareButton = findViewById(R.id.shareButton);

        dbHelper = new DBHelper(this);

        sendButton.setOnClickListener(v -> {
            if (checkSmsPermission()) {
                handleSend();
            } else {
                requestSmsPermission();
            }
        });

        shareButton.setOnClickListener(v -> handleShare());
    }

    private boolean checkSmsPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestSmsPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                handleSend(); // Retry sending after permission granted
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleSend() {
        String message = messageEditText.getText().toString().trim();
        String recipientType = recipientSpinner.getSelectedItem().toString();

        if (message.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<String> contacts = new ArrayList<>();

        switch (recipientType) {
            case "Teachers Only":
                contacts = getTeacherContacts();
                break;
            case "Students Only":
                contacts = getStudentContacts();
                break;
            case "All":
                contacts = getTeacherContacts();
                contacts.addAll(getStudentContacts());
                break;
            case "Other":
                Toast.makeText(this, "Please use 'Share via' for other recipients", Toast.LENGTH_SHORT).show();
                return;
            default:
                Toast.makeText(this, "Invalid recipient type", Toast.LENGTH_SHORT).show();
                return;
        }

        if (contacts.isEmpty()) {
            Toast.makeText(this, "No contacts found", Toast.LENGTH_SHORT).show();
            return;
        }

        for (String number : contacts) {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(number, null, message, null, null);
            } catch (Exception e) {
                Toast.makeText(this, "Failed to send SMS to " + number, Toast.LENGTH_SHORT).show();
            }
        }

        Toast.makeText(this, "Message sent to selected recipients", Toast.LENGTH_LONG).show();
    }

    private void handleShare() {
        String message = messageEditText.getText().toString().trim();
        if (message.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);
        Intent chooser = Intent.createChooser(shareIntent, "Share via...");
        startActivity(chooser);
    }

    private ArrayList<String> getTeacherContacts() {
        ArrayList<String> contacts = new ArrayList<>();
        Cursor cursor = dbHelper.getAllTeachers();
        if (cursor != null && cursor.moveToFirst()) {
            int contactIndex = cursor.getColumnIndex(DBHelper.COL_TEACHER_CONTACT);
            do {
                if (contactIndex != -1) {
                    String contact = cursor.getString(contactIndex);
                    if (contact != null && !contact.isEmpty()) {
                        contacts.add(contact);
                    }
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return contacts;
    }

    private ArrayList<String> getStudentContacts() {
        ArrayList<String> contacts = new ArrayList<>();
        Cursor cursor = dbHelper.getAllStudents();
        if (cursor != null && cursor.moveToFirst()) {
            int contactIndex = cursor.getColumnIndex(DBHelper.COL_STUDENT_CONTACT);
            do {
                if (contactIndex != -1) {
                    String contact = cursor.getString(contactIndex);
                    if (contact != null && !contact.isEmpty()) {
                        contacts.add(contact);
                    }
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return contacts;
    }
}

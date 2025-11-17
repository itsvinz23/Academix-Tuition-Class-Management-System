package com.academix.academix;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AllTeachers extends AppCompatActivity {

    CardView addTeacherCard;
    private LinearLayout teachersContainer;
    private DBHelper dbHelper;
    private EditText searchEditText;
    private ImageView searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_all_teachers);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        teachersContainer = findViewById(R.id.teachersContainer);
        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        addTeacherCard = findViewById(R.id.addTeacherCard);
        dbHelper = new DBHelper(this);

        loadTeachers("");

        searchButton.setOnClickListener(v -> {
            String searchQuery = searchEditText.getText().toString().trim();
            loadTeachers(searchQuery);
        });

        addTeacherCard.setOnClickListener(v -> {
            Intent intent = new Intent(AllTeachers.this, AdminTeachers.class);
            startActivity(intent);
        });
    }

    private void loadTeachers(String searchQuery) {
        teachersContainer.removeAllViews();

        Cursor cursor = dbHelper.searchTeachers(searchQuery);

        if (cursor.moveToFirst()) {
            do {
                View teacherRow = getLayoutInflater().inflate(R.layout.teacher_row, teachersContainer, false);

                TextView idView = teacherRow.findViewById(R.id.teacherId);
                TextView nameView = teacherRow.findViewById(R.id.teacherName);
                TextView teacherUserName = teacherRow.findViewById(R.id.teacherUserName);
                TextView teacherPassword = teacherRow.findViewById(R.id.teacherPassword);
                TextView subjectView = teacherRow.findViewById(R.id.teacherSubject);
                TextView contactView = teacherRow.findViewById(R.id.teacherContact);

                // Fetch values from cursor
                final int teacherId = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_TEACHER_ID));
                final String fname = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TEACHER_FNAME));
                final String lname = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TEACHER_LNAME));
                final String contact = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TEACHER_CONTACT));
                final String subject = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TEACHER_SUBJECT));
                final String username = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TEACHER_USERNAME));
                final String password = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TEACHER_PASSWORD));

                // Set values to text views
                idView.setText(String.valueOf(teacherId));
                nameView.setText(fname + " " + lname);
                teacherUserName.setText(username);
                teacherPassword.setText(password);
                subjectView.setText(subject);
                contactView.setText(contact);

                // Pass data to EditTeacher
                teacherRow.setOnClickListener(v -> {
                    Intent intent = new Intent(AllTeachers.this, EditTeacher.class);
                    intent.putExtra("teacher_id", teacherId);
                    intent.putExtra("fname", fname);
                    intent.putExtra("lname", lname);
                    intent.putExtra("contact", contact);
                    intent.putExtra("subjectname", subject);
                    intent.putExtra("username", username);
                    intent.putExtra("password", password);
                    startActivity(intent);
                });

                teachersContainer.addView(teacherRow);

            } while (cursor.moveToNext());
        }

        cursor.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTeachers("");
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}

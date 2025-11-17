package com.academix.academix;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TeacherDashboard extends AppCompatActivity {

    private int teacherId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_teacher_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        teacherId = intent.getIntExtra("teacher_id", -1);

        TextView usernameText = findViewById(R.id.usernameText);
        usernameText.setText(username);

        Toast.makeText(this, "Logged in as teacher ID: " + teacherId, Toast.LENGTH_SHORT).show();

        LinearLayout logoutLayout = findViewById(R.id.logoutLayout);
        logoutLayout.setOnClickListener(v -> performLogout());

    }


    public void setResultsCard(View v){
        CardView resultsCard = findViewById(R.id.resultsCard);
        Intent intent = new Intent(TeacherDashboard.this, ViewSubmissions.class);
        startActivity(intent);

    }

    public void setSubjectCard(View v){
        CardView subjectsCard = findViewById(R.id.subjectsCard);
        Intent intent = new Intent(TeacherDashboard.this, AllSubjects.class);
        startActivity(intent);

    }

    public void setAttendanceCard(View v){
        CardView attendanceCard = findViewById(R.id.attendanceCard);
        Intent intent = new Intent(TeacherDashboard.this, TeacherAttendance.class);
        intent.putExtra("teacher_id", teacherId);
        startActivity(intent);

    }
    public void setAssignmentsCard(View v){
        CardView assignmentsCard = findViewById(R.id.assignmentsCard);
        Intent intent = new Intent(TeacherDashboard.this, AllAssignments.class);
        startActivity(intent);
    }
    public void setStudentsCard(View v){
        CardView studentsCard = findViewById(R.id.studentsCard);
        Intent intent = new Intent(TeacherDashboard.this, AllStudentResults.class);
        startActivity(intent);
    }
    public void setProfileCard(View v){
        CardView profileCard = findViewById(R.id.profileCard);
        Intent intent = new Intent(TeacherDashboard.this, EditTeacherContact.class);
        intent.putExtra("teacher_id", teacherId);
        startActivity(intent);
    }


    private void performLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent intent = new Intent(TeacherDashboard.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
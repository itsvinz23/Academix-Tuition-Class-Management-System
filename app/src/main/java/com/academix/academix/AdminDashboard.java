package com.academix.academix;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdminDashboard extends AppCompatActivity {

    CardView studentsCard, teachersCard,coursesCard,notificationsCard,profileCard,attendanceCard,resultsCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        String adminName = getIntent().getStringExtra("username");
        TextView usernameText = findViewById(R.id.usernameText);
        usernameText.setText(adminName);

        LinearLayout logoutLayout = findViewById(R.id.logoutLayout);
        logoutLayout.setOnClickListener(v -> performLogout());
    }
    public void setStudentsCard(View v){
        studentsCard = findViewById(R.id.studentsCard);
        Intent intent = new Intent(AdminDashboard.this, AllStudents.class);
        startActivity(intent);
    }
    public void setTeachersCard(View v){
        teachersCard = findViewById(R.id.teachersCard);
        Intent intent = new Intent(AdminDashboard.this, AllTeachers.class);
        startActivity(intent);
    }
    public void setCoursesCard(View v){
        coursesCard = findViewById(R.id.coursesCard);
        Intent intent = new Intent(AdminDashboard.this, AllCourses.class);
        startActivity(intent);
    }
    public void setNotificationsCard(View v){
        notificationsCard = findViewById(R.id.notificationsCard);
        Intent intent = new Intent(AdminDashboard.this, AdminNotifications.class);
        startActivity(intent);
    }
    public void setProfileCard(View v){
        profileCard = findViewById(R.id.profileCard);
        Intent intent = new Intent(AdminDashboard.this, AdminProfile.class);
        startActivity(intent);
    }
    public void setAttendanceCard(View v){
        attendanceCard = findViewById(R.id.attendanceCard);
        Intent intent = new Intent(AdminDashboard.this, AttendanceReport.class);
        startActivity(intent);
    }
    public void setResultsCard(View v){
        resultsCard = findViewById(R.id.resultsCard);
        Intent intent = new Intent(AdminDashboard.this, ResultsReport.class);
        startActivity(intent);
    }
    private void performLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent intent = new Intent(AdminDashboard.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


}
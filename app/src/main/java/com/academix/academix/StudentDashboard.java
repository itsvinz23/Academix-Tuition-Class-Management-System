package com.academix.academix;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class StudentDashboard extends AppCompatActivity {

    private int studentId;
    private GoogleMap mMap;

    // NIBM Location
    private final LatLng nibmLocation = new LatLng(6.9016, 79.8612);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        studentId = intent.getIntExtra("student_id", -1);

        TextView usernameText = findViewById(R.id.usernameText);
        usernameText.setText(username);

        TextView idText = findViewById(R.id.studentIdText);
        idText.setText("ID - " + studentId);

        LinearLayout logoutLayout = findViewById(R.id.logoutLayout);
        logoutLayout.setOnClickListener(v -> performLogout());


        // Initialize Map Fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);

        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;

                    // Add marker for NIBM
                    mMap.addMarker(new MarkerOptions().position(nibmLocation).title("NIBM, Colombo 07"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(nibmLocation, 15f));

                    // Handle click on the map
                    mMap.setOnMapClickListener(latLng -> openGoogleMapsNavigation());
                }
            });
        }
    }

    // Open Google Maps with navigation to NIBM
    private void openGoogleMapsNavigation() {
        try {
            Uri uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=NIBM+Vidya+Mawatha+Colombo+07");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.google.android.apps.maps");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // If Google Maps is not installed
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }

    public void setSubjectsCard(View v) {
        CardView subjectsCard = findViewById(R.id.subjectsCard);
        Intent intent = new Intent(StudentDashboard.this, StudentSubjects.class);
        startActivity(intent);
    }

    public void setAssignmentCard(View v) {
        CardView assignmentsCard = findViewById(R.id.assignmentsCard);
        Intent intent = new Intent(StudentDashboard.this, StudentAssignments.class);
        intent.putExtra("student_id", studentId);
        startActivity(intent);
    }

    public void setAttendanceCard(View v) {
        CardView attendanceCard = findViewById(R.id.attendanceCard);
        Intent intent = new Intent(StudentDashboard.this, StudentAttendance.class);
        intent.putExtra("student_id", studentId);
        startActivity(intent);
    }
    public void setResultsCard(View v) {
        CardView resultsCard = findViewById(R.id.resultsCard);
        Intent intent = new Intent(StudentDashboard.this, StudentResults.class);
        intent.putExtra("student_id", studentId);
        startActivity(intent);
    }
    public void setProfileCard(View v) {
        CardView profileCard = findViewById(R.id.profileCard);
        Intent intent = new Intent(StudentDashboard.this, EditStudentContact.class);
        intent.putExtra("student_id", studentId);
        startActivity(intent);
    }

    private void performLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent intent = new Intent(StudentDashboard.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

}

package com.academix.academix;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


    }
    public void setSignup(View v){
        TextView signUpText = findViewById(R.id.signUpText);
        Intent intent = new Intent(MainActivity.this, AdminSignup.class);
        startActivity(intent);
    }

    public void setLogin(View v) {
        EditText usernameInput = findViewById(R.id.usernameEditText);
        EditText passwordInput = findViewById(R.id.passwordEditText);
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        DBHelper dbHelper = new DBHelper(this);

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        String role = null;
        if (dbHelper.checkAdminCredentials(username, password)) {
            role = "admin";
        } else if (dbHelper.checkStudentCredentials(username, password)) {
            role = "student";
        } else if (dbHelper.checkTeacherCredentials(username, password)) {
            role = "teacher";
        }

        if (role == null) {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent;
            switch (role) {
                case "admin":
                    intent = new Intent(MainActivity.this, AdminDashboard.class);
                    break;
                case "student":
                    int studentId = dbHelper.getStudentIdByUsername(username);
                    intent = new Intent(MainActivity.this, StudentDashboard.class);
                    intent.putExtra("student_id", studentId);
                    break;
                case "teacher":
                    int teacherId = dbHelper.getTeacherIdByUsername(username);
                    intent = new Intent(MainActivity.this, TeacherDashboard.class);
                    intent.putExtra("teacher_id",teacherId);
                    break;
                default:
                    return;
            }
            // Pass username to dashboard
            intent.putExtra("username", username);
            startActivity(intent);
            finish();
        }
    }

}
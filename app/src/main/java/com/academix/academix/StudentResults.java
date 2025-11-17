package com.academix.academix;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.academix.academix.R;
import com.academix.academix.TeacherDBHelper;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class StudentResults extends AppCompatActivity {

        private HorizontalBarChart horizontalBarChart;
        private TeacherDBHelper dbHelper;
        private int studentId;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_student_results);

            horizontalBarChart = findViewById(R.id.horizontalBarChart);
            dbHelper = new TeacherDBHelper(this);

            studentId = getIntent().getIntExtra("student_id", -1);
            if (studentId == -1) {
                Toast.makeText(this, "Student ID not found!", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            displayStudentGrades(studentId);
        }

    private void displayStudentGrades(int studentId) {
        Cursor gradeCursor = dbHelper.getGradesWithAssignmentsForStudent(studentId);

        if (gradeCursor == null) {
            Toast.makeText(this, "No grades found.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> assignmentNames = new ArrayList<>();
        List<BarEntry> entries = new ArrayList<>();

        int index = 0;
        while (gradeCursor.moveToNext()) {
            String assignmentName = gradeCursor.getString(0);
            int marks = gradeCursor.getInt(1);

            assignmentNames.add(assignmentName);
            entries.add(new BarEntry(index, marks));
            index++;
        }
        gradeCursor.close();

        if (entries.isEmpty()) {
            Toast.makeText(this, "No grades available for this student.", Toast.LENGTH_LONG).show();
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Marks");
        dataSet.setColor(Color.parseColor("#4a125b"));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(14f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.7f); // bar width between 0 and 1

        horizontalBarChart.setData(barData);

        // X-axis (index axis) setup
        XAxis xAxis = horizontalBarChart.getXAxis();
        xAxis.setGranularity(1f); // one label per bar
        xAxis.setGranularityEnabled(true);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(assignmentNames));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(12f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setLabelRotationAngle(-45f); // rotate labels if needed

        // Left Y-axis (values) — the numeric axis
        YAxis leftAxis = horizontalBarChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setTextColor(Color.BLACK);

        // Disable right Y-axis
        YAxis rightAxis = horizontalBarChart.getAxisRight();
        rightAxis.setEnabled(false);

        horizontalBarChart.getDescription().setEnabled(false);
        horizontalBarChart.setFitBars(true);

        horizontalBarChart.setTouchEnabled(true);
        horizontalBarChart.setDragEnabled(true);
        horizontalBarChart.setScaleEnabled(false);

        // Dynamically set chart height based on number of bars
        int barHeightPx = (int) (getResources().getDisplayMetrics().density * 50); // 50dp per bar
        int chartHeight = barHeightPx * entries.size() + 100; // extra padding

        horizontalBarChart.getLayoutParams().height = chartHeight;
        horizontalBarChart.requestLayout();

        horizontalBarChart.invalidate();
    }
}

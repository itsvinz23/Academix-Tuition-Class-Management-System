package com.academix.academix;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TeacherDBHelper {

    private static final String DB_NAME = "academix.db";
    private static final int DB_VERSION = 1;

    // ===== Subjects Table =====
    private static final String TABLE_SUBJECTS = "subjects";
    private static final String COL_SUBJECT_ID = "subject_id";
    private static final String COL_SUBJECT_NAME = "subject_name";
    private static final String COL_COURSE_MATERIAL = "course_material"; // PDF BLOB

    // ===== Assignments Table =====
    private static final String TABLE_ASSIGNMENTS = "assignments";
    private static final String COL_ASSIGNMENT_ID = "assignment_id";
    private static final String COL_ASSIGNMENT_NAME = "assignment_name";
    private static final String COL_DUE_DATE = "due_date";
    private static final String COL_DUE_TIME = "due_time";
    private static final String COL_ASSIGNMENT_PDF = "assignment_pdf"; // PDF BLOB

    //===== Submitted Assignments Table =====
    private static final String TABLE_SUBMITTED_ASSIGNMENTS = "submitted_assignments";
    private static final String COL_SUBMIT_ID = "submit_id";
    private static final String COL_STUDENT_ID = "student_id";  // username
    private static final String COL_SUBMITTED_PDF = "submitted_pdf";
    private static final String COL_SUBMIT_DATE = "submit_date";
    private static final String COL_SUBMIT_TIME = "submit_time";

    // ===== Grades Table =====
    public static final String TABLE_GRADES = "grades";
    private static final String COL_GRADE_ID = "grade_id";
    public static final String COL_GRADE_STUDENT_ID = "student_id"; // INTEGER
    public static final String COL_GRADE_ASSIGNMENT_ID = "assignment_id"; // INTEGER
    public static final String COL_GRADE_MARKS = "marks"; // INTEGER
    private static final String COL_GRADE_NOTE = "note"; // TEXT (nullable)

    // ===== Attendance Table =====
    private static final String TABLE_ATTENDANCE = "attendance";
    private static final String COL_ATTENDANCE_ID = "attendance_id";
    private static final String COL_ATTENDANCE_STUDENT_ID = "student_id";
    private static final String COL_ATTENDANCE_TEACHER_ID = "teacher_id";
    private static final String COL_ATTENDANCE_TIMESTAMP = "timestamp";


    private SQLiteDatabase db;

    public TeacherDBHelper(Context context) {
        DBHelper helper = new DBHelper(context); // Assume this initializes the DB
        db = helper.getWritableDatabase();

        // ===== Create Subjects Table if not exists =====
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SUBJECTS + " (" +
                COL_SUBJECT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_SUBJECT_NAME + " TEXT NOT NULL, " +
                COL_COURSE_MATERIAL + " BLOB);");

        // ===== Create Assignments Table if not exists =====
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ASSIGNMENTS + " (" +
                COL_ASSIGNMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ASSIGNMENT_NAME + " TEXT NOT NULL, " +
                COL_DUE_DATE + " TEXT NOT NULL, " +
                COL_DUE_TIME + " TEXT NOT NULL, " +
                COL_ASSIGNMENT_PDF + " BLOB);");

        // ===== Create Submitted Assignments Table if not exists =====
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SUBMITTED_ASSIGNMENTS + " (" +
                COL_SUBMIT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_STUDENT_ID + " INTEGER NOT NULL, " +
                COL_ASSIGNMENT_ID + " INTEGER NOT NULL, " +
                COL_SUBMITTED_PDF + " BLOB NOT NULL, " +
                COL_SUBMIT_DATE + " TEXT NOT NULL, " +
                COL_SUBMIT_TIME + " TEXT NOT NULL);");

        // ===== Create Grades Table if not exists =====
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_GRADES + " (" +
                COL_GRADE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_GRADE_STUDENT_ID + " INTEGER NOT NULL, " +
                COL_GRADE_ASSIGNMENT_ID + " INTEGER NOT NULL, " +
                COL_GRADE_MARKS + " INTEGER NOT NULL, " +
                COL_GRADE_NOTE + " TEXT);");

        // ===== Create Attendance Table if not exists =====
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ATTENDANCE + " (" +
                COL_ATTENDANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ATTENDANCE_STUDENT_ID + " INTEGER NOT NULL, " +
                COL_ATTENDANCE_TEACHER_ID + " INTEGER NOT NULL, " +
                COL_ATTENDANCE_TIMESTAMP + " TEXT NOT NULL);");

    }


    public SQLiteDatabase getReadableDatabase() {
        return db;
    }


    // ============ SUBJECT METHODS ============

    public boolean addSubject(String subjectName, byte[] pdfBytes) {
        ContentValues values = new ContentValues();
        values.put(COL_SUBJECT_NAME, subjectName);
        values.put(COL_COURSE_MATERIAL, pdfBytes);
        return db.insert(TABLE_SUBJECTS, null, values) != -1;
    }

    public Cursor getAllSubjects() {
        return db.query(TABLE_SUBJECTS, null, null, null, null, null, null);
    }

    public Cursor getSubjectById(int id) {
        return db.query(TABLE_SUBJECTS, null, COL_SUBJECT_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
    }

    public boolean deleteSubject(int id) {
        return db.delete(TABLE_SUBJECTS, COL_SUBJECT_ID + "=?",
                new String[]{String.valueOf(id)}) > 0;
    }

    public boolean updateSubject(int id, String name, byte[] newPdf) {
        ContentValues values = new ContentValues();
        values.put(COL_SUBJECT_NAME, name);
        values.put(COL_COURSE_MATERIAL, newPdf);
        return db.update(TABLE_SUBJECTS, values, COL_SUBJECT_ID + "=?",
                new String[]{String.valueOf(id)}) > 0;
    }

    // ============ ASSIGNMENT METHODS ============

    public boolean addAssignment(String name, String dueDate, String dueTime, byte[] pdfBytes) {
        ContentValues values = new ContentValues();
        values.put(COL_ASSIGNMENT_NAME, name);
        values.put(COL_DUE_DATE, dueDate);
        values.put(COL_DUE_TIME, dueTime);
        values.put(COL_ASSIGNMENT_PDF, pdfBytes);
        return db.insert(TABLE_ASSIGNMENTS, null, values) != -1;
    }

    public Cursor getAllAssignments() {
        return db.query(TABLE_ASSIGNMENTS, null, null, null, null, null, null);
    }

    public Cursor getAssignmentById(int id) {
        return db.query(TABLE_ASSIGNMENTS, null, COL_ASSIGNMENT_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
    }

    public boolean deleteAssignment(int id) {
        return db.delete(TABLE_ASSIGNMENTS, COL_ASSIGNMENT_ID + "=?",
                new String[]{String.valueOf(id)}) > 0;
    }

    public boolean updateAssignment(int id, String name, String dueDate, String dueTime, byte[] newPdf) {
        ContentValues values = new ContentValues();
        values.put(COL_ASSIGNMENT_NAME, name);
        values.put(COL_DUE_DATE, dueDate);
        values.put(COL_DUE_TIME, dueTime);
        values.put(COL_ASSIGNMENT_PDF, newPdf);
        return db.update(TABLE_ASSIGNMENTS, values, COL_ASSIGNMENT_ID + "=?",
                new String[]{String.valueOf(id)}) > 0;
    }

    public boolean addSubmittedAssignment(String studentId, int assignmentId, byte[] pdfBytes, String date, String time) {
        ContentValues values = new ContentValues();
        values.put(COL_STUDENT_ID, studentId);
        values.put(COL_ASSIGNMENT_ID, assignmentId);
        values.put(COL_SUBMITTED_PDF, pdfBytes);
        values.put(COL_SUBMIT_DATE, date);
        values.put(COL_SUBMIT_TIME, time);
        return db.insert(TABLE_SUBMITTED_ASSIGNMENTS, null, values) != -1;
    }


    public boolean addGrade(int studentId, int assignmentId, int marks, String note) {
        ContentValues values = new ContentValues();
        values.put(COL_GRADE_STUDENT_ID, studentId);
        values.put(COL_GRADE_ASSIGNMENT_ID, assignmentId);
        values.put(COL_GRADE_MARKS, marks);
        values.put(COL_GRADE_NOTE, note);

        return db.insert(TABLE_GRADES, null, values) != -1;
    }


    public Cursor getGrade(int studentId, int assignmentId) {
        return db.query(TABLE_GRADES, null,
                COL_GRADE_STUDENT_ID + "=? AND " + COL_GRADE_ASSIGNMENT_ID + "=?",
                new String[]{String.valueOf(studentId), String.valueOf(assignmentId)},
                null, null, null);
    }

    public Integer getGradeMarks(int studentId, int assignmentId) {
        Cursor cursor = db.query(TABLE_GRADES, new String[]{COL_GRADE_MARKS},
                COL_GRADE_STUDENT_ID + "=? AND " + COL_GRADE_ASSIGNMENT_ID + "=?",
                new String[]{String.valueOf(studentId), String.valueOf(assignmentId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int marks = cursor.getInt(cursor.getColumnIndexOrThrow(COL_GRADE_MARKS));
            cursor.close();
            return marks;
        }

        return null;
    }

    public boolean updateGrade(int studentId, int assignmentId, int marks, String note) {
        ContentValues values = new ContentValues();
        values.put(COL_GRADE_MARKS, marks);
        values.put(COL_GRADE_NOTE, note);

        int rows = db.update(TABLE_GRADES, values,
                COL_GRADE_STUDENT_ID + "=? AND " + COL_GRADE_ASSIGNMENT_ID + "=?",
                new String[]{String.valueOf(studentId), String.valueOf(assignmentId)});

        return rows > 0;
    }

    // Add a new attendance entry
    public boolean addAttendance(int studentId, int teacherId, String timestamp) {
        ContentValues values = new ContentValues();
        values.put(COL_ATTENDANCE_STUDENT_ID, studentId);
        values.put(COL_ATTENDANCE_TEACHER_ID, teacherId);
        values.put(COL_ATTENDANCE_TIMESTAMP, timestamp);
        return db.insert(TABLE_ATTENDANCE, null, values) != -1;
    }

    // Check if already marked on the same date
    public boolean hasAttendanceForDate(int studentId, int teacherId, String dateOnly) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ATTENDANCE +
                        " WHERE " + COL_ATTENDANCE_STUDENT_ID + "=? AND " +
                        COL_ATTENDANCE_TEACHER_ID + "=? AND date(" + COL_ATTENDANCE_TIMESTAMP + ")=?",
                new String[]{String.valueOf(studentId), String.valueOf(teacherId), dateOnly});

        boolean exists = (cursor != null && cursor.moveToFirst());
        if (cursor != null) cursor.close();
        return exists;
    }

    public Cursor getTodayAttendance(String todayDate, int teacherId) {
        return db.rawQuery("SELECT " +
                        COL_ATTENDANCE_ID + " AS _id, " +
                        COL_ATTENDANCE_STUDENT_ID + ", " +
                        COL_ATTENDANCE_TIMESTAMP +
                        " FROM " + TABLE_ATTENDANCE +
                        " WHERE strftime('%Y-%m-%d', " + COL_ATTENDANCE_TIMESTAMP + ") = ? AND " +
                        COL_ATTENDANCE_TEACHER_ID + "=?",
                new String[]{todayDate, String.valueOf(teacherId)});
    }

    // Check if student has *any* attendance for today (regardless of teacher)
    public boolean hasAnyAttendanceForDate(int studentId, String dateOnly) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ATTENDANCE +
                        " WHERE " + COL_ATTENDANCE_STUDENT_ID + "=? AND date(" + COL_ATTENDANCE_TIMESTAMP + ")=?",
                new String[]{String.valueOf(studentId), dateOnly});

        boolean exists = (cursor != null && cursor.moveToFirst());
        if (cursor != null) cursor.close();
        return exists;
    }
    public Cursor getGradesWithAssignmentsForStudent(int studentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT assignments." + COL_ASSIGNMENT_NAME + ", grades." + COL_GRADE_MARKS +
                " FROM " + TABLE_GRADES + " grades" +
                " JOIN " + TABLE_ASSIGNMENTS + " assignments ON grades." + COL_GRADE_ASSIGNMENT_ID + " = assignments." + COL_ASSIGNMENT_ID +
                " WHERE grades." + COL_GRADE_STUDENT_ID + " = ?";
        return db.rawQuery(query, new String[] { String.valueOf(studentId) });
    }
    public boolean hasSubmittedAssignment(int studentId, int assignmentId) {
        Cursor cursor = db.query(TABLE_SUBMITTED_ASSIGNMENTS,
                null,
                COL_STUDENT_ID + "=? AND " + COL_ASSIGNMENT_ID + "=?",
                new String[]{String.valueOf(studentId), String.valueOf(assignmentId)},
                null, null, null);
        boolean exists = (cursor != null && cursor.moveToFirst());
        if (cursor != null) cursor.close();
        return exists;
    }

}

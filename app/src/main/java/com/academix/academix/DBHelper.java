package com.academix.academix;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "academix.db";
    private static final int DB_VERSION = 1;

    // Admin Table
    private static final String TABLE_ADMIN = "admin";
    private static final String COL_ADMIN_ID = "id";
    private static final String COL_ADMIN_USERNAME = "username";
    private static final String COL_ADMIN_PASSWORD = "password";

    // Student Table
    public static final String TABLE_STUDENT = "students";
    public static final String COL_STUDENT_ID = "student_id";
    public static final String COL_STUDENT_FNAME = "fname";
    public static final String COL_STUDENT_LNAME = "lname";
    public static final String COL_STUDENT_CONTACT = "contact";
    public static final String COL_STUDENT_COURSE = "coursename";
    public static final String COL_STUDENT_USERNAME = "username";
    public static final String COL_STUDENT_PASSWORD = "password";

    // Teacher Table
    public static final String TABLE_TEACHER = "teachers";
    public static final String COL_TEACHER_ID = "teacher_id";
    public static final String COL_TEACHER_FNAME = "fname";
    public static final String COL_TEACHER_LNAME = "lname";
    public static final String COL_TEACHER_CONTACT = "contact";
    public static final String COL_TEACHER_SUBJECT = "subjectname";
    public static final String COL_TEACHER_USERNAME = "username";
    public static final String COL_TEACHER_PASSWORD = "password";

    // Course Table
    public static final String TABLE_COURSE = "courses";
    public static final String COL_COURSE_ID = "course_id";
    public static final String COL_COURSE_NAME = "coursename";
    public static final String COL_COURSE_DURATION = "duration";
    public static final String COL_COURSE_MAX_STUDENTS = "max_students";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Admin table
        String createAdminTable = "CREATE TABLE " + TABLE_ADMIN + " (" +
                COL_ADMIN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ADMIN_USERNAME + " TEXT UNIQUE, " +
                COL_ADMIN_PASSWORD + " TEXT);";
        db.execSQL(createAdminTable);

        // Create Student table
        String createStudentTable = "CREATE TABLE " + TABLE_STUDENT + " (" +
                COL_STUDENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_STUDENT_FNAME + " TEXT, " +
                COL_STUDENT_LNAME + " TEXT, " +
                COL_STUDENT_CONTACT + " TEXT, " +
                COL_STUDENT_COURSE + " TEXT, " +
                COL_STUDENT_USERNAME + " TEXT UNIQUE, " +
                COL_STUDENT_PASSWORD + " TEXT);";
        db.execSQL(createStudentTable);

        // Create Teacher table
        String createTeacherTable = "CREATE TABLE " + TABLE_TEACHER + " (" +
                COL_TEACHER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TEACHER_FNAME + " TEXT, " +
                COL_TEACHER_LNAME + " TEXT, " +
                COL_TEACHER_CONTACT + " TEXT, " +
                COL_TEACHER_SUBJECT + " TEXT, " +
                COL_TEACHER_USERNAME + " TEXT UNIQUE, " +
                COL_TEACHER_PASSWORD + " TEXT);";
        db.execSQL(createTeacherTable);

        // Create Course table
        String createCourseTable = "CREATE TABLE " + TABLE_COURSE + " (" +
                COL_COURSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_COURSE_NAME + " TEXT UNIQUE, " +
                COL_COURSE_DURATION + " INTEGER, " +
                COL_COURSE_MAX_STUDENTS + " INTEGER);";
        db.execSQL(createCourseTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ADMIN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEACHER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSE);
        onCreate(db);
    }

    // ================= ADMIN METHODS =================
    public boolean addAdmin(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ADMIN_USERNAME, username);
        values.put(COL_ADMIN_PASSWORD, password);
        return db.insert(TABLE_ADMIN, null, values) != -1;
    }

    public boolean updateAdmin(int id, String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ADMIN_USERNAME, username);
        values.put(COL_ADMIN_PASSWORD, password);
        return db.update(TABLE_ADMIN, values, COL_ADMIN_ID + "=?",
                new String[]{String.valueOf(id)}) > 0;
    }

    public boolean checkAdminCredentials(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ADMIN,
                new String[]{COL_ADMIN_ID},
                COL_ADMIN_USERNAME + "=? AND " + COL_ADMIN_PASSWORD + "=?",
                new String[]{username, password},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // ================= STUDENT METHODS =================
    public boolean addStudent(String fname, String lname, String contact,
                              String coursename, String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STUDENT_FNAME, fname);
        values.put(COL_STUDENT_LNAME, lname);
        values.put(COL_STUDENT_CONTACT, contact);
        values.put(COL_STUDENT_COURSE, coursename);
        values.put(COL_STUDENT_USERNAME, username);
        values.put(COL_STUDENT_PASSWORD, password);
        return db.insert(TABLE_STUDENT, null, values) != -1;
    }

    public boolean updateStudent(int id, String fname, String lname, String contact,
                                 String coursename, String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STUDENT_FNAME, fname);
        values.put(COL_STUDENT_LNAME, lname);
        values.put(COL_STUDENT_CONTACT, contact);
        values.put(COL_STUDENT_COURSE, coursename);
        values.put(COL_STUDENT_USERNAME, username);
        values.put(COL_STUDENT_PASSWORD, password);
        return db.update(TABLE_STUDENT, values, COL_STUDENT_ID + "=?",
                new String[]{String.valueOf(id)}) > 0;
    }

    public boolean deleteStudent(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_STUDENT, COL_STUDENT_ID + "=?",
                new String[]{String.valueOf(id)}) > 0;
    }

    public Cursor getAllStudents() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_STUDENT, null, null, null, null, null, null);
    }

    public boolean checkStudentCredentials(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_STUDENT,
                new String[]{COL_STUDENT_ID},
                COL_STUDENT_USERNAME + "=? AND " + COL_STUDENT_PASSWORD + "=?",
                new String[]{username, password},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // ================= TEACHER METHODS =================
    public boolean addTeacher(String fname, String lname, String contact,
                              String subjectname, String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TEACHER_FNAME, fname);
        values.put(COL_TEACHER_LNAME, lname);
        values.put(COL_TEACHER_CONTACT, contact);
        values.put(COL_TEACHER_SUBJECT, subjectname);
        values.put(COL_TEACHER_USERNAME, username);
        values.put(COL_TEACHER_PASSWORD, password);
        return db.insert(TABLE_TEACHER, null, values) != -1;
    }

    public boolean updateTeacher(int id, String fname, String lname, String contact,
                                 String subjectname, String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TEACHER_FNAME, fname);
        values.put(COL_TEACHER_LNAME, lname);
        values.put(COL_TEACHER_CONTACT, contact);
        values.put(COL_TEACHER_SUBJECT, subjectname);
        values.put(COL_TEACHER_USERNAME, username);
        values.put(COL_TEACHER_PASSWORD, password);
        return db.update(TABLE_TEACHER, values, COL_TEACHER_ID + "=?",
                new String[]{String.valueOf(id)}) > 0;
    }

    public boolean deleteTeacher(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_TEACHER, COL_TEACHER_ID + "=?",
                new String[]{String.valueOf(id)}) > 0;
    }

    public Cursor getAllTeachers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_TEACHER, null, null, null, null, null, null);
    }

    public boolean checkTeacherCredentials(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TEACHER,
                new String[]{COL_TEACHER_ID},
                COL_TEACHER_USERNAME + "=? AND " + COL_TEACHER_PASSWORD + "=?",
                new String[]{username, password},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // ================= COURSE METHODS =================
    public boolean addCourse(String coursename, int duration, int maxStudents) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_COURSE_NAME, coursename);
        values.put(COL_COURSE_DURATION, duration);
        values.put(COL_COURSE_MAX_STUDENTS, maxStudents);
        return db.insert(TABLE_COURSE, null, values) != -1;
    }

    public boolean updateCourse(int id, String coursename, int duration, int maxStudents) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_COURSE_NAME, coursename);
        values.put(COL_COURSE_DURATION, duration);
        values.put(COL_COURSE_MAX_STUDENTS, maxStudents);
        return db.update(TABLE_COURSE, values, COL_COURSE_ID + "=?",
                new String[]{String.valueOf(id)}) > 0;
    }

    public boolean deleteCourse(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_COURSE, COL_COURSE_ID + "=?",
                new String[]{String.valueOf(id)}) > 0;
    }

    public Cursor getAllCourses() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_COURSE, null, null, null, null, null, null);
    }

    public Cursor getCourseNames() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT rowid AS _id, coursename FROM courses", null); // 🔁 fixed
    }

    public Cursor searchTeachers(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (query == null || query.isEmpty()) {
            // If search is empty, return all teachers
            return db.query(TABLE_TEACHER, null, null, null, null, null, null);
        } else {
            return db.query(TABLE_TEACHER,
                    null,
                    COL_TEACHER_ID + " = ? OR " +
                            COL_TEACHER_FNAME + " = ? OR " +
                            COL_TEACHER_LNAME + " = ? OR " +
                            COL_TEACHER_SUBJECT + " = ? OR " +
                            COL_TEACHER_USERNAME + " = ?",
                    new String[]{query, query, query, query, query},
                    null, null, null);
        }
    }



    public Cursor searchStudents(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (query == null || query.isEmpty()) {
            // If query is empty, return all students
            return db.query(TABLE_STUDENT, null, null, null, null, null, null);
        } else {
            return db.query(TABLE_STUDENT,
                    null,
                    COL_STUDENT_ID + " = ? OR " +
                            COL_STUDENT_FNAME + " = ? OR " +
                            COL_STUDENT_LNAME + " = ? OR " +
                            COL_STUDENT_COURSE + " = ? OR " +
                            COL_STUDENT_USERNAME + " = ?",
                    new String[]{query, query, query, query, query},
                    null, null, null);
        }
    }

    public Cursor searchCourses(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_COURSE,
                null,
                COL_COURSE_ID + " = ? OR " + COL_COURSE_NAME + " = ?",
                new String[]{query, query},
                null, null, null);
    }

    public Cursor getFirstAdmin() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_ADMIN + " LIMIT 1", null);
    }

    public int getStudentIdByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_STUDENT,
                new String[]{COL_STUDENT_ID},
                COL_STUDENT_USERNAME + "=?",
                new String[]{username},
                null, null, null);

        int id = -1; // default if not found
        if (cursor != null && cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_STUDENT_ID));
            cursor.close();
        }
        return id;
    }
    public int getTeacherIdByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TEACHER,
                new String[]{COL_TEACHER_ID},
                COL_TEACHER_USERNAME + "=?",
                new String[]{username},
                null, null, null);

        int id = -1; // default if not found
        if (cursor != null && cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TEACHER_ID));
            cursor.close();
        }
        return id;
    }
    public Cursor getDistinctCourses() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT DISTINCT " + COL_STUDENT_COURSE + " FROM " + TABLE_STUDENT, null);
    }

    // 2. Get Students by Course
    public Cursor getStudentsByCourse(String course) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_STUDENT,
                null,
                COL_STUDENT_COURSE + "=?",
                new String[]{course},
                null, null, null);
    }

    public Cursor getStudentGradesByCourseAndAssignment(String courseName, int assignmentId) {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT s." + COL_STUDENT_ID + ", s." + COL_STUDENT_FNAME + ", s." + COL_STUDENT_LNAME + ", " +
                        "s." + COL_STUDENT_USERNAME + ", g." + TeacherDBHelper.COL_GRADE_MARKS + " " +
                        "FROM " + TABLE_STUDENT + " s " +
                        "LEFT JOIN " + TeacherDBHelper.TABLE_GRADES + " g ON s." + COL_STUDENT_ID + " = g." + TeacherDBHelper.COL_GRADE_STUDENT_ID + " " +
                        "WHERE s." + COL_STUDENT_COURSE + " = ? AND g." + TeacherDBHelper.COL_GRADE_ASSIGNMENT_ID + " = ?",
                new String[]{courseName, String.valueOf(assignmentId)}
        );
    }
    public boolean updateStudentContactOnly(int id, String contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STUDENT_CONTACT, contact);
        return db.update(TABLE_STUDENT, values, COL_STUDENT_ID + "=?", new String[]{String.valueOf(id)}) > 0;
    }

    public Cursor getStudentById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_STUDENT,
                null,
                COL_STUDENT_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null);
    }
    public boolean updateTeacherContactOnly(int teacherId, String contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TEACHER_CONTACT, contact);
        return db.update(TABLE_TEACHER, values, COL_TEACHER_ID + "=?", new String[]{String.valueOf(teacherId)}) > 0;
    }

    public Cursor getTeacherById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_TEACHER,
                null,
                COL_TEACHER_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null);
    }




}
package com.example.omegeleapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * UserDatabaseHelper - Quản lý cơ sở dữ liệu SQLite cho người dùng
 * 
 * Chức năng chính:
 * - Tạo và quản lý database SQLite cho thông tin người dùng
 * - Lưu trữ thông tin đăng nhập (email, password)
 * - Cung cấp các method để thêm và kiểm tra người dùng
 * - Xử lý việc tạo và nâng cấp database schema
 * 
 * Kế thừa từ SQLiteOpenHelper để quản lý database lifecycle
 */
public class UserDatabaseHelper extends SQLiteOpenHelper {

    // Tên database và version
    private static final String DB_NAME = "user_db";    // Tên file database
    private static final int DB_VERSION = 1;            // Version hiện tại của database

    /**
     * Constructor tạo database helper
     * @param context Context của application
     */
    public UserDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Được gọi khi database được tạo lần đầu
     * Tạo các bảng cần thiết trong database
     * @param db SQLiteDatabase object
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng users với các cột: id, email, password
        String query = "CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, email TEXT, password TEXT)";
        db.execSQL(query);
    }

    /**
     * Được gọi khi cần nâng cấp database (khi DB_VERSION tăng)
     * Xóa bảng cũ và tạo lại bảng mới
     * @param db SQLiteDatabase object
     * @param oldVersion Version cũ của database
     * @param newVersion Version mới của database
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xóa bảng users nếu tồn tại
        db.execSQL("DROP TABLE IF EXISTS users");
        // Tạo lại bảng với schema mới
        onCreate(db);
    }

    /**
     * Thêm người dùng mới vào database
     * @param email Email của người dùng
     * @param password Mật khẩu của người dùng
     * @return true nếu thêm thành công, false nếu thất bại
     */
    public boolean insertUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();  // Mở database để ghi
        
        // Tạo ContentValues để chứa dữ liệu cần insert
        ContentValues values = new ContentValues();
        values.put("email", email);
        values.put("password", password);
        
        // Thực hiện insert và kiểm tra kết quả
        long result = db.insert("users", null, values);
        return result != -1;  // -1 nghĩa là insert thất bại
    }

    /**
     * Kiểm tra thông tin đăng nhập của người dùng
     * @param email Email của người dùng
     * @param password Mật khẩu của người dùng
     * @return true nếu thông tin đúng, false nếu sai
     */
    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();  // Mở database để đọc
        
        // Thực hiện query để kiểm tra email và password
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE email=? AND password=?", 
                                   new String[]{email, password});
        
        // Trả về true nếu có ít nhất 1 record thỏa mãn
        return cursor.getCount() > 0;
    }
}


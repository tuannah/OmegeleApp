package com.example.omegeleapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.omegeleapp.R;
import com.example.omegeleapp.database.UserDatabaseHelper;
import com.example.omegeleapp.activities.MainActivity;
import com.example.omegeleapp.activities.RegisterActivity;

/**
 * LoginActivity - Màn hình đăng nhập
 * 
 * Chức năng chính:
 * - Hiển thị form đăng nhập với email và mật khẩu
 * - Kiểm tra thông tin đăng nhập với database
 * - Chuyển hướng đến MainActivity khi đăng nhập thành công
 * - Chuyển hướng đến RegisterActivity để đăng ký tài khoản mới
 * - Sử dụng UserDatabaseHelper để kiểm tra thông tin người dùng
 */
public class LoginActivity extends AppCompatActivity {

    // UI components
    EditText edtEmail, edtPassword;    // Ô nhập email và mật khẩu
    Button btnLogin;                   // Nút đăng nhập
    TextView btnGoRegister;            // Link chuyển đến trang đăng ký
    UserDatabaseHelper db;             // Database helper để kiểm tra thông tin

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Ánh xạ các view components từ layout
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoRegister = findViewById(R.id.btnGoRegister);
        
        // Khởi tạo database helper
        db = new UserDatabaseHelper(this);

        // Xử lý sự kiện khi nhấn nút đăng nhập
        btnLogin.setOnClickListener(v -> {
            // Lấy thông tin từ các ô nhập liệu
            String email = edtEmail.getText().toString();
            String password = edtPassword.getText().toString();

            // Kiểm tra thông tin đăng nhập với database
            if (db.checkUser(email, password)) {
                // Đăng nhập thành công
                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                // Chuyển đến MainActivity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();  // Đóng LoginActivity để không thể quay lại
            } else {
                // Đăng nhập thất bại
                Toast.makeText(this, "Email hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý sự kiện khi nhấn link đăng ký
        btnGoRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        
        // Ẩn action bar để có giao diện fullscreen
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }
}

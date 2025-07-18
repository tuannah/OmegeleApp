package com.example.omegeleapp.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.omegeleapp.R;
import com.example.omegeleapp.database.UserDatabaseHelper;

/**
 * RegisterActivity - Màn hình đăng ký tài khoản
 * 
 * Chức năng chính:
 * - Hiển thị form đăng ký với email và mật khẩu
 * - Validate dữ liệu đầu vào (không được để trống)
 * - Thêm người dùng mới vào database
 * - Hiển thị thông báo thành công/thất bại
 * - Chuyển về LoginActivity sau khi đăng ký thành công
 * - Sử dụng UserDatabaseHelper để lưu thông tin người dùng
 */
public class RegisterActivity extends AppCompatActivity {

    // UI components
    EditText edtEmail, edtPassword;    // Ô nhập email và mật khẩu
    Button btnRegister;                // Nút đăng ký
    TextView txtBackToLogin;           // Link quay về trang đăng nhập
    UserDatabaseHelper db;             // Database helper để lưu thông tin

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Ánh xạ các view components từ layout
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegister = findViewById(R.id.btnRegister);
        txtBackToLogin = findViewById(R.id.txtBackToLogin);
        
        // Khởi tạo database helper
        db = new UserDatabaseHelper(this);

        // Xử lý sự kiện khi nhấn nút đăng ký
        btnRegister.setOnClickListener(v -> {
            // Lấy và làm sạch dữ liệu từ các ô nhập liệu
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            // Kiểm tra dữ liệu đầu vào
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Thêm người dùng mới vào database
            boolean success = db.insertUser(email, password);
            if (success) {
                // Đăng ký thành công
                Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                finish(); // Quay về trang đăng nhập
            } else {
                // Đăng ký thất bại (có thể do email đã tồn tại)
                Toast.makeText(this, "Email đã tồn tại", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý sự kiện khi nhấn link quay về đăng nhập
        txtBackToLogin.setOnClickListener(v -> finish());
    }
}


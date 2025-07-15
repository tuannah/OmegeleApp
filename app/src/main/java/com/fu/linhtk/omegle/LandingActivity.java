package com.fu.linhtk.omegle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LandingActivity extends AppCompatActivity {

    private Button signInButton;
    private Button attendanceButton;
    private UserDatabaseHelper dbHelper;
    private String loggedInEmail = null; // Lưu email của người dùng đã đăng nhập

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        // Khởi tạo UserDatabaseHelper
        dbHelper = new UserDatabaseHelper(this);

        // Khởi tạo các thành phần
        signInButton = findViewById(R.id.sign_in_button);
        attendanceButton = findViewById(R.id.attendance_button);

        // Giả lập email đã đăng nhập (thay bằng logic thực tế từ LoginActivity)
        // Ví dụ: loggedInEmail = getIntent().getStringExtra("email");
        loggedInEmail = "test@example.com"; // Chỉ để demo, xóa sau khi tích hợp LoginActivity

        // Xử lý sự kiện cho nút "Start to chat online"
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loggedInEmail != null && dbHelper.isUserLoggedIn(loggedInEmail)) {
                    // Nếu đã đăng nhập, chuyển đến MainActivity
                    Intent intent = new Intent(LandingActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    // Nếu chưa đăng nhập, chuyển đến LoginActivity
                    Intent intent = new Intent(LandingActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }
        });

        // Xử lý sự kiện cho nút "Start to call"
        attendanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loggedInEmail != null && dbHelper.isUserLoggedIn(loggedInEmail)) {
                    // Logic cho "Start to call" (có thể thêm CallActivity)
                    // Ở đây tạm thời giữ nguyên
                } else {
                    // Nếu chưa đăng nhập, chuyển đến LoginActivity
                    Intent intent = new Intent(LandingActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}


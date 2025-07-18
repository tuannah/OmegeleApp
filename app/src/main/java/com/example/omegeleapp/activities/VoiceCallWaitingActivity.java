package com.example.omegeleapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

import com.example.omegeleapp.R;
import com.example.omegeleapp.activities.MainActivity;
import com.example.omegeleapp.activities.VoiceCallActivity;

public class  VoiceCallWaitingActivity extends AppCompatActivity {

    private static final String TAG = "VoiceCallWaitingActivity";
    
    private TextView textViewStatus;
    private Button btnCancel;
    private Handler handler;
    private Runnable statusUpdater;
    private boolean isSearching = true;
    private int searchTime = 0;
    
    private String[] searchingMessages = {
        "Đang tìm người để trò chuyện...",
        "Đang tìm giọng nói thân thiện...",
        "Đang tìm người phù hợp...",
        "Đang kết nối với người lạ...",
        "Sắp xong rồi...",
        "Đã tìm thấy! Đang kết nối..."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_call_waiting);

        // Ẩn action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initializeViews();
        startSearching();
    }

    private void initializeViews() {
        textViewStatus = findViewById(R.id.textViewStatus);
        btnCancel = findViewById(R.id.btnCancel);

        // Xử lý sự kiện nút
        btnCancel.setOnClickListener(v -> cancelSearch());

        // Hiển thị thông tin user
        displayUserInfo();
    }

    private void displayUserInfo() {
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        String userName = prefs.getString("name", "Người dùng");
        String userAge = prefs.getString("age", "");
        String userCountry = prefs.getString("country", "");
        String userInterests = prefs.getString("interests", "");

        TextView textViewUserInfo = findViewById(R.id.textViewUserInfo);
        if (textViewUserInfo != null) {
            StringBuilder info = new StringBuilder();
            info.append("Tên: ").append(userName);
            if (!userAge.isEmpty()) {
                info.append(" | Tuổi: ").append(userAge);
            }
            if (!userCountry.isEmpty()) {
                info.append(" | Quốc gia: ").append(userCountry);
            }
            if (!userInterests.isEmpty()) {
                info.append("\nSở thích: ").append(userInterests);
            }
            textViewUserInfo.setText(info.toString());
        }
    }

    private void startSearching() {
        handler = new Handler(Looper.getMainLooper());
        statusUpdater = new Runnable() {
            @Override
            public void run() {
                if (isSearching) {
                    updateSearchingStatus();
                    searchTime++;
                    
                    // Tự động tìm thấy sau 10-20 giây
                    if (searchTime > 10 + new Random().nextInt(10)) {
                        findMatch();
                    } else {
                        handler.postDelayed(this, 2000);
                    }
                }
            }
        };
        handler.post(statusUpdater);
    }

    private void updateSearchingStatus() {
        int messageIndex = (searchTime / 2) % searchingMessages.length;
        String message = searchingMessages[messageIndex];
        textViewStatus.setText(message);
        Log.d(TAG, "Searching status: " + message);
    }

    private void findMatch() {
        isSearching = false;
        textViewStatus.setText("Đã tìm thấy! Đang kết nối...");
        Log.d(TAG, "Match found, starting voice call");
        
        // Chuyển sang màn hình voice call sau 2 giây
        handler.postDelayed(() -> {
            Intent intent = new Intent(VoiceCallWaitingActivity.this, VoiceCallActivity.class);
            startActivity(intent);
            finish();
        }, 2000);
    }

    private void cancelSearch() {
        isSearching = false;
        Log.d(TAG, "Search cancelled by user");
        
        // Quay về màn hình chính
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && statusUpdater != null) {
            handler.removeCallbacks(statusUpdater);
        }
    }

    @Override
    public void onBackPressed() {
        cancelSearch();
        super.onBackPressed();
    }
} 
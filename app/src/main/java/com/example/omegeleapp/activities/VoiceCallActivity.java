package com.example.omegeleapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.omegeleapp.R;
import com.example.omegeleapp.managers.AgoraVoiceManager;
import com.example.omegeleapp.utils.LogoutManager;
import com.example.omegeleapp.managers.AgoraSignalingManager;
import com.example.omegeleapp.managers.RtmSignalingManager;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;
import java.util.Random;
import android.os.Handler;
import android.os.Looper;
import android.widget.LinearLayout;

/**
 * VoiceCallActivity - Màn hình cuộc gọi thoại
 * 
 * Chức năng chính:
 * - Hiển thị giao diện cuộc gọi thoại
 * - Quản lý microphone và speaker
 * - Kết nối voice call qua Agora SDK
 * - Xử lý các sự kiện cuộc gọi (join, leave, error)
 * - Tích hợp với signaling server để tìm người chat
 */
public class VoiceCallActivity extends AppCompatActivity
        implements AgoraSignalingManager.SignalingListener {
    private static final String TAG = "VoiceCallActivity";

    // UI components
    private TextView tvStatus;
    private TextView tvPartnerInfo;
    private TextView tvPartnerName;
    private TextView tvPartnerAge;
    private TextView tvPartnerCountry;
    private TextView tvConnectionTime;
    private LinearLayout layoutPartnerInfo;
    private Button btnMute;
    private Button btnSpeaker;
    private Button btnEndCall;
    private Button btnLogout;

    // Managers
    private AgoraSignalingManager signalingManager;
    private LogoutManager logoutManager;
    private RtmSignalingManager rtmSignalingManager;
    private String rtmPartnerUserId;

    // Thêm biến để chọn hệ thống voice call
    private boolean dungAgora = false; // Luôn dùng Local
    private LocalVoiceManager localVoiceManager;

    // State variables
    private boolean isMuted = false;
    private boolean isSpeakerOn = false;
    private String currentUserId;
    private String partnerUserId;
    private String channelName;
    private long connectionStartTime;
    private Handler connectionTimeHandler;
    private Runnable connectionTimeRunnable;
    
    // Thông tin người dùng thật
    private String currentUserName;
    private String currentUserAge;
    private String currentUserCountry;

    /**
     * Interface để lắng nghe sự kiện voice call
     */
    public interface VoiceCallListener {
        void onUserJoined(int uid);

        void onUserOffline(int uid);

        void onJoinChannelSuccess(String channel, int uid, int elapsed);

        void onLeaveChannel();

        void onError(int errorCode);
    }

    /**
     * Constructor
     */
    public VoiceCallActivity() {
        // Default constructor
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_call);

        // Khởi tạo UI
        initializeViews();

        // Khởi tạo managers
        initializeManagers();

        // Lấy thông tin người dùng
        loadUserInfo();

        // Yêu cầu quyền
        requestPermissions();

        // Bắt đầu tìm người chat
        startVoiceCall();
    }

    /**
     * Khởi tạo các view components
     */
    private void initializeViews() {
        tvStatus = findViewById(R.id.textViewStatus);
        tvPartnerInfo = findViewById(R.id.textViewStatus); // Using the same status text view for now
        tvPartnerName = findViewById(R.id.tvPartnerName);
        tvPartnerAge = findViewById(R.id.tvPartnerAge);
        tvPartnerCountry = findViewById(R.id.tvPartnerCountry);
        tvConnectionTime = findViewById(R.id.tvConnectionTime);
        layoutPartnerInfo = findViewById(R.id.layoutPartnerInfo);
        btnMute = findViewById(R.id.btnMute);
        btnSpeaker = findViewById(R.id.btnSpeaker);
        btnEndCall = findViewById(R.id.btnEndCall);
        btnLogout = findViewById(R.id.btnLogout);

        // Khởi tạo handler cho cập nhật thời gian kết nối
        connectionTimeHandler = new Handler(Looper.getMainLooper());
        connectionTimeRunnable = new Runnable() {
            @Override
            public void run() {
                updateConnectionTime();
                connectionTimeHandler.postDelayed(this, 1000); // Cập nhật mỗi giây
            }
        };

        // Thiết lập click listeners
        btnMute.setOnClickListener(v -> toggleMute());
        btnSpeaker.setOnClickListener(v -> toggleSpeaker());
        btnEndCall.setOnClickListener(v -> endCall());
        btnLogout.setOnClickListener(v -> logout());

        // Cập nhật trạng thái ban đầu
        updateUI();
    }

    /**
     * Khởi tạo các manager classes
     */
    private void initializeManagers() {
        // Không khởi tạo AgoraVoiceManager nữa
        // Khởi tạo LocalVoiceManager
        localVoiceManager = new LocalVoiceManager(this);
        localVoiceManager.setLocalVoiceListener(new LocalVoiceManager.LocalVoiceListener() {
            @Override
            public void onConnected() {
                runOnUiThread(() -> tvStatus.setText("Đã kết nối (Local Voice)"));
            }
            @Override
            public void onDisconnected() {
                runOnUiThread(() -> tvStatus.setText("Đã ngắt kết nối (Local Voice)"));
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(VoiceCallActivity.this, error, Toast.LENGTH_LONG).show());
            }
            @Override
            public void onAudioReceived(byte[] audioData) {
                // Có thể xử lý thêm nếu muốn
            }
        });
        // Khởi tạo AgoraSignalingManager
        signalingManager = new AgoraSignalingManager(this);
        signalingManager.setListener(this);

        // Khởi tạo LogoutManager
        //logoutManager = new LogoutManager(this);
    }

    /**
     * Lấy thông tin người dùng từ SharedPreferences
     */
    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        currentUserId = "user_" + System.currentTimeMillis();
        currentUserName = prefs.getString("name", "ời dùng");
        currentUserAge = prefs.getString("age", "25");
        currentUserCountry = prefs.getString("country", "Việt Nam");
        channelName = "omegele_" + System.currentTimeMillis(); // Tạo channel name duy nhất
        
        Log.d(TAG, "Thông tin người dùng: " + currentUserName + ", " + currentUserAge + " tuổi, " + currentUserCountry);
    }

    /**
     * Yêu cầu quyền microphone và record audio
     */
    private void requestPermissions() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.MODIFY_AUDIO_SETTINGS)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            Log.d(TAG, "Tất cả quyền đã được cấp");
                        } else {
                            Toast.makeText(VoiceCallActivity.this,
                                    "Cần quyền microphone để thực hiện cuộc gọi",
                                    Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
                            PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    /**
     * Bắt đầu voice call
     */
    private void startVoiceCall() {
        tvStatus.setText("Đang tìm người chat...");

        // Đăng nhập RTM và tìm partner
        rtmSignalingManager = new RtmSignalingManager(this, currentUserId, new RtmSignalingManager.PartnerInfoListener() {
            @Override
            public void onPartnerInfoReceived(String partnerName, String partnerAge, String partnerCountry) {
                showPartnerInfo(partnerName, partnerAge, partnerCountry);
            }
            @Override
            public void onPartnerUserId(String partnerUserId) {
                rtmPartnerUserId = partnerUserId;
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(VoiceCallActivity.this, error, Toast.LENGTH_LONG).show());
            }
        });
        rtmSignalingManager.loginAndFindPartner(currentUserName, currentUserAge, currentUserCountry);
    }

    /**
     * Bật/tắt microphone
     */
    private void toggleMute() {
        isMuted = !isMuted;
        localVoiceManager.setMicrophoneEnabled(!isMuted);
        if (isMuted) {
            btnMute.setText("Bật Mic");
            btnMute.setBackgroundResource(R.drawable.button_muted);
        } else {
            btnMute.setText("Tắt Mic");
            btnMute.setBackgroundResource(R.drawable.button_normal);
        }
    }

    /**
     * Bật/tắt speaker
     */
    private void toggleSpeaker() {
        isSpeakerOn = !isSpeakerOn;
        localVoiceManager.setSpeakerEnabled(isSpeakerOn);
        if (isSpeakerOn) {
            btnSpeaker.setText("Tắt Loa");
            btnSpeaker.setBackgroundResource(R.drawable.button_speaker_on);
        } else {
            btnSpeaker.setText("Bật Loa");
            btnSpeaker.setBackgroundResource(R.drawable.button_normal);
        }
    }

    /**
     * Kết thúc cuộc gọi
     */
    private void endCall() {
        new AlertDialog.Builder(this)
                .setTitle("Kết thúc cuộc gọi")
                .setMessage("Bạn có chắc muốn kết thúc cuộc gọi?")
                .setPositiveButton("Có", (dialog, which) -> {
                    // Rời kênh Local
                    if (localVoiceManager.isConnected()) {
                        localVoiceManager.disconnect();
                    }
                    // Ẩn thông tin người dùng
                    hidePartnerInfo();
                    // Ngắt kết nối signaling
                    signalingManager.disconnect();
                    // Quay về MainActivity
                    Intent intent = new Intent(VoiceCallActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Không", null)
                .show();
    }

    /**
     * Đăng xuất
     */
    private void logout() {
        LogoutManager.showLogoutDialog(this, () -> {
            // Rời kênh nếu đang trong cuộc gọi
            if (localVoiceManager.isConnected()) {
                localVoiceManager.disconnect();
            }
            // Ngắt kết nối signaling
            signalingManager.disconnect();
            // Quay về LoginActivity
            Intent intent = new Intent(VoiceCallActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Cập nhật giao diện
     */
    private void updateUI() {
        runOnUiThread(() -> {
            if (partnerUserId != null) {
                tvPartnerInfo.setText("Đang nói chuyện với:" + partnerUserId);
            } else {
                tvPartnerInfo.setText("Chưa có người tham gia");
            }
        });
    }

    /**
     * Hiển thị thông tin người dùng được kết nối (thông tin thật)
     */
    private void showPartnerInfo(String partnerName, String partnerAge, String partnerCountry) {
        runOnUiThread(() -> {
            tvPartnerName.setText("👤  " + partnerName);
            tvPartnerAge.setText("🎂  " + partnerAge + " tuổi");
            tvPartnerCountry.setText("🌍  " + partnerCountry);
            
            // Hiển thị layout thông tin với animation
            layoutPartnerInfo.setVisibility(View.VISIBLE);
            layoutPartnerInfo.setAlpha(0f);
            layoutPartnerInfo.animate().alpha(1f).setDuration(500).start();
            
            // Bắt đầu đếm thời gian kết nối
            connectionStartTime = System.currentTimeMillis();
            connectionTimeHandler.post(connectionTimeRunnable);
            
            // Cập nhật status
            tvStatus.setText("Đang nói chuyện với  " + partnerName);
            
            // Gửi thông tin người dùng thật đến partner
            signalingManager.sendUserInfo();
        });
    }

    /**
     * Ẩn thông tin người dùng
     */
    private void hidePartnerInfo() {
        runOnUiThread(() -> {
            // Animation ẩn thông tin
            layoutPartnerInfo.animate().alpha(0f).setDuration(300).withEndAction(() -> {
                layoutPartnerInfo.setVisibility(View.GONE);
            }).start();
            
            connectionTimeHandler.removeCallbacks(connectionTimeRunnable);
        });
    }

    /**
     * Cập nhật thời gian kết nối
     */
    private void updateConnectionTime() {
        if (connectionStartTime > 0) {
            long elapsedTime = System.currentTimeMillis() - connectionStartTime;
            long seconds = elapsedTime /1000;
            long minutes = seconds / 60;
            seconds = seconds % 60;
            String timeText = String.format("Kết nối: %02d:%02d", minutes, seconds);
            tvConnectionTime.setText(timeText);
        }
    }

    // ========== AgoraVoiceManager.VoiceCallListener ==========

    @Override
    public void onUserJoined(int uid) {
        Log.d(TAG, "Người dùng đã tham gia kênh: " + uid);
        runOnUiThread(() -> {
            tvStatus.setText("Đã kết nối với người chat");
            tvPartnerInfo.setText("Đang nói chuyện với người dùng #" + uid);
            
            // Thông báo có người kết nối
            Toast.makeText(this, "🎉 Đã kết nối với người lạ!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onUserOffline(int uid) {
        Log.d(TAG, "Người dùng đã rời kênh: " + uid);
        runOnUiThread(() -> {
            tvStatus.setText("Người chat đã rời cuộc gọi");
            tvPartnerInfo.setText("Chờ người khác tham gia...");
            
            // Ẩn thông tin người dùng
            hidePartnerInfo();

            // Tự động tìm người chat mới sau 3iây
            tvStatus.postDelayed(() -> {
                // No auto-reconnect logic for LocalVoiceManager
            }, 30);
        });
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        Log.d(TAG, "Đã tham gia kênh thành công: " + channel + ", uid: " + uid);
        runOnUiThread(() -> {
            tvStatus.setText("Đã tham gia cuộc gọi");
        });
    }

    @Override
    public void onLeaveChannel() {
        Log.d(TAG, "Đã rời kênh");
        runOnUiThread(() -> {
            tvStatus.setText("Đã rời cuộc gọi");
        });
    }

    @Override
    public void onError(int errorCode) {
        Log.e(TAG, "Lỗi Agora: " + errorCode);
        runOnUiThread(() -> {
            tvStatus.setText("Lỗi kết nối: " + errorCode);
            Toast.makeText(this, "Lỗi kết nối voice call", Toast.LENGTH_SHORT).show();
        });
    }

    // ========== AgoraSignalingManager.SignalingListener ==========

    @Override
    public void onConnected() {
        Log.d(TAG, "Đã kết nối với signaling server");
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "Đã ngắt kết nối với signaling server");
    }

    @Override
    public void onUserJoined(String userId) {
        Log.d(TAG, "Người dùng đã tham gia phòng: " + userId);
        partnerUserId = userId;
        updateUI();
    }

    @Override
    public void onUserLeft(String userId) {
        Log.d(TAG, "Người dùng đã rời phòng: " + userId);
        partnerUserId = null;
        updateUI();
    }

    @Override
    public void onMatchFound(String partnerUserId) {
        Log.d(TAG, "Đã tìm thấy người chat: " + partnerUserId);
        this.partnerUserId = partnerUserId;

        runOnUiThread(() -> {
            tvStatus.setText("Đã tìm thấy người chat, đang kết nối...");
        });

        // No Agora channel joining for LocalVoiceManager
    }
    
    /**
     * Callback nhận thông tin người dùng thật từ đối phương
     */
    @Override
    public void onPartnerInfoReceived(String partnerName, String partnerAge, String partnerCountry) {
        Log.d(TAG, "Nhận thông tin đối phương: " + partnerName + ", " + partnerAge + " tuổi, " + partnerCountry);
        showPartnerInfo(partnerName, partnerAge, partnerCountry);
    }

    @Override
    public void onWaitingForPartner() {
        runOnUiThread(() -> {
            tvStatus.setText("Đang tìm người chat...");
        });
    }

    @Override
    public void onOffer(Object offer, String fromUserId) {
        // Không cần xử lý cho voice call
    }

    @Override
    public void onAnswer(Object answer, String fromUserId) {
        // Không cần xử lý cho voice call
    }

    @Override
    public void onIceCandidate(Object candidate, String fromUserId) {
        // Không cần xử lý cho voice call
    }

    @Override
    public void onError(String error) {
        Log.e(TAG, "Lỗi signaling: " + error);
        runOnUiThread(() -> {
            tvStatus.setText("Lỗi kết nối: " + error);
            Toast.makeText(this, "Lỗi kết nối: " + error, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Dừng cập nhật thời gian kết nối
        if (connectionTimeHandler != null) {
            connectionTimeHandler.removeCallbacks(connectionTimeRunnable);
        }

        // Dọn dẹp resources
        if (localVoiceManager != null) {
            localVoiceManager.destroy();
        }

        if (signalingManager != null) {
            signalingManager.disconnect();
        }
        if (rtmSignalingManager != null) rtmSignalingManager.logout();
    }
}
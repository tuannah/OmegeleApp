package com.example.omegeleapp.managers;

import android.content.Context;
import android.util.Log;

// import io.agora.rtc.IRtcEngineEventHandler;
// import io.agora.rtc.RtcEngine;

import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.IRtcEngineEventHandler.RtcStats;

/**
 * Quản lý voice call sử dụng Agora SDK
 * Cho phép 2 thiết bị gọi nhau qua internet
 * 
 * HƯỚNG DẪN LẤY APP ID:
 * 1. Đăng ký tại: https://console.agora.io/
 * 2. Tạo project mới
 * 3 App ID từ project
 * 4. Thay thế YOUR_AGORA_APP_ID bên dưới
 */
public class AgoraVoiceManager {
    private static final String TAG = "AgoraVoiceManager";

    // TODO: THAY THẾ BẰNG APP ID THẬT TỪ AGORA CONSOLE
    // HƯỚNG DẪN: Đăng nhập https://console.agora.io/ -> Project Management -> Copy App ID
    // 
    // NẾU GẶP LỖI 110:
    // 1App ID hiện tại có thể đã hết hạn hoặc bị vô hiệu hóa
    // 2o project mới trên Agora Console
    // 3Copy App ID mới và thay thế bên dưới
    // 4Đảm bảo project có trạng thái "Active"
    private static final String AGORA_APP_ID = "bdd8b54cad4c4af1afd377f65f2bcc7b";
    // Nếu sử dụng Token authentication (khuyến nghị cho production)
    // private static final String AGORA_TOKEN = "your_token_here";

    private Context context;
    private RtcEngine rtcEngine;
    private VoiceCallListener listener;
    private boolean isInCall = false;
    private String currentChannelName;

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
    public AgoraVoiceManager(Context context) {
        this.context = context;
        initializeAgoraEngine();
    }

    /**
     * Khởi tạo Agora Engine
     */
    private void initializeAgoraEngine() {
        try {
            // Tạo RtcEngine với App ID
            rtcEngine = RtcEngine.create(context, AGORA_APP_ID, rtcEventHandler);

            // Cấu hình audio
            rtcEngine.enableAudio();

            Log.d(TAG, "Agora Engine đã được khởi tạo thành công");

        } catch (Exception e) {
            Log.e(TAG, "Lỗi khởi tạo Agora Engine: " + e.getMessage());
        }
    }

    /**
     * Event handler cho Agora
     */
    private final IRtcEngineEventHandler rtcEventHandler = new IRtcEngineEventHandler() {
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            Log.d(TAG, "Đã tham gia kênh thành công: " + channel + ", uid: " + uid);
            isInCall = true;
            if (listener != null) {
                listener.onJoinChannelSuccess(channel, uid, elapsed);
            }
        }

        public void onLeaveChannel() {
            Log.d(TAG, "Đã rời kênh");
            isInCall = false;
            if (listener != null) {
                listener.onLeaveChannel();
            }
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            Log.d(TAG, "Người dùng đã tham gia: " + uid);
            if (listener != null) {
                listener.onUserJoined(uid);
            }
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            Log.d(TAG, "Người dùng đã rời: " + uid);
            if (listener != null) {
                listener.onUserOffline(uid);
            }
        }

        @Override
        public void onError(int errorCode) {
            Log.e(TAG, "Lỗi Agora: " + errorCode);
            if (listener != null) {
                listener.onError(errorCode);
            }
        }
    };

    /**
     * Tham gia kênh voice call
     */
    public void joinChannel(String channelName) {
        if (rtcEngine == null) {
            Log.e(TAG, "RtcEngine chưa được khởi tạo");
            return;
        }

        if (isInCall) {
            Log.w(TAG, "Đang trong cuộc gọi, không thể tham gia kênh mới");
            return;
        }

        currentChannelName = channelName;

        // Token authentication (có thể để null nếu dùng No Auth)
        // LƯU Ý: Với App ID mới, có thể cần Token authentication
        String token = null;
        // String token = AGORA_TOKEN; // Uncomment nếu sử dụng Token

        // Tham gia kênh với uid = 0 để Agora tự sinh
        int result = rtcEngine.joinChannel(token, channelName, "", 0);

        if (result == 0) {
            Log.d(TAG, "Đang tham gia kênh: " + channelName);
        } else {
            Log.e(TAG, "Lỗi tham gia kênh: " + result);
            // Xử lý các lỗi cụ thể
            switch (result) {
                case 110:
                    Log.e(TAG, "Lỗi110: App ID không hợp lệ hoặc đã hết hạn. Vui lòng kiểm tra App ID trong Agora Console.");
                    break;
                case 101:
                    Log.e(TAG, "Lỗi101: App ID không tồn tại hoặc bị vô hiệu hóa.");
                    break;
                case 102:
                    Log.e(TAG, "Lỗi 102 Token không hợp lệ hoặc đã hết hạn.");
                    break;
                default:
                    Log.e(TAG, "Lỗi không xác định: " + result);
                    break;
            }
        }
    }

    /**
     * Rời kênh voice call
     */
    public void leaveChannel() {
        if (rtcEngine == null) {
            Log.e(TAG, "RtcEngine chưa được khởi tạo");
            return;
        }

        if (!isInCall) {
            Log.w(TAG, "Không trong cuộc gọi");
            return;
        }

        rtcEngine.leaveChannel();
        currentChannelName = null;
        Log.d(TAG, "Đã rời kênh");
    }

    /**
     * Bật/tắt microphone
     */
    public void setMicrophoneEnabled(boolean enabled) {
        if (rtcEngine == null) {
            Log.e(TAG, "RtcEngine chưa được khởi tạo");
            return;
        }

        rtcEngine.muteLocalAudioStream(!enabled);
        Log.d(TAG, "Microphone " + (enabled ? "đã bật" : "đã tắt"));
    }

    /**
     * Bật/tắt speaker
     */
    public void setSpeakerEnabled(boolean enabled) {
        if (rtcEngine == null) {
            Log.e(TAG, "RtcEngine chưa được khởi tạo");
            return;
        }

        rtcEngine.setEnableSpeakerphone(enabled);
        Log.d(TAG, "Speaker " + (enabled ? "đã bật" : "đã tắt"));
    }

    /**
     * Thiết lập listener
     */
    public void setVoiceCallListener(VoiceCallListener listener) {
        this.listener = listener;
    }

    /**
     * Kiểm tra có đang trong cuộc gọi không
     */
    public boolean isInCall() {
        return isInCall;
    }

    /**
     * Lấy tên kênh hiện tại
     */
    public String getCurrentChannelName() {
        return currentChannelName;
    }

    /**
     * Dọn dẹp tài nguyên
     */
    public void destroy() {
        if (rtcEngine != null) {
            if (isInCall) {
                leaveChannel();
            }
            RtcEngine.destroy();
            rtcEngine = null;
        }
        Log.d(TAG, "AgoraVoiceManager đã được dọn dẹp");
    }
}
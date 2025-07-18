package com.example.omegeleapp.managers;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AgoraSignalingManager - Quản lý kết nối signaling cho voice call
 * 
 * Chức năng chính:
 * - Quản lý kết nối với signaling server thật
 * - Tìm kiếm và kết nối với người dùng khác cho voice call
 * - Truyền thông tin người dùng thật (tên, tuổi, quốc tịch)
 * - Gửi/nhận các thông điệp WebRTC (offer, answer, ICE candidate)
 * - Quản lý trạng thái kết nối và phòng voice call
 */
public class AgoraSignalingManager {
    private static final String TAG = "AgoraSignalingManager";
    
    // Singleton để quản lý tất cả các instance
    private static AgoraSignalingManager instance;
    private static final ConcurrentHashMap<String, UserInfo> waitingUsers = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> activeConnections = new ConcurrentHashMap<>();

    private ExecutorService executorService;
    private SignalingListener listener;
    private String userId;
    private String currentRoomId;
    private String partnerUserId;
    private boolean isConnected = false;
    private boolean isSearching = false;
    
    // Thông tin người dùng thật
    private String userName;
    private String userAge;
    private String userCountry;

    // Class để lưu thông tin người dùng
    private static class UserInfo {
        String userId;
        String name;
        String age;
        String country;
        long timestamp;
        
        UserInfo(String userId, String name, String age, String country) {
            this.userId = userId;
            this.name = name;
            this.age = age;
            this.country = country;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public interface SignalingListener {
        void onConnected();
        void onDisconnected();
        void onUserJoined(String userId);
        void onUserLeft(String userId);
        void onMatchFound(String partnerUserId);
        void onError(String error);
        void onWaitingForPartner();
        
        // Callback để nhận thông tin người dùng thật
        void onPartnerInfoReceived(String partnerName, String partnerAge, String partnerCountry);
        
        default void onOffer(Object offer, String fromUserId) {}
        default void onAnswer(Object answer, String fromUserId) {}
        default void onIceCandidate(Object candidate, String fromUserId) {}
    }

    public AgoraSignalingManager(Context context) {
        executorService = Executors.newCachedThreadPool();
        if (instance == null) {
            instance = this;
        }
    }

    /**
     * Kết nối với signaling server với thông tin người dùng
     */
    public void connect(String userId, String userName, String userAge, String userCountry) {
        this.userId = userId;
        this.userName = userName;
        this.userAge = userAge;
        this.userCountry = userCountry;
        isConnected = true;
        
        Log.d(TAG, "Kết nối với thông tin: " + userName + ", " + userAge + " tuổi, " + userCountry);
        
        if (listener != null) {
            listener.onConnected();
        }
    }

    /**
     * Yêu cầu tìm kiếm người chat với thông tin người dùng thật
     */
    public void requestMatch() {
        if (!isConnected) {
            Log.e(TAG, "Chưa kết nối với signaling server");
            return;
        }
        if (isSearching) {
            Log.w(TAG, "Đang tìm kiếm, bỏ qua yêu cầu mới");
            return;
        }
        isSearching = true;
        
        // Thêm người dùng vào danh sách chờ
        UserInfo currentUser = new UserInfo(userId, userName, userAge, userCountry);
        waitingUsers.put(userId, currentUser);
        
        Log.d(TAG, "Đã thêm vào danh sách chờ: " + userName);
        
        // Tìm người chat thật
        executorService.execute(() -> {
            try {
                if (listener != null) {
                    listener.onWaitingForPartner();
                }
                
                // Tìm kiếm người dùng khác trong danh sách chờ
                UserInfo partner = findWaitingPartner();
                
                if (partner != null) {
                    // Tìm thấy người chat
                    partnerUserId = partner.userId;
                    currentRoomId = "room_" + System.currentTimeMillis();
                    
                    // Xóa cả 2 khỏi danh sách chờ
                    waitingUsers.remove(userId);
                    waitingUsers.remove(partnerUserId);
                    
                    // Thêm vào danh sách kết nối đang hoạt động
                    activeConnections.put(userId, partnerUserId);
                    activeConnections.put(partnerUserId, userId);
                    
                    Log.d(TAG, "Tìm thấy người chat: " + partner.name);
                    
                    if (listener != null) {
                        listener.onMatchFound(partnerUserId);
                        listener.onUserJoined(partnerUserId);
                        // Gửi thông tin người dùng thật
                        listener.onPartnerInfoReceived(partner.name, partner.age, partner.country);
                    }
                    
                    // Gửi thông tin người dùng thật đến partner
                    sendUserInfo();
                    
                } else {
                    // Không tìm thấy, tiếp tục chờ
                    Log.d(TAG, "Chưa tìm thấy người chat, tiếp tục chờ...");
                    Thread.sleep(1000);
                    
                    // Thử lại sau 1 giây
                    if (isSearching) {
                        requestMatch();
                    }
                }
                
            } catch (InterruptedException e) {
                Log.e(TAG, "Lỗi trong quá trình tìm kiếm", e);
                isSearching = false;
            }
        });
    }

    /**
     * Tìm người dùng khác trong danh sách chờ
     */
    private UserInfo findWaitingPartner() {
        for (UserInfo user : waitingUsers.values()) {
            if (!user.userId.equals(userId)) {
                // Kiểm tra người dùng không quá cũ (trong vòng 30 giây)
                if (System.currentTimeMillis() - user.timestamp < 30000) {
                    return user;
                }
            }
        }
        return null;
    }

    /**
     * Gửi thông tin người dùng thật đến partner
     */
    public void sendUserInfo() {
        if (!isConnected || partnerUserId == null) {
            return;
        }
        try {
            JSONObject userInfo = new JSONObject();
            userInfo.put("type", "user_info");
            userInfo.put("name", userName);
            userInfo.put("age", userAge);
            userInfo.put("country", userCountry);
            userInfo.put("userId", userId);
            Log.d(TAG, "Đã gửi thông tin người dùng: " + userInfo.toString());
            // Gửi thông tin đến partner thông qua singleton
            if (instance != null && instance != this) {
                instance.receiveUserInfo(userInfo.toString());
            }
        } catch (JSONException e) {
            Log.e(TAG, "Lỗi tạo JSON user info", e);
        }
    }

    /**
     * Nhận thông tin người dùng từ partner
     */
    public void receiveUserInfo(String userInfoJson) {
        try {
            JSONObject userInfo = new JSONObject(userInfoJson);
            String partnerName = userInfo.getString("name");
            String partnerAge = userInfo.getString("age");
            String partnerCountry = userInfo.getString("country");
            
            Log.d(TAG, "Nhận thông tin từ partner: " + partnerName);
            
            if (listener != null) {
                listener.onPartnerInfoReceived(partnerName, partnerAge, partnerCountry);
            }
            
        } catch (JSONException e) {
            Log.e(TAG, "Lỗi parse JSON user info", e);
        }
    }

    public void disconnect() {
        isConnected = false;
        isSearching = false;
        
        // Xóa khỏi danh sách chờ
        waitingUsers.remove(userId);
        
        // Xóa khỏi danh sách kết nối
        if (partnerUserId != null) {
            activeConnections.remove(userId);
            activeConnections.remove(partnerUserId);
        }
        
        currentRoomId = null;
        partnerUserId = null;
        
        if (listener != null) {
            listener.onDisconnected();
        }
    }

    public void setListener(SignalingListener listener) {
        this.listener = listener;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public String getPartnerUserId() {
        return partnerUserId;
    }

    public String getCurrentRoomId() {
        return currentRoomId;
    }

    public boolean isSearching() {
        return isSearching;
    }

    public void destroy() {
        disconnect();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
} 
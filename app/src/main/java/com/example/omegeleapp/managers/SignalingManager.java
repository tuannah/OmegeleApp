package com.example.omegeleapp.managers;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * SignalingManager - Quản lý kết nối và giao tiếp với signaling server
 * 
 * Chức năng chính:
 * - Kết nối với AWS Lambda signaling server thông qua HTTP API
 * - Quản lý việc tìm kiếm và kết nối với người dùng khác
 * - Gửi/nhận các thông điệp WebRTC (offer, answer, ICE candidate)
 * - Gửi/nhận tin nhắn chat
 * - Quản lý trạng thái kết nối và phòng chat
 * 
 * Sử dụng OkHttp để thực hiện HTTP requests và ExecutorService để xử lý bất đồng bộ
 */
public class SignalingManager {
    private static final String TAG = "SignalingManager";
    
    // URL của AWS Lambda API Gateway endpoint
    private static final String LAMBDA_API_URL = "https://your-api-gateway-url.amazonaws.com/prod/signaling";
    
    // Media type cho JSON requests
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    // Các thành phần cần thiết cho HTTP communication
    private OkHttpClient httpClient;           // Client để thực hiện HTTP requests
    private ExecutorService executorService;   // Thread pool để xử lý bất đồng bộ
    
    // Callback listener để thông báo các sự kiện
    private SignalingListener listener;
    
    // Thông tin người dùng và phòng
    private String userId;           // ID của người dùng hiện tại
    private String roomId;           // ID của phòng hiện tại
    private String partnerUserId;    // ID của người bạn chat
    private boolean isConnected = false;  // Trạng thái kết nối
    
    /**
     * Interface định nghĩa các callback cho signaling events
     * Các activity sẽ implement interface này để nhận thông báo
     */
    public interface SignalingListener {
        void onConnected();                    // Khi kết nối thành công với server
        void onDisconnected();                 // Khi ngắt kết nối với server
        void onUserJoined(String userId);      // Khi có người tham gia phòng
        void onUserLeft(String userId);        // Khi có người rời phòng
        void onOffer(Object offer, String fromUserId);      // Nhận offer từ người khác
        void onAnswer(Object answer, String fromUserId);    // Nhận answer từ người khác
        void onIceCandidate(Object candidate, String fromUserId);  // Nhận ICE candidate
        void onMatchFound(String partnerUserId);           // Tìm thấy người chat
        void onError(String error);                        // Có lỗi xảy ra
        void onWaitingForPartner();                        // Đang chờ tìm người chat
    }
    
    /**
     * Constructor khởi tạo SignalingManager
     * Tạo OkHttpClient và ExecutorService để xử lý HTTP requests
     */
    public SignalingManager() {
        httpClient = new OkHttpClient();
        executorService = Executors.newCachedThreadPool();
    }
    
    /**
     * Kết nối với signaling server
     * @param userId ID của người dùng hiện tại
     */
    public void connect(String userId) {
        this.userId = userId;
        isConnected = true;
        if (listener != null) {
            listener.onConnected();
        }
    }
    
    /**
     * Yêu cầu tìm kiếm người chat
     * Gửi request đến server để tìm người phù hợp
     */
    public void requestMatch() {
        if (!isConnected) {
            Log.e(TAG, "Not connected to signaling server");
            return;
        }
        
        // Thực hiện request trong background thread
        executorService.execute(() -> {
            try {
                // Tạo JSON request body
                JSONObject requestBody = new JSONObject();
                requestBody.put("action", "find_partner");
                requestBody.put("userId", userId);
                
                // Gửi HTTP request và nhận response
                String response = makeHttpRequest(requestBody.toString());
                JSONObject responseJson = new JSONObject(response);
                
                String status = responseJson.getString("status");
                
                if ("waiting".equals(status)) {
                    // Chưa tìm thấy người chat, tiếp tục chờ
                    if (listener != null) {
                        listener.onWaitingForPartner();
                    }
                    // Poll định kỳ để kiểm tra có người mới không
                    pollForPartner();
                } else if ("connected".equals(status)) {
                    // Đã tìm thấy người chat
                    roomId = responseJson.getString("roomId");
                    partnerUserId = responseJson.getString("partner");
                    
                    if (listener != null) {
                        listener.onMatchFound(partnerUserId);
                        listener.onUserJoined(partnerUserId);
                    }
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error requesting match", e);
                if (listener != null) {
                    listener.onError("Lỗi khi tìm người kết nối: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Poll định kỳ để kiểm tra có người chat mới không
     * Chạy trong background thread và sleep 2 giây giữa các lần poll
     */
    private void pollForPartner() {
        executorService.execute(() -> {
            try {
                Thread.sleep(2000);  // Chờ 2 giây
                requestMatch();      // Thử tìm người chat lại
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
    
    /**
     * Tham gia vào một phòng chat cụ thể
     * @param roomId ID của phòng cần tham gia
     */
    public void joinRoom(String roomId) {
        this.roomId = roomId;
        
        executorService.execute(() -> {
            try {
                JSONObject requestBody = new JSONObject();
                requestBody.put("action", "join_room");
                requestBody.put("userId", userId);
                requestBody.put("roomId", roomId);
                
                String response = makeHttpRequest(requestBody.toString());
                JSONObject responseJson = new JSONObject(response);
                
                String status = responseJson.getString("status");
                
                if ("joined".equals(status)) {
                    partnerUserId = responseJson.getString("partner");
                    if (listener != null) {
                        listener.onUserJoined(partnerUserId);
                    }
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error joining room", e);
                if (listener != null) {
                    listener.onError("Lỗi khi tham gia phòng: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Gửi WebRTC offer đến người dùng khác
     * @param offer Đối tượng offer (hiện tại là mock)
     * @param toUserId ID của người nhận offer
     */
    public void sendOffer(Object offer, String toUserId) {
        executorService.execute(() -> {
            try {
                JSONObject requestBody = new JSONObject();
                requestBody.put("action", "send_sdp");
                requestBody.put("userId", userId);
                requestBody.put("targetUserId", toUserId);
                requestBody.put("sdp", "mock_sdp_offer");  // Mock SDP offer
                
                makeHttpRequest(requestBody.toString());
                Log.d(TAG, "Sent offer to: " + toUserId);
                
            } catch (Exception e) {
                Log.e(TAG, "Error sending offer", e);
                if (listener != null) {
                    listener.onError("Lỗi khi gửi offer: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Gửi WebRTC answer đến người dùng khác
     * @param answer Đối tượng answer (hiện tại là mock)
     * @param toUserId ID của người nhận answer
     */
    public void sendAnswer(Object answer, String toUserId) {
        executorService.execute(() -> {
            try {
                JSONObject requestBody = new JSONObject();
                requestBody.put("action", "send_sdp");
                requestBody.put("userId", userId);
                requestBody.put("targetUserId", toUserId);
                requestBody.put("sdp", "mock_sdp_answer");  // Mock SDP answer
                
                makeHttpRequest(requestBody.toString());
                Log.d(TAG, "Sent answer to: " + toUserId);
                
            } catch (Exception e) {
                Log.e(TAG, "Error sending answer", e);
                if (listener != null) {
                    listener.onError("Lỗi khi gửi answer: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Gửi ICE candidate đến người dùng khác
     * @param candidate Đối tượng ICE candidate (hiện tại là mock)
     * @param toUserId ID của người nhận ICE candidate
     */
    public void sendIceCandidate(Object candidate, String toUserId) {
        executorService.execute(() -> {
            try {
                JSONObject requestBody = new JSONObject();
                requestBody.put("action", "send_ice_candidate");
                requestBody.put("userId", userId);
                requestBody.put("targetUserId", toUserId);
                requestBody.put("iceCandidate", "mock_ice_candidate");  // Mock ICE candidate
                
                makeHttpRequest(requestBody.toString());
                Log.d(TAG, "Sent ICE candidate to: " + toUserId);
                
            } catch (Exception e) {
                Log.e(TAG, "Error sending ICE candidate", e);
                if (listener != null) {
                    listener.onError("Lỗi khi gửi ICE candidate: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Rời khỏi phòng chat hiện tại
     * Thông báo cho server và reset trạng thái phòng
     */
    public void leaveRoom() {
        executorService.execute(() -> {
            try {
                JSONObject requestBody = new JSONObject();
                requestBody.put("action", "leave_room");
                requestBody.put("userId", userId);
                requestBody.put("roomId", roomId);
                
                makeHttpRequest(requestBody.toString());
                Log.d(TAG, "Left room: " + roomId);
                
                // Reset trạng thái phòng
                roomId = null;
                partnerUserId = null;
                
            } catch (Exception e) {
                Log.e(TAG, "Error leaving room", e);
                if (listener != null) {
                    listener.onError("Lỗi khi rời phòng: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Thực hiện HTTP POST request đến signaling server
     * @param jsonBody JSON body của request
     * @return Response body từ server
     * @throws IOException Nếu có lỗi network
     */
    private String makeHttpRequest(String jsonBody) throws IOException {
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(LAMBDA_API_URL)
                .post(body)
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP error: " + response.code());
            }
            return response.body().string();
        }
    }
    
    /**
     * Ngắt kết nối với signaling server
     * Thông báo cho server và reset trạng thái kết nối
     */
    public void disconnect() {
        executorService.execute(() -> {
            try {
                JSONObject requestBody = new JSONObject();
                requestBody.put("action", "disconnect");
                requestBody.put("userId", userId);
                
                makeHttpRequest(requestBody.toString());
                Log.d(TAG, "Disconnected from signaling server");
                
            } catch (Exception e) {
                Log.e(TAG, "Error disconnecting", e);
            } finally {
                isConnected = false;
                if (listener != null) {
                    listener.onDisconnected();
                }
            }
        });
    }
    
    /**
     * Set listener để nhận các callback events
     * @param listener SignalingListener implementation
     */
    public void setListener(SignalingListener listener) {
        this.listener = listener;
    }
    
    /**
     * Kiểm tra trạng thái kết nối
     * @return true nếu đã kết nối, false nếu chưa
     */
    public boolean isConnected() {
        return isConnected;
    }
    
    /**
     * Lấy ID của người bạn chat hiện tại
     * @return ID của partner hoặc null nếu chưa có
     */
    public String getPartnerUserId() {
        return partnerUserId;
    }
    
    /**
     * Lấy ID của phòng hiện tại
     * @return ID của phòng hoặc null nếu chưa tham gia phòng nào
     */
    public String getRoomId() {
        return roomId;
    }
} 
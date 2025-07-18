package com.example.omegeleapp.managers;

import android.content.Context;
import android.util.Log;
import java.io.IOException;
import okhttp3.*;
import org.json.JSONObject;

import io.agora.rtm.*;
import java.util.*;

public class RtmSignalingManager {
    private static final String TAG = "RtmSignalingManager";
    private static final String APP_ID = "bdd8b54cad4c4af1afd377f65f2bcc7b"; // Sử dụng App ID thật
    private static final String MATCH_CHANNEL = "omegle_match";
    // URL server để lấy token (thay thế bằng server thật của bạn)
    private static final String TOKEN_SERVER_URL = "007eJxTYLgg49in31zOs3K+zEXPHWaRvT/bHvzPX/7exXnRedd3PZUKDEkpKRZJpibJiSkmySaJaYaJaSnG5uZpZqZpRknJyeZJb9IrMhoCGRnef9zIzMgAgSA+D0N+bmp6Tmp8bmJJcgYDAwAsRiaE";
    private static final OkHttpClient httpClient = new OkHttpClient();

    private RtmClient rtmClient;
    private RtmChannel rtmChannel;
    private String userId;
    private String rtmToken = "007eJxTYLgg49in31zOs3K+zEXPHWaRvT/bHvzPX/7exXnRedd3PZUKDEkpKRZJpibJiSkmySaJaYaJaSnG5uZpZqZpRknJyeZJb9IrMhoCGRnef9zIzMgAgSA+D0N+bmp6Tmp8bmJJcgYDAwAsRiaE";
    private PartnerInfoListener partnerInfoListener;
    private Context context;

    // Thêm biến token RTM trực tiếp (nếu muốn dùng token lấy từ Agora Console/Token Builder)
    private static final String RTM_TOKEN = "007eJxTYLgg49in31zOs3K+zEXPHWaRvT/bHvzPX/7exXnRedd3PZUKDEkpKRZJpibJiSkmySaJaYaJaSnG5uZpZqZpRknJyeZJb9IrMhoCGRnef9zIzMgAgSA+D0N+bmp6Tmp8bmJJcgYDAwAsRiaE"; // Dán token RTM của bạn ở đây nếu muốn dùng trực tiếp

    public interface PartnerInfoListener {
        void onPartnerInfoReceived(String partnerName, String partnerAge, String partnerCountry);
        void onPartnerUserId(String partnerUserId);
        void onError(String error);
    }

    public RtmSignalingManager(Context context, String userId, PartnerInfoListener listener) {
        this.context = context;
        this.userId = userId;
        this.partnerInfoListener = listener;
        
        if (context == null) {
            Log.e(TAG, "Context không được null");
            if (partnerInfoListener != null) partnerInfoListener.onError("Context không hợp lệ");
            return;
        }
        
        if (APP_ID == null || APP_ID.isEmpty() || APP_ID.equals("YOUR_AGORA_APP_ID_HERE")) {
            Log.e(TAG, "App ID không hợp lệ: " + APP_ID);
            if (partnerInfoListener != null) partnerInfoListener.onError("App ID không hợp lệ. Vui lòng cập nhật App ID thật từ Agora Console.");
            return;
        }
        
        Log.d(TAG, "Khởi tạo RTM Client với App ID: " + APP_ID);
        
        try {
            rtmClient = RtmClient.createInstance(context, APP_ID, new RtmClientListener() {
                @Override public void onConnectionStateChanged(int state, int reason) {
                    Log.d(TAG, "RTM Connection State: " + state + ", Reason: " + reason);
                }
                @Override public void onMessageReceived(RtmMessage message, String peerId) {}
                @Override public void onTokenExpired() {
                    Log.w(TAG, "RTM Token đã hết hạn");
                    // Tự động lấy token mới
                    fetchRtmToken();
                }
                @Override public void onTokenPrivilegeWillExpire() {
                    Log.w(TAG, "RTM Token sắp hết hạn");
                    // Lấy token mới trước khi hết hạn
                    fetchRtmToken();
                }
                @Override public void onPeersOnlineStatusChanged(Map<String, Integer> status) {}
            });
            Log.d(TAG, "RTM Client đã được khởi tạo thành công");
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khởi tạo RTM: " + e.getMessage(), e);
            if (partnerInfoListener != null) partnerInfoListener.onError("Lỗi khởi tạo RTM: " + e.getMessage());
        }
    }

    /**
     * Lấy RTM Token từ server
     */
    private void fetchRtmToken() {
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "User ID không hợp lệ để lấy token");
            return;
        }

        // Tạo request body
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("uid", userId);
            requestBody.put("appId", APP_ID);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi tạo request body: " + e.getMessage());
            return;
        }

        RequestBody body = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            requestBody.toString()
        );

        // Check if we have a valid token server URL
        if (TOKEN_SERVER_URL == null || TOKEN_SERVER_URL.isEmpty() || !TOKEN_SERVER_URL.startsWith("http")) {
            Log.w(TAG, "Không có token server URL hợp lệ, bỏ qua việc lấy token"); // For development, we'll proceed without a token
            return;
        }

        Request request = new Request.Builder()
            .url(TOKEN_SERVER_URL)
            .post(body)
            .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Lỗi kết nối server token: " + e.getMessage());
                if (partnerInfoListener != null) {
                    partnerInfoListener.onError("Không thể kết nối server token: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        
                        if (jsonResponse.has("token")) {
                            rtmToken = jsonResponse.getString("token");
                            Log.d(TAG, "Đã lấy RTM Token thành công");
                            
                            // Nếu đang trong cuộc gọi, tự động đăng nhập lại với token mới
                            if (rtmClient != null) {
                                rtmClient.renewToken(rtmToken, new ResultCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "Đã renew token thành công");
                                    }
                                    @Override
                                    public void onFailure(ErrorInfo errorInfo) {
                                        Log.e(TAG, "Lỗi renew token: " + errorInfo.getErrorDescription());
                                    }
                                });
                            }
                        } else {
                            Log.e(TAG, "Server không trả về token");
                            if (partnerInfoListener != null) {
                                partnerInfoListener.onError("Server không trả về token hợp lệ");
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi parse response token: " + e.getMessage());
                        if (partnerInfoListener != null) {
                            partnerInfoListener.onError("Lỗi xử lý response token: " + e.getMessage());
                        }
                    }
                } else {
                    Log.e(TAG, "Server trả về lỗi: " + response.code());
                    if (partnerInfoListener != null) {
                        partnerInfoListener.onError("Server token trả về lỗi: " + response.code());
                    }
                }
            }
        });
    }

    public void loginAndFindPartner(final String name, final String age, final String country) {
        if (rtmClient == null) {
            if (partnerInfoListener != null) partnerInfoListener.onError("RTM Client chưa được khởi tạo");
            return;
        }
        
        if (userId == null || userId.isEmpty()) {
            if (partnerInfoListener != null) partnerInfoListener.onError("User ID không hợp lệ");
            return;
        }

        Log.d(TAG, "Đăng nhập RTM với token trực tiếp (nếu có)");
        // Nếu RTM_TOKEN được cấu hình, dùng luôn, không gọi fetchRtmToken nữa
        if (RTM_TOKEN != null && !RTM_TOKEN.isEmpty()) {
            rtmToken = RTM_TOKEN;
        } else {
            // Nếu không có token trực tiếp, thử lấy từ server (nếu có server)
            fetchRtmToken();
        }

        // Đăng nhập với token nếu có, nếu không thì fallback
        if (rtmToken != null && !rtmToken.isEmpty()) {
            loginWithToken(name, age, country);
        } else {
            Log.w(TAG, "Không có token, đăng nhập không token (chỉ cho development)");
            loginWithoutToken(name, age, country);
        }
    }

    private void loginWithToken(final String name, final String age, final String country) {
        Log.d(TAG, "Đang đăng nhập RTM với token");
        
        rtmClient.login(rtmToken, userId, new ResultCallback<Void>() {
            @Override public void onSuccess(Void aVoid) {
                Log.d(TAG, "Đăng nhập RTM thành công với token");
                joinMatchChannel(name, age, country);
            }
            @Override public void onFailure(ErrorInfo errorInfo) {
                Log.e(TAG, "Lỗi đăng nhập RTM với token: " + errorInfo.getErrorCode() + " - " + errorInfo.getErrorDescription());
                if (partnerInfoListener != null) {
                    String errorMsg = "Lỗi đăng nhập RTM: " + errorInfo.getErrorDescription();
                    if (errorInfo.getErrorCode() == 1) {
                        errorMsg = "App ID không hợp lệ hoặc hết hạn. Vui lòng kiểm tra lại App ID.";
                    } else if (errorInfo.getErrorCode() == 2) {
                        errorMsg = "User ID không hợp lệ hoặc đã được sử dụng.";
                    } else if (errorInfo.getErrorCode() == 102) {
                        errorMsg = "Token không hợp lệ hoặc đã hết hạn.";
                    }
                    partnerInfoListener.onError(errorMsg);
                }
            }
        });
    }

    private void loginWithoutToken(final String name, final String age, final String country) {
        Log.d(TAG, "Đang đăng nhập RTM không token (development mode)");
        
        rtmClient.login(null, userId, new ResultCallback<Void>() {
            @Override public void onSuccess(Void aVoid) {
                Log.d(TAG, "Đăng nhập RTM thành công không token");
                joinMatchChannel(name, age, country);
            }
            @Override public void onFailure(ErrorInfo errorInfo) {
                Log.e(TAG, "Lỗi đăng nhập RTM không token: " + errorInfo.getErrorCode() + " - " + errorInfo.getErrorDescription());
                if (partnerInfoListener != null) {
                    String errorMsg = "Lỗi đăng nhập RTM: " + errorInfo.getErrorDescription();
                    if (errorInfo.getErrorCode() == 1) {
                        errorMsg = "App ID không hợp lệ hoặc hết hạn. Vui lòng kiểm tra lại App ID.";
                    } else if (errorInfo.getErrorCode() == 2) {
                        errorMsg = "User ID không hợp lệ hoặc đã được sử dụng.";
                    }
                    partnerInfoListener.onError(errorMsg);
                }
            }
        });
    }

    private void joinMatchChannel(final String name, final String age, final String country) {
        rtmChannel = rtmClient.createChannel(MATCH_CHANNEL, new RtmChannelListener() {
            @Override public void onMemberCountUpdated(int i) {}
            @Override public void onAttributesUpdated(List<RtmChannelAttribute> list) {}
            @Override public void onMessageReceived(RtmMessage message, RtmChannelMember fromMember) {
                try {
                    String json = message.getText();
                    if (json.startsWith("{")) {
                        org.json.JSONObject obj = new org.json.JSONObject(json);
                        if (obj.has("type") && obj.getString("type").equals("user_info")) {
                            String partnerName = obj.getString("name");
                            String partnerAge = obj.getString("age");
                            String partnerCountry = obj.getString("country");
                            String partnerUserId = obj.getString("userId");
                            if (partnerInfoListener != null) {
                                partnerInfoListener.onPartnerInfoReceived(partnerName, partnerAge, partnerCountry);
                                partnerInfoListener.onPartnerUserId(partnerUserId);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Lỗi parse RTM message: " + e.getMessage());
                }
            }
            @Override public void onMemberJoined(RtmChannelMember member) {}
            @Override public void onMemberLeft(RtmChannelMember member) {}
        });
        rtmChannel.join(new ResultCallback<Void>() {
            @Override public void onSuccess(Void aVoid) {
                sendUserInfoToChannel(name, age, country);
            }
            @Override public void onFailure(ErrorInfo errorInfo) {
                if (partnerInfoListener != null) partnerInfoListener.onError("Lỗi join channel: " + errorInfo.getErrorDescription());
            }
        });
    }

    private void sendUserInfoToChannel(String name, String age, String country) {
        try {
            org.json.JSONObject obj = new org.json.JSONObject();
            obj.put("type", "user_info");
            obj.put("name", name);
            obj.put("age", age);
            obj.put("country", country);
            obj.put("userId", userId);
            RtmMessage msg = rtmClient.createMessage(obj.toString());
            rtmChannel.sendMessage(msg, new ResultCallback<Void>() {
                @Override public void onSuccess(Void aVoid) {}
                @Override public void onFailure(ErrorInfo errorInfo) {
                    if (partnerInfoListener != null) partnerInfoListener.onError("Lỗi gửi info: " + errorInfo.getErrorDescription());
                }
            });
        } catch (Exception e) {
            if (partnerInfoListener != null) partnerInfoListener.onError("Lỗi tạo/gửi info: " + e.getMessage());
        }
    }

    public void logout() {
        if (rtmChannel != null) rtmChannel.leave(null);
        if (rtmClient != null) rtmClient.logout(null);
    }
} 
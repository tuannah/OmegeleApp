# Hướng dẫn lấy Agora API cho ứng dụng Omegle

## Bước 1 Đăng ký tài khoản Agora

1cập [Agora Console](https://console.agora.io/)
2. Nhấn **"Sign Up** hoặc **"Đăng ký"**3. Điền thông tin:
   - Email
   - Mật khẩu
   - Tên công ty (có thể để trống)4Xác thực email qua link được gửi

## Bước 2 Tạo Project

1. Đăng nhập vào [Agora Console](https://console.agora.io/)2 **Create Project** hoặc **Tạo dự án"**3. Điền thông tin:
   - **Project Name**: `OmegleApp` (hoặc tên bạn muốn)
   - **Authentication**: Chọn **"No Auth"** (cho test) hoặc **"Token"** (cho production)
4hấn **"Submit"**

## Bước3Lấy App ID

1. Sau khi tạo project, bạn sẽ thấy **App ID** (dạng: `1234567890ef1234567890abcdef`)
2. **Copy App ID này** - đây chính là `AGORA_APP_ID` cần dùng trong code

## Bước 4: Cập nhật code

1. Mở file `app/src/main/java/com/example/omegeleapp/managers/AgoraVoiceManager.java`
2. Tìm dòng:
   ```java
   private static final String AGORA_APP_ID = YOUR_AGORA_APP_ID";
   ```
3 Thay thế `YOUR_AGORA_APP_ID` bằng App ID thật:
   ```java
   private static final String AGORA_APP_ID =1234567890ef1234567890bcdef;
   ```

## Bước 5: Cấu hình Authentication (Tùy chọn)

### Nếu chọnNo Auth (Khuyến nghị cho test):
- Không cần làm gì thêm
- Token sẽ được để `null` trong code

### Nếu chọn "Token" (Cho production):1 Trong project, chọn tab **"Project Management**
2. Chọn**"App Certificate"**
3. Tạo **App Certificate** (một chuỗi secret)
4ng **Token Builder** để tạo token:
   - Channel Name: tên kênh
   - UID: ID người dùng (có thể để 0)
   - App Certificate: chuỗi secret vừa tạo
   - Expire Time: thời gian hết hạn

## Bước 6: Test ứng dụng
1. Build và cài đặt app lên 2 thiết bị
2. Mở app trên cả 2hiết bị
3. Đăng nhập và nhấnStart Voice Call
4. Cả 2iết bị sẽ tham gia cùng một kênh và có thể nói chuyện

## Lưu ý quan trọng

### Quota miễn phí:
- **100/tháng** cho voice call
- **10/tháng** cho video call
- Đủ cho việc test và phát triển

### Bảo mật:
- **Không commit App ID** lên Git public
- Sử dụng **Token authentication** cho production
- Lưu App ID trong **local.properties** hoặc **BuildConfig**

### Troubleshooting:
- **Lỗi 11**: App ID không đúng
- **Lỗi 102*: Token không hợp lệ
- **Lỗi103**: Kênh không tồn tại
- **Lỗi 14**: Không có quyền truy cập

## Ví dụ sử dụng trong code

```java
// Khởi tạo AgoraVoiceManager
AgoraVoiceManager voiceManager = new AgoraVoiceManager(this);

// Thiết lập listener
voiceManager.setVoiceCallListener(new AgoraVoiceManager.VoiceCallListener() {
    @Override
    public void onUserJoined(int uid) [object Object]       // Có người tham gia kênh
    }
    
    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed)[object Object]       // Tham gia kênh thành công
    }
    
    @Override
    public void onLeaveChannel() [object Object]        // Rời kênh
    }
    
    @Override
    public void onError(int errorCode) [object Object]
        // Có lỗi xảy ra
    }
});

// Tham gia kênh
voiceManager.joinChannel("omegele_room_123Bật/tắt microphone
voiceManager.setMicrophoneEnabled(true);

// Bật/tắt speaker
voiceManager.setSpeakerEnabled(false);

// Rời kênh
voiceManager.leaveChannel();

// Dọn dẹp
voiceManager.destroy();
```

## Liên kết hữu ích

- [Agora Console](https://console.agora.io/)
- [Agora Documentation](https://docs.agora.io/)
- [Android SDK Guide](https://docs.agora.io/en/Voice/API%20Reference/java/index.html)
- [Token Builder](https://console.agora.io/token-builder) 
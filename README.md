# Ứng dụng Omegle Android

## Tổng quan
Ứng dụng Omegle Android là một ứng dụng chat và gọi thoại với người lạ, được viết bằng Java cho Android. Ứng dụng hỗ trợ cả chat văn bản và cuộc gọi thoại, với giao diện hoàn toàn bằng tiếng Việt.

## Cấu trúc dự án

### 📁 Thư mục chính
```
OmegeleApp/
├── app/                          # Thư mục chính của ứng dụng
│   ├── src/main/                 # Source code chính
│   │   ├── java/com/example/omegeleapp/  # Java source files
│   │   ├── res/                  # Resources (layouts, strings, drawables)
│   │   └── AndroidManifest.xml   # Cấu hình ứng dụng
│   └── build.gradle.kts          # Cấu hình build
├── gradle/                       # Gradle wrapper
└── build.gradle.kts              # Root build configuration
```

### 🔧 Các file Java chính

#### 1. **MainActivity.java** - Màn hình chính
- **Chức năng**: Hiển thị form nhập thông tin người dùng và các nút để bắt đầu chat/call
- **Tính năng chính**:
  - Form nhập tên, tuổi, sở thích, quốc tịch
  - Lưu trữ thông tin vào SharedPreferences
  - Validate dữ liệu đầu vào
  - Chuyển hướng đến ChatActivity hoặc VoiceCallActivity

#### 2. **SignalingManager.java** - Quản lý kết nối signaling
- **Chức năng**: Quản lý kết nối và giao tiếp với signaling server
- **Tính năng chính**:
  - Kết nối với AWS Lambda signaling server qua HTTP API
  - Tìm kiếm và kết nối với người dùng khác
  - Gửi/nhận WebRTC messages (offer, answer, ICE candidate)
  - Gửi/nhận tin nhắn chat
  - Quản lý trạng thái phòng và kết nối

#### 3. **VoiceCallActivity.java** - Màn hình cuộc gọi thoại
- **Chức năng**: Quản lý cuộc gọi thoại với tính năng chat tích hợp
- **Tính năng chính**:
  - Quản lý WebRTC connection (mock implementation)
  - Tích hợp chat văn bản trong khi gọi
  - Điều khiển âm thanh (mute, speaker, end call)
  - Quản lý quyền microphone
  - Chuyển đổi hiển thị/ẩn chat panel

#### 4. **ChatActivity.java** - Màn hình chat văn bản
- **Chức năng**: Màn hình chat văn bản đơn giản với người lạ
- **Tính năng chính**:
  - Giao diện chat văn bản
  - Kết nối với signaling server để tìm người chat
  - Gửi và nhận tin nhắn
  - Hiển thị trạng thái kết nối

#### 5. **WebRTCManager.java** - Quản lý WebRTC
- **Chức năng**: Quản lý WebRTC connection cho cuộc gọi thoại
- **Tính năng chính**:
  - Mock implementation của WebRTC (do chưa tích hợp thật)
  - Quản lý peer connection, audio tracks
  - Xử lý audio recording và playback
  - Thiết lập audio settings
  - Tạo offer/answer cho WebRTC handshake

#### 6. **UserDatabaseHelper.java** - Quản lý database
- **Chức năng**: Quản lý cơ sở dữ liệu SQLite cho người dùng
- **Tính năng chính**:
  - Tạo và quản lý database SQLite
  - Lưu trữ thông tin đăng nhập (email, password)
  - Thêm và kiểm tra người dùng
  - Xử lý database schema

#### 7. **LoginActivity.java** - Màn hình đăng nhập
- **Chức năng**: Màn hình đăng nhập với email và mật khẩu
- **Tính năng chính**:
  - Form đăng nhập
  - Kiểm tra thông tin với database
  - Chuyển hướng đến MainActivity khi thành công
  - Link đến RegisterActivity

#### 8. **RegisterActivity.java** - Màn hình đăng ký
- **Chức năng**: Màn hình đăng ký tài khoản mới
- **Tính năng chính**:
  - Form đăng ký
  - Validate dữ liệu đầu vào
  - Thêm người dùng mới vào database
  - Hiển thị thông báo thành công/thất bại

### 📦 Các class hỗ trợ

#### 9. **Country.java** - Lớp đại diện quốc gia
- **Chức năng**: Lưu trữ thông tin quốc gia và cờ
- **Sử dụng**: Trong Spinner để chọn quốc tịch

#### 10. **CountryAdapter.java** - Adapter cho Spinner quốc gia
- **Chức năng**: Hiển thị danh sách quốc gia với cờ
- **Tính năng**: Sử dụng ViewHolder pattern, tùy chỉnh giao diện

#### 11. **Message.java** - Lớp đại diện tin nhắn
- **Chức năng**: Lưu trữ thông tin tin nhắn (người gửi, nội dung, trạng thái)
- **Sử dụng**: Trong MessageAdapter để hiển thị chat

#### 12. **MessageAdapter.java** - Adapter cho ListView tin nhắn
- **Chức năng**: Hiển thị danh sách tin nhắn trong chat
- **Tính năng**: Phân biệt tin nhắn của mình và người khác, căn chỉnh khác nhau

### 🎨 Resources (res/)

#### Layouts
- **activity_main.xml**: Layout màn hình chính với form nhập thông tin
- **activity_chat.xml**: Layout màn hình chat văn bản
- **activity_voice_call.xml**: Layout màn hình cuộc gọi thoại
- **activity_login.xml**: Layout màn hình đăng nhập
- **activity_register.xml**: Layout màn hình đăng ký
- **item_country.xml**: Layout cho item trong Spinner quốc gia

#### Strings (values/strings.xml)
- Tất cả text trong ứng dụng đã được dịch sang tiếng Việt
- Bao gồm: tên ứng dụng, hints, button text, thông báo lỗi

#### Drawables
- **flag_usa.png**: Cờ Hoa Kỳ
- **flag_vietnam.png**: Cờ Việt Nam
- **ic_launcher.xml**: Icon ứng dụng

### ⚙️ Cấu hình

#### build.gradle.kts
- **Dependencies chính**:
  - AndroidX libraries (AppCompat, Material, Activity, ConstraintLayout)
  - Socket.IO client (cho real-time communication)
  - OkHttp (cho HTTP requests)
  - Gson (cho JSON parsing)
  - Dexter (cho permission handling)

#### AndroidManifest.xml
- **Permissions**:
  - INTERNET: Truy cập internet
  - RECORD_AUDIO: Ghi âm cho voice call
  - MODIFY_AUDIO_SETTINGS: Thay đổi cài đặt âm thanh
  - ACCESS_NETWORK_STATE: Kiểm tra trạng thái mạng
  - WAKE_LOCK: Giữ màn hình sáng
  - FOREGROUND_SERVICE: Chạy service background

- **Activities**:
  - LoginActivity (launcher)
  - MainActivity
  - RegisterActivity
  - ChatActivity
  - VoiceCallActivity
  - VoiceCallWaitingActivity
  - VoiceCallSettingsActivity

## Tính năng chính

### 🔐 Hệ thống xác thực
- Đăng ký tài khoản với email/password
- Đăng nhập và lưu trữ thông tin
- Sử dụng SQLite database local

### 💬 Chat văn bản
- Kết nối với signaling server để tìm người chat
- Gửi và nhận tin nhắn văn bản
- Hiển thị trạng thái kết nối
- Giao diện chat đơn giản và dễ sử dụng

### 📞 Cuộc gọi thoại
- Tích hợp WebRTC (hiện tại là mock implementation)
- Điều khiển âm thanh (mute, speaker, end call)
- Chat văn bản tích hợp trong cuộc gọi
- Quản lý quyền microphone
- Giữ màn hình sáng khi gọi

### 🌐 Signaling Server
- Sử dụng AWS Lambda + API Gateway
- HTTP-based signaling thay vì WebSocket
- Hỗ trợ tìm kiếm người chat
- Quản lý phòng và kết nối

## Công nghệ sử dụng

### Frontend
- **Java**: Ngôn ngữ lập trình chính
- **Android SDK**: Framework phát triển
- **Material Design**: UI/UX guidelines
- **ConstraintLayout**: Layout system

### Backend (Signaling)
- **AWS Lambda**: Serverless computing
- **API Gateway**: HTTP API management
- **DynamoDB**: NoSQL database
- **HTTP/JSON**: Communication protocol

### Libraries
- **OkHttp**: HTTP client
- **Gson**: JSON parsing
- **Dexter**: Permission handling
- **Socket.IO**: Real-time communication (backup)

## Hướng dẫn sử dụng

### Cài đặt
1. Clone repository
2. Mở project trong Android Studio
3. Sync Gradle files
4. Build và chạy trên thiết bị/emulator

### Sử dụng
1. Đăng ký tài khoản mới
2. Đăng nhập vào ứng dụng
3. Nhập thông tin cá nhân
4. Chọn "Bắt đầu chat văn bản" hoặc "Bắt đầu cuộc gọi thoại"
5. Chờ kết nối với người lạ
6. Bắt đầu trò chuyện!

## Lưu ý kỹ thuật

### WebRTC Implementation
- Hiện tại sử dụng mock implementation
- Cần tích hợp WebRTC thật để có cuộc gọi thực tế
- Có thể sử dụng Agora SDK hoặc WebRTC native

### Signaling Server
- URL trong SignalingManager cần được cập nhật
- Cần deploy AWS Lambda function
- Cấu hình DynamoDB table

### Permissions
- Ứng dụng yêu cầu quyền microphone cho voice call
- Sử dụng Dexter để xử lý runtime permissions

## Phát triển tiếp theo

### Tính năng có thể thêm
- Video call
- File sharing
- User profiles
- Chat history
- Push notifications
- Multi-language support

### Cải thiện kỹ thuật
- Tích hợp WebRTC thật
- Implement proper error handling
- Add unit tests
- Optimize performance
- Improve UI/UX

## Đóng góp

Dự án này được phát triển như một ứng dụng demo. Mọi đóng góp đều được chào đón!

## License

MIT License - Xem file LICENSE để biết thêm chi tiết. 
# 🎤 Voice Chat Testing Guide

Hướng dẫn test kết nối voice chat trong ứng dụng OmegeleApp.

## 🚀 Quick Start

### Bước1: Chuẩn bị thiết bị
- **2Android Emulator** hoặc
- **1Emulator +1thiết bị thật** hoặc  
- **2 thiết bị thật**

### Bước 2: Chạy script test
```bash
./test_voice_chat.sh
```

### Bước 3: Chọn option 7 để full test
Script sẽ tự động:
- Kiểm tra thiết bị
- Build ứng dụng
- Cài đặt trên tất cả thiết bị
- Cấp quyền microphone
- Khởi chạy app

## 📱 Manual Testing

### Bước1: Kiểm tra thiết bị
```bash
adb devices
```
Kết quả mong đợi:
```
List of devices attached
emulator-5554   device
emulator-5556   device
```

### Bước2: Build và cài đặt
```bash
./gradlew clean
./gradlew assembleDebug
```

Cài đặt trên từng thiết bị:
```bash
adb -s emulator-5554stall -r app/build/outputs/apk/debug/app-debug.apk
adb -s emulator-5556stall -r app/build/outputs/apk/debug/app-debug.apk
```

### Bước 3: Cấp quyền
```bash
adb -s emulator-5554shell pm grant com.example.omegeleapp android.permission.RECORD_AUDIO
adb -s emulator-5554shell pm grant com.example.omegeleapp android.permission.MODIFY_AUDIO_SETTINGS
adb -s emulator-5556shell pm grant com.example.omegeleapp android.permission.RECORD_AUDIO
adb -s emulator-5556shell pm grant com.example.omegeleapp android.permission.MODIFY_AUDIO_SETTINGS
```

### Bước4 Khởi chạy app
```bash
adb -s emulator-5554 shell am start -n com.example.omegeleapp/.activities.MainActivity
adb -s emulator-5556 shell am start -n com.example.omegeleapp/.activities.MainActivity
```

## 🧪 Test Voice Chat

### Test Case 1Đăng ký và đăng nhập
1 **Thiết bị 1**: Đăng ký tài khoản mới
2 **Thiết bị 2**: Đăng ký tài khoản khác
3. **Kiểm tra**: Dữ liệu được lưu đúng

### Test Case 2: Voice Call
1 **Thiết bị 1: 
   - Nhập thông tin profile
   - TapStart Voice Call"
   - Grant microphone permission
   - Chờ kết nối
2. **Thiết bị 2**:
   - Làm tương tự
   - Chờ kết nối
3. **Kiểm tra**:
   - Cả 2 thiết bị hiển thị thông tin partner
   - Call timer hoạt động
   - Controls (mute, speaker) hoạt động
   - Audio quality tốt

## 📊 Monitoring

### Xem logs real-time
```bash
# Xem logs của app
adb logcat | grep -E(VoiceCall|LocalVoice|RtmSignaling|OmegeleApp)"

# Xem logs của specific device
adb -s emulator-5554ogcat | grep -E(VoiceCall|LocalVoice|RtmSignaling|OmegeleApp)
```

### Key log tags để theo dõi
- `VoiceCallActivity`: Main call interface
- `LocalVoiceManager`: Local UDP voice handling
- `RtmSignalingManager`: Agora RTM signaling
- `OmegeleApp`: General app logs

## 🔧 Troubleshooting

### Vấn đề thường gặp

####1 Emulator không có microphone
```bash
# Kiểm tra microphone
adb -s emulator-5554hell dumpsys audio | grep -i mic

# Nếu không có, tạo emulator mới với microphone support
```

#### 2. App không install được
```bash
# Uninstall trước
adb -s emulator-5554nstall com.example.omegeleapp

# Install lại
adb -s emulator-5554 install app/build/outputs/apk/debug/app-debug.apk
```

#### 3. Permission denied
```bash
# Grant permissions manually
adb -s emulator-5554shell pm grant com.example.omegeleapp android.permission.RECORD_AUDIO
adb -s emulator-5554shell pm grant com.example.omegeleapp android.permission.MODIFY_AUDIO_SETTINGS
```

#### 4. Voice call không kết nối
- Kiểm tra internet connection
- Kiểm tra Agora App ID và Token
- Xem logs để debug

## 📈 Test Results Template

### Test Session: [Date]
```
Devices Used:
- Device1ulator/Real Device] - [ID]
- Device2ulator/Real Device] - [ID]

Test Results:
✅ Registration: [Pass/Fail]
✅ Login: [Pass/Fail]
✅ Voice Call UI: [Pass/Fail]
✅ Call Controls: [Pass/Fail]
✅ Audio Quality: [Pass/Fail]
✅ Partner Info Display: [Pass/Fail]
✅ Call Timer: [Pass/Fail]

Issues Found:
- Issue 1]
- Issue 2]

Notes:
[Additional observations]
```

## 🎯 Tips cho testing hiệu quả

### 1. Sử dụng Test Mode
- App có test mode để bypass waiting time
- Nhanh chóng test call interface

###2Monitor Device IDs
- Mỗi thiết bị có unique device ID
- Dễ dàng phân biệt trong logs

### 3. Use Multiple Terminals
```bash
# Terminal 1: Build và install
./gradlew assembleDebug

# Terminal 2: Monitor logs
adb logcat | grep -E(VoiceCall|LocalVoice)"

# Terminal 3: Control devices
adb devices
```

## 🔍 Voice Chat Architecture

Ứng dụng sử dụng 2 hệ thống voice chat:

###1. Local Voice (UDP)
- **Manager**: `LocalVoiceManager`
- **Protocol**: UDP streaming
- **Connection**: Peer-to-peer
- **Port**: 12345

### 2. Agora RTM Signaling
- **Manager**: `RtmSignalingManager`
- **Protocol**: Agora RTM
- **Connection**: Cloud-based
- **Features**: Partner matching, user info exchange

### 3. Voice Call Activity
- **File**: `VoiceCallActivity.java`
- **Features**: 
  - UI controls (mute, speaker, end call)
  - Partner info display
  - Connection timer
  - Error handling

## 📞 Expected Behavior

### Khi kết nối thành công:
1. Cả 2 thiết bị hiển thị thông tin partner2Call timer bắt đầu đếm3 Audio controls hoạt động
4 Có thể nói chuyện 2 chiều

### Khi có lỗi:1. Hiển thị thông báo lỗi
2 Logs ghi chi tiết lỗi3. Có thể retry kết nối

## 🎉 Success Criteria

Voice chat được coi là hoạt động tốt khi:
- ✅ Kết nối thành công giữa 2 thiết bị
- ✅ Audio quality rõ ràng, không lag
- ✅ Controls (mute, speaker) hoạt động
- ✅ Partner info hiển thị đúng
- ✅ Call timer chính xác
- ✅ End call hoạt động
- ✅ Không crash app 
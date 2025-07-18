# 🧪 Voice Call App Testing Guide

Hướng dẫn chi tiết để test ứng dụng voice call trên nhiều thiết bị.

## 📋 **Yêu cầu hệ thống**

### **1. Android Studio**
- Android Studio Arctic Fox hoặc mới hơn
- Android SDK 26+ (API level 26)
- ADB (Android Debug Bridge)

### **2. Thiết bị test**
- **2 Android Emulator** hoặc
- **1 Emulator + 1 thiết bị thật** hoặc
- **2 thiết bị thật**

## 🚀 **Cách 1: Sử dụng Script tự động**

### **Bước 1: Cấp quyền cho script**
```bash
chmod +x test_voice_call.sh
```

### **Bước 2: Chạy script**
```bash
./test_voice_call.sh
```

### **Bước 3: Chọn option 8 để full test**
Script sẽ tự động:
- Build ứng dụng
- Install trên tất cả thiết bị
- Launch app trên tất cả thiết bị

## 🖥️ **Cách 2: Test thủ công**

### **Bước 1: Tạo 2 Android Emulator**

1. **Mở Android Studio**
2. **Vào Tools → AVD Manager**
3. **Tạo 2 emulator:**
   ```
   Emulator 1: Pixel 4, API 30
   Emulator 2: Pixel 5, API 30
   ```
4. **Đảm bảo cả 2 có microphone support**

### **Bước 2: Chạy emulators**
```bash
# Terminal 1
emulator -avd Pixel_4_API_30

# Terminal 2  
emulator -avd Pixel_5_API_30
```

### **Bước 3: Kiểm tra thiết bị**
```bash
adb devices
```
Kết quả mong đợi:
```
List of devices attached
emulator-5554    device
emulator-5556    device
```

### **Bước 4: Build ứng dụng**
```bash
./gradlew clean
./gradlew assembleDebug
```

### **Bước 5: Install trên cả 2 thiết bị**
```bash
# Install trên emulator 1
adb -s emulator-5554 install -r app/build/outputs/apk/debug/app-debug.apk

# Install trên emulator 2
adb -s emulator-5556 install -r app/build/outputs/apk/debug/app-debug.apk
```

### **Bước 6: Launch app trên cả 2 thiết bị**
```bash
# Launch trên emulator 1
adb -s emulator-5554 shell am start -n com.example.omegeleapp/.LoginActivity

# Launch trên emulator 2
adb -s emulator-5556 shell am start -n com.example.omegeleapp/.LoginActivity
```

## 📱 **Cách 3: Emulator + Thiết bị thật**

### **Bước 1: Kết nối thiết bị thật**
1. **Bật Developer Options** trên thiết bị
2. **Bật USB Debugging**
3. **Kết nối USB**
4. **Chấp nhận debug trên thiết bị**

### **Bước 2: Kiểm tra kết nối**
```bash
adb devices
```
Kết quả:
```
List of devices attached
emulator-5554    device
1234567890ABCDEF device
```

### **Bước 3: Install và launch**
```bash
# Install trên cả 2
adb -s emulator-5554 install -r app/build/outputs/apk/debug/app-debug.apk
adb -s 1234567890ABCDEF install -r app/build/outputs/apk/debug/app-debug.apk

# Launch trên cả 2
adb -s emulator-5554 shell am start -n com.example.omegeleapp/.LoginActivity
adb -s 1234567890ABCDEF shell am start -n com.example.omegeleapp/.LoginActivity
```

## 🧪 **Quy trình test**

### **Test Case 1: Đăng ký và đăng nhập**
1. **Thiết bị 1**: Đăng ký tài khoản mới
2. **Thiết bị 2**: Đăng ký tài khoản khác
3. **Kiểm tra**: Dữ liệu được lưu đúng

### **Test Case 2: Voice Call**
1. **Thiết bị 1**: 
   - Nhập thông tin profile
   - Tap "Start Voice Call"
   - Grant microphone permission
   - Tap "Test Mode" để test nhanh

2. **Thiết bị 2**:
   - Làm tương tự
   - Tap "Test Mode"

3. **Kiểm tra**:
   - Cả 2 thiết bị hiển thị device ID khác nhau
   - Call timer hoạt động
   - Controls (mute, speaker) hoạt động

### **Test Case 3: Settings**
1. **Thiết bị 1**: 
   - Vào Settings
   - Thay đổi audio quality
   - Set age range
   - Save settings

2. **Kiểm tra**: Settings được lưu và áp dụng

## 📊 **Monitoring và Debug**

### **Xem logs real-time**
```bash
# Xem logs của app
adb logcat | grep -E "(VoiceCall|WebRTC|Signaling|OmegeleApp)"

# Xem logs của specific device
adb -s emulator-5554 logcat | grep -E "(VoiceCall|WebRTC|Signaling|OmegeleApp)"
```

### **Key log tags để theo dõi**
- `VoiceCallActivity`: Main call interface
- `VoiceCallWaitingActivity`: Waiting room
- `WebRTCManager`: Audio handling
- `SignalingManager`: Network communication

## 🔧 **Troubleshooting**

### **Vấn đề thường gặp**

#### **1. Emulator không có microphone**
```bash
# Kiểm tra microphone
adb -s emulator-5554 shell dumpsys audio | grep -i mic

# Nếu không có, tạo emulator mới với microphone support
```

#### **2. App không install được**
```bash
# Uninstall trước
adb -s emulator-5554 uninstall com.example.omegeleapp

# Install lại
adb -s emulator-5554 install app/build/outputs/apk/debug/app-debug.apk
```

#### **3. Permission denied**
```bash
# Grant permissions manually
adb -s emulator-5554 shell pm grant com.example.omegeleapp android.permission.RECORD_AUDIO
adb -s emulator-5554 shell pm grant com.example.omegeleapp android.permission.MODIFY_AUDIO_SETTINGS
```

#### **4. App crash**
```bash
# Xem crash logs
adb logcat | grep -i "fatal\|crash\|exception"
```

## 📈 **Test Results Template**

### **Test Session: [Date]**
```
Devices Used:
- Device 1: [Emulator/Real Device] - [ID]
- Device 2: [Emulator/Real Device] - [ID]

Test Results:
✅ Registration: [Pass/Fail]
✅ Login: [Pass/Fail]
✅ Voice Call UI: [Pass/Fail]
✅ Call Controls: [Pass/Fail]
✅ Settings: [Pass/Fail]
✅ Device ID Display: [Pass/Fail]
✅ Test Mode: [Pass/Fail]

Issues Found:
- [Issue 1]
- [Issue 2]

Notes:
[Additional observations]
```

## 🎯 **Tips cho testing hiệu quả**

### **1. Sử dụng Test Mode**
- Tap "Test Mode" để bypass waiting time
- Nhanh chóng test call interface

### **2. Monitor Device IDs**
- Mỗi thiết bị có unique device ID
- Dễ dàng phân biệt trong logs

### **3. Use Multiple Terminals**
```bash
# Terminal 1: Build và install
./gradlew assembleDebug

# Terminal 2: Monitor logs
adb logcat | grep -E "(VoiceCall|WebRTC)"

# Terminal 3: Control devices
adb devices
```

### **4. Save Test APK**
```bash
# Copy APK để share
cp app/build/outputs/apk/debug/app-debug.apk voice_call_test.apk
```

## 🚀 **Next Steps**

Sau khi test thành công:
1. **Implement real WebRTC** cho voice calling thực sự
2. **Set up signaling server** cho peer-to-peer
3. **Add video calling** support
4. **Implement user matching** algorithm

---

**Happy Testing! 🎉** 
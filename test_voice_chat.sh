#!/bin/bash

echo "=========================================="
echo "    VOICE CHAT TESTING SCRIPT"
echo "=========================================="
echo

# Thông tin ứng dụng
PACKAGE_NAME="com.example.omegeleapp
MAIN_ACTIVITY=".activities.MainActivity"
APK_PATH=app/build/outputs/apk/debug/app-debug.apk"

# Hàm kiểm tra thiết bị
check_devices() [object Object]   echo "[STEP] Kiểm tra thiết bị Android..."
    devices=$(adb devices | grep -vList of devices| grep device$ | cut -f1)
    
    if [ -z $devices" ]; then
        echo [ERROR] Không tìm thấy thiết bị Android nào được kết nối!"
        echo INFO] Hãy đảm bảo:"
        echo [INFO] 1. Thiết bị Android đã bật USB Debugging"
        echo [INFO] 2. Thiết bị đã được kết nối qua USB"
        echo "[INFO] 3. Emulator đã được khởi động"
        echo [INFO]4 lệnh: adb devices"
        exit1fi
    
    device_count=$(echo $devices" | wc -l)
    echo [INFO] Tìm thấy $device_count thiết bị:"
    echo $devices" | while read -r device; do
        device_name=$(adb -s "$device shellgetprop ro.product.model 2>/dev/null || echo Unknown)   android_version=$(adb -s "$device shell getprop ro.build.version.release 2>/dev/null || echo Unknown)       echo "[INFO]   - $device ($device_name, Android $android_version)"
    done
    
    if$device_count" -lt2 then
        echo [WARNING] Chỉ tìm thấy $device_count thiết bị. Cần ít nhất 2t bị để test voice call."
        echo[INFO] Bạn có muốn tiếp tục với $device_count thiết bị không? (y/n)"
        read -r response
        if ! $response" =~ ^[Yy]$ ]]; then
            echo "[INFO] Thoát script.
            exit 0
        fi
    fi
    
    DEVICE_LIST=($(echo$devices))}

# Hàm build ứng dụng
build_app() [object Object]echo "[STEP] Clean và build ứng dụng..."
    ./gradlew clean
    if  $? -ne0 then
        echo[ERROR] Clean thất bại!"
        exit1    fi
    
    ./gradlew assembleDebug
    if  $? -ne0 then
        echo[ERROR] Build thất bại!"
        exit1    fi
    
    if [ ! -f "$APK_PATH" ]; then
        echo [ERROR] APK không được tạo!"
        exit1  fi
    
    echo "[SUCCESS] Build thành công! APK: $APK_PATH
}
# Hàm cài đặt ứng dụng
install_app() [object Object]    local device=$1    echo "[STEP] Cài đặt ứng dụng trên thiết bị $device..."
    
    adb -s "$device uninstall $PACKAGE_NAME >/dev/null 2   adb -s$device install -r $APK_PATH
    if  $? -eq0 then
        echo [SUCCESS] Cài đặt thành công trên thiết bị $device else
        echo "[ERROR] Cài đặt thất bại trên thiết bị $device
        return1
    fi
}

# Hàm cấp quyền
grant_permissions() [object Object]    local device=$1    echo[STEP] Cấp quyền cho ứng dụng trên thiết bị $device..."
    
    adb -s $device" shell pm grant "$PACKAGE_NAME" android.permission.RECORD_AUDIO
    adb -s $device" shell pm grant "$PACKAGE_NAME" android.permission.MODIFY_AUDIO_SETTINGS
    adb -s $device" shell pm grant "$PACKAGE_NAME" android.permission.INTERNET
    adb -s $device" shell pm grant "$PACKAGE_NAME" android.permission.ACCESS_NETWORK_STATE
    
    echo[SUCCESS] Đã cấp quyền cho thiết bị $device
}

# Hàm khởi chạy ứng dụng
launch_app() [object Object]    local device=$1   echo[STEP] Khởi chạy ứng dụng trên thiết bị $device..."
    
    adb -s $device shell am start -n $PACKAGE_NAME/$MAIN_ACTIVITY
    if  $? -eq0 then
        echo SUCCESS] Khởi chạy thành công trên thiết bị $device else
        echo [ERROR] Khởi chạy thất bại trên thiết bị $device
        return1    fi
}

# Hàm hiển thị log
show_logs() [object Object]    local device=$1 local device_name=$(adb -s "$device shellgetprop ro.product.model 2>/dev/null || echo "Unknown")
    
    echo "[STEP] Hiển thị log cho thiết bị $device ($device_name)...   echo[INFO] Nhấn Ctrl+C để dừng log"
    
    adb -s $device logcat -s VoiceCallActivity:*" LocalVoiceManager:*" "RtmSignalingManager:* eleApp:*" AndroidRuntime:E" System.err:*"
}

# Hàm test voice call
test_voice_call() [object Object]   if ${#DEVICE_LIST[@]}" -lt2 then
        echo "[ERROR] Cần ít nhất 2t bị để test voice call!
        return1 fi
    
    local device1=${DEVICE_LIST[0]}
    local device2=${DEVICE_LIST1}
    
    echo "[STEP] === TEST VOICE CALL GIỮA 2 THIẾT BỊ ===  echo [INFO] Thiết bị 1: $device1  echo [INFO] Thiết bị 2$device2"
    
    echo
    echo INFO] 📋 HƯỚNG DẪN TEST VOICE CALL:
    echo "[INFO] 1. Trên thiết bị 1:   echo[INFO]    - Đăng nhập với tài khoản   echo[INFO]    - Nhấn Start Voice Call   echo[INFO]    - Chấp nhận quyền microphone   echo [INFO]    - Chờ kết nối..."
    echo
    echo "[INFO] 2. Trên thiết bị 2:   echo[INFO]    - Đăng nhập với tài khoản khác   echo[INFO]    - Nhấn Start Voice Call   echo[INFO]    - Chấp nhận quyền microphone   echo [INFO]    - Chờ kết nối..."
    echo
    echo [INFO] 3. Khi kết nối thành công:   echo[INFO]    - Cả 2hiết bị sẽ hiển thị thông tin partner   echo[INFO]    - Test các nút: Mute, Speaker, End Call   echo "[INFO]    - Nói chuyện để test audio"
    echo
    echo[INFO] 4. Để kết thúc: Nhấn 'End Call trên một thiết bị"
    echo
    
    echo [INFO] 📊 Hiển thị log cho cả 2 thiết bị..."
    
    adb -s$device1 logcat -s VoiceCallActivity:*" LocalVoiceManager:*" "RtmSignalingManager:* eleApp:*" AndroidRuntime:ESystem.err:*" > "log_device1_$(date +%Y%m%d_%H%M%S).txt2>&1&
    LOG_PID1=$!
    
    adb -s$device2 logcat -s VoiceCallActivity:*" LocalVoiceManager:*" "RtmSignalingManager:* eleApp:*" AndroidRuntime:ESystem.err:*" > "log_device2_$(date +%Y%m%d_%H%M%S).txt2>&1&
    LOG_PID2!
    
    echo "[SUCCESS] Log được lưu vào file riêng biệt   echo [INFO] Nhấn Enter để dừng log và thoát..."
    read -r
    
    kill $LOG_PID1G_PID2 2>/dev/null
    echo "[INFO] Đã dừng log.
}# Hàm chạy tự động
run_full_test() [object Object]  echo "[STEP] === CHẠY FULL TEST TỰ ĐỘNG ==="
    
    check_devices
    build_app
    
    for device in ${DEVICE_LIST[@]}"; do
        install_app$device"
        grant_permissions $device"
    done
    
    for device in ${DEVICE_LIST[@]}"; do
        launch_app$device       sleep 2
    done
    
    echo "[SUCCESS] === FULL TEST HOÀN TẤT ===
    echo [INFO] Ứng dụng đã được cài đặt và khởi chạy trên tất cả thiết bị.    echo [INFO] Bạn có thể bắt đầu test voice call giữa các thiết bị."
    echo
    echo INFO] 📋 BƯỚC TIẾP THEO:
    echo [INFO]1 Đăng nhập trên cả 2 thiết bị
    echo[INFO] 2. Nhấn Start Voice Call' trên cả 2
    echo [INFO] 3n quyền microphone
    echo "[INFO] 4 Test voice chat
}

# Hàm hiển thị menu
show_menu() {
    echo
    echo "==========================================   echo "    VOICE CHAT TESTING MENU"
    echo "=========================================="
    echo 1. Kiểm tra thiết bị"
    echo 2. Build và cài đặt app"
    echo "3 Test Voice Call (Agora RTM)"
    echo "4. Hiển thị log real-time   echo5. Cấp quyền cho app
    echo6.Khởi chạy app trên tất cả thiết bị"
    echo7.Full test (tự động)    echo "0 Thoát"
    echo "=========================================="
    echo -n "Chọn option: 
}

# Main script
main() {
    # Kiểm tra adb
    if ! command -v adb &> /dev/null; then
        echo [ERROR] ADB không được tìm thấy! Hãy cài đặt Android SDK."
        exit1
    fi
    
    # Kiểm tra gradlew
    if [ ! -f "./gradlew" ]; then
        echo "[ERROR] gradlew không được tìm thấy! Hãy chạy script từ thư mục gốc của project."
        exit1
    fi
    
    # Menu loop
    while true; do
        show_menu
        read -r choice
        
        case $choice in
            1             check_devices
                ;;
            2             check_devices
                build_app
                for device in ${DEVICE_LIST[@]}"; do
                    install_app "$device"
                done
                ;;
            3              test_voice_call
                ;;
            4                if ${#DEVICE_LIST[@]}" -ge 1 ]; then
                    show_logs ${DEVICE_LIST[0]}"
                else
                    check_devices
                    show_logs ${DEVICE_LIST[0]}"
                fi
                ;;
            5             check_devices
                for device in ${DEVICE_LIST[@]}"; do
                    grant_permissions "$device"
                done
                ;;
            6             check_devices
                for device in ${DEVICE_LIST[@]}"; do
                    launch_app "$device"
                    sleep1              done
                ;;
            7               run_full_test
                ;;
            0              echo "[INFO] Thoát script."
                exit0               ;;
            *)
                echo "[ERROR] Option không hợp lệ!"
                ;;
        esac
        
        echo
        echo [INFO] Nhấn Enter để tiếp tục...       read -r
    done
}

# Chạy main function
main "$@" 
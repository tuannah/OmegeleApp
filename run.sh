#!/bin/bash

# Script chạy ứng dụng Omegle trên 2 thiết bị Android
# Tác giả: Assistant
# Ngày tạo: $(date)

# Màu sắc cho output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Thông tin ứng dụng
PACKAGE_NAME="com.example.omegeleapp"
MAIN_ACTIVITY=".activities.MainActivity"
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"

# Hàm in thông báo với màu
print_message() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# Hàm kiểm tra thiết bị có sẵn
check_devices() {
    print_step "Kiểm tra thiết bị Android..."
    
    # Lấy danh sách thiết bị
    devices=$(adb devices | grep -v "List of devices" | grep "device$" | cut -f1)
    
    if [ -z "$devices" ]; then
        print_error "Không tìm thấy thiết bị Android nào được kết nối!"
        print_message "Hãy đảm bảo:"
        print_message "1. Thiết bị Android đã bật USB Debugging"
        print_message "2. Thiết bị đã được kết nối qua USB"
        print_message "3. Emulator đã được khởi động"
        exit 1
    fi
    
    # Đếm số thiết bị
    device_count=$(echo "$devices" | wc -l)
    print_message "Tìm thấy $device_count thiết bị:"
    
    # Hiển thị danh sách thiết bị
    echo "$devices" | while read -r device; do
        device_name=$(adb -s "$device" shell getprop ro.product.model 2>/dev/null || echo "Unknown")
        print_message "  - $device ($device_name)"
    done
    
    # Kiểm tra có đủ 2 thiết bị không
    if [ "$device_count" -lt 2 ]; then
        print_warning "Chỉ tìm thấy $device_count thiết bị. Cần ít nhất 2 thiết bị để test voice call."
        print_message "Bạn có muốn tiếp tục với $device_count thiết bị không? (y/n)"
        read -r response
        if ! [[ "$response" =~ ^[Yy]$ ]]; then
            print_message "Thoát script."
            exit 0
        fi
    fi
    
    # Lưu danh sách thiết bị vào biến
    DEVICE_LIST=($(echo "$devices"))
}

# Hàm build ứng dụng
build_app() {
    print_step "Clean project..."
    ./gradlew clean
    if [ $? -ne 0 ]; then
        print_error "Clean thất bại!"
        exit 1
    fi
    print_step "Build ứng dụng..."
    if [ ! -f "$APK_PATH" ]; then
        print_message "APK chưa tồn tại, đang build..."
        ./gradlew assembleDebug
        if [ $? -ne 0 ]; then
            print_error "Build thất bại!"
            exit 1
        fi
    else
        print_message "APK đã tồn tại, bỏ qua build."
    fi
    print_message "Build thành công!"
}
# Hàm cài đặt ứng dụng trên thiết bị
install_app() {
    local device=$1
    print_step "Cài đặt ứng dụng trên thiết bị $device..."
    
    # Gỡ cài đặt nếu đã có
    adb -s "$device" uninstall "$PACKAGE_NAME" >/dev/null 2>&1    
    # Cài đặt APK mới
    adb -s "$device" install -r "$APK_PATH"    
    if [ $? -eq 0 ]; then
        print_message "Cài đặt thành công trên thiết bị $device"
    else
        print_error "Cài đặt thất bại trên thiết bị $device"
        return 1
    fi
}

# Hàm khởi chạy ứng dụng trên thiết bị
launch_app() {
    local device=$1
    print_step "Khởi chạy ứng dụng trên thiết bị $device..."
    
    adb -s "$device" shell am start -n "$PACKAGE_NAME/$MAIN_ACTIVITY"    
    if [ $? -eq 0 ]; then
        print_message "Khởi chạy thành công trên thiết bị $device"
    else
        print_error "Khởi chạy thất bại trên thiết bị $device"
        return 1
    fi
}

# Hàm hiển thị log của thiết bị
show_logs() {
    local device=$1
    local device_name=$(adb -s "$device" shell getprop ro.product.model 2>/dev/null || echo "Unknown")
    
    print_step "Hiển thị log cho thiết bị $device ($device_name)..."
    print_message "Nhấn Ctrl+C để dừng log"
    
    adb -s "$device" logcat -s "OmegeleApp:*" AndroidRuntime:E" System.err:*"
}

# Hàm test voice call giữa 2 thiết bị
test_voice_call() {
    if [ "${#DEVICE_LIST[@]}" -lt 2 ]; then
        print_error "Cần ít nhất 2 thiết bị để test voice call!"
        return 1
    fi
    
    local device1=${DEVICE_LIST[0]}
    local device2=${DEVICE_LIST[1]}
    print_step "Test voice call giữa 2 thiết bị..."
    print_message "Thiết bị 1: $device1"
    print_message "Thiết bị 2: $device2"
    
    print_message "Hướng dẫn test:"
    print_message "1. Trên thiết bị 1: Đăng nhập và nhấn Start Voice Call"
    print_message "2. Trên thiết bị 2: Đăng nhập và nhấn Start Voice Call"
    print_message "3i thiết bị sẽ được kết nối và có thể nói chuyện"
    print_message "4. Nhấn End Call để kết thúc cuộc gọi"
    
    # Hiển thị log cho cả 2 thiết bị
    print_message "Hiển thị log cho cả 2 thiết bị..."
    
    # Chạy log cho thiết bị 1 trong background
    adb -s "$device1" logcat -s "OmegeleApp:*" AndroidRuntime:E" System.err:*" > log_device1.txt 2>&1 &
    LOG_PID1=$!
    
    # Chạy log cho thiết bị 2 trong background
    adb -s "$device2" logcat -s "OmegeleApp:*" AndroidRuntime:E" System.err:*" > log_device2.txt 2>&1 &
    LOG_PID2=$!
    
    print_message "Log được lưu vào log_device1.txt và log_device2.txt"
    print_message "Nhấn Enter để dừng log và thoát..."
    read -r
    
    # Dừng log
    kill $LOG_PID1 $LOG_PID2 2>/dev/null
    print_message "Đã dừng log."
}

# Hàm dọn dẹp
cleanup() {
    print_step "Dọn dẹp..."
    
    # Dừng tất cả log đang chạy
    pkill -f "adb.*logcat" 2>/dev/null
    
    # Xóa file log tạm
    rm -f log_device*.txt 2>/dev/null
    
    print_message "Dọn dẹp hoàn tất."
}

# Hàm chạy tự động
run_automatically() {
    print_step "Chạy tự động cho 2 thiết bị..."
    
    # Kiểm tra thiết bị
    check_devices
    
    # Build ứng dụng
    build_app
    
    # Cài đặt và khởi chạy trên tất cả thiết bị
    for device in "${DEVICE_LIST[@]}"; do
        install_app "$device"
        launch_app "$device"
    done
    
    print_message "Hoàn tất! Ứng dụng đã được cài đặt và khởi chạy trên tất cả thiết bị."
    print_message "Bạn có thể bắt đầu test voice call giữa các thiết bị."
}

# Main script
main() {
    echo "=========================================="
    echo "    SCRIPT CHẠY ỨNG DỤNG OMEGLE"
    echo "=========================================="
    echo
    # Kiểm tra adb có sẵn không
    if ! command -v adb &> /dev/null; then
        print_error "ADB không được tìm thấy! Hãy cài đặt Android SDK."
        exit 1
    fi
    # Kiểm tra gradlew có sẵn không
    if [ ! -f "./gradlew" ]; then
        print_error "gradlew không được tìm thấy! Hãy chạy script từ thư mục gốc của project."
        exit 1
    fi
    # Chạy tự động
    run_automatically
}

# Trap để dọn dẹp khi thoát
trap cleanup EXIT

# Chạy main function
main "$@" 
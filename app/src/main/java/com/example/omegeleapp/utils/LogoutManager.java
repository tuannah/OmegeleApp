package com.example.omegeleapp.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.omegeleapp.activities.LoginActivity;

/**
 * LogoutManager - Utility class để quản lý logout chung cho toàn bộ ứng dụng
 * 
 * Chức năng chính:
 * - Hiển thị dialog xác nhận đăng xuất
 * - Xóa dữ liệu người dùng từ SharedPreferences
 * - Chuyển về màn hình đăng nhập
 * - Cung cấp method logout chung cho tất cả activity
 */
public class LogoutManager {
    /**
     * Hiển thị dialog xác nhận đăng xuất
     * @param context Context của activity
     * @param onConfirm Callback khi người dùng xác nhận đăng xuất
     */
    public static void showLogoutDialog(Context context, Runnable onConfirm) {
        new AlertDialog.Builder(context)
            .setTitle("Xác nhận đăng xuất")
            .setMessage("Bạn có chắc muốn đăng xuất?")
            .setPositiveButton("Có", (dialog, which) -> performLogout(context))
            .setNegativeButton("Không", null)
            .show();
    }

    /**
     * Thực hiện đăng xuất
     * @param context Context của activity
     */
    public static void performLogout(Context context) {
        // Xóa dữ liệu người dùng từ SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("UserData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        // Hiển thị thông báo thành công
        Toast.makeText(context, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show();

        // Chuyển về màn hình đăng nhập và xóa stack activity
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    /**
     * Thực hiện đăng xuất với callback tùy chỉnh
     * @param context Context của activity
     * @param onLogoutComplete Callback khi logout hoàn tất
     */
    public static void performLogout(Context context, Runnable onLogoutComplete) {
        // Xóa dữ liệu người dùng từ SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("UserData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        // Hiển thị thông báo thành công
        Toast.makeText(context, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show();

        // Thực hiện callback nếu có
        if (onLogoutComplete != null) {
            onLogoutComplete.run();
        }

        // Chuyển về màn hình đăng nhập và xóa stack activity
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
} 
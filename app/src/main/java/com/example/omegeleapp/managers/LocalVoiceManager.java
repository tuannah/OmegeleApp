package com.example.omegeleapp.managers;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * LocalVoiceManager - Quản lý voice call local sử dụng UDP streaming
 * 
 * Chức năng chính:
 * - Streaming audio trực tiếp qua UDP
 * - Không cần server trung gian
 * - Kết nối peer-to-peer đơn giản
 * - Hỗ trợ microphone và speaker control
 */
public class LocalVoiceManager {
    private static final String TAG = "LocalVoiceManager";
    
    // Audio configuration
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = android.media.AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = android.media.AudioFormat.ENCODING_PCM_16;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
    
    // Network configuration
    private static final int UDP_PORT = 12345;
    private static final int PACKET_SIZE = 1024;
    // Components
    private Context context;
    private AudioManager audioManager;
    private AudioRecord audioRecord;
    private AudioTrack audioTrack;
    private DatagramSocket socket;
    
    // Threads
    private Thread recordingThread;
    private Thread playbackThread;
    
    // State
    private AtomicBoolean isRecording = new AtomicBoolean(false);
    private AtomicBoolean isPlaying = new AtomicBoolean(false);
    private AtomicBoolean isConnected = new AtomicBoolean(false);
    
    // Partner info
    private String partnerIpAddress;
    private int partnerPort = UDP_PORT;
    
    // Callback listener
    private LocalVoiceListener listener;
    
    /**
     * Interface để lắng nghe sự kiện voice call local
     */
    public interface LocalVoiceListener {
        void onConnected();
        void onDisconnected();
        void onError(String error);
        void onAudioReceived(byte[] audioData);
    }
    
    /**
     * Constructor
     */
    public LocalVoiceManager(Context context) {
        this.context = context;
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        initializeAudio();
    }
    
    /**
     * Khởi tạo audio system
     */
    private void initializeAudio() {
        try {
            // Khởi tạo AudioRecord để ghi âm
            audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                BUFFER_SIZE
            );
            
            // Khởi tạo AudioTrack để phát âm
            audioTrack = new AudioTrack.Builder()
                .setAudioAttributes(new android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build())
                .setAudioFormat(new android.media.AudioFormat.Builder()
                    .setEncoding(AUDIO_FORMAT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(android.media.AudioFormat.CHANNEL_OUT_MONO)
                    .build())
                .setBufferSizeInBytes(BUFFER_SIZE)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build();
            
            // Khởi tạo UDP socket
            socket = new DatagramSocket(UDP_PORT);
            
            Log.d(TAG, "LocalVoiceManager initialized successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing LocalVoiceManager", e);
            if (listener != null) {
                listener.onError("Không thể khởi tạo audio system: " + e.getMessage());
            }
        }
    }
    
    /**
     * Kết nối với partner qua IP address
     */
    public void connectToPartner(String partnerIp) {
        if (isConnected.get()) {
            Log.w(TAG, "Already connected to partner");
            return;
        }
        
        this.partnerIpAddress = partnerIp;
        
        try {
            // Test connection
            InetAddress.getByName(partnerIp);
            
            // Bắt đầu recording và playback threads
            startRecording();
            startPlayback();
            
            isConnected.set(true);
            
            if (listener != null) {
                listener.onConnected();
            }
            
            Log.d(TAG, "Connected to partner: " + partnerIp);
            
        } catch (UnknownHostException e) {
            Log.e(TAG, "Invalid partner IP address: " + partnerIp);
            if (listener != null) {
                listener.onError("Địa chỉ IP không hợp lệ: " + partnerIp);
            }
        }
    }
    
    /**
     * Bắt đầu ghi âm và gửi qua UDP
     */
    private void startRecording() {
        if (isRecording.get()) {
            return;
        }
        
        recordingThread = new Thread(() -> {
            byte[] buffer = new byte[PACKET_SIZE];
            
            try {
                audioRecord.startRecording();
                isRecording.set(true);
                
                while (isRecording.get() && isConnected.get()) {
                    int bytesRead = audioRecord.read(buffer, 0, buffer.length);
                    
                    if (bytesRead > 0) {
                        // Gửi audio data qua UDP
                        sendAudioData(buffer, bytesRead);
                    }
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error in recording thread", e);
                if (listener != null) {
                    listener.onError("Lỗi ghi âm: " + e.getMessage());
                }
            } finally {
                if (audioRecord != null) {
                    audioRecord.stop();
                }
            }
        });
        
        recordingThread.start();
    }
    
    /**
     * Bắt đầu nhận và phát âm thanh
     */
    private void startPlayback() {
        if (isPlaying.get()) {
            return;
        }
        
        playbackThread = new Thread(() -> {
            byte[] buffer = new byte[PACKET_SIZE];
            
            try {
                audioTrack.play();
                isPlaying.set(true);
                
                while (isPlaying.get() && isConnected.get()) {
                    // Nhận audio data từ UDP
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    
                    // Phát âm thanh
                    audioTrack.write(packet.getData(), 0, packet.getLength());
                    
                    // Thông báo cho listener
                    if (listener != null) {
                        listener.onAudioReceived(packet.getData());
                    }
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error in playback thread", e);
                if (listener != null) {
                    listener.onError("Lỗi phát âm: " + e.getMessage());
                }
            } finally {
                if (audioTrack != null) {
                    audioTrack.stop();
                }
            }
        });
        
        playbackThread.start();
    }
    
    /**
     * Gửi audio data qua UDP
     */
    private void sendAudioData(byte[] data, int length) {
        try {
            DatagramPacket packet = new DatagramPacket(
                data, length,
                InetAddress.getByName(partnerIpAddress),
                partnerPort
            );
            socket.send(packet);
        } catch (Exception e) {
            Log.e(TAG, "Error sending audio data", e);
        }
    }
    
    /**
     * Bật/tắt microphone
     */
    public void setMicrophoneEnabled(boolean enabled) {
        if (enabled) {
            if (!isRecording.get() && isConnected.get()) {
                startRecording();
            }
        } else {
            isRecording.set(false);
        }
        Log.d(TAG, "Microphone " + (enabled ? "enabled" : "disabled"));
    }
    
    /**
     * Bật/tắt speaker
     */
    public void setSpeakerEnabled(boolean enabled) {
        audioManager.setSpeakerphoneOn(enabled);
        Log.d(TAG, "Speaker " + (enabled ? "enabled" : "disabled"));
    }
    
    /**
     * Ngắt kết nối
     */
    public void disconnect() {
        Log.d(TAG, "Disconnecting from partner");
        
        isConnected.set(false);
        isRecording.set(false);
        isPlaying.set(false);
        
        // Dừng threads
        if (recordingThread != null) {
            recordingThread.interrupt();
        }
        if (playbackThread != null) {
            playbackThread.interrupt();
        }
        
        // Dừng audio components
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
        }
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
        }
        
        // Đóng socket
        if (socket != null) {
            socket.close();
        }
        
        if (listener != null) {
            listener.onDisconnected();
        }
    }
    
    /**
     * Thiết lập listener
     */
    public void setLocalVoiceListener(LocalVoiceListener listener) {
        this.listener = listener;
    }
    
    /**
     * Kiểm tra trạng thái kết nối
     */
    public boolean isConnected() {
        return isConnected.get();
    }
    
    /**
     * Lấy IP address hiện tại của thiết bị
     */
    public String getLocalIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            Log.e(TAG, "Error getting local IP address", e);
            return "127.0.0.1";
        }
    }
    
    /**
     * Dọn dẹp tài nguyên
     */
    public void destroy() {
        disconnect();
    }
} 
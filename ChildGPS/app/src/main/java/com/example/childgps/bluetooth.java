package com.example.childgps;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class bluetooth extends Service {
    private static final String RASPBERRY_PI_MAC_ADDRESS = "2C:CF:67:65:FC:BA";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;

    @Override
    public void onCreate() {
        super.onCreate();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        connectToDevice();
    }

    @SuppressLint("MissingPermission")
    private void connectToDevice() {
        BluetoothDevice raspberryPi = bluetoothAdapter.getRemoteDevice(RASPBERRY_PI_MAC_ADDRESS);
        try {
            bluetoothSocket = raspberryPi.createRfcommSocketToServiceRecord(MY_UUID);
            bluetoothSocket.connect(); // 연결 시도
            inputStream = bluetoothSocket.getInputStream();
            new Thread(new ReceiveThread()).start(); // 스레드 시작
        } catch (IOException e) {
            Log.e("BluetoothService", "연결 실패", e);
            stopSelf(); // 연결 실패 시 서비스 종료
        }
    }

    private class ReceiveThread implements Runnable {
        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (!Thread.currentThread().isInterrupted()) { // 스레드가 중단될 때까지 유지
                try {
                    if ((bytes = inputStream.read(buffer)) > 0) {
                        String receivedMessage = new String(buffer, 0, bytes, "utf-8");
                        Log.d("BluetoothService", "수신된 메시지: " + receivedMessage);

                        // 원하는 UI 업데이트 로직이 있다면 Broadcast나 Handler를 사용
                    }
                } catch (IOException e) {
                    Log.e("BluetoothService", "수신 실패", e);
                    break; // 예외 발생 시 루프 종료
                }
            }
            closeConnection();
        }
    }

    private void closeConnection() {
        try {
            if (inputStream != null) inputStream.close();
            if (bluetoothSocket != null) bluetoothSocket.close();
        } catch (IOException e) {
            Log.e("BluetoothService", "소켓 닫기 실패", e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeConnection();
    }
}

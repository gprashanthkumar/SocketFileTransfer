package com.example.androidsocketft;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ClientActivity extends AppCompatActivity {

    EditText editTextAddress;
    Button buttonConnect;
    TextView textPort;

    static final int SocketServerPORT = 8080;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        editTextAddress = (EditText) findViewById(R.id.address);
        textPort = (TextView) findViewById(R.id.port);
        textPort.setText("port: " + SocketServerPORT);
        buttonConnect = (Button) findViewById(R.id.connect);

        buttonConnect.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                ClientRxThread clientRxThread =
                        new ClientRxThread(
                                editTextAddress.getText().toString(),
                                SocketServerPORT);

                clientRxThread.start();
            }});
        String[] requiredPermissions = { Manifest.permission.READ_EXTERNAL_STORAGE };
        ActivityCompat.requestPermissions(this, requiredPermissions, 0);
    }

    private class ClientRxThread extends Thread {
        private static final long  MEGABYTE = 1024L * 1024L;
        private static final long  KILOBYTE = 1024L ;
        public long bytesToMeg(long bytes) {
            return bytes / MEGABYTE ;
        }

        public long bytesToKiloBytes(long bytes) {
            return bytes / KILOBYTE ;
        }
        String dstAddress;
        int dstPort;

        ClientRxThread(String address, int port) {
            dstAddress = address;
            dstPort = port;
        }

        @Override
        public void run() {
            Socket socket = null;

            try {
                socket = new Socket(dstAddress, dstPort);
                if (socket.isConnected()) {
                    Log.i("test", "Socket connected to server");
                }

                //File file = new File(Environment.getExternalStorageDirectory(),"test.txt");
//Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS ).getPath()
               // final File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS ).getPath(),"test.txt");
                final File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS ).getPath(),"test.mp4");
                if (file.exists()) {
                    Log.i("test","File Exists " + file.getAbsolutePath());
                    Log.i("test","File length " + file.length());
                }else {
                    file.createNewFile();
                    Log.e("test","File does not Exists check again " + file.getAbsolutePath());
                }

                byte[] bytes = new byte[1024];
               // byte[] bytes = new byte[(int)file.length()];
                InputStream is = socket.getInputStream();
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
//                int bytesRead = is.read(bytes, 0, bytes.length);
////                if (bytesRead > -1) {
////                    bos.write(bytes, 0, bytesRead);
////                }
                int readLength = -1;

                while ((readLength = is.read(bytes)) > 0) {
                    Log.i("test","Reading data from socket...............");
                    bos.write(bytes, 0, readLength);

                }

                bos.close();
                socket.close();

               // final File file2 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS ).getPath(),"test.txt");
                final File file2 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS ).getPath(),"test.mp4");
               final String _size =  bytesToKiloBytes(file2.length()) > 1024 ? String.valueOf(bytesToMeg(file2.length())) + " MB" : String.valueOf(bytesToKiloBytes(file2.length())) + "  KB";
               Log.i("test", "File received successfully. File Size: " + _size);
                ClientActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(ClientActivity.this,
                                "File received successfully. File Size: " + _size ,
                                Toast.LENGTH_LONG).show();
                    }});

            } catch ( IOException e) {

                e.printStackTrace();

                final String eMsg = "Something wrong: " + e.getMessage();
                ClientActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(ClientActivity.this,
                                eMsg,
                                Toast.LENGTH_LONG).show();
                    }});

            } finally {
                if(socket != null){
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
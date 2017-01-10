package com.antoha.carcontrol;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    Button forward;
    Button backward;
    Button right;
    Button left;
    EditText etIp;
    EditText etPort;
    EditText logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        forward = (Button) findViewById(R.id.forward);
        backward = (Button) findViewById(R.id.backward);
        right = (Button) findViewById(R.id.right);
        left = (Button) findViewById(R.id.left);
        etIp = (EditText) findViewById(R.id.etIp);
        etPort = (EditText) findViewById(R.id.etPort);
        logger = (EditText) findViewById(R.id.logger);

        setKeyListeners();
        log("Activity created");
    }

    private void setKeyListeners() {
        forward.setOnClickListener(new ClickListener("FORWARD"));
        backward.setOnClickListener(new ClickListener("BACKWARD"));
        left.setOnClickListener(new ClickListener("LEFT"));
        right.setOnClickListener(new ClickListener("RIGHT"));
    }


    private boolean isServerInputed(){
        return etIp.getText().length() > 0 && etPort.getText().length() > 0;

    }

    private void log(String newText){
        String text = logger.getText().toString();
        text += "\n"+newText;
        logger.setText(text);
    }

    private void sendCommand(final String msg) {
        final Handler mainHandler = new Handler(this.getMainLooper());
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String logStr;
                try {
                    String ip = etIp.getText().toString();
                    int port = Integer.parseInt(etPort.getText().toString());
                    Socket socket = new Socket(ip, port);
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    dos.writeUTF(msg);
                    dos.flush();
                    dos.close();
                    socket.close();
                    logStr = "Command " + msg + "sended";
                } catch (IOException e) {
                    e.printStackTrace();
                    logStr = "IOException " + e.getMessage();
                } catch (Exception e){
                    e.printStackTrace();
                    logStr = "Exception " +e.toString()+ e.getMessage();
                }
                final String logToUi = logStr;
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        log(logToUi);
                    }
                };
                mainHandler.post(myRunnable);
            }
        });
        thread.start();
    }

    class ClickListener implements View.OnClickListener {
        String msg;
        ClickListener(String msg){
            this.msg = msg;
        }

        @Override
        public void onClick(View v) {
            if(isServerInputed()){
                log("Button " +msg +" clicked");
                sendCommand(msg);
            } else{
                log("Ip or port is empty");
                Toast.makeText(MainActivity.this, "IP/Port error", Toast.LENGTH_SHORT).show();
            }
        }
    }

}

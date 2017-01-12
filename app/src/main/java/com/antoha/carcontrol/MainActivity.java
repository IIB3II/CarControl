package com.antoha.carcontrol;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private Button forward;
    private Button backward;
    private Button right;
    private Button left;
    private EditText etIp;
    private EditText etPort;
    private EditText logger;

    private boolean forwardPressed = false;
    private boolean backwardPressed = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private Runnable runnable = null;

    private final int SEND_COMMAND_TIMEOUT = 100; //ms

    public interface Message {
        String FORWARD = "FORWARD";
        String BACKWARD = "BACKWARD";
        String LEFT = "LEFT";
        String RIGHT = "RIGHT";
    }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(1, 1, 1, "").setTitle("Clean log");
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                logger.setText("");
                return true;
        }
        return false;
    }

    private void setKeyListeners() {
        forward.setOnTouchListener(new ToutchListener(Message.FORWARD));
        backward.setOnTouchListener(new ToutchListener(Message.BACKWARD));
        left.setOnTouchListener(new ToutchListener(Message.LEFT));
        right.setOnTouchListener(new ToutchListener(Message.RIGHT));;
    }


    private boolean isServerInputed(){
        return etIp.getText().length() > 0 && etPort.getText().length() > 0;

    }

    private void log(String newText){
        String text = logger.getText().toString();
        text += "\n"+newText;
        logger.setText(text);
    }

    /**
     * Send string to server via socket
     * @param msg
     */
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

    /**
     * Run task on new thread until button is pressed
     * @param msg - button command
     */
    private void startSendingCommands(final String msg) {
        final Handler mainHandler = new Handler(this.getMainLooper());
        switch(msg){
            case Message.BACKWARD:
                backwardPressed = true;
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        while(backwardPressed){
                            sendWithTimeout(mainHandler, msg);
                        }
                    }
                };
                break;
            case Message.FORWARD:
                forwardPressed = true;
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        while(forwardPressed){
                            sendWithTimeout(mainHandler, msg);
                        }
                    }
                };
                break;
            case Message.LEFT:
                leftPressed = true;
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        while(leftPressed){
                            sendWithTimeout(mainHandler, msg);
                        }
                    }
                };
                break;
            case Message.RIGHT:
                rightPressed = true;
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        while(rightPressed){
                            sendWithTimeout(mainHandler, msg);
                        }
                    }
                };
                break;
        }
        if(runnable != null) {
            AsyncTask.execute(runnable);
        }

    }

    /**
     * Wait constatnt SEND_COMMAND_TIMEOUT time and try to send command
     * @param mainHandler
     * @param msg
     */
    private void sendWithTimeout(Handler mainHandler, final String msg){
        try {
            Thread.sleep(SEND_COMMAND_TIMEOUT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Run log on main thread
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                log("Loop sending " +msg);
            }
        };
        mainHandler.post(myRunnable);
        sendCommand(msg);
    }

    /**
     * Set pressed flug false, for button with command msg
     * @param msg
     */
    private void stopSendingCommands(String msg){
        switch(msg){
            case Message.BACKWARD:
                backwardPressed = false;
                break;
            case Message.FORWARD:
                forwardPressed = false;
                break;
            case Message.LEFT:
                leftPressed = false;
                break;
            case Message.RIGHT:
                rightPressed = false;
                break;
        }
    }

    /**
     * Button listener
     * On button is down start sending and on release stop
     */
    class ToutchListener implements View.OnTouchListener {
        String msg;
        ToutchListener(String msg){
            this.msg = msg;
        }
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if(isServerInputed()){
                    log("Button " +msg +" ACTION_DOWN");
                    startSendingCommands(msg);
                } else{
                    log("Ip or port is empty");
                    Toast.makeText(MainActivity.this, "IP/Port error", Toast.LENGTH_SHORT).show();
                }

            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                stopSendingCommands(msg);

            }
            return false;
        }

    }

}

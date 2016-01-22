package fr.valentinporchet.romeo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {
    private TouchDisplayView mTouchView;
    private SwipeView mSwipeView;
    private ServerSocket serverSocket;
    private ClientThread clientThread;
    private Thread socketThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove the status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // remove the title of the application
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // set the main content of the application
        setContentView(R.layout.activity_main);

        // initialisation of touch view and relation to seekbar
        mTouchView = (TouchDisplayView) findViewById(R.id.touch_display_view);
        initializeButtons();

        mSwipeView = (SwipeView) findViewById(R.id.swipe_view);
        initializeSwipeView();

        // Code for server
        //socketThread = new Thread(new ServerThread(serverSocket, mTouchView));

        // Code for client
        clientThread = new ClientThread();
        socketThread = new Thread(clientThread);

        // we start the socket
        socketThread.start();
    }

    private void startSettingsActivity(View v) {
        // go to the new options activity
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i); // we start the settings activity
    }

    private void initializeButtons() {
        // Initialisation of animate button
        Button animateButton = (Button) findViewById(R.id.animate_button);
        animateButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTouchView.launchAnimation();
            }
        });

        // Initialisation of options button
        Button optionsButton = (Button) findViewById(R.id.options_button);
        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSettingsActivity(v);
            }
        });

        // Initialisation of color buttons with hashmap <button_id, color>
        Map<Integer, Integer> colors = new HashMap<>();
        colors.put(R.id.black_button, 0xFF000000);
        colors.put(R.id.blue_button, 0xFF0000FF);
        colors.put(R.id.cyan_button, 0xFF00FFFF);
        colors.put(R.id.green_button, 0xFF00FF00);
        colors.put(R.id.orange_button, 0xFFFF8000);
        colors.put(R.id.pink_button, 0xFFFF0080);
        colors.put(R.id.purple_button, 0xFF8000FF);
        colors.put(R.id.red_button, 0xFFFF0000);
        colors.put(R.id.yellow_button, 0xFFFFFF00);

        // we iterate the hashmap above, in order to add the click listeners on buttons
        for (Map.Entry<Integer, Integer> entry : colors.entrySet()) {
            final int value = entry.getValue();
            findViewById(entry.getKey()).setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTouchView.setSelectedColor(value);
                }
            });
        }
        
        // Initialisation of the eraser button
        ImageButton eraserButton = (ImageButton) findViewById(R.id.eraser_button);
        eraserButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTouchView.activeEraseMode();
            }
        });
    }

    private void initializeSwipeView() {
        // we add the FlingGestureListener, in order to add the sending event on swiping
        mSwipeView.setOnTouchListener(new FlingGestureListener() {
            @Override
            public void onRightToLeft() {
                Log.i("SwipeViewActivity", "swipe rightToLeft");
            }

            @Override
            public void onLeftToRight() {
                Log.i("SwipeViewActivity", "swipe leftToRight");
                clientThread.send(mTouchView.getTouchData()); // send the data
            }

            @Override
            public void onBottomToTop() {
                Log.i("SwipeViewActivity", "bottomToTop");
            }

            @Override
            public void onTopToBottom() {
                Log.i("SwipeViewActivity", "topToBottom");
            }
        });
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        //mTouchView.pause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        //mTouchView.resume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        /*try {
            // make sure you close the socket upon exiting
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}
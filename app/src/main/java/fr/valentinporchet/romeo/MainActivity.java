package fr.valentinporchet.romeo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {
    private TouchDisplayView mTouchView;
    private SwipeView mSwipeView;
    private ServerSocket mServerSocket;
    private ClientThread mClientThread;
    private Thread mServerSocketThread;
    private Thread mClientSocketThread;
    private SharedPreferences sharedPrefs;

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
        mServerSocketThread = new Thread(new ServerThread(mServerSocket, mTouchView));
        mServerSocketThread.start();

        // initialisation of settings
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        initializePrefListener();

        // depending on the value, display the right gender icon
        updateGenderIcon();
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
        colors.put(R.id.black_button, 0xFF242424);
        colors.put(R.id.red_button, 0xFFC31D40);
        colors.put(R.id.green_button, 0xFF2EC196);
        colors.put(R.id.blue_button, 0xFF272F80);

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
        // On simple click, just erase the last path
        eraserButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("MainActivity", "Erasing last path");
                mTouchView.eraseLastPath();
            }
        });
        // On long click, erase all
        eraserButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.i("MainActivity", "Erasing all paths");
                return mTouchView.eraseAllPaths();
            }
        });
    }

    private void initializeSwipeView() {
        if (mSwipeView != null) {
            // we add the FlingGestureListener, in order to add the sending event on swiping
            mSwipeView.setOnTouchListener(new FlingGestureListener() {
                @Override
                public void onRightToLeft() {
                    Log.i("SwipeViewActivity", "swipe rightToLeft");
                }

                @Override
                public void onLeftToRight() {
                    Log.i("SwipeViewActivity", "swipe leftToRight");
                    // we create the new client thread with the data and the server IP in preferences
                    mClientThread = new ClientThread(mTouchView.getTouchData(), sharedPrefs.getString("preference_penpal_IP", "192.168.1.1"));
                    // then we start it
                    mClientSocketThread = new Thread(mClientThread);
                    mClientSocketThread.start(); // we start the thread
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
    }

    private void initializePrefListener() {
        // we add a listener here, in order to change the male/female icon
        sharedPrefs.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals("preference_gender")) {
                    updateGenderIcon();
                }
            }
        });
    }

    /**
     * Method called in order to update the gender icon depending on the configuration
     */
    public void updateGenderIcon() {
        ImageView genderIcon = (ImageView) findViewById(R.id.gender_icon);
        // if we are a male, so the other person is a female, and vice versa
        int drawableID = R.drawable.top_right_male;
        if (sharedPrefs.getString("preference_gender", "female").equals("male")) {
            drawableID = R.drawable.top_right_female;
        }
        genderIcon.setBackgroundResource(drawableID);
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
            mServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}
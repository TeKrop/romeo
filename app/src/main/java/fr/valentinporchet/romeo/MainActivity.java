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
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {
    private TouchDisplayView mTouchView;
    private TouchThroughView mTouchThroughView;
    private SwipeView mSwipeView;

    private ServerSocket mServerSocket;
    private ServerThread mServerThread;
    private Thread mServerSocketThread;

    private ClientThread mClientThread;
    private Thread mClientSocketThread;

    private TTServerThread mTTServerThread;
    private Thread mTTServerSocketThread;
    private ServerSocket mTTServerSocket;

    private SharedPreferences sharedPrefs;
    private SharedPreferences.OnSharedPreferenceChangeListener sharedPrefsListener;
    private Mode mCurrentMode;

    private LinearLayout mColorsButtons;

    public enum Mode {
        MESSAGE,
        TOUCH_THROUGH
    };

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

        mTouchThroughView = (TouchThroughView) findViewById(R.id.touch_through_view);

        // initialisation of settings
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Code for message server
        mServerThread = new ServerThread(mServerSocket, mTouchView, sharedPrefs.getString("preference_status", "available"));
        mServerSocketThread = new Thread(mServerThread);
        mServerSocketThread.start();

        // add listener to settings, and update the gender icon
        sharedPrefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals("preference_gender")) {
                    Log.i("MainActivity", "Gender changed. Changing the pic...");
                    updateGenderIcon(sharedPreferences);
                } else if (key.equals("preference_status")) {
                    Log.i("MainActivity", "Status changed. Updating server thread...");
                    mServerThread.setStatus(sharedPreferences.getString(key, "available"));
                } else if (key.equals("preference_penpal_IP")) {
                    Log.i("MainActivity", "IP changed. Updating server thread...");
                    mTouchThroughView.setServerIP(sharedPreferences.getString(key, "192.168.1.1"));
                }
            }
        };
        initializePrefListener();
        updateGenderIcon(sharedPrefs);

        // we add a reference to the letter button to the touch display view
        mTouchView.setLetterButton((ImageButton) findViewById(R.id.letter_button));
        mColorsButtons = (LinearLayout) findViewById(R.id.colors_buttons);

        // we set the current mode to MESSAGE
        mCurrentMode = Mode.MESSAGE;

        // code for touch through server
        mTTServerThread = new TTServerThread(mTTServerSocket, mTouchThroughView);
        mTTServerSocketThread = new Thread(mTTServerThread);
        mTTServerSocketThread.start();

        // and we add the server IP to the client thread of touch through
        mTouchThroughView.setServerIP(sharedPrefs.getString("preference_penpal_IP", "192.168.1.1"));
    }

    private void startSettingsActivity() {
        // go to the new options activity
        Intent i = new Intent(this, SettingsActivity.class);
        initializePrefListener();
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
                startSettingsActivity();
            }
        });

        // Initialisation of letter button
        final ImageButton letterButton = (ImageButton) findViewById(R.id.letter_button);
        final ImageView letterOpened = (ImageView) findViewById(R.id.letter_opened);
        letterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // we hide the letter button
                letterButton.setVisibility(View.INVISIBLE);
                // then we show the letter opened image, and start to animate
                // the fadeout
                letterOpened.setVisibility(View.VISIBLE);

                Animation fadeOut = new AlphaAnimation(1, 0);
                fadeOut.setInterpolator(new AccelerateInterpolator());
                fadeOut.setDuration(1000);

                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    public void onAnimationEnd(Animation animation) {
                        letterOpened.setVisibility(View.GONE);
                        mTouchView.launchTempStoredAnimation();
                    }

                    public void onAnimationRepeat(Animation animation) {
                    }

                    public void onAnimationStart(Animation animation) {
                    }
                });

                letterOpened.startAnimation(fadeOut);
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

        final ImageButton changeModeButton = (ImageButton) findViewById(R.id.change_mode_button);
        changeModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("MainActivity", "Switching current mode");
                if (mCurrentMode == Mode.MESSAGE) {
                    // we change the current mode
                    mCurrentMode = Mode.TOUCH_THROUGH;
                    // and the background resource on the button
                    changeModeButton.setBackgroundResource(R.drawable.btn_message);
                    // and the mode itself
                    mTouchView.setVisibility(View.INVISIBLE);
                    mTouchThroughView.setVisibility(View.VISIBLE);
                    // and we hide the colors buttons
                    findViewById(R.id.colors_buttons).setVisibility(View.INVISIBLE);
                } else {
                    // we change the current mode
                    mCurrentMode = Mode.MESSAGE;
                    // and the background resource on thebutton
                    changeModeButton.setBackgroundResource(R.drawable.btn_touch_through);
                    // and the mode itself
                    mTouchThroughView.setVisibility(View.INVISIBLE);
                    mTouchView.setVisibility(View.VISIBLE);
                    // and we hide the colors buttons
                    findViewById(R.id.colors_buttons).setVisibility(View.VISIBLE);
                }
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
                    mClientThread = new ClientThread(new ArrayList<>(mTouchView.getTouchData()),
                            sharedPrefs.getString("preference_penpal_IP", "192.168.1.1"));
                    // then we start it
                    mClientSocketThread = new Thread(mClientThread);
                    mClientSocketThread.start(); // we start the thread
                    // now, we can clear the data on our screen
                    mTouchView.clearCurrentTouchData();
                    // then, we launch stored received data if there is any
                    mTouchView.launchTempStoredAnimation();
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
        // if already here for previous instance, we unregister, and then register for the new
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(sharedPrefsListener);
        sharedPrefs.registerOnSharedPreferenceChangeListener(sharedPrefsListener);
    }

    /**
     * Method called in order to update the gender icon depending on the configuration
     */
    public void updateGenderIcon(SharedPreferences pref) {
        ImageView genderIcon = (ImageView) findViewById(R.id.gender_icon);
        // if we are a male, so the other person is a female, and vice versa
        int drawableID = R.drawable.top_right_male;
        if (pref.getString("preference_gender", "female").equals("male")) {
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
}
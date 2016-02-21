package fr.valentinporchet.romeo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {
    private MainActivity me;

    private boolean mUserActive = true;
    private CountDownTimer mInactiveTimer;
    private int USER_TIMEOUT = 15000;
    private String mMessageStatus;

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
    private Animation mRotateCorner;
    private ImageView mCircleProgressBar;

    public void setStatus(String status) {
        mMessageStatus = status;
        mRotateCorner.cancel();
    }

    public enum Mode {
        MESSAGE,
        TOUCH_THROUGH
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        me = this; // pointer to the instance

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
        mServerThread = new ServerThread(mServerSocket, mTouchView, mUserActive);
        mServerSocketThread = new Thread(mServerThread);
        mServerSocketThread.start();

        // add listener to settings, and update the gender icon
        sharedPrefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals("preference_gender")) {
                    Log.i("MainActivity", "Gender changed. Changing the pic...");
                    updateGenderIcon(sharedPreferences);
                } else if (key.equals("preference_penpal_IP")) {
                    Log.i("MainActivity", "IP changed. Updating server thread...");
                    mTouchThroughView.setServerIP(sharedPreferences.getString(key, "192.168.1.1"));
                } else if (key.equals("preference_other_visible")) {
                    Log.i("MainActivity", "Other icon visibility changed...");
                    updateGenderIconVisibility(sharedPreferences.getBoolean(key, true));
                }
            }
        };
        initializePrefListener();
        updateGenderIcon(sharedPrefs);
        updateGenderIconVisibility(sharedPrefs.getBoolean("preference_other_visible", true));

        // initialize the animation
        mRotateCorner = AnimationUtils.loadAnimation(this, R.anim.rotate_corner);
        mCircleProgressBar = (ImageView) findViewById(R.id.circle_progressbar);
        initializeRotateListener();

        // we add a reference to the letter button to the touch display view. Same for drawing progressbar.
        mTouchView.setLetterButton((ImageButton) findViewById(R.id.letter_button));
        mTouchView.setDrawingProgressBar((ProgressBar) findViewById(R.id.drawing_progressbar));
        mTouchView.setLittleEnvelope((ImageButton) findViewById(R.id.little_envelope_button));

        // we also link the popup view to the progressBar and to the touch view
        PopupView popupView = (PopupView) findViewById(R.id.popup_view);
        mTouchView.setPopupView(popupView);
        popupView.setDrawingProgressBar((ProgressBar) findViewById(R.id.drawing_progressbar));

        // we set the current mode to MESSAGE
        mCurrentMode = Mode.MESSAGE;

        // code for touch through server
        mTTServerThread = new TTServerThread(mTTServerSocket, mTouchThroughView);
        mTTServerSocketThread = new Thread(mTTServerThread);
        mTTServerSocketThread.start();

        // and we add the server IP to the client thread of touch through
        mTouchThroughView.setServerIP(sharedPrefs.getString("preference_penpal_IP", "192.168.1.1"));
        mColorsButtons = (LinearLayout) findViewById(R.id.colors_buttons);

        // initializing countdown timer
        mInactiveTimer = new CountDownTimer(USER_TIMEOUT, 1000) {
            public void onTick(long millisUntilFinished) {
                //Some code
            }
            public void onFinish() {
                // when the timer is finished, the user is inactive, we set the option to inactive
                mUserActive = false;
                mServerThread.setStatus(mUserActive);
                Log.v("MainActivity", "You're now inactive");
            }
        };
        // start the timer
        mInactiveTimer.start();
    }

    private void startSettingsActivity() {
        // go to the new options activity
        Intent i = new Intent(this, SettingsActivity.class);
        initializePrefListener();
        startActivity(i); // we start the settings activity
    }

    // PART CONCERNING THE PROGRESS CIRCLE FOR SENDING
    private void startCircleLoadingAnimation() {
        // we set the circle visible and start animation
        mCircleProgressBar.setVisibility(View.VISIBLE);
        mCircleProgressBar.startAnimation(mRotateCorner);
        // we lower the opacity of the drawing
        mTouchView.setAlpha(0.3f);
    }

    private void initializeRotateListener() {
        mRotateCorner.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.v("MainActivity", "Message status : " + mMessageStatus);
                Log.v("MainActivity", "Animation done.");
                mCircleProgressBar.clearAnimation();
                // if this is an error, we show the error circle
                if (mMessageStatus.equals("Error")) {
                    mCircleProgressBar.setImageResource(R.drawable.circular_progressbar_error);
                }
                // one second after that, reset the animation
                CountDownTimer resetTimer = new CountDownTimer(1000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        mCircleProgressBar.setVisibility(View.INVISIBLE);
                        mCircleProgressBar.setImageResource(R.drawable.circular_progressbar);
                        mTouchView.setAlpha(1);
                    }
                };
                resetTimer.start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    // INITIALISATION OF ALL BUTTONS OF THE SCREEN
    private void initializeButtons() {
        // Initialisation of settings button
        ImageButton optionsButton = (ImageButton) findViewById(R.id.settings_button);
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
                    changeModeButton.setImageResource(R.drawable.btn_message);
                    // and the mode itself
                    mTouchView.setVisibility(View.INVISIBLE);
                    mTouchThroughView.setVisibility(View.VISIBLE);
                    // and we hide the colors buttons
                    LinearLayout.LayoutParams loparams = (LinearLayout.LayoutParams) mColorsButtons.getLayoutParams();
                    loparams.weight = 0;
                    mColorsButtons.setLayoutParams(loparams);
                } else {
                    // we change the current mode
                    mCurrentMode = Mode.MESSAGE;
                    // and the background resource on thebutton
                    changeModeButton.setImageResource(R.drawable.btn_touch_through);
                    // and the mode itself
                    mTouchThroughView.setVisibility(View.INVISIBLE);
                    mTouchView.setVisibility(View.VISIBLE);
                    // and we show the colors buttons
                    LinearLayout.LayoutParams loparams = (LinearLayout.LayoutParams) mColorsButtons.getLayoutParams();
                    loparams.weight = 1;
                    mColorsButtons.setLayoutParams(loparams);
                }
            }
        });

        final ImageButton littleEnvelope = (ImageButton) findViewById(R.id.little_envelope_button);
        littleEnvelope.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // we change the background of the envelope
                littleEnvelope.setImageResource(R.drawable.btn_letter_opened);

                Animation fadeOut = new AlphaAnimation(1, 0);
                fadeOut.setInterpolator(new AccelerateInterpolator());
                fadeOut.setDuration(1000);

                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    public void onAnimationEnd(Animation animation) {
                        littleEnvelope.setVisibility(View.GONE);
                        littleEnvelope.setImageResource(R.drawable.btn_letter_closed);
                        mTouchView.launchTempStoredAnimation();
                    }

                    public void onAnimationRepeat(Animation animation) {
                    }

                    public void onAnimationStart(Animation animation) {
                    }
                });

                littleEnvelope.startAnimation(fadeOut);
            }
        });
    }

    // PART CONCERNING THE SENDING DATA PROCESS
    private void sendDrawingData() {
        // if there is data to send
        if (!mTouchView.getTouchData().isEmpty()) {
            setStatus("Sending");
            // we create the new client thread with the data and the server IP in preferences
            mClientThread = new ClientThread(new ArrayList<>(mTouchView.getTouchData()),
                    sharedPrefs.getString("preference_penpal_IP", "192.168.1.1"), me);
            // then we start it
            mClientSocketThread = new Thread(mClientThread);
            mClientSocketThread.start(); // we start the thread
            startCircleLoadingAnimation(); // we start the circle animation
        } else {
            Toast.makeText(getApplication(), "Error : no drawing to send", Toast.LENGTH_LONG).show();
        }
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
                    sendDrawingData();
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

        // we also initialize the button
        ImageButton genderIcon = (ImageButton) findViewById(R.id.gender_icon);
        genderIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDrawingData();
            }
        });
    }

    // PART CONCERNING PREFERENCES LISTENER
    private void initializePrefListener() {
        // if already here for previous instance, we unregister, and then register for the new
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(sharedPrefsListener);
        sharedPrefs.registerOnSharedPreferenceChangeListener(sharedPrefsListener);
    }

    // PART CONCERNING GENDER ICON
    public void updateGenderIcon(SharedPreferences pref) {
        ImageButton genderIcon = (ImageButton) findViewById(R.id.gender_icon);
        // if we are a male, so the other person is a female, and vice versa
        int drawableID = R.drawable.top_right_male;
        if (pref.getString("preference_gender", "female").equals("male")) {
            drawableID = R.drawable.top_right_female;
        }
        genderIcon.setImageResource(drawableID);
    }

    public void updateGenderIconVisibility(boolean visible) {
        ImageButton genderIcon = (ImageButton) findViewById(R.id.gender_icon);
        genderIcon.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    // PART CONCERNING THE ACTIVE/INACTIVE PROCESS
    @Override
    public void onUserInteraction(){
        mUserActive = true; // as soon as we touch the screen, the user is active...
        mServerThread.setStatus(mUserActive);
        // ... and we restart the timer
        mInactiveTimer.cancel();
        mInactiveTimer.start();
        Log.v("MainActivity", "You're now active");
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        mUserActive = false; // as soon as we touch the screen, the user is active...
        mServerThread.setStatus(mUserActive);
        mInactiveTimer.cancel();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mUserActive = true; // the user is active...
        mServerThread.setStatus(mUserActive);
        // ... and we restart the timer
        mInactiveTimer.cancel();
        mInactiveTimer.start();
    }
}
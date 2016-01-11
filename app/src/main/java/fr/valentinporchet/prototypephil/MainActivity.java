package fr.valentinporchet.prototypephil;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;

public class MainActivity extends Activity {
    // TouchDisplayView
    private TouchDisplayView mTouchView;
    // seekbar for path thickness
    private SeekBar mPathThicknessController = null;
    // seekbar for animation speed
    private SeekBar mAnimationSpeedController = null;

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
        initializeSeekBars();
    }

    /**
     * Method used to initialize the SeekBar for thickness
     */
    private void initializeSeekBars() {
        // Initialization of thickness seekBar
        mPathThicknessController = (SeekBar) findViewById(R.id.thickness_seek_bar);
        mPathThicknessController.setProgress((int) TouchDisplayView.PATH_THICKNESS);

        mPathThicknessController.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // we call the method in the view in order to update the thickness
                mTouchView.updatePathThickness(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // ...
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // ...
            }
        });

        // initialize the seekbar for animation speed
        mAnimationSpeedController = (SeekBar) findViewById(R.id.animation_seek_bar);
        mAnimationSpeedController.setMax(3000);
        mAnimationSpeedController.setProgress(TouchDisplayView.ANIMATION_SPEED);

        mAnimationSpeedController.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTouchView.updateAnimationSpeed(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // ...
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // ...
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
}
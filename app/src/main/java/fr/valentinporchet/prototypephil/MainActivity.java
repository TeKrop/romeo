package fr.valentinporchet.prototypephil;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.SeekBar;

public class MainActivity extends Activity {
    // TouchDisplayView
    private TouchDisplayView mView;
    // seekbar for path thickness
    private SeekBar mPathThicknessController = null;
    // seekbar for animation speed
    private SeekBar mAnimationSpeedController = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // remove the title
        setContentView(R.layout.activity_main);

        // initialisation of touch view and relation to seekbar
        mView = (TouchDisplayView) findViewById(R.id.touch_display_view);
        initializeSeekBars();
    }

    /**
     * Method used to initialize the SeekBar for thickness
     */
    private void initializeSeekBars() {
        // Initialization of thickness seekBar
        mPathThicknessController = (SeekBar) findViewById(R.id.thickness_seek_bar);
        mPathThicknessController.setProgress((int) mView.PATH_THICKNESS);

        mPathThicknessController.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // we call the method in the view in order to update the thickness
                mView.updatePathThickness(progress);
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
        mAnimationSpeedController.setProgress((int) mView.ANIMATION_SPEED);

        mAnimationSpeedController.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mView.updateAnimationSpeed(progress);
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
        //mView.pause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        //mView.resume();
    }
}
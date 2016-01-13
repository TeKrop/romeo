package fr.valentinporchet.prototypephil;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {
    // TouchDisplayView
    private TouchDisplayView mTouchView;

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
    }

    private void initializeButtons() {
        Button animateButton = (Button) findViewById(R.id.animate_button);
        animateButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTouchView.launchAnimation();
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

        // Initialisation of size buttons with hashmap <button_id, size>
        Map<Integer, Integer> sizes = new HashMap<>();
        sizes.put(R.id.small_size_button, 10);
        sizes.put(R.id.normal_size_button, 20);
        sizes.put(R.id.big_size_button, 30);

        for (Map.Entry<Integer, Integer> entry : sizes.entrySet()) {
            final int value = entry.getValue();
            findViewById(entry.getKey()).setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTouchView.setSelectedThickness(value);
                }
            });
        }
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
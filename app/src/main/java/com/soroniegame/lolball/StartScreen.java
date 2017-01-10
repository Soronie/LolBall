package com.soroniegame.lolball;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class StartScreen extends AppCompatActivity {
    private TextView gameMode, description;
    private Intent game;
    private boolean singOrMult;
    private int singleHighScore, multiHighScore;
    private MediaPlayer tapSound;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Assures that the most recent (paused) activity is launched on resume
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }

        preferences = this.getPreferences(Context.MODE_PRIVATE);
        boolean gameSaved = getIntent().getBooleanExtra("gameSaved", false);

        // Restore high scores even when app closes or save data
        if(!gameSaved){
            // New game
            singleHighScore = preferences.getInt("singleHighScore", 0);
            multiHighScore = preferences.getInt("multiHighScore", 0);
        }else {
            // Saved game
            singleHighScore = getIntent().getIntExtra("singleHighScore", 0);
            multiHighScore = getIntent().getIntExtra("multiHighScore", 0);
        }

        singOrMult = getIntent().getBooleanExtra("singOrMult",
                preferences.getBoolean("singOrMult", true));

        // Activities initialization
        game = new Intent(getApplicationContext(), BallActivity.class);

        // Hide the action and status bars on start-up
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_start_screen);

        // Initialize views and set the title to a custom font
        gameMode = (TextView) findViewById(R.id.gameMode);
        description = (TextView) findViewById(R.id.gameDescription);
        TextView gameTitle = (TextView)findViewById(R.id.gameTitle);
        Typeface custom_font = Typeface.createFromAsset(getAssets(),
                "fonts/Impregnable_Personal_Use_Only.ttf");
        gameTitle.setTypeface(custom_font);

        // Create the tap sound for button presses
        tapSound = MediaPlayer.create(getApplicationContext(), R.raw.balltap);

        // The previous mode played is toggled
        if(singOrMult) {
            gameMode.setText(getString(R.string.single_tap));
            description.setText(R.string.single_tap_desc);
        }
        else {
            gameMode.setText(getString(R.string.multi_tap));
            description.setText(R.string.multi_tap_desc);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Handles button presses: HOME, BACK
        if ((keyCode == KeyEvent.KEYCODE_HOME) ||
                keyCode == KeyEvent.KEYCODE_BACK) {
            //onPause() is automatically called here
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onPause(){
        // HOME or MENU was pressed
        super.onPause();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("singleHighScore", singleHighScore);
        editor.putInt("multiHighScore", multiHighScore);
        editor.putBoolean("singOrMult", singOrMult);
        editor.apply();
    }

    public void rotateView(View view){
        // Rotate an image when being tapped
        RotateAnimation rotate = new RotateAnimation(0.0f, 360, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(1000);
        rotate.setFillAfter(true);
        view.startAnimation(rotate);
    }

    public void singleTap(View view){
        tapSound.start();
        gameMode.setText(getString(R.string.single_tap));
        description.setText(R.string.single_tap_desc);
        singOrMult = true;
    }

    public void multiTap(View view){
        tapSound.start();
        gameMode.setText(getString(R.string.multi_tap));
        description.setText(R.string.multi_tap_desc);
        singOrMult = false;
    }

    public void startGame(View view){
        // Trigger the tap sound and transfer the previously-saved data
        tapSound.start();
        game.putExtra("singleHighScore", singleHighScore);
        game.putExtra("multiHighScore", multiHighScore);
        game.putExtra("singOrMult", singOrMult);
        finish();
        startActivity(game);
    }
}

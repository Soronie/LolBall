package com.soroniegame.lolball;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

public class BallActivity extends AppCompatActivity {
    private Random r;
    private boolean cuedOnce, gameRunning, singOrMult;
    private int x, y, counter, screenWidth, screenHeight, duration, currentScore, singleHighScore, multiHighScore;
    private ImageView ballView, cueView;
    private TextView introView, refreshView, resumeView, counterView, currentScoreView, highScoreView;
    private ViewPropertyAnimator animation;
    private MediaPlayer tapSound, rotateSound;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the action and status bars on start-up
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_ball_activity);
        initializeValues();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        // Update maximum height and width measurements whenever the screen rotates
        super.onConfigurationChanged(newConfig);
        cueView.setVisibility(View.GONE);
        counterView.setVisibility(View.GONE);
        rotateSound.start();
        calculateDimensions();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Handles button presses: HOME, BACK
        if ((keyCode == KeyEvent.KEYCODE_HOME)) {
            //onPause() is automatically called here
            moveTaskToBack(true);
            return true;
        }
        else if((keyCode == KeyEvent.KEYCODE_BACK))
        {
            if(gameRunning) {
                gameRunning = false;
                animation.cancel();
                resumeView.setVisibility(View.VISIBLE);
            }
            else
                // Pause only when the game has actually started
                returnToStartScreen();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onPause(){
        // HOME or MENU button was pressed, or app is closed. Prevents instance from being destroyed
        super.onPause();
        if (gameRunning) {
            gameRunning = false;
            animation.cancel();
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("singleHighScore", singleHighScore);
        editor.putInt("multiHighScore", multiHighScore);
        editor.putBoolean("singOrMult", singOrMult);
        editor.apply();
    }

    @Override
    public void onResume(){
        // Display the 'resume' message when the game is paused
        super.onResume();
        if(introView.getVisibility() == View.GONE && !gameRunning)
            // Called when game was paused previously via HOME and is now about to resume
            resumeView.setVisibility(View.VISIBLE);
    }

    public void returnToStartScreen(){
        // Transfer save data, end this activity, and go to the start screen
        Intent startScreen = new Intent(getApplicationContext(), StartScreen.class);
        startScreen.putExtra("singleHighScore", singleHighScore);
        startScreen.putExtra("multiHighScore", multiHighScore);
        startScreen.putExtra("singOrMult", singOrMult);
        startScreen.putExtra("gameSaved", true);
        finish();
        startActivity(startScreen);
    }

    public void calculateDimensions(){
        // Obtain maximum height and width measurements of the game screen
        DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenHeight = displaymetrics.heightPixels;
        screenWidth = displaymetrics.widthPixels;
    }

    public void initializeValues(){
        gameRunning = false;
        r = new Random();
        calculateDimensions();

        // Restore high scores even when app is closed
        preferences = this.getPreferences(Context.MODE_PRIVATE);
        singleHighScore = preferences.getInt("singleHighScore",
                getIntent().getIntExtra("singleHighScore", 0));
        multiHighScore = preferences.getInt("multiHighScore",
                getIntent().getIntExtra("multiHighScore", 0));
        singOrMult = getIntent().getBooleanExtra("singOrMult", true);

        // Initialize views and keep them hidden before the game starts
        introView = (TextView) findViewById(R.id.intro);
        refreshView = (TextView) findViewById(R.id.reset);
        resumeView = (TextView) findViewById(R.id.resume);
        counterView = (TextView) findViewById(R.id.counter);
        cueView = (ImageView) findViewById(R.id.googly_eyes);
        currentScoreView = (TextView) findViewById(R.id.score);
        highScoreView = (TextView) findViewById(R.id.high_score);
        ballView = (ImageView) findViewById(R.id.ball);

        cueView.setVisibility(View.GONE);
        resumeView.setVisibility(View.GONE);
        counterView.setVisibility(View.GONE);
        currentScoreView.setVisibility(View.GONE);

        // Display the appropriate high score based on the chosen game mode
        if(singOrMult)
            highScoreView.setText(getString(R.string.high_score, singleHighScore));
        else
            highScoreView.setText(getString(R.string.high_score, multiHighScore));

        // Animation initialization
        animation = ballView.animate();
        animation.setListener(new MyAnimatorListener());

        //Sounds initialization
        tapSound = MediaPlayer.create(getApplicationContext(), R.raw.balltap);
        rotateSound = MediaPlayer.create(getApplicationContext(), R.raw.woosh);
    }

    public void toRandomEdge(){
        // Calculate an edge based on randomized (+) and (-) coordinates
        boolean edgeBool = r.nextBoolean();
        boolean xBool = r.nextBoolean();
        boolean yBool = r.nextBoolean();

        if(edgeBool){
            // Go to the left or right edge at a random height
            x = screenWidth/2;
            y = r.nextInt(screenHeight/2);
        }
        else{
            // Go to the top or bottom edge at a random width
            x = r.nextInt(screenWidth/2);
            y = screenHeight/2;
        }

        // Subtract coordinates by image radius
        x = x - ballView.getWidth()/2;
        y = y - ballView.getHeight()/2;

        // Wherever false, make negative
        if(!xBool)
            x = -x;
        if(!yBool)
            y = -y;
    }

    public void randomCue(){
        // Make the cue appear randomly
        // cuedOnce ensures the cue doesn't appear consecutively
        boolean cueBool = r.nextBoolean();
        if(cueBool && !cuedOnce) {
            cueView.setVisibility(View.VISIBLE);
            cuedOnce = true;
            if(singOrMult) {
                // Single tap mode
                counter = 1;
            }else{
                // Multi tap mode
                counter = r.nextInt(3) + 1; //random number from 1 to 3
                counterView.setText(getString(R.string.counter, counter));
                counterView.setVisibility(View.VISIBLE);
            }
        }
        else
            cuedOnce = false;
    }

    public void updateScores(){
        // Update the appropriate scores per ball movement
        if(singOrMult){
            if(currentScore > singleHighScore)
                singleHighScore = currentScore;
        }else
            if(currentScore > multiHighScore)
                multiHighScore = currentScore;
        currentScoreView.setText(String.valueOf(currentScore));
        if(singOrMult)
            highScoreView.setText(getString(R.string.high_score, singleHighScore));
        else
            highScoreView.setText(getString(R.string.high_score, multiHighScore));
    }

    public void ballTapped(View view){
        // Trigger sound effect, increment score or reset, and move
        tapSound.start();

        if(gameRunning) {
            if (cueView.getVisibility() == View.VISIBLE) {
                // If the ball is tapped during a cue, then increment score,
                // increase animation speed, and remove the cue
                    counter--;
                    counterView.setText(getString(R.string.counter, counter));
                    // Don't move the ball or update the score until the number of taps is reached
                    if(counter > 0) return;
                if(counter == 0) {
                    currentScore++;
                    if (duration >= 0)
                        duration = duration - 10;
                    animation.setDuration(duration);
                    cueView.setVisibility(View.GONE);
                    counterView.setVisibility(View.GONE);
                }
            } else// If the ball is tapped NOT during a cue, reset the score
                resetRound();
        } else{
            gameRunning = true;
            if(resumeView.getVisibility() == View.GONE) // The game begins
                resetRound();
            introView.setVisibility(View.GONE);
            refreshView.setVisibility(View.GONE);
            resumeView.setVisibility(View.GONE);
        }
        move();
    }

    public void move(){
        // Launch the ball to a random point in 2D space
        updateScores();
        toRandomEdge();
        animation.translationX(x);
        animation.translationY(y);
    }

    public void screenTapped(View view){
        // Reset the round whenever the screen is tapped
        if(gameRunning) {
            resetRound();
            cueView.setVisibility(View.GONE);
            counterView.setVisibility(View.GONE);
        }
        else if(resumeView.getVisibility() == View.VISIBLE){
            // The game was paused and now resumes
            resumeView.setVisibility(View.GONE);
            gameRunning = true;
            move();
        }
    }

    public void resetRound(){
        // Restore the original animation speed and reset the current score
        if(singOrMult)
            duration = 1000;
        else
            duration = 1150;
        currentScore = 0;
        animation.setDuration(duration);
        currentScoreView.setText(String.valueOf(currentScore));
        currentScoreView.setVisibility(View.VISIBLE);
        highScoreView.setVisibility(View.VISIBLE);
    }

    private class MyAnimatorListener implements Animator.AnimatorListener{
        @Override
        public void onAnimationStart(Animator animator) {
            // Before the ball moves, possibly trigger a cue
            randomCue();
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            if(cueView.getVisibility() == View.VISIBLE) {
                // If the ball is untapped after the cue, remove the cue and reset
                cueView.setVisibility(View.GONE);
                counterView.setVisibility(View.GONE);
                if(gameRunning)
                    // Never reset the score when the game doesn't run in the foreground
                    resetRound();
            }
            move();
        }

        @Override
        public void onAnimationCancel(Animator animator) {
        }

        @Override
        public void onAnimationRepeat(Animator animator) {
        }
    }
}
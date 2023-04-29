package edu.acg.taxidodger;

import static android.content.Context.VIBRATOR_SERVICE;


import android.annotation.SuppressLint;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.os.Vibrator;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Random;

/*
    The core functionality of this class was based on the following youtube tutorial:
    https://www.youtube.com/watch?v=gfX8UHTpq3o&ab_channel=SandipBhattacharya

    Primary creative credit goes to the creator of the above video.
 */

// Define a class named GameView that extends the View class
public class GameView extends View {

    Cat cat = new Cat(getContext(), SplashActivity.catColor);
    Bitmap catFrame;
    Rect rectBackground;
    Context context;
    Handler handler;
    final long UPDATE_MILLIS = 30;
    Runnable runnable;
    Paint textPaint = new Paint();
    Paint healthPaint = new Paint();
    float TEXT_SIZE = 64;
    int points = 0;
    int life = 3;
    static int dWidth, dHeight;
    Random random;
    float catX, catY;
    float oldX;
    float oldCatX;
    ArrayList<Taxi> taxis;
    ArrayList<Explosion> explosions;

    public static MediaPlayer currentlyPlayingSong;

    @SuppressLint("SourceLockedOrientationActivity")
    public GameView(Context context) {
        super(context);
        this.context = context;
        int currentOrientation = getResources().getConfiguration().orientation;
        Activity activity = (Activity) getContext();

        // Lock in screen orientation based on device orientation for the duration of the game
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
        } else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }

        // Stop any music that is currently playing
        if (GameView.currentlyPlayingSong != null) {
            GameView.stopMusic();
        }

        // Start playing background music
        playMusic();

        // Set the starting cat frame based on selected color
        if (SplashActivity.catColor == 0)
            catFrame = BitmapFactory.decodeResource(context.getResources(), R.drawable.cat_stand64);
        else if (SplashActivity.catColor == 1)
            catFrame = BitmapFactory.decodeResource(context.getResources(), R.drawable.white_0);

        // Get display size and create background rectangle
        Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        dWidth = size.x;
        dHeight = size.y;
        rectBackground = new Rect(0, 0, dWidth, dHeight);
        handler = new Handler();
        runnable = this::invalidate;
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(ResourcesCompat.getFont(context, R.font.press_start));
        healthPaint.setColor(ContextCompat.getColor(context, R.color.cat_eye_green));
        random = new Random();
        catX = (int) (dWidth / 2.0 - catFrame.getWidth() / 2.0);
        catY = dHeight - catFrame.getHeight() * 2;
        taxis = new ArrayList<>();
        explosions = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Taxi taxi = new Taxi(context);
            taxis.add(taxi);
        }
    }


    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Increment the cat's animation frame and loop back to 0 after reaching 7
        cat.catFrame++;
        if (cat.catFrame > 7) {
            cat.catFrame = 0;
        }

        // Draw the cat bitmap at its current position
        catFrame = cat.getCat(cat.catFrame);
        canvas.drawBitmap(catFrame, catX, catY, null);

        // Loop through all taxis and draw them on the canvas
        // Also update their positions and increase their speed if necessary
        for (int i = 0; i < taxis.size(); i++) {
            canvas.drawBitmap(taxis.get(i).taxi, taxis.get(i).taxiX, taxis.get(i).taxiY, null);
            taxis.get(i).taxiY += taxis.get(i).taxiVelocity;
            if (taxis.get(i).taxiY >= dHeight) {
                points += 10;
                if (points % 100 == 0) {
                    // Increase the speed of all taxis if the player has reached a multiple of 100 points
                    for (int x = 0; x < taxis.size(); x++) {
                        taxis.get(x).increaseSpeed();
                    }
                }
                taxis.get(i).resetPosition();
            }
        }

        // Check for collision between each taxi and the cat
        for (int i = 0; i < taxis.size(); i++) {
            if (taxis.get(i).taxiX + taxis.get(i).getTaxiWidth() >= catX
                    && taxis.get(i).taxiX <= catX + catFrame.getWidth()
                    && taxis.get(i).taxiY + taxis.get(i).getTaxiWidth() >= catY
                    && taxis.get(i).taxiY <= catY + catFrame.getHeight()) {
                // Decrease the player's life count and play a damage effect
                life--;
                damageEffect();
                // Create an explosion at the taxi's position and reset the taxi's position
                Explosion explosion = new Explosion(context);
                explosion.explosionX = taxis.get(i).taxiX - 50;
                explosion.explosionY = taxis.get(i).taxiY;
                explosions.add(explosion);
                taxis.get(i).resetPosition();
                // If the player's life count has reached 0, end the game and go to the GameOverActivity
                if (life == 0) {
                    stopMusic();
                    Activity activity = (Activity) getContext();
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    @SuppressLint("DrawAllocation") Intent intent = new Intent(context, GameOverActivity.class);
                    intent.putExtra("points", points);
                    intent.putExtra("color", SplashActivity.catColor);
                    intent.putExtra("name", SplashActivity.userName);
                    context.startActivity(intent);
                    ((Activity) context).finish();
                }
            }
        }

        // Draw any explosions currently on the canvas and remove them when their animation is finished
        for (int i = 0; i < explosions.size(); i++) {
            canvas.drawBitmap(explosions.get(i).getExplosion(explosions.get(i).explosionFrame),
                    explosions.get(i).explosionX, explosions.get(i).explosionY, null);
            explosions.get(i).explosionFrame++;
            if (explosions.get(i).explosionFrame > 2) {
                explosions.remove(i);
            }
        }

        // Update the health bar color based on the player's remaining life count
        if (life == 2) {
            healthPaint.setColor(ContextCompat.getColor(context, R.color.cat_orange));
        } else if (life == 1) {
            healthPaint.setColor(Color.RED);
        }
        canvas.drawRect(dWidth - 300, 30, dWidth - 300 + 90 * life, 80, healthPaint);
        canvas.drawText("" + points, 25, TEXT_SIZE + 25, textPaint);
        handler.postDelayed(runnable, UPDATE_MILLIS);
    }

    // Plays music if the sound is on
    private void playMusic() {
        if (SplashActivity.soundOn) {
            // Create a new MediaPlayer object with game_theme audio file
            currentlyPlayingSong = MediaPlayer.create(getContext(), R.raw.game_theme);
            // Set the looping flag to true
            currentlyPlayingSong.setLooping(true);
            // Start playing the audio file
            currentlyPlayingSong.start();
        }
    }

    // Stops the currently playing music
    public static void stopMusic() {
        // Release currentlyPlayingSong to stop audio file
        if (currentlyPlayingSong != null) {
            // Stop the audio playback
            currentlyPlayingSong.stop();
            // Release the MediaPlayer resources
            currentlyPlayingSong.release();
            // Set the MediaPlayer object to null
            currentlyPlayingSong = null;
        }
    }

    // Handles the touch event of the screen
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            // Store the old touch X-coordinate and cat X-coordinate
            oldX = event.getX();
            oldCatX = catX;
        }
        if (action == MotionEvent.ACTION_MOVE) {
            // Calculate the shift in the touch X-coordinate
            float shift = oldX - touchX;
            // Calculate the new cat X-coordinate based on the shift
            float newCatX = oldCatX - shift;
            // Check if the new cat X-coordinate is within the bounds of the screen
            if (newCatX <= 0) {
                catX = 0;
            } else if (newCatX >= dWidth - catFrame.getWidth()) {
                catX = dWidth - catFrame.getWidth();
            } else {
                catX = newCatX;
            }
        }
        // Return true to indicate that the touch event is consumed
        return true;
    }

    // Applies a damage effect to the device if it is running Android Oreo or higher
    public void damageEffect() {
        if (Build.VERSION.SDK_INT >= 26) {
            // Create a new VibrationEffect with a duration of 150 milliseconds
            ((Vibrator) getContext().getSystemService(VIBRATOR_SERVICE)).vibrate(android.os.VibrationEffect.createOneShot(150, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            // Vibrate for 100 milliseconds if the device is running below Android Oreo
            ((Vibrator) getContext().getSystemService(VIBRATOR_SERVICE)).vibrate(100);
        }
    }
}
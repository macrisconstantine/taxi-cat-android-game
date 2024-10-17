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
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.os.Vibrator;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
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
    final long UPDATE_MILLIS = 20;
    Runnable runnable;
    Paint textPaint = new Paint();
    Paint healthPaint = new Paint();
    float TEXT_SIZE = 64;
    int points = 0;
    int life = 3;
    static int dWidth, dHeight;
    Random random;
    float catX, catY;
    float oldX, oldY;
    float oldCatX, oldCatY;
    ArrayList<Taxi> taxis;
    ArrayList<Explosion> explosions;
    long lastUpdateTime;
    boolean changeFrame = true;
    String formattedDate;
    int taxiColor;
    int catWidth, catHeight;
    private SoundPool soundPool;
    private final int explosionSoundId;

    public static MediaPlayer currentlyPlayingSong;

    @SuppressLint("SourceLockedOrientationActivity")
    public GameView(Context context) {
        super(context);
        this.context = context;
        int currentOrientation = getResources().getConfiguration().orientation;
        taxiColor = 0;
        Activity activity = (Activity) getContext();
        Date currentDate = new Date();
        // Initialize SoundPool
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(3) // Adjust based on how many sounds you need to play at the same time
                .setAudioAttributes(audioAttributes)
                .build();

        // Load the explosion sound into the SoundPool
        explosionSoundId = soundPool.load(context, R.raw.explosion_sound, 1);

        // Define the desired date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm", Locale.UK);

        // Format the date
        formattedDate = dateFormat.format(currentDate);

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

        catFrame = BitmapFactory.decodeResource(context.getResources(), R.drawable.cat_run4);

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
        catWidth = catFrame.getWidth();
        catHeight = catFrame.getHeight();
        catX = (int) (dWidth / 2.0 - catWidth / 2.0);
        catY = dHeight - catWidth*3;
        taxis = new ArrayList<>();
        explosions = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Taxi taxi = new Taxi(context);
            taxis.add(taxi);
        }
    }


    // Method to play the explosion sound
    private void playExplosionSound() {
        if (soundPool != null) {
            soundPool.play(explosionSoundId, 1.0f, 1.0f, 1, 0, 1.0f); // Adjust volume, priority, loop, and rate as needed
        }
    }

    @SuppressLint("DrawAllocation")
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000.0f; // Convert delta time to seconds
        lastUpdateTime = currentTime;

        // Increment the cat's animation frame and loop back to 0 after reaching 7
        if (changeFrame) {
            cat.catFrame++;
            changeFrame = false;
        } else {
            changeFrame = true;
        }
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
                if (points % 50 == 0) {
                    // Increase the speed of all taxis if the player has reached a multiple of 100 points
                    for (int x = 0; x < taxis.size(); x++) {
                        taxis.get(x).increaseSpeed();
                    }
                }
                if (points%1000==0) {
                    life=3;
                    taxiColor++;
                    taxis.add(new Taxi(context));
                    for (int x = 0; x < taxis.size(); x++) {
                        taxis.get(x).resetSpeed();
                        taxis.get(x).changeColor(context, taxiColor);
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
                playExplosionSound();
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        intent.putExtra("date", String.valueOf(LocalDateTime.now()));
                    } else {
                        intent.putExtra("date", String.valueOf(formattedDate));
                    }
                    context.startActivity(intent);
                    ((Activity) context).finish();
                }
            }
        }

        // Draw any explosions currently on the canvas and remove them when their animation is finished
        for (int i = 0; i < explosions.size(); i++) {
            canvas.drawBitmap(explosions.get(i).getExplosion(explosions.get(i).explosionFrame),
                    explosions.get(i).explosionX, explosions.get(i).explosionY, null);
            if (changeFrame) {
                explosions.get(i).explosionFrame++;
            }

            if (explosions.get(i).explosionFrame > 2) {
                explosions.remove(i);
            }
        }

        // Update the health bar color based on the player's remaining life count
        if (life == 3) {
            healthPaint.setColor(ContextCompat.getColor(context, R.color.cat_eye_green));
        } else if (life == 2) {
            healthPaint.setColor(ContextCompat.getColor(context, R.color.cat_orange));
        }else if (life == 1) {
            healthPaint.setColor(Color.RED);
        }
        canvas.drawRect(dWidth - 300, 30, dWidth - 300 + 90 * life, 80, healthPaint);
        canvas.drawText("" + points, 25, TEXT_SIZE + 25, textPaint);
        // Schedule the next frame with adjusted delay time
        long adjustedDelay = (long) (UPDATE_MILLIS - deltaTime * 1000.0f); // Convert delay time to milliseconds
        handler.postDelayed(runnable, adjustedDelay);
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

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    // Handles the touch event of the screen
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            // Store the old touch X-coordinate and cat X-coordinate
            oldY = event.getY();
            oldX = event.getX();
            oldCatY = catY;
            oldCatX = catX;
        }
        if (action == MotionEvent.ACTION_MOVE) {
            // Calculate the shift in the touch X-coordinate
            float shiftX = oldX - touchX;
            float shiftY = oldY - touchY;
            // Calculate the new cat X-coordinate based on the shift
            float newCatX = oldCatX - shiftX;
            float newCatY = oldCatY - shiftY;
            // Check if the new cat X-coordinate is within the bounds of the screen
            if (newCatX <= 0) {
                catX = 0;
            } else if (newCatX >= dWidth - catWidth) {
                catX = dWidth - catWidth;
            } else {
                catX = newCatX;
            }
            if (newCatY <= 0) {
                catY = 0;
            } else if (newCatY >= dHeight - catWidth*3){
                catY = dHeight - catWidth*3;
            } else {
                catY = newCatY;
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
package edu.acg.taxidodger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;


// This activity class describes the functionality of the GameOver activity

public class GameOverActivity extends AppCompatActivity {

    // The bulk of this class is contained within the onCreate method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        // Relevant widgets and classes declared/initialized
        HighScores scores;
        Button playAgain = findViewById(R.id.play_again_button);
        Button toMenu = findViewById(R.id.menu_button);
        Button shareButton = findViewById(R.id.share_button);

        // Retrieve the score from the Intent
        int score = getIntent().getIntExtra("points",0);
        String userName = getIntent().getStringExtra("name");
        String date = getIntent().getStringExtra("date");
        // Find the TextView for displaying the score
        TextView scoreTextView = findViewById(R.id.score_text1);
        // Set the text of the TextView to the score
        scoreTextView.setText(String.valueOf(score));

        // Utilizes shared preferences to retrieve a stored array of names/high scores
        SharedPreferences prefs = this.getSharedPreferences("high_scores", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("scores", "");
        if (!json.isEmpty()) {
            Type type = new TypeToken<HighScores>() {}.getType();
            scores = gson.fromJson(json, type);
        } else {
            scores = new HighScores();
        }

        // Adds the new score of the most recent session (if given a username)
        if (userName != null) {
            scores.addScore(userName, score, date);
        }

        // Removes any score listings that are below the top 20 highest entries
        if (scores.getScores().size()>20)
            scores.trimScores();

        // Locally stores the new high scores list with the addition of the most recent score
        prefs = this.getSharedPreferences("high_scores", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        gson = new Gson();
        json = gson.toJson(scores);
        editor.putString("scores", json);
        editor.apply();

        // Uses intents to access communication mechanisms of other applications and share a string
        shareButton.setOnClickListener(v -> {

            // Shares some text with the high score
            String textToShare = "I just got a score of " + score + " in Taxi Cat!\n\n" +
                    "Think you can beat that?\n\n\n\nbtw Constantine Macris made Taxi Cat...his GitHub is " +
                    "https://github.com/macrisconstantine";
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, textToShare);

            // Opens the standard pop-up grid to select a means of sharing
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });

        // This button restarts the game by creating a new game view and setting it as content view
        playAgain.setOnClickListener(v -> {
            GameView gameView = new GameView(this);
            setContentView(gameView);
            if (SplashActivity.currentlyPlayingSong!=null){
                SplashActivity.stopMusic();
                SplashActivity.currentlyPlayingSong = MediaPlayer.create(this, R.raw.game_theme);
                SplashActivity.currentlyPlayingSong.setLooping(true);
                SplashActivity.currentlyPlayingSong.start();
            }
        });

        // This button navigates back to the main menu/splash activity and calls onDestroy(this)
        toMenu.setOnClickListener(v -> {
            Intent intent = new Intent(GameOverActivity.this, SplashActivity.class);
            startActivity(intent);
            intent.putExtra("name", userName);
            if (SplashActivity.currentlyPlayingSong!=null){
                SplashActivity.stopMusic();
            }
            finish();
        });
    }
}
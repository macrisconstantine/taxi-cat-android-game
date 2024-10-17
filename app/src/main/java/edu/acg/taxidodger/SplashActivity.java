package edu.acg.taxidodger;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    // Variables to store the current state of the sound, cat color, and player name
    public static MediaPlayer currentlyPlayingSong;
    public static boolean soundOn = true;
    public static int catColor = 0;
    public static String userName = "Player";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Find all necessary views in the layout
        CheckBox soundCheck = findViewById(R.id.sound_checkbox);
        EditText nameEdit = findViewById(R.id.name_text_edit);
        Spinner colorChoice = findViewById(R.id.choose_color_spinner);
        Button playButton = findViewById(R.id.play_button);
        Button highScoreButton = findViewById(R.id.high_scores_button);
        ImageView logoCat = findViewById(R.id.logo_image);

        // Get SharedPreferences to save and retrieve values between sessions
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        // Restore saved values from SharedPreferences if available
        if (sharedPreferences != null){
            soundOn = sharedPreferences.getBoolean("checkBoxState", true);
            catColor = sharedPreferences.getInt("color", 0);
            userName = sharedPreferences.getString("username", "");
        }

        // Set the saved values in the appropriate views
        if (!userName.equals("")) {
            nameEdit.setText(userName);
        }
        soundCheck.setChecked(soundOn);
        if (catColor == 0){
            colorChoice.setSelection(0);
            logoCat.setImageResource(R.drawable.cat_logo);
        }
        else if (catColor == 1) {
            colorChoice.setSelection(1);
            logoCat.setImageResource(R.drawable.white_logo_cat);
        }
        else if (catColor == 2) {
            colorChoice.setSelection(2);
            logoCat.setImageResource(R.drawable.pink_logo_cat);
        }
        else if (catColor == 3) {
            colorChoice.setSelection(3);
            logoCat.setImageResource(R.drawable.neon_logo_cat);
        }
        else if (catColor == 4) {
            colorChoice.setSelection(4);
            logoCat.setImageResource(R.drawable.night_logo_cat);
        }
        else if (catColor == 5) {
            colorChoice.setSelection(5);
            logoCat.setImageResource(R.drawable.gold_logo_cat);
        }
        else if (catColor == 6) {
            colorChoice.setSelection(6);
            logoCat.setImageResource(R.drawable.sphynx_logo_cat);
        }
        // Listen for changes to the sound checkbox
        soundCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // enable/disable audio when the checkbox state changes
            if (isChecked) {
                playMusic();
                soundOn = true;
            } else {
                stopMusic();
                soundOn = false;
            }

            // Save the new sound state in SharedPreferences
            if (sharedPreferences != null) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("checkBoxState", soundOn);
                editor.apply();
            }
        });

        // Listen for changes to the player name text edit
        nameEdit.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Save the new player name in SharedPreferences
                userName = nameEdit.getText().toString();
                if (sharedPreferences != null) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", userName);
                    editor.apply();
                }
            }
            return false;
        });

        // Listen for changes to the cat color spinner
        colorChoice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Update the logo image based on the selected color
                int selectedColor = parent.getSelectedItemPosition();
                if (selectedColor == 0){
                    logoCat.setImageResource(R.drawable.cat_logo);
                } else if (selectedColor == 1){
                    logoCat.setImageResource(R.drawable.white_logo_cat);
                } else if (selectedColor == 2){
                    logoCat.setImageResource(R.drawable.pink_logo_cat);
                } else if (selectedColor == 3){
                    logoCat.setImageResource(R.drawable.neon_logo_cat);
                } else if (selectedColor == 4){
                    logoCat.setImageResource(R.drawable.night_logo_cat);
                } else if (selectedColor == 5){
                    logoCat.setImageResource(R.drawable.gold_logo_cat);
                } else if (selectedColor == 6){
                    logoCat.setImageResource(R.drawable.sphynx_logo_cat);
                }
                // Save the new cat color in SharedPreferences
                if (sharedPreferences != null) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("color", selectedColor);
                    editor.apply();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Set the default logo image if nothing is selected
                logoCat.setImageResource(R.drawable.cat_logo);
            }
        });

        if (currentlyPlayingSong!=null){
            stopMusic();
        } else if (soundCheck.isChecked()){
            playMusic();
        }

        playButton.setOnClickListener(v -> {
            stopMusic();
            // Gets the selected color choice from a spinner
            catColor = colorChoice.getSelectedItemPosition();
            // Gets the user's name from an EditText field
            userName = nameEdit.getText().toString();

            // Save the new player name in SharedPreferences
            userName = nameEdit.getText().toString();
            if (sharedPreferences != null) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username", userName);
                editor.apply();
            }

            // If the user did not enter a name, set a default name
            if (userName.equals(""))
                userName = "AnonymousCat69";

            // Create a new GameView and set it as the content view
            GameView gameView = new GameView(this);
            setContentView(gameView);
        });

        highScoreButton.setOnClickListener(v -> {
            // Start the HighScoreActivity when the High Score button is clicked
            Intent intent = new Intent(SplashActivity.this, HighScoreActivity.class);
            startActivity(intent);
        });
    }

    // Various methods to control audio playback on navigation
    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        stopMusic();
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopMusic();
        if (soundOn)
            playMusic();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMusic();
    }

    @Override
    protected void onPause() {
        super.onPause();
        GameView.stopMusic();
        stopMusic();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopMusic();
    }

    @Override
    public void onBackPressed() {
        super.onStop();
        stopMusic();
    }

    // Creates a new MediaPlayer object and starts playing the menu theme
    private void playMusic() {
        currentlyPlayingSong = MediaPlayer.create(this, R.raw.menu_theme);
        currentlyPlayingSong.setLooping(true);
        currentlyPlayingSong.start();
    }

    // Stops any currently playing music and releases the MediaPlayer object
    public static void stopMusic(){
        if (currentlyPlayingSong != null) {
            currentlyPlayingSong.stop();
            currentlyPlayingSong.reset();
            currentlyPlayingSong.release();
            currentlyPlayingSong = null;
        }
    }
}
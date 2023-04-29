package edu.acg.taxidodger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

public class HighScoreActivity extends AppCompatActivity {

    HighScores scores;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_score);

        // Retrieve the saved high scores from SharedPreferences.
        SharedPreferences prefs = this.getSharedPreferences("high_scores", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("scores", "");

        // If saved high scores exist, deserialize them into HighScores object.
        if (!json.isEmpty()) {
            Type type = new TypeToken<HighScores>() {}.getType();
            scores = gson.fromJson(json, type);
        } else {
            // If saved high scores do not exist, create a new HighScores object.
            scores = new HighScores();
        }

        // Retrieve the list of high scores from the HighScores object.
        List<HighScores.Score> scoreList = scores.getScores();

        // Set up the display of high scores using a custom adapter.
        HighScoresAdapter highScoreDisplay = new HighScoresAdapter(this, scoreList);
        ListView scoresList = findViewById(R.id.high_scores_list);
        scoresList.setAdapter(highScoreDisplay);

        // Handle click events on the list items.
        scoresList.setOnItemClickListener((parent, view, position, id) -> {
            HighScores.Score highScore = scoreList.get(position);
            Intent intent = new Intent(HighScoreActivity.this, HighScoreDetailActivity.class);
            intent.putExtra("name", highScore.getName());
            intent.putExtra("score", highScore.getScore());
            startActivity(intent);
        });

        // Set up the "back" button to finish the activity and return to the previous activity.
        Button esc = findViewById(R.id.back_list_button);
        esc.setOnClickListener(v -> finish());

    }


}
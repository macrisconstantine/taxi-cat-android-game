package edu.acg.taxidodger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class HighScoreDetailActivity extends AppCompatActivity {

    /**
     * This method initializes the activity, sets the layout, and retrieves the high score data from the intent.
     * The name and score are then displayed in their respective TextViews.
     * The back button is also initialized and handles the click event by finishing the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_score_detail);

        Button back = findViewById(R.id.back_detail_button);
        TextView nameTextView = findViewById(R.id.name_score_text_view);
        TextView scoreTextView = findViewById(R.id.score_detail_view);

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        int score = intent.getIntExtra("score", 0);

        nameTextView.setText(name);
        scoreTextView.setText(String.valueOf(score));

        // Handle click events on the back button
        back.setOnClickListener(v -> finish());
    }
}
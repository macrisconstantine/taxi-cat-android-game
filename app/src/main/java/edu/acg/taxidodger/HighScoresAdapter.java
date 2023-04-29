package edu.acg.taxidodger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class HighScoresAdapter extends BaseAdapter {
    private final Context context;
    private final List<HighScores.Score> scores;

    // Constructor for the adapter
    public HighScoresAdapter(Context context, List<HighScores.Score> scores) {
        this.context = context;
        this.scores = scores;
    }

    // Returns the number of items in the list
    @Override
    public int getCount() {
        return scores.size();
    }

    // Returns the item at a given position
    @Override
    public Object getItem(int position) {
        return scores.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // If no view is provided, inflate the view from the layout
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.high_score_list_item, parent, false);
        }

        // Calculate the rank of the score
        int rank = position + 1;
        TextView rankTextView = convertView.findViewById(R.id.high_score_rank);
        TextView scoreTextView = convertView.findViewById(R.id.high_score_score);

        // Get the score at the given position
        HighScores.Score score = scores.get(position);
        // Set the rank and score text views
        rankTextView.setText(String.valueOf(rank));
        scoreTextView.setText(String.valueOf(score.getScore()));

        return convertView;
    }
}
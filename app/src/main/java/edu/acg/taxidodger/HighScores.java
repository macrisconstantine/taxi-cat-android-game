package edu.acg.taxidodger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HighScores {
    //List to store high scores
    private final List<Score> scores;

    // Constructor to initialize scores list
    public HighScores() {
        scores = new ArrayList<>();
    }

    // Method to add a new score to the list
    public void addScore(String name, int score) {
        Score newScore = new Score(name, score);
        scores.add(newScore);
        Collections.sort(scores);
    }

    // Method to get the list of high scores
    public List<Score> getScores() {
        return scores;
    }

    // Method to trim the list to 20 scores
    public void trimScores() {
        while (scores.size() > 20){
            scores.remove(scores.size()-1);
        }
    }

    // Nested class to represent a score entry
    public static class Score implements Comparable<Score> {
        private final String name;
        private final int score;

        // Constructor to initialize a score entry
        public Score(String name, int score) {
            this.name = name;
            this.score = score;
        }

        public String getName() {
            return name;
        }

        public int getScore() {
            return score;
        }

        // Override compareTo method to compare scores in descending order
        @Override
        public int compareTo(Score other) {
            return Integer.compare(other.getScore(), this.getScore());
        }
    }
}
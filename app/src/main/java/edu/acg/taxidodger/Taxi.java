package edu.acg.taxidodger;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.Random;

public class Taxi{
    Bitmap taxi;
    int taxiX, taxiY, taxiVelocity;
    int speedIncrease = 0;
    Random random;

    // Constructor for the Taxi class that loads the taxi image and initializes random number generator
    public Taxi(Context context){
        taxi = BitmapFactory.decodeResource(context.getResources(), R.drawable.taxi128);
        random = new Random();
        resetPosition();
    }

    // Sets the position and velocity of the taxi to a random value
    public void resetPosition() {
        taxiX = random.nextInt(GameView.dWidth - getTaxiWidth());
        taxiY = -200 + random.nextInt(600) * -1;
        taxiVelocity = 35 + random.nextInt(16) + speedIncrease;
    }

    // Increases the speed of the taxi by a fixed amount
    public void increaseSpeed() {
        speedIncrease += 5;
    }

    // Returns the width of the taxi image
    public int getTaxiWidth(){
        return taxi.getWidth();
    }
}

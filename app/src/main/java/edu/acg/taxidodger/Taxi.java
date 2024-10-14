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
        taxiVelocity = 15 + random.nextInt(16) + speedIncrease;
    }

    // Increases the speed of the taxi by a fixed amount
    public void increaseSpeed() {
        speedIncrease += 1;
    }
    public void resetSpeed(){
        speedIncrease = 0;
    }

    public void changeColor(Context context, int color) {
        if (color==0){
            taxi = BitmapFactory.decodeResource(context.getResources(), R.drawable.taxi128);
        } else if (color==1){
            taxi = BitmapFactory.decodeResource(context.getResources(), R.drawable.orange_taxi);
        } else if (color==2){
            taxi = BitmapFactory.decodeResource(context.getResources(), R.drawable.red_taxi);
        } else if (color==3){
            taxi = BitmapFactory.decodeResource(context.getResources(), R.drawable.purple_taxi);
        } else if (color==4){
            taxi = BitmapFactory.decodeResource(context.getResources(), R.drawable.blue_taxi);
        } else if (color==5){
            taxi = BitmapFactory.decodeResource(context.getResources(), R.drawable.green_taxi);
        } else if (color==6){
            taxi = BitmapFactory.decodeResource(context.getResources(), R.drawable.ghost_taxi);
        } else if (color==7){
            taxi = BitmapFactory.decodeResource(context.getResources(), R.drawable.teal_taxi);
        } else if (color==8){
            taxi = BitmapFactory.decodeResource(context.getResources(), R.drawable.blorange_taxi);
        } else if (color==9){
            taxi = BitmapFactory.decodeResource(context.getResources(), R.drawable.black_taxi);
        } else if (color==10) {
            taxi = BitmapFactory.decodeResource(context.getResources(), R.drawable.pink_taxi);
        } else if (color==11){
            taxi = BitmapFactory.decodeResource(context.getResources(), R.drawable.gold_taxi);
        } else {
            taxi = BitmapFactory.decodeResource(context.getResources(), R.drawable.gurple_taxi);
        }
    }

    // Returns the width of the taxi image
    public int getTaxiWidth(){
        return taxi.getWidth();
    }
}

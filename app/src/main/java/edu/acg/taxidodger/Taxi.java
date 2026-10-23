package edu.acg.taxidodger;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Taxi {

    /** Drives straight down. */
    public static final int NORMAL = 0;
    /** Drifts side to side as it falls. Shows up from district 3. */
    public static final int SWERVER = 1;
    /** Narrower window to react to. Shows up from district 2. */
    public static final int SPEEDER = 2;

    private static final int[] COLOR_RES = {
            R.drawable.taxi128,
            R.drawable.orange_taxi,
            R.drawable.red_taxi,
            R.drawable.purple_taxi,
            R.drawable.blue_taxi,
            R.drawable.green_taxi,
            R.drawable.ghost_taxi,
            R.drawable.teal_taxi,
            R.drawable.blorange_taxi,
            R.drawable.black_taxi,
            R.drawable.pink_taxi,
            R.drawable.gold_taxi,
            R.drawable.gurple_taxi,
    };

    // Re-decoding the same PNG for every taxi on every level change was pure churn.
    private static final Map<Integer, Bitmap> BITMAP_CACHE = new HashMap<>();

    Bitmap taxi;
    float taxiX, taxiY;
    /** Pixels per second, so the game plays the same on any refresh rate. */
    float speed;
    int type = NORMAL;
    /** Set once the taxi has drawn level with the cat, so a near miss scores once. */
    boolean passedCat;

    private float swerveAmp, swervePhase, swerveFreq, homeX;
    private final Random random = new Random();

    public Taxi(Context context) {
        taxi = cached(context, R.drawable.taxi128);
        resetPosition();
    }

    static Bitmap cached(Context context, int resId) {
        Bitmap bitmap = BITMAP_CACHE.get(resId);
        if (bitmap == null || bitmap.isRecycled()) {
            bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
            BITMAP_CACHE.put(resId, bitmap);
        }
        return bitmap;
    }

    /** Drops the taxi back above the top of the screen with fresh random values. */
    public void resetPosition() {
        int w = Math.max(1, GameView.dWidth);
        int h = Math.max(1, GameView.dHeight);

        taxiX = random.nextFloat() * Math.max(1, w - getTaxiWidth());
        taxiY = -getTaxiHeight() - random.nextFloat() * h * 0.9f;
        speed = h * (0.42f + random.nextFloat() * 0.30f);

        homeX = taxiX;
        swervePhase = random.nextFloat() * 6.2831855f;
        swerveFreq = 1.2f + random.nextFloat() * 1.0f;
        swerveAmp = w * (0.07f + random.nextFloat() * 0.10f);
        type = NORMAL;
        passedCat = false;
    }

    /** Picks a behaviour appropriate to how far the player has got. */
    public void assignType(int district) {
        float roll = random.nextFloat();
        if (district >= 2 && roll < 0.22f) {
            type = SWERVER;
        } else if (district >= 1 && roll < 0.40f) {
            type = SPEEDER;
            speed *= 1.40f;
        } else {
            type = NORMAL;
        }
        homeX = taxiX;
    }

    public void update(float dt, float speedMultiplier) {
        taxiY += speed * speedMultiplier * dt;
        if (type == SWERVER) {
            swervePhase += swerveFreq * dt;
            float x = homeX + swerveAmp * (float) Math.sin(swervePhase);
            float max = GameView.dWidth - getTaxiWidth();
            taxiX = Math.max(0f, Math.min(max, x));
        }
    }

    public void changeColor(Context context, int color) {
        int index = ((color % COLOR_RES.length) + COLOR_RES.length) % COLOR_RES.length;
        taxi = cached(context, COLOR_RES[index]);
    }

    public int getTaxiWidth() {
        return taxi.getWidth();
    }

    public int getTaxiHeight() {
        return taxi.getHeight();
    }

    public float centerX() {
        return taxiX + getTaxiWidth() / 2f;
    }

    public float centerY() {
        return taxiY + getTaxiHeight() / 2f;
    }
}

package edu.acg.taxidodger;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
/*
    This class describes the cat character and assigns the relevant resource files for each
    frame of the sprite's animation cycle. The constructor also takes a parameter to determine
    which color sprites will be used.
 */
public class Cat {
    Bitmap[] cat = new Bitmap[8];
    int catFrame = 0;

    public Cat(Context context, int color) {
        if (color == 0) {
            cat[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.cat_run0);
            cat[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.cat_run1);
            cat[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.cat_run2);
            cat[3] = BitmapFactory.decodeResource(context.getResources(), R.drawable.cat_run3);
            cat[4] = BitmapFactory.decodeResource(context.getResources(), R.drawable.cat_run4);
            cat[5] = BitmapFactory.decodeResource(context.getResources(), R.drawable.cat_run5);
            cat[6] = BitmapFactory.decodeResource(context.getResources(), R.drawable.cat_run6);
            cat[7] = BitmapFactory.decodeResource(context.getResources(), R.drawable.cat_run7);
        }
        else if (color == 1) {
            cat[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.white_cat_run0);
            cat[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.white_cat_run1);
            cat[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.white_cat_run2);
            cat[3] = BitmapFactory.decodeResource(context.getResources(), R.drawable.white_cat_run3);
            cat[4] = BitmapFactory.decodeResource(context.getResources(), R.drawable.white_cat_run4);
            cat[5] = BitmapFactory.decodeResource(context.getResources(), R.drawable.white_cat_run5);
            cat[6] = BitmapFactory.decodeResource(context.getResources(), R.drawable.white_cat_run6);
            cat[7] = BitmapFactory.decodeResource(context.getResources(), R.drawable.white_cat_run7);
        }
        else if (color == 2) {
            cat[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.pink_cat_run0);
            cat[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.pink_cat_run1);
            cat[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.pink_cat_run2);
            cat[3] = BitmapFactory.decodeResource(context.getResources(), R.drawable.pink_cat_run3);
            cat[4] = BitmapFactory.decodeResource(context.getResources(), R.drawable.pink_cat_run4);
            cat[5] = BitmapFactory.decodeResource(context.getResources(), R.drawable.pink_cat_run5);
            cat[6] = BitmapFactory.decodeResource(context.getResources(), R.drawable.pink_cat_run6);
            cat[7] = BitmapFactory.decodeResource(context.getResources(), R.drawable.pink_cat_run7);
        }
        else if (color == 3) {
            cat[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.neon_cat_run0);
            cat[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.neon_cat_run1);
            cat[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.neon_cat_run2);
            cat[3] = BitmapFactory.decodeResource(context.getResources(), R.drawable.neon_cat_run3);
            cat[4] = BitmapFactory.decodeResource(context.getResources(), R.drawable.neon_cat_run4);
            cat[5] = BitmapFactory.decodeResource(context.getResources(), R.drawable.neon_cat_run5);
            cat[6] = BitmapFactory.decodeResource(context.getResources(), R.drawable.neon_cat_run6);
            cat[7] = BitmapFactory.decodeResource(context.getResources(), R.drawable.neon_cat_run7);
        }
        else if (color == 4) {
            cat[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.night_cat_run0);
            cat[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.night_cat_run1);
            cat[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.night_cat_run2);
            cat[3] = BitmapFactory.decodeResource(context.getResources(), R.drawable.night_cat_run3);
            cat[4] = BitmapFactory.decodeResource(context.getResources(), R.drawable.night_cat_run4);
            cat[5] = BitmapFactory.decodeResource(context.getResources(), R.drawable.night_cat_run5);
            cat[6] = BitmapFactory.decodeResource(context.getResources(), R.drawable.night_cat_run6);
            cat[7] = BitmapFactory.decodeResource(context.getResources(), R.drawable.night_cat_run7);
        }
        else if (color == 5) {
            cat[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.gold_cat_run0);
            cat[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.gold_cat_run1);
            cat[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.gold_cat_run2);
            cat[3] = BitmapFactory.decodeResource(context.getResources(), R.drawable.gold_cat_run3);
            cat[4] = BitmapFactory.decodeResource(context.getResources(), R.drawable.gold_cat_run4);
            cat[5] = BitmapFactory.decodeResource(context.getResources(), R.drawable.gold_cat_run5);
            cat[6] = BitmapFactory.decodeResource(context.getResources(), R.drawable.gold_cat_run6);
            cat[7] = BitmapFactory.decodeResource(context.getResources(), R.drawable.gold_cat_run7);
        }
        else if (color == 6) {
            cat[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.sphynx_cat_run0);
            cat[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.sphynx_cat_run1);
            cat[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.sphynx_cat_run2);
            cat[3] = BitmapFactory.decodeResource(context.getResources(), R.drawable.sphynx_cat_run3);
            cat[4] = BitmapFactory.decodeResource(context.getResources(), R.drawable.sphynx_cat_run4);
            cat[5] = BitmapFactory.decodeResource(context.getResources(), R.drawable.sphynx_cat_run5);
            cat[6] = BitmapFactory.decodeResource(context.getResources(), R.drawable.sphynx_cat_run6);
            cat[7] = BitmapFactory.decodeResource(context.getResources(), R.drawable.sphynx_cat_run7);
        }
    }

    public Bitmap getCat(int catFrame) {
        return cat[catFrame];
    }

}


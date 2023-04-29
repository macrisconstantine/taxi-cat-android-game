package edu.acg.taxidodger;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Similar to the Cat class, the explosion class defines and assigns an array of frames
 * to be used for the explosion animation.
 */
public class Explosion {
    Bitmap[] explosion = new Bitmap[5];
    int explosionFrame = 0;
    int explosionX = 0;
    int explosionY = 0;

    public Explosion(Context context) {
        explosion[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.l0_sprite_13);
        explosion[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.l0_sprite_14);
        explosion[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.l0_sprite_21);
    }

    public Bitmap getExplosion(int explosionFrame) {
        return explosion[explosionFrame];
    }
}

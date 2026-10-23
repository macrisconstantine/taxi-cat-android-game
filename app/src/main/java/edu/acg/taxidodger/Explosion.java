package edu.acg.taxidodger;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * Short crash animation played where a taxi hit the cat. Frames advance on a
 * timer rather than on the draw count, so the animation lasts the same length
 * of time regardless of frame rate.
 */
public class Explosion {

    private static final int[] FRAMES = {
            R.drawable.l0_sprite_13,
            R.drawable.l0_sprite_14,
            R.drawable.l0_sprite_21,
    };

    private static final float FRAME_SECONDS = 0.07f;

    private final Bitmap[] explosion = new Bitmap[FRAMES.length];

    float explosionX, explosionY;
    int explosionFrame;
    private float timer;

    public Explosion(Context context) {
        for (int i = 0; i < FRAMES.length; i++) {
            explosion[i] = Taxi.cached(context, FRAMES[i]);
        }
    }

    /** Returns false once the last frame has played and the explosion should be dropped. */
    public boolean update(float dt) {
        timer += dt;
        explosionFrame = (int) (timer / FRAME_SECONDS);
        return explosionFrame < FRAMES.length;
    }

    public Bitmap current() {
        int index = Math.min(explosionFrame, FRAMES.length - 1);
        return explosion[Math.max(0, index)];
    }
}

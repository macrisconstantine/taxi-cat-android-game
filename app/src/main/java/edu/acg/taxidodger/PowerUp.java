package edu.acg.taxidodger;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * A collectible that drifts down the road. Drawn procedurally as a pulsing glyph
 * token, so adding a new one costs nothing in artwork.
 */
public class PowerUp {
    public static final int SHIELD = 0;
    public static final int SLOWMO = 1;
    public static final int DOUBLE = 2;
    public static final int LIFE = 3;
    public static final int TYPE_COUNT = 4;

    int type;
    float x, y, vy, radius;
    private float spin;

    public PowerUp(int type, float x, float y, float vy, float radius) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.vy = vy;
        this.radius = radius;
    }

    /** Returns false once the token has fallen off the bottom of the screen. */
    public boolean update(float dt, int screenHeight) {
        y += vy * dt;
        spin += dt;
        return y - radius * 2f <= screenHeight;
    }

    public int color() {
        switch (type) {
            case SHIELD:
                return Color.rgb(90, 200, 255);
            case SLOWMO:
                return Color.rgb(170, 130, 255);
            case DOUBLE:
                return Color.rgb(255, 205, 60);
            default:
                return Color.rgb(255, 95, 125);
        }
    }

    public String glyph() {
        switch (type) {
            case SHIELD:
                return "S";
            case SLOWMO:
                return "T";
            case DOUBLE:
                return "2";
            default:
                return "+";
        }
    }

    public String label() {
        switch (type) {
            case SHIELD:
                return "SHIELD";
            case SLOWMO:
                return "SLOW-MO";
            case DOUBLE:
                return "DOUBLE PTS";
            default:
                return "+1 LIFE";
        }
    }

    /** text must already be centre-aligned by the caller. */
    public void draw(Canvas canvas, Paint fill, Paint stroke, Paint text) {
        float r = radius * (1f + 0.12f * (float) Math.sin(spin * 5f));
        int c = color();

        fill.setColor(c);
        fill.setAlpha(60);
        canvas.drawCircle(x, y, r * 1.7f, fill);
        fill.setAlpha(255);
        canvas.drawCircle(x, y, r, fill);

        stroke.setColor(Color.WHITE);
        stroke.setStrokeWidth(Math.max(2f, r * 0.12f));
        canvas.drawCircle(x, y, r, stroke);

        text.setColor(Color.WHITE);
        text.setTextSize(r * 1.1f);
        canvas.drawText(glyph(), x, y + r * 0.4f, text);
    }
}

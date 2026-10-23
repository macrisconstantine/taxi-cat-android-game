package edu.acg.taxidodger;

import android.graphics.Canvas;
import android.graphics.Paint;

/** Score popup that drifts upward and fades out. Drawn centred on x. */
public class FloatingText {
    String text;
    float x, y, life, maxLife, size;
    int color;

    public FloatingText(String text, float x, float y, float life, float size, int color) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.life = life;
        this.maxLife = life;
        this.size = size;
        this.color = color;
    }

    public boolean update(float dt) {
        y -= 120f * dt;
        life -= dt;
        return life > 0f;
    }

    /** Caller is responsible for the paint's typeface and centre alignment. */
    public void draw(Canvas canvas, Paint paint) {
        float t = life / maxLife;
        if (t < 0f) {
            t = 0f;
        }
        // Pop in quickly, then hold, so the number is readable before it fades.
        float scale = 1f + 0.35f * (1f - Math.min(1f, (maxLife - life) * 6f));
        paint.setTextSize(size * scale);
        paint.setColor(color);
        paint.setAlpha((int) (255 * Math.min(1f, t * 2.2f)));
        canvas.drawText(text, x, y, paint);
    }
}

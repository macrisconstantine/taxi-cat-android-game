package edu.acg.taxidodger;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * A single short-lived spark. Used for taxi exhaust, pickup bursts and crash debris.
 * Everything is drawn as a fading circle, so no art assets are needed.
 */
public class Particle {
    float x, y, vx, vy;
    float life, maxLife, size;
    int color;
    boolean heavy;

    public Particle(float x, float y, float vx, float vy, float life, float size, int color, boolean heavy) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.life = life;
        this.maxLife = life;
        this.size = size;
        this.color = color;
        this.heavy = heavy;
    }

    /** Returns false once the spark has burned out and should be dropped. */
    public boolean update(float dt) {
        x += vx * dt;
        y += vy * dt;
        if (heavy) {
            vy += 1600f * dt;
        }
        float drag = 1f - 1.8f * dt;
        if (drag < 0f) {
            drag = 0f;
        }
        vx *= drag;
        life -= dt;
        return life > 0f;
    }

    public void draw(Canvas canvas, Paint paint) {
        float t = life / maxLife;
        if (t < 0f) {
            t = 0f;
        }
        paint.setColor(color);
        paint.setAlpha((int) (255 * t));
        canvas.drawCircle(x, y, size * (0.3f + 0.7f * t), paint);
    }
}

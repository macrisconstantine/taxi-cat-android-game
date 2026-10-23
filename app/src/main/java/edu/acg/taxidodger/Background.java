package edu.acg.taxidodger;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;

import java.util.Random;

/**
 * The scrolling street the game is played on. Everything here is drawn with
 * primitives rather than bitmaps, so each district is just a palette swap.
 */
public class Background {

    /** asphalt dark, asphalt light, kerb, kerb stripe, lane line, lamp glow */
    private static final int[][] PALETTES = {
            {0xFF14141A, 0xFF2C2C35, 0xFF4C4C58, 0xFFE6E6EE, 0xFFF2E85C, 0xFFFFD98A},
            {0xFF11161D, 0xFF2A3340, 0xFF46545F, 0xFFDDE8EE, 0xFF8FD8FF, 0xFF9FE4FF},
            {0xFF18101D, 0xFF33283A, 0xFF574566, 0xFFF0DFF5, 0xFFFF7AD9, 0xFFFF9BE3},
            {0xFF101810, 0xFF26362C, 0xFF415A47, 0xFFDDF0E0, 0xFF9CF06E, 0xFFB6FF8F},
            {0xFF1C1310, 0xFF3A2E26, 0xFF604B3C, 0xFFF5E6D8, 0xFFFFB45C, 0xFFFFC98A},
            {0xFF0B0D1C, 0xFF1E2240, 0xFF3A4170, 0xFFDCE0FF, 0xFFC2A6FF, 0xFFB9A2FF},
    };

    private static final String[] DISTRICTS = {
            "DOWNTOWN", "HARBOUR", "NEON MILE", "THE PARK", "OLD TOWN", "MIDNIGHT LOOP",
    };

    private static final int LANES = 5;
    private static final int FEATURE_COUNT = 5;
    private static final int LAMP_COUNT = 6;

    private static final int FEATURE_CROSSWALK = 0;
    private static final int FEATURE_ARROW = 1;
    private static final int FEATURE_MANHOLE = 2;

    private final int width, height;
    private final Random random = new Random();

    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint lane = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();

    private final float[] featureY = new float[FEATURE_COUNT];
    private final int[] featureType = new int[FEATURE_COUNT];
    private final float[] featureX = new float[FEATURE_COUNT];

    private final float[] lampY = new float[LAMP_COUNT];
    private final boolean[] lampLeft = new boolean[LAMP_COUNT];

    /** One gradient per lamp, rebuilt only when the district changes. */
    private final RadialGradient[] lampShader = new RadialGradient[LAMP_COUNT];

    private final float kerbWidth, dashLength, dashGap, lampRadius;
    private float dashScroll, kerbScroll;

    private int district;
    private int[] palette = PALETTES[0];
    private LinearGradient asphaltShader;

    public Background(int width, int height) {
        this.width = width;
        this.height = height;
        this.kerbWidth = width * 0.045f;
        this.dashLength = height * 0.07f;
        this.dashGap = height * 0.06f;
        this.lampRadius = width * 0.38f;

        lane.setStyle(Paint.Style.FILL);
        glow.setStyle(Paint.Style.FILL);

        for (int i = 0; i < FEATURE_COUNT; i++) {
            featureY[i] = -i * (height * 0.8f) - random.nextFloat() * height;
            randomiseFeature(i);
        }
        for (int i = 0; i < LAMP_COUNT; i++) {
            lampY[i] = -i * (height / 3f);
            lampLeft[i] = (i % 2 == 0);
        }
        setDistrict(0);
    }

    private void randomiseFeature(int i) {
        int roll = random.nextInt(10);
        if (roll < 2) {
            featureType[i] = FEATURE_CROSSWALK;
        } else if (roll < 6) {
            featureType[i] = FEATURE_ARROW;
        } else {
            featureType[i] = FEATURE_MANHOLE;
        }
        featureX[i] = kerbWidth + random.nextFloat() * (width - 2 * kerbWidth);
    }

    public void setDistrict(int index) {
        district = ((index % PALETTES.length) + PALETTES.length) % PALETTES.length;
        palette = PALETTES[district];
        asphaltShader = new LinearGradient(0, 0, 0, height,
                palette[0], palette[1], Shader.TileMode.CLAMP);

        // Shaders are positioned in local space and then translated at draw time,
        // so one per lamp is enough no matter where the lamp has scrolled to.
        int soft = (palette[5] & 0x00FFFFFF) | 0x3C000000;
        for (int i = 0; i < LAMP_COUNT; i++) {
            lampShader[i] = new RadialGradient(0, 0, lampRadius, soft,
                    Color.TRANSPARENT, Shader.TileMode.CLAMP);
        }
    }

    public String districtName() {
        return DISTRICTS[district];
    }

    public int laneColor() {
        return palette[4];
    }

    /** speed is in pixels per second, matching whatever the taxis are doing. */
    public void update(float dt, float speed) {
        dashScroll = (dashScroll + speed * dt) % (dashLength + dashGap);
        kerbScroll = (kerbScroll + speed * dt) % (height * 0.05f);

        for (int i = 0; i < FEATURE_COUNT; i++) {
            featureY[i] += speed * 0.85f * dt;
            if (featureY[i] > height + height * 0.2f) {
                featureY[i] -= FEATURE_COUNT * height * 0.8f;
                randomiseFeature(i);
            }
        }
        for (int i = 0; i < LAMP_COUNT; i++) {
            lampY[i] += speed * 0.7f * dt;
            if (lampY[i] > height + height * 0.1f) {
                lampY[i] -= LAMP_COUNT * (height / 3f);
                lampLeft[i] = !lampLeft[i];
            }
        }
    }

    public void draw(Canvas canvas) {
        fill.setShader(asphaltShader);
        canvas.drawRect(0, 0, width, height, fill);
        fill.setShader(null);

        drawRoadFeatures(canvas);
        drawLaneDashes(canvas);
        drawKerbs(canvas);
        drawLamps(canvas);
    }

    private void drawLaneDashes(Canvas canvas) {
        lane.setColor(palette[4]);
        lane.setAlpha(150);
        float w = Math.max(3f, width * 0.008f);
        float step = dashLength + dashGap;
        for (int l = 1; l < LANES; l++) {
            float x = kerbWidth + (width - 2 * kerbWidth) * l / (float) LANES;
            for (float y = dashScroll - step; y < height + step; y += step) {
                canvas.drawRect(x - w / 2f, y, x + w / 2f, y + dashLength, lane);
            }
        }
        lane.setAlpha(255);
    }

    private void drawKerbs(Canvas canvas) {
        fill.setColor(palette[2]);
        canvas.drawRect(0, 0, kerbWidth, height, fill);
        canvas.drawRect(width - kerbWidth, 0, width, height, fill);

        // Alternating stripes along the kerb edge give the sense of speed.
        fill.setColor(palette[3]);
        fill.setAlpha(110);
        float stripe = height * 0.025f;
        float step = height * 0.05f;
        for (float y = kerbScroll - step; y < height + step; y += step) {
            canvas.drawRect(kerbWidth * 0.55f, y, kerbWidth, y + stripe, fill);
            canvas.drawRect(width - kerbWidth, y + step / 2f,
                    width - kerbWidth * 0.55f, y + step / 2f + stripe, fill);
        }
        fill.setAlpha(255);
    }

    private void drawRoadFeatures(Canvas canvas) {
        for (int i = 0; i < FEATURE_COUNT; i++) {
            float y = featureY[i];
            if (featureType[i] == FEATURE_CROSSWALK) {
                fill.setColor(palette[3]);
                fill.setAlpha(55);
                float bandW = (width - 2 * kerbWidth) / 11f;
                for (int b = 0; b < 6; b++) {
                    float x = kerbWidth + b * bandW * 2f;
                    canvas.drawRect(x, y, x + bandW, y + height * 0.09f, fill);
                }
                fill.setAlpha(255);
            } else if (featureType[i] == FEATURE_ARROW) {
                fill.setColor(palette[3]);
                fill.setAlpha(45);
                float s = width * 0.05f;
                float cx = featureX[i];
                path.reset();
                path.moveTo(cx, y);
                path.lineTo(cx - s, y + s * 1.1f);
                path.lineTo(cx - s * 0.4f, y + s * 1.1f);
                path.lineTo(cx - s * 0.4f, y + s * 2.2f);
                path.lineTo(cx + s * 0.4f, y + s * 2.2f);
                path.lineTo(cx + s * 0.4f, y + s * 1.1f);
                path.lineTo(cx + s, y + s * 1.1f);
                path.close();
                canvas.drawPath(path, fill);
                fill.setAlpha(255);
            } else {
                fill.setColor(palette[0]);
                fill.setAlpha(170);
                canvas.drawCircle(featureX[i], y, width * 0.035f, fill);
                fill.setAlpha(255);
            }
        }
    }

    private void drawLamps(Canvas canvas) {
        for (int i = 0; i < LAMP_COUNT; i++) {
            float x = lampLeft[i] ? kerbWidth * 0.5f : width - kerbWidth * 0.5f;
            float y = lampY[i];

            canvas.save();
            canvas.translate(x, y);
            glow.setShader(lampShader[i]);
            canvas.drawCircle(0, 0, lampRadius, glow);
            glow.setShader(null);
            canvas.restore();

            glow.setColor(palette[5]);
            canvas.drawCircle(x, y, Math.max(4f, width * 0.012f), glow);
        }
    }
}

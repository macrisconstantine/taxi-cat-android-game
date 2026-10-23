package edu.acg.taxidodger;

import static android.content.Context.VIBRATOR_SERVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowMetrics;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/*
    The core functionality of this class was based on the following youtube tutorial:
    https://www.youtube.com/watch?v=gfX8UHTpq3o&ab_channel=SandipBhattacharya

    Primary creative credit goes to the creator of the above video.
 */

// Define a class named GameView that extends the View class
public class GameView extends View {

    private static final long FRAME_MILLIS = 16;
    /** Clamped so a stall never teleports taxis through the cat. */
    private static final float MAX_DT = 0.05f;

    private static final int MAX_LIFE = 5;
    private static final int POINTS_PER_DISTRICT = 750;
    private static final int MAX_TAXIS = 9;

    private static final float COMBO_WINDOW = 2.5f;
    private static final float SLOWMO_SECONDS = 5f;
    private static final float DOUBLE_SECONDS = 8f;
    private static final float SLOWMO_SCALE = 0.45f;
    private static final float BANNER_SECONDS = 2.2f;

    /** Collision boxes are smaller than the art - a pixel-tight dodger is no fun. */
    private static final float CAT_HITBOX = 0.70f;
    private static final float TAXI_HITBOX = 0.84f;

    private static final int MAX_PARTICLES = 180;

    Cat cat;
    Bitmap catFrame;
    Context context;
    Handler handler;
    Runnable runnable;

    static int dWidth, dHeight;
    private int topInset, bottomInset;

    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shapePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path heartPath = new Path();
    private final RectF pillRect = new RectF();
    private final Rect pauseButton = new Rect();

    private float textSize;

    private Background background;
    private final ArrayList<Taxi> taxis = new ArrayList<>();
    private final ArrayList<Explosion> explosions = new ArrayList<>();
    private final ArrayList<Particle> particles = new ArrayList<>();
    private final ArrayList<FloatingText> popups = new ArrayList<>();
    private final ArrayList<PowerUp> powerUps = new ArrayList<>();

    private Random random;

    private int points;
    private int life = 3;
    private int district;
    private int combo;
    private int bestCombo;

    private float comboTimer;
    private float shieldTimer;
    private float slowMoTimer;
    private float doubleTimer;
    private float bannerTimer;
    private float powerUpTimer;
    private float shakeTime, shakeMagnitude;
    private float catAnimTimer;
    private float speedMultiplier = 1f;

    private boolean shielded;
    private boolean paused;
    private boolean gameOver;
    private boolean detached;

    private String bannerText = "";

    float catX, catY;
    float oldX, oldY;
    float oldCatX, oldCatY;
    private int catWidth, catHeight;

    private long lastUpdateTime;
    private String formattedDate;

    public static MediaPlayer currentlyPlayingSong;

    @SuppressLint("SourceLockedOrientationActivity")
    public GameView(Context context) {
        super(context);
        this.context = context;
        this.cat = new Cat(context, SplashActivity.catColor);
        this.random = new Random();

        Activity activity = (Activity) context;
        int currentOrientation = getResources().getConfiguration().orientation;

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm", Locale.UK);
        formattedDate = dateFormat.format(new Date());

        // Lock in screen orientation based on device orientation for the duration of the game
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
        } else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }

        if (GameView.currentlyPlayingSong != null) {
            GameView.stopMusic();
        }
        playMusic();

        measureScreen(activity);

        // Under edge-to-edge the canvas runs behind the status and nav bars, so the
        // HUD needs the real inset values to stay clear of them.
        ViewCompat.setOnApplyWindowInsetsListener(this, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            topInset = bars.top;
            bottomInset = bars.bottom;
            return insets;
        });

        textSize = Math.max(28f, dWidth * 0.062f);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(ResourcesCompat.getFont(context, R.font.press_start));
        // The road is busy; text needs a drop shadow to stay readable over a taxi.
        textPaint.setShadowLayer(textSize * 0.12f, 0f, textSize * 0.05f, 0xCC000000);

        centerPaint.setColor(Color.WHITE);
        centerPaint.setTextAlign(Paint.Align.CENTER);
        centerPaint.setTypeface(ResourcesCompat.getFont(context, R.font.press_start));
        centerPaint.setShadowLayer(textSize * 0.12f, 0f, textSize * 0.05f, 0xCC000000);

        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(Math.max(3f, dWidth * 0.008f));

        background = new Background(dWidth, dHeight);

        catFrame = cat.getCat(0);
        catWidth = catFrame.getWidth();
        catHeight = catFrame.getHeight();
        catX = dWidth / 2f - catWidth / 2f;
        catY = dHeight - catHeight * 2.5f;

        for (int i = 0; i < 3; i++) {
            Taxi taxi = new Taxi(context);
            taxi.assignType(district);
            taxis.add(taxi);
        }

        powerUpTimer = 7f;
        bannerText = background.districtName();
        bannerTimer = BANNER_SECONDS;

        handler = new Handler();
        runnable = () -> {
            if (!detached) {
                invalidate();
            }
        };
        lastUpdateTime = System.currentTimeMillis();
    }

    @SuppressWarnings("deprecation")
    private void measureScreen(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics metrics = activity.getWindowManager().getCurrentWindowMetrics();
            Rect bounds = metrics.getBounds();
            dWidth = bounds.width();
            dHeight = bounds.height();
        } else {
            Display display = activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            dWidth = size.x;
            dHeight = size.y;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // Without this the loop kept reposting itself after the activity had gone.
        detached = true;
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
    }

    @SuppressLint("DrawAllocation")
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long now = System.currentTimeMillis();
        float dt = (now - lastUpdateTime) / 1000f;
        lastUpdateTime = now;
        if (dt > MAX_DT) {
            dt = MAX_DT;
        }
        if (dt < 0f) {
            dt = 0f;
        }

        if (!paused && !gameOver) {
            update(dt);
        }
        render(canvas);

        if (!gameOver && !detached) {
            handler.postDelayed(runnable, FRAME_MILLIS);
        }
    }

    // ---------------------------------------------------------------- update

    private void update(float realDt) {
        // Power-up countdowns run on real time; everything in the world runs on
        // scaled time, so slow-mo does not also slow down its own expiry.
        tickTimers(realDt);

        float dt = realDt * (slowMoTimer > 0f ? SLOWMO_SCALE : 1f);

        catAnimTimer += dt;
        if (catAnimTimer >= 0.045f) {
            catAnimTimer = 0f;
            cat.catFrame = (cat.catFrame + 1) % 8;
        }
        catFrame = cat.getCat(cat.catFrame);

        background.update(dt, dHeight * 0.5f * speedMultiplier);

        updateTaxis(dt);
        updatePowerUps(dt);
        updateEffects(dt);
        checkDistrict();
    }

    private void tickTimers(float dt) {
        if (slowMoTimer > 0f) {
            slowMoTimer -= dt;
        }
        if (doubleTimer > 0f) {
            doubleTimer -= dt;
        }
        if (shieldTimer > 0f) {
            shieldTimer -= dt;
        }
        if (bannerTimer > 0f) {
            bannerTimer -= dt;
        }
        if (shakeTime > 0f) {
            shakeTime -= dt;
        }
        if (comboTimer > 0f) {
            comboTimer -= dt;
            if (comboTimer <= 0f) {
                combo = 0;
            }
        }
        powerUpTimer -= dt;
        if (powerUpTimer <= 0f) {
            spawnPowerUp();
            powerUpTimer = 9f + random.nextFloat() * 7f;
        }
    }

    private void updateTaxis(float dt) {
        float catCenterY = catY + catHeight / 2f;
        float catCenterX = catX + catWidth / 2f;
        float nearMissRange = catWidth * 1.15f;

        for (int i = 0; i < taxis.size(); i++) {
            Taxi taxi = taxis.get(i);
            taxi.update(dt, speedMultiplier);

            // Exhaust trail
            if (random.nextFloat() < 0.35f) {
                addParticle(new Particle(
                        taxi.centerX() + (random.nextFloat() - 0.5f) * taxi.getTaxiWidth() * 0.4f,
                        taxi.taxiY,
                        (random.nextFloat() - 0.5f) * 40f,
                        -120f - random.nextFloat() * 80f,
                        0.35f, taxi.getTaxiWidth() * 0.07f,
                        Color.rgb(200, 200, 210), false));
            }

            // Collision first: a taxi that actually hits must not also pay out a
            // near miss on the same frame.
            if (overlapsCat(taxi)) {
                if (handleCollision(taxi)) {
                    return;
                }
                continue;
            }

            if (!taxi.passedCat && taxi.centerY() > catCenterY) {
                taxi.passedCat = true;
                float gap = Math.abs(taxi.centerX() - catCenterX);
                if (gap < nearMissRange) {
                    registerNearMiss(taxi);
                }
            }

            if (taxi.taxiY >= dHeight) {
                points += 10 * scoreMultiplier();
                taxi.resetPosition();
                taxi.assignType(district);
            }
        }
    }

    private void registerNearMiss(Taxi taxi) {
        combo++;
        comboTimer = COMBO_WINDOW;
        if (combo > bestCombo) {
            bestCombo = combo;
        }
        int bonus = 25 * Math.min(combo, 10) * scoreMultiplier();
        points += bonus;

        popups.add(new FloatingText("+" + bonus, taxi.centerX(), taxi.centerY(),
                0.9f, textSize * 0.55f, background.laneColor()));
        if (combo >= 3) {
            popups.add(new FloatingText("COMBO x" + combo, catX + catWidth / 2f,
                    catY - catHeight * 0.6f, 1.0f, textSize * 0.5f, Color.rgb(255, 215, 90)));
        }
        for (int i = 0; i < 6; i++) {
            addParticle(new Particle(taxi.centerX(), taxi.centerY(),
                    (random.nextFloat() - 0.5f) * 300f, (random.nextFloat() - 0.5f) * 300f,
                    0.4f, catWidth * 0.05f, background.laneColor(), false));
        }
    }

    private boolean overlapsCat(Taxi taxi) {
        float cw = catWidth * CAT_HITBOX;
        float ch = catHeight * CAT_HITBOX;
        float cx = catX + (catWidth - cw) / 2f;
        float cy = catY + (catHeight - ch) / 2f;

        float tw = taxi.getTaxiWidth() * TAXI_HITBOX;
        float th = taxi.getTaxiHeight() * TAXI_HITBOX;
        float tx = taxi.taxiX + (taxi.getTaxiWidth() - tw) / 2f;
        float ty = taxi.taxiY + (taxi.getTaxiHeight() - th) / 2f;

        return tx < cx + cw && tx + tw > cx && ty < cy + ch && ty + th > cy;
    }

    /** Returns true when the hit ended the game and the update should stop. */
    private boolean handleCollision(Taxi taxi) {
        Explosion explosion = new Explosion(context);
        explosion.explosionX = taxi.centerX() - explosion.current().getWidth() / 2f;
        explosion.explosionY = taxi.centerY() - explosion.current().getHeight() / 2f;
        explosions.add(explosion);

        for (int p = 0; p < 16; p++) {
            addParticle(new Particle(taxi.centerX(), taxi.centerY(),
                    (random.nextFloat() - 0.5f) * 700f, (random.nextFloat() - 0.8f) * 700f,
                    0.6f + random.nextFloat() * 0.3f, catWidth * 0.06f,
                    p % 3 == 0 ? Color.rgb(255, 220, 90) : Color.rgb(255, 120, 40), true));
        }

        taxi.resetPosition();
        taxi.assignType(district);

        if (shielded) {
            // The shield eats the hit and the combo survives.
            shielded = false;
            shieldTimer = 0f;
            shake(0.25f, dWidth * 0.012f);
            popups.add(new FloatingText("BLOCKED!", catX + catWidth / 2f,
                    catY - catHeight * 0.5f, 1.1f, textSize * 0.55f, Color.rgb(90, 200, 255)));
            damageEffect();
            return false;
        }

        life--;
        combo = 0;
        comboTimer = 0f;
        shake(0.4f, dWidth * 0.02f);
        damageEffect();

        if (life <= 0) {
            endGame();
            return true;
        }
        return false;
    }

    private void updatePowerUps(float dt) {
        float cw = catWidth * 0.9f;
        float ch = catHeight * 0.9f;
        float cx = catX + (catWidth - cw) / 2f;
        float cy = catY + (catHeight - ch) / 2f;

        for (int i = powerUps.size() - 1; i >= 0; i--) {
            PowerUp powerUp = powerUps.get(i);
            boolean alive = powerUp.update(dt, dHeight);

            boolean collected = powerUp.x + powerUp.radius > cx
                    && powerUp.x - powerUp.radius < cx + cw
                    && powerUp.y + powerUp.radius > cy
                    && powerUp.y - powerUp.radius < cy + ch;

            if (collected) {
                collect(powerUp);
                powerUps.remove(i);
            } else if (!alive) {
                powerUps.remove(i);
            }
        }
    }

    private void collect(PowerUp powerUp) {
        switch (powerUp.type) {
            case PowerUp.SHIELD:
                shielded = true;
                shieldTimer = 12f;
                break;
            case PowerUp.SLOWMO:
                slowMoTimer = SLOWMO_SECONDS;
                break;
            case PowerUp.DOUBLE:
                doubleTimer = DOUBLE_SECONDS;
                break;
            default:
                life = Math.min(MAX_LIFE, life + 1);
                break;
        }

        popups.add(new FloatingText(powerUp.label(), powerUp.x, powerUp.y,
                1.2f, textSize * 0.55f, powerUp.color()));
        for (int i = 0; i < 18; i++) {
            double angle = random.nextFloat() * Math.PI * 2;
            float speed = 180f + random.nextFloat() * 320f;
            addParticle(new Particle(powerUp.x, powerUp.y,
                    (float) Math.cos(angle) * speed, (float) Math.sin(angle) * speed,
                    0.55f, powerUp.radius * 0.22f, powerUp.color(), false));
        }
    }

    private void spawnPowerUp() {
        int type;
        float roll = random.nextFloat();
        if (roll < 0.34f) {
            type = PowerUp.SHIELD;
        } else if (roll < 0.62f) {
            type = PowerUp.SLOWMO;
        } else if (roll < 0.90f) {
            type = PowerUp.DOUBLE;
        } else {
            type = PowerUp.LIFE;
        }

        float radius = Math.max(26f, dWidth * 0.055f);
        float x = radius * 2f + random.nextFloat() * (dWidth - radius * 4f);
        powerUps.add(new PowerUp(type, x, -radius * 2f, dHeight * 0.30f, radius));
    }

    private void updateEffects(float dt) {
        for (int i = particles.size() - 1; i >= 0; i--) {
            if (!particles.get(i).update(dt)) {
                particles.remove(i);
            }
        }
        for (int i = popups.size() - 1; i >= 0; i--) {
            if (!popups.get(i).update(dt)) {
                popups.remove(i);
            }
        }
        for (int i = explosions.size() - 1; i >= 0; i--) {
            if (!explosions.get(i).update(dt)) {
                explosions.remove(i);
            }
        }
    }

    private void checkDistrict() {
        int newDistrict = points / POINTS_PER_DISTRICT;
        if (newDistrict <= district) {
            return;
        }

        district = newDistrict;
        speedMultiplier += 0.11f;
        background.setDistrict(district);

        for (int i = 0; i < taxis.size(); i++) {
            taxis.get(i).changeColor(context, district);
        }
        if (taxis.size() < MAX_TAXIS) {
            Taxi taxi = new Taxi(context);
            taxi.changeColor(context, district);
            taxi.assignType(district);
            taxis.add(taxi);
        }
        // A life back every third district, so a long run stays survivable.
        if (district % 3 == 0 && life < MAX_LIFE) {
            life++;
        }

        bannerText = background.districtName();
        bannerTimer = BANNER_SECONDS;
    }

    private int scoreMultiplier() {
        return doubleTimer > 0f ? 2 : 1;
    }

    private void addParticle(Particle particle) {
        if (particles.size() < MAX_PARTICLES) {
            particles.add(particle);
        }
    }

    private void shake(float duration, float magnitude) {
        shakeTime = Math.max(shakeTime, duration);
        shakeMagnitude = Math.max(shakeMagnitude, magnitude);
    }

    private void endGame() {
        gameOver = true;
        stopMusic();
        handler.removeCallbacks(runnable);

        Activity activity = (Activity) context;
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        Intent intent = new Intent(context, GameOverActivity.class);
        intent.putExtra("points", points);
        intent.putExtra("color", SplashActivity.catColor);
        intent.putExtra("name", SplashActivity.userName);
        intent.putExtra("combo", bestCombo);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra("date", String.valueOf(LocalDateTime.now()));
        } else {
            intent.putExtra("date", String.valueOf(formattedDate));
        }
        context.startActivity(intent);
        activity.finish();
    }

    // ---------------------------------------------------------------- render

    private void render(Canvas canvas) {
        int saveCount = canvas.save();
        if (shakeTime > 0f) {
            float amount = shakeMagnitude * (shakeTime / 0.4f);
            canvas.translate((random.nextFloat() - 0.5f) * amount,
                    (random.nextFloat() - 0.5f) * amount);
        } else {
            shakeMagnitude = 0f;
        }

        background.draw(canvas);

        for (int i = 0; i < particles.size(); i++) {
            particles.get(i).draw(canvas, shapePaint);
        }
        shapePaint.setAlpha(255);

        for (int i = 0; i < powerUps.size(); i++) {
            powerUps.get(i).draw(canvas, shapePaint, strokePaint, centerPaint);
        }
        shapePaint.setAlpha(255);

        for (int i = 0; i < taxis.size(); i++) {
            Taxi taxi = taxis.get(i);
            canvas.drawBitmap(taxi.taxi, taxi.taxiX, taxi.taxiY, null);
        }

        drawCat(canvas);

        for (int i = 0; i < explosions.size(); i++) {
            Explosion explosion = explosions.get(i);
            canvas.drawBitmap(explosion.current(), explosion.explosionX, explosion.explosionY, null);
        }

        centerPaint.setTextAlign(Paint.Align.CENTER);
        for (int i = 0; i < popups.size(); i++) {
            popups.get(i).draw(canvas, centerPaint);
        }
        centerPaint.setAlpha(255);

        canvas.restoreToCount(saveCount);

        if (slowMoTimer > 0f) {
            shapePaint.setColor(Color.rgb(120, 150, 255));
            shapePaint.setAlpha(40);
            canvas.drawRect(0, 0, dWidth, dHeight, shapePaint);
            shapePaint.setAlpha(255);
        }

        drawHud(canvas);

        if (paused) {
            drawPauseOverlay(canvas);
        }
    }

    private void drawCat(Canvas canvas) {
        if (shielded) {
            float cx = catX + catWidth / 2f;
            float cy = catY + catHeight / 2f;
            float r = Math.max(catWidth, catHeight) * 0.78f;
            // Flash faster as the shield runs low so its loss is never a surprise.
            float urgency = shieldTimer < 3f ? 9f : 3f;
            int alpha = (int) (90 + 70 * Math.sin(System.currentTimeMillis() / 1000.0 * urgency));

            shapePaint.setColor(Color.rgb(90, 200, 255));
            shapePaint.setAlpha(Math.max(30, Math.min(200, alpha)));
            canvas.drawCircle(cx, cy, r, shapePaint);
            shapePaint.setAlpha(255);

            strokePaint.setColor(Color.rgb(160, 230, 255));
            canvas.drawCircle(cx, cy, r, strokePaint);
        }
        canvas.drawBitmap(catFrame, catX, catY, null);
    }

    private void drawHud(Canvas canvas) {
        float left = dWidth * 0.045f;
        // Baseline has to clear the full ascent or the digits lose their tops.
        float top = topInset + textSize * 1.15f;

        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(textSize);
        canvas.drawText(String.valueOf(points), left, top, textPaint);

        if (combo > 1) {
            textPaint.setTextSize(textSize * 0.5f);
            textPaint.setColor(Color.rgb(255, 215, 90));
            canvas.drawText("COMBO x" + combo, left, top + textSize * 0.65f, textPaint);
            textPaint.setTextSize(textSize);
        }

        // Pause button, top right
        int size = (int) (textSize * 0.9f);
        int right = (int) (dWidth - dWidth * 0.045f);
        pauseButton.set(right - size, (int) (topInset + textSize * 0.2f), right,
                (int) (topInset + textSize * 0.2f) + size);

        // Backing plate: white-on-yellow-taxi left the icon almost invisible.
        float padding = size * 0.32f;
        pillRect.set(pauseButton.left - padding, pauseButton.top - padding,
                pauseButton.right + padding, pauseButton.bottom + padding);
        shapePaint.setColor(Color.BLACK);
        shapePaint.setAlpha(110);
        canvas.drawRoundRect(pillRect, size * 0.3f, size * 0.3f, shapePaint);

        shapePaint.setColor(Color.WHITE);
        shapePaint.setAlpha(230);
        float barW = size * 0.26f;
        if (paused) {
            heartPath.reset();
            heartPath.moveTo(pauseButton.left, pauseButton.top);
            heartPath.lineTo(pauseButton.right, pauseButton.centerY());
            heartPath.lineTo(pauseButton.left, pauseButton.bottom);
            heartPath.close();
            canvas.drawPath(heartPath, shapePaint);
        } else {
            canvas.drawRect(pauseButton.left, pauseButton.top,
                    pauseButton.left + barW, pauseButton.bottom, shapePaint);
            canvas.drawRect(pauseButton.right - barW, pauseButton.top,
                    pauseButton.right, pauseButton.bottom, shapePaint);
        }
        shapePaint.setAlpha(255);

        // Hearts, under the pause button
        float heartSize = textSize * 0.33f;
        float hy = pauseButton.bottom + heartSize * 1.6f;
        for (int i = 0; i < life; i++) {
            float hx = right - heartSize - i * heartSize * 2.4f;
            shapePaint.setColor(i == 0 && life == 1 ? Color.rgb(255, 70, 70) : Color.rgb(255, 120, 150));
            drawHeart(canvas, hx, hy, heartSize, shapePaint);
        }

        drawPowerUpPills(canvas);

        if (bannerTimer > 0f) {
            float fade = Math.min(1f, bannerTimer / 0.6f);
            centerPaint.setTextAlign(Paint.Align.CENTER);
            centerPaint.setTextSize(textSize * 0.8f);
            centerPaint.setColor(background.laneColor());
            centerPaint.setAlpha((int) (255 * fade));
            canvas.drawText(bannerText, dWidth / 2f, dHeight * 0.32f, centerPaint);

            centerPaint.setTextSize(textSize * 0.4f);
            centerPaint.setColor(Color.WHITE);
            centerPaint.setAlpha((int) (200 * fade));
            canvas.drawText("DISTRICT " + (district + 1), dWidth / 2f,
                    dHeight * 0.32f + textSize * 0.7f, centerPaint);
            centerPaint.setAlpha(255);
        }
    }

    private void drawPowerUpPills(Canvas canvas) {
        float x = dWidth * 0.045f;
        float y = dHeight - bottomInset - textSize * 0.8f;
        float h = textSize * 0.42f;

        if (slowMoTimer > 0f) {
            x = drawPill(canvas, x, y, h, "SLOW " + (int) Math.ceil(slowMoTimer),
                    Color.rgb(170, 130, 255), slowMoTimer / SLOWMO_SECONDS);
        }
        if (doubleTimer > 0f) {
            x = drawPill(canvas, x, y, h, "2X " + (int) Math.ceil(doubleTimer),
                    Color.rgb(255, 205, 60), doubleTimer / DOUBLE_SECONDS);
        }
        if (shielded) {
            drawPill(canvas, x, y, h, "SHIELD", Color.rgb(90, 200, 255), 1f);
        }
    }

    /** Returns the x position the next pill should start at. */
    private float drawPill(Canvas canvas, float x, float y, float h, String label,
                           int color, float fraction) {
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(h * 0.62f);
        float w = textPaint.measureText(label) + h * 1.1f;

        pillRect.set(x, y - h, x + w, y);
        shapePaint.setColor(color);
        shapePaint.setAlpha(60);
        canvas.drawRoundRect(pillRect, h / 2f, h / 2f, shapePaint);

        pillRect.set(x, y - h, x + w * Math.max(0f, Math.min(1f, fraction)), y);
        shapePaint.setAlpha(150);
        canvas.drawRoundRect(pillRect, h / 2f, h / 2f, shapePaint);
        shapePaint.setAlpha(255);

        textPaint.setColor(Color.WHITE);
        canvas.drawText(label, x + h * 0.55f, y - h * 0.28f, textPaint);
        textPaint.setTextSize(textSize);

        return x + w + h * 0.5f;
    }

    private void drawHeart(Canvas canvas, float cx, float cy, float size, Paint paint) {
        heartPath.reset();
        heartPath.moveTo(cx, cy + size * 0.6f);
        heartPath.cubicTo(cx - size * 1.6f, cy - size * 0.35f,
                cx - size * 0.55f, cy - size * 1.1f, cx, cy - size * 0.3f);
        heartPath.cubicTo(cx + size * 0.55f, cy - size * 1.1f,
                cx + size * 1.6f, cy - size * 0.35f, cx, cy + size * 0.6f);
        heartPath.close();
        canvas.drawPath(heartPath, paint);
    }

    private void drawPauseOverlay(Canvas canvas) {
        shapePaint.setColor(Color.BLACK);
        shapePaint.setAlpha(165);
        canvas.drawRect(0, 0, dWidth, dHeight, shapePaint);
        shapePaint.setAlpha(255);

        centerPaint.setTextAlign(Paint.Align.CENTER);
        centerPaint.setColor(Color.WHITE);
        centerPaint.setTextSize(textSize);
        canvas.drawText("PAUSED", dWidth / 2f, dHeight * 0.45f, centerPaint);

        centerPaint.setTextSize(textSize * 0.4f);
        centerPaint.setColor(background.laneColor());
        canvas.drawText("TAP THE ARROW TO RESUME", dWidth / 2f,
                dHeight * 0.45f + textSize * 0.9f, centerPaint);

        centerPaint.setColor(Color.WHITE);
        canvas.drawText("BEST COMBO x" + bestCombo, dWidth / 2f,
                dHeight * 0.45f + textSize * 1.6f, centerPaint);
    }

    // ----------------------------------------------------------------- input

    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            // Generous target - the drawn button is small. The bounds are only
            // known once the HUD has been laid out by the first draw.
            float pad = textSize * 0.6f;
            if (!pauseButton.isEmpty()
                    && touchX >= pauseButton.left - pad && touchX <= pauseButton.right + pad
                    && touchY >= pauseButton.top - pad && touchY <= pauseButton.bottom + pad) {
                paused = !paused;
                return true;
            }
            oldY = touchY;
            oldX = touchX;
            oldCatY = catY;
            oldCatX = catX;
        }

        if (action == MotionEvent.ACTION_MOVE && !paused) {
            float newCatX = oldCatX - (oldX - touchX);
            float newCatY = oldCatY - (oldY - touchY);

            float maxX = dWidth - catWidth;
            float maxY = dHeight - bottomInset - catHeight;
            float minY = topInset;

            catX = Math.max(0f, Math.min(maxX, newCatX));
            catY = Math.max(minY, Math.min(maxY, newCatY));
        }
        return true;
    }

    // ----------------------------------------------------------------- audio

    private void playMusic() {
        if (SplashActivity.soundOn) {
            currentlyPlayingSong = MediaPlayer.create(getContext(), R.raw.game_theme);
            if (currentlyPlayingSong != null) {
                currentlyPlayingSong.setLooping(true);
                currentlyPlayingSong.start();
            }
        }
    }

    public static void stopMusic() {
        if (currentlyPlayingSong != null) {
            currentlyPlayingSong.stop();
            currentlyPlayingSong.release();
            currentlyPlayingSong = null;
        }
    }

    // Applies a damage effect to the device if it is running Android Oreo or higher
    public void damageEffect() {
        Vibrator vibrator = (Vibrator) getContext().getSystemService(VIBRATOR_SERVICE);
        if (vibrator == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(android.os.VibrationEffect.createOneShot(
                    150, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(100);
        }
    }
}

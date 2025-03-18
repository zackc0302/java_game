package com.example.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

class GamePanel extends SurfaceView implements SurfaceHolder.Callback {

    private MainThread thread;
    private Rect r = new Rect();
    private Rect pauseButton; // Pause button region
    private RectPlayer player;
    private Point playerPoint;
    private ObstacleManager obstacleManager;
    private boolean movingPlayer = false;
    private boolean gameOver = false;
    private boolean paused = false; // Pause state
    private long gameOverTime;
    private Bitmap background;
    private MediaPlayer backgroundMusic;

    public GamePanel(Context context) {
        super(context);

        getHolder().addCallback(this);

        thread = new MainThread(getHolder(), this);

        // Initialize the player
        player = new RectPlayer(new Rect(100, 100, 200, 200), Color.rgb(255, 0, 0), context);
        playerPoint = new Point(Constants.SCREEN_WIDTH / 2, 3 * Constants.SCREEN_HEIGHT / 4);
        player.update(playerPoint);

        // Initialize the obstacle manager
        obstacleManager = new ObstacleManager(200, 350, 75, Color.BLACK);

        // Initialize the pause button (top-right corner)
        int buttonSize = 150;
        pauseButton = new Rect(Constants.SCREEN_WIDTH - buttonSize - 30, 30,
                Constants.SCREEN_WIDTH - 30, 30 + buttonSize);

        // Load the background image
        background = BitmapFactory.decodeResource(getResources(), R.drawable.sky);
        background = Bitmap.createScaledBitmap(background, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, false);

        // Load background music
        backgroundMusic = MediaPlayer.create(context, R.raw.background_music);
        backgroundMusic.setLooping(true);
        backgroundMusic.start();

        setFocusable(true);
    }

    public void reset() {
        player = new RectPlayer(new Rect(100, 100, 200, 200), Color.rgb(255, 0, 0), getContext());
        playerPoint = new Point(Constants.SCREEN_WIDTH / 2, 3 * Constants.SCREEN_HEIGHT / 4);
        obstacleManager = new ObstacleManager(200, 350, 75, Color.BLACK);
        movingPlayer = false;
        gameOver = false;
        paused = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread = new MainThread(getHolder(), this);
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        while (retry) {
            try {
                thread.setRunning(false);
                thread.join();
                retry = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (backgroundMusic != null) {
            backgroundMusic.release();
            backgroundMusic = null;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (pauseButton.contains((int) event.getX(), (int) event.getY())) {
                    paused = !paused;
                    if (paused) {
                        obstacleManager.pause();
                        backgroundMusic.pause();
                    } else {
                        obstacleManager.resume();
                        backgroundMusic.start();
                    }
                    return true;
                }
                if (!gameOver && player.getRectangle().contains((int) event.getX(), (int) event.getY())) {
                    movingPlayer = true;
                }
                if (gameOver && System.currentTimeMillis() - gameOverTime >= 2000) {
                    reset();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (movingPlayer) {
                    playerPoint.set((int) event.getX(), (int) event.getY());
                }
                break;

            case MotionEvent.ACTION_UP:
                movingPlayer = false;
                break;
        }
        return true;
    }

    public void update() {
        if (!paused && !gameOver) {
            player.update(playerPoint);
            obstacleManager.update(); // Only update when not paused or game over
            if (obstacleManager.playerCollide(player)) {
                gameOver = true;
                gameOverTime = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawBitmap(background, 0, 0, null);

        // Draw the player and obstacles
        player.draw(canvas);
        obstacleManager.draw(canvas);

        // Draw the pause button
        Paint paint = new Paint();
        paint.setColor(Color.GRAY);
        canvas.drawRect(pauseButton, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(50);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Pause", pauseButton.centerX(), pauseButton.centerY() + 15, paint);

        if (gameOver) {
            paint.setTextSize(100);
            paint.setColor(Color.MAGENTA);
            drawCenterText(canvas, paint, new String[]{"Game Over", "Tap to Restart"});
        } else if (paused) {
            paint.setTextSize(100);
            paint.setColor(Color.BLUE);
            drawCenterText(canvas, paint, new String[]{"Paused", "Tap to Resume"});
        }
    }

    private void drawCenterText(Canvas canvas, Paint paint, String[] lines) {
        canvas.getClipBounds(r);
        int cHeight = r.height();
        int cWidth = r.width();

        paint.setTextAlign(Paint.Align.LEFT);
        int totalTextHeight = 0;

        // Calculate total text height
        for (String line : lines) {
            paint.getTextBounds(line, 0, line.length(), r);
            totalTextHeight += r.height();
        }

        // Set starting y position to center the text block
        float y = (cHeight - totalTextHeight) / 2f;

        for (String line : lines) {
            paint.getTextBounds(line, 0, line.length(), r);
            float x = cWidth / 2f - r.width() / 2f - r.left;
            y += r.height(); // Move to the next line
            canvas.drawText(line, x, y, paint);
        }
    }
}

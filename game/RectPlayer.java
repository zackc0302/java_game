package com.example.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;

public class RectPlayer implements GameObject {

    private Rect rectangle;
    private Bitmap playerImage;

    public Rect getRectangle() {
        return rectangle;
    }

    public RectPlayer(Rect rectangle, int color, Context context) {
        this.rectangle = rectangle;

        // 加載飛機圖片
        playerImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.plane);
        playerImage = Bitmap.createScaledBitmap(playerImage, rectangle.width(), rectangle.height(), false);
    }

    @Override
    public void draw(Canvas canvas) {
        if (playerImage != null) {
            canvas.drawBitmap(playerImage, rectangle.left, rectangle.top, null);
        }
    }

    @Override
    public void update() {}

    public void update(Point point) {
        // 更新玩家位置
        rectangle.set(point.x - rectangle.width() / 2, point.y - rectangle.height() / 2,
                point.x + rectangle.width() / 2, point.y + rectangle.height() / 2);
    }
}

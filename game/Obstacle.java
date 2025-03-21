package com.example.game;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
public class Obstacle implements GameObject {
    private Rect rectangle;
    private Rect rectangle2;
    private int color;

    public Rect getRectangle() {
        return rectangle;
    }

    public void incrementY(float y) {
        rectangle.top += y;
        rectangle.bottom +=y;
        rectangle2.top += y;
        rectangle2.bottom +=y;
    }

    public Obstacle(int rectHeight, int color, int startX, int startY, int playerGap) {

        this.color = color;
        //l,t,r,b
        rectangle = new Rect(0, startY, startX, startY + rectHeight);
        rectangle2 = new Rect(startX + playerGap, startY, Constants.SCREEN_WIDTH, startY + rectHeight);
    }

    public boolean playerCollide(RectPlayer player) {
        return Rect.intersects(rectangle,player.getRectangle()) ||Rect.intersects(rectangle2,player.getRectangle());
    }

    @Override
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawRect(rectangle, paint);
        canvas.drawRect(rectangle2, paint);
    }

    public void update() {

    }
}
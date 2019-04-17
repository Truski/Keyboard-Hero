package com.truskilol.keyboardhero;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.awt.*;

public class FloatingText {
    public String text;
    public float end_time;
    public Point location;
    public static final float DEFAULT_LENGTH = 1;

    public FloatingText(String text, float currentTime, Point location) {
        this.text = text;
        this.location = location;
        this.end_time = currentTime + DEFAULT_LENGTH;
    }

    public FloatingText(String text, float currentTime, float duration, Point location) {
        this.text = text;
        this.end_time = currentTime + duration;
        this.location = location;
    }

    public void draw(SpriteBatch batch, BitmapFont font) {
        font.draw(batch, text, location.x, location.y);
    }
}

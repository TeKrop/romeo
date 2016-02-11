package fr.valentinporchet.romeo;

import java.io.Serializable;

/**
 * Created by Valentin on 25/01/2016.
 */
public class TTData implements Serializable {
    private float x;
    private float y;

    public TTData(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    @Override
    public String toString() {
        return "X=" + x + ";Y=" + y;
    }
}

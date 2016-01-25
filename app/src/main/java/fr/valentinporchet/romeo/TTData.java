package fr.valentinporchet.romeo;

import java.io.Serializable;

/**
 * Created by Valentin on 25/01/2016.
 */
public class TTData implements Serializable {
    public float posX;
    public float posY;

    public TTData(float x, float y) {
        posX = x; posY = y;
    }
}

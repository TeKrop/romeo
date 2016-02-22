package fr.valentinporchet.romeo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Valentin on 25/01/2016.
 */
public class TTData implements Serializable, Iterable<TTData.Position> {
    // little class for position storage
    public class Position implements Serializable {
        public float x; public float y;

        public Position(float x, float y) {
            this.x = x; this.y = y;
        }
    }

    private ArrayList<Position> mPositions;
    private int mColor;

    public TTData() {
        mPositions = new ArrayList<>();
        mColor = 0xFF000000;
    }

    // METHODS FOR ITERABLE
    public Iterator<Position> iterator() {
        Iterator<Position> itPos = mPositions.iterator();
        return itPos;
    }

    // METHODS FOR POSITIONS ARRAYLIST
    public void add(float x, float y) {
        mPositions.add(new Position(x, y));
    }

    public boolean isEmpty() {
        return mPositions.isEmpty();
    }

    public Position get(int index) {
        return mPositions.get(index);
    }

    public int size() {
        return mPositions.size();
    }

    public void clear() {
        mPositions.clear();
    }

    // METHODS FOR COLOR
    public void setColor(int mColor) {
        this.mColor = mColor;
    }

    public int getColor() {
        return mColor;
    }
}

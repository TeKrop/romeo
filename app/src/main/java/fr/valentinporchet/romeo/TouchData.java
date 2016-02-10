package fr.valentinporchet.romeo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Class used to store data about touch events (one path)
 */
public class TouchData implements Serializable {
    // As Path is not Serializable, we use a custom class
    public SerializablePath mPath = new SerializablePath();
    public ArrayList<Float> mTempPathLengths = new ArrayList<>();
    public ArrayList<Long> mTimeForPaths = new ArrayList<>();
    public int mPathColor;
    public float mPathThickness;
    public UUID uuid;

    public TouchData() {
        uuid = UUID.randomUUID();
    }

    @Override
    public String toString() {
        return "TouchData{" +
                "mPath=" + mPath +
                ", mTempPathLengths=" + mTempPathLengths +
                ", mTimeForPaths=" + mTimeForPaths +
                ", mPathColor=" + mPathColor +
                ", mPathThickness=" + mPathThickness +
                ", uuid=" + uuid +
                '}';
    }
}

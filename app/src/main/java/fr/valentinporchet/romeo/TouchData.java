package fr.valentinporchet.romeo;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class used to store data about touch events (one path)
 */
public class TouchData implements Serializable {
    // As Path is not Serializable, we use a custom class
    public SerializablePath mPath = new SerializablePath(); // the path

    public ArrayList<Float> mTempPathLengths = new ArrayList<>(); // array containing path length at each move event
    public ArrayList<Long> mTimeForPaths = new ArrayList<>(); // array containing time elapsed at each move event
    public int mPathColor; // color of the path
    public float mPathThickness; // size of the path

    @Override
    public String toString() {
        return "TouchData{" +
                "mPath=" + mPath +
                ", mTempPathLengths=" + mTempPathLengths +
                ", mTimeForPaths=" + mTimeForPaths +
                ", mPathColor=" + mPathColor +
                ", mPathThickness=" + mPathThickness +
                '}';
    }
}

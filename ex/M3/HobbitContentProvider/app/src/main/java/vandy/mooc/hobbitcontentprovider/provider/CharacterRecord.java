package vandy.mooc.hobbitcontentprovider.provider;

import android.database.Cursor;
import android.support.annotation.NonNull;

/**
 * A simple POJO that stores information about Hobbit characters.
 */
public class CharacterRecord {
    /**
     * Start at 0.
     */
    static long sInitialId = 0;

    /**
     * Id of the character in the map.
     */
    private final long mId;

    /**
     * Name of the character.
     */
    private final String mName;

    /**
     * Race of the character.
     */
    private String mRace;

    /**
     * Constructor initializes all the name and race fields.
     */
    CharacterRecord(String name,
                    String race) {
        mName = name;
        mRace = race;
        mId = ++sInitialId;
    }

    /**
     * Constructor initializes all the fields.
     */
    CharacterRecord(long id,
                    String name,
                    String race) {
        mId = id;
        mName = name;
        mRace = race;
    }

    /**
     * Constructor initializes all fields from a cursor.
     */
    CharacterRecord(@NonNull Cursor cursor) {
        mId = cursor.getInt(cursor.getColumnIndex
                (CharacterContract.CharacterEntry._ID));
        mName = cursor.getString(cursor.getColumnIndex
                (CharacterContract.CharacterEntry.COLUMN_NAME));
        mRace = cursor.getString(cursor.getColumnIndex
                (CharacterContract.CharacterEntry.COLUMN_RACE));
    }

    /**
     * Static builder method returns a new character record from a given cursor.
     */
    public static CharacterRecord fromCursor(Cursor cursor) {
        return new CharacterRecord(cursor);
    }

    /**
     * @return the id of the character.
     */
    public long getId() {
        return mId;
    }

    /**
     * @return the name of the character.
     */
    public String getName() {
        return mName;
    }

    /**
     * @return the race of the character.
     */
    public String getRace() {
        return mRace;
    }

    /**
     * Set the race of the character.
     */
    public void setRace(String race) {
        mRace = race;
    }
}

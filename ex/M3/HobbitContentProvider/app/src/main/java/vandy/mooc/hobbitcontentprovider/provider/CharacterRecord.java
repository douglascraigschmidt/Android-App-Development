package vandy.mooc.hobbitcontentprovider.provider;

/**
 * A simple POJO that stores information about Hobbit characters.
 */
class CharacterRecord {
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
     * @return the id of the character.
     */
    long getId() {
        return mId;
    }

    /**
     * @return the name of the character.
     */
    String getName() {
        return mName;
    }

    /**
     * @return the race of the character.
     */
    String getRace() {
        return mRace;
    }

    /**
     * Set the race of the character.
     */
    void setRace(String race) {
        mRace = race;
    }
}

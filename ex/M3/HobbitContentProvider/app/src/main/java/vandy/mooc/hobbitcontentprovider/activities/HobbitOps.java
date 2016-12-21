package vandy.mooc.hobbitcontentprovider.activities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.widget.Toast;

import vandy.mooc.hobbitcontentprovider.provider.CharacterContract;

/**
 * Helper class that consolidates and simplifies operations on the
 * HobbitProvider.
 */
public class HobbitOps {
    /**
     * Reference back to the HobbitActivity.
     */
    private final HobbitActivity mHobbitActivity;

    /**
     * Contains the most recent result from a query so the display can
     * be updated after a runtime configuration change.
     */
    private Cursor mCursor;

    /**
     * Define the Proxy for accessing the HobbitProvider.
     */
    private ContentResolver mContentResolver;

    /**
     * All the races in our program.
     */
    private static String[] sAllRaces = new String[] {
        "Dwarf",
        "Maia",
        "Hobbit",
        "Dragon",
        "Human",
        "Bear"
    };

    /**
     * Constructor initializes the fields.
     */
    public HobbitOps(HobbitActivity hobbitActivity) {
        mHobbitActivity = hobbitActivity;
        mContentResolver = mHobbitActivity.getContentResolver();
    }

    /**
     * Insert a Hobbit @a character of a particular @a race into
     * the HobbitProvider.
     */
    public Uri insert(String character,
                      String race) throws RemoteException {
        final ContentValues cvs = new ContentValues();

        // Insert data.
        cvs.put(CharacterContract.CharacterEntry.COLUMN_NAME,
                character);
        cvs.put(CharacterContract.CharacterEntry.COLUMN_RACE,
                race);

        // Insert the content at the designated URI.
        return insert(CharacterContract.CharacterEntry.CONTENT_URI,
                      cvs);
    }

    /**
     * Insert @a ContentValues into the HobbitProvider at
     * the @a uri.
     */
    protected Uri insert(Uri uri, ContentValues cvs) {
        return mContentResolver.insert(uri, cvs);
    }

    /**
     * Insert an array of Hobbit @a characters of a particular @a
     * race into the HobbitProvider.
     */
    public int bulkInsert(String[] characters,
                          String race) throws RemoteException {
        // Use ContentValues to store the values in appropriate
        // columns, so that ContentResolver can process it.  Since
        // more than one rows needs to be inserted, an Array of
        // ContentValues is needed.
        ContentValues[] cvsArray =
            new ContentValues[characters.length];

        // Index counter.
        int i = 0;

        // Insert all the characters into the ContentValues array.
        for (String character : characters) {
            ContentValues cvs = new ContentValues();
            cvs.put(CharacterContract.CharacterEntry.COLUMN_NAME,
                    character);
            cvs.put(CharacterContract.CharacterEntry.COLUMN_RACE,
                    race);
            cvsArray[i++] = cvs;
        }

        // Insert the array of content at the designated URI.
        return bulkInsert
            (CharacterContract.CharacterEntry.CONTENT_URI,
             cvsArray);
    }

    /**
     * Insert an array of @a ContentValues into the
     * HobbitProvider at the @a uri.
     */
    protected int bulkInsert(Uri uri,
                             ContentValues[] cvsArray) {
        return mContentResolver.bulkInsert(uri,
                              cvsArray);
    }

    /**
     * Return a Cursor from a query on the HobbitProvider at
     * the @a uri.
     */
    public Cursor query(Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectionArgs,
                        String sortOrder) {
        return mContentResolver.query(uri,
                         projection,
                         selection,
                         selectionArgs,
                         sortOrder);

    }

    /**
     * Update the @a name and @a race of a Hobbit character at a
     * designated @a uri from the HobbitProvider.
     */
    public int updateByUri(Uri uri,
                           String name,
                           String race) throws RemoteException {
        // Initialize the content values.
        final ContentValues cvs = new ContentValues();
        cvs.put(CharacterContract.CharacterEntry.COLUMN_NAME,
                name);
        cvs.put(CharacterContract.CharacterEntry.COLUMN_RACE,
                race);

        // Update the content at the designated URI.
        return update(uri,
                      cvs,
                      null,
                      null);
    }

    /**
     * Update the @a race of a Hobbit character with a given
     * @a name in the HobbitProvider.
     */
    public int updateRaceByName(String name,
                                String race) throws RemoteException {
        // Initialize the content values.
        final ContentValues cvs = new ContentValues();
        cvs.put(CharacterContract.CharacterEntry.COLUMN_NAME,
                name);
        cvs.put(CharacterContract.CharacterEntry.COLUMN_RACE,
                race);

        // Update the content at the designated URI.
        return update(CharacterContract.CharacterEntry.CONTENT_URI,
                      cvs,
                      CharacterContract.CharacterEntry.COLUMN_NAME,
                      new String[] { name });
    }

    /**
     * Update the @a selection and @a selectionArgs with the @a
     * ContentValues in the HobbitProvider at the @a uri.
     */
    public int update(Uri uri,
                      ContentValues cvs,
                      String selection,
                      String[] selectionArgs) {
        return mContentResolver.update(uri,
                          cvs,
                          selection,
                          selectionArgs);
    }

    /**
     * Delete an array of Hobbit @a characterNames from the
     * HobbitProvider.
     */
    public int deleteByName(String[] characterNames)
        throws RemoteException {
        return delete(CharacterContract.CharacterEntry.CONTENT_URI,
                      CharacterContract.CharacterEntry.COLUMN_NAME,
                      characterNames);
    }

    /**
     * Delete an array of Hobbit @a characterRaces from the
     * HobbitProvider.
     */
    public int deleteByRace(String[] characterRaces)
        throws RemoteException {
        return delete(CharacterContract.CharacterEntry.CONTENT_URI,
                      CharacterContract.CharacterEntry.COLUMN_RACE,
                      characterRaces);
    }

    /**
     * Delete the @a selection and @a selectionArgs from the
     * HobbitProvider at the @a uri.
     */
    protected int delete(Uri uri,
                         String selection,
                         String[] selectionArgs) {
        return mContentResolver.delete
            (uri,
             selection,
             selectionArgs);
    }

    /**
     * Delete all characters from the HobbitProvider.
     */
    public int deleteAll()
        throws RemoteException {
        return delete(CharacterContract.CharacterEntry.CONTENT_URI,
                      null,
                      null);
    }

    /**
     * Display the current contents of the HobbitProvider.
     */
    public void displayAll()
        throws RemoteException {
        // Query for all characters in the HobbitProvider by their race.
        mCursor = query(CharacterContract.CharacterEntry.CONTENT_URI,
                        CharacterContract.CharacterEntry.sColumnsToDisplay,
                        CharacterContract.CharacterEntry.COLUMN_RACE,
                        sAllRaces,
                        null);

        // Inform the user if there's nothing to display.
        if (mCursor.getCount() == 0) {
            Toast.makeText(mHobbitActivity,
                           "No items to display",
                           Toast.LENGTH_SHORT).show();
            // Remove the display if there's nothing left to show.
            mHobbitActivity.displayCursor(mCursor = null);
        } else
            // Display the results of the query.
            mHobbitActivity.displayCursor(mCursor);
    }
}

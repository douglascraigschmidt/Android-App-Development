package vandy.mooc.hobbitcontentprovider.activities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import vandy.mooc.hobbitcontentprovider.R;
import vandy.mooc.hobbitcontentprovider.provider.CharacterContract;

/**
 * This activity shows how to use the HobbitContentProvider to perform
 * various "CRUD" (i.e., insert, query, update, and delete) operations
 * using characters from Tolkien's classic book "The Hobbit."
 */
public class HobbitActivity 
       extends LifecycleLoggingActivity {
    /**
     * ListView displays the Hobbit character information.
     */
    private ListView mListView;

    /**
     * Uri for the "Necromancer".
     */
    private Uri mNecromancerUri;
    
    /**
     * Used to display the results of contacts queried from the
     * HobbitContentProvider.
     */
    private SimpleCursorAdapter mAdapter;

    /**
     * An instance of an inner class that consolidates and simplifies
     * operations on the HobbitContentProvider.
     */
    private HobbitOps mHobbitOps;

    /**
     * Hook method called when a new activity is created.  One time
     * initialization code goes here, e.g., initializing views.
     *
     * @param savedInstanceState
     *            object that contains saved state information.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the content view for this Activity.
        setContentView(R.layout.hobbit_activity);

        // Initialize the List View.
        mListView = (ListView) findViewById(R.id.list);

        // Initialize the SimpleCursorAdapter.
        mAdapter = makeCursorAdapter();

        // Connect the ListView with the SimpleCursorAdapter.
        mListView.setAdapter(mAdapter);

        // Create a new HobbitOps instance.
        mHobbitOps = new HobbitOps();
    }

    /**
     * This method is run when the user clicks the "Add All" button.
     * It insert various characters from the Hobbit book into the
     * database managed by the HobbitContentProvider.
     */
    public void addAll(View v) {
        try {
            // Insert the main protagonist.
            mHobbitOps.insert("Bilbo",
                              "Hobbit");

            // Insert the main wizard.
            mHobbitOps.insert("Gandalf",
                              "Maia");

            // Insert all the dwarves.
            mHobbitOps.bulkInsert(new String[] { 
                    "Thorin", "Kili", "Fili",
                    "Balin", "Dwalin", "Oin", "Gloin",
                    "Dori", "Nori", "Ori",
                    "Bifur", "Bofur", "Bombur"
                },
                "Dwarf");

            // Insert the main antagonist.
            mHobbitOps.insert("Smaug",
                              "Dragon");

            // Insert Beorn.
            mHobbitOps.insert("Beorn",
                              "Man");

            // Insert the Master of Laketown
            mHobbitOps.insert("Master",
                              "Man");

            // Insert another antagonist.
            mNecromancerUri = 
                mHobbitOps.insert("Necromancer",
                                  "Maia");

            // Display the results;
            mHobbitOps.displayAll();
        } catch (RemoteException e) {
            Log.d(TAG, 
                  "exception " 
                  + e);
        }
    }

    /**
     * This method is run when the user clicks the "Modify All" button
     * to modify certain Hobbit characters from the database managed
     * by the HobbitContentProvider.
     */
    public void modifyAll(View v) {
        try {
            // Update Beorn's race since he's a skinchanger.
            mHobbitOps.updateRaceByName("Beorn",
                                        "Bear");

            if (mNecromancerUri != null)
                // The Necromancer is really Sauron the Deceiver.
                mHobbitOps.updateByUri(mNecromancerUri,
                                       "Sauron",
                                       "Maia");

            // Delete dwarves who get killed in the Battle of Five
            // Armies.
            mHobbitOps.deleteByName(new String[] { 
                    "Thorin",
                    "Kili",
                    "Fili" 
                });

            // Delete Smaug since he gets killed by Bard the Bowman
            // and the "Master" (who's a man) since he's killed later
            // in the book.
            mHobbitOps.deleteByRace(new String[] { 
                    "Dragon",
                    "Man" 
                });

            // Display the results;
            mHobbitOps.displayAll();
        } catch (RemoteException e) {
            Log.d(TAG, 
                  "exception " 
                  + e);
        }
    }

    /**
     * This method is run when the user clicks the "Delete All" button
     * to remove all Hobbit characters from the database managed by
     * the HobbitContentProvider.
     */
    public void deleteAll(View v) {
        try {
            // Clear out the database.
            int numDeleted = mHobbitOps.deleteAll();

            // Inform the user how many characters were deleted.
            Toast.makeText(this,
                           "Deleted "
                           + numDeleted
                           + " Hobbit characters",
                           Toast.LENGTH_SHORT).show();

            // Display the results;
            mHobbitOps.displayAll();
        } catch (RemoteException e) {
            Log.d(TAG, 
                  "exception " 
                  + e);
        }
    }

    /**
     * This method is run when the user clicks the "Display All"
     * button to display all races of Hobbit characters from the
     * database managed by the HobbitContentProvider.
     */
    public void displayAll(View v) {
        try {
            // Display the results.
            mHobbitOps.displayAll();
        } catch (RemoteException e) {
            Log.d(TAG, 
                  "exception " 
                  + e);
        }
    }

    /**
     * Display the contents of the cursor as a ListView.
     */
    public void displayCursor(Cursor cursor) {
    	// Display the designated columns in the cursor as a List in
        // the ListView connected to the SimpleCursorAdapter.
        mAdapter.changeCursor(cursor);
    }

    /**
     * Return a @a SimpleCursorAdapter that can be used to display the
     * contents of the Hobbit ContentProvider.
     */
    public SimpleCursorAdapter makeCursorAdapter() {
        return new SimpleCursorAdapter
            (this,
             R.layout.list_layout, 
             null,
             CharacterContract.CharacterEntry.sColumnsToDisplay,
             CharacterContract.CharacterEntry.sColumnResIds,
             1);
    }

    /**
     * Inner class that consolidates and simplifies operations on the
     * HobbitContentProvider.
     */
    public class HobbitOps {
        /**
         * Contains the most recent result from a query so the display
         * can be updated after a runtime configuration change.
         */
        private Cursor mCursor;

        /**
         * Define the Proxy for accessing the HobbitContentProvider.
         */
        private ContentResolver mCr;

        /**
         * Constructor initializes the fields.
         */
        public HobbitOps() {
            mCr = HobbitActivity.this.getContentResolver();
        }

        /**
         * Insert a Hobbit @a character of a particular @a race into
         * the HobbitContentProvider.
         */
        public Uri insert(String character,
                          String race) throws RemoteException {
            final ContentValues cvs = new ContentValues();

            // Insert data.
            cvs.put(CharacterContract.CharacterEntry.COLUMN_NAME,
                    character);
            cvs.put(CharacterContract.CharacterEntry.COLUMN_RACE,
                    race);

            // Call to the hook method.
            return insert(CharacterContract.CharacterEntry.CONTENT_URI,
                          cvs);
        }

        /**
         * Insert @a ContentValues into the HobbitContentProvider at
         * the @a uri.
         */
        protected Uri insert(Uri uri,
                             ContentValues cvs)
            throws RemoteException {
            return mCr.insert(uri,
                              cvs);
        }

        /**
         * Insert an array of Hobbit @a characters of a particular @a
         * race into the HobbitContentProvider.
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

            return bulkInsert
                (CharacterContract.CharacterEntry.CONTENT_URI,
                 cvsArray);
        }
    
        /**
         * Insert an array of @a ContentValues into the
         * HobbitContentProvider at the @a uri.
         */
        protected int bulkInsert(Uri uri,
                                 ContentValues[] cvsArray)
            throws RemoteException {
            return mCr.bulkInsert(uri,
                                  cvsArray);
        }

        /**
         * Return a Cursor from a query on the HobbitContentProvider at
         * the @a uri.
         */
        public Cursor query(Uri uri,
                            String[] projection,
                            String selection,
                            String[] selectionArgs,
                            String sortOrder)
            throws RemoteException {
            return mCr.query(uri,
                             projection,
                             selection,
                             selectionArgs,
                             sortOrder);

        }

        /**
         * Update the @a name and @a race of a Hobbit character at a
         * designated @a uri from the HobbitContentProvider.
         */
        public int updateByUri(Uri uri,
                               String name,
                               String race) throws RemoteException {
            final ContentValues cvs = new ContentValues();
            cvs.put(CharacterContract.CharacterEntry.COLUMN_NAME,
                    name);
            cvs.put(CharacterContract.CharacterEntry.COLUMN_RACE,
                    race);
            return update(uri,
                          cvs,
                          null,
                          null);
        }

        /**
         * Update the @a race of a Hobbit character with a given
         * @a name in the HobbitContentProvider.
         */
        public int updateRaceByName(String name,
                                    String race) throws RemoteException {
            final ContentValues cvs = new ContentValues();
            cvs.put(CharacterContract.CharacterEntry.COLUMN_NAME,
                    name);
            cvs.put(CharacterContract.CharacterEntry.COLUMN_RACE,
                    race);
            return update(CharacterContract.CharacterEntry.CONTENT_URI,
                          cvs,
                          CharacterContract.CharacterEntry.COLUMN_NAME,
                          new String[] { name });
        }

        /**
         * Delete the @a selection and @a selectionArgs with the @a
         * ContentValues in the HobbitContentProvider at the @a uri.
         */
        public int update(Uri uri,
                          ContentValues cvs,
                          String selection,
                          String[] selectionArgs)
            throws RemoteException {
            return mCr.update(uri,
                              cvs,
                              selection,
                              selectionArgs);
        }

        /**
         * Delete an array of Hobbit @a characterNames from the
         * HobbitContentProvider.
         */
        public int deleteByName(String[] characterNames)
            throws RemoteException {
            return delete(CharacterContract.CharacterEntry.CONTENT_URI,
                          CharacterContract.CharacterEntry.COLUMN_NAME,
                          characterNames);
        }

        /**
         * Delete an array of Hobbit @a characterRaces from the
         * HobbitContentProvider.
         */
        public int deleteByRace(String[] characterRaces)
            throws RemoteException {
            return delete(CharacterContract.CharacterEntry.CONTENT_URI,
                          CharacterContract.CharacterEntry.COLUMN_RACE,
                          characterRaces);
        }

        /**
         * Delete the @a selection and @a selectionArgs from the
         * HobbitContentProvider at the @a uri.
         */
        protected int delete(Uri uri,
                             String selection,
                             String[] selectionArgs)
            throws RemoteException {
            return mCr.delete
                (uri,
                 selection,
                 selectionArgs);
        }
         
        /**
         * Delete all characters from the HobbitContentProvider.
         * Plays the role of a "template method" in the Template
         * Method pattern.
         */
        public int deleteAll() 
            throws RemoteException {
            return delete(CharacterContract.CharacterEntry.CONTENT_URI,
                          null,
                          null);
        }

        /**
         * Display the current contents of the HobbitContentProvider.
         */
        public void displayAll()
            throws RemoteException {
            // Query for all the characters in the HobbitContentProvider.
            mCursor = query(CharacterContract.CharacterEntry.CONTENT_URI,
                            CharacterContract.CharacterEntry.sColumnsToDisplay,
                            CharacterContract.CharacterEntry.COLUMN_RACE,
                            new String[] { 
                                "Dwarf",
                                "Maia",
                                "Hobbit",
                                "Dragon",
                                "Man",
                                "Bear"
                            },
                            null);
            if (mCursor.getCount() == 0) {
                Toast.makeText(HobbitActivity.this,
                               "No items to display",
                               Toast.LENGTH_SHORT).show();
                // Remove the display if there's nothing left to show.
                displayCursor(mCursor = null);
            } else
                // Display the results of the query.
                displayCursor(mCursor);
        }
    }
}

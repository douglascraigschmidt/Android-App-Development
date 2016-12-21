package vandy.mooc.hobbitcontentprovider.activities;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import vandy.mooc.hobbitcontentprovider.R;

/**
 * This activity interacts with the user to coordinate performing various "CRUD"
 * (i.e., insert, query, update, and delete) operations on the HobbitProvider
 * using characters from Tolkien's classic novel "The Hobbit."
 */
public class HobbitActivity
       extends LifecycleLoggingActivity
       implements RaceFragment.OnClassClickCallback {
    /**
     * Uri for the "Necromancer" to store its value for update operations.
     */
    private Uri mNecromancerUri;

    /**
     * An instance of a helper class that consolidates and simplifies operations
     * on the HobbitProvider.
     */
    private HobbitOps mHobbitOps;

    /**
     * Hook method called when a new activity is created.  One time
     * initialization code goes here, e.g., initializing views.
     *
     * @param savedInstanceState object that contains saved state information.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the content view for this Activity.
        setContentView(R.layout.activity_main);

        // Setup support toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create a new HobbitOps instance.
        mHobbitOps = new HobbitOps(this);

        // If we are creating this activity for the first time
        // (savedInstanceState == null) or if we are recreating this
        // activity after a configuration change (savedInstanceState
        // != null), we always want to display the current contents of
        // the SQLite database.
        try {
            mHobbitOps.displayAll();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is run when the user clicks the "Add All" button. It insert
     * various characters from the Hobbit book into the database managed by the
     * HobbitContentProvider.
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
            mHobbitOps.bulkInsert(new String[]{ "Thorin", "Kili", "Fili",
                                                "Balin", "Dwalin", "Oin", "Gloin",
                                                "Dori", "Nori", "Ori",
                                                "Bifur", "Bofur", "Bombur"
                                  },
                                  "Dwarf");

            // Insert the main antagonist.
            mHobbitOps.insert("Smaug",
                              "Dragon");

            // Insert various humans, including Bilbo's mother.
            mHobbitOps.bulkInsert(new String[] {"Beorn",
                                                "Master",
                                                "Belladonna"
                                  },
                                  "Human");

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
     * This method is run when the user clicks the "Modify All" button to modify
     * certain Hobbit characters from the database managed by the
     * HobbitContentProvider.
     */
    public void modifyAll(View v) {
        try {
            // Update Beorn's race since he's a skinchanger.
            mHobbitOps.updateRaceByName("Beorn",
                                        "Bear");

            if (mNecromancerUri != null)
                // The Necromancer is really Sauron the Deceiver.
                {
                    mHobbitOps.updateByUri(mNecromancerUri,
                                           "Sauron",
                                           "Maia");
                }

            // Delete dwarves who get killed in the Battle of Five
            // Armies.
            mHobbitOps.deleteByName(new String[]{
                    "Thorin",
                    "Kili",
                    "Fili"
                });

            // Delete Smaug since he gets killed by Bard the Bowman
            // and the humans (e.g., "Master" since he's killed later
            // in the book and "Belladonna" since she dies at some
            // point of old age).
            mHobbitOps.deleteByRace(new String[]{
                    "Dragon",
                    "Human"
                });

            // Display the results;
            mHobbitOps.displayAll();

            // Update the backdrop to original image.
            updateImage(R.drawable.bagend);
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

            // Update backdrop to display original image.
            updateImage(R.drawable.bagend);
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
        RaceFragment raceFragment =
            (RaceFragment) getFragmentManager().findFragmentById(R.id.race_fragment);
        if (raceFragment != null) 
            raceFragment.setData(cursor);
    }

    /**
     * Callback used to set the currently displayed image backdrop.
     */
    @Override
    public void updateImage(int imageId) {
        ImageView imageView = (ImageView) findViewById(R.id.backdrop);

        if (imageView != null) {
            imageView.setImageResource(imageId);
            // Fade in the image.
            imageView.startAnimation(AnimationUtils.loadAnimation(this,
                                                                  R.anim.fade_in));
        }
    }
}

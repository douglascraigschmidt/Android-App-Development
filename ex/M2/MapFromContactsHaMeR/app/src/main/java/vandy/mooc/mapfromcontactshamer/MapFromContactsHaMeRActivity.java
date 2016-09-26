package vandy.mooc.mapfromcontactshamer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Animatable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.Explode;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * An Activity that maps a location from the address of a contact
 * using the Android HaMeR concurrency framework.
 */
public class MapFromContactsHaMeRActivity 
       extends LifecycleLoggingActivity {
    /**
     * Debugging tag used by the Android logger.
     */
    private String TAG = getClass().getSimpleName();

    /**
     * A "code" that identifies the request.
     */
    private static final int PICK_CONTACT_REQUEST = 0;

    /**
     * Holds reference to the floating action button for animations.
     */
    private ImageButton mAddButton;

    /**
     * Implements the details of starting a mapper Activity from
     * contact data.
     */
    private ContactAddressMapper mContactAddressMapper;

    /**
     * Hook method called when a new instance of Activity is created.
     * One time initialization code goes here, e.g., UI layout.
     *
     * @param savedInstanceState that contains saved state information.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Always call super class for necessary
        // initialization/implementation.
        super.onCreate(savedInstanceState);

        // Set the default layout.
        setContentView(R.layout.main);

        // Create a reference to the add FAB
        mAddButton = (ImageButton) findViewById(R.id.addButton);

        // Create a ContactMapper to start the appropriate mapper for
        // the contact data.
        mContactAddressMapper = new ContactAddressMapper(this);
    }

    /**
     * Called by the Android Activity framework when the user clicks
     * the "Floating Action Button" on the screen (specified by the
     * android:onClick="findAddress" element in the main.xml layout file).
     *
     * @param v The view.
     */
    public void findAddress(View v) {
        // Animation that morphs the design of the floating action button.
        animateAddFab(/* reverse = */ false);

        // Start the ContactsContentProvider Activity to get a Uri
        // for a selected contact.
        mContactAddressMapper.startContactPicker(PICK_CONTACT_REQUEST);
    }

    /**
     * Hook method called back by the Android Activity framework when
     * an Activity that's been launched exits, giving the requestCode
     * it was started with, the resultCode it returned, and any
     * additional data from it.
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who
     *                    this result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     */
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    final Intent data) {
        // Check if the started Activity completed successfully and
        // the request code is what we're expecting.
        if (resultCode == Activity.RESULT_OK
            && requestCode == PICK_CONTACT_REQUEST)
            // Display a map with the contact data.
            displayMap(data);

        // Animate FAB back to + for the next add operation.
        animateAddFab(/* reverse = */ true);
    }

    /**
     * Perform a short animation from a + to a check mark or the reverse.
     *
     * @param reverse {@code true} to animate back to a + from a check mark.
     */
    private void animateAddFab(boolean reverse) {
        // Use passed boolean to determine the morph animation from + to check
        // mark or the reverse).
        mAddButton.setImageResource(
                reverse ? R.drawable.icon_morph_reverse : R.drawable.icon_morph);

        // Run the FAB icon animation (the drawable is defined as an
        // xml animation).
        ((Animatable) mAddButton.getDrawable()).start();
    }

    /**
     * Method that displays the map after gaining READ_CONTACTS
     * permission from the user.
     *
     * @param data Intent that holds the data of the contact
     */
    private void displayMap(final Intent data) {
        // Use the Android HaMeR framework to create a Runnable so the
        // (potentially) long-duration getAddressFromContact() method
        // can run without blocking the UI Thread.
        final Runnable getAndDisplayAddressFromContact = () -> {
            // Extract address from contact record indicated
            // by the Uri associated with the Intent data.
            String address =
                mContactAddressMapper.getAddressFromContact(data.getData());

                // Launch the mapper Activity in the UI thread.
                runOnUiThread(() -> {
                    if (!TextUtils.isEmpty(address.trim())) 
                        mContactAddressMapper.startMapperActivity(address);
                    else 
                        Toast.makeText(this,
                                       "No address found.",
                                       Toast.LENGTH_SHORT).show();
                    });
        };

        // Create and start a new thread to get the address from the
        // contact and launch an activity to map the address.
        new Thread(getAndDisplayAddressFromContact).start();
    }
}

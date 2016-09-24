package vandy.mooc.mapfromcontactshamer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Animatable;
import android.os.Build;
import android.os.Bundle;
import android.transition.Slide;
import android.view.View;
import android.widget.ImageButton;

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
    private static ImageButton mAddButton;

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

        // Sets up the slide animation upon the exit of the main
        // activity.
        setupWindowAnimations();
    }

    /**
     * Called by the Android Activity framework when the user clicks
     * the "Floating Action Button" on the screen.
     */
    public void findAddress(View v) {
        // Animation that morphs the design of the floating action
        // button.
        mAddButton.setImageResource(R.drawable.icon_morph);
        Animatable mAnimatable =
            (Animatable) (mAddButton).getDrawable();
        mAnimatable.start();

        // Start the ContactsContentProvider Activity to get a Uri
        // for a selected contact.
        mContactAddressMapper.startContactPicker(PICK_CONTACT_REQUEST);
    }

    /**
     * Hook method called back by the Android Activity framework when
     * an Activity that's been launched exits, giving the requestCode
     * it was started with, the resultCode it returned, and any
     * additional data from it.
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
            runOnUiThread(() -> 
                          mContactAddressMapper.startMapperActivity(address));
        };

        // Create a new Thread to get the address from the contact and
        // launch the appropriate Activity to display the address.
        new Thread(getAndDisplayAddressFromContact).start();

        // If you don't want to use a separate Thread just say:
        // getAndDisplayAddressFromContact.run();
    }

    /**
     * Sets up the slide animation upon the exit of main Activity.
     */
    private void setupWindowAnimations() {
        Slide slide = new Slide();
        slide.setDuration(1000);
        getWindow().setExitTransition(slide);
    }
}

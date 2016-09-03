package vandy.mooc.maplocationfromcontacts;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * An Activity that maps a location from the address of a contact.
 */
public class MapLocationFromContactsActivity 
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
     *  Request code for READ_CONTACTS.
     */
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    /**
     * Holds reference to the floating action button for animations.
     */
    private static ImageButton mAddButton;

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

        // Sets up the slide animation upon the exit of the main
        // activity.
        setupWindowAnimations();
    }

    /**
     * Called by the Android Activity framework when the user clicks
     * the "Find Address" button.
     */
    public void findAddress(View v) {
        try {
            // Animation that morphs the design of the floating action
            // button.
            mAddButton.setImageResource(R.drawable.icon_morph);

            Animatable mAnimatable =
                (Animatable) (mAddButton).getDrawable();
            mAnimatable.start();

            // Create a new Intent that matches with the Contacts
            // ContentProvider.
            Intent intent = new Intent(Intent.ACTION_PICK,
                                       ContactsContract.Contacts.CONTENT_URI);

            // Pass on a bundle to achieve the screen transitions used when the
            // activity changes.
            Bundle bundle = 
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle();

            // Start the Contacts ContentProvider Activity, which will
            // prompt the user to pick a contact and then return the
            // Uri for the selected contact via the onActivityResult()
            // hook method.
            startActivityForResult(intent,
                                   PICK_CONTACT_REQUEST,
                                   bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            && requestCode == PICK_CONTACT_REQUEST) {

            // Checks whether the build SDK version is greater than
            // that of Android M; if it is then ask for permission to
            // read contacts as per the changes implemented in permission
            // requests for Android M and above.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M 
                && checkSelfPermission(Manifest.permission.READ_CONTACTS) 
                != PackageManager.PERMISSION_GRANTED) 
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                                   PERMISSIONS_REQUEST_READ_CONTACTS);
            else 
                displayMap(data);
        }
    }

    /**
     * Method that displays the map after gaining READ_CONTACTS
     * permission from the user.
     *
     * @param data Intent that holds the data of the contact
     */
    private void displayMap(final Intent data) {
        // Create a Runnable so the (potentially) long-duration
        // getAddressFromContact() method can run without blocking the
        // UI Thread.
        final Runnable getAndDisplayAddressFromContact =
            new Runnable() {
                @Override
                public void run() {
                    // Extract the address from the contact record
                    // indicated by the Uri associated with the
                    // Intent.
                    final String address =
                            getAddressFromContact(data.getData());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Launch the activity by sending an
                            // intent.  Android will choose the right
                            // one or let the user choose if more than
                            // one Activity can handle it.

                            // Create an Intent that will launch the
                            // "Maps" app.
                            final Intent geoIntent =
                                    makeMapsIntent(address);

                            // Check to see if there's a Map app to
                            // handle the "geo" intent.
                            if (geoIntent.resolveActivity
                                (getPackageManager()) != null)
                                startActivity(geoIntent);
                            else
                                // Start the Browser app instead.
                                startActivity(makeBrowserIntent(address));
                        }
                    });
                }
            };

        // Create a new Thread to get the address from the contact and
        // launch the appropriate Activity to display the address.
        new Thread(getAndDisplayAddressFromContact).start();

        // if you don't want to use a separate Thread just say:
        // getAndDisplayAddressFromContact.run();
    }

    /**
     * Extracts a street address from the Uri of the contact in the
     * Contacts Content Provider.
     */
    private String getAddressFromContact(Uri contactUri) {
        // Obtain a reference to our Content Resolver.
        ContentResolver cr = getContentResolver();

        // Obtain a cursor to the appropriate contact at the
        // designated Uri.
        Cursor cursor =
            cr.query(contactUri,
                     null, null, null, null);

        // Start the cursor at the beginning.
        cursor.moveToFirst();

        // Obtain the id of the contact.
        String id = cursor.getString
            (cursor.getColumnIndex(ContactsContract.Contacts._ID));

        // Create an SQL "where" clause that will search for the
        // street address of the designated contact Id.
        String where = ContactsContract.Data.CONTACT_ID 
            + " = ? AND "
            + ContactsContract.Data.MIMETYPE 
            + " = ?";
        String[] whereParameters = new String[] {
            id,
            ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE 
        };

        // Create a cursor that contains the results of a query for
        // the street address of the designated contact Id.
        try (Cursor addrCursor = cr.query(ContactsContract.Data.CONTENT_URI,
                                          null,
                                          where,
                                          whereParameters,
                                          null)) {
            // Start the cursor at the beginning.
            addrCursor.moveToFirst();

            // Extract the street name.
            String street = addrCursor
                .getString(addrCursor
                           .getColumnIndex(ContactsContract.
                                           CommonDataKinds.
                                           StructuredPostal.
                                           STREET));

            // Extract the city name.
            String city = addrCursor
                .getString(addrCursor
                           .getColumnIndex(ContactsContract.
                                           CommonDataKinds.
                                           StructuredPostal.
                                           CITY));

            // Extract the state.
            String state = addrCursor
                .getString(addrCursor
                           .getColumnIndex(ContactsContract.
                                           CommonDataKinds.
                                           StructuredPostal.
                                           REGION));

            // Extract the zip code.
            String postalCode = addrCursor
                .getString(addrCursor
                           .getColumnIndex(ContactsContract.
                                           CommonDataKinds.
                                           StructuredPostal.
                                           POSTCODE));

            // Create an address from the various pieces obtained
            // above.
            String address =
                street
                + "+"
                + city
                + "+"
                + state
                + "+"
                + postalCode;

            // Return the address.
            return address;
        }
    }

    /**
     * Factory method that returns an Intent that designates the "Map"
     * app.
     */
    private Intent makeMapsIntent(String address) {
        // Note the "loose coupling" between the Intent and the app(s)
        // that handle this Intent.
        return new Intent(Intent.ACTION_VIEW,
                          Uri.parse("geo:0,0?q="
                                    + address));
    }

    /**
     * Factory method that returns an Intent that designates the
     * "Browser" app.
     */
    private Intent makeBrowserIntent(String address) {
        // Note the "loose coupling" between the Intent and the app(s)
        // that handle this Intent.
        return new Intent(Intent.ACTION_VIEW,
                          Uri.parse("http://maps.google.com/?q="
                                    + address));
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

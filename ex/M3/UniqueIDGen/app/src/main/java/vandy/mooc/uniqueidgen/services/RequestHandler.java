package vandy.mooc.uniqueidgen.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vandy.mooc.uniqueidgen.views.GeneratorView;

import static java.lang.Thread.sleep;

/**
 * This class generates unique IDs via a pool of threads and sends
 * them back to UniqueIDGenActivity via a reply messenger contained
 * in the request message.
 */
class RequestHandler
      extends Handler {
    /**
     * Used for debugging.
     */
    private final String TAG = getClass().getName();

    /**
     * The ExecutorService implementation that references a fixed-size
     * thread pool.
     */
    private ExecutorService mExecutor;

    /**
     * A reference to the default shared preferences in which we store
     * a collection of unique IDs implemented internally using a
     * persistent Java HashMap.
     */
    private SharedPreferences mSharedPrefs;

    /**
     * An artificial sleep delay to simulate a busy thread.
     */
    private static final int MAX_DELAY = 2000;

    /**
     * Initialize RequestHandler to generate IDs concurrently.
     */
    public RequestHandler(Context context) {
        // Get a SharedPreferences instance that points to the default
        // file used by the preference framework in this Service.
        mSharedPrefs = 
            PreferenceManager.getDefaultSharedPreferences(context);

        // Create a FixedThreadPool Executor that's configured to use
        // MAX_THREADS.
        mExecutor =
            Executors.newFixedThreadPool(UniqueIDGenService.MAX_THREADS);
    }

    // Ensure threads used by the ThreadPoolExecutor complete and
    // are reclaimed by the system.
    public void shutdown() {
        mExecutor.shutdownNow();
    }

    /**
     * Hook method called back when a request message arrives from the
     * UniqueIDGenActivity.  The message it receives contains
     * the messenger used to reply to the activity.
     */
    public void handleMessage(Message request) {
        // Store the reply messenger so it doesn't change out from
        // underneath us.
        final Messenger replyMessenger = request.replyTo;

        // Store the request id so that it can be bounced back
        // as in arg1 of the replay message.
        final int requestId = request.arg1;

        // Create a runnable give it to the thread pool for subsequent
        // concurrent processing.
        mExecutor.execute(() -> {
            try {
                // Send an reply to show an animation from the
                // service to this thread.
                Message animationReply = Message.obtain();
                animationReply.arg1 = requestId;
                animationReply.arg2 = (int) Thread.currentThread().getId();
                replyMessenger.send(animationReply);

                // Generate a unique ID that's 128 bytes long.
                Message reply = generateUniqueID();

                // Send a replay to show an animation back
                // from thread to service.
                animationReply.arg2 = GeneratorView.SERVICE_NODE;
                replyMessenger.send(animationReply);

                // Set reply message id and path to animate.
                reply.arg1 = requestId;
                reply.arg2 = GeneratorView.ACTIVITY_NODE;

                Log.d(TAG, "UUID reply = " + reply);

                // Send the reply back to the Activity.
                replyMessenger.send(reply);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });

    }

    /**
     * Return a Message containing an ID that's unique system-wide.
     */
    private Message generateUniqueID() {
        String uniqueID;

        // Protect critical section to ensure the IDs are unique.
        synchronized (this) {
            // This loop keeps generating a random UUID if it's not
            // unique (i.e., is not currently found in the persistent
            // collection of SharedPreferences).  The likelihood of a
            // non-unique UUID is low, as per the discussion in
            // en.wikipedia.org/wiki/Universally_unique_identifier
            // #Random_UUID_probability_of_duplicates.  However, we're
            // being extra paranoid for the sake of this example.. ;-)
            do {
                uniqueID = UUID.randomUUID().toString();
            } while (mSharedPrefs.getInt(uniqueID, 0) == 1);

            // We found a unique ID, so add it as the "key" to the
            // persistent collection of SharedPreferences, with a
            // value of 1 to indicate this ID is already "used".
            SharedPreferences.Editor editor = mSharedPrefs.edit();
            editor.putInt(uniqueID, 1);

            // Commit the change so it's stored persistently.
            editor.commit();

            // Simulate a delay for the animation to make sense.
            try {
                sleep(MAX_DELAY);
            } catch (InterruptedException e) {
            }
        }

        // Create a Message that's used to send the unique ID back to
        // the UniqueIDGeneratorActivity.
        Message reply = Message.obtain();
        Bundle data = new Bundle();
        data.putString(UniqueIDGenService.ID,
                       uniqueID);
        reply.setData(data);
        return reply;
    }
}


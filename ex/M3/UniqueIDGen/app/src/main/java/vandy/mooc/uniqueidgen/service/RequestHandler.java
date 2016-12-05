package vandy.mooc.uniqueidgen.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.IBinder;
import android.preference.PreferenceManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.UUID;


/**
 * This class generates unique IDs via a pool of Threads and sends
 * them back to UniqueIDGeneratorActivity. When it's created, it
 * creates a ThreadPoolExecutor using the newFixedThreadPool() method
 * of the Executors class.
 */
class RequestHandler 
      extends Handler {
    /**
     * A class constant that determines the maximum number of threads
     * used to service download requests.
     */
    private final int MAX_THREADS = 4;
	
    /**
     * The ExecutorService implementation that references a fixed-size
     * thread pool.
     */
    private ExecutorService mExecutor;

    /**
     * A collection of unique IDs implemented internally using a
     * persistent Java HashMap.
     */
    private SharedPreferences mUniqueIds;

    /**
     * Initialize RequestHandler to generate IDs concurrently.
     */
    public RequestHandler(Context context) {
        // Get a SharedPreferences instance that points to the default
        // file used by the preference framework in this Service.
        mUniqueIds = 
            PreferenceManager.getDefaultSharedPreferences
            (context);

        // Create a FixedThreadPool Executor that's configured to use
        // MAX_THREADS.
        mExecutor = 
            Executors.newFixedThreadPool(MAX_THREADS);
    }

    // Ensure threads used by the ThreadPoolExecutor complete and
    // are reclaimed by the system.
    public void shutdown() {
	// Shutdown the thread pool *now*!
        mExecutor.shutdownNow();
    }

    /**
     * Return a Message containing an ID that's unique
     * system-wide.
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
            } while (mUniqueIds.getInt(uniqueID, 0) == 1);

            // We found a unique ID, so add it as the "key" to the
            // persistent collection of SharedPreferences, with a
            // value of 1 to indicate this ID is already "used".
            SharedPreferences.Editor editor = mUniqueIds.edit();
            editor.putInt(uniqueID, 1);
            
            // Commit the change so it's stored persistently.
            editor.commit();
        }

        // Create a Message that's used to send the unique ID back to
        // the UniqueIDGeneratorActivity.
        Message reply = Message.obtain();
        Bundle data = new Bundle();
        data.putString(UniqueIDGenService.ID, uniqueID);
        reply.setData(data);
        return reply;
    }

    /**
     * Hook method called back when a request Message arrives from the
     * UniqueIDGeneratorActivity.  The message it receives contains
     * the Messenger used to reply to the Activity.
     */
    public void handleMessage(Message request) {
        // Store the reply messenger so it doesn't change out from
        // underneath us.
        final Messenger replyMessengerRef = request.replyTo;

        // Log.d(TAG, "replyMessenger = " + replyMessenger.hashCode());

        // Create a lambda expression (runnable) and give it to the
        // thread pool for subsequent concurrent processing.
        mExecutor.execute(() -> {
                // Generate a unique ID.
                Message reply = generateUniqueID();
                        
                try {
                    // Send the reply back to the UniqueIDGenActivity.
                    replyMessengerRef.send(reply);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            });

    }
}


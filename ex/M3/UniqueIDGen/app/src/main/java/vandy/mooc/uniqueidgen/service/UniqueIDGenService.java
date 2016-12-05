package vandy.mooc.uniqueidgen.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Messenger;

/**
 * This Service generates unique IDs via a thread pool and returns the
 * IDs to the UniqueIDGenActivity.  A thread pool can process requests
 * concurrently and improve performance on a multi-core device.
 * <p>
 * This class implements the Synchronous Service layer of the
 * Half-Sync/Half-Async pattern.  It also implements a variant of the
 * Factory Method pattern.
 */
public class UniqueIDGenService 
       extends Service {
    /**
     * String used as a key for the unique ID stored in the reply Message.
     */
    public final static String ID = "ID";

    /**
     * A class constant that determines the maximum number of threads
     * used to service download requests. Made public so that
     * GeneratorView will know how many threads can be animated.
     */
    public final static int MAX_THREADS = 4;

    /**
     * Used for debugging.
     */
    private final String TAG = getClass().getName();

    /**
     * A RequestHandler that processes Messages from the UniqueIDGenService
     * within a pool of Threads.
     */
    private RequestHandler mRequestHandler = null;

    /**
     * A Messenger that encapsulates the RequestHandler used to handle
     * request Messages sent from the MainActivity.
     */
    private Messenger mReqMessenger = null;

    /**
     * Factory method to make the desired Intent.
     */
    public static Intent makeIntent(Context context) {
        // Create the Intent that's associated to the
        // UniqueIDGenService class.
        return new Intent(context,
                          UniqueIDGenService.class);
    }

    /**
     * Hook method called when the Service is created.
     */
    @Override
    public void onCreate() {
        // The Messenger encapsulates the RequestHandler used to
        // handle request Messages sent from MainActivity.
        mRequestHandler = new RequestHandler(this);
        mReqMessenger = new Messenger(mRequestHandler);
    }

    /**
     * Factory method that returns the underlying IBinder associated
     * with the RequestMessenger.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mReqMessenger.getBinder();
    }

    /**
     * Called when the service is destroyed, which is the last call
     * the Service receives informing it to clean up any resources it
     * holds.
     */
    @Override
    public void onDestroy() {
        // Ensure threads used by the ThreadPoolExecutor complete and
        // are reclaimed by the system.
        mRequestHandler.shutdown();
    }
}
    

package vandy.mooc.hobbitcontentprovider.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.net.Uri;
import android.util.Log;

/**
 * Implements a Content Provider used to manage Hobbit characters.
 */
public class HobbitProvider 
       extends ContentProvider {
    /**
     * Debugging tag used by the Android logger.
     */
    protected final static String TAG =
        HobbitProvider.class.getSimpleName();

    /**
     * Use HobbitDatabaseHelper to manage database creation and version
     * management.
     */
    private HobbitDatabaseHelper mOpenHelper;

    /**
     * Context for the Content Provider.
     */
    private Context mContext;

    /**
     * The code that is returned when a URI for more than 1 items is
     * matched against the given components.  Must be positive.
     */
    public static final int CHARACTERS = 100;

    /**
     * The code that is returned when a URI for exactly 1 item is
     * matched against the given components.  Must be positive.
     */
    public static final int CHARACTER = 101;

    /**
     * The URI Matcher used by this content provider.
     */
    private static final UriMatcher sUriMatcher =
        buildUriMatcher();

    /**
     * Hook method returns true if successfully started.
     */
    @Override
    public boolean onCreate() {
        mContext = getContext();

        // Select the concrete implementor.
        // Create the HobbitDatabaseHelper.
        mOpenHelper =
                new HobbitDatabaseHelper(mContext);
        return true;
    }

    /**
     * Helper method that matches each URI to the integer
     * constants defined above.
     * 
     * @return UriMatcher
     */
    protected static UriMatcher buildUriMatcher() {
        // All paths added to the UriMatcher have a corresponding code
        // to return when a match is found.  The code passed into the
        // constructor represents the code to return for the rootURI.
        // It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = 
            new UriMatcher(UriMatcher.NO_MATCH);

        // For each type of URI that is added, a corresponding code is
        // created.
        matcher.addURI(CharacterContract.CONTENT_AUTHORITY,
                       CharacterContract.PATH_CHARACTER,
                       CHARACTERS);
        matcher.addURI(CharacterContract.CONTENT_AUTHORITY,
                       CharacterContract.PATH_CHARACTER
                       + "/#",
                       CHARACTER);
        return matcher;
    }

    /**
     * Method called to handle type requests from client applications.
     * It returns the MIME type of the data associated with each
     * URI.  
     */
    @Override
    public String getType(@NonNull Uri uri) {
        // Match the id returned by UriMatcher to return appropriate
        // MIME_TYPE.
        switch (sUriMatcher.match(uri)) {
            case CHARACTERS:
                return CharacterContract.CharacterEntry.CONTENT_ITEMS_TYPE;
            case CHARACTER:
                return CharacterContract.CharacterEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: "
                        + uri);
        }
    }

    /**
     * Method called to handle insert requests from client apps.
     */
    @Override
    public Uri insert(@NonNull Uri uri,
                      ContentValues cvs) {
        Uri returnUri;

        printCharacters("inserting", cvs, uri);

        // Try to match against the path in a url.  It returns the
        // code for the matched node (added using addURI), or -1 if
        // there is no matched node.  If there's a match insert a new
        // row.
        switch (sUriMatcher.match(uri)) {
        case CHARACTERS:
            returnUri = insertCharacters(uri,
                                         cvs);
            break;
        default:
            throw new UnsupportedOperationException("Unknown uri: " 
                                                    + uri);
        }

        // Notifies registered observers that a row was inserted.
        mContext.getContentResolver().notifyChange(uri, 
                                                   null);
        return returnUri;
    }

    private Uri insertCharacters(Uri uri,
                                 ContentValues cvs) {
        final SQLiteDatabase db =
            mOpenHelper.getWritableDatabase();

        long id =
            db.insert(CharacterContract.CharacterEntry.TABLE_NAME,
                      null,
                      cvs);

        // Check if a new row is inserted or not.
        if (id > 0)
            return CharacterContract.CharacterEntry.buildUri(id);
        else
            throw new android.database.SQLException
                ("Failed to insert row into " 
                 + uri);
    }

    /**
     * Method that handles bulk insert requests.
     */
    @Override
    public int bulkInsert(@NonNull Uri uri,
                          ContentValues[] cvsArray) {

        for (ContentValues cvs : cvsArray)
            printCharacters("bulk inserting", cvs, uri);

        // Try to match against the path in a url.  It returns the
        // code for the matched node (added using addURI), or -1 if
        // there is no matched node.  If there's a match bulk insert
        // new rows.
        switch (sUriMatcher.match(uri)) {
        case CHARACTERS:
            int returnCount = bulkInsertCharacters(uri,
                                                   cvsArray);

            if (returnCount > 0)
                // Notifies registered observers that row(s) were
                // inserted.
                mContext.getContentResolver().notifyChange(uri, 
                                                           null);
            return returnCount;
        default:
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Method that handles bulk insert requests.
     */
    private int bulkInsertCharacters(Uri uri,
                                     ContentValues[] cvsArray) {
        // Create and/or open a database that will be used for reading
        // and writing. Once opened successfully, the database is
        // cached, so you can call this method every time you need to
        // write to the database.
        final SQLiteDatabase db =
            mOpenHelper.getWritableDatabase();

        int returnCount = 0;

        // Begins a transaction in EXCLUSIVE mode. 
        db.beginTransaction();
        try {
            for (ContentValues cvs : cvsArray) {
                final long id =
                    db.insert(CharacterContract.CharacterEntry.TABLE_NAME,
                              null,
                              cvs);
                if (id != -1)
                    returnCount++;
            }

            // Marks the current transaction as successful.
            db.setTransactionSuccessful();
        } finally {
            // End a transaction.
            db.endTransaction();
        }
        return returnCount;
    }

    /**
     * Method called to handle query requests from client
     * applications.
     */
    @Override
    public Cursor query(@NonNull Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectionArgs,
                        String sortOrder) {
        Cursor cursor;

        // Match the id returned by UriMatcher to query appropriate
        // rows.
        switch (sUriMatcher.match(uri)) {
        case CHARACTERS:
            cursor = queryCharacters(uri,
                                     projection,
                                     selection,
                                     selectionArgs,
                                     sortOrder);
            break;
        case CHARACTER:
            cursor = queryCharacter(uri,
                                    projection,
                                    selection,
                                    selectionArgs,
                                    sortOrder);
            break;
        default:
            throw new UnsupportedOperationException("Unknown uri: " 
                                                    + uri);
        }

        // Register to watch a content URI for changes.
        cursor.setNotificationUri(mContext.getContentResolver(), 
                                  uri);
        return cursor;
    }

    /**
     * Method called to handle query requests from client
     * applications.  
     */
    private Cursor queryCharacters(Uri uri,
                                   String[] projection,
                                   String selection,
                                   String[] selectionArgs,
                                   String sortOrder) {
        // Expand the selection if necessary.
        selection = addSelectionArgs(selection, 
                                     selectionArgs,
                                     "OR");
        return mOpenHelper.getReadableDatabase().query
            (CharacterContract.CharacterEntry.TABLE_NAME,
             projection,
             selection,
             selectionArgs,
             null,
             null,
             sortOrder);
    }

    /**
     * Method called to handle query requests from client
     * applications.  
     */
    private Cursor queryCharacter(Uri uri,
                                  String[] projection,
                                  String selection,
                                  String[] selectionArgs,
                                  String sortOrder) {
        // Query the SQLite database for the particular rowId based on
        // (a subset of) the parameters passed into the method.
        return mOpenHelper.getReadableDatabase().query
            (CharacterContract.CharacterEntry.TABLE_NAME,
             projection,
             addKeyIdCheckToWhereStatement(selection,
                                           ContentUris.parseId(uri)),
             selectionArgs,
             null,
             null,
             sortOrder);
    }

    /**
     * Method called to handle update requests from client
     * applications.
     */
    @Override
    public int update(@NonNull Uri uri,
                      ContentValues cvs,
                      String selection,
                      String[] selectionArgs) {
        int returnCount;

        printCharacters("updating", cvs, uri);

        // Try to match against the path in a url.  It returns the
        // code for the matched node (added using addURI), or -1 if
        // there is no matched node.  If there's a match update rows.
        switch (sUriMatcher.match(uri)) {
        case CHARACTERS:
            returnCount = updateCharacters(uri,
                                           cvs,
                                           selection,
                                           selectionArgs);
            break;
        case CHARACTER:
            returnCount =  updateCharacter(uri,
                                           cvs,
                                           selection,
                                           selectionArgs);
            break;
        default:
            throw new UnsupportedOperationException();
        }

        if (returnCount > 0)
            // Notifies registered observers that row(s) were
            // updated.
            mContext.getContentResolver().notifyChange(uri, 
                                                           null);
        return returnCount;
    }

    /**
     * Method called to handle update requests from client
     * applications.  
     */
    private int updateCharacters(Uri uri,
                                 ContentValues cvs,
                                 String selection,
                                 String[] selectionArgs) {
        // Expand the selection if necessary.
        selection = addSelectionArgs(selection, 
                                     selectionArgs,
                                     " OR ");
        return mOpenHelper.getWritableDatabase().update
            (CharacterContract.CharacterEntry.TABLE_NAME,
             cvs,
             selection,
             selectionArgs);
    }

    /**
     * Method called to handle update requests from client
     * applications.  
     */
    private int updateCharacter(Uri uri,
                                ContentValues cvs,
                                String selection,
                                String[] selectionArgs) {
        // Expand the selection if necessary.
        selection = addSelectionArgs(selection,
                                     selectionArgs,
                                     " OR ");
        // Just update a single row in the database.
        return mOpenHelper.getWritableDatabase().update
            (CharacterContract.CharacterEntry.TABLE_NAME,
             cvs,
             addKeyIdCheckToWhereStatement(selection,
                                           ContentUris.parseId(uri)),
             selectionArgs);
    }

    /**
     * Method called to handle delete requests from client
     * applications.
     */
    @Override
    public int delete(@NonNull Uri uri,
                      String selection,
                      String[] selectionArgs) {
        int returnCount;

        printSelectionArgs("deleting", selection, selectionArgs, uri);

        // Try to match against the path in a url.  It returns the
        // code for the matched node (added using addURI), or -1 if
        // there is no matched node.  If there's a match delete rows.
        switch (sUriMatcher.match(uri)) {
        case CHARACTERS:
            returnCount = deleteCharacters(uri,
                                           selection,
                                           selectionArgs);
            break;
        case CHARACTER:
            returnCount =  deleteCharacter(uri,
                                           selection,
                                           selectionArgs);
            break;
        default:
            throw new UnsupportedOperationException();
        }

        if (selection == null
            || returnCount > 0)
            // Notifies registered observers that row(s) were deleted.
            mContext.getContentResolver().notifyChange(uri, 
                                                       null);

        return returnCount;
    }

    /**
     * Method called to handle delete requests from client
     * applications.  
     */
    private int deleteCharacters(Uri uri,
                                 String selection,
                                 String[] selectionArgs) {
        // Expand the selection if necessary.
        selection = addSelectionArgs(selection, 
                                     selectionArgs,
                                     " OR ");
        return mOpenHelper.getWritableDatabase().delete
            (CharacterContract.CharacterEntry.TABLE_NAME,
             selection,
             selectionArgs);
    }

    /**
     * Method called to handle delete requests from client
     * applications.  
     */
    private int deleteCharacter(Uri uri,
                                String selection,
                                String[] selectionArgs) {
        // Expand the selection if necessary.
        selection = addSelectionArgs(selection, 
                                     selectionArgs,
                                     " OR ");
        // Just delete a single row in the database.
        return mOpenHelper.getWritableDatabase().delete
            (CharacterContract.CharacterEntry.TABLE_NAME,
             addKeyIdCheckToWhereStatement(selection,
                                           ContentUris.parseId(uri)),
             selectionArgs);
    }

    /**
     * Return a selection string that concatenates all the
     * @a selectionArgs for a given @a selection using the given @a
     * operation.
     */
    private String addSelectionArgs(String selection,
                                    String [] selectionArgs,
                                    String operation) {
        // Handle the "null" case.
        if (selection == null
            || selectionArgs == null)
            return null;
        else {
            String selectionResult = "";

            // Properly add the selection args to the selectionResult.
            for (int i = 0;
                 i < selectionArgs.length - 1;
                 ++i)
                selectionResult += (selection 
                           + " = ? " 
                           + operation 
                           + " ");
            
            // Handle the final selection case.
            selectionResult += (selection
                       + " = ?");

            printSelectionArgs(operation,
                               selectionResult,
                               selectionArgs,
                               null);

            return selectionResult;
        }
    }        

    /**
     * Helper method that appends a given key id to the end of the
     * WHERE statement parameter.
     */
    private static String addKeyIdCheckToWhereStatement(String whereStatement,
                                                        long id) {
        String newWhereStatement;
        if (TextUtils.isEmpty(whereStatement)) 
            newWhereStatement = "";
        else 
            newWhereStatement = whereStatement + " AND ";

        // Append the key id to the end of the WHERE statement.
        return newWhereStatement 
            + CharacterContract.CharacterEntry._ID
            + " = '"
            + id 
            + "'";
    }

    /**
     * Print out the characters to logcat.
     *
     * @param operation
     * @param cvs
     * @param uri
     */
    void printCharacters(String operation,
                         ContentValues cvs,
                         Uri uri) {
        Log.d(TAG, operation + " on " + uri);
        for (String key : cvs.keySet()) {
            Log.d(TAG, key + " " + cvs.get(key));
        }

    }

    /**
     * Printout the selection args to logcat.
     *
     * @param operation
     * @param selectionResult
     * @param selectionArgs
     */
    void printSelectionArgs(String operation,
                            String selectionResult,
                            String[] selectionArgs,
                            Uri uri) {
        // Output the selectionResults to Logcat.
        Log.d(TAG,
                operation
                        + " on "
                        + (uri == null ? "null" : uri)
                        + " selection = "
                        + selectionResult
                        + " selectionArgs = ");
        if (selectionArgs != null && selectionArgs.length > 0)
            for (String args : selectionArgs)
                Log.d(TAG,
                        args + " ");
    }
}

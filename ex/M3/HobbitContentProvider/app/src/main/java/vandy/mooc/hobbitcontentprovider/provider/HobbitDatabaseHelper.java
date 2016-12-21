package vandy.mooc.hobbitcontentprovider.provider;

import java.io.File;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * The database helper used by the HobbitProvider to create
 * and manage its underlying SQLite database.
 */
public class HobbitDatabaseHelper 
       extends SQLiteOpenHelper {
    /**
     * Database name.
     */
    private static final String DATABASE_NAME =
        "vandy_mooc_hobbit_db";

    /**
     * Database version number, which is updated with each schema
     * change.
     */
    private static int DATABASE_VERSION = 1;

    /*
     * SQL create table statements.
     */

    /**
     * SQL statement used to create the Hobbit table.
     */
    final String SQL_CREATE_HOBBIT_TABLE =
        "CREATE TABLE "
        + CharacterContract.CharacterEntry.TABLE_NAME + " (" 
        + CharacterContract.CharacterEntry._ID + " INTEGER PRIMARY KEY, " 
        + CharacterContract.CharacterEntry.COLUMN_NAME + " TEXT NOT NULL, " 
        + CharacterContract.CharacterEntry.COLUMN_RACE + " TEXT NOT NULL "
        + " );";

     /**
     * Constructor - initialize database name and version, but don't
     * actually construct the database (which is done in the
     * onCreate() hook method). It places the database in the
     * application's cache directory, which will be automatically
     * cleaned up by Android if the device runs low on storage space.
     * 
     * @param context Any context
     */
    public HobbitDatabaseHelper(Context context) {
        super(context, 
              context.getCacheDir()
              + File.separator 
              + DATABASE_NAME, 
              null,
              DATABASE_VERSION);
    }

    /**
     * Hook method called when the database is created.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the table.
        db.execSQL(SQL_CREATE_HOBBIT_TABLE);
    }

    /**
     * Hook method called when the database is upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion,
                          int newVersion) {
        // Delete the existing tables.
        db.execSQL("DROP TABLE IF EXISTS "
                   + CharacterContract.CharacterEntry.TABLE_NAME);
        // Create the new tables.
        onCreate(db);
    }
}

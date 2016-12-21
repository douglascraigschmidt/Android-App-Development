package vandy.mooc.hobbitcontentprovider.provider;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * This contract defines the metadata for the HobbitProvider,
 * including the provider's access URIs and its "database" constants.
 */
public final class CharacterContract {
    /**
     * This ContentProvider's unique identifier.
     */
    public static final String CONTENT_AUTHORITY =
        "vandy.mooc.hobbitprovider";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which
     * apps will use to contact the content provider.
     */
    public static final Uri BASE_CONTENT_URI =
        Uri.parse("content://"
                  + CONTENT_AUTHORITY);

    /**
     * This string is appended to base content URI for possible
     * URI's, e.g., a valid path for looking at Character data is
     * content://vandy.mooc.hobbitcontentprovider/character_table.
     * However, content://vandy.mooc.hobbitcontentprovider/givemeroot
     * will fail, as the ContentProvider hasn't been given any
     * information on what to do with "givemeroot".
     */
    public static final String PATH_CHARACTER =
        CharacterEntry.TABLE_NAME;

    /*
     * Columns
     */

    /**
     * Nested class that defines the table contents of the Hobbit
     * table.
     */
    public static final class CharacterEntry 
           implements BaseColumns {
        /**
         * Use BASE_CONTENT_URI to create the unique URI for Character
         * Table that apps will use to contact the content provider.
         */
        public static final Uri CONTENT_URI = 
            BASE_CONTENT_URI.buildUpon()
            .appendPath(PATH_CHARACTER).build();

        /**
         * When the Cursor returned for a given URI by the
         * ContentProvider contains 0..x items.
         */
        public static final String CONTENT_ITEMS_TYPE =
            "vnd.android.cursor.dir/"
            + CONTENT_AUTHORITY
            + "/" 
            + PATH_CHARACTER;
            
        /**
         * When the Cursor returned for a given URI by the
         * ContentProvider contains 1 item.
         */
        public static final String CONTENT_ITEM_TYPE =
            "vnd.android.cursor.item/"
            + CONTENT_AUTHORITY
            + "/" 
            + PATH_CHARACTER;

        /**
         * Columns to display.
         */
        public static final String sColumnsToDisplay [] = 
            new String[] {
            CharacterContract.CharacterEntry._ID,
            CharacterContract.CharacterEntry.COLUMN_NAME,
            CharacterContract.CharacterEntry.COLUMN_RACE
        };
    
        /**
         * Name of the database table.
         */
        public static final String TABLE_NAME =
            "character_table";

        /**
         * Columns to store data.
         */
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_RACE = "race";

        /**
         * Return a Uri that points to the row containing a given id.
         *
         * @param id row id
         * @return Uri URI for the specified row id
         */
        public static Uri buildUri(Long id) {
            return ContentUris.withAppendedId(CONTENT_URI,
                                              id);
        }
    }
}

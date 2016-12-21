package vandy.mooc.hobbitcontentprovider.activities;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import vandy.mooc.hobbitcontentprovider.R;
import vandy.mooc.hobbitcontentprovider.provider.CharacterRecord;

/**
 * A fragment framework that organizes all Hobbit characters into race
 * groups.
 */
public class RaceFragment 
       extends Fragment {
    /**
     * Maps CardView ids to race adapters.
     */
    private ArrayMap<Integer, RaceInfo> mCardMap = new ArrayMap<>();

    /**
     * Object used to notify activity to update backdrop image.
     */
    private OnClassClickCallback mOnClassClickCallback;

    /**
     * Mandatory empty constructor for the fragment manager to
     * instantiate the fragment (e.g., upon screen orientation
     * changes).
     */
    public RaceFragment() {
    }

    /**
     * Hook method called when the fragment view is created.  One time
     * initialization goes here.
     *
     * @param savedInstanceState object that contains saved state information.
     */
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the fragment view layout.
        View view = inflater.inflate(R.layout.fragment_race, 
                                     container,
                                     false);

        // Initialize race cards their recycler views.
        initializeCards(view);

        return view;
    }

    /**
     * Called when a fragment is first attached to its context. {@link
     * #onCreate(Bundle)} will be called after this.
     *
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        // Assign the click callback.
        if (context instanceof OnClassClickCallback) 
            mOnClassClickCallback = (OnClassClickCallback) context;

        // Call up to the super class.
        super.onAttach(context);
    }

    /**
     * Initializes the race map and creates an adapter for each
     * recycler view.
     *
     * @param view the top level fragment view.
     */
    private void initializeCards(View view) {
        // Initialize map with each CardView id and a matching RaceInfo
        // instance.
        mCardMap = new ArrayMap<>();
        mCardMap.put(R.id.hobbit_card,
                     new RaceInfo(R.drawable.hobbit,
                                  new RaceAdapter("hobbit", null)));
        mCardMap.put(R.id.maia_card,
                     new RaceInfo(R.drawable.gandalf,
                                  new RaceAdapter("maia", null)));
        mCardMap.put(R.id.bear_card,
                     new RaceInfo(R.drawable.bear,
                                  new RaceAdapter("bear", null)));
        mCardMap.put(R.id.human_card,
                     new RaceInfo(R.drawable.human,
                                  new RaceAdapter("human", null)));
        mCardMap.put(R.id.dwarf_card,
                     new RaceInfo(R.drawable.dwarf,
                                  new RaceAdapter("dwarf", null)));
        mCardMap.put(R.id.dragon_card,
                     new RaceInfo(R.drawable.dragon,
                                  new RaceAdapter("dragon", null)));

        // The number of grid layout columns (span) depends on the
        // current orientation.
        int span = getResources()
            .getConfiguration()
            .orientation == Configuration.ORIENTATION_PORTRAIT ? 3 : 4;

        // Initialize each recycler view with the appropriate race
        // adapter and set the CardView titles to display the race
        // name.
        mCardMap.forEach((integer, raceInfo) -> {
            // Get the card view instance.
            CardView cardView = (CardView) view.findViewById(integer);

            // Get the recycler view instance.
            RecyclerView recyclerView =
                    (RecyclerView) cardView.findViewById(R.id.list);

            // Set a grid layout manager with the appropriate span
            // count.
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(),
                                                                span));

            // Install the card's RecyclerView adapter.
            recyclerView.setAdapter(raceInfo.mAdapter);

            // Set the card title.
            TextView titleView = (TextView) cardView.findViewById(R.id.title);
            titleView.setText(formatDisplayText(raceInfo.mAdapter.getRace()));

            // Set the race image when the user clicks anywhere on the
            // card.
            cardView.setOnClickListener(v -> setImage(raceInfo.mImageId));
        });
    }

    /**
     * Helper that sets the activity's backdrop image to match the
     * passed class card view resource id.
     */
    private void setImage(int id) {
        if (mOnClassClickCallback != null) 
            mOnClassClickCallback.updateImage(id);
    }

    /**
     * Helper that sets the activity's backdrop image to match the
     * passed class name.
     */
    private void setImage(String race) {
        for (final Map.Entry<Integer, RaceInfo> entry : mCardMap.entrySet()) {
            RaceInfo raceInfo = entry.getValue();
            if (race.equalsIgnoreCase(raceInfo.mAdapter.getRace())) 
                setImage(raceInfo.mImageId);
        }
    }

    /**
     * Converts that passed string to uppercase first letter followed
     * by lower case letters.
     */
    private String formatDisplayText(String text) {
        return text.substring(0, 1)
                   .toUpperCase() + text.substring(1);
    }

    /**
     * Reset each race group adapter to the values stored in the passed cursor.
     *
     * @param cursor a database cursor containing a list of characters.
     */
    public void setData(Cursor cursor) {
        mCardMap.forEach((integer, raceAdapter) -> {
                List<CharacterRecord> records =
                    getAllRaceCharacters(raceAdapter.mAdapter.getRace(),
                                         cursor);
                raceAdapter.mAdapter.setData(records);
                getView().findViewById(integer)
                         .setVisibility(records.isEmpty() ? View.GONE : View.VISIBLE);
            });
    }

    /**
     * Builds and returns a list of characters belonging to the
     * specified race.
     *
     * @param race   Race to filter for
     * @param cursor a database cursor to extract race characters from
     * @return all the characters belonging to the specified race
     */
    private List<CharacterRecord> getAllRaceCharacters(String race,
                                                       Cursor cursor) {
        ArrayList<CharacterRecord> records = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) 
            do {
                CharacterRecord record =
                    CharacterRecord.fromCursor(cursor);
                if (record.getRace().equalsIgnoreCase(race)) 
                    records.add(record);
            } while (cursor.moveToNext());

        return records;
    }

    /**
     * Interface used to notify activity to update backdrop image.
     */
    public interface OnClassClickCallback {
        /**
         * Method that updates theimage.
         */
        void updateImage(int imageId);
    }

    /**
     * Pure immutable data class containing information for a race.
     */
    private class RaceInfo {
        @DrawableRes final int mImageId;
        final RaceAdapter mAdapter;

        public RaceInfo(@DrawableRes int imageId,
                        RaceAdapter adapter) {
            mImageId = imageId;
            mAdapter = adapter;
        }
    }

    /**
     * A local adapter class that displays all characters for a given race using
     * the RecyclerViewHolder create and bind view holder pattern.
     */
    private class RaceAdapter
        extends RecyclerView.Adapter<RaceAdapter.RaceViewHolder> {

        private final String mRace;
        private List<CharacterRecord> mRecords;

        /**
         * Constructor keeps track of both the race group and the character
         * records.
         *
         * @param race    the race group of the characters in this adapter
         * @param records all the characters belonging to the specified race
         */
        public RaceAdapter(String race, List<CharacterRecord> records) {
            mRace = race;
            mRecords = records != null ? records : new ArrayList<>();
        }

        /**
         * Hook method called when a new view holder is required for display.
         */
        @Override
        public RaceViewHolder onCreateViewHolder(ViewGroup parent,
                                                 int viewType) {
            View view = 
                LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_race_item,
                         parent,
                         false);
            return new RaceViewHolder(view);
        }

        /**
         * Hook method called when an existing view holder is about to
         * be reused to display a recycler list item.
         */
        @Override
        public void onBindViewHolder(final RaceViewHolder holder,
                                     int position) {
            CharacterRecord record = mRecords.get(position);
            holder.mTextView.setText(formatDisplayText(record.getName()));
        }

        /**
         * Hook method called to determine the total number of items
         * backing this adapter.
         *
         * @return the total number of list items
         */
        @Override
            public int getItemCount() {
            return mRecords.size();
        }

        /**
         * Helper method that replaces the current list of displayed records
         * with a new list.
         *
         * @param records new list to display
         */
        public void setData(List<CharacterRecord> records) {
            mRecords = records != null ? records : new ArrayList<>();
            notifyDataSetChanged();
        }

        /**
         * Helper method that returns the race of this adapter.
         *
         * @return race string
         */
        public String getRace() {
            return mRace;
        }

        /**
         * Custom view holder that stores any views used to display data for a
         * single recycler view list item.
         */
        class RaceViewHolder
              extends RecyclerView.ViewHolder {
            final TextView mTextView;

            /**
             * Constructor initializes the field.
             */
            public RaceViewHolder(View view) {
                super(view);
                mTextView = (TextView) view;
                mTextView.setClickable(false);
                mTextView.setOnClickListener(v -> setImage(mRace));
            }

            /**
             * Return a string value for this object.
             */
            @Override
            public String toString() {
                return super.toString() + " '" + mTextView.getText() + "'";
            }
        }
    }
}

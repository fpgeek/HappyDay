package com.toda.happyday;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import com.toda.happyday.db.DailyInfo;
import com.toda.happyday.db.DailyInfoDbHelper;

public class WriteActivity extends Activity {

    private DailyInfoDbHelper dbHelper = null;
    private long dailyInfoID;
    private static PlaceholderFragment placeholderFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        dailyInfoID = getIntent().getLongExtra(getString(R.string.EXTRA_DAILY_GROUP_ID), 0);
        String diaryText = getIntent().getStringExtra(getString(R.string.EXTRA_DAIRY_TEXT));
        dbHelper = new DailyInfoDbHelper(this);

        if (savedInstanceState == null) {
            placeholderFragment = new PlaceholderFragment(diaryText);
            getFragmentManager().beginTransaction()
                    .add(R.id.container, placeholderFragment)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.write, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_save:
                new SaveDairyTextTask(dailyInfoID).execute(placeholderFragment.getDiaryEditText().getText().toString());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class SaveDairyTextTask extends AsyncTask<String, Void, Boolean> {

        private long rowId;

        public SaveDairyTextTask(long rowId) {
            this.rowId = rowId;
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            String dairyText = strings[0];
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DailyInfo.DailyEntry.COLUMN_NAME_DIARY_TEXT, dairyText);

            String selection = DailyInfo.DailyEntry._ID + " = ?";
            String[] selectionArgs = { String.valueOf(rowId) };

            assert db != null;
            int count = db.update(
                    DailyInfo.DailyEntry.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs
            );

            return count == 1;
        }

        @Override
        protected void onPostExecute(Boolean updateSuccess) {
            super.onPostExecute(updateSuccess);

            if (updateSuccess) {
                finish();
            }
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private EditText diaryEditText;
        private String diaryText;

        public PlaceholderFragment(String diaryText) {
            this.diaryText = diaryText;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_write, container, false);

            diaryEditText = (EditText)rootView.findViewById(R.id.diary_edit_text);
            diaryEditText.setText(diaryText);
            diaryEditText.requestFocus();
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

            return rootView;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.write, menu);
        }

        public EditText getDiaryEditText() {
            return diaryEditText;
        }
    }



}
package com.example.robert.bettersnapchat;

/**
 * Created by Robert on 10/16/2015.
 */

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class GroupFragment extends Fragment implements   LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.group_fragment, container, false);
    }

    private final static String[] FROM_COLUMNS = {
           GroupDatabase.Groups.COLUMN_NAME_GROUP_NAME,
           GroupDatabase.Groups.COLUMN_NAME_INBOX
    };

    private final static int[] TO_IDS = {
           R.id.group_text,
           R.id.group_text_Inbox
    };

    ListView groupsList;

    String groupName;

    Uri groupUri;


    private SimpleCursorAdapter mCursorAdapter;

    public GroupFragment() {


    }


    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        GroupDbHelper snDb = new GroupDbHelper(getContext());
        getLoaderManager().initLoader(0, null, this);
        // Gets the ListView from the View list of the parent activity
        SQLiteDatabase db = snDb.getWritableDatabase();
        Cursor cursor = snDb.getAllGroups(db);

        groupsList = (ListView) getActivity().findViewById(R.id.group_list);
        // Gets a CursorAdapter
        mCursorAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.group_item,
                cursor,
                FROM_COLUMNS, TO_IDS,
                0);
        // Sets the adapter for the ListView
        groupsList.setAdapter(mCursorAdapter);

        groupsList.setOnItemClickListener(this);

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}

package com.android.uberurl;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class UberUrlMain extends ListActivity {
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;

    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int LAUNCH_ID = Menu.FIRST + 2;

    private UrlDbAdapter mDbHelper;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.url_list);
        mDbHelper = new UrlDbAdapter(this);
        mDbHelper.open();
        fillData();
        registerForContextMenu(getListView());
    }

    private void fillData() {
        // Get all of the rows from the database and create the item list
    	Cursor urlsCursor = mDbHelper.fetchAllUrls();

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{UrlDbAdapter.KEY_TITLE};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.text1};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter urls = 
            new SimpleCursorAdapter(this, R.layout.url_row, urlsCursor, from, to);
        setListAdapter(urls);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_insert);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case INSERT_ID:
                createUrl();
                return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.menu_delete);
        menu.add(0, LAUNCH_ID, 0, R.string.menu_launch);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	switch(item.getItemId()) {
            case DELETE_ID:
                mDbHelper.deleteUrl(info.id);
                fillData();
                return true;
            case LAUNCH_ID:
            	Cursor url = mDbHelper.fetchUrl(info.id);
                startManagingCursor(url);
                
            	launchURL(url.getString(
                        url.getColumnIndexOrThrow(UrlDbAdapter.KEY_TITLE))
            			);
            	return true;
        }
        return super.onContextItemSelected(item);
    }

    private void createUrl() {
        Intent i = new Intent(this, UrlEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }
    
    private void launchURL(String url) {
    	Intent i = new Intent(Intent.ACTION_VIEW, 
    	Uri.parse(url)); 
    	startActivity(i); 
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Intent i = new Intent(this, UrlEdit.class);
        i.putExtra(UrlDbAdapter.KEY_ROWID, id);

        startActivityForResult(i, ACTIVITY_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }
}

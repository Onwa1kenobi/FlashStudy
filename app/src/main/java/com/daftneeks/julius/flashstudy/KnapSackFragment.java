package com.daftneeks.julius.flashstudy;

import android.app.Activity;
//import android.app.ListFragment;
//import android.app.LoaderManager;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;

import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A placeholder fragment containing a simple view.
 */
public class KnapSackFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    //we declare an instance variable to refer to the HomeActivity in this fragment
    //i.e it refers to the object that implements the interface
    private KnapSackFragmentListener listener;

    //this adapter refers to the adapter that populates the knapsackListView
    private SimpleCursorAdapter knapsackAdapter;

    public KnapSackFragment() {
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), FlashProvider.CLASSES_CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        knapsackAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed. Ensure that the adapter is no
        // longer using the cursor by setting it to null
        knapsackAdapter.swapCursor(null);
    }

    //This KnapSackFragmentListener contains the callback methods that the HomeActivity implements to
    // communicate with the fragment
    public interface KnapSackFragmentListener {

        //called when the fragment launches to update the action bar title
        void setActionBarTitle(String title);

        //called when the user touches a class
        void onClassSelected(long rowID, String item);

        //called when the user touches the add class button
        void onAddClass();

        //called when the user touches the delete class button
        void onClassDeleted();
    }

    //the default methods a fragment overrides i.e the fragment lifecycle methods
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);

        //refer the listener variable declared above to the host activity when the fragment is attached
        listener = (KnapSackFragmentListener) activity;
    }

    @Override
    public void onDetach(){
        super.onDetach();

        //remove the KnapSackFragmentListener when the fragment is detached and set the listener variable to null
        listener = null;
    }

    /*@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.knapsack_fragment_home, container, false);
    }*/

    //fragment lifecycle method called after the onCreateView
    //this method is implemented so that the KnapSackFragment can perform the tasks it needs to once the default layout is inflated
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        listener.setActionBarTitle("KnapSack");

        //indicate that the fragment be retained rather than recreated when the HomeActivity is recreated
        //probably on a configuration change
        setRetainInstance(true);

        //has options menu items
        setHasOptionsMenu(true);

        //default text to display when the knapsackListView is empty
        setEmptyText(getResources().getString(R.string.empty_knapsack));

        //get reference to the built in listView
        ListView knapsackListView = getListView();

        knapsackListView.setPadding(8,8,8,8);

        //setOnItemClickListener for the knapsackListView
        knapsackListView.setOnItemClickListener(viewKnapsackListener);

        //the setChoiceMode that indicates that only one item can be selected at a time
        knapsackListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        //map each class' name to a TextView in the ListView layout

        //Because we are using a SimpleCursorAdapter(subClass of CursorAdapter), we first define
        //arrays containing the column names to map to GUI components and the resource IDs of GUI
        //components that will display the data from the named columns
        String[] from = new String[] {FlashProvider.COLUMN_CLASS_NAME};    //indicating that only the className will be displayed
        int[] to = new int[] {R.id.classNameHolder};  //int array containing corresponding GUI components' resource IDs

        knapsackAdapter = new SimpleCursorAdapter(getActivity(), R.layout.knapsack_list_item, null, from, to, 0);

        //bind the ListView to the Adapter
        setListAdapter(knapsackAdapter);

        getLoaderManager().initLoader(0, null, this);

        registerForContextMenu(getListView());

    }

    @Override
    public void onResume() {
        super.onResume();
        listener.setActionBarTitle("KnapSack");
    }


    @Override
    public void onStart() {
        super.onStart();
        listener.setActionBarTitle("KnapSack");
    }

    //displays this Fragment's menu items
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.knapsack_fragment_menu, menu);
    }

    //Handles choice from OptionsMenu
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch (menuItem.getItemId()){
            case R.id.action_add_class:
                listener.onAddClass();
                return true;
        }
        //call Super's method
        return super.onOptionsItemSelected(menuItem);
    }

    //this responds to user touching a defined class
    AdapterView.OnItemClickListener viewKnapsackListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //pass selected class to HomeActivity via the item's id
            listener.onClassSelected(id, ((TextView)view).getText().toString());
        }
    };

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.knapsack_list_longpress, menu);
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_delete:
                //the delete task
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Are you sure you want to delete this Class? \nThis will delete the Class and its Cards.")
                        .setTitle("CONFIRM!")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

                                int a = getActivity().getContentResolver().delete(
                                        ContentUris.withAppendedId(FlashProvider.DELETE_CLASS_CONTENT_URI, info.id),
                                        null, null);

                                listener.onClassDeleted();

                                Toast.makeText(getActivity(), "" + a + " Number of Card Rows deleted!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                builder.create().show();
                return true;
            }
        return super.onContextItemSelected(item);
    }
}
package com.daftneeks.julius.flashstudy;


import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.text.DecimalFormat;


/**
 * A simple {@link Fragment} subclass.
 */
public class ClassDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    //we declare an instance variable to refer to the HomeActivity in this fragment
    //i.e it refers to the object that implements the interface
    private ClassDetailsFragmentListener listener;

    //database rowID of the selected class
    private long rowID;
    static long class_ID;

    static String className;

    private Button runQuizButton;
    private ListView classDetailsListView;
    private TextView scoreText;

    CustomListViewAdapter customListViewAdapter;

    public ClassDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        CursorLoader cursorLoader;

        if (id == 0) {
            cursorLoader = new CursorLoader(getActivity(), FlashProvider.CARDS_CONTENT_URI, null, null, null, null);
            return cursorLoader;
        }
        else {
            cursorLoader = new CursorLoader(getActivity(), FlashProvider.CLASS_SCORE_CONTENT_URI, null, null, null, null);
            return cursorLoader;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        switch (loader.getId()) {
            case 0:
                if (data.getCount() > 1){
                    runQuizButton.setVisibility(View.VISIBLE);
                }
                customListViewAdapter = new CustomListViewAdapter(getActivity(), data);
                classDetailsListView.setAdapter(customListViewAdapter);
                break;

            case 1:

                if (data.getDouble(data.getColumnIndex(FlashProvider.COLUMN_CLASS_SCORE)) < 200) {
                    DecimalFormat decimalFormat = new DecimalFormat("#.##");
                    double a = data.getDouble(data.getColumnIndex(FlashProvider.COLUMN_CLASS_SCORE));
                    String finalScore = decimalFormat.format(a);
                    scoreText.setVisibility(View.VISIBLE);
                    scoreText.setText("Last Study Score: " + finalScore + "%");
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed. Ensure that the adapter is no
        // longer using the cursor by setting it to null
    }


    //This ClassDetailsFragmentListener contains the callback methods that the HomeActivity implements to
    // communicate with the fragment
    public interface ClassDetailsFragmentListener {

        //called when the fragment launches to update the action bar title
        void setActionBarTitle(String title);

        //called when the user touches a card
        void onCardSelected(long rowID);

        //called when the user touches the add card button and passes a Bundle containing the class' rowID
        void onAddCard(long classRowID);

        //called when the user touches the run quiz button
        void onRunQuiz(long classID);

        //called when the user touches the delete class button
        void onClassDeleted();
    }

    //the default methods a fragment overrides i.e the fragment lifecycle methods
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        //refer the listener variable declared above to the host activity when the fragment is attached
        listener = (ClassDetailsFragmentListener) activity;
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onDetach(){
        super.onDetach();
        //remove the ClassDetailsFragmentListener when the fragment is detached and set the listener variable to null
        listener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_class_details, container, false);

        scoreText = (TextView) view.findViewById(R.id.scoreText);
        runQuizButton = (Button) view.findViewById(R.id.runQuizButton);
        runQuizButton.setOnClickListener(runQuizButtonListener);

        classDetailsListView = (ListView) view.findViewById(R.id.classDetailsListView);



        return view;
    }



    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        //indicate that the fragment be retained rather than recreated when the HomeActivity is recreated
        //probably on a configuration change
        setRetainInstance(true);

        //if ClassDetailsFragment is being restored, get the saved rowID
        if (savedInstanceState != null){
            rowID = savedInstanceState.getLong(HomeActivity.ROW_ID);
            class_ID = rowID;
            className = savedInstanceState.getString(HomeActivity.CLASS_NAME);
        }
        else {
            // get Bundle of arguments then extract the rowID
            Bundle argument = getArguments();
            if (argument != null){
                rowID = argument.getLong(HomeActivity.ROW_ID);
                class_ID = rowID;
                className = argument.getString(HomeActivity.CLASS_NAME);
            }
        }

        listener.setActionBarTitle(className);

        //has options menu items
        setHasOptionsMenu(true);

        TextView emptyText = (TextView) view.findViewById(R.id.empty);

        //default text to display when the classDetailsListView is empty
        classDetailsListView.setEmptyView(emptyText);

        //setOnItemClickListener for the classDetailsListView
        classDetailsListView.setOnItemClickListener(viewClassDetailsListener);

        //the setChoiceMode that indicates that only one item can be selected at a time
        classDetailsListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        getLoaderManager().initLoader(1, null, this);

        //map each class' name to a TextView in the ListView layout.
        //Because we are using a SimpleCursorAdapter(subClass of CursorAdapter), we first define
        //arrays containing the column names to map to GUI components and the resource IDs of GUI
        //components that will display the data from the named columns

        //register the ListView with a contextMenu
        registerForContextMenu(classDetailsListView);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(HomeActivity.ROW_ID, rowID);
        outState.putString(HomeActivity.CLASS_NAME, className);
    }

    //displays this Fragment's menu items
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.class_details_fragment_menu, menu);
    }

    //Handles choice from OptionsMenu
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch (menuItem.getItemId()){
            case R.id.action_add_card:
                //create Bundle containing class' rowID
                Bundle arg = new Bundle();
                arg.putLong(HomeActivity.ROW_ID, rowID);
                //pass Bundle to listener
                listener.onAddCard(rowID);
                return true;

            case R.id.action_delete_class:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Are you sure you want to delete this Class? \nThis will delete the Class and its Cards.")
                        .setTitle("CONFIRM!")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                getActivity().getContentResolver().delete(
                                        ContentUris.withAppendedId(FlashProvider.DELETE_CLASS_CONTENT_URI, rowID),
                                        null, null);
                                listener.onClassDeleted();
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
        //call Super's method
        return super.onOptionsItemSelected(menuItem);
    }

    //this responds to user touching a defined class
    AdapterView.OnItemClickListener viewClassDetailsListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //pass selected class to HomeActivity via the item's id
            listener.onCardSelected(id);
        }
    };

    //this responds to the user touching the runQuizButton
    View.OnClickListener runQuizButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //create Bundle containing class' rowID
            Bundle bundle = new Bundle();
            bundle.putLong(HomeActivity.ROW_ID, rowID);
            //pass bundle to listener
            listener.onRunQuiz(rowID);
        }
    };

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.card_list_longpress, menu);
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_delete_card:
                //the delete task
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Are you sure you want to delete this Card? \n This cannot be Undone.")
                        .setTitle("CONFIRM!")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                                    getActivity().getContentResolver().delete(
                                        ContentUris.withAppendedId(FlashProvider.CARDS_CONTENT_URI, info.id),
                                        null, null);

//                                cursor = getActivity().managedQuery(FlashProvider.CARDS_CONTENT_URI, null, null, null, null);
//                                customListViewAdapter = new CustomListViewAdapter(getActivity(), cursor);
//                                classDetailsListView.setAdapter(customListViewAdapter);
//                                if (cursor.getCount() < 2){
//                                    runQuizButton.setVisibility(View.GONE);
//                                }

                                //get the selected card's Image path
                                String delPic = deleteImage(getImagePath(info.id));
                                //Toast.makeText(getActivity(), "" + delPic + " was deleted!", Toast.LENGTH_LONG).show();
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

    public String getImagePath(long id) {
        File imageDir = getActivity().getDir("images", Context.MODE_PRIVATE);
        return "" + imageDir + "/" + id + ".png";
    }

    private String deleteImage(String picPath) {
        if (picPath != null && picPath.length() != 0){
            File picFilePath = new File(picPath);
            picFilePath.delete();
        }
        return picPath;
    }
}

package com.daftneeks.julius.flashstudy;


import android.app.Activity;
//import android.app.AlertDialog;
//import android.app.Dialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
//import android.app.DialogFragment;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddEditClassFragment extends Fragment {

    //variable to reference the HomeActivity
    private AddEditClassFragmentListener listener;

    //database row ID of the Class
    private long rowID;

    //argument for editing an existing Class
    private Bundle classInfoBundle;

    //EditText for Class Information
    private EditText classEditText;


    public AddEditClassFragment() {
        // Required empty public constructor
    }

    //the default methods a fragment overrides i.e the fragment lifecycle methods
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);

        //refer the listener variable declared above to the host activity when the fragment is attached
        listener = (AddEditClassFragmentListener) activity;
    }

    @Override
    public void onDetach(){
        super.onDetach();

        //remove the AddEditClassFragmentListener when the fragment is detached and set the listener variable to null
        listener = null;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        //indicate that the fragment be retained rather than recreated when the HomeActivity is recreated
        //probably on a configuration change
        setRetainInstance(true);

        //has options menu items
        setHasOptionsMenu(true);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_edit_class, container, false);
        //get reference to the GUI views
        classEditText = (EditText) view.findViewById(R.id.classEditText);

        //Get Bundle of arguments (if any)
        //null if creating a new class and not null if editing an existing class
        classInfoBundle = getArguments();

        //Editing an existing class
        if (classInfoBundle != null){
            //read arguments out of the Bundle
            rowID = classInfoBundle.getLong(HomeActivity.ROW_ID);
            classEditText.setText(classInfoBundle.getString(FlashProvider.COLUMN_CLASS_NAME));
        }

        //set Save Class button event listener
        Button saveClassButton = (Button) view.findViewById(R.id.classSaveButton);
        saveClassButton.setOnClickListener(saveClassButtonClicked);

        return view;
    }

    //This AddEditClassFragmentListener contains the callback methods that the HomeActivity implements to
    // communicate with the fragment
    public interface AddEditClassFragmentListener {

        //called after a class has been successfully added or edited
        void onAddEditSuccess(long rowID);
    }

    //responds to the saveClassButton Click Event
    View.OnClickListener saveClassButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            //check if an entry was entered for the Class' Name
            if (classEditText.getText().toString().trim().length() != 0) {
                ContentValues values = new ContentValues();
                values.put(FlashProvider.COLUMN_ROWID, rowID);
                values.put(FlashProvider.COLUMN_CLASS_NAME, classEditText.getText().toString());
                values.put(FlashProvider.COLUMN_CLASS_SCORE, 200);

                if (classInfoBundle == null) {
                    Uri uri = getActivity().getContentResolver().insert(FlashProvider.CLASSES_CONTENT_URI, values);
                    rowID = ContentUris.parseId(uri);
                } else {
                    int count = getActivity().getContentResolver().update(ContentUris.withAppendedId(FlashProvider.CLASSES_CONTENT_URI, rowID),
                            values, null, null);
                    if (count != 1) {
                        throw new IllegalStateException("Unable to update " + rowID);
                    }
                }
                //hide the soft keyboard
                InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                listener.onAddEditSuccess(rowID);
            }

            //Class Name field is empty, display an error
            else {
                classEditText.setError("Class title must be entered");
            }

        }
    };
}

package com.daftneeks.julius.flashstudy;


import android.app.Activity;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
//import android.app.Fragment;
//import android.app.LoaderManager;
//import android.content.CursorLoader;
//import android.content.Loader;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


/**
 * A simple {@link Fragment} subclass.
 */
public class CardDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    //we declare an instance variable to refer to the HomeActivity in this fragment
    //i.e it refers to the object that implements the interface
    private CardDetailsFragmentListener listener;

    TextView cardQuestion;
    Button flipCardButton;
    ImageView cardImage;
    String picPath;

    String answer;

    //database rowID of the card
    private long rowID;
    static long card_ID;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                ContentUris.withAppendedId(FlashProvider.CARDS_CONTENT_URI, rowID), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        answer = data.getString(data.getColumnIndex(FlashProvider.CARDS_COLUMN_ANSWER));

        picPath = data.getString(data.getColumnIndex(FlashProvider.CARDS_COLUMN_IMAGE_PATH));

        cardImage.setImageBitmap(getCardImage(picPath));
        cardQuestion.setText(data.getString(data.getColumnIndex(FlashProvider.CARDS_COLUMN_QUESTION)) + "?");


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public interface CardDetailsFragmentListener {

        //called when the fragment launches to update the action bar title
        void setActionBarTitle(String title);

        //called when the user wants to flip the card
        void onFlipCard(long rowID, String answer, boolean check);

        //called when the user touches the delete action button
        void onCardDeleted();

    }


    public CardDetailsFragment() {
        // Required empty public constructor
    }

    //the default methods a fragment overrides i.e the fragment lifecycle methods
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);


        //refer the listener variable declared above to the host activity when the fragment is attached
        listener = (CardDetailsFragmentListener) activity;
    }

    @Override
    public void onDetach(){
        super.onDetach();

        //remove the CardDetailsFragmentListener when the fragment is detached and set the listener variable to null
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

        //if CardDetailsFragment is being restored, get the saved rowID
        if (savedInstanceState != null){
            rowID = savedInstanceState.getLong(HomeActivity.ROW_ID);
            card_ID = rowID;
        }
        else {
            // get Bundle of arguments then extract the rowID
            Bundle argument = getArguments();
            if (argument != null){
                rowID = argument.getLong(HomeActivity.ROW_ID);
                card_ID = rowID;
            }
        }

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_card_details, container, false);


        cardQuestion = (TextView) view.findViewById(R.id.cardQuestion);
        cardImage = (ImageView) view.findViewById(R.id.cardImageView);
        flipCardButton = (Button) view.findViewById(R.id.flipCardButton);

        flipCardButton.setOnClickListener(flipCardListener);

        getLoaderManager().initLoader(0, null, this);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        listener.setActionBarTitle(ClassDetailsFragment.className);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(HomeActivity.ROW_ID, rowID);
    }


    private Bitmap getCardImage(String path) {

        Bitmap bitmap = null;
        try {
            File file = new File(path);
            bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    View.OnClickListener flipCardListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            listener.onFlipCard(rowID, answer, false);
        }
    };


    //displays this Fragment's menu items
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.card_details_fragment_menu, menu);
    }

    //Handles choice from OptionsMenu
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch (menuItem.getItemId()){

            case R.id.action_delete_card:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Are you sure you want to delete this Card? \n This cannot be Undone.")
                        .setTitle("CONFIRM!")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                getActivity().getContentResolver().delete(
                                        ContentUris.withAppendedId(FlashProvider.CARDS_CONTENT_URI, rowID),
                                        null, null);
                                String delPic = deleteImage(picPath);
                                listener.onCardDeleted();
                                Toast.makeText(getActivity(), delPic + " was deleted!", Toast.LENGTH_SHORT).show();
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

    private String deleteImage(String picPath) {
        if (picPath != null && picPath.length() != 0){
            File picFilePath = new File(picPath);
            picFilePath.delete();
        }
        return picPath;
    }

}

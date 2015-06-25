package com.daftneeks.julius.flashstudy;


import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


/**
 * A simple {@link Fragment} subclass.
 */
public class QuizFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    //we declare an instance variable to refer to the HomeActivity in this fragment
    //i.e it refers to the object that implements the interface
    private QuizFragmentListener listener;

    TextView cardQuestion;
    Button flipCardButton, doneStudying;
    ImageView cardImage;
    String picPath;

    String answer;

    boolean runningQuiz = false;

    //database rowID of the card and its class
    private long rowID;
    static long card_ID;
    int classID;

    public QuizFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //indicate that the fragment be retained rather than recreated when the HomeActivity is recreated
        //probably on a configuration change
        setRetainInstance(true);

        //has options menu items
        setHasOptionsMenu(true);

        //if QuizFragment is being restored, get the saved rowID
        if (savedInstanceState != null){
            rowID = savedInstanceState.getLong(HomeActivity.ROW_ID);
            card_ID = rowID;
            runningQuiz = savedInstanceState.getBoolean(HomeActivity.RUNNING_QUIZ_KEY);
        }

        Bundle bundle = getArguments();
        if (bundle != null){
            runningQuiz = bundle.getBoolean(HomeActivity.RUNNING_QUIZ_KEY);
        }

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);

        cardQuestion = (TextView) view.findViewById(R.id.cardQuestion);
        cardImage = (ImageView) view.findViewById(R.id.cardImageView);
        flipCardButton = (Button) view.findViewById(R.id.flipCardButton);
        doneStudying = (Button) view.findViewById(R.id.doneStudyingButton);

        flipCardButton.setOnClickListener(flipCardListener);
        doneStudying.setOnClickListener(doneStudyingListener);

        getLoaderManager().initLoader(0, null, this);


        return view;
    }


    public interface QuizFragmentListener {

        //called when the fragment launches to update the action bar title
        void setActionBarTitle(String title);

        //called when the user wants to flip the card
        void onFlipCard(long rowID, String answer, boolean check);

        //called when the user stops studying
        void saveScore(int classID);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader;
        cursorLoader = new CursorLoader(getActivity(), FlashProvider.QUIZ_CONTENT_URI, null, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        data.moveToFirst();

        answer = data.getString(data.getColumnIndex(FlashProvider.CARDS_COLUMN_ANSWER));

        picPath = data.getString(data.getColumnIndex(FlashProvider.CARDS_COLUMN_IMAGE_PATH));

        cardImage.setImageBitmap(getCardImage(picPath));

        cardQuestion.setText(data.getString(data.getColumnIndex(FlashProvider.CARDS_COLUMN_QUESTION)) + "?");

        classID = data.getInt(data.getColumnIndex(FlashProvider.CARDS_CLASSES_COLUMN));

        //data.close();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    //the default methods a fragment overrides i.e the fragment lifecycle methods
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        //refer the listener variable declared above to the host activity when the fragment is attached
        listener = (QuizFragmentListener) activity;
    }

    @Override
    public void onDetach(){
        super.onDetach();
        //remove the QuizFragmentListener when the fragment is detached and set the listener variable to null

        listener = null;
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
        outState.putBoolean(HomeActivity.RUNNING_QUIZ_KEY, runningQuiz);
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
            listener.onFlipCard(rowID, answer, true);
        }
    };

    View.OnClickListener doneStudyingListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            listener.saveScore(classID);
        }
    };
}

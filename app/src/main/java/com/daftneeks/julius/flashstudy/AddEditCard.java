package com.daftneeks.julius.flashstudy;


import android.app.Activity;
//import android.app.AlertDialog;
//import android.app.Dialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
//import android.app.DialogFragment;
//import android.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddEditCard extends Fragment {

    private ImageView imageView;
    int RESULT_LOAD_IMAGE = 1;
    private EditText questionEditText, answerEditText;


    //variable to reference the HomeActivity
    private AddEditCardListener listener;

    //database row ID of the Card
    private long rowID;

    //database row ID of the class
    private long class_rowID;
    static long cROW_ID;

    private Bitmap bitmap;


    public AddEditCard() {
        // Required empty public constructor
    }

    //This AddEditCardListener contains the callback methods that the HomeActivity implements to
    // communicate with the fragment
    public interface AddEditCardListener {

        //called after a card has been successfully added or edited
        void onAddEditCardSuccess(long rowID);
    }

    //the default methods a fragment overrides i.e the fragment lifecycle methods
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);

        //refer the listener variable declared above to the host activity when the fragment is attached
        listener = (AddEditCardListener) activity;
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
        // Inflate the layout for this fragment
         View view = inflater.inflate(R.layout.fragment_add_edit_card, container, false);

        ImageButton selectImageButton = (ImageButton) view.findViewById(R.id.selectImageButton);
        imageView = (ImageView) view.findViewById(R.id.imageView);
        questionEditText = (EditText) view.findViewById(R.id.questionEditText);
        answerEditText = (EditText) view.findViewById(R.id.answerEditText);

        Button makeCardButton = (Button) view.findViewById(R.id.makeCardButton);
        selectImageButton.setOnClickListener(imageButtonListener);
        makeCardButton.setOnClickListener(cardButtonListener);

        //get Bundle of arguments to get the rowID of the class
        Bundle classArg = getArguments();
        class_rowID = classArg.getLong(HomeActivity.CLASS_ROW_ID);

        return view;
    }

    View.OnClickListener imageButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            Intent i = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            startActivityForResult(i, RESULT_LOAD_IMAGE);
        }

    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && null != data) {
            Uri selectedImageUri = data.getData();
            imageView.setImageURI(selectedImageUri);

            BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();

            bitmap = bitmapDrawable.getBitmap();

            imageView.setVisibility(View.VISIBLE);

        }


    }

    View.OnClickListener cardButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {

            //check if an entry was entered for the question, answer and image
            if (questionEditText.getText().toString().trim().length() != 0 && answerEditText.getText().toString().trim().length() != 0
                     && imageView.getVisibility() == View.VISIBLE){
                ContentValues cardValues = new ContentValues();
                    cardValues.put(FlashProvider.CARDS_COLUMN_ROWID, rowID);
                    cardValues.put(FlashProvider.CARDS_COLUMN_QUESTION, questionEditText.getText().toString());
                    cardValues.put(FlashProvider.CARDS_COLUMN_ANSWER, answerEditText.getText().toString());
                    cardValues.put(FlashProvider.CARDS_CLASSES_COLUMN, class_rowID);

                    cROW_ID = class_rowID;

                    Uri uri = getActivity().getContentResolver().insert(FlashProvider.CARDS_CONTENT_URI, cardValues);
                    rowID = ContentUris.parseId(uri);

                    ContentValues withImage;
                    withImage = addImage(rowID, bitmap);

                    getActivity().getContentResolver().update(
                            ContentUris.withAppendedId(FlashProvider.CARDS_IMAGE_CONTENT_URI, rowID), withImage, null, null);

                listener.onAddEditCardSuccess(rowID);
            }

            else {

                if (questionEditText.getText().toString().trim().length() < 7){
                    questionEditText.setError("Question is too short");
                }

                else if (answerEditText.getText().toString().trim().length() == 0) {
                    answerEditText.setError("Answer cannot be Empty");
                }

                else if (imageView.getVisibility() != View.VISIBLE) {
                    DialogFragment errorSavingCard = new DialogFragment() {
                        @Override
                        public Dialog onCreateDialog(Bundle savedInstanceState) {
                            //declare the AlertDialog
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage(R.string.error_message_image);
                            builder.setPositiveButton(R.string.ok, null);
                            return builder.create();
                        }
                    };
                    errorSavingCard.show(getFragmentManager(), "Error saving Card");
                }

            }

        }

    };

    private ContentValues addImage(long id, Bitmap bitmapImage){
        File imageDir = getActivity().getDir("images", Context.MODE_PRIVATE);
        File imageFilePath = new File(imageDir, id + ".png");
        ContentValues img = new ContentValues();
        img.put(FlashProvider.CARDS_COLUMN_IMAGE_PATH, imageFilePath.toString());

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(imageFilePath);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        }
        catch (Exception e) {
            Log.i("DATABASE", "Problem Uploading Picture", e);
        }

        return img;
    }


}

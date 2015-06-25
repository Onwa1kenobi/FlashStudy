package com.daftneeks.julius.flashstudy;


import android.os.Bundle;
//import android.app.Fragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class CardBackFragment extends Fragment {

    ImageButton correctButton, wrongButton;

    TextView answerText;


    public CardBackFragment() {
        // Required empty public constructor
    }

    public interface CardBackFragmentListener {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_card_back, container, false);

        correctButton = (ImageButton) view.findViewById(R.id.correctImageButton);
        wrongButton = (ImageButton) view.findViewById(R.id.wrongImageButton);

        Bundle bundle = getArguments();

        answerText = (TextView) view.findViewById(R.id.cardAnswerTextView);

        answerText.setText(bundle.getString(HomeActivity.ANSWER_KEY));

        return view;
    }
}

package com.daftneeks.julius.flashstudy;


import android.app.Activity;
import android.os.Bundle;
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
public class SolutionFragment extends Fragment {

    //we declare an instance variable to refer to the HomeActivity in this fragment
    //i.e it refers to the object that implements the interface
    private SolutionFragmentListener listener;

    ImageButton correctButton, wrongButton;
    TextView answerText;

    int correct, wrong;

    public SolutionFragment() {
        // Required empty public constructor
    }

    public interface SolutionFragmentListener {

        //called when the user touches any of the choice buttons
        void onChoiceMade(boolean runningQuiz, int correct, int wrong);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_solution, container, false);

        correctButton = (ImageButton) view.findViewById(R.id.correctImageButton);
        wrongButton = (ImageButton) view.findViewById(R.id.wrongImageButton);

        correctButton.setOnClickListener(correctButtonListener);
        wrongButton.setOnClickListener(wrongButtonListener);

        Bundle bundle = getArguments();

        answerText = (TextView) view.findViewById(R.id.cardAnswerTextView);
        answerText.setText(bundle.getString(HomeActivity.ANSWER_KEY));

        correct = bundle.getInt(HomeActivity.CORRECT_KEY);
        wrong = bundle.getInt(HomeActivity.WRONG_KEY);

        return view;
    }

    //the default methods a fragment overrides i.e the fragment lifecycle methods
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);


        //refer the listener variable declared above to the host activity when the fragment is attached
        listener = (SolutionFragmentListener) activity;
    }

    @Override
    public void onDetach(){
        super.onDetach();

        //remove the SolutionFragmentListener when the fragment is detached and set the listener variable to null
        listener = null;
    }

    View.OnClickListener correctButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            correct +=1;
            listener.onChoiceMade(true, correct, wrong);
        }
    };

    View.OnClickListener wrongButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            wrong += 1;
            listener.onChoiceMade(true, correct, wrong);
        }
    };
}

package com.daftneeks.julius.flashstudy;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.ActivityInfo;
//import android.app.FragmentTransaction;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class HomeActivity extends ActionBarActivity implements KnapSackFragment.KnapSackFragmentListener,
        ClassDetailsFragment.ClassDetailsFragmentListener, AddEditClassFragment.AddEditClassFragmentListener,
        AddEditCard.AddEditCardListener, CardDetailsFragment.CardDetailsFragmentListener,
        CardBackFragment.CardBackFragmentListener, QuizFragment.QuizFragmentListener, SolutionFragment.SolutionFragmentListener {

    public static final String ROW_ID = "row_id";
    public static final String CLASS_NAME = "class_name";
    public static final String CLASS_ROW_ID = "class_row_id";
    public static final String ANSWER_KEY = "answer_key";
    public static final String RUNNING_QUIZ_KEY = "running_quiz";
    public static final String CORRECT_KEY = "correct_key";
    public static final String WRONG_KEY = "wrong_key";

    int correct, wrong;


    KnapSackFragment knapSackFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //if activity is being restored, this makes the GUI return, instead of recreating it
        if (savedInstanceState != null)
            return;

        //This checks if the view with id phoneFragment is available in the layout,
        // hence telling us the device is a phone and therefore
        //we need to populate the view with the KnapSackFragment
        if (findViewById(R.id.phoneFragment) != null) {

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            knapSackFragment = new KnapSackFragment();
            //start the fragment transaction
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    //inflate the fragment_container frameLayout with a new ContactListFragment() fragment
                    transaction.add(R.id.phoneFragment, knapSackFragment);
                    //displays the fragment
                    transaction.commit();
        }
    }


    public void setActionBarTitle(String title){
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(title);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (findViewById(R.id.phoneFragment) != null) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        //To check if knapSackFragment is null here, if it is, then app is running on a tablet
        //i.e the if(findViewById...) method did not run in the onCreate method
        else if (findViewById(R.id.rightPaneContainer) != null){
            knapSackFragment = (KnapSackFragment) getSupportFragmentManager().findFragmentById(R.id.sackFragment);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            getSupportActionBar().setTitle("KnapSack");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }


    //onClassSelected method that tells the HomeActivity that a class was selected from the
    //KnapSackFragment using its listener interface
    @Override
    public void onClassSelected(long rowID, String item){
            //check if it is running on a phone
        if (findViewById(R.id.phoneFragment) != null){
            //call displayClass method to replace the KnapSackFragment with the
            // ClassDetailsFragment in the phoneFragment fragmentLayout
            displayClass(rowID, R.id.phoneFragment, item);
        }
        //tablet
        else {
            //remove top of back stack for a tablet
            getSupportFragmentManager().popBackStack();
            //call displayClass method to replace the whatever is in the right_pane_container with
            // the ClassDetailsFragment
            displayClass(rowID, R.id.rightPaneContainer, item);
        }
    }

    //displayClass method that displays a selected class
    private void displayClass(long rowID, int viewID, String className){


        ClassDetailsFragment classDetailsFragment = new ClassDetailsFragment();

        //Specify rowID as an argument and then passing it to a fragment (i.e ClassDetailsFragment)
        //by placing it in a Bundle of key-value pairs
        // this is done to pass the selected class' rowID to let the ClassDetailsFragment know what class to get from the database
        Bundle fragArguments = new Bundle();
        //store a key�value pair containing the ROW_ID (a String) as the key and the rowID (a long) as the value
        fragArguments.putLong(ROW_ID, rowID);
        fragArguments.putString(CLASS_NAME, className);
        //passes the Bundle to the Fragment�s setArguments method
        // the Fragment can then extract the information from the Bundle
        classDetailsFragment.setArguments(fragArguments);

        //we then use a fragment transaction to display the ClassDetailsFragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //replace whatever view with the id in the first argument with the classDetailsFragment declared locally above
        transaction.replace(viewID, classDetailsFragment);
        //adds the classDetailsFragment to the backStack so that when the back button is pressed,
        //the fragment is removed from the backStack. also, the HomeActivity can programmatically pop the
        //fragment from the backStack
        transaction.addToBackStack(null);
        //display the fragment
        transaction.commit();
    }

    // display the AddEditClassFragment to add a new contact
    @Override
    public void onAddClass(){
        //Phone
        if (findViewById(R.id.phoneFragment) != null){
            displayAddEditFragment(R.id.phoneFragment, null);
        }
        //tablet
        else
            displayAddEditFragment(R.id.rightPaneContainer, null);
    }

    // display fragment(AddEditClassFragment) for adding a new or editing an existing class
    private void displayAddEditFragment(int viewID, Bundle fragArguments) {

        AddEditClassFragment addEditClassFragment = new AddEditClassFragment();

        //if Bundle is null, then a new class is being created. But if it is not null, an existing class is being edited
        if (fragArguments != null)
            addEditClassFragment.setArguments(fragArguments);

        //we then use a fragment transaction to display the AddEditClassFragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //replace whatever view with the id in the first argument with the addEditClassFragment declared locally above
        transaction.replace(viewID, addEditClassFragment);
        //adds the addEditClassFragment to the backStack so that when the back button is pressed,
        //the fragment is removed from the backStack. also, the HomeActivity can programmatically pop the
        //fragment from the backStack
        transaction.addToBackStack(null);
        //display the fragment
        transaction.commit();
    }

    //Return to the KnapSackFragment when the displayed class is deleted
    @Override
    public void onClassDeleted(){
        //removes the ClassDetailsFragment from the top of backStack
        getSupportFragmentManager().popBackStack();

        if (findViewById(R.id.phoneFragment) == null){
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void saveScore(int classID) {
        double totalStudied = correct + wrong;

        if (totalStudied == 0){
            getSupportFragmentManager().popBackStack();
        }
        else {
        double score = ((correct / totalStudied) * 100);
        ContentValues values = new ContentValues();
        values.put(FlashProvider.COLUMN_CLASS_SCORE, score);
        getApplicationContext().getContentResolver().update(
                ContentUris.withAppendedId(FlashProvider.CLASS_SCORE_CONTENT_URI, (long) classID), values, null, null);
            getSupportFragmentManager().popBackStack();
        }
    }


    // displays the AddEditClassFragment to edit an existing class
///////    @Override
    public void onEditClass(Bundle fragArguments){

        //Phone
        if (findViewById(R.id.phoneFragment) != null){
            displayAddEditFragment(R.id.phoneFragment, fragArguments);
        }
        //Tablet
        else
            displayAddEditFragment(R.id.rightPaneContainer, fragArguments);
    }

    //update GUI if new class is added or an existing one updated
    @Override
    public void onAddEditSuccess(long rowID){
        //removes the AddEditClassFragment from the top of backStack
        getSupportFragmentManager().popBackStack();

        //Tablet
        if (findViewById(R.id.phoneFragment) == null){
            //pops the ClassDetailsFragment from the backStack again (if there is one)
            getSupportFragmentManager().popBackStack();
            //On tablet, we display the class that was just added or edited
            displayClass(rowID, R.id.rightPaneContainer, null);
        }
    }

    //update GUI if new class is added or an existing one updated
    @Override
    public void onAddEditCardSuccess(long rowID){
        //removes the AddEditCard Fragment from the top of backStack
        getSupportFragmentManager().popBackStack();

        //Tablet
        if (findViewById(R.id.phoneFragment) == null){
            //pops the CardDetailsFragment from the backStack again (if there is one)
            getSupportFragmentManager().popBackStack();
            //On tablet, we display the card that was just added or edited
            displayCard(rowID, R.id.rightPaneContainer);
        }
    }

    @Override
    public void onCardSelected(long rowID) {
        //check if it is running on a phone
        if (findViewById(R.id.phoneFragment) != null){
            //call displayCard method to replace the ClassDetailsFragment with the
            // CardDetailsFragment in the phoneFragment fragmentLayout
            displayCard(rowID, R.id.phoneFragment);
        }
        //tablet
        else {
            //remove top of back stack for a tablet
            getSupportFragmentManager().popBackStack();
            //call displayCard method to replace the whatever is in the right_pane_container with
            // the CardDetailsFragment
            displayCard(rowID, R.id.rightPaneContainer);
        }
    }

    //Return to the ClassDetailsFragment when the displayed class is deleted
    @Override
    public void onCardDeleted(){
        //removes the CardDetailsFragment from the top of backStack
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onAddCard(long classRowID) {
        //Phone
        if (findViewById(R.id.phoneFragment) != null){
            displayAddEditCard(R.id.phoneFragment, classRowID, null);
        }
        //tablet
        else
            displayAddEditCard(R.id.rightPaneContainer, classRowID, null);

    }

    // display fragment(AddEditCard) for adding a new or editing an existing card
    private void displayAddEditCard(int viewID, long classRowID, Bundle fragArguments) {

        AddEditCard addEditCard = new AddEditCard();

        //if Bundle is null, then a new card is being created. But if it is not null, an existing card is being edited
        if (fragArguments != null){
            fragArguments.putLong(CLASS_ROW_ID, classRowID);
            addEditCard.setArguments(fragArguments);
        }
        else {
            Bundle arg = new Bundle();
            arg.putLong(CLASS_ROW_ID, classRowID);
            addEditCard.setArguments(arg);
        }

        //we then use a fragment transaction to display the AddEditCard
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //replace whatever view with the id in the first argument with the addEditCard declared locally above
        transaction.replace(viewID, addEditCard);
        //adds the addEditCard to the backStack so that when the back button is pressed,
        //the fragment is removed from the backStack. also, the HomeActivity can programmatically pop the
        //fragment from the backStack
        transaction.addToBackStack(null);
        //display the fragment
        transaction.commit();
    }

    //displayCard method that displays a selected card
    private void displayCard(long rowID, int viewID){

        CardDetailsFragment cardDetailsFragment = new CardDetailsFragment();

        //Specify rowID as an argument and then passing it to a fragment (i.e CardDetailsFragment)
        //by placing it in a Bundle of key-value pairs
        // this is done to pass the selected cards' rowID to let the CardDetailsFragment know what card to get from the database
        Bundle fragArguments = new Bundle();
        //store a key�value pair containing the ROW_ID (a String) as the key and the rowID (a long) as the value
        fragArguments.putLong(ROW_ID, rowID);
        //passes the Bundle to the Fragment�s setArguments method
        // the Fragment can then extract the information from the Bundle
        cardDetailsFragment.setArguments(fragArguments);

        //we then use a fragment transaction to display the CardDetailsFragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //replace whatever view with the id in the first argument with the cardDetailsFragment declared locally above
        transaction.replace(viewID, cardDetailsFragment);
        //adds the cardDetailsFragment to the backStack so that when the back button is pressed,
        //the fragment is removed from the backStack. also, the HomeActivity can programmatically pop the
        //fragment from the backStack
        transaction.addToBackStack(null);
        //display the fragment
        transaction.commit();
    }

    @Override
    public void onRunQuiz(long classRowID) {

        correct = 0;
        wrong = 0;

        //Phone
        if (findViewById(R.id.phoneFragment) != null){
            displayQuiz(R.id.phoneFragment, classRowID);
        }
        //tablet
        else
            displayQuiz(R.id.rightPaneContainer, classRowID);

    }

    private void displayQuiz(int viewID, long classRowID) {
        QuizFragment quizFragment = new QuizFragment();

        Bundle bundle = new Bundle();
        bundle.putLong(ROW_ID, classRowID);

        //quizFragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(viewID, quizFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onFlipCard(long rowID, String answerText, boolean check) {
        //Phone
        if (findViewById(R.id.phoneFragment) != null){
            flipCard(rowID, R.id.phoneFragment, answerText, check);
        }
        //tablet
        else
            flipCard(rowID, R.id.rightPaneContainer, answerText, check);

    }

    @Override
    public void onChoiceMade(boolean runningQuiz, int mCorrect, int mWrong){

        correct = mCorrect;
        wrong = mWrong;

        //removes the SolutionFragment from the top of backStack
        getSupportFragmentManager().popBackStack();

        //phone
        if (findViewById(R.id.phoneFragment) != null){
            getSupportFragmentManager().popBackStack();

            flipNewCard(runningQuiz, R.id.phoneFragment);
        }
        //tablet
        else {
            getSupportFragmentManager().popBackStack();

            flipNewCard(runningQuiz, R.id.rightPaneContainer);
        }
    }

    private void flipNewCard(boolean runningQuiz, int viewID) {
        QuizFragment quizFragment = new QuizFragment();

        Bundle bundle = new Bundle();
        bundle.putBoolean(RUNNING_QUIZ_KEY, runningQuiz);

        quizFragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right,
                R.anim.card_slide_in_right, R.anim.card_slide_out_left);
        transaction.replace(viewID, quizFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void flipCard(long id, int viewID, String answerText, boolean check) {

        CardBackFragment cardBackFragment = new CardBackFragment();
        SolutionFragment solutionFragment = new SolutionFragment();

        Bundle args = new Bundle();

        args.putLong(ROW_ID, id);
        args.putString(ANSWER_KEY, answerText);

        if (!check){
            cardBackFragment.setArguments(args);

            // Flip to the back.

            // Create and commit a new fragment transaction that adds the fragment for the back of
            // the card, uses custom animations, and is part of the fragment manager's back stack.

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            // Replace the default fragment animations with animator resources representing
            // rotations when switching to the back of the card, as well as animator
            // resources representing rotations when flipping back to the front (e.g. when
            // the system Back button is pressed).
//          transaction.setCustomAnimations(R.anim.card_flip_left_in, R.anim.card_flip_left_out, R.anim.card_flip_right_in, R.anim.card_flip_right_out);

            transaction.setCustomAnimations(R.anim.card_slide_in_right, R.anim.card_slide_out_left,
                    android.R.anim.slide_in_left, android.R.anim.slide_out_right);

            // Replace any fragments currently in the container view with a fragment
            // representing the next page (indicated by the just-incremented currentPage variable).
            transaction.replace(viewID, cardBackFragment);

            // Add this transaction to the back stack, allowing users to press Back
            // to get to the front of the card.
            transaction.addToBackStack(null);
            // Commit the transaction.
            transaction.commit();
        }

        else {
            args.putInt(CORRECT_KEY, correct);
            args.putInt(WRONG_KEY, wrong);
            solutionFragment.setArguments(args);

            // Flip to the back.

            // Create and commit a new fragment transaction that adds the fragment for the back of
            // the card, uses custom animations, and is part of the fragment manager's back stack.

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            // Replace the default fragment animations with animator resources representing
            // rotations when switching to the back of the card, as well as animator
            // resources representing rotations when flipping back to the front (e.g. when
            // the system Back button is pressed).
//        transaction.setCustomAnimations(R.anim.card_flip_left_in, R.anim.card_flip_left_out, R.anim.card_flip_right_in, R.anim.card_flip_right_out);

            transaction.setCustomAnimations(R.anim.card_slide_in_right, R.anim.card_slide_out_left,
                    android.R.anim.slide_in_left, android.R.anim.slide_out_right);

            // Replace any fragments currently in the container view with a fragment
            // representing the next page (indicated by the just-incremented currentPage variable).
            transaction.replace(viewID, solutionFragment);

            // Add this transaction to the back stack, allowing users to press Back
            // to get to the front of the card.
            transaction.addToBackStack(null);
            // Commit the transaction.
            transaction.commit();
        }


    }
}

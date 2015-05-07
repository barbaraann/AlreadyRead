package com.example.barbara.alreadyread;


import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;


public class MainActivity extends Activity
        implements BookListFragment.BookListFragmentListener,
        DetailsFragment.DetailsFragmentListener,
        AddEditFragment.AddEditFragmentListener {

    //keys for storing rowID in Bundle passed to a fragment
    public static final String ROW_ID = "row_id";

    BookListFragment bookListFragment; //displays book list

    //display bookListFragment when MainActivity first loads
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //return if Activity is being restored
        if (savedInstanceState != null)
            return;
        //check whether layout contains fragmentContainer
        //BookListFragment is always displayed
        if (findViewById(R.id.fragmentContainer) != null) {
            //create BookListFragment
            bookListFragment = new BookListFragment();

            //add the fragment to the FrameLayout
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(R.id.fragmentContainer, bookListFragment);
            transaction.commit();
        }
    }
    // called when MainActivity resumes
    @Override
    protected void onResume()
    {
        super.onResume();

        // if bookListFragment is null, activity running on tablet,
        // so get reference from FragmentManager
        if (bookListFragment == null)
        {
            bookListFragment =
                    (BookListFragment) getFragmentManager().findFragmentById(
                            R.id.contactListFragment);
        }
    }
            //display DetailsFragment for selected book
           @Override
            public void onBookSelected(long rowID) {
               if (findViewById(R.id.fragmentContainer) != null)//phone
                   displayBook(rowID, R.id.fragmentContainer);
               else//tablet
               {
                   getFragmentManager().popBackStack();//removes top of back stack
                   displayBook(rowID, R.id.rightPaneContainer);
               }
           }
    //display a book
    private void displayBook(long rowID, int viewID) {
        DetailsFragment detailsFragment = new DetailsFragment();

        //specify rowID as an argument to the DetailsFragment
        Bundle arguments = new Bundle();
        arguments.putLong(ROW_ID, rowID);
        detailsFragment.setArguments(arguments);

        //use a FragmentTransaction to display the DetailsFragment
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(viewID, detailsFragment);
        transaction.addToBackStack(null);
        transaction.commit();  //causes DetailsFragment to display
    }
    //display the AddEditFragment to add a new book
    @Override
    public void onAddBook(){
        if(findViewById(R.id.fragmentContainer) != null)
            if (findViewById(R.id.fragmentContainer) != null)
                displayAddEditFragment(R.id.fragmentContainer, null);
            else
                displayAddEditFragment(R.id.rightPaneContainer, null);
    }

    // display fragment for adding a new or editing an existing contact
    private void displayAddEditFragment(int viewID, Bundle arguments)
    {
        AddEditFragment addEditFragment = new AddEditFragment();

        if (arguments != null) // editing existing contact
            addEditFragment.setArguments(arguments);

        // use a FragmentTransaction to display the AddEditFragment
        FragmentTransaction transaction =
                getFragmentManager().beginTransaction();
        transaction.replace(viewID, addEditFragment);
        transaction.addToBackStack(null);
        transaction.commit(); // causes AddEditFragment to display
    }

    //return to contact list when displayed contact deleted
    @Override
    public void onBookDeleted()
    {
        getFragmentManager().popBackStack();  //remove top of back stack

        if (findViewById(R.id.fragmentContainer) == null) // tablet
            bookListFragment.updateBookList();
    }

    // display the AddEditFragment to edit an existing book
    @Override
    public void onEditBook(Bundle arguments)
    {
        if (findViewById(R.id.fragmentContainer) != null) // phone
            displayAddEditFragment(R.id.fragmentContainer, arguments);
        else // tablet
            displayAddEditFragment(R.id.rightPaneContainer, arguments);
    }

    //update GUI after new book or updated book saved
    @Override
    public void onAddEditCompleted(long rowID){
        getFragmentManager().popBackStack();  //removes top of back stack

        if(findViewById(R.id.fragmentContainer) == null) //tablet
        {
            getFragmentManager().popBackStack(); //removes top of back stack
            bookListFragment.updateBookList(); //refresh books
            //on tablet, display book that was just added or edited
            displayBook(rowID, R.id.rightPaneContainer);
        }
    }
}



























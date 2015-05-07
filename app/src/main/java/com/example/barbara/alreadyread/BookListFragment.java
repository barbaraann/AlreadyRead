/**
 * BookListFragment.java
 * Displays the list of books
 * Created by Barbara on 5/2/2015.
 */
package com.example.barbara.alreadyread;

import android.app.Activity;
import android.app.ListFragment;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class BookListFragment extends ListFragment{

    //callback methods implemented by MainActivity
    public interface BookListFragmentListener{

        //called when user selects a book
        public void  onBookSelected(long rowID);

        //called when user decides to add a book
        public void onAddBook();
    }

    private BookListFragmentListener listener;
    private ListView bookListView;  //the ListActivity's ListView
    private CursorAdapter bookAdapter;  //adapter for ListView

    //set BookListFragmentListener when fragment attached

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (BookListFragmentListener) activity;
            }
    //remove BookListFragmentListener when Fragment detached

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    //called after View is created

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRetainInstance(true);//save fragment across config changes
        setHasOptionsMenu(true);//this fragment has menu items to display

        // set text to display when there are no books
        setEmptyText(getResources().getString(R.string.no_books));

        // get ListView reference and configure ListView
        bookListView = getListView();
        bookListView.setOnItemClickListener(viewBookListener);
        bookListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        //map each book's title to a TextView in the ListView layout
        String[] from = new String[] { "bookTitle" };
        int[] to = new int[] { android.R.id.text1 };
        bookAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_1, null, from, to, 0);
        setListAdapter(bookAdapter); // set adapter that supplies data
    }
    //responds to the user touching a book's title in the ListView
    OnItemClickListener viewBookListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            listener.onBookSelected(id); //pass selection to MainActivity
        }
    };  //end viewBookListener
    //when fragment resumes, use a GetBooksTask to load books

    @Override
    public void onResume() {
        super.onResume();
        new GetBooksTask().execute((Object[]) null);
    }
    //performs database query outside GUI thread
    private class GetBooksTask extends AsyncTask<Object, Object, Cursor>{
        DatabaseConnector databaseConnector = new DatabaseConnector(getActivity());

        //open database and return Cursor for all books
        @Override
        protected Cursor doInBackground(Object... params){
            databaseConnector.open();
            return databaseConnector.getAllBooks();
        }

        //use the Cursor returned from the doInBackground method

        @Override
        protected void onPostExecute(Cursor result) {
            bookAdapter.changeCursor(result); //set the adapter's Cursor
            databaseConnector.close();
        }
    } //end class GetBooksTask

    // when fragment stops, close Cursor and remove from bookAdapter

    @Override
    public void onStop() {
        Cursor cursor = bookAdapter.getCursor();  //get current cursor
        bookAdapter.changeCursor(null);  //adapter now has no cursor

        if (cursor != null)
            cursor.close();  //release the Cursor's resources
        super.onStop();
    }

    //display this fragment's menu items

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_book_list_menu, menu);
//        menu.add(0,R.id.action_add, 0, getResources().getString(R.string.menuitem_add))
//                .setIcon(R.drawable.ic_menu_add)
//                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    //handle choice from options menu

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add:
                listener.onAddBook();
                return true;
        }

        return super.onOptionsItemSelected(item);  //call super's method
    }

    //update data set
    public void updateBookList(){

        new GetBooksTask().execute((Object[]) null);
    }
} //end class BookListFragment



























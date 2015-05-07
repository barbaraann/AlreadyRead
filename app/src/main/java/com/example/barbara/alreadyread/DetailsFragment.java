package com.example.barbara.alreadyread;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;


public class DetailsFragment extends Fragment {

    //callback methods implemented by MainActivity
    public interface DetailsFragmentListener {
        //called when a book is deleted
        public void onBookDeleted();

        //called to pass Bundle of book's info for editing
        public void onEditBook(Bundle arguments);
    }
    private DetailsFragmentListener listener;

    private long rowID = -1; //selected book's rowID
    private TextView bookTitleTextView; //display book's title
    private TextView authorTextView; //display a book's author
    private TextView seriesTextView; //display a series title
    private TextView orderInSeriesTextView; //display the order of a book within a series
    private CheckBox checkboxAlreadyRead; //display a check if the book has been read

    //set DetailsFragmentListener when fragment attached

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (DetailsFragmentListener) activity;
    }

        //remove DetailsFragmentListener when fragment detached

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

        //called when DetailsFragmentListener's view needs to be created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setRetainInstance(true); // save fragment across config changes

        // if DetailsFragment is being restored, get saved row ID
        if (savedInstanceState != null)
            rowID = savedInstanceState.getLong(MainActivity.ROW_ID);
        else
        {
            // get Bundle of arguments then extract the contact's row ID
            Bundle arguments = getArguments();

            if (arguments != null)
                rowID = arguments.getLong(MainActivity.ROW_ID);
        }

        //inflate DetailsFragment's Layout
        View view = inflater.inflate(R.layout.fragment_details, container, false);
        setHasOptionsMenu(true);  //this fragment has menu items to display

        //get the EditTexts
        bookTitleTextView = (TextView) view.findViewById(R.id.bookTitleTextView);
        authorTextView = (TextView) view.findViewById(R.id.authorTextView);
        seriesTextView = (TextView) view.findViewById(R.id.seriesTextView);
        orderInSeriesTextView = (TextView) view.findViewById(R.id.orderInSeriesTextView);
        checkboxAlreadyRead = (CheckBox) view.findViewById(R.id.checkboxAlreadyRead);
        return view;
    }
    //called when the DetailsFragment resumes


    @Override
    public void onResume() {
        super.onResume();
        new LoadBookTask().execute(rowID); //load book at rowID
            }
    //save currently displayed book's rowID

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(MainActivity.ROW_ID, rowID);
    }

    //display this fragment's menu items

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_details_menu, menu);
    }
    //handle menu item selections

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_edit:
                //create Bundle containing book data to edit
                Bundle arguments = new Bundle();
                arguments.putLong(MainActivity.ROW_ID, rowID);
                arguments.putCharSequence("bookTitle", bookTitleTextView.getText());
                arguments.putCharSequence("author", authorTextView.getText());
                arguments.putCharSequence("series", seriesTextView.getText());
                arguments.putCharSequence("orderInSeries", orderInSeriesTextView.getText());
                arguments.putBoolean("checkboxAlreadyRead", checkboxAlreadyRead.isChecked());
                listener.onEditBook(arguments);//pass Bundle to listener
                return true;

            case R.id.action_delete:
                deleteBook();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //performs database query outside GUI thread
    private class LoadBookTask extends AsyncTask<Long, Object, Cursor>{
        DatabaseConnector databaseConnector = new DatabaseConnector(getActivity());

        //open database and get Cursor representing specified book's data
        @Override
        protected Cursor doInBackground(Long... params)
        {
            databaseConnector.open();
            return databaseConnector.getOneBook(params[0]);
        }

        //use the Cursor returned from the doInBackground method

        @Override
        protected void onPostExecute(Cursor result) {
            super.onPostExecute(result);
            result.moveToFirst(); //move to the first item

            //get the column index for each data item
            int bookTitleIndex = result.getColumnIndex("bookTitle");
            int authorIndex = result.getColumnIndex("author");
            int seriesIndex = result.getColumnIndex("series");
            int orderInSeriesIndex = result.getColumnIndex("orderInSeries");
            int checkboxAlreadyReadIndex = result.getColumnIndex("alreadyRead");

            //fill TextViews with the retrieved data
            bookTitleTextView.setText(result.getString(bookTitleIndex));
            authorTextView.setText(result.getString(authorIndex));
            seriesTextView.setText(result.getString(seriesIndex));
            orderInSeriesTextView.setText(result.getString(orderInSeriesIndex));

            //checkboxAlreadyRead
           checkboxAlreadyRead.setClickable(true);

            result.close();//close the result cursor
            databaseConnector.close(); //close the database connection
        }//end method onPostExecute
    }//end method LoadBookTask

    //delete a book
    private void deleteBook(){
        //useFragmentManager to display the confirmDelete DialogFragment
        confirmDelete.show(getFragmentManager(), "confirm delete");
    }
    //DialogFragment to confirm deletion of book
    private DialogFragment confirmDelete = new DialogFragment(){
        //create an AlertDialog and return it
        @Override
        public Dialog onCreateDialog(Bundle bundle){
            //create a new AlertDialog Builder
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Are you sure?");
            builder.setMessage("This will permanently delete the book");
            //provide an OK button that dismisses the dialog
            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int button) {
                    final DatabaseConnector databaseConnector = new DatabaseConnector(getActivity());

                    AsyncTask<Long, Object, Object> deleteTask = new AsyncTask<Long, Object, Object>() {
                        @Override
                        protected Object doInBackground(Long... params) {
                            databaseConnector.deleteBook(params[0]);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Object result) {
                            listener.onBookDeleted();
                        }
                    }; //end new AsyncTask

                    //execute the AsyncTask to delete book at rowID
                    deleteTask.execute(new Long[] { rowID });

                } //end method onClick
            } //end anonymous inner class
            );  //end call to method setPositiveButton
            builder.setNegativeButton("Cancel", null);
            return builder.create();  //return the AlertDialog
        }
    };  //end DialogFragment anonymous inner class
}  //end class DetailsFragment

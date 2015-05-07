/**AddEditFragment
 * Allow user to add a new book or edit an existing one
 */

package com.example.barbara.alreadyread;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class AddEditFragment extends Fragment {

    //callback method implemented by MainActivity
    public interface AddEditFragmentListener {

        //called after edit completed so book info can be redisplayed
        public void onAddEditCompleted(long rowID);
    }

    private AddEditFragmentListener listener;

    private long rowID; //database row ID of the book
    private Bundle bookInfoBundle;//arguments for editing a book's info

    //EditTexts for book information
    private EditText bookTitleEditText;
    private EditText authorEditText;
    private EditText seriesEditText;
    private EditText orderInSeriesEditText;
    private CheckBox checkboxAlreadyReadClicked;

    //Set AddEditFragmentListener when Fragment attached

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (AddEditFragmentListener) activity;
    }

    //remove AddEditFragmentListener when Fragment detached

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    //called when Fragment's view needs to be created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setRetainInstance(true); //save fragment across config changes
        setHasOptionsMenu(true);//fragment has menu items to display

        // Inflate the layout for this fragment and get references to EditTexts and Checkbox
        View view = inflater.inflate(R.layout.fragment_add_edit, container, false);
        bookTitleEditText = (EditText) view.findViewById(R.id.bookTitleEditText);
        authorEditText = (EditText) view.findViewById(R.id.authorEditText);
        seriesEditText = (EditText) view.findViewById(R.id.seriesEditText);
        orderInSeriesEditText = (EditText) view.findViewById(R.id.orderInSeriesEditText);
        checkboxAlreadyReadClicked = (CheckBox) view.findViewById(R.id.checkboxAlreadyRead);


        bookInfoBundle = getArguments(); //null if creating new book info

        if (bookInfoBundle !=null){
            rowID = bookInfoBundle.getLong(MainActivity.ROW_ID);
            bookTitleEditText.setText(bookInfoBundle.getString("bookTitle"));
            authorEditText.setText(bookInfoBundle.getString("author"));
            seriesEditText.setText(bookInfoBundle.getString("series"));
            orderInSeriesEditText.setText(bookInfoBundle.getString("orderInSeries"));
            checkboxAlreadyReadClicked.setChecked(bookInfoBundle.getBoolean("checkboxAlreadyReadChecked"));
        }



        //set Save bookButton's event listener
        Button saveBookButton = (Button) view.findViewById(R.id.saveBookButton);
        saveBookButton.setOnClickListener(saveBookButtonClicked);
        return view;
    }

    //responds to event generated when user saves a contact
    OnClickListener saveBookButtonClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (bookTitleEditText.getText().toString().trim().length() != 0){
                //AsyncTask to save book, then notify listener
                AsyncTask<Object, Object, Object> saveBookTask = new AsyncTask<Object, Object, Object>() {
                    @Override
                    protected Object doInBackground(Object... params) {
                        saveBook(); //save book to the database
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object result) {
                        //hide soft keyboard
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

                        listener.onAddEditCompleted(rowID);
                    }
                };  //end AsyncTask

                //save the book to the database using a separate thread
                saveBookTask.execute((Object[]) null);
            }else{
                DialogFragment errorSaving = new DialogFragment(){
                    @Override
                    public Dialog onCreateDialog(Bundle savedInstanceState) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("You must enter a book title");
                        builder.setPositiveButton("OK", null);
                        return builder.create();
                    }
                };
                errorSaving.show(getFragmentManager(), "error saving book");
            }
        } //end method OnClick
    }; //end OnClickListener saveBookButtonClicked
    //save book information to the database
    private void saveBook(){
        //get DatabaseConnector to interact with the SQLite database
        DatabaseConnector databaseConnector = new DatabaseConnector(getActivity());

        if (bookInfoBundle == null) {
            //insert the book information into the database
            rowID = databaseConnector.insertBook(
                    bookTitleEditText.getText().toString(),
                    authorEditText.getText().toString(),
                    seriesEditText.getText().toString(),
                    orderInSeriesEditText.getText().toString(),
                    checkboxAlreadyReadClicked.isChecked());
        }else{
                databaseConnector.updateBook(rowID,
                        bookTitleEditText.getText().toString(),
                        authorEditText.getText().toString(),
                        seriesEditText.getText().toString(),
                        orderInSeriesEditText.getText().toString(),
                        checkboxAlreadyReadClicked.isChecked());

            }
//        Toast.makeText(this, "Book Info Saved!", Toast.LENGTH_LONG).show();
    }//end method saveBook
}//end method addEditFragment


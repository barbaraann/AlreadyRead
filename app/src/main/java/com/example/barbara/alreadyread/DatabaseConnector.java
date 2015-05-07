/**
 * DatabaseConnector
 * Provide connection and creation of Books AlreadyRead database
 * Created by Barbara on 5/2/2015.
 */
package com.example.barbara.alreadyread;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseConnector {

    //database name
    private static final String DATABASE_NAME = "BooksAlreadyRead";

    private SQLiteDatabase database; //for interacting with the database
    private DatabaseOpenHelper databaseOpenHelper;

    //public constructor for DatabaseConnector
    public DatabaseConnector(Context context){
        //create a new databaseOpenHelper
        databaseOpenHelper = new DatabaseOpenHelper(context, DATABASE_NAME, null, 1);
            }
        //open the database connector
    public void open() throws SQLException{
        //create or open database for reading/writing
        database = databaseOpenHelper.getWritableDatabase();
    }
     //close the database connection
    public void close(){
        if (database != null)
            database.close();  //close the database connection
    }
    //inserts a new book in the database
    public long insertBook(String bookTitle, String author, String series, String orderInSeries, boolean alreadyRead){
        ContentValues newBook = new ContentValues();
        newBook.put("bookTitle", bookTitle);
        newBook.put("author", author);
        newBook.put("series", series);
        newBook.put("orderInSeries", orderInSeries);
        newBook.put("alreadyRead", alreadyRead);

        open();  //open the database
        long rowID = database.insert("books", null, newBook);
        close();  //close the database
        return rowID;
          }
    //updates an existing Book in the database
    public void updateBook (long id, String bookTitle, String author, String series, String orderInSeries, boolean alreadyRead){
        ContentValues editBook = new ContentValues();
        editBook.put("bookTitle", bookTitle);
        editBook.put("author", author);
        editBook.put("series", series);
        editBook.put("orderInSeries", orderInSeries);
        editBook.put("alreadyRead", alreadyRead);

        open(); //open the database
        database.update("books", editBook, "_id=" + id, null);
        close(); //close the database
    }//end method updateBook

    //return a Cursor with all book titles  in the database
    public Cursor getAllBooks(){
        return database.query("books", new String[] {"_id", "bookTitle"}, null, null, null, null, "bookTitle");
    }
    //return a cursor containing specified book's information
    public Cursor getOneBook(long id){
        return database.query("books", null, "_id=" + id, null, null, null, null, null );
    }

    //delete the book specified by the given String name
    public void deleteBook(long id){
        open();  //open the database
        database.delete("books", "_id=" + id, null);
        close(); //close the database
    }

    private class DatabaseOpenHelper extends SQLiteOpenHelper{
        //constructor
        public DatabaseOpenHelper(Context context, String name, CursorFactory factory, int version){
            super(context, name, factory, version);
        }

        //creates the book table when the database is created
        @Override
        public void onCreate(SQLiteDatabase db){
            //query to create a new table named books
            String createQuery = "CREATE TABLE books" +
                    "(_id integer primary key autoincrement," +
                    "bookTitle TEXT, author TEXT, series TEXT, orderInSeries TEXT, alreadyRead BOOLEAN);";

            db.execSQL(createQuery); //execute query to create the database
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

        }
    } //end class DatabaseOpenHelper
} //end class DatabaseConnector























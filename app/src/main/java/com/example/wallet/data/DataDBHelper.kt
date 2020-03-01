package com.example.wallet.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import com.example.wallet.data.DataContract.BudgetEntry
import com.example.wallet.data.DataContract.CategoryEntry
import com.example.wallet.data.DataContract.StatementEntry
import com.example.wallet.data.DataContract.CurrencyExEntry
import com.example.wallet.data.DataContract.LocationEntry

/**
 * Created by FBrigati on 26/04/2017.
 */

class DataDBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {

        //Creation of statements table
        val SQL_CREATE_STATEMENT_TABLE = "CREATE TABLE " + StatementEntry.TABLE_NAME + " (" +
                StatementEntry.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                StatementEntry.COLUMN_USER_ID + " TEXT NOT NULL, " +
                StatementEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
                StatementEntry.COLUMN_TIME + " TEXT NOT NULL, " +
                StatementEntry.COLUMN_SEQUENCE + " INTEGER NOT NULL, " +
                StatementEntry.COLUMN_DESCRIPTION_ORIGIN + " TEXT NOT NULL, " +
                StatementEntry.COLUMN_DESCRIPTION_USER + " TEXT NOT NULL, " +
                StatementEntry.COLUMN_AMOUNT + " REAL NOT NULL DEFAULT 0, " +
                StatementEntry.COLUMN_TRANSACTION_CODE + " INTEGER NOT NULL, " +
                StatementEntry.COLUMN_ACQUIRER_ID + " TEXT NOT NULL, " +
                StatementEntry.COLUMN_CATEGORY_KEY + " TEXT NOT NULL, " +

                " UNIQUE (" + StatementEntry.COLUMN_USER_ID + "," +
                StatementEntry.COLUMN_DATE + "," +
                StatementEntry.COLUMN_TIME + "," +
                StatementEntry.COLUMN_SEQUENCE + ")  ON CONFLICT REPLACE ," +
                // Set up the category column as a foreign key to location table.
                " FOREIGN KEY (" + StatementEntry.COLUMN_CATEGORY_KEY + ") REFERENCES " +
                CategoryEntry.TABLE_NAME + " (" + CategoryEntry.ID + ") " +
                " );"

        //Creation of category table
        val SQL_CREATE_CATEGORY_TABLE = "CREATE TABLE " + CategoryEntry.TABLE_NAME + " (" +
                CategoryEntry.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CategoryEntry.COLUMN_CATEGORY_USER_KEY + " TEXT NOT NULL DEFAULT ' ', " +
                CategoryEntry.COLUMN_CATEGORY_DEFAULT + " TEXT NOT NULL DEFAULT 'NA', " +
                CategoryEntry.COLUMN_ACQUIRER_ID + " TEXT NOT NULL DEFAULT 'NA', " +
                // to assure user does not repeat categories
                " UNIQUE (" + CategoryEntry.COLUMN_CATEGORY_USER_KEY + ") ON CONFLICT REPLACE);"


        //Creation of budget table
        val SQL_CREATE_BUDGET_TABLE = "CREATE TABLE " + BudgetEntry.TABLE_NAME + " (" +
                BudgetEntry.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                BudgetEntry.COLUMN_MONTH + " INTEGER NOT NULL, " +
                BudgetEntry.COLUMN_YEAR + " INTEGER NOT NULL, " +
                BudgetEntry.COLUMN_AMOUNT + " REAL NOT NULL, " +
                BudgetEntry.COLUMN_CATEGORY + " TEXT NOT NULL DEFAULT 'ALL', " +
                // to assure user does not repeat budgets for different categories..
                " UNIQUE (" + BudgetEntry.COLUMN_MONTH + ", " +
                BudgetEntry.COLUMN_YEAR + ", " + BudgetEntry.COLUMN_CATEGORY + ") ON CONFLICT REPLACE);"

        //Creation of currencyex table
        val SQL_CREATE_CUREX_TABLE = "CREATE TABLE " + CurrencyExEntry.TABLE_NAME + " (" +
                CurrencyExEntry.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CurrencyExEntry.COLUMN_SYMBOL + " TEXT NOT NULL, " +
                CurrencyExEntry.COLUMN_RATE + " REAL NOT NULL, " +
                CurrencyExEntry.COLUMN_DATE + " TEXT NOT NULL, " +
                "UNIQUE (" + CurrencyExEntry.COLUMN_SYMBOL + ") ON CONFLICT REPLACE);"

        //Creation of location table
        val SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
                LocationEntry.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                LocationEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
                LocationEntry.COLUMN_TIME + " TEXT NOT NULL, " +
                LocationEntry.COLUMN_LAT + " TEXT NOT NULL, " +
                LocationEntry.COLUMN_LNG + " TEXT NOT NULL );"


        //Creation of primary budget items
        val SQL_CREATE_BUDGET_ITEMS = "INSERT INTO " + BudgetEntry.TABLE_NAME +
                " (" + BudgetEntry.COLUMN_YEAR + "," + BudgetEntry.COLUMN_MONTH + "," +
                BudgetEntry.COLUMN_CATEGORY + "," + BudgetEntry.COLUMN_AMOUNT + ") " +
                "VALUES " +
                "(strftime('%Y','now'), strftime('%m','now'),'Transportation',0)," +
                "(strftime('%Y','now'), strftime('%m','now'),'Leisure',0)," +
                "(strftime('%Y','now'), strftime('%m','now'),'Food',0)," +
                "(strftime('%Y','now'), strftime('%m','now'),'Education',0)," +
                "(strftime('%Y','now'), strftime('%m','now'),'HealthCare',0)," +
                "(strftime('%Y','now'), strftime('%m','now'),'Groceries',0)," +
                "(strftime('%Y','now'), strftime('%m','now'),'Rent',0);"


        //Creation of primary category items
        val SQL_CREATE_CATEGORY_ITEMS = "INSERT INTO " + CategoryEntry.TABLE_NAME +
                " (" + CategoryEntry.COLUMN_CATEGORY_USER_KEY + "," + CategoryEntry.COLUMN_CATEGORY_DEFAULT + "," +
                CategoryEntry.COLUMN_ACQUIRER_ID + ") " +
                "VALUES " +
                "('Transportation','Transportation',0)," +
                "('Leisure','Leisure',0)," +
                "('Food','Food',0)," +
                "('Education','Education',0)," +
                "('HealthCare','HealthCare',0)," +
                "('Groceries','Groceries',0)," +
                "('Rent','Rent',0);"



        db.execSQL(SQL_CREATE_STATEMENT_TABLE)
        db.execSQL(SQL_CREATE_CATEGORY_TABLE)
        db.execSQL(SQL_CREATE_BUDGET_TABLE)
        db.execSQL(SQL_CREATE_CUREX_TABLE)
        db.execSQL(SQL_CREATE_LOCATION_TABLE)
        db.execSQL(SQL_CREATE_BUDGET_ITEMS)
        db.execSQL(SQL_CREATE_CATEGORY_ITEMS)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        db.execSQL("DROP TABLE IF EXISTS " + StatementEntry.TABLE_NAME)
        db.execSQL("DROP TABLE IF EXISTS " + CategoryEntry.TABLE_NAME)
        db.execSQL("DROP TABLE IF EXISTS " + BudgetEntry.TABLE_NAME)
        db.execSQL("DROP TABLE IF EXISTS " + CurrencyExEntry.TABLE_NAME)
        db.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME)
        onCreate(db)
    }

    companion object {


        // If you change the database schema, you must increment the database version.
        private val DATABASE_VERSION = 12

        internal val DATABASE_NAME = "wallet.db"
    }
}

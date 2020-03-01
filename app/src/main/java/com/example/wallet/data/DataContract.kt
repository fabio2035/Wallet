package com.example.wallet.data


import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.BaseColumns

/**
 * Created by FBrigati on 26/04/2017.
 */

object DataContract {

    //name of content provider
    val CONTENT_AUTHORITY = "com.example.wallet"

    val BASE_CONTENT_URI = Uri.parse("content://$CONTENT_AUTHORITY")

    //possible paths
    val PATH_STATEMENT = "statement"
    val PATH_BUDGET = "budget"
    val PATH_CATEGORY = "category"
    val PATH_CUREX = "currencyex"
    val PATH_LOCATION = "location"


    //Definition of satement table *****
    class StatementEntry : BaseColumns {
        companion object {

            val CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_STATEMENT).build()

            val CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STATEMENT
            val CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STATEMENT

            // Table name
            val TABLE_NAME = "statement"

            val STATEMENT_COLUMNS = arrayOf(
                StatementEntry.TABLE_NAME + "." + StatementEntry.ID,
                StatementEntry.COLUMN_USER_ID,
                //StatementEntry.COLUMN_ACCOUNT_NUMBER, /*Account number is being represented by User_id */
                StatementEntry.COLUMN_DATE,
                StatementEntry.COLUMN_TIME,
                StatementEntry.COLUMN_SEQUENCE,
                StatementEntry.COLUMN_DESCRIPTION_ORIGIN,
                StatementEntry.COLUMN_DESCRIPTION_USER,
                StatementEntry.COLUMN_AMOUNT,
                StatementEntry.COLUMN_TRANSACTION_CODE,
                StatementEntry.COLUMN_ACQUIRER_ID,
                StatementEntry.COLUMN_CATEGORY_KEY
            )

            //bounded columns for general statement...
            val COL_ACCOUNT_NUMBER = 1
            val COL_DATE = 2
            val COL_TIME = 3
            val COL_SEQUENCE = 4
            val COL_DESCRIPTION_ORIGIN = 5
            val COL_DESCRIPTION_USER = 6
            val COL_AMOUNT = 7
            val COL_TRANSACTION_CODE = 8
            val COL_ACQUIRER_ID = 9
            val COL_CATEGORY_KEY = 10

            //table column names...
            val ID = "_id"
            val COLUMN_USER_ID = "user_id"
            //val COLUMN_ACCOUNT_NUMBER = "accountNumber" /*Account number is being represented by User_id */
            val COLUMN_DATE = "date"
            val COLUMN_TIME = "time"
            val COLUMN_SEQUENCE = "sequence"
            val COLUMN_DESCRIPTION_ORIGIN = "desc_origin"
            val COLUMN_DESCRIPTION_USER = "desc_user"
            val COLUMN_AMOUNT = "amount"
            val COLUMN_TRANSACTION_CODE = "trxcode"
            val COLUMN_ACQUIRER_ID = "acquirer_id"
            val COLUMN_CATEGORY_KEY = "category"

            fun buildStatementUri(id: Long): Uri {
                return ContentUris.withAppendedId(CONTENT_URI, id)
            }

            fun buildStatsMonthUri(month: Int): Uri {
                return CONTENT_URI.buildUpon().appendPath("stats")
                    .appendPath(month.toString()).build()
            }

            fun buildStatsPieChartTrimUri(trimestre: Int): Uri {
                return CONTENT_URI.buildUpon().appendPath("stats").appendPath("trimester")
                    .appendPath(trimestre.toString()).build()
            }

            fun buildStatsLineGraphChartTrimUri(trimestre: Int, category: String): Uri {
                return CONTENT_URI.buildUpon().appendPath("stats").appendPath("trimester")
                    .appendPath(category)
                    .appendPath(trimestre.toString()).build()
            }

            fun buildStatsLineGraphChartAllTrimUri(trimestre: Int, category: String): Uri {
                return CONTENT_URI.buildUpon().appendPath("stats").appendPath("trimester")
                    .appendPath(category)
                    .appendPath(trimestre.toString()).build()
            }

            fun buildWidgetDataUri(): Uri {
                return CONTENT_URI.buildUpon().appendPath("widget")
                    .appendPath("data").build()
            }


            fun getMonthFromUri(uri: Uri): Int {
                return Integer.parseInt(uri.lastPathSegment!!)
            }

            fun getCategoryFromUri(uri: Uri): String {
                return uri.pathSegments[3]
            }

            fun getAccountFromUri(uri: Uri): String {
                return uri.pathSegments[1]
            }

            fun getDateFromUri(uri: Uri): Int {
                return Integer.parseInt(uri.pathSegments[2])
            }

            fun getIDFromUri(uri: Uri): Int {
                return Integer.parseInt(uri.lastPathSegment!!)
            }
        }

    }

    //Definition of categroy table *****
    class CategoryEntry : BaseColumns {
        companion object {

            val CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CATEGORY).build()

            val CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CATEGORY
            val CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CATEGORY

            // Table name
            val TABLE_NAME = "category"

            val CATEGORY_COLUMNS = arrayOf(
                CategoryEntry.TABLE_NAME + "." + CategoryEntry.ID,
                CategoryEntry.COLUMN_ACQUIRER_ID,
                CategoryEntry.COLUMN_CATEGORY_DEFAULT,
                CategoryEntry.COLUMN_CATEGORY_USER_KEY
            )

            //bounded columns...
            val COL_CATEGORY_USER_KEY = 1
            val COL_CATEGORY_DEFAULT = 2
            val COL_ACQUIRER_ID = 3


            //table columns names...
            val ID = "_id"
            val COLUMN_ACQUIRER_ID = "acquirer_id"
            val COLUMN_CATEGORY_DEFAULT = "desc_default"
            val COLUMN_CATEGORY_USER_KEY = "category_id"

            fun buildCategoryUri(id: Long): Uri {
                return ContentUris.withAppendedId(CONTENT_URI, id)
            }
        }

    }

    //Definition of budget table *****
    class BudgetEntry : BaseColumns {
        companion object {

            val CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BUDGET).build()

            val CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BUDGET
            val CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BUDGET

            // Table name
            val TABLE_NAME = "budget"

            //bounded columns...
            val COL_MONTH = 1
            val COL_YEAR = 2
            val COL_AMOUNT = 3
            val COL_CATEGORY = 4
            val COL_SPENT = 5

            //table columns names...
            val ID = "_id"
            val COLUMN_YEAR = "year"
            val COLUMN_MONTH = "month"
            val COLUMN_CATEGORY = "category"
            val COLUMN_AMOUNT = "amount"


            fun buildBudgetUri(id: Long): Uri {
                return ContentUris.withAppendedId(CONTENT_URI, id)
            }

            fun buildBudgetWidgetUri(month: Int): Uri {
                return CONTENT_URI.buildUpon().appendPath("widget")
                    .appendPath(month.toString()).build()
            }

            fun getBudgetCategory(uri: Uri): String {
                return uri.pathSegments[1]
            }

            fun buildBudgetMonth(month: Int): Uri {
                return CONTENT_URI.buildUpon().appendPath(month.toString()).build()
            }

            fun getBudgetMonth(uri: Uri): Int {
                return Integer.parseInt(uri.lastPathSegment!!)
            }
        }

    }

    //Definition of location table *****
    class LocationEntry : BaseColumns {
        companion object {

            val CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build()

            val CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION
            val CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION

            // Table name
            val TABLE_NAME = "location"

            val LOCATION_COLUMNS = arrayOf(
                LocationEntry.TABLE_NAME + "." + LocationEntry.ID,
                LocationEntry.COLUMN_DATE,
                LocationEntry.COLUMN_TIME,
                LocationEntry.COLUMN_LAT,
                LocationEntry.COLUMN_LNG
            )


            //bounded columns...
            val COL_DATETIME = 1
            val COL_DATE = 2
            val COL_TIME = 3
            val COL_LAT = 4
            val COL_LNG = 5

            //table columns names...
            val ID = "_id"
            val COLUMN_DATE = "date"
            val COLUMN_TIME = "time"
            val COLUMN_LAT = "lat"
            val COLUMN_LNG = "lng"
            val COLUMN_CLASS = "class"  //classification of location


            fun buildLocationUri(id: Long): Uri {
                return ContentUris.withAppendedId(CONTENT_URI, id)
            }

            fun buildLocationUri(symbol: String): Uri {
                return CONTENT_URI.buildUpon().appendPath(symbol).build()
            }

            fun getLocationFromUri(uri: Uri?): String? {
                return uri?.lastPathSegment
            }
        }

    }

    //Definition of currencies table *****
    class CurrencyExEntry : BaseColumns {
        companion object {

            val CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CUREX).build()

            val CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CUREX
            val CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CUREX

            // Table name
            val TABLE_NAME = "currencyex"

            val CURRENCIES_COLUMNS = arrayOf(
                CurrencyExEntry.TABLE_NAME + "." + CurrencyExEntry.ID,
                CurrencyExEntry.COLUMN_SYMBOL,
                CurrencyExEntry.COLUMN_RATE,
                CurrencyExEntry.COLUMN_DATE
            )


            //bounded columns...
            val COL_SYMBOL = 1
            val COL_RATE = 2
            val COL_DATE = 3

            //table columns names...
            val ID = "_id"
            val COLUMN_SYMBOL = "symbol"
            val COLUMN_RATE = "rate"
            val COLUMN_DATE = "date"


            fun buildCurrencyExUri(id: Long): Uri {
                return ContentUris.withAppendedId(CONTENT_URI, id)
            }

            fun buildCurrencyUri(symbol: String): Uri {
                return CONTENT_URI.buildUpon().appendPath(symbol).build()
            }

            fun getBaseCurrenyFromUri(uri: Uri): String? {
                return uri.lastPathSegment
            }
        }

    }

}
package com.example.wallet.data


import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.provider.ContactsContract
import androidx.annotation.NonNull
import androidx.annotation.Nullable

/**
 * Created by FBrigati on 27/04/2017.
 */

 class DataProvider:ContentProvider() {
lateinit var mOpenHelper:DataDBHelper

private fun getCurrenciesByBaseCurrency(
uri:Uri, projection:Array<String>?, sortOrder:String?):Cursor {
val baseCurrency = DataContract.CurrencyExEntry.getBaseCurrenyFromUri(uri)
 //Log.v(LOG_TAG, "baseCurrency: " + baseCurrency);
        return mCurrencyQueryBuilder.query(mOpenHelper!!.readableDatabase,
projection,
sBaseCurrencySelection,
arrayOf("$baseCurrency%"), null, null,
sortOrder
)
}

override fun onCreate():Boolean {
mOpenHelper = DataDBHelper(this!!.context!!)
return true
}

@Nullable
override fun query(uri:Uri, projection:Array<String>?, selection:String?, selectionArgs:Array<String>?, sortOrder:String?):Cursor? {
 //Log.v(LOG_TAG, "Query ID: " + uri);

        val retCursor:Cursor?
when (sUriMatcher.match(uri)) {
 // statement/*
            STATEMENT_WITH_ID -> {
retCursor = getStatementByID(uri, projection, sortOrder)
}
 // statement/#"
            STATEMENT_WITH_USERID -> {
retCursor = getStatementByUserID(uri, projection, sortOrder)
}
 // "statement/#/*"
            STATEMENT_STATS_MONTH -> {
retCursor = getStatsByMOnth(uri, projection, sortOrder)
}
STATEMENT_STATS_TRIMESTER -> {
retCursor = getStatsPieChartByTrimester(uri, projection, sortOrder)
}
STATEMENT_LINEGRAPH_DATA -> {
retCursor = getStatsLineGraphByTrimester(uri, projection, sortOrder)
}
 // "statement"
            STATEMENT -> {
retCursor = mOpenHelper!!.readableDatabase.query(
DataContract.StatementEntry.TABLE_NAME,
projection,
selection,
selectionArgs, null, null,
sortOrder
)
}
STATEMENT_WIDGET_DATA -> {
retCursor = getWidgetDataCursor(uri, projection, sortOrder)
}
 // "Category"
            CATEGORY -> {
retCursor = mOpenHelper!!.readableDatabase.query(
DataContract.CategoryEntry.TABLE_NAME,
projection,
selection,
selectionArgs, null, null,
sortOrder
)
}
 // "Budget"
            BUDGET_WITH_MONTH -> {
retCursor = getBudgetWithMonth(uri, projection, sortOrder)
}
BUDGET_WIDGET -> {
retCursor = getWidgetCollectionCursor(uri, projection, sortOrder)
}
 // "Currencies"
            CUREX -> {
 //Log.v(LOG_TAG, "currency query called");
                retCursor = mOpenHelper!!.readableDatabase.query(
DataContract.CurrencyExEntry.TABLE_NAME,
projection,
selection,
selectionArgs, null, null,
sortOrder
)
}
 // "Currencies"
            CUREX_WITH_BASE -> {

retCursor = getCurrenciesByBaseCurrency(uri, projection, sortOrder)
}

else -> throw UnsupportedOperationException("Unknown uri: $uri")
}
retCursor!!.setNotificationUri(context!!.contentResolver, uri)
return retCursor
}

private fun getStatsByMOnth(uri:Uri, projection:Array<String>?, sortOrder:String?):Cursor {
 //String category = DataContract.BudgetEntry.getBudgetCategory(uri);
        val month = DataContract.StatementEntry.getMonthFromUri(uri)


return mOpenHelper!!.readableDatabase.rawQuery(
    "select a._ID, a.date, a.amount, a.category from statement a " + " where substr(a.date,5,2)*1 = ? ", arrayOf(
        month.toString())) // new String[] {String.valueOf(month)});
}

private fun getStatsPieChartByTrimester(uri:Uri, projection:Array<String>?, sortOrder:String?):Cursor? {
 //String category = DataContract.BudgetEntry.getBudgetCategory(uri);
        val trimestre = DataContract.StatementEntry.getMonthFromUri(uri)
val year = Utility.getStatsNavYear(this!!.context!!)


when (trimestre) {
1 -> return mOpenHelper!!.readableDatabase.rawQuery(
    "select a.category, sum(a.amount) from statement a " +
    " where substr(a.date,5,2)*1 BETWEEN 1 AND 3 " +
    " and substr(a.date,1,4)*1 =" + year +
    " and a.trxcode >=6 " +
    " group by a.category", null)
2 -> return mOpenHelper!!.readableDatabase.rawQuery(
("select a.category, sum(a.amount) from statement a " +
" where substr(a.date,5,2)*1 BETWEEN 4 AND 6 " +
" and substr(a.date,1,4)*1 =" + year +
" and a.trxcode >=6 " +
" group by a.category"), null)
3 -> return mOpenHelper!!.readableDatabase.rawQuery(
("select a.category, sum(a.amount) from statement a " +
" where substr(a.date,5,2)*1 BETWEEN 7 AND 9 " +
" AND substr(a.date,1,4)*1 =" + year +
" and a.trxcode >=6 " +
" group by a.category"), null)
4 -> return mOpenHelper!!.readableDatabase.rawQuery(
("select a.category, sum(a.amount) from statement a " +
" where substr(a.date,5,2)*1 BETWEEN 10 AND 12 " +
" AND substr(a.date,1,4)*1 =" + year +
" and a.trxcode >=6 " +
" group by a.category"), null)
else -> return null
}
}

private fun getStatsLineGraphByTrimester(uri:Uri, projection:Array<String>?, sortOrder:String?):Cursor? {
 //String category = DataContract.BudgetEntry.getBudgetCategory(uri);
        val trimestre = DataContract.StatementEntry.getMonthFromUri(uri)
val cat = DataContract.StatementEntry.getCategoryFromUri(uri)
val year = Utility.getStatsNavYear(this!!.context!!)


when (trimestre) {
1 -> return if (cat.trim { it <= ' ' } == "All") {
    mOpenHelper!!.readableDatabase.rawQuery(
        ("select a.category, a.date, sum(a.amount) from statement a " +
                " where substr(a.date,5,2)*1 BETWEEN 1 AND 3 " +
                " and substr(a.date,1,4)*1 =" + year +
                " and a.trxcode >=6 " +
                " group by a.date, a.category " +
                " order by a.category, a.date "), null)
} else {
    mOpenHelper!!.readableDatabase.rawQuery(
        ("select a.category, a.date, sum(a.amount) from statement a " +
                " where substr(a.date,5,2)*1 BETWEEN 1 AND 3 " +
                " and substr(a.date,1,4)*1 =" + year +
                " and a.category ='" + cat + "'" +
                " and a.trxcode >=6 " +
                " group by a.date, a.category " +
                " order by a.date "), null)
}
2 -> return if (cat.trim { it <= ' ' } == "All") {
    mOpenHelper!!.readableDatabase.rawQuery(
        ("select a.category, a.date, sum(a.amount) from statement a " +
                " where substr(a.date,5,2)*1 BETWEEN 4 AND 6 " +
                " and substr(a.date,1,4)*1 =" + year +
                " and a.trxcode >=6 " +
                " group by a.date, a.category " +
                " order by a.category, a.date "), null)
} else {
    mOpenHelper!!.readableDatabase.rawQuery(
        ("select a.category, a.date, sum(a.amount) from statement a " +
                " where substr(a.date,5,2)*1 BETWEEN 4 AND 6 " +
                " and substr(a.date,1,4)*1 =" + year +
                " and a.category ='" + cat + "'" +
                " and a.trxcode >=6 " +
                " group by a.date, a.category " +
                " order by a.date "), null)
}
3 -> return if (cat.trim { it <= ' ' } == "All") {
    mOpenHelper!!.readableDatabase.rawQuery(
        ("select a.category, a.date, sum(a.amount) from statement a " +
                " where substr(a.date,5,2)*1 BETWEEN 7 AND 9 " +
                " AND substr(a.date,1,4)*1 =" + year +
                " and a.trxcode >=6 " +
                " group by a.date, a.category " +
                " order by a.category, a.date "), null)
} else {
    mOpenHelper!!.readableDatabase.rawQuery(
        ("select a.category, a.date, sum(a.amount) from statement a " +
                " where substr(a.date,5,2)*1 BETWEEN 7 AND 9 " +
                " AND substr(a.date,1,4)*1 =" + year +
                " and a.category ='" + cat + "'" +
                " and a.trxcode >=6 " +
                " group by a.date, a.category " +
                " order by a.date "), null)
}
4 -> return if (cat.trim { it <= ' ' } == "All") {
    mOpenHelper!!.readableDatabase.rawQuery(
        ("select a.category, a.date, sum(a.amount) from statement a " +
                " where substr(a.date,5,2)*1 BETWEEN 10 AND 12 " +
                " AND substr(a.date,1,4)*1 =" + year +
                " and a.trxcode >=6 " +
                " group by a.date, a.category " +
                " order by a.category, a.date "), null)
} else {
    mOpenHelper!!.readableDatabase.rawQuery(
        ("select a.category, a.date, sum(a.amount) from statement a " +
                " where substr(a.date,5,2)*1 BETWEEN 10 AND 12 " +
                " AND substr(a.date,1,4)*1 =" + year +
                " and a.category ='" + cat + "'" +
                " and a.trxcode >=6 " +
                " group by a.date, a.category " +
                " order by a.date "), null)
}
else -> return null
}
}

private fun getWidgetDataCursor(uri:Uri, projection:Array<String>?, sortOrder:String?):Cursor {

return mOpenHelper!!.readableDatabase.rawQuery(
("ifnull(" +
"(SELECT amount FROM statement as a " +
"WHERE a.date = cast(date('now', '%Y%m%d') as INT) " +
" and a.trxcode >=6 " +
"), 0) " +
"UNION " +
"ifnull(SELECT date, amount, 'week' as period FROM statement as b " +
"WHERE b.date >= DATE('now', 'weekday 0', '-7 days') " +
" and a.trxcode >=6 " +
"), 0) " +
"UNION " +
"ifnull(" +
"(SELECT date, amount, 'month' as period FROM statement as c " +
"WHERE substr(c.date,5,2)*1 = date('now', %m)*1 " +
" and a.trxcode >=6 " +
"), 0)"), null)
}

private fun getWidgetCollectionCursor(uri:Uri, projection:Array<String>?, sortOrder:String?):Cursor {

val month = DataContract.BudgetEntry.getBudgetMonth(uri)

return mOpenHelper!!.readableDatabase.rawQuery(
("select S._ID, S.month, S.year , S.category, S.amount, T.amount, T.date From budget as S inner join " +
"(select substr(a.date,5,2)*1 as Month, sum(amount) amount, category, date from statement as a " +
"group by category, substr(a.date,5,2)*1) as T ON " +
"S.month = T.month and S.category = T.category " +
"where S.month = ? ORDER BY T.date DESC LIMIT 2"), arrayOf((month).toString()))

}

private fun checkCategory(uri:Uri, projection:Array<String>, sortOrder:String):Cursor {

val query = "SELECT * FROM category "

return mOpenHelper!!.readableDatabase.rawQuery(
query, null)//, String.valueOf(month)});
}

private fun getBudgetWithMonth(uri:Uri, projection:Array<String>?, sortOrder:String?):Cursor {

 //String category = DataContract.BudgetEntry.getBudgetCategory(uri);
        val month = DataContract.BudgetEntry.getBudgetMonth(uri)

val query = ("SELECT B._ID, A.desc_default, ifnull(C.amount,0), ifnull(B.amount,0), " +
"ifnull(C.month_1,0), B.month, B.year FROM category as A " +
"LEFT JOIN (SELECT * from budget WHERE month =" + month + ") AS B ON A.desc_default = B.category " +
"LEFT JOIN (SELECT substr(t.date,5,2)*1 as month_1, " +
"sum(t.amount) as amount, t.category category FROM statement AS t " +
"WHERE substr(t.date,5,2)*1 = " + month + " AND trxcode >=6" +
" group by t.category, substr(t.date,5,2)*1) AS C ON " +
"A.desc_default = C.category " +
"ORDER BY C.amount desc, A.desc_default")

 //Log.v(LOG_TAG, "Query is: " + query );

        return mOpenHelper!!.readableDatabase.rawQuery(
query, null) // new String[] {String.valueOf(month)});//, String.valueOf(month)});
}


private fun getStatementByID(uri:Uri, projection:Array<String>?, sortOrder:String?):Cursor {

val ID = (DataContract.StatementEntry.getIDFromUri(uri)).toString()

 //Log.v(LOG_TAG, "ID = " +ID);

        return mStatementQueryBuilder.query(mOpenHelper!!.readableDatabase,
arrayOf(DataContract.StatementEntry.COLUMN_TRANSACTION_CODE, DataContract.StatementEntry.COLUMN_CATEGORY_KEY, DataContract.StatementEntry.COLUMN_TRANSACTION_CODE, DataContract.StatementEntry.COLUMN_DESCRIPTION_USER, DataContract.StatementEntry.COLUMN_DATE, DataContract.StatementEntry.COLUMN_TIME, DataContract.StatementEntry.COLUMN_AMOUNT, DataContract.CategoryEntry.COLUMN_CATEGORY_USER_KEY, DataContract.StatementEntry.COLUMN_TRANSACTION_CODE),
sStatementIDSelection,
arrayOf(ID), null, null,
sortOrder
)
}

private fun getStatementByUserID(uri:Uri, projection:Array<String>?, sortOrder:String?):Cursor {

val userId = DataContract.StatementEntry.getAccountFromUri(uri)
val date = DataContract.StatementEntry.getDateFromUri(uri)

return mStatementQueryBuilder.query(mOpenHelper!!.readableDatabase,
projection,
sAcctnumberAndDateSelection,
arrayOf(userId, Integer.toString(date)), null, null,
sortOrder
)
}

@Nullable
override fun getType(uri:Uri):String? {
val match = sUriMatcher.match(uri)

when (match) {
STATEMENT -> return DataContract.StatementEntry.CONTENT_TYPE
STATEMENT_WITH_USERID -> return DataContract.StatementEntry.CONTENT_TYPE
STATEMENT_STATS_MONTH -> return DataContract.StatementEntry.CONTENT_TYPE
CATEGORY -> return DataContract.CategoryEntry.CONTENT_TYPE
CATEGORY_WITH_ACQUIRER -> return DataContract.CategoryEntry.CONTENT_ITEM_TYPE
BUDGET -> return DataContract.BudgetEntry.CONTENT_TYPE
BUDGET_WITH_MONTH -> return DataContract.BudgetEntry.CONTENT_ITEM_TYPE
BUDGET_WIDGET -> return DataContract.BudgetEntry.CONTENT_ITEM_TYPE
CUREX -> return DataContract.CurrencyExEntry.CONTENT_TYPE
    LOCATION -> return DataContract.LocationEntry.CONTENT_TYPE

else -> throw UnsupportedOperationException("Unknown uri: $uri")
}
}

@Nullable
override fun insert(uri:Uri, values:ContentValues?):Uri? {
    val db = mOpenHelper!!.writableDatabase
    val match = sUriMatcher.match(uri)
    val returnUri: Uri


    when (match) {
        STATEMENT -> {
            //normalizeData();
            val _id = db.insert(DataContract.StatementEntry.TABLE_NAME, null, values)
            if (_id > 0) {
                returnUri = DataContract.StatementEntry.buildStatementUri(_id)
            } else
                throw android.database.SQLException("Failed to insert row into $uri")
        }
        CATEGORY -> {
            val _id = db.insert(DataContract.CategoryEntry.TABLE_NAME, null, values)
            if (_id > 0)
                returnUri = DataContract.CategoryEntry.buildCategoryUri(_id)
            else
                throw android.database.SQLException("Failed to insert row into $uri")
        }
        BUDGET -> {
            val _id = db.insert(DataContract.BudgetEntry.TABLE_NAME, null, values)
            if (_id > 0) {
                returnUri = DataContract.BudgetEntry.buildBudgetUri(_id)
            } else
                throw android.database.SQLException("Failed to insert row into $uri")
        }
        CUREX -> {
            val _id = db.insert(DataContract.CurrencyExEntry.TABLE_NAME, null, values)
            if (_id > 0)
                returnUri = DataContract.CurrencyExEntry.buildCurrencyExUri(_id)
            else
                throw android.database.SQLException("Failed to insert row into $uri")
        }

        LOCATION -> {
            val _id = db.insert(DataContract.LocationEntry.TABLE_NAME, null, values)
            if (_id > 0)
                returnUri = DataContract.LocationEntry.buildLocationUri(_id)
            else
                throw android.database.SQLException("Failed to insert row into $uri")
        }

        else -> throw UnsupportedOperationException("Unknown uri: $uri")
    }

    context!!.contentResolver.notifyChange(uri, null)
    return returnUri
}

override fun delete(uri:Uri, selection:String?, selectionArgs:Array<String>?):Int {
    var selection = selection
    val db = mOpenHelper!!.writableDatabase
    val match = sUriMatcher.match(uri)
    val rowsDeleted: Int
    // this makes delete all rows return the number of rows deleted
    if (null == selection) selection = "1"
    when (match) {
        STATEMENT -> rowsDeleted = db.delete(
            DataContract.StatementEntry.TABLE_NAME, selection, selectionArgs
        )
        CATEGORY -> rowsDeleted = db.delete(
            DataContract.CategoryEntry.TABLE_NAME, selection, selectionArgs
        )
        BUDGET -> rowsDeleted = db.delete(
            DataContract.BudgetEntry.TABLE_NAME, selection, selectionArgs
        )
        CUREX -> rowsDeleted = db.delete(
            DataContract.CurrencyExEntry.TABLE_NAME, selection, selectionArgs
        )
        LOCATION -> rowsDeleted = db.delete(
            DataContract.LocationEntry.TABLE_NAME, selection, selectionArgs
        )
        else -> throw UnsupportedOperationException("Unknown uri: $uri")
    }
    // Because a null deletes all rows
    if (rowsDeleted != 0) {
        context!!.contentResolver.notifyChange(uri, null)
    }
    return rowsDeleted
}

override fun update(uri:Uri, values:ContentValues?, selection:String?, selectionArgs:Array<String>?):Int {
val db = mOpenHelper!!.writableDatabase
val match = sUriMatcher.match(uri)
val rowsUpdated:Int

when (match) {
STATEMENT ->
 //normalizeDate(values);
                    rowsUpdated = db.update(DataContract.StatementEntry.TABLE_NAME, values, selection,
selectionArgs)
CATEGORY -> rowsUpdated = db.update(DataContract.CategoryEntry.TABLE_NAME, values, selection,
selectionArgs)
BUDGET -> rowsUpdated = db.update(DataContract.BudgetEntry.TABLE_NAME, values, selection,
selectionArgs)
else -> throw UnsupportedOperationException("Unknown uri: $uri")
}
if (rowsUpdated != 0)
{
context!!.contentResolver.notifyChange(uri, null)
}
return rowsUpdated
}

override fun bulkInsert(@NonNull uri:Uri, @NonNull values:Array<ContentValues>):Int {

val db = mOpenHelper!!.writableDatabase

when (sUriMatcher.match(uri)) {
CUREX -> {
db.beginTransaction()
val returnCount = 0
 //Log.v(LOG_TAG, "About to insert values in currencyExchange..");
                try
{
for (value in values)
{
db.insert(
DataContract.CurrencyExEntry.TABLE_NAME, null,
value
)
}
db.setTransactionSuccessful()
}

finally
{
db.endTransaction()
}
context!!.contentResolver.notifyChange(uri, null)
return returnCount
}
else -> return super.bulkInsert(uri, values)
}
}

companion object {

internal val LOG_TAG = DataProvider::class.java!!.simpleName

 // The URI Matcher used by this content provider.
    private val sUriMatcher = buildUriMatcher()

internal val STATEMENT = 100
internal val STATEMENT_WITH_ID = 101
internal val STATEMENT_WITH_USERID = 102
internal val STATEMENT_STATS_TRIMESTER = 103
internal val STATEMENT_STATS_MONTH = 104
internal val STATEMENT_WIDGET_DATA = 105
internal val STATEMENT_LINEGRAPH_DATA = 106
internal val CATEGORY = 200
internal val CATEGORY_WITH_ACQUIRER = 201
internal val BUDGET = 300
internal val BUDGET_WITH_MONTH = 301
internal val BUDGET_WIDGET = 302
internal val CUREX = 400
internal val CUREX_WITH_BASE = 401
    internal val LOCATION = 500



private val mStatementQueryBuilder:SQLiteQueryBuilder
private val mCurrencyQueryBuilder:SQLiteQueryBuilder



init{
mStatementQueryBuilder = SQLiteQueryBuilder()

 //This is an inner join which looks like
        //statement LEFT JOIN category ON statement.category = category._id
    mStatementQueryBuilder.tables = (DataContract.StatementEntry.TABLE_NAME + " LEFT JOIN " +
            DataContract.CategoryEntry.TABLE_NAME +
            " ON " + DataContract.StatementEntry.TABLE_NAME +
            "." + DataContract.StatementEntry.COLUMN_CATEGORY_KEY +
            " = " + DataContract.CategoryEntry.TABLE_NAME +
            "." + DataContract.CategoryEntry.COLUMN_CATEGORY_USER_KEY)
}

init{
mCurrencyQueryBuilder = SQLiteQueryBuilder()
    mCurrencyQueryBuilder.tables = DataContract.CurrencyExEntry.TABLE_NAME
}



 //statement._ID = ?
    private val sStatementIDSelection = (
DataContract.StatementEntry.TABLE_NAME +
"." + DataContract.StatementEntry.ID + " = ?")


 //currencyex.symbol like '%Base'
    private val sBaseCurrencySelection = (
DataContract.CurrencyExEntry.TABLE_NAME +
"." + DataContract.CurrencyExEntry.COLUMN_SYMBOL + " like ?")


 //statement.account = ? AND date = ?
    private val sAcctnumberAndDateSelection = (
DataContract.StatementEntry.TABLE_NAME +
"." + DataContract.StatementEntry.COLUMN_USER_ID + " = ? AND " +
DataContract.StatementEntry.COLUMN_DATE + " = ? ")


internal fun buildUriMatcher():UriMatcher {
 // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        val matcher = UriMatcher(UriMatcher.NO_MATCH)
val authority = DataContract.CONTENT_AUTHORITY

 // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, DataContract.PATH_STATEMENT, STATEMENT)
matcher.addURI(authority, DataContract.PATH_STATEMENT + "/*", STATEMENT_WITH_ID)
matcher.addURI(authority, DataContract.PATH_STATEMENT + "/#", STATEMENT_WITH_USERID)
matcher.addURI(authority, DataContract.PATH_STATEMENT + "/*/#", STATEMENT_STATS_MONTH)
matcher.addURI(authority, DataContract.PATH_STATEMENT + "/widget/data", STATEMENT_WIDGET_DATA)
matcher.addURI(authority, DataContract.PATH_STATEMENT + "/*/*/#", STATEMENT_STATS_TRIMESTER)
matcher.addURI(authority, DataContract.PATH_STATEMENT + "/*/*/*/#", STATEMENT_LINEGRAPH_DATA)


matcher.addURI(authority, DataContract.PATH_CATEGORY, CATEGORY)
matcher.addURI(authority, DataContract.PATH_CATEGORY + "/*", CATEGORY_WITH_ACQUIRER)

matcher.addURI(authority, DataContract.PATH_BUDGET, BUDGET)
matcher.addURI(authority, DataContract.PATH_BUDGET + "/#", BUDGET_WITH_MONTH)
matcher.addURI(authority, DataContract.PATH_BUDGET + "/widget/#", BUDGET_WIDGET)

matcher.addURI(authority, DataContract.PATH_CUREX, CUREX)
matcher.addURI(authority, DataContract.PATH_CUREX + "/*", CUREX_WITH_BASE)

    matcher.addURI(authority, DataContract.PATH_LOCATION, LOCATION)

return matcher
}
}
}

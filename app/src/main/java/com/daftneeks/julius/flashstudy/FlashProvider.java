package com.daftneeks.julius.flashstudy;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.widget.Toast;

public class FlashProvider extends ContentProvider {

    //classes provider
    static final String PROVIDER_AUTHORITY_NAME = "com.daftneeks.julius.flashstudy";
    static final String URL = "content://" + PROVIDER_AUTHORITY_NAME + "/classes";
    static final Uri CLASSES_CONTENT_URI = Uri.parse(URL);
    static final String SCORE_URL = "content://" + PROVIDER_AUTHORITY_NAME + "/class";
    static final Uri CLASS_SCORE_CONTENT_URI = Uri.parse(SCORE_URL);
    static final Uri DELETE_CLASS_CONTENT_URI = Uri.parse(URL);

    //cards provider
    static final String CARDS_URL = "content://" + PROVIDER_AUTHORITY_NAME + "/cards";
    static final String CARDS_IMAGE_URL = "content://" + PROVIDER_AUTHORITY_NAME + "/cards/image";
    static final Uri CARDS_CONTENT_URI = Uri.parse(CARDS_URL);
    static final Uri CARDS_IMAGE_CONTENT_URI = Uri.parse(CARDS_IMAGE_URL);
    static final String QUIZ_URL = "content://" + PROVIDER_AUTHORITY_NAME + "/quiz";
    static final Uri QUIZ_CONTENT_URI = Uri.parse(QUIZ_URL);

    //database specific constant declaration with columns for the database table
    private SQLiteDatabase database;
    static final String DATABASE_NAME = "User_Database";
    static final int DATABASE_VERSION = 1;
    static final String CLASSES_TABLE_NAME = "classes";
    static final String COLUMN_ROWID = "_id";
    static final String COLUMN_CLASS_NAME = "class_name";
    static final String COLUMN_CLASS_SCORE = "class_score";

    //cards table info
    static final String CARDS_TABLE_NAME = "cards";
    static final String CARDS_COLUMN_ROWID = "_id";
    static final String CARDS_COLUMN_QUESTION = "question";
    static final String CARDS_COLUMN_ANSWER = "answer";
    static final String CARDS_COLUMN_IMAGE_PATH = "image";
    static final String CARDS_CLASSES_COLUMN = "class";

    static final String CREATE_DATABASE_CLASSES_TABLE = "CREATE TABLE " + CLASSES_TABLE_NAME + " (" + COLUMN_ROWID +
            " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_CLASS_NAME + " TEXT NOT NULL, " + COLUMN_CLASS_SCORE + " REAL NOT NULL);";

    static final String CREATE_DATABASE_CARDS_TABLE = "CREATE TABLE " + CARDS_TABLE_NAME + " (" + CARDS_COLUMN_ROWID +
            " INTEGER PRIMARY KEY AUTOINCREMENT, " + CARDS_COLUMN_QUESTION + " TEXT NOT NULL, "
            + CARDS_COLUMN_ANSWER + " TEXT NOT NULL, " + CARDS_COLUMN_IMAGE_PATH + " TEXT, "
            + CARDS_CLASSES_COLUMN + " INTEGER NOT NULL);";


    public FlashProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO: Implement this to handle requests to delete one or more rows.

        int count;
        switch (mURIMatcher.match(uri)){
            case CLASS_DELETE:
                count = database.delete(FlashProvider.CLASSES_TABLE_NAME, FlashProvider.COLUMN_ROWID +
                        " = ?", new String[]{Long.toString(ContentUris.parseId(uri))});
                int cardCount = database.delete(FlashProvider.CARDS_TABLE_NAME, FlashProvider.CARDS_CLASSES_COLUMN +
                        " = ?", new String[]{Long.toString(ContentUris.parseId(uri))});
                if (count > 0 || cardCount > 0){
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return cardCount;

            case CARDS_ID:
                count = database.delete(FlashProvider.CARDS_TABLE_NAME, FlashProvider.CARDS_COLUMN_ROWID +
                        " = ?", new String[]{Long.toString(ContentUris.parseId(uri))});
                if (count > 0){
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return count;

            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        switch (mURIMatcher.match(uri)){
            case CLASSES_LIST:
                return CLASSES_MIME_TYPE;

            case CARDS_LIST:
                return CARDS_MIME_TYPE;

            case CARDS_ID:
                return CARDS_ID_MIME_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.

        long id;
        switch (mURIMatcher.match(uri)){
            case CLASSES_LIST:
                values.remove(FlashProvider.COLUMN_ROWID);
                id = database.insertOrThrow(FlashProvider.CLASSES_TABLE_NAME, null, values);
            break;

            case CARDS_LIST:
                values.remove(FlashProvider.CARDS_COLUMN_ROWID);
                id = database.insertOrThrow(FlashProvider.CARDS_TABLE_NAME, null, values);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);

        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);

    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        database = new DatabaseHelper(getContext()).getWritableDatabase();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients.

        //using the UriMatcher to see the query type and format the db query accordingly
        Cursor cursor;
        switch (mURIMatcher.match(uri)) {
            case CLASSES_LIST:
                cursor = database.query(FlashProvider.CLASSES_TABLE_NAME, null,
                        null, null, null, null, FlashProvider.COLUMN_CLASS_NAME);
                if (cursor != null && cursor.getCount() > 0){
                    cursor.moveToFirst();
                }
                break;

            case CLASS_SCORE:
                cursor = database.query(FlashProvider.CLASSES_TABLE_NAME, null,
                        FlashProvider.COLUMN_ROWID + "=" + ClassDetailsFragment.class_ID, null, null, null, null);
                if (cursor != null && cursor.getCount() > 0){
                    cursor.moveToFirst();
                }
                break;

            case CARDS_LIST:
                cursor = database.query(FlashProvider.CARDS_TABLE_NAME, null,
                        FlashProvider.CARDS_CLASSES_COLUMN + "=" + ClassDetailsFragment.class_ID, null, null, null, null);
                break;

            case QUIZ:
                    cursor = database.query(FlashProvider.CARDS_TABLE_NAME, null,
                            FlashProvider.CARDS_CLASSES_COLUMN + "=" + ClassDetailsFragment.class_ID, null, null, null, "RANDOM()", "1");
                break;

            case CARDS_ID:
                projection = new String[] {FlashProvider.CARDS_COLUMN_ROWID,
                        FlashProvider.CARDS_COLUMN_QUESTION, FlashProvider.CARDS_COLUMN_ANSWER,
                        FlashProvider.CARDS_COLUMN_IMAGE_PATH};
                cursor = database.query(FlashProvider.CARDS_TABLE_NAME, projection,
                        FlashProvider.CARDS_COLUMN_ROWID + "=" + CardDetailsFragment.card_ID, null, null, null, null);
                if (cursor != null && cursor.getCount() > 0){
                    cursor.moveToFirst();
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.

        switch (mURIMatcher.match(uri)){
            case CARDS_ID_IMAGE:
                int count = database.update(FlashProvider.CARDS_TABLE_NAME, values, FlashProvider.CARDS_COLUMN_ROWID +
                " = ?", new String[] { Long.toString(ContentUris.parseId(uri)) });
                if (count > 0){
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return count;

            case CLASS_SCORE:
                int count1 = database.update(FlashProvider.CLASSES_TABLE_NAME, values, FlashProvider.COLUMN_ROWID +
                " = ?", new String[] { Long.toString(ContentUris.parseId(uri)) });
                if (count1 > 0){
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return count1;

            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    private class DatabaseHelper extends SQLiteOpenHelper{
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DATABASE_CLASSES_TABLE);
            db.execSQL(CREATE_DATABASE_CARDS_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    public static final String CLASSES_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.daftneeks.julius.flashstudy.classes";
    public static final String CARDS_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.daftneeks.julius.flashstudy.cards";
    public static final String CARDS_ID_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.daftneeks.julius.flashstudy.cards";

    //Uri matcher stuff
    private static final int CLASSES_LIST = 100;
    private static final int CLASS_SCORE = 110;
    private static final int CARDS_LIST = 200;
    private static final int CARDS_ID = 201;
    private static final int CARDS_ID_IMAGE = 202;
    private static final int CLASS_DELETE = 120;
    private static final int QUIZ = 500;
    private static final UriMatcher mURIMatcher = buildUriMatcher();


    private static UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_AUTHORITY_NAME, "classes", CLASSES_LIST);
        uriMatcher.addURI(PROVIDER_AUTHORITY_NAME, "class", CLASS_SCORE);
        uriMatcher.addURI(PROVIDER_AUTHORITY_NAME, "class/#", CLASS_SCORE);
        uriMatcher.addURI(PROVIDER_AUTHORITY_NAME, "classes/#", CLASS_DELETE);
        uriMatcher.addURI(PROVIDER_AUTHORITY_NAME, "cards", CARDS_LIST);
        uriMatcher.addURI(PROVIDER_AUTHORITY_NAME, "cards/image/#", CARDS_ID_IMAGE);
        uriMatcher.addURI(PROVIDER_AUTHORITY_NAME, "cards/#", CARDS_ID);
        uriMatcher.addURI(PROVIDER_AUTHORITY_NAME, "quiz", QUIZ);
        return uriMatcher;
    }
}

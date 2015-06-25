package com.daftneeks.julius.flashstudy;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Julius on 15/06/2015.
 */
public class CustomListViewAdapter extends BaseAdapter {

    /** The parent context */
    private Context myContext;
    private Cursor cursor;

    /** Simple Constructor saving the 'parent' context. and cursor */
    public CustomListViewAdapter(Context c, Cursor cursor) {
        this.myContext = c;
        this.cursor = cursor;
        LayoutInflater inflater = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


    }

    @Override
    public int getCount() {
        return cursor.getCount();
    }

    @Override
    public Object getItem(int position) {
        return cursor.getPosition();
    }

    @Override
    public long getItemId(int position) {
        cursor.moveToPosition(position);
        return cursor.getLong(0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {



        cursor.moveToPosition(position);
        LayoutInflater inflater = (LayoutInflater) myContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = convertView;

        if (convertView == null)
        rowView = inflater.inflate(R.layout.list_item, parent, false);

        TextView question = (TextView) rowView.findViewById(R.id.cardQuestionHolder);
        question.setText(cursor.getString(cursor.getColumnIndex(FlashProvider.CARDS_COLUMN_QUESTION)));

        ImageView cardImage = (ImageView) rowView.findViewById(R.id.cardImageViewHolder);
        Bitmap bitmap = BitmapFactory.decodeFile(cursor.getString(cursor.getColumnIndex(FlashProvider.CARDS_COLUMN_IMAGE_PATH)));
        cardImage.setImageBitmap(bitmap);

        return rowView;
    }
}

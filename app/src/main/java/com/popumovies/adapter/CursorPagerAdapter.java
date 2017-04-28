package com.popumovies.adapter;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

public class CursorPagerAdapter<F extends Fragment> extends FragmentStatePagerAdapter {
    private final String LOG_TAG = getClass().getSimpleName();
    private final Class<F> fragmentClass;
    private Cursor cursor;
//    private ViewPager mPager;
//    private Map FragmentTagsPositions;

    public CursorPagerAdapter(FragmentManager fm, Class<F> fragmentClass, String[] projection, Cursor cursor) {
        super(fm);
        this.fragmentClass = fragmentClass;
        this.cursor = cursor;
    }

    @Override
    public F getItem(int position) {
        if (cursor == null) // shouldn't happen
            return null;

        cursor.moveToPosition(position);
        F frag;
        try {
            frag = fragmentClass.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        Bundle args = new Bundle();
        Log.d(LOG_TAG,"name => " + cursor.getString(2));
        for (int i = 0; i < cursor.getColumnCount(); ++i) {
            args.putString(cursor.getColumnName(i), cursor.getString(i));
        }
//        args.putInt("position", position);
//        for (int i = 0; i < projection.length; ++i) {
//            args.putString(projection[i], cursor.getString(i));
//        }
        frag.setArguments(args);
        return frag;
    }

    @Override
    public int getCount() {
        if (cursor == null)
            return 0;
        else
            return cursor.getCount();
    }

    //    @Override
//    public Object instantiateItem(ViewGroup container, int position) {
//        Fragment frag = (Fragment) super.instantiateItem(container, position);
//        FragmentTagsPositions.put(position, frag.getTag());
//        return frag;
//    }
//
//    public String getFragmentTagByPosition(int position) {
//        return (String)FragmentTagsPositions.get(position);
//    }

    public void swapCursor(Cursor c) {
        if (cursor == c)
            return;

        this.cursor = c;
        notifyDataSetChanged();

    }
    public void swapCursor(Cursor c,ViewPager vp) {
        if (cursor == c)
            return;

        this.cursor = c;
        notifyDataSetChanged();
    }

    public Cursor getCursor() {
        return cursor;
    }
}
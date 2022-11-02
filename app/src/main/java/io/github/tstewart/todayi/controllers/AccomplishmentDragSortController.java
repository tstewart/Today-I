package io.github.tstewart.todayi.controllers;

import static android.view.View.GONE;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.opengl.Visibility;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import io.github.tstewart.todayi.R;

/* Uses partial implementation of SimpleFloatViewManager, with modifications to hide description from float view.
 * Author: bauerca
 * https://github.com/bauerca/drag-sort-listview/blob/master/library/src/com/mobeta/android/dslv/SimpleFloatViewManager.java */
public class AccomplishmentDragSortController extends DragSortController {

    ListView mListView;

    Bitmap mFloatBitmap;

    ImageView mImageView;

    Context mContext;

    public AccomplishmentDragSortController(Context context, DragSortListView dslv) {
        super(dslv);
        mListView = dslv;
        mContext = context;
    }

    @Override
    public View onCreateFloatView(int pos) {

        View view = mListView.getChildAt(pos+mListView.getHeaderViewsCount()-mListView.getFirstVisiblePosition());

        if (view != null) {

            TextView descriptionTextView = view.findViewById(R.id.textViewDescription);
            if(descriptionTextView != null) {
                int targetHiddenHeight = view.getHeight() - descriptionTextView.getHeight();
                if(targetHiddenHeight<=0) targetHiddenHeight = view.getHeight();


                mFloatBitmap = Bitmap.createBitmap(view.getWidth(), targetHiddenHeight, Bitmap.Config.ARGB_8888);

                if (mImageView == null) {
                    mImageView = new ImageView(mListView.getContext());
                }
                mImageView.setBackgroundColor(mContext.getColor(R.color.colorDragHoverBackground));
                mImageView.setPadding(0, 0, 0, 0);
                mImageView.setImageBitmap(mFloatBitmap);
                mImageView.setLayoutParams(new ViewGroup.LayoutParams(view.getWidth(), targetHiddenHeight));

                return mImageView;
            }

            return null;
        }
        return null;
    }

    @Override
    public void onDragFloatView(View view, Point point, Point point1) {

    }

    @Override
    public void onDestroyFloatView(View view) {
        ((ImageView) view).setImageDrawable(null);

        mFloatBitmap.recycle();
        mFloatBitmap = null;
    }
}

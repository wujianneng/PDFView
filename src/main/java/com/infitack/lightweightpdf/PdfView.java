package com.infitack.lightweightpdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.senab.photoview.PhotoView;

public class PdfView extends VerticalViewPager {

    List<PhotoView> views = new ArrayList<>();
    PdfRenderer.Page mCurrentPage = null;
    ParcelFileDescriptor mFileDescriptor;
    PdfRenderer mPdfRenderer;
    Context mContext;
    Map<Integer, Bitmap> bitmapMap = new HashMap<>();

    public PdfView(Context context) {
        super(context);
        mContext = context;
    }

    public PdfView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public void loadPdf(final File file) throws Exception {
        if (Build.VERSION.SDK_INT >= 21) {
            mFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            if (mFileDescriptor != null) {
                mPdfRenderer = new PdfRenderer(mFileDescriptor);
                final int count = mPdfRenderer.getPageCount();
                for (int i = 0; i < count; i++) {
                    PhotoView view = (PhotoView) View.inflate(mContext, R.layout.photo_view, null);
                    views.add(view);
                }
                setAdapter(new CustomPagerAdapter(count));
                setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int i, float v, int i1) {

                    }

                    @Override
                    public void onPageSelected(int i) {
                        showImage(i);
                    }

                    @Override
                    public void onPageScrollStateChanged(int i) {

                    }
                });
                showImage(0);
            }
        }
    }


    public void showImage(int index) {
        if (null != mCurrentPage) {
            mCurrentPage.close();
        }
        mCurrentPage = mPdfRenderer.openPage(index);
        Bitmap mbitmap = null;
        if (!bitmapMap.containsKey(index)) {
            mbitmap = Bitmap.createBitmap(mCurrentPage.getWidth(), mCurrentPage.getHeight(),
                    Bitmap.Config.ARGB_4444);
            mCurrentPage.render(mbitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            bitmapMap.put(index, mbitmap);
        } else {
            mbitmap = bitmapMap.get(index);
        }
        views.get(index).setImageBitmap(mbitmap);
    }

    public class CustomPagerAdapter extends PagerAdapter {
        //数据
        private int mcount;

        public CustomPagerAdapter(int count) {
            mcount = count;
        }

        @Override
        public int getCount() {
            return mcount;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = views.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }


    public void recycleResources() {
        try {
            if (mCurrentPage != null)
                mCurrentPage.close();
            if (mPdfRenderer != null)
                mPdfRenderer.close();
            if (mFileDescriptor != null)
                mFileDescriptor.close();
            if (views != null)
                views.clear();
            if (bitmapMap.size() != 0) {
                for (Bitmap bitmap : bitmapMap.values()) {
                    bitmap.recycle();
                }
                bitmapMap.clear();
            }
        } catch (Exception e) {
        }
    }
}

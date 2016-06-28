package com.sam_chordas.android.stockhawk.service;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

public class WidgetRemoteViewsService extends RemoteViewsService {
   @Override
   public RemoteViewsFactory onGetViewFactory(Intent intent) {
      // return remote view factory
      return new RemoteViewsFactory() {
         Cursor mCursor = null;

         @Override
         public void onCreate() {
            // nothing to do
         }

         @Override
         public void onDataSetChanged() {
            if (mCursor != null)
               mCursor.close();
            // calling Binder.clearCallingIdentity() and Binder.restoreCallingIdentity() is necessary
            // to avoid java.lang.SecurityException: Permission Denial: reading
            // com.sam_chordas.android.stockhawk.data.generated.QuoteProvider
            // uri content://com.sam_chordas.android.stockhawk.data.QuoteProvider/quotes
            // from pid=1563, uid=10039 requires the provider be exported, or grantUriPermission()
            long identityToken = Binder.clearCallingIdentity();
            mCursor = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                  new String[]{ QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                  QuoteColumns.ISCURRENT + " = ?",
                  new String[]{"1"},
                  null);
            Binder.restoreCallingIdentity(identityToken);
         }

         @Override
         public void onDestroy() {
            if (mCursor != null) {
               mCursor.close();
               mCursor = null;
            }
         }

         @Override
         public int getCount() {
            return mCursor == null ? 0 : mCursor.getCount();
         }

         @Override
         public RemoteViews getViewAt(int position) {
            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.stock_widget_item);
            if (mCursor != null && mCursor.moveToPosition(position)) {
               remoteViews.setTextViewText(R.id.stock_symbol, mCursor.getString(mCursor.getColumnIndex("symbol")));
               remoteViews.setTextViewText(R.id.bid_price, mCursor.getString(mCursor.getColumnIndex("bid_price")));
               remoteViews.setTextViewText(R.id.change, mCursor.getString(mCursor.getColumnIndex("change")));
            }
            return remoteViews;
         }

         @Override
         public RemoteViews getLoadingView() {
            return null;
         }

         @Override
         public int getViewTypeCount() {
            return 1;
         }

         @Override
         public long getItemId(int position) {
            return position;
         }

         @Override
         public boolean hasStableIds() {
            return true;
         }
      };
   }
}

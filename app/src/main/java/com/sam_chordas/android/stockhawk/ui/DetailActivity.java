package com.sam_chordas.android.stockhawk.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private LineChartView mLineChart;
    private LineSet mLineSet;
    Context context;
    boolean isConnected;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                 NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                 isConnected = activeNetwork != null &&
                 activeNetwork.isConnectedOrConnecting();
                 setContentView(R.layout.activity_line_graph);

        mLineSet = new LineSet();

        mLineChart = (LineChartView) findViewById(R.id.linechart);
        assert mLineChart != null;
        mLineChart.setAxisBorderValues(0, 1000, 50)
                .setLabelsColor(getResources().getColor(R.color.material_red_700))
                .setXLabels(AxisController.LabelPosition.NONE)
                .setGrid(ChartView.GridType.FULL, new Paint(R.color.material_blue_700));
        Intent intent = getIntent();
        Bundle arguments = new Bundle();
        arguments.putString("symbol",
                intent.getStringExtra("symbol"));
        getLoaderManager().initLoader(0, arguments, this);

        Log.v("check", "check" + isConnected);
        if (!isConnected) {
            setTitle(R.string.error_loading);
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setMessage(R.string.graph_error);

            builder.setNegativeButton(R.string.exit_now, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });
            builder.setPositiveButton(R.string.continue_now, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (isConnected) {
                                dialog.dismiss();
                            }
                        }

                    }
            );
            AlertDialog dialog = builder.create(); // calling builder.create after adding buttons
            dialog.show();
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI, new String[]{QuoteColumns.BIDPRICE},
                QuoteColumns.SYMBOL + " = ?",
                new String[]{args.getString("symbol")}, null);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            float price = Float.parseFloat(cursor.getString(cursor.getColumnIndex(QuoteColumns.BIDPRICE)));
            mLineSet.addPoint(String.valueOf(i), price);
            cursor.moveToNext();
        }
        mLineSet.setColor(getResources().getColor(R.color.material_red_700));
        mLineChart.addData(mLineSet);
        mLineChart.show();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
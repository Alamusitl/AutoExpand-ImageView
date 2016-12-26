package com.alms.simple;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RelativeLayout;

import com.alms.expand.AutoExpandImageView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AutoExpandImageView downloadView = new AutoExpandImageView(this);
        int minWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
        int maxWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 160, getResources().getDisplayMetrics());
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
        downloadView.setWidth(minWidth, maxWidth, height);
        downloadView.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.down_unfold));
        downloadView.setDownloadListener(new AutoExpandImageView.OnClickListener() {
            @Override
            public void onClickView() {
                Log.i("MainActivity", "onClickView");
            }
        });
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lp.bottomMargin = 26;
        downloadView.setLayoutParams(lp);

        RelativeLayout mRootView = (RelativeLayout) findViewById(R.id.activity_main);
        mRootView.addView(downloadView);
    }
}

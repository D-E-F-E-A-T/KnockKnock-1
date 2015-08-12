package com.markmao.pulltorefresh.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.markmao.pulltorefresh.R;

/**
 * The footer view for {@link com.markmao.pulltorefresh.widget.XListView} and
 * {@link com.markmao.pulltorefresh.widget.XScrollView}
 *
 * @author markmjw
 * @date 2013-10-08
 */
public class XFooterView extends LinearLayout {
    public static final int STATE_NORMAL = 0;
    public static final int STATE_READY = 1;
    public static final int STATE_LOADING = 2;

    private View mLayout;

    private View mProgressBar;

    private TextView mHintView;

    private int mState = STATE_NORMAL;

    public XFooterView(Context context) {
        super(context);
        initView(context);
    }

    public XFooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        mLayout = LayoutInflater.from(context).inflate(R.layout.vw_footer, this);
        mLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));

        mProgressBar = mLayout.findViewById(R.id.footer_progressbar);
        mHintView = (TextView) mLayout.findViewById(R.id.footer_hint_text);

    }

    /**
     * Set footer view state
     *
     * @see #STATE_LOADING
     * @see #STATE_NORMAL
     * @see #STATE_READY
     */
    public void setState(int state) {
        if (state == mState) return;

        if (state == STATE_LOADING) {
//            mHintImage.clearAnimation();
//            mHintImage.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
            mHintView.setVisibility(View.INVISIBLE);
        } else {
            mHintView.setVisibility(View.VISIBLE);
//            mHintImage.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.INVISIBLE);
        }

        switch (state) {
            case STATE_NORMAL:
//                if (mState == STATE_READY) {
//                    mHintImage.startAnimation(mRotateDownAnim);
//                }
//                if (mState == STATE_LOADING) {
//                    mHintImage.clearAnimation();
//                }
                mHintView.setText(R.string.footer_hint_load_normal);
                break;

            case STATE_READY:
                if (mState != STATE_READY) {
//                    mHintImage.clearAnimation();
//                    mHintImage.startAnimation(mRotateUpAnim);
                    mHintView.setText(R.string.footer_hint_load_ready);
                }
                break;

            case STATE_LOADING:
                break;
        }

        mState = state;
    }

    /**
     * Set footer view bottom margin.
     */
    public void setBottomMargin(int margin) {
        if (margin < 0) return;
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mLayout.getLayoutParams();
        lp.bottomMargin = margin;
        mLayout.setLayoutParams(lp);
    }

    /**
     * Get footer view bottom margin.
     */
    public int getBottomMargin() {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mLayout.getLayoutParams();
        return lp.bottomMargin;
    }

    /**
     * normal status
     */
    @SuppressWarnings("unused")
    public void normal() {
        mHintView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    /**
     * loading status
     */
    @SuppressWarnings("unused")
    public void loading() {
        mHintView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * hide footer when disable pull load more
     */
    public void hide() {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mLayout.getLayoutParams();
        lp.height = 0;
        mLayout.setLayoutParams(lp);
    }

    /**
     * show footer
     */
    public void show() {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mLayout.getLayoutParams();
        lp.height = LayoutParams.WRAP_CONTENT;
        mLayout.setLayoutParams(lp);
    }

}

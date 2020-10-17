package com.baianju.live_plugin.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.baianju.live_plugin.R;


public class TalkView extends RelativeLayout {
    OnHalfTalkLsn onHalfTalkLsn;
    TranslateAnimation mShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);

    public void setOnHalfTalkLsn(OnHalfTalkLsn onHalfTalkLsn) {
        this.onHalfTalkLsn = onHalfTalkLsn;
    }

    public TalkView(Context context) {
        super(context);
        init();
    }

    public TalkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void show() {
        mShowAction.setDuration(500);
        startAnimation(mShowAction);
        setVisibility(View.VISIBLE);
        bringToFront();
    }

    public void hide() {
        setVisibility(View.GONE);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        View parentView = LayoutInflater.from(getContext()).inflate(R.layout.video_half_talk, this, true);
        ImageButton talk_close_btn = parentView.findViewById(R.id.talk_close_btn);
        talk_close_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onHalfTalkLsn != null) {
                    onHalfTalkLsn.close();
                }
                TalkView.this.hide();
            }
        });
        final Button start_talk = parentView.findViewById(R.id.start_talk);
        start_talk.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (onHalfTalkLsn != null) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        start_talk.setBackgroundResource(R.drawable.video_half_duplex2);
                        onHalfTalkLsn.onDown();
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        start_talk.setBackgroundResource(R.drawable.video_half_duplex1);
                        onHalfTalkLsn.onUp();
                    }
                }
                return true;
            }
        });
    }


    public interface OnHalfTalkLsn {
        void onDown();

        void onUp();

        void close();
    }

}

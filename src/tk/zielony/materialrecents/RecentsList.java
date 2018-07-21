package tk.zielony.materialrecents;

import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.meui.SwipeRecentApps.*;
import com.noas.view.*;

/**
 * Created by Marcin on 2015-04-13.
 * Bugs fix come from https://github.com/m0r0/MaterialRecents
 * Modify by ME Failed Coder
 */
public class RecentsList extends FrameLayout implements GestureDetector.OnGestureListener {
    Scroller scroller;
    RecentsAdapter adapter;
    GestureDetector gestureDetector;// = new GestureDetector(this);
    int scroll = 0;
    OnItemClickListener onItemClickListener;
    Rect childTouchRect[];

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public RecentsList(Context context) {
        super(context);
        initRecentsList();
    }

    public RecentsList(Context context, AttributeSet attrs) {
        super(context, attrs);
        initRecentsList();
    }

    public RecentsList(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initRecentsList();
    }

    /* @TargetApi(Build.VERSION_CODES.LOLLIPOP)
	 public RecentsList(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
	 super(context, attrs, defStyleAttr, defStyleRes);
	 initRecentsList();
	 } */

    private void initRecentsList() {
        scroller = new Scroller(getContext());
		setClipChildren(false);
	    setClipToPadding(false);
		gestureDetector = new GestureDetector(getContext(), this);
		gestureDetector.setIsLongpressEnabled(false);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public RecentsAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(RecentsAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (adapter == null)
            return;

        if (getChildCount() != adapter.getCount())
            initChildren();

        if (childTouchRect == null) childTouchRect = new Rect[getChildCount()];
		for (int i = 0; i < getChildCount(); i++) {
			getChildAt(i).layout(0, 0, getWidth() - getPaddingLeft() - getPaddingRight(), getWidth() - getPaddingLeft() - getPaddingRight());
			childTouchRect[i] = new Rect();
		}
    }

    private void initChildren() {
        removeAllViews();
        for (int i = 0; i < adapter.getCount(); i++) {
            final View card = View.inflate(getContext(), R.layout.material_recents_card, null);
            TextView title = (TextView) card.findViewById(R.id.materialrecents_recentTitle);
            title.setText(adapter.getTitle(i));
            android.widget.ImageView icon = (android.widget.ImageView) card.findViewById(R.id.materialrecents_recentIcon);
            Drawable drawable = adapter.getIcon(i);
            if (drawable == null) {
                icon.setVisibility(View.GONE);
            } else {
                icon.setImageDrawable(drawable);
            }
            View header = card.findViewById(R.id.materialrecents_recentHeader);
            header.setBackgroundColor(adapter.getHeaderColor(i));
            card.findViewById(R.id.materialrecents_recentContent).setBackgroundColor(adapter.getViewColor(i));
			// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			//     card.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            addView(card, i, generateDefaultLayoutParams());
            /*final int finalI = i;
			 card.setOnTouchListener(new OnTouchListener() {

			 @Override
			 public boolean onTouch(View p1, MotionEvent p2) {
			 // TODO: Implement this method
			 if (onItemClickListener != null)
			 onItemClickListener.onItemClick(card, finalI);

			 return true;
			 }});
			 /*
			 @Override
			 public void onClick(View view) {
			 if (onItemClickListener != null)
			 onItemClickListener.onItemClick(card, finalI);
			 }
			 });*/
        }
    }

    private void layoutChildren() {
		int width = getWidth() - getPaddingLeft() - getPaddingRight();
		int height = getHeight() - getPaddingTop() - getPaddingBottom();
		float topSpace = (height - width);
        for (int i = 0; i < getChildCount(); i++) {
			final View currentChild = getChildAt(i);
            int y = (int) (topSpace * Math.pow(2, (i * (width) - scroll) / (float) (width)));
            float scale = (float) (-Math.pow(2, -y / topSpace / 10.0f) + 19.0f / 10);
            childTouchRect[i].set(getPaddingLeft(), y + getPaddingTop(), (int) (scale * (getPaddingLeft() + getWidth() - getPaddingLeft() - getPaddingRight())), (int) (scale * (y + getPaddingTop() + getWidth() - getPaddingLeft() - getPaddingRight())));
			currentChild.layout(0, 0, getWidth() - getPaddingLeft() - getPaddingRight(), getHeight() - getPaddingTop() - getPaddingBottom()); //getWidth() - getPaddingLeft() - getPaddingRight());
            ViewHelper.setTranslationX(currentChild, getPaddingLeft());
            ViewHelper.setTranslationY(currentChild, y + getPaddingTop());
            ViewHelper.setScaleX(currentChild, scale);
            ViewHelper.setScaleY(currentChild, scale);
        }
    }

    private int getMaxScroll() {
        return (getChildCount() - 1) * (getWidth() - getPaddingLeft() - getPaddingRight());
    }

	boolean isFirst = true;
    @Override
    protected void dispatchDraw(Canvas canvas) {
		if (isFirst) {
			scroll = getMaxScroll();
			//postInvalidate();
		}
        layoutChildren();
        //requestLayout();
        super.dispatchDraw(canvas);
        //doScrolling();
		isFirst = false;
    }

	/*@Override
	 public boolean dispatchTouchEvent(MotionEvent ev) {
	 // TODO: Implement this method
	 gestureDetector.onTouchEvent(ev);if (ev.getAction() == MotionEvent.ACTION_DOWN || ev.getAction() == MotionEvent.ACTION_MOVE) {
	 forceFinished();
	 }

	 return true;
	 }*/


	/*
	 @Override
	 public boolean dispatchTouchEvent(MotionEvent event) {

	 if (gestureDetector.onTouchEvent(event)) {
	 for (int i = getChildCount() - 1;i >= 0; i--) { // TEST!
	 MotionEvent e = MotionEvent.obtain(event);
	 event.setAction(MotionEvent.ACTION_CANCEL);
	 e.offsetLocation(-childTouchRect[i].left, -childTouchRect[i].top);
	 getChildAt(i).dispatchTouchEvent(e);
	 }
	 return true;
	 }

	 if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
	 forceFinished();
	 }

	 for (int i = getChildCount() - 1;i >= 0; i--) {
	 if (childTouchRect[i].contains((int) event.getX(), (int) event.getY())) {
	 MotionEvent e = MotionEvent.obtain(event);
	 e.offsetLocation(-childTouchRect[i].left, -childTouchRect[i].top);
	 if (getChildAt(i).dispatchTouchEvent(e))
	 break;
	 }
	 }

	 return true;
	 }*/
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return /*event.getAction() == MotionEvent.ACTION_DOWN ||= */ gestureDetector.onTouchEvent(event);
	}
    @Override
    public boolean onDown(MotionEvent motionEvent) {
		forceFinished();
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
		if (onItemClickListener == null) return true;
		for (int i = getChildCount() - 1;i >= 0; i--) {
			if (childTouchRect[i].contains((int) event.getX(), (int) event.getY())) {
				onItemClickListener.onItemClick(getChildAt(i), i);
				break;
			}
		}
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
        scroll = (int) Math.max(0, Math.min(scroll + v2, getMaxScroll()));
        postInvalidate();
        return true;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
    }

    void startScrolling(float initialVelocity) {
        scroller.fling(0, scroll, 0, (int) initialVelocity, 0,
					   0, Integer.MIN_VALUE, Integer.MAX_VALUE);

        postInvalidate();
    }
	@Override 
	public void computeScroll() {
		if (scroller.isFinished())
			return;

		if (scroller.computeScrollOffset()) {
			final int newScroll = Math.max(0, Math.min(scroller.getCurrY(), getMaxScroll()));
			if (newScroll != scroll) {
				scroll = newScroll;
				postInvalidate();
			}
		}
	}

    /*private void doScrolling() {
	 if (scroller.isFinished())
	 return;

	 boolean more = scroller.computeScrollOffset();
	 int y = scroller.getCurrY();

	 scroll = Math.max(0, Math.min(y, getMaxScroll()));

	 if (more)
	 postInvalidate();
	 }*/

    boolean isFlinging() {
        return !scroller.isFinished();
    }

    void forceFinished() {
        if (!scroller.isFinished()) {
            scroller.forceFinished(true);
        }
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
		forceFinished();
        startScrolling(-v2 * 2);
        return true;
    }

}

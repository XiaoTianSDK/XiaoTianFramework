package com.xiaotian.framework.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.widget.CompoundButton;

import com.xiaotian.framework.util.UtilLayoutAttribute;

import java.util.Locale;

public class Switch extends CompoundButton {
	private static final int TOUCH_MODE_IDLE = 0;
	private static final int TOUCH_MODE_DOWN = 1;
	private static final int TOUCH_MODE_DRAGGING = 2;
	// Enum for the "typeface" XML parameter.
	private static final int SANS = 1;
	private static final int SERIF = 2;
	private static final int MONOSPACE = 3;
	private Drawable mThumbDrawable;
	private Drawable mTrackDrawable;
	private int mThumbTextPadding;
	private int mSwitchMinWidth;
	private int mSwitchPadding;
	private CharSequence mTextOn;
	private CharSequence mTextOff;
	private int mTouchMode;
	private int mTouchSlop;
	private float mTouchX;
	private float mTouchY;
	private VelocityTracker mVelocityTracker = VelocityTracker.obtain();
	private int mMinFlingVelocity;
	private float mThumbPosition;
	private int mSwitchWidth;
	private int mSwitchHeight;
	private int mThumbWidth; // Does not include padding
	private int mSwitchLeft;
	private int mSwitchTop;
	private int mSwitchRight;
	private int mSwitchBottom;
	private TextPaint mTextPaint;
	private ColorStateList mTextColors;
	private Layout mOnLayout;
	private Layout mOffLayout;
	private TransformationMethodCompat2 mSwitchTransformationMethod;
	private final Rect mTempRect = new Rect();
	private static final int[] CHECKED_STATE_SET = { android.R.attr.state_checked };

	public Switch(Context context) {
		this(context, null);
	}

	public Switch(Context context, AttributeSet attrs) {
		//		this(context, attrs, R.attr.switchStyle);
		this(context, attrs, -1);
	}

	public Switch(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		Resources res = getResources();
		mTextPaint.density = res.getDisplayMetrics().density;
		// TODO resolve error
		UtilLayoutAttribute ua = new UtilLayoutAttribute(context, attrs);
		mThumbDrawable = ua.getDrawableAttribute("thumb");
		mTrackDrawable = ua.getDrawableAttribute("track");
		mTextOn = ua.getStringAttribute("textOn");
		mTextOff = ua.getStringAttribute("textOff");
		mThumbTextPadding = (int) ua.getDimension(ua.getNSXiaoTian(), "thumbTextPadding", 0);
		mSwitchMinWidth = (int) ua.getDimension(ua.getNSXiaoTian(), "switchMinWidth", 0);
		mSwitchPadding = (int) ua.getDimension(ua.getNSXiaoTian(), "switchPadding", 0);
		int appearance = ua.getAttributeResourceValue(ua.getNSXiaoTian(), "switchTextAppearance", 0);
		if (appearance != 0) {
			setSwitchTextAppearance(context, appearance);
		}
		ViewConfiguration config = ViewConfiguration.get(context);
		mTouchSlop = config.getScaledTouchSlop();
		mMinFlingVelocity = config.getScaledMinimumFlingVelocity();
		// Refresh display with current params
		refreshDrawableState();
		setChecked(isChecked());
		// TODO I don't know why, but in order for the OnCheckedChangeListener to work this switch has to have an OnClickListener
		this.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// do nothing
			}
		});
	}

	public void setSwitchTextAppearance(Context context, int resid) {
		//
		//		TypedArray appearance = context.obtainStyledAttributes(resid, R.styleable.TextAppearanceSwitch);
		//		ColorStateList colors;
		//		int ts;
		//		colors = appearance.getColorStateList(R.styleable.TextAppearanceSwitch_textColor);
		//		if (colors != null) {
		//			mTextColors = colors;
		//		} else {
		//			// If no color set in TextAppearance, default to the view's textColor
		//			mTextColors = getTextColors();
		//		}
		//		ts = appearance.getDimensionPixelSize(R.styleable.TextAppearanceSwitch_textSize, 0);
		//		if (ts != 0) {
		//			if (ts != mTextPaint.getTextSize()) {
		//				mTextPaint.setTextSize(ts);
		//				requestLayout();
		//			}
		//		}
		//		int typefaceIndex, styleIndex;
		//		typefaceIndex = appearance.getInt(R.styleable.TextAppearanceSwitch_typeface, -1);
		//		styleIndex = appearance.getInt(R.styleable.TextAppearanceSwitch_textStyle, -1);
		//		setSwitchTypefaceByIndex(typefaceIndex, styleIndex);
		//		boolean allCaps = appearance.getBoolean(R.styleable.TextAppearanceSwitch_textAllCaps, false);
		//		if (allCaps) {
		//			mSwitchTransformationMethod = new AllCapsTransformationMethod(getContext());
		//			mSwitchTransformationMethod.setLengthChangesAllowed(true);
		//		} else {
		//			mSwitchTransformationMethod = null;
		//		}
		//		appearance.recycle();
	}

	private void setSwitchTypefaceByIndex(int typefaceIndex, int styleIndex) {
		Typeface tf = null;
		switch (typefaceIndex) {
		case SANS:
			tf = Typeface.SANS_SERIF;
			break;
		case SERIF:
			tf = Typeface.SERIF;
			break;
		case MONOSPACE:
			tf = Typeface.MONOSPACE;
			break;
		}
		setSwitchTypeface(tf, styleIndex);
	}

	public void setSwitchTypeface(Typeface tf, int style) {
		if (style > 0) {
			if (tf == null) {
				tf = Typeface.defaultFromStyle(style);
			} else {
				tf = Typeface.create(tf, style);
			}
			setSwitchTypeface(tf);
			// now compute what (if any) algorithmic styling is needed
			int typefaceStyle = tf != null ? tf.getStyle() : 0;
			int need = style & ~typefaceStyle;
			mTextPaint.setFakeBoldText((need & Typeface.BOLD) != 0);
			mTextPaint.setTextSkewX((need & Typeface.ITALIC) != 0 ? -0.25f : 0);
		} else {
			mTextPaint.setFakeBoldText(false);
			mTextPaint.setTextSkewX(0);
			setSwitchTypeface(tf);
		}
	}

	public void setSwitchTypeface(Typeface tf) {
		if (mTextPaint.getTypeface() != tf) {
			mTextPaint.setTypeface(tf);
			requestLayout();
			invalidate();
		}
	}

	public void setSwitchPadding(int pixels) {
		mSwitchPadding = pixels;
		requestLayout();
	}

	public int getSwitchPadding() {
		return mSwitchPadding;
	}

	public void setSwitchMinWidth(int pixels) {
		mSwitchMinWidth = pixels;
		requestLayout();
	}

	public int getSwitchMinWidth() {
		return mSwitchMinWidth;
	}

	public void setThumbTextPadding(int pixels) {
		mThumbTextPadding = pixels;
		requestLayout();
	}

	public int getThumbTextPadding() {
		return mThumbTextPadding;
	}

	public void setTrackDrawable(Drawable track) {
		mTrackDrawable = track;
		requestLayout();
	}

	public void setTrackResource(int resId) {
		setTrackDrawable(getContext().getResources().getDrawable(resId));
	}

	public Drawable getTrackDrawable() {
		return mTrackDrawable;
	}

	public void setThumbDrawable(Drawable thumb) {
		mThumbDrawable = thumb;
		requestLayout();
	}

	public void setThumbResource(int resId) {
		setThumbDrawable(getContext().getResources().getDrawable(resId));
	}

	public Drawable getThumbDrawable() {
		return mThumbDrawable;
	}

	public CharSequence getTextOn() {
		return mTextOn;
	}

	public void setTextOn(CharSequence textOn) {
		mTextOn = textOn;
		requestLayout();
	}

	public CharSequence getTextOff() {
		return mTextOff;
	}

	public void setTextOff(CharSequence textOff) {
		mTextOff = textOff;
		requestLayout();
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (mOnLayout == null) {
			mOnLayout = makeLayout(mTextOn);
		}
		if (mOffLayout == null) {
			mOffLayout = makeLayout(mTextOff);
		}
		mTrackDrawable.getPadding(mTempRect);
		final int maxTextWidth = Math.max(mOnLayout.getWidth(), mOffLayout.getWidth());
		final int switchWidth = Math.max(mSwitchMinWidth, maxTextWidth * 2 + mThumbTextPadding * 4 + mTempRect.left + mTempRect.right);
		final int switchHeight = mTrackDrawable.getIntrinsicHeight();
		mThumbWidth = maxTextWidth + mThumbTextPadding * 2;
		mSwitchWidth = switchWidth;
		mSwitchHeight = switchHeight;
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int measuredHeight = getMeasuredHeight();
		if (measuredHeight < switchHeight) {
			setMeasuredDimension(getMeasuredWidth(), switchHeight);
		}
	}

	@Override
	public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
		super.onPopulateAccessibilityEvent(event);
		Layout layout = isChecked() ? mOnLayout : mOffLayout;
		if (layout != null && !TextUtils.isEmpty(layout.getText())) {
			event.getText().add(layout.getText());
		}
	}

	private Layout makeLayout(CharSequence text) {
		final CharSequence transformed = (mSwitchTransformationMethod != null) ? mSwitchTransformationMethod.getTransformation(text, this) : text;
		return new StaticLayout(transformed, mTextPaint, (int) Math.ceil(Layout.getDesiredWidth(transformed, mTextPaint)), Layout.Alignment.ALIGN_NORMAL, 1.f, 0, true);
	}

	private boolean hitThumb(float x, float y) {
		mThumbDrawable.getPadding(mTempRect);
		final int thumbTop = mSwitchTop - mTouchSlop;
		final int thumbLeft = mSwitchLeft + (int) (mThumbPosition + 0.5f) - mTouchSlop;
		final int thumbRight = thumbLeft + mThumbWidth + mTempRect.left + mTempRect.right + mTouchSlop;
		final int thumbBottom = mSwitchBottom + mTouchSlop;
		return x > thumbLeft && x < thumbRight && y > thumbTop && y < thumbBottom;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		mVelocityTracker.addMovement(ev);
		final int action = ev.getActionMasked();
		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			final float x = ev.getX();
			final float y = ev.getY();
			if (isEnabled() && hitThumb(x, y)) {
				mTouchMode = TOUCH_MODE_DOWN;
				mTouchX = x;
				mTouchY = y;
			}
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			switch (mTouchMode) {
			case TOUCH_MODE_IDLE:
				// Didn't target the thumb, treat normally.
				break;
			case TOUCH_MODE_DOWN: {
				final float x = ev.getX();
				final float y = ev.getY();
				if (Math.abs(x - mTouchX) > mTouchSlop || Math.abs(y - mTouchY) > mTouchSlop) {
					mTouchMode = TOUCH_MODE_DRAGGING;
					getParent().requestDisallowInterceptTouchEvent(true);
					mTouchX = x;
					mTouchY = y;
					return true;
				}
				break;
			}
			case TOUCH_MODE_DRAGGING: {
				final float x = ev.getX();
				final float dx = x - mTouchX;
				float newPos = Math.max(0, Math.min(mThumbPosition + dx, getThumbScrollRange()));
				if (newPos != mThumbPosition) {
					mThumbPosition = newPos;
					mTouchX = x;
					invalidate();
				}
				return true;
			}
			}
			break;
		}
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL: {
			if (mTouchMode == TOUCH_MODE_DRAGGING) {
				stopDrag(ev);
				return true;
			}
			mTouchMode = TOUCH_MODE_IDLE;
			mVelocityTracker.clear();
			break;
		}
		}
		return super.onTouchEvent(ev);
	}

	private void cancelSuperTouch(MotionEvent ev) {
		MotionEvent cancel = MotionEvent.obtain(ev);
		cancel.setAction(MotionEvent.ACTION_CANCEL);
		super.onTouchEvent(cancel);
		cancel.recycle();
	}

	private void stopDrag(MotionEvent ev) {
		mTouchMode = TOUCH_MODE_IDLE;
		// Up and not canceled, also checks the switch has not been disabled during the drag
		boolean commitChange = ev.getAction() == MotionEvent.ACTION_UP && isEnabled();
		cancelSuperTouch(ev);
		if (commitChange) {
			boolean newState;
			mVelocityTracker.computeCurrentVelocity(1000);
			float xvel = mVelocityTracker.getXVelocity();
			if (Math.abs(xvel) > mMinFlingVelocity) {
				// newState = isLayoutRtl() ? (xvel < 0) : (xvel > 0);
				newState = xvel > 0;
			} else {
				newState = getTargetCheckedState();
			}
			animateThumbToCheckedState(newState);
		} else {
			animateThumbToCheckedState(isChecked());
		}
	}

	private void animateThumbToCheckedState(boolean newCheckedState) {
		float targetPos = newCheckedState ? 0 : getThumbScrollRange();
		mThumbPosition = targetPos;
		setChecked(newCheckedState);
	}

	private boolean getTargetCheckedState() {
		return mThumbPosition >= getThumbScrollRange() / 2;
	}

	private void setThumbPosition(boolean checked) {
		mThumbPosition = checked ? getThumbScrollRange() : 0;
	}

	@Override
	public void setChecked(boolean checked) {
		super.setChecked(checked);
		setThumbPosition(isChecked());
		invalidate();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		setThumbPosition(isChecked());
		int switchRight;
		int switchLeft;
		switchRight = getWidth() - getPaddingRight();
		switchLeft = switchRight - mSwitchWidth;
		int switchTop = 0;
		int switchBottom = 0;
		switch (getGravity() & Gravity.VERTICAL_GRAVITY_MASK) {
		default:
		case Gravity.TOP:
			switchTop = getPaddingTop();
			switchBottom = switchTop + mSwitchHeight;
			break;
		case Gravity.CENTER_VERTICAL:
			switchTop = (getPaddingTop() + getHeight() - getPaddingBottom()) / 2 - mSwitchHeight / 2;
			switchBottom = switchTop + mSwitchHeight;
			break;
		case Gravity.BOTTOM:
			switchBottom = getHeight() - getPaddingBottom();
			switchTop = switchBottom - mSwitchHeight;
			break;
		}
		mSwitchLeft = switchLeft;
		mSwitchTop = switchTop;
		mSwitchBottom = switchBottom;
		mSwitchRight = switchRight;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// Draw the switch
		int switchLeft = mSwitchLeft;
		int switchTop = mSwitchTop;
		int switchRight = mSwitchRight;
		int switchBottom = mSwitchBottom;
		mTrackDrawable.setBounds(switchLeft, switchTop, switchRight, switchBottom);
		mTrackDrawable.draw(canvas);
		canvas.save();
		mTrackDrawable.getPadding(mTempRect);
		int switchInnerLeft = switchLeft + mTempRect.left;
		int switchInnerTop = switchTop + mTempRect.top;
		int switchInnerRight = switchRight - mTempRect.right;
		int switchInnerBottom = switchBottom - mTempRect.bottom;
		canvas.clipRect(switchInnerLeft, switchTop, switchInnerRight, switchBottom);
		mThumbDrawable.getPadding(mTempRect);
		final int thumbPos = (int) (mThumbPosition + 0.5f);
		int thumbLeft = switchInnerLeft - mTempRect.left + thumbPos;
		int thumbRight = switchInnerLeft + thumbPos + mThumbWidth + mTempRect.right;
		mThumbDrawable.setBounds(thumbLeft, switchTop, thumbRight, switchBottom);
		mThumbDrawable.draw(canvas);
		// mTextColors should not be null, but just in case
		if (mTextColors != null) {
			mTextPaint.setColor(mTextColors.getColorForState(getDrawableState(), mTextColors.getDefaultColor()));
		}
		mTextPaint.drawableState = getDrawableState();
		Layout switchText = getTargetCheckedState() ? mOnLayout : mOffLayout;
		if (switchText != null) {
			canvas.translate((thumbLeft + thumbRight) / 2 - switchText.getWidth() / 2, (switchInnerTop + switchInnerBottom) / 2 - switchText.getHeight() / 2);
			switchText.draw(canvas);
		}
		canvas.restore();
	}

	@Override
	public int getCompoundPaddingRight() {
		int padding = super.getCompoundPaddingRight() + mSwitchWidth;
		if (!TextUtils.isEmpty(getText())) {
			padding += mSwitchPadding;
		}
		return padding;
	}

	private int getThumbScrollRange() {
		if (mTrackDrawable == null) {
			return 0;
		}
		mTrackDrawable.getPadding(mTempRect);
		return mSwitchWidth - mThumbWidth - mTempRect.left - mTempRect.right;
	}

	@Override
	protected int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
		if (isChecked()) {
			mergeDrawableStates(drawableState, CHECKED_STATE_SET);
		}
		return drawableState;
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		int[] myDrawableState = getDrawableState();
		// Set the state of the Drawable
		// Drawable may be null when checked state is set from XML, from super constructor
		if (mThumbDrawable != null) mThumbDrawable.setState(myDrawableState);
		if (mTrackDrawable != null) mTrackDrawable.setState(myDrawableState);
		invalidate();
	}

	@Override
	protected boolean verifyDrawable(Drawable who) {
		return super.verifyDrawable(who) || who == mThumbDrawable || who == mTrackDrawable;
	}

	// TODO check if even necessary?!
	@Override
	public void jumpDrawablesToCurrentState() {
		super.jumpDrawablesToCurrentState();
		mThumbDrawable.jumpToCurrentState();
		mTrackDrawable.jumpToCurrentState();
	}

	public class AllCapsTransformationMethod implements TransformationMethodCompat2 {
		private static final String TAG = "AllCapsTransformationMethod";

		private boolean mEnabled;
		private Locale mLocale;

		public AllCapsTransformationMethod(Context context) {
			mLocale = context.getResources().getConfiguration().locale;
		}

		@Override
		public CharSequence getTransformation(CharSequence source, View view) {
			if (mEnabled) {
				return source != null ? source.toString().toUpperCase(mLocale) : null;
			}
			Log.w(TAG, "Caller did not enable length changes; not transforming text");
			return source;
		}

		@Override
		public void onFocusChanged(View view, CharSequence sourceText, boolean focused, int direction, Rect previouslyFocusedRect) {}

		@Override
		public void setLengthChangesAllowed(boolean allowLengthChanges) {
			mEnabled = allowLengthChanges;
		}

	}

	public interface TransformationMethodCompat {
		public CharSequence getTransformation(CharSequence source, View view);

		public void onFocusChanged(View view, CharSequence sourceText, boolean focused, int direction, Rect previouslyFocusedRect);
	}

	public interface TransformationMethodCompat2 extends TransformationMethodCompat {
		public void setLengthChangesAllowed(boolean allowLengthChanges);
	}
}
package com.bedrock.gaodedrawerdemo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.customview.widget.ViewDragHelper;

public class BottomDrawerLayout extends FrameLayout {

    private static final String TAG = "qiu";

    private View contentView;

    private View dragView;

    private XhsEmoticonsKeyBoard bottomView;

    private ViewDragHelper mDragHelper;

    private Status mStatus = Status.Open;

    public static enum Status {
        Close, Open, Draging, Middle;
    }

    private Context mContext;


    private final static int Y_VELOCITY = 600;
    //在中间位置的比例
    private float middleTopPercent = 0.4f;

    public Status getStatus() {
        return mStatus;
    }

    private OnDragStatusChangeListener mListener;

    public void setOnDragStatusChangeListener(OnDragStatusChangeListener listener) {
        this.mListener = listener;
    }

    /**
     * 状态监听
     */
    public interface OnDragStatusChangeListener {
        void onClose();

        void onOpen();

        void onMiddle();

        void onDraging(int top);
    }


    public BottomDrawerLayout(Context context) {
        this(context, null);
    }

    public BottomDrawerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomDrawerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init(context);
        //View Configuration 获取当前view的一些配置信息
        //最大 fling 速度
        mMaxVelocity = ViewConfiguration.get(context).getMaximumFlingVelocity();

        //ViewDragHelper是针对 ViewGroup 中的拖拽和重新定位 views
        // 操作时提供了一系列非常有用的方法和状态追踪。基本上使用在自定义ViewGroup处理拖拽中！
        ///
        //ViewDragHelper create(ViewGroup forParent, Callback cb)；一个静态的创建方法，
        //参数1：出入的是相应的ViewGroup
        //参数2：是一个回掉（其实这个回掉你可以自己在外面实现，后面在细说）
        //shouldInterceptTouchEvent(MotionEvent ev) 处理事件分发的（怎么说这个方法呢？主要是将ViewGroup的事件分发，委托给ViewDragHelper进行处理）
        //参数1：MotionEvent ev 主要是ViewGroup的事件
        //processTouchEvent(MotionEvent event) 处理相应TouchEvent的方法，这里要注意一个问题，处理相应的TouchEvent的时候要将结果返回为true，消费本次事件！否则将无法使用ViewDragHelper处理相应的拖拽事件！

        this.mDragHelper = ViewDragHelper.create(this, mCallback);
    }


    private int mOldh = -1;
    private int mNowh = -1;
    protected boolean mIsSoftKeyboardPop = false;
    protected int mScreenHeight;

    private void init(final Context context) {

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                //获取显示的区域的大小
                ((Activity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
                if (mScreenHeight == 0) {
                    mScreenHeight = r.bottom;
                }
                mNowh = mScreenHeight - r.bottom;
                if (mOldh != -1 && mNowh != mOldh) {
                    if (mNowh > 0) {
                        mIsSoftKeyboardPop = true;
                        Log.d(TAG, "键盘弹起");
                        if (getStatus() == Status.Middle) {
                            open(false);
                        }
                    } else {
                        mIsSoftKeyboardPop = false;
                        Log.d(TAG, "键盘关闭");
                    }
                }
                mOldh = mNowh;
            }
        });
    }


    ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {
        //tryCaptureView(View child, int pointerId) 这是一个抽象类，必须去实现
        // ，也只有在这个方法返回true的时候下面的方法才会生效；
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == dragView;
        }


        //onViewDragStateChanged(int state) 当状态改变的时候回调，返回相应的状态（这里有三种状态）
        //
        //STATE_IDLE 闲置状态
        //STATE_DRAGGING 正在拖动
        //STATE_SETTLING 放置到某个位置

        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
        }


        //参数1：拖拽的View
        //参数2：距离顶部的距离
        //参数3：变化量
        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            //getTop + dy = top
            Log.d(TAG, "clampViewPositionVertical() called with: " + "getTop = [" + child.getTop() + "], top = [" + top + "], dy = [" + dy + "]");
            if (child == dragView) {
                return fixTop(top);
            }
            return top;

        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            return super.clampViewPositionHorizontal(child, left, dx);
        }

        //
        @Override
        public int getViewVerticalDragRange(View child) {
            if (child == dragView) {
                return getMeasuredHeight() - child.getMeasuredHeight();
            }
            return super.getViewVerticalDragRange(child);
        }

        //onViewPositionChanged(View changedView, int left, int top, int dx, int dy) 当你拖动的View位置发生改变的时候回调
        //
        //参数1：你当前拖动的这个View
        //参数2：距离左边的距离
        //参数3：距离右边的距离
        //参数4：x轴的变化量
        //参数5：y轴的变化量

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
//            Log.d(TAG, "onViewPositionChanged() " + "left = [" + left + "], top = [" + top + "], dx = [" + dx + "], dy = [" + dy + "]");
            if (changedView == dragView) {
                changeMarginByTop(top);
                dispatchDragViewEvent(top);
            }
            invalidate();
        }


        //onViewCaptured(View capturedChild, int activePointerId)捕获View的时候调用的方法
        //
        //参数1：捕获的View（也就是你拖动的这个View）
        //参数2：这个参数我也不知道什么意思API中写的一个什么指针，这里没有到也没有注意

        @Override
        public void onViewCaptured(@NonNull View capturedChild, int activePointerId) {
            super.onViewCaptured(capturedChild, activePointerId);
        }

        //当View停止拖拽的时候调用的方法，一般在这个方法中重置一些参数，比如回弹什么的。。。
        //
        //参数1：你拖拽的这个View
        //参数2：x轴的速率
        //参数3：y轴的速率

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            Log.d(TAG, "onViewReleased()" + " xvel = [" + xvel + "], yvel = [" + yvel + "]");
            if (releasedChild == dragView) {
                int top = releasedChild.getTop();
//                Log.d(TAG, "releasedChild.getTop()=" + top);
                Log.d(TAG, "mStatus=" + mStatus);
                //先判断速度
                if (mPreStatus == Status.Close && yvel < -Y_VELOCITY) {
                    //向上的手势
                    middle();
                } else if (mPreStatus == Status.Middle && yvel > Y_VELOCITY) {
                    close();
                } else if (mPreStatus == Status.Middle && yvel < -Y_VELOCITY) {
                    open();
                } else if (mPreStatus == Status.Open && yvel > Y_VELOCITY) {
                    middle();
                } else if (top >= middleTop && top <= closeTop) {
                    //在关闭 -- 中间的位置情况 位置
                    if (top > closeToMiddleHalf) {
                        close();
                    } else {
                        middle();
                    }
                } else if (top > openTop && top <= middleTop) {
                    //在 中间 -- 打开的位置
                    if (top > middleToOpenHalf) {
                        middle();
                    } else {
                        open();
                    }
                }
            }

        }
    };


    /**
     * 上一个非拖拽状态
     */
    private Status mPreStatus;

    /**
     * 事件分发处理
     *
     * @param top
     */
    private void dispatchDragViewEvent(int top) {
        if (mListener != null) {
            mListener.onDraging(top);
        }
        if (mStatus != Status.Draging) {
            mPreStatus = mStatus;
        }
        // 更新状态, 执行回调
        Status preStatus = mStatus;
        mStatus = updateStatus(top);

        if (mStatus == Status.Draging) {
            //当前为滑动,关闭软键盘
            if (mStatus != preStatus) {
                EmoticonsKeyboardUtils.closeSoftKeyboard(mContext);
                bottomView.hideFuncLayout();
            }
        }
        if (mStatus != preStatus) {
            // 状态发生变化
            if (mStatus == Status.Close) {
                // 当前变为关闭状态
                if (mListener != null) {
                    mListener.onClose();
                }
            } else if (mStatus == Status.Open) {
                if (mListener != null) {
                    mListener.onOpen();
                }
            } else if (mStatus == Status.Middle) {
                if (mListener != null) {
                    mListener.onMiddle();
                }
            }
        }

    }

    private Status updateStatus(int top) {
        if (top == closeTop) {
            return Status.Close;
        } else if (top == openTop) {
            return Status.Open;
        } else if (top == middleTop) {
            return Status.Middle;
        }
        return Status.Draging;
    }


    private int fixTop(int top) {
        if (top <= openTop) {
            return openTop;
        } else if (top >= closeTop) {
            return closeTop;
        } else {
            return top;
        }
    }


    public void setStatus(Status status) {
        this.mStatus = status;

    }

    /**
     * 初始化状态使用
     */
    private boolean mConfigurationChangedFlag = true;

    /**
     * 解决Android虚拟按键 重新计算的问题
     */


    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mConfigurationChangedFlag = true;


    }

    public void close() {
        close(true);
    }

    public void close(boolean isSmooth) {
        if (isSmooth) {
            animateHandler(closeTop);
        } else {
            changeMarginByTop(closeTop);
            mStatus = Status.Close;
        }
    }

    public void middle() {
        middle(true);
    }

    public void middle(boolean isSmooth) {
        if (isSmooth) {
            animateHandler(middleTop);
        } else {
            changeMarginByTop(middleTop);
            mStatus = Status.Middle;
        }
    }


    public void open() {
        open(true);
    }

    public void open(boolean isSmooth) {
        if (isSmooth) {
            animateHandler(openTop);
        } else {
            changeMarginByTop(openTop);
            mStatus = Status.Open;
        }

    }

    private void animateHandler(int top) {
        if (mDragHelper.smoothSlideViewTo(dragView, 0, top)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        // 2. 持续平滑动画 (高频率调用)
        if (mDragHelper.continueSettling(true)) {
            //  如果返回true, 动画还需要继续执行
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private int closeTop;
    private int openTop;
    private int middleTop;

    //关闭状态与中间状态一半的位置
    private int closeToMiddleHalf;
    //中间状态到打开状态的一半位置
    private int middleToOpenHalf;

    /**
     * 初始化的高度
     */
    private int mInitHeight;

    private boolean isAssignment = false;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //防止软键盘弹出重复计算
//        Log.d("qiu", "onMeasure Maxheight = " + mMaxParentHeight);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (height > 0 && !isAssignment) {
            mInitHeight = height;
            isAssignment = true;
        }
        if (mInitHeight != 0) {
            //加上虚拟按键的高度
            int barHeight = getVirtualBarHeigh(getContext());
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int expandSpec = MeasureSpec.makeMeasureSpec(mInitHeight + barHeight, heightMode);
            super.onMeasure(widthMeasureSpec, expandSpec);
            return;
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
//        Log.d(TAG, "bottom onSizeChanged()" + "w = [" + w + "], h = [" + h + "], oldw = [" + oldw + "], oldh = [" + oldh + "] , height = " + getMeasuredHeight());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (mConfigurationChangedFlag) {
            mConfigurationChangedFlag = false;

            closeTop = getMeasuredHeight() - dragView.getMeasuredHeight();
            openTop = 0;
            middleTop = (int) ((getMeasuredHeight() - dragView.getMeasuredHeight()) * middleTopPercent);
            closeToMiddleHalf = (closeTop - middleTop) / 2 + middleTop;
            middleToOpenHalf = (middleTop - openTop) / 2 + openTop;
            switch (mStatus) {
                case Close:
                    close(false);
                    break;
                case Open:
                    open(false);
                    break;
                case Middle:
                    middle(false);
                    break;
            }
        }
    }

    private void changeMarginByTop(int top) {

        LayoutParams lp = (LayoutParams) dragView.getLayoutParams();
        lp.topMargin = top;
        dragView.setLayoutParams(lp);

        LayoutParams lp2 = (LayoutParams) bottomView.getLayoutParams();
        lp2.topMargin = top + dragView.getMeasuredHeight();
        lp2.height = getMeasuredHeight() - lp.topMargin;
//        bottomView.setLayoutParams(lp2);
        bottomView.updateMaxParentHeight(lp2.height - dragView.getMeasuredHeight());
        bottomView.layout(0, lp2.topMargin, bottomView.getMeasuredWidth(), getMeasuredHeight());
    }


    protected void onFinishInflate() {
        super.onFinishInflate();
        contentView = getChildAt(0);
        dragView = getChildAt(1);
        bottomView = (XhsEmoticonsKeyBoard) getChildAt(2);
    }

    private float xDistance, yDistance, xLast, yLast;


    private VelocityTracker mVelocityTracker;
    private int mMaxVelocity;

    // 传递触摸事件
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // 传递给mDragHelper
//        return mDragHelper.shouldInterceptTouchEvent(ev);
        boolean interceptOr = mDragHelper.shouldInterceptTouchEvent(ev);
        Log.d("qiu", "interceptOr " + interceptOr);
//        acquireVelocityTracker(ev);
        final VelocityTracker verTracker = mVelocityTracker;
//        return interceptOr;
        //自己的代码
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xDistance = yDistance = 0f;
                xLast = ev.getX();
                yLast = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float curX = ev.getX();
                final float curY = ev.getY();

                xDistance += Math.abs(curX - xLast);
                yDistance += Math.abs(curY - yLast);
                xLast = curX;
                yLast = curY;
                //xDistance < yDistance ：表示向下滑动偏垂直向下,而不是偏左右；
                //这个时候就拦截事件，自己处理
                if (interceptOr && xDistance < yDistance * 0.7f) {
                    Log.d("qiu", "onInterceptTouchEvent 纵向-");

                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
//                releaseVelocityTracker();
                break;
            default:
                break;
        }
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("qiu", "onTouchEvent mDragHelper 开始");
        View captrueView = mDragHelper.getCapturedView();
        try {
            mDragHelper.processTouchEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("qiu", "onTouchEvent mDragHelper 结束");
        // 返回true, 持续接受事件
        return true;
    }

    private void releaseVelocityTracker() {
        if (null != mVelocityTracker) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void acquireVelocityTracker(final MotionEvent event) {
        if (null == mVelocityTracker) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    /**
     * 获取虚拟功能键高度
     */
    public static int getVirtualBarHeigh(Context context) {
        int vh = 0;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        try {
            @SuppressWarnings("rawtypes")
            Class c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            vh = dm.heightPixels - windowManager.getDefaultDisplay().getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vh;
    }

}

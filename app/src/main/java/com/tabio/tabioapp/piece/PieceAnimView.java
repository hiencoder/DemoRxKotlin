package com.tabio.tabioapp.piece;

import com.bartoszlipinski.viewpropertyobjectanimator.ViewPropertyObjectAnimator;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.ui.widget.LearningCurveTextView;
import com.tabio.tabioapp.util.FontUtils;
import com.tabio.tabioapp.util.ImageUtils;
import com.tabio.tabioapp.util.ImeUtils;
import com.tabio.tabioapp.util.LogUtils;
import com.tabio.tabioapp.util.animation.EaseInCubicInterpolator;
import com.tabio.tabioapp.util.animation.EaseInQuintInterpolator;
import com.tabio.tabioapp.util.animation.EaseInSineInterpolator;
import com.tabio.tabioapp.util.animation.EaseOutToIn;
import com.tabio.tabioapp.util.animation.PathAnimator;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class PieceAnimView extends FrameLayout {
    public static final String TAG = makeLogTag(PieceAnimView.class);

    public static int DEFAULT_RANK_MAX_PIECE_NUM = Me.EXCHANGE_PIECE;
    private static int EXCHANGE_POINT = Me.EXCHANGE_POINT;

    private static int[] DROP_ORDER = {5, 15, 7, 9, 1, 3, 11, 13, 8, 4, 14, 6, 2, 0, 10, 12};

    private static long DROP_SPEED = 600L;
    private static int PIECE_VERTICAL_INITIAL_OFFSET = 2;

    private final static boolean debug = false;
    private static int POINT_PER_RANK = 100;
    private static final float RANK_UP_PIECE_CULLING_RATIO = 0.6f;

    private Piece.Param pieceParam;

    private boolean initialized;

    private boolean isReady;

    private int pieceVerticalNum;

    private int maxRankPieceNum = DEFAULT_RANK_MAX_PIECE_NUM;

    private int currentPieceNum;

    private int newPieceNum;

    private List<Piece> pieces = new ArrayList<>();

    private Rectangle rectCover;

    private LearningCurveTextView pieceNumLabel;

    private LearningCurveTextView pieceLabel;

    public TextView expirationDateLabel;

    private List<Bar> shortBars = new ArrayList<>();

    private List<Bar> longBars = new ArrayList<>();

    private Circle.CircleFrameLayout largeCircle;

    private Circle middleCircle;

    private Circle smallCircle;

    private LearningCurveTextView upperOnCircleLabel;

    private LearningCurveTextView underOnCircleLabel;

    private LearningCurveTextView centerOnCircleLabel;

    public interface OnPieceAnimListener {

        void animViewInitialized(PieceAnimView view);

        void readyToAnim(PieceAnimView view);

        void finishedAnim(PieceAnimView view);

        void finishCoverUp(PieceAnimView view, int oldRank, int newRank, int point);

        void finishedCloseAnim(PieceAnimView view);
    }

    private OnPieceAnimListener animListener;

    public PieceAnimView(Context context) {
        super(context);
        init(null, 0);
    }

    public PieceAnimView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public PieceAnimView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    public void setPieceAnimListener(OnPieceAnimListener listener) {
        animListener = listener;
    }

    public boolean isReady() {
        return isReady;
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.PieceAnimView, defStyle, 0);
        a.recycle();

        getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        init();
                        getViewTreeObserver().removeOnPreDrawListener(this);
                        return true;
                    }
                });
    }

    private void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        final float width = getWidth();
        final float height = getHeight();
        pieceParam = Piece.createParam(width, height);
        pieceVerticalNum = (int) ((height - pieceParam.margin * 2f) /
                (pieceParam.targetHeight + pieceParam.marginVertical));
        LogUtils.LOGD("PieceAnimView", " pieceVerticalNum=" + pieceVerticalNum + " " + pieceParam);

        Bitmap pieceBmpBase = ImageUtils.getBitmapFromAssets(
                getContext(), "piece_white.png");
        if (pieceBmpBase == null) {
            throw new IllegalStateException("piece_white.png load fail");
        }
        Bitmap pieceBmp = Bitmap.createScaledBitmap(pieceBmpBase, (int) pieceParam.targetWidth,
                (int) pieceParam.targetHeight, true);
        pieceBmpBase.recycle();

        // init pieces
        final int pieceColor = Piece.pieceColor(1);
        for (int v = 0; v < pieceVerticalNum; v++) {
            for (int h : DROP_ORDER) {
                Piece piece = Piece.createPiece(this, pieceBmp, pieceParam);
                piece.move(h, v + pieceVerticalNum + PIECE_VERTICAL_INITIAL_OFFSET);
                piece.setColor(pieceColor);
                pieces.add(piece);
            }
        }

        // bars
        Bar.Param barParamShort = Bar.createParamShort(width, height);
        for (int i = 0; i < Bar.NUM; i++) {
            Bar bar = Bar.createBar(this, barParamShort);
            bar.setRotation(Bar.DEGREE_PITCH * i);
            shortBars.add(bar);
        }
        Bar.Param barParamLong = Bar.createParamLong(width, height);
        for (int i = 0; i < Bar.NUM; i++) {
            Bar bar = Bar.createBar(this, barParamLong);
            bar.setRotation(Bar.DEGREE_PITCH * i + Bar.DEGREE_PITCH / 2);
            longBars.add(bar);
        }

        // rect cover
        rectCover = Rectangle.createRectangle(this);
        rectCover.setColor(pieceColor);
        rectCover.setTranslationY(height);

        // labels
        pieceNumLabel = new LearningCurveTextView(getContext());
        addView(pieceNumLabel);
        pieceNumLabel.getLayoutParams().width = (int) width;
        pieceNumLabel.setText(String.valueOf(0));
        pieceNumLabel.setGravity(Gravity.CENTER);
        pieceNumLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 130);
        pieceNumLabel.setTextColor(ContextCompat.getColor(getContext(), R.color.greenDark100));
        final float pieceNumLabelHeight = FontUtils.getTextHeight(pieceNumLabel);
        pieceNumLabel.getLayoutParams().height = (int) pieceNumLabelHeight;
        pieceNumLabel.setPivotX(width * 0.5f);
        pieceNumLabel.setPivotY(pieceNumLabelHeight * 0.5f);
        pieceNumLabel.setTranslationY(height * 0.5f - pieceNumLabelHeight * 0.5f);

        pieceLabel = new LearningCurveTextView(getContext());
        addView(pieceLabel);
        pieceLabel.getLayoutParams().width = (int) width;
        pieceLabel.setText(R.string.text_main_title_piece);
        pieceLabel.setGravity(Gravity.CENTER);
        pieceLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 56);
        pieceLabel.setTextColor(ContextCompat.getColor(getContext(), R.color.greenDark100));
        final float pieceLabelHeight = FontUtils.getTextHeight(pieceLabel);
        pieceLabel.getLayoutParams().height = (int) pieceLabelHeight;
        pieceLabel.setPivotX(width * 0.5f);
        pieceLabel.setPivotY(pieceLabelHeight * 0.5f);
        pieceLabel.setTranslationY(pieceNumLabel.getTranslationY() + pieceNumLabelHeight * 0.7f);

        expirationDateLabel = new TextView(getContext());
        addView(expirationDateLabel);
        expirationDateLabel.getLayoutParams().width = (int) width;

//        SimpleDateFormat sdf = new SimpleDateFormat("ピース有効期限：yyyy年MM月dd日");
//        expirationDateLabel.setText(sdf.format(new Date()));
        expirationDateLabel.setGravity(Gravity.CENTER);
        expirationDateLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        expirationDateLabel
                .setTextColor(ContextCompat.getColor(getContext(), R.color.greenDark100));
        final float expirationDateLabelHeight = FontUtils.getTextHeight(expirationDateLabel);
        expirationDateLabel.getLayoutParams().height = (int) expirationDateLabelHeight;
        expirationDateLabel.setPivotX(width * 0.5f);
        expirationDateLabel.setPivotY(expirationDateLabelHeight * 0.5f);
        expirationDateLabel.setTranslationY((height - expirationDateLabelHeight * 1.1f)-30);

        // circles
        largeCircle = Circle.createLarge(this);
        largeCircle.setColor(pieceColor);
        largeCircle.setVisibility(View.GONE);
        middleCircle = Circle.createMiddle(this);
        middleCircle.setColor(pieceColor);
        middleCircle.setVisibility(View.GONE);
        smallCircle = Circle.createSmall(this);
        smallCircle.setColor(pieceColor);
        smallCircle.setVisibility(View.GONE);

        // labels on largeCircle
        upperOnCircleLabel = new LearningCurveTextView(getContext());
        largeCircle.addView(upperOnCircleLabel);
        upperOnCircleLabel.getLayoutParams().width = largeCircle.getLayoutParams().width;
        upperOnCircleLabel.setGravity(Gravity.CENTER);
        upperOnCircleLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 100);
        upperOnCircleLabel.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        final float upperOnCircleLabelHeight = FontUtils.getTextHeight(upperOnCircleLabel);
        upperOnCircleLabel.getLayoutParams().height = (int) upperOnCircleLabelHeight;
        upperOnCircleLabel.setTranslationY(
                largeCircle.getLayoutParams().height / 2 - upperOnCircleLabelHeight * 0.8f);

        underOnCircleLabel = new LearningCurveTextView(getContext());
        largeCircle.addView(underOnCircleLabel);
        underOnCircleLabel.getLayoutParams().width = largeCircle.getLayoutParams().width;
        underOnCircleLabel.setGravity(Gravity.CENTER);
        underOnCircleLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 68);
        underOnCircleLabel.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        final float underOnCircleLabelHeight = FontUtils.getTextHeight(underOnCircleLabel);
        underOnCircleLabel.getLayoutParams().height = (int) underOnCircleLabelHeight;
        underOnCircleLabel.setTranslationY(
                largeCircle.getLayoutParams().height / 2 + underOnCircleLabelHeight * 0.1f);

        centerOnCircleLabel = new LearningCurveTextView(getContext());
        largeCircle.addView(centerOnCircleLabel);
        centerOnCircleLabel.getLayoutParams().width = largeCircle.getLayoutParams().width;
        centerOnCircleLabel.setText("Thank you");
        centerOnCircleLabel.setGravity(Gravity.CENTER);
        centerOnCircleLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 68);
        centerOnCircleLabel.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        final float centerOnCircleLabelHeight = FontUtils.getTextHeight(centerOnCircleLabel);
        centerOnCircleLabel.getLayoutParams().height = (int) centerOnCircleLabelHeight;
        centerOnCircleLabel.setTranslationY(
                largeCircle.getLayoutParams().height / 2 - centerOnCircleLabelHeight * 0.5f);

        if (this.animListener != null) {
            this.animListener.animViewInitialized(this);
        }
        // debug
        if (debug) {
            createDebugUi();
        }
    }

    /**
     * ピースアニメーションの為の計算を行う
     */
    public void prepare(int rank, int currentPieceCount, int newPieceCount) {
        // color
        final int color = Piece.pieceColor(rank);
        for (Piece piece : pieces) {
            piece.setColor(color);
        }
        rectCover.setColor(color);
        for (int i = 0; i < Bar.NUM; i++) {
            Bar shortBar = shortBars.get(i);
            shortBar.setColor(color);
            Bar longBar = longBars.get(i);
            longBar.setColor(color);
        }
        largeCircle.setColor(color);
        largeCircle.setRotation(0);
        largeCircle.setScaleX(0.001f);
        largeCircle.setScaleY(0.001f);
        largeCircle.move(getWidth() / 2, getHeight() / 2);
        largeCircle.setVisibility(View.GONE);
        middleCircle.setColor(color);
        middleCircle.setVisibility(View.GONE);
        middleCircle.move(getWidth() / 2, getHeight());
        smallCircle.setColor(color);
        smallCircle.setVisibility(View.GONE);
        smallCircle.move(getWidth() / 2, getHeight());

        // calc dropped sprite
        currentPieceNum = currentPieceCount;
        newPieceNum = newPieceCount;
        int droppedSpriteNum = (int) (getDroppedSpriteRatio() * getMaxSpriteNum());

        // move to initial position
        for (int i = 0; i < pieces.size(); i++) {
            Piece piece = pieces.get(i);
            piece.setVisibility(View.VISIBLE);
            int vOffset = i < droppedSpriteNum ? 0 : pieceVerticalNum + PIECE_VERTICAL_INITIAL_OFFSET;
            int v = i / Piece.PIECE_HORIZONTAL_NUM + vOffset;
            int tmpH = i - ((i / Piece.PIECE_HORIZONTAL_NUM) * Piece.PIECE_HORIZONTAL_NUM);
            int h = DROP_ORDER[tmpH];
            piece.move(h, v);
        }
        rectCover.setTranslationY(getHeight());

        // labels
        pieceNumLabel.setText(String.valueOf(newPieceNum % maxRankPieceNum));
        setPieceExpiresDate();
        pieceNumLabel.setVisibility(View.VISIBLE);
        pieceLabel.setVisibility(View.VISIBLE);
//        SimpleDateFormat sdf = new SimpleDateFormat("ピース有効期限：yyyy年MM月dd日");
//        expirationDateLabel.setText(sdf.format(expireDate));
//        expirationDateLabel.setVisibility(View.VISIBLE);

        centerOnCircleLabel.setAlpha(0);

        isReady = true;
        if (animListener != null) {
            animListener.readyToAnim(this);
        }
    }

    /**
     * ピースアニメーションをスタートさせる
     */
    public void startAnim() {
        if (!isReady) {
            return; // not ready to anim
        }
        isReady = false;

        // calc dropped sprite
        int droppedSpriteNum = (int) (getDroppedSpriteRatio() * getMaxSpriteNum());

        // detect rank , rank up
        int nowRank = currentPieceNum / maxRankPieceNum + 1;
        int newRank = newPieceNum / maxRankPieceNum + 1;
        boolean rankUp = newRank > nowRank;
        // calc drop sprite
        int remPieceNum = newPieceNum % maxRankPieceNum;
        int targetDropNum = (int) Math.ceil((float) remPieceNum * getSpritePieceRatio());
        if (rankUp) {
            targetDropNum = pieces.size();
        } else if (targetDropNum == pieces.size()) {
            targetDropNum = pieces.size() - 1;
        }

        for (int idx = droppedSpriteNum; idx < targetDropNum; idx++) {
            Piece piece = pieces.get(idx);
            int v = piece.getV() - pieceVerticalNum - PIECE_VERTICAL_INITIAL_OFFSET;
            int h = idx % Piece.PIECE_HORIZONTAL_NUM;
            float moveY = pieceParam.viewHeight - pieceParam.margin - pieceParam.targetHeight
                    - (float) v * (pieceParam.targetHeight + pieceParam.marginVertical);
            long delay = v * DROP_SPEED
                    + (h - droppedSpriteNum) * (DROP_SPEED / Piece.PIECE_HORIZONTAL_NUM);
            ObjectAnimator drop = ViewPropertyObjectAnimator.animate(piece)
                    .setStartDelay(delay)
                    .setDuration(DROP_SPEED)
                    .translationY(moveY)
                    .setInterpolator(new EaseInSineInterpolator())
                    .get();
            drop.start();
            if (idx == targetDropNum - 1) {
                long after = v * DROP_SPEED + (h - droppedSpriteNum)
                        * (DROP_SPEED / Piece.PIECE_HORIZONTAL_NUM) + DROP_SPEED + 363;
                if (rankUp) {
                    rankUpAnimation(after, nowRank, newRank);
                } else {
                    // no rank up
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (animListener != null) {
                                animListener.finishedAnim(PieceAnimView.this);
                            }
                        }
                    }, after);
                }
            }
        }
    }

    /**
     * ランクアップアニメーションの為のアニメーション
     */
    public void closeAnim() {
    }

    private int getMaxSpriteNum() {
        return pieceVerticalNum * Piece.PIECE_HORIZONTAL_NUM;
    }

    public int getMaxRankPieceNum() {
        return maxRankPieceNum;
    }

    public void setMaxRankPieceNum(int maxRankPieceNum) {
        this.maxRankPieceNum = maxRankPieceNum;
    }

    public float getSpritePieceRatio() {
        if (getMaxSpriteNum() == 0 || maxRankPieceNum == 0) {
            return 0;
        }
        return (float) getMaxSpriteNum() / (float) maxRankPieceNum;
    }

    public float getDroppedSpriteRatio() {
        int remPieceNum = currentPieceNum % maxRankPieceNum;
        return ((float) remPieceNum * getSpritePieceRatio()) / (float) getMaxSpriteNum();
    }

    private void rankUpAnimation(long after, final int oldRank, final int newRank) {
        // label
        hideLabelAnimation(after);
        // cover up
        rectCover.setVisibility(View.VISIBLE);
        final long coverUpDuration = 900L;
        ObjectAnimator coverUp = ViewPropertyObjectAnimator.animate(rectCover)
                .setStartDelay(after)
                .setDuration(coverUpDuration)
                .translationY(0)
                .setInterpolator(new EaseInCubicInterpolator())
                .get();
        ObjectAnimator callback = ViewPropertyObjectAnimator.animate(rectCover)
                .setDuration(1)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        if (animListener != null) {
                            animListener.finishCoverUp(PieceAnimView.this, oldRank, newRank,
                                    (newRank - oldRank) * POINT_PER_RANK);
                        }
                    }
                })
                .get();
        final long callback2Delay = 100L;
        ObjectAnimator callback2 = ViewPropertyObjectAnimator.animate(rectCover)
                .setStartDelay(callback2Delay)
                .setDuration(1)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        piecePopGatherAnimation();
                    }
                })
                .get();

        final long coverHideDelay = 500L;
        ObjectAnimator coverHideAndCircleAnimStart = ViewPropertyObjectAnimator.animate(rectCover)
                .setStartDelay(coverHideDelay)
                .setDuration(1)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        circleAnimation(newRank - oldRank);
                        rectCover.setVisibility(View.GONE);
                    }
                })
                .get();
        AnimatorSet coverAnimSet = new AnimatorSet();
        coverAnimSet.play(coverUp);
        coverAnimSet.play(callback).after(coverUp);
        coverAnimSet.play(callback2).after(callback);
        coverAnimSet.play(coverHideAndCircleAnimStart).after(callback2);
        coverAnimSet.start();
    }

    private void setPieceExpiresDate() {
        String cond = getContext().getString(R.string.text_main_condition,
                maxRankPieceNum-(newPieceNum % maxRankPieceNum),
                EXCHANGE_POINT);
        expirationDateLabel.setText(cond);
    }

    private void hideLabelAnimation(long after) {
        final long duration = 400L;
        final long delay = after + 700L;
        ObjectAnimator pieceNumLabelChange = ViewPropertyObjectAnimator.animate(pieceNumLabel)
                .setStartDelay(delay)
                .setDuration(1)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        LOGD(TAG, ""+String.valueOf(newPieceNum % maxRankPieceNum));
                        pieceNumLabel.setText(String.valueOf(newPieceNum % maxRankPieceNum));
                        setPieceExpiresDate();
                    }
                })
                .get();
        ObjectAnimator pieceNumLabelHide = ViewPropertyObjectAnimator.animate(pieceNumLabel)
                .scales(0.1f)
                .setDuration(duration)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        pieceNumLabel.setVisibility(View.GONE);
                        pieceNumLabel.setScaleX(1);
                        pieceNumLabel.setScaleY(1);
                    }
                })
                .get();
        AnimatorSet pieceNumLabelAs = new AnimatorSet();
        pieceNumLabelAs.play(pieceNumLabelChange);
        pieceNumLabelAs.play(pieceNumLabelHide).after(pieceNumLabelChange);
        pieceNumLabelAs.start();

        ObjectAnimator pieceLabelHide = ViewPropertyObjectAnimator.animate(pieceLabel)
                .scales(0.1f)
                .setStartDelay(delay)
                .setDuration(duration)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        pieceLabel.setVisibility(View.GONE);
                        pieceLabel.setScaleX(1);
                        pieceLabel.setScaleY(1);
                    }
                })
                .get();
        pieceLabelHide.start();

//        ObjectAnimator expirationDateLabelHide = ViewPropertyObjectAnimator.animate(
//                expirationDateLabel)
//                .scales(0.1f)
//                .setStartDelay(delay)
//                .setDuration(duration)
//                .withEndAction(new Runnable() {
//                    @Override
//                    public void run() {
//                        expirationDateLabel.setVisibility(View.GONE);
//                        expirationDateLabel.setScaleX(1);
//                        expirationDateLabel.setScaleY(1);
//                    }
//                })
//                .get();
//        expirationDateLabelHide.start();
    }

    private void showLabelAnimation() {
        final long duration = 600L;
        pieceNumLabel.setScaleX(1);
        pieceNumLabel.setScaleY(1);
        pieceNumLabel.setAlpha(0);
        pieceNumLabel.setVisibility(View.VISIBLE);
        ViewPropertyObjectAnimator.animate(pieceNumLabel)
                .alpha(1)
                .setDuration(duration)
                .get()
                .start();
        pieceLabel.setScaleX(1);
        pieceLabel.setScaleY(1);
        pieceLabel.setAlpha(0);
        pieceLabel.setVisibility(View.VISIBLE);
        ViewPropertyObjectAnimator.animate(pieceLabel)
                .alpha(1)
                .setDuration(duration)
                .get()
                .start();
//        expirationDateLabel.setScaleX(1);
//        expirationDateLabel.setScaleY(1);
//        expirationDateLabel.setAlpha(0);
//        expirationDateLabel.setVisibility(View.VISIBLE);
//        ViewPropertyObjectAnimator.animate(expirationDateLabel)
//                .alpha(1)
//                .setDuration(duration)
//                .get()
//                .start();
    }

    private void piecePopGatherAnimation() {

        final long scaleDuration = 600L;
        final long popGatherDuration = 2400L;
        final float gatherRange = largeCircle.getCircle().getRadius() * 0.3f;

        final float cx = getWidth() / 2;
        final float cy = getHeight() / 2;
        final float popMinX = - (float)getWidth() * 0.1f;
        final float popMaxX = (float)getWidth() + (float)getWidth() * 0.1f;
        final float popRangeX = (float)getWidth() * 1.2f;
        final float popMinY = - (float)getHeight() * 0.1f;
        final float popMaxY = (float)getHeight() + (float)getHeight() * 0.1f;
        final float popRangeY = (float)getHeight() * 1.2f;
        for (int i = 0; i < pieces.size(); i++) {
            final Piece piece = pieces.get(i);
            // 全ピースをアニメーションさせるとカクつくので一部間引く
            if (Math.random() < RANK_UP_PIECE_CULLING_RATIO) {
                piece.setVisibility(View.GONE);
                continue;
            }
            final float pieceX = piece.getTranslationX();
            final float pieceY = piece.getTranslationY();

            piece.setScaleX(3);
            piece.setScaleY(3);
            piece.setRotation((float) (Math.random() * 360.0));

            // scale
            ObjectAnimator scale = ViewPropertyObjectAnimator.animate(piece)
                    .setDuration(scaleDuration)
                    .scales(1)
                    .get();
            // pop
            final float popX = (float) (Math.random() * popRangeX) - pieceParam.targetWidth * 0.5f;
            final float popY = (float) (Math.random() * popRangeY) - pieceParam.targetHeight * 0.5f;
            float popControlX = popX;
            float popControlY = popY;
            if (Math.random() > 0.5) {
                popControlX += (popX - pieceX) * Math.random();
                popControlX = Math.min(popMaxX, Math.max(popMinX, popControlX));
            } else {
                popControlY += (popY - pieceY) * Math.random();
                popControlY = Math.min(popMaxY, Math.max(popMinY, popControlY));
            }
            // gather
            double rad = Math.random() * Math.PI * 2.0;
            float gatherControlX = cx + (float) Math.cos(rad) * gatherRange
                    - gatherRange * (float) Math.random();
            float gatherControlY = cy + (float) Math.sin(rad) * gatherRange
                    - gatherRange * (float) Math.random();

            Path path = new Path();
            path.moveTo(pieceX, pieceY);
            path.quadTo(popControlX, popControlY, popX, popY);
            path.quadTo(gatherControlX, gatherControlY, cx, cy);
            PathAnimator popGatherAnimation = new PathAnimator(piece, path);
            long durationDiff = (long)(Math.random() * 100);
            popGatherAnimation.setDuration(popGatherDuration - durationDiff);
            popGatherAnimation.setInterpolator(new EaseOutToIn());

            // rotate
            float rotateDegree = 720f;
            piece.startRotate(rotateDegree);
            ObjectAnimator stopRotate = ViewPropertyObjectAnimator.animate(piece)
                    .setDuration(1)
                    .withStartAction(new Runnable() {
                        @Override
                        public void run() {
                            piece.stopRotate();
                            piece.setVisibility(View.GONE);
                        }
                    })
                    .get();

            AnimatorSet as = new AnimatorSet();
            as.play(scale);
            as.play(popGatherAnimation.getValueAnimator()).with(scale);
            as.play(stopRotate).after(popGatherAnimation.getValueAnimator());
            as.start();
        }
    }

    private void circleAnimation(final int upRank) {
        final float viewHeight = getHeight();
        final long bigDuration = 430L;
        final long rotateDuration = 644L;
        final long largeStartDelay = 1400L;
        final long rotateStartDelay = 5800L;
        largeCircle.setScaleX(0.0001f);
        ObjectAnimator big = ViewPropertyObjectAnimator.animate(largeCircle)
                .scales(1)
                .setInterpolator(new AccelerateInterpolator())
                .setStartDelay(largeStartDelay)
                .setDuration(bigDuration)
                .withStartAction(new Runnable() {
                    @Override
                    public void run() {
                        largeCircle.setVisibility(View.VISIBLE);
                    }
                })
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        barAnimation();
                    }
                })
                .get();
        ObjectAnimator dropRotate = ViewPropertyObjectAnimator.animate(largeCircle)
                .rotation(180)
                .translationY(getHeight())
                .setStartDelay(rotateStartDelay)
                .setDuration(rotateDuration)
                .setInterpolator(new EaseInQuintInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        largeCircle.setVisibility(View.GONE);
                    }
                })
                .get();
        AnimatorSet largeAs = new AnimatorSet();
        largeAs.play(big);
        largeAs.play(dropRotate).after(big);
        largeAs.start();

        final long middleDuration = 200L;
        final long middleDelay = 8150L;
        final float middleRiseY = viewHeight - viewHeight * 0.15f - middleCircle.getHeight();
        middleCircle.setTranslationY(viewHeight);
        ObjectAnimator middleRise = ViewPropertyObjectAnimator.animate(middleCircle)
                .translationY(middleRiseY)
                .setInterpolator(new DecelerateInterpolator())
                .setStartDelay(middleDelay)
                .setDuration(middleDuration)
                .withStartAction(new Runnable() {
                    @Override
                    public void run() {
                        middleCircle.setVisibility(View.VISIBLE);
                    }
                })
                .get();
        middleCircle.setTranslationY(middleRiseY);
        ObjectAnimator middleDown = ViewPropertyObjectAnimator.animate(middleCircle)
                .translationY(viewHeight)
                .setInterpolator(new AccelerateInterpolator())
                .setDuration(middleDuration)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        middleCircle.setVisibility(View.GONE);
                    }
                })
                .get();
        middleCircle.setTranslationY(viewHeight);
        AnimatorSet middleAs = new AnimatorSet();
        middleAs.play(middleRise);
        middleAs.play(middleDown).after(middleRise);
        middleAs.start();

        final long smallDuration = 150L;
        final long smallDelay = 8500L;
        final float smallRiseY = viewHeight - viewHeight * 0.05f - smallCircle.getHeight();
        smallCircle.setTranslationY(viewHeight);
        ObjectAnimator smallRise = ViewPropertyObjectAnimator.animate(smallCircle)
                .translationY(smallRiseY)
                .setInterpolator(new DecelerateInterpolator())
                .setStartDelay(smallDelay)
                .setDuration(smallDuration)
                .withStartAction(new Runnable() {
                    @Override
                    public void run() {
                        smallCircle.setVisibility(View.VISIBLE);
                    }
                })
                .get();
        smallCircle.setTranslationY(smallRiseY);
        ObjectAnimator smallDown = ViewPropertyObjectAnimator.animate(smallCircle)
                .translationY(viewHeight)
                .setInterpolator(new AccelerateInterpolator())
                .setDuration(smallDuration)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        if (animListener != null) {
                            animListener.finishedCloseAnim(PieceAnimView.this);
                        }
                        smallCircle.setVisibility(View.GONE);
                        showLabelAnimation();
                    }
                })
                .get();
        smallCircle.setTranslationY(viewHeight);
        AnimatorSet smallAs = new AnimatorSet();
        smallAs.play(smallRise);
        smallAs.play(smallDown).after(smallRise);
        smallAs.start();

        // label
        final long labelFadeInStartDelay = 1900L;
        final long labelFadeInDuration = 462L;
        final long labelChangeTextStartDelay = 2120L;
        final long labelHideDelay = 2000L;
        upperOnCircleLabel.setText(String.valueOf(upRank));
        upperOnCircleLabel.setAlpha(0);
        ObjectAnimator upperFadeIn = ViewPropertyObjectAnimator.animate(upperOnCircleLabel)
                .alpha(1)
                .setStartDelay(labelFadeInStartDelay)
                .setDuration(labelFadeInDuration)
                .get();
        final ObjectAnimator upperChangeText = ViewPropertyObjectAnimator
                .animate(upperOnCircleLabel)
                .setStartDelay(labelChangeTextStartDelay)
                .setDuration(1)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        upperOnCircleLabel.setText(String.valueOf(upRank * POINT_PER_RANK));
                    }
                })
                .get();
        ObjectAnimator upperHide = ViewPropertyObjectAnimator.animate(upperOnCircleLabel)
                .setStartDelay(labelHideDelay)
                .setDuration(1)
                .alpha(0)
                .get();
        AnimatorSet upperLabelAs = new AnimatorSet();
        upperLabelAs.play(upperFadeIn);
        upperLabelAs.play(upperChangeText).after(upperFadeIn);
        upperLabelAs.play(upperHide).after(upperChangeText);
        upperLabelAs.start();

        underOnCircleLabel.setText("Rank up!");
        underOnCircleLabel.setAlpha(0);
        ObjectAnimator underFadeIn = ViewPropertyObjectAnimator.animate(underOnCircleLabel)
                .alpha(1)
                .setStartDelay(labelFadeInStartDelay)
                .setDuration(labelFadeInDuration)
                .get();
        ObjectAnimator underChangeText = ViewPropertyObjectAnimator.animate(underOnCircleLabel)
                .setStartDelay(labelChangeTextStartDelay)
                .setDuration(1)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        underOnCircleLabel.setText("point get!");
                    }
                })
                .get();
        ObjectAnimator underHide = ViewPropertyObjectAnimator.animate(underOnCircleLabel)
                .setStartDelay(labelHideDelay)
                .setDuration(1)
                .alpha(0)
                .get();
        AnimatorSet underLabelAs = new AnimatorSet();
        underLabelAs.play(underFadeIn);
        underLabelAs.play(underChangeText).after(underFadeIn);
        underLabelAs.play(underHide).after(underChangeText);
        underLabelAs.start();

        final long centerShowDelay = labelFadeInStartDelay + labelFadeInDuration
                + labelChangeTextStartDelay + labelHideDelay;
        centerOnCircleLabel.setAlpha(0);
        ObjectAnimator centerShow = ViewPropertyObjectAnimator.animate(centerOnCircleLabel)
                .setStartDelay(centerShowDelay)
                .setDuration(1)
                .alpha(1)
                .get();
        centerShow.start();
    }

    private void barAnimation() {
        final long duration = longBars.get(0).getDuration();
        final long delayBase = 500;
        for (int i = 0; i < 4; i++) {
            long delay = delayBase * i + duration * i;
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    barAnimationOneShot();
                }
            }, delay);
        }
    }

    private void barAnimationOneShot() {
        for (int i = 0; i < Bar.NUM; i++) {
            Bar shortBar = shortBars.get(i);
            shortBar.startAnimation();
            Bar longBar = longBars.get(i);
            longBar.startAnimation();
        }
    }

    // region DEBUG UI

    private void createDebugUi() {
        final Context ctx = getContext();
        final Button showButton = new Button(ctx);
        showButton.setText("SHOW");
        addView(showButton);
        showButton.getLayoutParams().width = getWidth() / 4;
        showButton.getLayoutParams().height = getWidth() / 5;
        showButton.setTranslationX(10);
        showButton.setTranslationY(getHeight() - showButton.getLayoutParams().height - 10);
        showButton.setAlpha(0.5f);

        final LinearLayout layout = new LinearLayout(ctx);
        layout.setVisibility(View.GONE);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(0xffffffff);
        addView(layout);
        layout.getLayoutParams().width = getWidth() - 20;
        layout.getLayoutParams().height = getHeight() - 20;
        layout.setTranslationX(10);
        layout.setTranslationY(10);

        TextView tvMaxPiece = new TextView(ctx);
        tvMaxPiece.setText("MAX PIECE");
        layout.addView(tvMaxPiece);

        final EditText editMaxPiece = new EditText(ctx);
        editMaxPiece.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(editMaxPiece);

        TextView tvCurrentPiece = new TextView(ctx);
        tvCurrentPiece.setText("CURRENT PIECE");
        layout.addView(tvCurrentPiece);

        final EditText editCurrentPiece = new EditText(ctx);
        editCurrentPiece.setText(String.valueOf(0));
        editCurrentPiece.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(editCurrentPiece);

        TextView tvNewPiece = new TextView(ctx);
        tvNewPiece.setText("NEW PIECE");
        layout.addView(tvNewPiece);

        final EditText editNewPiece = new EditText(ctx);
        editNewPiece.setText(String.valueOf(0));
        editNewPiece.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(editNewPiece);

        final Button hideButton = new Button(ctx);
        hideButton.setText("HIDE");
        layout.addView(hideButton);
        hideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.setVisibility(View.GONE);
                showButton.setVisibility(View.VISIBLE);
                ImeUtils.hideScreenKeyboard(PieceAnimView.this);
            }
        });

        Button startButton = new Button(ctx);
        startButton.setText("START");
        layout.addView(startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.setVisibility(View.GONE);
                showButton.setVisibility(View.VISIBLE);
                ImeUtils.hideScreenKeyboard(PieceAnimView.this);

                int max = DEFAULT_RANK_MAX_PIECE_NUM;
                try {
                    max = Integer.valueOf(editMaxPiece.getText().toString());
                } catch (Exception e) {
                    // no-op
                }
                maxRankPieceNum = max;
                int cur = 0;
                try {
                    cur = Integer.valueOf(editCurrentPiece.getText().toString());
                } catch (Exception e) {
                    // no-op
                }
                int next = 0;
                try {
                    next = Integer.valueOf(editNewPiece.getText().toString());
                } catch (Exception e) {
                    // no-op
                }
                int rank = cur / maxRankPieceNum + 1;
                prepare(rank, cur, next);
                startAnim();
            }
        });

        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editMaxPiece.setText(String.valueOf(maxRankPieceNum));
                layout.setVisibility(View.VISIBLE);
                showButton.setVisibility(View.GONE);
            }
        });

    }
    // endregion DEBUG UI


    public LearningCurveTextView getPieceNumLabel() {
        return pieceNumLabel;
    }
}

package com.tabio.tabioapp.tutorial;

import com.bartoszlipinski.viewpropertyobjectanimator.ViewPropertyObjectAnimator;
import com.tabio.tabioapp.piece.Piece;
import com.tabio.tabioapp.util.ImageUtils;
import com.tabio.tabioapp.util.animation.AnimatorRepeatListener;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * TutorialBackgroundView.
 */
public class TutorialBackgroundView extends FrameLayout {

    private static int[] DROP_OFFSET = {13, 4, 12, 5, 9, 0, 11, 2, 8, 3, 14, 6, 15, 7, 10, 1};

    private static long DROP_SPEED = 2000L;

    private boolean initialized;

    private int pieceVerticalNum;

    private List<Piece> darkPieces1 = new ArrayList<>();

    private List<Piece> darkPieces2 = new ArrayList<>();

    public TutorialBackgroundView(Context context) {
        this(context, null);
    }

    public TutorialBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);

        getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        initView();
                        getViewTreeObserver().removeOnPreDrawListener(this);
                        return true;
                    }
                });
    }

    private void initView() {
        if (initialized) {
            return;
        }
        initialized = true;
        final float width = getWidth();
        final float height = getHeight();
        Piece.Param param = Piece.createParam(width, height);
        pieceVerticalNum = (int) ((height - param.margin * 2f) /
                (param.targetHeight + param.marginVertical));

        Bitmap pieceBmpBase = ImageUtils.getBitmapFromAssets(
                getContext(), "piece_white.png");
        if (pieceBmpBase == null) {
            throw new IllegalStateException("piece_white.png load fail");
        }
        Bitmap pieceBmp = Bitmap.createScaledBitmap(pieceBmpBase, (int) param.targetWidth,
                (int) param.targetHeight, true);
        pieceBmpBase.recycle();

        // fill pieces
        for (int v = 0; v < pieceVerticalNum; v++) {
            for (int h = 0; h < Piece.PIECE_HORIZONTAL_NUM; h++) {
                Piece piece = Piece.createPiece(this, pieceBmp, param);
                piece.setColor(0xffe9e7e4);
                piece.move(h, v);
            }
        }
        // dark pieces
        for (int h = 0; h < Piece.PIECE_HORIZONTAL_NUM; h++) {
            int p1v = DROP_OFFSET[h] + pieceVerticalNum - 1;
            Piece p1 = Piece.createPiece(this, pieceBmp, param);
            p1.setColor(0xffdcd8d6);
            p1.move(h, p1v);
            p1.setAlpha(0);
            darkPieces1.add(p1);

            int p2v = p1v - 1;
            Piece p2 = Piece.createPiece(this, pieceBmp, param);
            p2.setColor(0xffdcd8d6);
            p2.move(h, p2v);
            p2.setAlpha(0);
            darkPieces2.add(p2);
        }
        updateDarkPieceHidden();
        startDrop();
    }

    private void updateDarkPieceHidden() {
        for (int i = 0; i < Piece.PIECE_HORIZONTAL_NUM; i++) {
            Piece p1 = darkPieces1.get(i);
            p1.setVisibility(p1.getV() < pieceVerticalNum ? View.VISIBLE : View.INVISIBLE);
            Piece p2 = darkPieces2.get(i);
            p2.setVisibility(p2.getV() < pieceVerticalNum ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private void startDrop() {
        for (final Piece p : darkPieces1) {
            p.setAlpha(0);
            ObjectAnimator oa1 = ViewPropertyObjectAnimator.animate(p).alpha(1f)
                    .setDuration(DROP_SPEED / 4).get();
            p.setAlpha(1);
            ObjectAnimator oa2 = ViewPropertyObjectAnimator.animate(p)
                    .setDuration(DROP_SPEED / 4 - 1).get();
            ObjectAnimator oa3 = ViewPropertyObjectAnimator.animate(p).alpha(0f)
                    .setDuration(DROP_SPEED / 2).get();
            p.setAlpha(0);
            ObjectAnimator oa4 = ViewPropertyObjectAnimator.animate(p)
                    .withStartAction(new Runnable() {
                        @Override
                        public void run() {
                            int newV = p.getV() - 2;
                            if (newV < 0) {
                                newV += pieceVerticalNum;
                            }
                            p.moveV(newV);
                            p.setVisibility(p.getV() < pieceVerticalNum ? View.VISIBLE : View.INVISIBLE);
                        }
                    }).setDuration(1).get();

            AnimatorSet as = new AnimatorSet();
            as.play(oa1);
            as.play(oa2).after(oa1);
            as.play(oa3).after(oa2);
            as.play(oa4).after(oa3);
            as.addListener(new AnimatorRepeatListener());
            as.start();
        }
        for (final Piece p : darkPieces2) {
            p.setAlpha(0);
            ObjectAnimator oa1 = ViewPropertyObjectAnimator.animate(p).alpha(1f)
                    .setDuration(DROP_SPEED / 4).get();
            p.setAlpha(1);
            ObjectAnimator oa2 = ViewPropertyObjectAnimator.animate(p)
                    .setDuration(DROP_SPEED / 4 - 1).get();
            ObjectAnimator oa3 = ViewPropertyObjectAnimator.animate(p).alpha(0f)
                    .setDuration(DROP_SPEED / 2).get();
            p.setAlpha(0);
            ObjectAnimator oa4 = ViewPropertyObjectAnimator.animate(p)
                    .withStartAction(new Runnable() {
                        @Override
                        public void run() {
                            int newV = p.getV() - 2;
                            if (newV < 0) {
                                newV += pieceVerticalNum;
                            }
                            p.moveV(newV);
                            p.setVisibility(p.getV() < pieceVerticalNum ? View.VISIBLE : View.INVISIBLE);
                        }
                    }).setDuration(1).get();

            AnimatorSet as = new AnimatorSet();
            as.play(oa1);
            as.play(oa2).after(oa1);
            as.play(oa3).after(oa2);
            as.play(oa4).after(oa3);
            as.addListener(new AnimatorRepeatListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    animation.setStartDelay(0);
                    super.onAnimationEnd(animation);
                }
            });
            as.setStartDelay(DROP_SPEED / 2);
            as.start();
        }
    }
}

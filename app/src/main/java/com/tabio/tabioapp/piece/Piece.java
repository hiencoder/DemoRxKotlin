package com.tabio.tabioapp.piece;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;

/**
 * Piece.
 */
public class Piece extends View {

    public static final int PIECE_HORIZONTAL_NUM = 16;

    private static final float WIDTH_RATIO = 0.064f;

    private static final float HEIGHT_RATIO = 0.0752f;

    private static final float MARGIN_WIDTH_RATIO = 0.0133f;

    private static final float MARGIN_VERTICAL_RATIO = 0.0106f;

    private static final float MARGIN_HORIZONTAL_RATIO = 0.0106f;

    private static final float ODD_PIECE_NEGATIVE_OFFSET_RATIO = 0.016f;

    /**
     #f1e402,
     #7fe5f1,
     #f6b3fa,
     #a9ed3e,
     #ffacac,
     #bdb7f2,
     #ffba75,
     #86B5FF,
     #d5d5d5,
     #d3d087,
     */
    private static int[] PIECE_RANK_COLORS = {
            0xfff1e402,
            0xff7fe5f1,
            0xfff6b3fa,
            0xffa9ed3e,
            0xffffacac,
            0xffbdb7f2,
            0xffffba75,
            0xff86B5FF,
            0xffd5d5d5,
            0xffd3d087,
    };

    private Paint paint;

    private Bitmap bitmap;

    private int h = 0;

    private int v = 0;

    private Param param;

    private RotateUpdater rotateUpdater;

    public static int pieceColor(int rank) {
        if (rank <= 0) {
            return 0xffffffff;
        }
        return PIECE_RANK_COLORS[(rank - 1) % PIECE_RANK_COLORS.length];
//        return PIECE_RANK_COLORS[(rank) % PIECE_RANK_COLORS.length];
    }

    public static Param createParam(float viewWidth, float viewHeight) {
        Param param = new Param();
        param.viewWidth = viewWidth;
        param.viewHeight = viewHeight;
        param.targetWidth = viewWidth * WIDTH_RATIO;
        param.targetHeight = viewWidth * HEIGHT_RATIO;
        param.margin = viewWidth * MARGIN_WIDTH_RATIO;
        param.marginHorizontal = viewWidth * MARGIN_HORIZONTAL_RATIO;
        param.marginVertical = viewWidth * MARGIN_VERTICAL_RATIO;
        param.oddPieceNegativeOffset = viewWidth * ODD_PIECE_NEGATIVE_OFFSET_RATIO;
        return param;
    }

    public static class Param {

        public float viewWidth;

        public float viewHeight;

        public float targetWidth;

        public float targetHeight;

        public float margin;

        public float marginVertical;

        public float marginHorizontal;

        public float oddPieceNegativeOffset;

        @Override
        public String toString() {
            return "Param viewWidth=" + viewWidth + " viewHeight=" + viewHeight
                    + " targetWidth=" + targetWidth + " targetHeight=" + targetHeight
                    + " margin=" + margin + " marginVertical=" + marginVertical
                    + " marginHorizontal=" + marginHorizontal
                    + " oddPieceNegativeOffset=" + oddPieceNegativeOffset;
        }
    }

    Piece(Context context) {
        super(context);
        paint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap != null && !bitmap.isRecycled()) {
            canvas.drawBitmap(bitmap, 0, 0, paint);
        }
    }

    public void setColor(int color) {
        paint.setColorFilter(new LightingColorFilter(color, 1));
    }

    public static Piece createPiece(ViewGroup parent, Bitmap image, Param param) {
        Piece piece = new Piece(parent.getContext());
        parent.addView(piece);
        piece.getLayoutParams().width = (int) param.targetWidth;
        piece.getLayoutParams().height = (int) param.targetHeight;
        piece.bitmap = image;
        piece.param = param;

        return piece;
    }

    public int getH() {
        return h;
    }

    public int getV() {
        return v;
    }

    public void move(int h, int v) {
        this.h = h;
        setRotation((h % 2 == 0) ? 180 : 0);
        moveV(v);

        float x = param.margin + (float) h * param.targetWidth;
        float offsetCountH = h / 2;
        x += offsetCountH * param.marginHorizontal;
        x -= ((h + 1) / 2) * param.oddPieceNegativeOffset;
        setTranslationX(x);
    }

    public void moveV(int v) {
        this.v = v;
        setTranslationY(param.viewHeight - param.margin - param.targetHeight
                - (float) v * (param.targetHeight + param.marginVertical));
    }

    public void startRotate(float degreePerSec) {
        if (rotateUpdater == null) {
            rotateUpdater = new RotateUpdater();
        }
        rotateUpdater.setDegree(degreePerSec);
        rotateUpdater.start();
    }

    public void stopRotate() {
        rotateUpdater.setDegree(0);
        rotateUpdater.stop();
    }

    private class RotateUpdater implements Runnable {

        static final long FRAME = 33L;

        float degree = 0;

        boolean running;

        void setDegree(float degreePerSec) {
            this.degree = degreePerSec / (float) FRAME;
        }

        void start() {
            running = true;
            post(this);
        }

        void stop() {
            running = false;
        }

        @Override
        public void run() {
            if (running) {
                setRotation(getRotation() + degree);
                postDelayed(this, RotateUpdater.FRAME);
            }
        }
    }
}

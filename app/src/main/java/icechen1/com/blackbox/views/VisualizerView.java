package icechen1.com.blackbox.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import com.tyorikan.voicerecordingvisualizer.R.styleable;

public class VisualizerView extends FrameLayout {
    private static final int DEFAULT_NUM_COLUMNS = 20;
    private static final int RENDAR_RANGE_TOP = 0;
    private static final int RENDAR_RANGE_BOTTOM = 1;
    private static final int RENDAR_RANGE_TOP_BOTTOM = 2;
    private int mNumColumns;
    private int mRenderColor;
    private int mType;
    private int mRenderRange;
    private int mBaseY;
    private Canvas mCanvas;
    private Bitmap mCanvasBitmap;
    private Rect mRect = new Rect();
    private Paint mPaint = new Paint();
    private Paint mFadePaint = new Paint();
    private float mColumnWidth;
    private float mSpace;

    public VisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs);
        this.mPaint.setColor(this.mRenderColor);
        this.mFadePaint.setColor(Color.argb(138, 255, 255, 255));
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray args = context.obtainStyledAttributes(attrs, styleable.visualizerView);
        this.mNumColumns = args.getInteger(styleable.visualizerView_numColumns, 20);
        this.mRenderColor = args.getColor(styleable.visualizerView_renderColor, -1);
        this.mType = args.getInt(styleable.visualizerView_renderType, VisualizerView.Type.BAR.getFlag());
        this.mRenderRange = args.getInteger(styleable.visualizerView_renderRange, 0);
        args.recycle();
    }

    public void setBaseY(int baseY) {
        this.mBaseY = baseY;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.mRect.set(0, 0, this.getWidth(), this.getHeight());
        if(this.mCanvasBitmap == null) {
            this.mCanvasBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Config.ARGB_8888);
        }

        if(this.mCanvas == null) {
            this.mCanvas = new Canvas(this.mCanvasBitmap);
        }

        if(this.mNumColumns > this.getWidth()) {
            this.mNumColumns = 20;
        }

        this.mColumnWidth = (float)this.getWidth() / (float)this.mNumColumns;
        this.mSpace = this.mColumnWidth / 8.0F;
        if(this.mBaseY == 0) {
            this.mBaseY = this.getHeight() / 2;
        }

        canvas.drawBitmap(this.mCanvasBitmap, new Matrix(), (Paint)null);
    }

    public void receive(final int volume) {
        (new Handler(Looper.getMainLooper())).post(new Runnable() {
            public void run() {
                if(VisualizerView.this.mCanvas != null) {
                    if(volume == 0) {
                        VisualizerView.this.mCanvas.drawColor(0, Mode.CLEAR);
                    } else if((VisualizerView.this.mType & VisualizerView.Type.FADE.getFlag()) != 0) {
                        VisualizerView.this.mFadePaint.setXfermode(new PorterDuffXfermode(Mode.MULTIPLY));
                        VisualizerView.this.mCanvas.drawPaint(VisualizerView.this.mFadePaint);
                    } else {
                        VisualizerView.this.mCanvas.drawColor(0, Mode.CLEAR);
                    }

                    if((VisualizerView.this.mType & VisualizerView.Type.BAR.getFlag()) != 0) {
                        VisualizerView.this.drawBar(volume);
                    }

                    if((VisualizerView.this.mType & VisualizerView.Type.PIXEL.getFlag()) != 0) {
                        VisualizerView.this.drawPixel(volume);
                    }

                    VisualizerView.this.invalidate();
                }
            }
        });
    }

    private void drawBar(int volume) {
        for(int i = 0; i < this.mNumColumns; ++i) {
            float height = this.getRandomHeight(volume);
            float left = (float)i * this.mColumnWidth + this.mSpace;
            float right = (float)(i + 1) * this.mColumnWidth - this.mSpace;
            RectF rect = this.createRectF(left, right, height);
            this.mCanvas.drawRect(rect, this.mPaint);
        }

    }

    private void drawPixel(int volume) {
        for(int i = 0; i < this.mNumColumns; ++i) {
            float height = this.getRandomHeight(volume);
            float left = (float)i * this.mColumnWidth + this.mSpace;
            float right = (float)(i + 1) * this.mColumnWidth - this.mSpace;
            int drawCount = (int)(height / (right - left));
            if(drawCount == 0) {
                drawCount = 1;
            }

            float drawHeight = height / (float)drawCount;

            for(int j = 0; j < drawCount; ++j) {
                float top;
                float bottom;
                RectF rect;
                switch(this.mRenderRange) {
                    case 0:
                        bottom = (float)this.mBaseY - drawHeight * (float)j;
                        top = bottom - drawHeight + this.mSpace;
                        rect = new RectF(left, top, right, bottom);
                        break;
                    case 1:
                        top = (float)this.mBaseY + drawHeight * (float)j;
                        bottom = top + drawHeight - this.mSpace;
                        rect = new RectF(left, top, right, bottom);
                        break;
                    case 2:
                        bottom = (float)this.mBaseY - height / 2.0F + drawHeight * (float)j;
                        top = bottom - drawHeight + this.mSpace;
                        rect = new RectF(left, top, right, bottom);
                        break;
                    default:
                        return;
                }

                this.mCanvas.drawRect(rect, this.mPaint);
            }
        }

    }

    private float getRandomHeight(int volume) {
        double randomVolume = Math.random() * (double)volume + 1.0D;
        float height = (float)this.getHeight();
        switch(this.mRenderRange) {
            case 0:
                height = (float)this.mBaseY;
                break;
            case 1:
                height = (float)(this.getHeight() - this.mBaseY);
                break;
            case 2:
                height = (float)this.getHeight();
        }

        return height / 60.0F * (float)randomVolume;
    }

    private RectF createRectF(float left, float right, float height) {
        switch(this.mRenderRange) {
            case 0:
                return new RectF(left, (float)this.mBaseY - height, right, (float)this.mBaseY);
            case 1:
                return new RectF(left, (float)this.mBaseY, right, (float)this.mBaseY + height);
            case 2:
                return new RectF(left, (float)this.mBaseY - height, right, (float)this.mBaseY + height);
            default:
                return new RectF(left, (float)this.mBaseY - height, right, (float)this.mBaseY);
        }
    }

    public static enum Type {
        BAR(1),
        PIXEL(2),
        FADE(4);

        private int mFlag;

        private Type(int flag) {
            this.mFlag = flag;
        }

        public int getFlag() {
            return this.mFlag;
        }
    }
}
package edu.umd.hcil.impressionistpainter434;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.text.MessageFormat;
import java.util.Random;

/**
 * Created by jon on 3/20/2016.
 */
public class ImpressionistView extends View {
    private VelocityTracker mVelocityTracker = null;
    private ImageView _imageView;
    private Random r = new Random();

    private Canvas _offScreenCanvas = null;
    private Bitmap _offScreenBitmap = null;
    private Paint _paint = new Paint();

    private int _alpha = 150;
    private int _defaultRadius = 25;
    private Point _lastPoint = null;
    private long _lastPointTime = -1;
    private boolean _useMotionSpeedForBrushStrokeSize = true;
    private Paint _paintBorder = new Paint();
    private BrushType _brushType = BrushType.Square;
    private float _minBrushRadius = 5;

    public Bitmap getBitmap(){
        return _offScreenBitmap;
    }

    public ImpressionistView(Context context) {
        super(context);
        init(null, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    /**
     * Because we have more than one constructor (i.e., overloaded constructors), we use
     * a separate initialization method
     * @param attrs
     * @param defStyle
     */
    private void init(AttributeSet attrs, int defStyle){

        // Set setDrawingCacheEnabled to true to support generating a bitmap copy of the view (for saving)
        // See: http://developer.android.com/reference/android/view/View.html#setDrawingCacheEnabled(boolean)
        //      http://developer.android.com/reference/android/view/View.html#getDrawingCache()
        this.setDrawingCacheEnabled(true);

        _paint.setColor(Color.RED);
        _paint.setAlpha(_alpha);
        _paint.setAntiAlias(true);
        _paint.setStyle(Paint.Style.FILL);
        _paint.setStrokeWidth(4);

        _paintBorder.setColor(Color.BLACK);
        _paintBorder.setStrokeWidth(3);
        _paintBorder.setStyle(Paint.Style.STROKE);
        _paintBorder.setAlpha(50);

        //_paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh){

        Bitmap bitmap = getDrawingCache();
        Log.v("onSizeChanged", MessageFormat.format("bitmap={0}, w={1}, h={2}, oldw={3}, oldh={4}", bitmap, w, h, oldw, oldh));
        if(bitmap != null) {
            _offScreenBitmap = getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
            _offScreenCanvas = new Canvas(_offScreenBitmap);
        }
    }

    /**
     * Sets the ImageView, which hosts the image that we will paint in this view
     * @param imageView
     */
    public void setImageView(ImageView imageView){
        _imageView = imageView;
    }

    /**
     * Sets the brush type. Feel free to make your own and completely change my BrushType enum
     * @param brushType
     */
    public void setBrushType(BrushType brushType){
        _brushType = brushType;
    }

    /**
     * Clears the painting
     */
    public void clearPainting(){
        if(_offScreenCanvas != null) {
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
            _offScreenCanvas.drawRect(0, 0, this.getWidth(), this.getHeight(), paint);
        }
        invalidate();
    }

    /**
     * Saves the painting
     */
    public void savePainting(){
        //TODO

    }

    public void addSplat(BrushType code){
        Bitmap bitmap = _imageView.getDrawingCache();
        int rand_num = r.nextInt(20) + 5;
        int rand_size = r.nextInt(40) + 5;

        int rand_x;
        int rand_y;

        int xx = _offScreenCanvas.getWidth();
        int yy = _offScreenCanvas.getHeight();

        Log.i("TAG", "num= "+rand_num);
        for (int i = 1; i <= rand_num; i++){
            rand_x = r.nextInt(xx);
            rand_y = r.nextInt(yy);
            Log.i("TAG", "x= "+rand_x+", y= "+rand_y);

            _paint.setColor(bitmap.getPixel(rand_x, rand_y));
            if (code == BrushType.Circle)
                _offScreenCanvas.drawCircle(rand_x,rand_y,rand_size,_paint);
            else if (code == BrushType.Square){
                _paint.setStrokeWidth(rand_size);
                _offScreenCanvas.drawPoint(rand_x, rand_y, _paint);
            }
            else{
                if (rand_x%2 == 1)
                    _offScreenCanvas.drawCircle(rand_x,rand_y,rand_size,_paint);
                else {
                    _paint.setStrokeWidth(rand_size);
                    _offScreenCanvas.drawPoint(rand_x, rand_y, _paint);
                }
            }
            rand_size = r.nextInt(40) + 5;
        }

        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(_offScreenBitmap != null) {
            canvas.drawBitmap(_offScreenBitmap, 0, 0, _paint);
        }

        // Draw the border. Helpful to see the size of the bitmap in the ImageView
        canvas.drawRect(getBitmapPositionInsideImageView(_imageView), _paintBorder);

    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){
        int index = motionEvent.getActionIndex();
        int action = motionEvent.getActionMasked();
        int pointerId = motionEvent.getPointerId(index);

        //TODO
        //Basically, the way this works is to listen for Touch Down and Touch Move events and determine where those
        //touch locations correspond to the bitmap in the ImageView. You can then grab info about the bitmap--like the pixel color--
        //at that location

        float curTouchX = motionEvent.getX();
        float curTouchY = motionEvent.getY();
        int curTouchXRounded = (int) curTouchX;
        int curTouchYRounded = (int) curTouchY;
        float brushRadius = _defaultRadius;

        switch(motionEvent.getAction()){
            //http://developer.android.com/training/gestures/movement.html
            case MotionEvent.ACTION_DOWN:
                if(mVelocityTracker == null)
                    mVelocityTracker = VelocityTracker.obtain();
                else
                    mVelocityTracker.clear();

                mVelocityTracker.addMovement(motionEvent);
                break;
            case MotionEvent.ACTION_MOVE:
                int historySize = motionEvent.getHistorySize();

                Bitmap bitmap = _imageView.getDrawingCache();

                for (int i = 0; i < historySize; i++) {
                    float touchX = motionEvent.getHistoricalX(i);
                    float touchY = motionEvent.getHistoricalY(i);

                    _offScreenCanvas.drawPoint(touchX,touchY,_paint);
                }
                Log.i("TAG", "Reading");
                mVelocityTracker.addMovement(motionEvent);
                mVelocityTracker.computeCurrentVelocity(1000);
                Log.i("TAG", "X velocity: " + VelocityTrackerCompat.getXVelocity(mVelocityTracker, pointerId));
                Log.i("TAG", "Y velocity: " + VelocityTrackerCompat.getYVelocity(mVelocityTracker, pointerId));

                int speed_X = (int) Math.abs(VelocityTrackerCompat.getXVelocity(mVelocityTracker, pointerId));
                int speed_Y = (int) Math.abs(VelocityTrackerCompat.getYVelocity(mVelocityTracker, pointerId));
                int speed = (speed_X > speed_Y) ? speed_X:speed_Y;
                Log.i("TAG", "Speed " + speed + "   from   " + speed_X + ", " + speed_Y);
                brushRadius = speed/45;

                switch (_brushType) {

                    case Square:
                        _paint.setStrokeWidth(standardizeBrush(brushRadius));
                        _paint.setColor(bitmap.getPixel(curTouchXRounded, curTouchYRounded));
                        _offScreenCanvas.drawPoint(curTouchXRounded, curTouchYRounded, _paint);
                        break;
                    case Circle:
                        _paint.setStrokeWidth(brushRadius);
                        _paint.setColor(bitmap.getPixel(curTouchXRounded, curTouchYRounded));
                        _offScreenCanvas.drawCircle(
                                curTouchXRounded,
                                curTouchYRounded,
                                standardizeBrush(brushRadius),
                                _paint);
                        break;
                    case Line:
                        speed_X = (int) standardizeBrushLine(speed_X / 20);
                        speed_Y = (int) standardizeBrushLine(speed_Y / 20);
                        _paint.setStrokeWidth(2);
                        _paint.setColor(bitmap.getPixel(curTouchXRounded, curTouchYRounded));
                        _offScreenCanvas.drawLine(curTouchXRounded + speed_Y,
                                curTouchYRounded + speed_X,
                                curTouchXRounded - speed_Y,
                                curTouchYRounded - speed_X,
                                _paint);
                        break;
                    case Erase:
                        _paint.setColor(Color.WHITE);
                        _offScreenCanvas.drawCircle(curTouchXRounded, curTouchYRounded, 25, _paint);
                        break;

                }

                invalidate();

                break;
            case MotionEvent.ACTION_UP:
                break;
        }

        return true;
    }

    public float standardizeBrush(float speed){
        if(speed < 130)
            return speed;
        else if (speed >= 130 && speed < 500)
            return 130;
        else if (speed >= 500 && speed < 1000)
            return 180;
        else
            return 25;
    }
    public float standardizeBrushLine(float speed){
        if(speed < 200)
            return speed;
        else
            return 200;
    }


    /**
     * This method is useful to determine the bitmap position within the Image View. It's not needed for anything else
     * Modified from:
     *  - http://stackoverflow.com/a/15538856
     *  - http://stackoverflow.com/a/26930938
     * @param imageView
     * @return
     */
    private static Rect getBitmapPositionInsideImageView(ImageView imageView){
        Rect rect = new Rect();

        if (imageView == null || imageView.getDrawable() == null) {
            return rect;
        }

        // Get image dimensions
        // Get image matrix values and place them in an array
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        final int widthActual = Math.round(origW * scaleX);
        final int heightActual = Math.round(origH * scaleY);

        // Get image position
        // We assume that the image is centered into ImageView
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (int) (imgViewH - heightActual)/2;
        int left = (int) (imgViewW - widthActual)/2;

        rect.set(left, top, left + widthActual, top + heightActual);

        return rect;
    }
}


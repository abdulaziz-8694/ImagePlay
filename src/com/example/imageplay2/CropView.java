package com.example.imageplay2;

import java.io.ByteArrayOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class CropView extends ImageView {

    Paint paint = new Paint();
    private int initial_size = 100;
    private int affected=-1;
    private static Point leftTop, rightBottom, center, previous;

    private static final int DRAG= 0;
    private static final int LEFT= 1;
    private static final int TOP= 2;
    private static final int RIGHT= 3;
    private static final int BOTTOM= 4;

    // Adding parent class constructors   
    public CropView(Context context) {
        super(context);
        initCropView();
    }

    public CropView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        initCropView();
    }

    public CropView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initCropView();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        if(leftTop.x<0)
        	leftTop.x=0;
        if(leftTop.y<0)
        	leftTop.y=0;
        if(rightBottom.x-leftTop.x<0){
        	if(affected==RIGHT)
        	affected=LEFT;
        	else if(affected==LEFT)
        		affected=RIGHT;
        }
        if(rightBottom.y-leftTop.y<0)
        {

        	if(affected==TOP)
        	affected=BOTTOM;
        	else if(affected==BOTTOM)
        		affected=TOP;
        }        
        canvas.drawRect(leftTop.x, leftTop.y, rightBottom.x, rightBottom.y, paint);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try{
            Drawable drawable = getDrawable();
            if (drawable == null){
                setMeasuredDimension(0, 0);
            }
            else{
                int width = MeasureSpec.getSize(widthMeasureSpec);
                int height = width * drawable.getIntrinsicHeight() / drawable.getIntrinsicWidth();
                setMeasuredDimension(width, height);
            }
        }
        catch(Exception e){
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int eventaction = event.getAction();
        switch (eventaction) { 
            case MotionEvent.ACTION_DOWN:
            	if(isActionInsideRectangle(event.getX(), event.getY())) {
            		affected=getAffectedSide(event.getX(),event.getY());
                previous.set((int)event.getX(), (int)event.getY());
            	}
                break; 
            case MotionEvent.ACTION_MOVE: 
                    adjustRectangle((int)event.getX(), (int)event.getY(),affected);
                    invalidate(); // redraw rectangle
                    previous.set((int)event.getX(), (int)event.getY());
                break; 
            case MotionEvent.ACTION_UP:
            	affected=-1;
                previous = new Point();
                break;
        }         
        return true;
    }

    private void initCropView() {
        paint.setColor(Color.YELLOW);
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(5);  
        leftTop = new Point();
        rightBottom = new Point();
        center = new Point();
        previous = new Point();
        invalidate();
    }

    public void resetPoints() {
        center.set(getWidth()/2, getHeight()/2);
        leftTop.set((getWidth()-initial_size)/2,(getHeight()-initial_size)/2);
        rightBottom.set(leftTop.x+initial_size, leftTop.y+initial_size);
    }

    private static boolean isActionInsideRectangle(float x, float y) {
        int buffer = 10;
        return (x>=(leftTop.x-buffer)&&x<=(rightBottom.x+buffer)&& y>=(leftTop.y-buffer)&&y<=(rightBottom.y+buffer))?true:false;
    }

    private boolean isInImageRange(Point point) {
        return (point.x>=0&&point.x<=getWidth()&&point.y>=0&&point.y<=getHeight())?true:false;
    }

    private void adjustRectangle(int x, int y,int z) {
        int movement;
        switch(z) {
            case LEFT:
                movement = x-leftTop.x;
                if(isInImageRange(new Point(leftTop.x+movement,leftTop.y)))
                    leftTop.set(leftTop.x+movement,leftTop.y);
                break;
            case TOP:
                movement = y-leftTop.y;
                if(isInImageRange(new Point(leftTop.x,leftTop.y+movement)))
                    leftTop.set(leftTop.x,leftTop.y+movement);
                break;
            case RIGHT:
                movement = x-rightBottom.x;
                if(isInImageRange(new Point(rightBottom.x+movement,rightBottom.y)))
                    rightBottom.set(rightBottom.x+movement,rightBottom.y);
                break;
            case BOTTOM:
                movement = y-rightBottom.y;
                if(isInImageRange(new Point(rightBottom.x,rightBottom.y+movement)))
                    rightBottom.set(rightBottom.x,rightBottom.y+movement);
                break;      
            case DRAG:
                movement = x-previous.x;
                int movementY = y-previous.y;
                if(isInImageRange(new Point(leftTop.x+movement,leftTop.y+movementY)) && isInImageRange(new Point(rightBottom.x+movement,rightBottom.y+movementY))) {
                    leftTop.set(leftTop.x+movement,leftTop.y+movementY);
                    rightBottom.set(rightBottom.x+movement,rightBottom.y+movementY);
                }
                break;
            default:
                	break;
        }
    }

    private static int getAffectedSide(float x, float y) {
        int buffer = 10;
        if(x>=(leftTop.x-buffer)&&x<=(leftTop.x+buffer))
            return LEFT;
        else if(y>=(leftTop.y-buffer)&&y<=(leftTop.y+buffer))
            return TOP;
        else if(x>=(rightBottom.x-buffer)&&x<=(rightBottom.x+buffer))
            return RIGHT;
        else if(y>=(rightBottom.y-buffer)&&y<=(rightBottom.y+buffer))
            return BOTTOM;
        else
            return DRAG;
    }

    public byte[] getCroppedImage() {
        BitmapDrawable drawable = (BitmapDrawable)getDrawable();
        Bitmap scaled=drawable.getBitmap();
        scaled=Bitmap.createScaledBitmap(scaled, getWidth(), getHeight(), true);
        Bitmap cropped = Bitmap.createBitmap(scaled,leftTop.x,leftTop.y,(int)rightBottom.x-(int)leftTop.x,(int)rightBottom.y-(int)leftTop.y);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        cropped.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
}

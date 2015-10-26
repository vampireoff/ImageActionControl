package com.example.mypicturedemo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

@SuppressLint("FloatMath")
public class MainActivity extends Activity implements OnTouchListener{

	Matrix matrix = new Matrix();  
    Matrix savedMatrix = new Matrix();  
    DisplayMetrics dm;  
    ImageView imgView;  
    Bitmap bitmap;  
    float minScaleR;// 最小缩放比例  
    float MAX_SCALE = 3f;// 最大缩放比例  
    static final int NONE = 0;// 初始状态  
    static final int DRAG = 1;// 拖动  
    static final int ZOOM = 2;// 缩放  
    int mode = NONE;  
    PointF prev = new PointF();  
    PointF mid = new PointF();  
    float dist = 1f;  
    private Context mContext = MainActivity.this;
    float oldRotation = 0;  
    int widthScreen;
    int heightScreen;  
    boolean matrixCheck = false, isBig = false;
    Matrix matrix1 = new Matrix();
    private long lastClickTime = 0;
    private int primaryW, primaryH;
    private float scale, subX, subY;
  
    @SuppressLint("NewApi")
	@Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);  
        imgView = (ImageView) findViewById(R.id.imag);// 获取控件  
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img);// 获取图片资源  
        imgView.setImageBitmap(bitmap);// 填充控件  
        imgView.setOnTouchListener(this);// 设置触屏监听  
        dm = new DisplayMetrics();  
        getWindowManager().getDefaultDisplay().getMetrics(dm);// 获取分辨率  
        widthScreen = dm.widthPixels;
        heightScreen = dm.heightPixels;
        minZoom();  
        center();  
        imgView.setImageMatrix(matrix);  
//        imageInit();
//      View view = LayoutInflater.from(mContext).inflate(R.layout.dialog, null);
//		Dialog dialog = new Dialog(mContext, R.style.dialog);
//		dialog.setContentView(view);
//		dialog.show();
//		dialog.setCanceledOnTouchOutside(false);
    }  
  
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	// TODO Auto-generated method stub
    	menu.add(0, 1, 0, "生成图片");
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// TODO Auto-generated method stub
    	int id = item.getItemId();
    	if (id == 1) {
			CreatNewPhoto();
		}
    	return super.onOptionsItemSelected(item);
    }
    
    public void CreatNewPhoto() {  
        Bitmap mbitmap = Bitmap.createBitmap(widthScreen, heightScreen,  
                Config.ARGB_8888); // 背景图片  
        Canvas canvas = new Canvas(mbitmap); // 新建画布  
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, matrix, null); // 画图片  
        canvas.save(Canvas.ALL_SAVE_FLAG); // 保存画布  
        canvas.restore();  
        File file = new File(Environment.getExternalStorageDirectory(), "aaa.png");
        if (file.exists()) {
			file.delete();
		}
        
		try {
			FileOutputStream outputStream = new FileOutputStream(file);
			mbitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
			outputStream.flush();
			outputStream.close();
			Toast.makeText(mContext, "图片成功生成", 2000).show();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }  
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    	// TODO Auto-generated method stub
    	super.onWindowFocusChanged(hasFocus);
    	imageInit();
    	
    }
    
    public void imageInit() {  
        primaryW = bitmap.getWidth();  
        primaryH = bitmap.getHeight();  
        float scaleX = (float) widthScreen / primaryW;  
        float scaleY = (float) heightScreen / primaryH;  
        scale = scaleX < scaleY ? scaleX : scaleY;  
        if (scale < 1 && 1 / scale < MAX_SCALE) {  
            MAX_SCALE = (float) (1 / scale + 0.5);  
        }  
  
//        Matrix imgMatrix = new Matrix();  
        subX = (widthScreen - primaryW * scale) / 2;  
        subY = (heightScreen - primaryH * scale) / 2;  
//        matrix.postScale(scale, scale);  
//        matrix.postTranslate(subX, subY);  
        
    }  
    
    float limitY1, limitY2, limitX1, limitX2;
    private void changeSize(float x, float y) {  
        if (isBig) {  
            int screenHeight = dm.heightPixels;  
            savedMatrix.reset();  
            savedMatrix.postTranslate(0, screenHeight / 2);  
            savedMatrix.postScale(minScaleR, minScaleR);  
            matrix.set(savedMatrix);  
            imgView.invalidate();  
  
            isBig = false;  
        } else {  
            float transX = -((MAX_SCALE - 1) * x);  
            float transY = -((MAX_SCALE - 1) * (y - 0)); // (bigScale-1)(y-statusBarHeight-subY)+2*subY;  
            float currentWidth = primaryW * scale * MAX_SCALE; // 放大后图片大小  
            float currentHeight = primaryH * scale * MAX_SCALE;  
            // 如果图片放大后超出屏幕范围处理  
            if (currentHeight > heightScreen) {  
                limitY1 = -(currentHeight - heightScreen); // 平移限制  
                limitY2 = 0;  
                float currentSubY = MAX_SCALE * subY; // 当前平移距离  
                // 平移后，内容区域上部有空白处理办法  
                if (-transY < currentSubY) {  
                    transY = -currentSubY;  
                }  
                // 平移后，内容区域下部有空白处理办法  
                if (currentSubY + transY < limitY1) {  
                    transY = -(currentHeight + currentSubY - heightScreen);  
                }  
            } else {  
                // 如果图片放大后没有超出屏幕范围处理，则不允许拖动  
            }  
  
            if (currentWidth > widthScreen) {  
                limitX1 = -(currentWidth - widthScreen);  
                limitX2 = 0;  
                float currentSubX = MAX_SCALE * subX;  
                if (-transX < currentSubX) {  
                    transX = -currentSubX;  
                }  
                if (currentSubX + transX < limitX1) {  
                    transX = -(currentWidth + currentSubX - widthScreen);  
                }  
            } else {  
            }  
  
            matrix.postScale(MAX_SCALE, MAX_SCALE); // 在原有矩阵后乘放大倍数  
            matrix.postTranslate(transX, transY);  
            isBig = true; 
        }  
    }  
    
    /** * 触屏监听 */  
    public boolean onTouch(View v, MotionEvent event) {  
        switch (event.getAction() & MotionEvent.ACTION_MASK) { // 主点按下  
        case MotionEvent.ACTION_DOWN:  
            mode = NONE;  
            
            if (event.getEventTime() - lastClickTime < 300) {  
//            	imageInit();
                changeSize(event.getRawX(), event.getRawY());  
            }   else{  
            	savedMatrix.set(matrix);  
            	prev.set(event.getX(), event.getY());  
                mode = DRAG;  
            }  
            lastClickTime = event.getEventTime();  
            break; // 副点按下  
        case MotionEvent.ACTION_POINTER_DOWN:  
            dist = spacing(event); // 如果连续两点距离大于10，则判定为多点模式  
            oldRotation = rotation(event);
            if (spacing(event) > 10f) {  
                savedMatrix.set(matrix);  
                midPoint(mid, event);  
                mode = ZOOM;  
            }  
            break;  
        case MotionEvent.ACTION_UP:  
        case MotionEvent.ACTION_POINTER_UP:  
            mode = NONE;  
            break;  
        case MotionEvent.ACTION_MOVE:  
        	
            if (mode == DRAG) {  
                matrix1.set(savedMatrix);  
                matrix1.postTranslate(event.getX() - prev.x, event.getY()  
                        - prev.y);  
//                matrixCheck  = matrixCheck();
//                if (matrixCheck == false) {
					matrix.set(matrix1);
					imgView.invalidate();
//				}
            } else if (mode == ZOOM) {  
            	float rotation = rotation(event) - oldRotation;  
                float newDist = spacing(event);  
                if (newDist > 10f) {  
                    matrix1.set(savedMatrix);  
                    float tScale = newDist / dist;  
                    matrix1.postScale(tScale, tScale, mid.x, mid.y); 
                    matrix1.postRotate(rotation, mid.x, mid.y);// 旋轉  
//                    matrixCheck  = matrixCheck();
//                    if (matrixCheck == false) {
    					matrix.set(matrix1);
    					imgView.invalidate();
//    				}
                }  
            }  
            break;  
        }  
        imgView.setImageMatrix(matrix);  
        CheckView();  
        return true;  
    }  
  
 // 取旋转角度  
    private float rotation(MotionEvent event) {  
        double delta_x = (event.getX(0) - event.getX(1));  
        double delta_y = (event.getY(0) - event.getY(1));  
        double radians = Math.atan2(delta_y, delta_x);  
        return (float) Math.toDegrees(radians);  
    }  
    
    private boolean matrixCheck() {  
        float[] f = new float[9];  
        matrix1.getValues(f);  
        // 图片4个顶点的坐标  
        float x1 = f[0] * 0 + f[1] * 0 + f[2];  
        float y1 = f[3] * 0 + f[4] * 0 + f[5];  
        float x2 = f[0] * imgView.getWidth() + f[1] * 0 + f[2];  
        float y2 = f[3] * imgView.getWidth() + f[4] * 0 + f[5];  
        float x3 = f[0] * 0 + f[1] * imgView.getHeight() + f[2];  
        float y3 = f[3] * 0 + f[4] * imgView.getHeight() + f[5];  
        float x4 = f[0] * imgView.getWidth() + f[1] * imgView.getHeight() + f[2];  
        float y4 = f[3] * imgView.getWidth() + f[4] * imgView.getHeight() + f[5];  
        // 图片现宽度  
        double width = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));  
        // 缩放比率判断  
        if (width < widthScreen / 3 || width > widthScreen * 3) {  
            return true;  
        }  
        // 出界判断  
        if ((x1 < widthScreen / 3 && x2 < widthScreen / 3  
                && x3 < widthScreen / 3 && x4 < widthScreen / 3)  
                || (x1 > widthScreen * 2 / 3 && x2 > widthScreen * 2 / 3  
                        && x3 > widthScreen * 2 / 3 && x4 > widthScreen * 2 / 3)  
                || (y1 < heightScreen / 3 && y2 < heightScreen / 3  
                        && y3 < heightScreen / 3 && y4 < heightScreen / 3)  
                || (y1 > heightScreen * 2 / 3 && y2 > heightScreen * 2 / 3  
                        && y3 > heightScreen * 2 / 3 && y4 > heightScreen * 2 / 3)) {  
            return true;  
        }  
        return false;  
    }  
    
    /** * 限制最大最小缩放比例，自动居中 */  
    private void CheckView() {  
        float p[] = new float[9];  
        matrix.getValues(p);  
        if (mode == ZOOM) {  
            if (p[0] < minScaleR) {  
                matrix.setScale(minScaleR, minScaleR);  
            }  
            if (p[0] > MAX_SCALE) {  
            	isBig = true;
                matrix.set(savedMatrix);  
            }  
        }  
        center();  
    }  
  
    /** * 最小缩放比例，最大为100% */  
    private void minZoom() {  
        minScaleR = Math.min(  
                (float) dm.widthPixels / (float) bitmap.getWidth(),  
                (float) dm.heightPixels / (float) bitmap.getHeight());  
        if (minScaleR < 1.0) {  
            matrix.postScale(minScaleR, minScaleR);  
        }  
    }  
  
    private void center() {  
        center(true, true);  
    }  
  
    /** * 横向、纵向居中 */  
    protected void center(boolean horizontal, boolean vertical) {  
        Matrix m = new Matrix();  
        m.set(matrix);  
        RectF rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());  
        m.mapRect(rect);  
        float height = rect.height();  
        float width = rect.width();  
        float deltaX = 0, deltaY = 0;  
        if (vertical) {  
            // 图片小于屏幕大小，则居中显示。大于屏幕，上方留空则往上移，下放留空则往下移  
            int screenHeight = dm.heightPixels;  
            if (height < screenHeight) {  
                deltaY = (screenHeight - height) / 2 - rect.top;  
            } else if (rect.top > 0) {  
                deltaY = -rect.top;  
            } else if (rect.bottom < screenHeight) {  
                deltaY = imgView.getHeight() - rect.bottom;  
            }  
        }  
        if (horizontal) {  
            int screenWidth = dm.widthPixels;  
            if (width < screenWidth) {  
                deltaX = (screenWidth - width) / 2 - rect.left;  
            } else if (rect.left > 0) {  
                deltaX = -rect.left;  
            } else if (rect.right < screenWidth) {  
                deltaX = screenWidth - rect.right;  
            }  
        }  
        matrix.postTranslate(deltaX, deltaY);  
    }  
  
    /** * 两点的距离 */  
    private float spacing(MotionEvent event) {  
        float x = event.getX(0) - event.getX(1);  
        float y = event.getY(0) - event.getY(1);  
        return FloatMath.sqrt(x * x + y * y);  
    }  
  
    /** * 两点的中点 */  
	private void midPoint(PointF point, MotionEvent event) {  
        float x = event.getX(0) + event.getX(1);  
        float y = event.getY(0) + event.getY(1);  
        point.set(x / 2, y / 2);  
    }  

}

package hyc.desktopbrowser;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.*;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;

public class WebWindow extends LinearLayout {
    public WebWindow(Context context) {
        super(context);
        init();
    }

//    public MyWindow(Context context,AttributeSet attrs) {
//        super(context, attrs);
//        init();
//    }
//
//    public MyWindow(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        init();
//    }
//
//    public MyWindow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//        init();
//    }

//    public MyWindow(Context context, data)

    private int sx;
    private int sy;

    private View taskbarItem;
    public View getTaskbarItem() {
        return taskbarItem;
    }

    public String getWebWindowId() {
        return webWindowId;
    }

    private String webWindowId;
    HashMap<String, String> headers;

    private void init(){
        // 生成窗口id
        webWindowId = System.currentTimeMillis()+"";

        LayoutInflater.from(getContext()).inflate(R.layout.window_view, this);

        taskbarItem = LayoutInflater.from(getContext()).inflate(R.layout.taskbar_item, null);
        taskbarItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("taskBarItem", "onClick");

//                mined = !mined;
                if (mined) {
                    // 还原最小化的窗口
                    setVisibility(VISIBLE);
                    bringToFront();
                    mined = false;
                    if (onFocusListener != null) onFocusListener.onFocus(WebWindow.this);
                }else {
                    // 判断窗口是否在最前端
                    if (((MainActivity)getContext()).getFocusWindowId().equals(webWindowId)){
                        // 最小化
                        setVisibility(GONE);
                        mined = true;
                        if (onWindowBarItemClickListener != null) onWindowBarItemClickListener.onMin(WebWindow.this);
                    }else {
                        // 把窗口前置
                        bringToFront();
                        if (onFocusListener != null) onFocusListener.onFocus(WebWindow.this);
                    }
                }

            }
        });

        // 窗口的拖拽手势
        initTouchEvent();

        webView = (WebView) findViewById(R.id.window_webview);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 加载网页时更新地址栏
                editText.setText(url);
            }

        });
        // js
        webView.getSettings().setJavaScriptEnabled(true);

        editText = (EditText) findViewById(R.id.window_bar_url);
        editText.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // 回车键
                if (KeyEvent.KEYCODE_ENTER == keyCode && KeyEvent.ACTION_DOWN == event.getAction()) {
                    // TODO BUG 键盘收不了
                    editText.clearFocus();
                    webView.loadUrl(editText.getText().toString(), headers);
                    return true;
                }
                return false;
            }
        });

        findViewById(R.id.window_bar_refresh).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl(editText.getText().toString(), headers);
            }
        });

        findViewById(R.id.window_bar_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.goBack();
            }
        });

        findViewById(R.id.window_bar_forward).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.goForward();
            }
        });

        findViewById(R.id.window_bar_min).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mined) {
                    if (onWindowBarItemClickListener != null) onWindowBarItemClickListener.onMin(WebWindow.this);
                    WebWindow.this.setVisibility(GONE);
                    mined = true;
                }
            }
        });

        findViewById(R.id.window_bar_max).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) WebWindow.this.getLayoutParams();
                if (maxed){
                    // 还原位置和大小
                    lParams.width = unMaxWidth;
                    lParams.height = unMaxHeight;
                    lParams.leftMargin = unMaxLeft;
                    lParams.topMargin = unMaxTop;
                    lParams.rightMargin = unMaxRight;
                    lParams.bottomMargin = unMaxBottom;
                    WebWindow.this.setLayoutParams(lParams);
                    WebWindow.this.layout(0, 0, WebWindow.this.getWidth(), WebWindow.this.getHeight());

                    maxed = false;
                }else {
                    RelativeLayout parent = ((RelativeLayout)WebWindow.this.getParent());

                    // 保存现在的位置和大小
                    unMaxLeft = lParams.leftMargin;
                    unMaxTop = lParams.topMargin;
                    unMaxRight = lParams.rightMargin;
                    unMaxBottom = lParams.bottomMargin;
                    unMaxWidth = lParams.width;
                    unMaxHeight = lParams.height;

                    lParams.leftMargin = 0;
                    lParams.topMargin = 0;
                    lParams.width = parent.getWidth();
                    lParams.height = parent.getHeight()-84; // 减去任务栏高度

                    WebWindow.this.setLayoutParams(lParams);

                    maxed = true;
                }

            }
        });

        findViewById(R.id.window_bar_close).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onWindowBarItemClickListener != null) onWindowBarItemClickListener.onClose(WebWindow.this);
                ((ViewGroup)WebWindow.this.getParent()).removeView(WebWindow.this);
                ((ViewGroup)taskbarItem.getParent()).removeView(taskbarItem);
            }
        });

        headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.2) AppleWebKit/525.13 (KHTML, like Gecko) Chrome/0.2.149.27 Safari/525.13");
        webView.loadUrl(editText.getText().toString(), headers);

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            WebWindow.this.bringToFront();
            if (onFocusListener != null) onFocusListener.onFocus(WebWindow.this);
//            View view = getCurrentFocus();
            if (editText != null) {
                Rect r = new Rect();
                editText.getGlobalVisibleRect(r);
                int rawX = (int) ev.getRawX();
                int rawY = (int) ev.getRawY();
                // 判断点击的点是否落在当前焦点所在的 view 上；
                if (!r.contains(rawX, rawY)) {
                    editText.clearFocus();
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void initTouchEvent(){
//        setOnTouchListener(new OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
////                Log.d("WindowOnTouch", "window");
//                WebWindow.this.bringToFront();
//                return false;
//            }
//        });

        TextView titleView = (TextView) findViewById(R.id.window_bar_title);

        titleView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
//                Log.d("WindowOnTouch", "titleView");
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:// 获取手指第一次接触屏幕
                        sx = (int) event.getRawX();
                        sy = (int) event.getRawY();
//                        iv_dv_view.setImageResource(R.drawable.t);
                        break;
                    case MotionEvent.ACTION_MOVE:// 手指在屏幕上移动对应的事件
                        int x = (int) event.getRawX();
                        int y = (int) event.getRawY();
                        // 获取手指移动的距离
                        int dx = x - sx;
                        int dy = y - sy;
                        // 得到imageView最开始的各顶点的坐标
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) WebWindow.this.getLayoutParams();
                        layoutParams.leftMargin += dx;
                        layoutParams.topMargin += dy;
                        layoutParams.rightMargin -= dx;
                        layoutParams.bottomMargin -= dy;
                        int l = WebWindow.this.getLeft();
                        int r = WebWindow.this.getRight();
                        int t = WebWindow.this.getTop();
                        int b = WebWindow.this.getBottom();
                        int newL = l + dx;
                        int newT = t + dy;
                        int newR = r + dx;
                        int newB = b + dy;

                        // 限制窗口移动范围
//                        WindowManager wm = (WindowManager) getContext()
//                                .getSystemService(Context.WINDOW_SERVICE);
//                        DisplayMetrics outMetrics = new DisplayMetrics();
//                        wm.getDefaultDisplay().getMetrics(outMetrics);
//
//                        int sWidth = dip2px(getContext(), outMetrics.widthPixels);
//                        int sHeight = dip2px(getContext(), outMetrics.heightPixels);
//
//                        if (newL < 0) newL = 0;
//                        int maxL = sWidth - 60;
//                        if (newL > maxL) newL = maxL;
//
//                        if (newT < 0) newT = 0;
//                        int maxT = sHeight - 60;
//                        if (newT > maxT) newT = maxT;
//
//                        if (newR < WebWindow.this.getWidth()) newT = WebWindow.this.getWidth();
//                        int maxR = sWidth - 60 + WebWindow.this.getWidth();
//                        if (newR > maxR) newR = maxR;
//
//                        if (newB < WebWindow.this.getHeight()) newB = WebWindow.this.getHeight();
//                        int maxB = sHeight - 60 + WebWindow.this.getHeight();
//                        if (newB > maxB) newB = maxB;

//                        // 更改imageView在窗体的位置
                        WebWindow.this.layout(newL, newT, newR, newB);
                        // 获取移动后的位置
                        sx = (int) event.getRawX();
                        sy = (int) event.getRawY();
                        break;
//                    case MotionEvent.ACTION_UP:// 手指离开屏幕对应事件
//                        // 记录最后图片在窗体的位置
//                        int lasty = MyWindow.this.getTop();
//                        int lastx = MyWindow.this.getLeft();
//                        iv_dv_view.setImageResource(R.drawable.next);
//                        SharedPreferences.Editor editor = sp.edit();
//                        editor.putInt("lasty", lasty);
//                        editor.putInt("lastx", lastx);
//                        editor.commit();
//                        break;
                }
                return true;
            }
        });

        View resizeHandler = findViewById(R.id.window_resize_handler);


//        final TextView textView = (TextView) findViewById(R.id.window_status_bar);
        resizeHandler.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
//                Log.d("WindowOnTouch", "resizeHandler");
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:// 获取手指第一次接触屏幕
                        sx = (int) event.getRawX();
                        sy = (int) event.getRawY();
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) WebWindow.this.getLayoutParams();
//                        aWidth = layoutParams.width;
//                        aHeight = layoutParams.height;
//                        iv_dv_view.setImageResource(R.drawable.t);
//                        textView.setText(sx+","+sy);
//                        Log.d("getWidthTouch", layoutParams.width+"");
                        break;
                    case MotionEvent.ACTION_MOVE:// 手指在屏幕上移动对应的事件
                        int x = (int) event.getRawX();
                        int y = (int) event.getRawY();
//                        textView.setText(x+","+y);
                        // 获取手指移动的距离
                        int dx = x - sx;
                        int dy = y - sy;

                        // 缩放
                        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) WebWindow.this.getLayoutParams();
                        lParams.width += dx;
                        lParams.height += dy;

                        // 限制窗口最小的大小
                        if (lParams.width < 800) lParams.width = 800;
                        if (lParams.height < 600) lParams.height = 600;

                        WebWindow.this.setLayoutParams(lParams);

//                        int l = WebWindow.this.getLeft();
//                        int r = WebWindow.this.getRight();
//                        int t = WebWindow.this.getTop();
//                        int b = WebWindow.this.getBottom();
                        // 更改imageView在窗体的位置
//                        WebWindow.this.layout(l, t, r + dx, b + dy);

                        // 获取移动后的位置
                        sx = (int) event.getRawX();
                        sy = (int) event.getRawY();
                        break;
//                    case MotionEvent.ACTION_UP:// 手指离开屏幕对应事件
//                        // 记录最后图片在窗体的位置
//                        int lasty = MyWindow.this.getTop();
//                        int lastx = MyWindow.this.getLeft();
//                        iv_dv_view.setImageResource(R.drawable.next);
//                        SharedPreferences.Editor editor = sp.edit();
//                        editor.putInt("lasty", lasty);
//                        editor.putInt("lastx", lastx);
//                        editor.commit();
//                        break;
                }
                return true;
            }
        });
    }

    private WebView webView;
    private EditText editText;

    private boolean maxed = false;
    private boolean mined = false;

    private int unMaxLeft;
    private int unMaxTop;
    private int unMaxRight;
    private int unMaxBottom;
    private int unMaxWidth;
    private int unMaxHeight;

    public void setOnWindowBarItemClickListener(OnWindowBarItemClickListener onWindowBarItemClickListener) {
        this.onWindowBarItemClickListener = onWindowBarItemClickListener;
    }

    private OnWindowBarItemClickListener onWindowBarItemClickListener;

    public void setOnFocusListener(OnFocusListener onFocusListener) {
        this.onFocusListener = onFocusListener;
    }

    private OnFocusListener onFocusListener;

//    public static float getScreenDensity(Context context) {
//        return context.getResources().getDisplayMetrics().density;
//    }


//    public static int dip2px(Context context, float px) {
//        final float scale = getScreenDensity(context);
//        return (int) (px * scale + 0.5);
//    }
//
//    public static float px2dip(Context context, float dp) {
//        final float scale = getScreenDensity(context);
//        return (float) (dp - 0.5)/scale;
//    }

    public interface OnWindowBarItemClickListener{
        void onMin(WebWindow window);
        void onClose(WebWindow window);
    }

    public interface OnFocusListener{
        void onFocus(WebWindow window);
    }
}

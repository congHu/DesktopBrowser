package hyc.desktopbrowser;

import android.app.WallpaperManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout windowArea;
    private LinearLayout taskbar;

    private HashMap<String, WebWindow> windows = new HashMap<>();

    private String focusWindowId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        RelativeLayout desktopView = (RelativeLayout) findViewById(R.id.desktop);
        desktopView.setBackground(WallpaperManager.getInstance(this).getDrawable());

        windowArea = (RelativeLayout) findViewById(R.id.window_area);
        taskbar = (LinearLayout) findViewById(R.id.taskbar);

        // 点击开始按钮
        findViewById(R.id.start_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final WebWindow window = new WebWindow(MainActivity.this);
                // 默认800*600窗口大小
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(800, 600);
                window.setLayoutParams(layoutParams);

                windowArea.addView(window);
                taskbar.addView(window.getTaskbarItem());
                // 储存窗口的id和对象
                windows.put(window.getWebWindowId(), window);
                // 窗口变成前置时记录窗口id
                focusWindowId = window.getWebWindowId();

                window.setOnWindowBarItemClickListener(new WebWindow.OnWindowBarItemClickListener() {
                    @Override
                    public void onMin(WebWindow window) {

                    }

                    @Override
                    public void onClose(WebWindow window) {
                        // 移除window对象
                        windows.remove(window.getWebWindowId());
                    }
                });

                window.setOnFocusListener(new WebWindow.OnFocusListener() {
                    @Override
                    public void onFocus(WebWindow window) {
                        // 窗口变成前置时记录窗口id
                        focusWindowId = window.getWebWindowId();
                    }
                });

            }
        });



    }

    public String getFocusWindowId() {
        return focusWindowId;
    }
}

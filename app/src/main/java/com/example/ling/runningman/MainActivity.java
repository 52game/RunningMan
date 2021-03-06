package com.example.ling.runningman;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private WebView mWebView;
    private static final int SDK_PAY_FLAG = 1001;

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebView = (WebView) findViewById(R.id.webview);
//        mWebView.loadUrl("file:///android_asset/js_java_interaction.html");//加载本地asset下面的js_java_interaction.html文件
        mWebView.loadUrl("http://rider.shjcqg.com/index");//加载本地assets下面的js_java_interaction.html文件




        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);//打开js支持\
        webSettings.setDomStorageEnabled(true);
        /**
         * 打开js接口給H5调用，参数1为本地类名，参数2为别名；h5用window.别名.类名里的方法名才能调用方法里面的内容，例如：window.android.back();
         * */
        mWebView.addJavascriptInterface(new JsInteration(), "android");
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient());




    }
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SDK_PAY_FLAG:
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    //同步获取结果
                    String resultInfo = payResult.getResult();
                    Log.i("Pay", "Pay:" + resultInfo);
                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {
                        Toast.makeText(MainActivity.this, "支付成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "支付失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };
    /**
     * 自己写一个类，里面是提供给H5访问的方法
     * */
    public class JsInteration {

        @JavascriptInterface//一定要写，不然H5调不到这个方法
        public void AliPay(final String orderInfo) {
            Runnable payRunnable = new Runnable() {

                @Override
                public void run() {
                    //新建任务
                    PayTask alipay = new PayTask(MainActivity.this);
                    //获取支付结果
                    Map<String, String> result = alipay.payV2(orderInfo, true);
                    Message msg = new Message();
                    msg.what = SDK_PAY_FLAG;
                    msg.obj = result;
                    mHandler.sendMessage(msg);
                }
            };
            // 必须异步调用
            Thread payThread = new Thread(payRunnable);
            payThread.start();

        }
    }

    //点击按钮，访问H5里带返回值的方法
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void onClick(View v) {
        Log.e("TAG", "onClick: ");

        //        mWebView.loadUrl("JavaScript:show()");//直接访问H5里不带返回值的方法，show()为H5里的方法


        //传固定字符串可以直接用单引号括起来
        mWebView.loadUrl("javascript:alertMessage('哈哈')");//访问H5里带参数的方法，alertMessage(message)为H5里的方法

        //当出入变量名时，需要用转义符隔开
        String content="9880";
        mWebView.loadUrl("javascript:alertMessage(\""   +content+   "\")"   );


        //Android调用有返回值js方法，安卓4.4以上才能用这个方法
        mWebView.evaluateJavascript("sum(1,2)", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                Log.d(TAG, "js返回的结果为=" + value);
                Toast.makeText(MainActivity.this,"js返回的结果为=" + value,Toast.LENGTH_LONG).show();
            }
        });
    }


}

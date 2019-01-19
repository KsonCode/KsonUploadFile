package com.kson.ksonuploadfile;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CHOOSE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void takephoto(View view) {

        Matisse.from(this)
                .choose(MimeType.ofAll()) // 选择 mime 的类型
                .countable(true) // 显示选择的数量
                .capture(true)  // 开启相机，和 captureStrategy 一并使用否则报错
                .captureStrategy(new CaptureStrategy(true, "com.meiqu.pianxin.ui.publish.MyFileProvider")) // 拍照的图片路径
                .theme(R.style.Matisse_Dracula) // 黑色背景
                .maxSelectable(9) // 图片选择的最多数量
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f) // 缩略图的比例
                .imageEngine(new GlideEngine()) // 使用的图片加载引擎
                .forResult(REQUEST_CODE_CHOOSE); // 设置作为标记的请求码，返回图片时使用

    }

    private List<String> path = new ArrayList<>();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {

            path = Matisse.obtainPathResult(data);

            System.out.println("path:" + path);

            uploadFile();

        }
    }

    /**
     * 上传头像
     */
    private void uploadFile() {

        Map<String, Object> params = new HashMap<>();
        params.put("uid","71");
        params.put("file",new File(path.get(0)));

        //创建OkHttpClient请求对象

        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        //MultipartBody多功能的请求实体对象,,,formBody只能传表单形式的数据
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);

        //参数
        if (params != null) {
            for (Map.Entry<String, Object> map : params.entrySet()) {

                String key = map.getKey();
                Object value = map.getValue();

                if (value instanceof File) {
                    File file = (File) value;
                    builder.addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("image/*"), file));
                } else {
                    builder.addFormDataPart(key, (String) value);
                }


            }


        }

        //创建Request
        final Request request = new Request.Builder().url("http://www.zhaoapi.cn/file/upload").post(builder.build()).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Toast.makeText(MainActivity.this, response.body().string(), Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });

            }
        });

    }
}

package com.example.exoplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity {

    private RecyclerView.Adapter adapter;
    private RecyclerView videoRecyclerView;
    private GridLayoutManager layoutManager;

    private boolean IS_IN_GRID_VIEW = false;

    private ArrayList<VideoObject> videoList;

    private final int REQUEST_CODE = 1;
    private String languageToLoad = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //làm phần đổ bóng phần trên và dưới của action bar biến mất
        getSupportActionBar().setElevation(0);

        videoList = new ArrayList<>();

        videoRecyclerView = findViewById(R.id.video_recycler_view);
        videoRecyclerView.setSelected(true);
        layoutManager = new GridLayoutManager(this, 1);
        videoRecyclerView.setLayoutManager(layoutManager);

        adapter = new RecyclerAdapter(videoList, this, R.layout.video_item_layout);
        videoRecyclerView.setAdapter(adapter);

        //Cấp quyền truy cập bộ nhớ
        //Nếu kiểm tra mà chưa được cấp quyền thì phải gọi lệnh yêu cầu cấp quyền
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.permission_title).setNegativeButton(R.string.permission_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setMessage(R.string.permission_message)
                        .setPositiveButton(R.string.permission_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
                            }
                        }).show();

            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
            }

            //Còn nếu đã cấp quyền rồi thì lấy danh sách video được trả về từ hàm getVideoList() gán vào cho videoList
        } else {
            videoRecyclerView.setAdapter(adapter);
            videoList.clear();
            videoList.addAll(getVideoList());
            adapter.notifyDataSetChanged();
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        //tạo đối tượng Window để gắn cờ báo cho hệ thống biết là đặt status bar trong suốt
        Window w = getWindow();
        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        //phần này để gán background màu gradient cho action bar
        //do hàm getDrawable bắt đầu có từ Android Lollipop nên phải so sánh phiên bản hiện tại mới được gọi hàm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Drawable background = getResources().getDrawable(R.drawable.gradient_background);
            getSupportActionBar().setBackgroundDrawable(background);
            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            w.setStatusBarColor(this.getResources().getColor(R.color.transparent));
            w.setNavigationBarColor(this.getResources().getColor(R.color.transparent));
            w.setBackgroundDrawable(background);
        }


        videoRecyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        //Kiểm tra xem hiện tại list đang ở gridview 1 cột hay gridview 2 cột, từ đó sử dụng layout tương ứng
        if (i == R.id.change_grid_option) {
            if (IS_IN_GRID_VIEW == false) {
                item.setIcon(R.drawable.list_item);
                layoutManager.setSpanCount(2);
                videoRecyclerView.setLayoutManager(layoutManager);
                adapter = new RecyclerAdapter(videoList, this, R.layout.video_item_layout_grid);
                videoRecyclerView.setAdapter(adapter);
                IS_IN_GRID_VIEW = true;
            } else {
                item.setIcon(R.drawable.grid_item);
                layoutManager.setSpanCount(1);
                videoRecyclerView.setLayoutManager(layoutManager);
                adapter = new RecyclerAdapter(videoList, this, R.layout.video_item_layout);
                videoRecyclerView.setAdapter(adapter);
                IS_IN_GRID_VIEW = false;
            }
        } else if (i == R.id.language_choose_option) {
            showLanguageDialog();
        } else if (i == R.id.about_us_option) {
            Intent intent = new Intent(MainActivity.this, AboutUsActivity.class);
            startActivity(intent);
        }
        return true;
    }

    private void showLanguageDialog() {
        String english = getResources().getString(R.string.english);
        String vietnamese = getResources().getString(R.string.vietnamese);

        AlertDialog.Builder builer = new AlertDialog.Builder(this)
                .setTitle(R.string.language_choose_dialog)
                .setSingleChoiceItems(new String[]{english, vietnamese}, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            languageToLoad = "en";
                        } else if (which == 1) {
                            languageToLoad = "vi";
                        }
                    }
                }).setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadLanguage(languageToLoad);
                    }
                }).setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builer.show();
    }

    private void loadLanguage(String language) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration configuration = new Configuration();

            configuration.setLocale(locale);
            getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
            this.finish();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

    }

    //hàm lấy danh sách toàn bộ video có trong máy, thông qua URI MediaStore
    public ArrayList<VideoObject> getVideoList() {
        ArrayList<VideoObject> arrayList = new ArrayList<>();
        Cursor cursor = getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Video.VideoColumns.DISPLAY_NAME,
                        MediaStore.Video.VideoColumns.DURATION,
                        MediaStore.Video.VideoColumns.DATE_ADDED,
                        MediaStore.Video.VideoColumns.DATA,
                        MediaStore.Video.VideoColumns.SIZE,
                },
                null,
                null,
                MediaStore.Video.VideoColumns.DATE_ADDED + " DESC", // sắp xếp theo thứ tự video được thêm vào mới nhất
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                //tên
                String name = cursor.getString(0);
                String[] nameArray = name.split(".mp4");
                name = nameArray[0];
                //độ dài video
                long durationLong = cursor.getLong(1);
                String durationString = getTimeFromMillis(durationLong);
                //ngày được thêm vào
                long dateAddedLong = cursor.getLong(2);
                String dateAddedString = getDateFromTimeStamp(dateAddedLong);
                //đường dẫn URI của video
                String path = cursor.getString(3);
                //kích cỡ của video (trả về byte)
                long size = cursor.getLong(4);
                double sizeDouble = Double.valueOf(String.valueOf(size)) / (1024 * 1024);
                String videoSize = String.format("%.2f MB", sizeDouble);

                VideoObject aVideo = new VideoObject(name, durationString, dateAddedString, path, videoSize);
                arrayList.add(aVideo);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return arrayList;
    }

    //đổi định dạng thời gian từ mili giây sang định dạng phút:giây tương ứng
    public String getTimeFromMillis(long millis) {
        return new SimpleDateFormat("mm:ss").format(new Date(millis));
    }

    //đôi ngày từ định dạng TimeStamp (kiểu Long) về định dạng có thể đọc được
    public String getDateFromTimeStamp(long dateLong) {
        Date date = new Date((long) dateLong * 1000);
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.format(date);
    }



    //Kết quả sau khi yêu cầu quyền truy cập
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                //được phép truy cập
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    videoList.addAll(getVideoList());
                    adapter.notifyDataSetChanged();
                    //ngược lại
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.permission_title).setNegativeButton(R.string.permission_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).setMessage(R.string.permission_message)
                            .setPositiveButton(R.string.permission_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ActivityCompat.requestPermissions(MainActivity.this,
                                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
                                }
                            }).show();
                }
        }
    }
}

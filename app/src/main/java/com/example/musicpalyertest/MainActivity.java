package com.example.musicpalyertest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {


    private static final int INTERNAL_TIME = 1000;
    final MediaPlayer mp = new MediaPlayer();//实例化播放器
    String song_path = "";
    private SeekBar seekBar;//进度条
    private TextView currentTV;//实时时间
    private TextView totalTV;//总时间
    private TextView songName;//当前播放曲目
    boolean isStop = true;
    private boolean isSeekBarChanging;//互斥变量，防止进度条与定时器冲突。
    private int currentposition;//当前音乐播放的进度
    private Timer timer;
    private ArrayList<String> listName;//音乐名称，用于初次启动后扫描设备所得
    private ArrayList<String> listPath;//音乐路径，用于初次启动后扫描设备所得
    private ArrayList<String> listNameRefresh;//音乐名称，用于后续刷新后扫描
    private ArrayList<String> listPathRefresh;//音乐路径，用于后续刷新后扫描
    private Button switcher;
    private Button delete;
    private Button refresh;


    /**
     * 处理handler消息
     * 利用message传递的消息实时更新进度条
     */

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            // 展示给进度条和当前时间
            int progress = mp.getCurrentPosition();
            seekBar.setProgress(progress);
            currentTV.setText(formatTime(progress));
            // 继续定时发送数据
            updateProgress();
            return true;
        }
    });

    //3、使用formatTime方法对时间格式化：
    private String formatTime(int length) {
        Date date = new Date(length);
        //时间格式化工具
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        String totalTime = sdf.format(date);
        return totalTime;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        totalTV = findViewById(R.id.music_total_time);
        currentTV = findViewById(R.id.music_current_time);
        songName = findViewById(R.id.songName);
        seekBar = (SeekBar) findViewById(R.id.music_seekbar);
        seekBar.setOnSeekBarChangeListener(new MySeekBar());

//        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(MainActivity.this,
//                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
//            return;
//        }
//
//        //判断是否是AndroidN以及更高的版本 N=24
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//            StrictMode.setVmPolicy(builder.build());
//        }

        /**
         * 利用工具类扫描手机内部的.mp3文件
         * 将扫描完毕的文件按照列表的形式返回至音乐播放器
         * 返回值有音乐名称与音乐路径
         * @author 阿久琉璃
         */

        listPath = (ArrayList<String>) MusicUtils.getMusicData(MainActivity.this);   //音乐路径
        listName = (ArrayList<String>) MusicUtils.getMusicName(MainActivity.this);   //音乐名字

        listPathRefresh = (ArrayList<String>) MusicUtils.getMusicData(MainActivity.this);   //音乐路径
        listNameRefresh = (ArrayList<String>) MusicUtils.getMusicName(MainActivity.this);   //音乐名字


        /**
         * 播放模式按钮选择器
         * 根据用户点击，切换播放模式
         * 目前支持顺序播放，单曲循环，随机播放
         * @author：阿久琉璃
         */

        switcher = findViewById(R.id.switcher);
        switcher.setText("顺序循环");
        if (switcher.getText().toString() == "顺序循环") {
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    currentposition = currentposition + 1;
                    changeMusic(currentposition);
                }
            });         //顺序播放
        }
        switcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (switcher.getText().toString() == "随机循环") {
                    switcher.setText("顺序循环");
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            currentposition = currentposition + 1;
                            changeMusic(currentposition);
                        }
                    });         //顺序播放
                }
                else if (switcher.getText().toString() == "顺序循环"){
                    switcher.setText("单曲循环");
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            changeMusic(currentposition);
                        }
                    });         //顺序播放
                }
                else if (switcher.getText().toString() == "单曲循环") {
                    switcher.setText("随机循环");
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            currentposition = (int)(0 + Math.random() * (listPath.size() - 1 - 0 + 1));
                            changeMusic(currentposition);
                        }
                    });         //顺序播放
                }
            }
        });


        /**
         * 绑定音乐名称与音乐路径
         * 将对应的音乐名称显示在listView中
         * 添加删除按钮
         * @author：阿久琉璃
         */

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_selectable_list_item,
                listName);

        final ArrayAdapter<String> adapterRefresh = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_selectable_list_item,
                listNameRefresh);

        delete = (Button)findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listName.remove(currentposition);
                listPath.remove(currentposition);
                adapter.notifyDataSetChanged();
                if (listName.size() != 0)
                    changeMusic(currentposition);
                else {
                    timer = new Timer();
                    mp.stop();
                    totalTV.setText("00:00");
                    songName.setText("当前无音乐播放！");
                    Toast.makeText(MainActivity.this, "请添加歌曲！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        final ListView li = (ListView) findViewById(R.id.listView1);
        li.setAdapter(adapter);

        /**
         * 导入按钮
         * 用户在点击倒入按钮后，将会重新扫描手机中的文件，并且修改此时listView中的内容
         */

        refresh = (Button)findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                li.setAdapter(adapterRefresh);
            }
        });

        /**
         * listView显示
         * listView显示自己所接收的list文件，分别为音乐名称与音乐路径
         * 音乐名称显示在listView界面，后台分别绑定对应的音乐路径
         * 用户在点击后，初始化播放器，开始播放对应路径下的音乐
         * @autho：阿久琉璃
         */

        final ImageButton btnpause = (ImageButton) findViewById(R.id.btn_pause);

            li.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    btnpause.setImageResource(android.R.drawable.ic_media_pause);
                    song_path = ((TextView) view).getText().toString();
                    currentposition = position;
                    changeMusic(currentposition);
                    try {
                        mp.reset();    //重置
                        mp.setDataSource(song_path);        //获取音乐路径
                        mp.prepare();     //准备
                        mp.start(); //播放

                        seekBar.setMax(mp.getDuration());
                        isStop = false;
                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (!isSeekBarChanging) {
                                    seekBar.setProgress(mp.getCurrentPosition());
                                }
                            }
                        }, 0, 50);
                    } catch (Exception e) {
                    }
                }
            });

        /**
         * 暂停与播放
         * 用户在点击时，判断当前播放器的状态
         * 若此时没有选中的音乐在播放，该按钮无效，并且返回提示
         * 若有音乐在播放器中，则根据点击时的状态分别进行操作
         * @auth：阿久琉璃
         */

        btnpause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (song_path.isEmpty())
                    Toast.makeText(getApplicationContext(), "先选收歌曲先听听", Toast.LENGTH_SHORT).show();
                if (mp.isPlaying()) {
                    mp.pause();  //暂停
                    isStop = true;
                    btnpause.setImageResource(android.R.drawable.ic_media_play);
                } else if (!song_path.isEmpty()) {
                    mp.start();   //继续播放
                    isStop = false;
                    btnpause.setImageResource(android.R.drawable.ic_media_pause);
                }
            }
        });


        /**
         * 切换歌曲
         * 用户点击后，分别对当前正在播放的音乐位置进行判断
         * 依照判断的结果，提取不同位置的list位置音乐进行播放
         * 核心方法changeMusic();
         * @author：阿久琉璃
         */

        final ImageButton previous = (ImageButton) findViewById(R.id.previous);
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentposition = currentposition - 1;
                changeMusic(currentposition);
            }
        });

        final ImageButton next = (ImageButton) findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentposition = currentposition + 1;
                changeMusic(currentposition);
            }
        });
    }


    /**
     * @description：切换歌曲
     * @param position
     * 服务于各种与切换歌曲有关的工具，核心思路为修改当前音乐的播放路径
     * 判断当前播放音乐的位置，若在list的首尾，则进行特殊操作
     * 若为一般位置，则修改下一首歌的路径，并且初始化播放器
     * @author：阿久琉璃
     */

    private void changeMusic(int position) {
        if (position < 0) {         //list头部，修改位置至尾部
            currentposition = listPath.size() - 1;
        } else if (position == listPath.size()) {       //；list尾部，修改位置至头部
            currentposition = 0;
        }

        if(listName.size() == listNameRefresh.size()) {         //判断当前所使用的list对象
            song_path = listPath.get(currentposition);
            songName.setText(listName.get(currentposition));

            try {
                // 切歌之前先重置，释放掉之前的资源
                mp.reset();
                // 设置播放源
                mp.setDataSource(song_path);
                // 开始播放前的准备工作，加载多媒体资源，获取相关信息
                mp.prepare();

                // 开始播放
                mp.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            seekBar.setProgress(0);//将进度条初始化
            seekBar.setMax(mp.getDuration());//设置进度条最大值为歌曲总时间
            totalTV.setText(formatTime(mp.getDuration()));//显示歌曲总时长

            updateProgress();//更新进度条
        }
        else {
            song_path = listPathRefresh.get(currentposition);
            songName.setText(listNameRefresh.get(currentposition));

            try {
                // 切歌之前先重置，释放掉之前的资源
                mp.reset();
                // 设置播放源
                mp.setDataSource(song_path);
                // 开始播放前的准备工作，加载多媒体资源，获取相关信息
                mp.prepare();

                // 开始播放
                mp.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            seekBar.setProgress(0);//将进度条初始化
            seekBar.setMax(mp.getDuration());//设置进度条最大值为歌曲总时间
            totalTV.setText(formatTime(mp.getDuration()));//显示歌曲总时长

            updateProgress();//更新进度条
        }
    }

    /**
     * 进度条更新与拖动
     *利用handler和message来更新进度条
     */

    private void updateProgress() {
        // 使用Handler每间隔1s发送一次空消息，通知进度条更新
        Message msg = Message.obtain();// 获取一个现成的消息
        // 使用MediaPlayer获取当前播放时间除以总时间的进度
        int progress = mp.getCurrentPosition();
        msg.arg1 = progress;
        mHandler.sendMessageDelayed(msg, INTERNAL_TIME);
    }


    public class MySeekBar implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
        }

        /*滚动时,应当暂停后台定时器*/
        public void onStartTrackingTouch(SeekBar seekBar) {
            isSeekBarChanging = true;
        }

        /*滑动结束后，重新设置值*/
        public void onStopTrackingTouch(SeekBar seekBar) {
            isSeekBarChanging = false;
            mp.seekTo(seekBar.getProgress());
        }

    }

    protected void onDestroy() {
        super.onDestroy();
        if (mp != null) {
            mp.stop();
            mp.release();
        }
        Toast.makeText(getApplicationContext(), "已退出应用！", Toast.LENGTH_SHORT).show();
    }
}


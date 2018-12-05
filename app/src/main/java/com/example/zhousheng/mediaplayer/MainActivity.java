package com.example.zhousheng.mediaplayer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.zhousheng.mediaplayer.Music;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
private MediaPlayer mediaPlayer=new MediaPlayer();
protected ArrayList<Music> musiclist;
    private ArrayList<Map<String, Object>> music_item;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button play=(Button)findViewById(R.id.playButton);
        Button pause=(Button)findViewById(R.id.pauseButton);
        Button stop=(Button)findViewById(R.id.stopButton);
        ListView listview = (ListView)findViewById(R.id.list_view);
        play.setOnClickListener(this);
        pause.setOnClickListener(this);
        stop.setOnClickListener(this);
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]
                    {
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
        else
        {
            initMediaPlayer();
        }
    }

    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults)
    {
        switch (requestCode)
        {
            case 1:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    initMediaPlayer();
                }
                else
                {
                    Toast.makeText(this,"拒绝权限将无法使用该程序！",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }
    public String change_size(long size)
    {
        long GB = 1024 * 1024 * 1024;//定义GB的计算常量
        long  MB = 1024 * 1024;//定义MB的计算常量
        long KB = 1024;//定义KB的计算常量
        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
        String resultSize = "";
        if (size / GB >= 1) {
            //如果当前Byte的值大于等于1GB
            resultSize = df.format(size / (float) GB) + "GB   ";
        } else if (size / MB >= 1) {
            //如果当前Byte的值大于等于1MB
            resultSize = df.format(size / (float) MB) + "MB   ";
        } else if (size / KB >= 1) {
            //如果当前Byte的值大于等于1KB
            resultSize = df.format(size / (float) KB) + "KB   ";
        } else {
            resultSize = size + "B   ";
        }
      return resultSize;

    }
private void initMediaPlayer()
{
    findMusic();
    ListView listview = (ListView)findViewById(R.id.list_view);
    music_item = new ArrayList<Map<String, Object>>();
    for (Iterator iterator = musiclist.iterator(); iterator.hasNext();) {
        Map<String, Object> map = new HashMap<String, Object>();
        Music mp3 = (Music) iterator.next();
        map.put("title", mp3.getTitle());
        map.put("singer", mp3.getSinger());
        map.put("album", mp3.getAlbum());
        map.put("size",change_size(mp3.getSize()));
        map.put("duration", mp3.getDuration());
        map.put("url", mp3.getUrl());
        //map.put("bitmap", R.drawable.musicfile);
        music_item.add(map);
    }
    SimpleAdapter mSimpleAdapter = new SimpleAdapter(
        this,
        music_item,
        R.layout.music_list_item,
        new String[] {"title","size","singer", "album"},
        new int[] {R.id.title,R.id.size,R.id.singer,R.id.album});
    listview.setAdapter(mSimpleAdapter);

}

    public void findMusic(){
        musiclist = new ArrayList<Music>();
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if(cursor.moveToFirst()){
            while (!cursor.isAfterLast()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));  //id号
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));  //歌名
                System.out.println(title);
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));  //专辑
                int albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));   //专辑号
                String singer = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));  //歌手
                System.out.println(singer);
                String url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));   //路径
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));  //事件
                Long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));  //大小
                if (size >1024*800){//大于800K
                    Music music = new Music();
                    music.setId(id);
                    music.setSinger(singer);
                    music.setSize(size);
                    music.setTitle(title);
                    music.setDuration(duration);
                    music.setUrl(url);
                    music.setAlbum(album);
                    music.setAlbumId(albumId);
                    musiclist.add(music);
                }
                cursor.moveToNext();
            }
        }
    }

    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.playButton:
                if(!mediaPlayer.isPlaying())
                {
                    mediaPlayer.start(); //开始播放
                }
                break;
            case R.id.pauseButton:
                if(!mediaPlayer.isPlaying())
                {
                    mediaPlayer.pause();
                }
                break;
            case R.id.stopButton:
                if(mediaPlayer.isPlaying())
                {
                    mediaPlayer.reset();
                    initMediaPlayer();
                }
                break;
            default:
                break;
        }
    }

    protected void onDestroy()
{
    super.onDestroy();
    if(mediaPlayer!=null)
    {
        mediaPlayer.stop();
        mediaPlayer.release();
    }
}

}

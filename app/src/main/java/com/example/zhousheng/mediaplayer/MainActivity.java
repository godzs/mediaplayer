package com.example.zhousheng.mediaplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static java.lang.String.format;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private MediaPlayer mediaPlayer=new MediaPlayer();
    protected ArrayList<Music> musiclist;
    Handler handler=new Handler();
    int time;
    private Bitmap circle_photo;
    SeekBar mediaSeekbar;
    private boolean click;

    private ArrayList<Map<String, Object>> music_item;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView  small_title=(TextView)findViewById(R.id.small_title);
        final  TextView  small_singer=(TextView)findViewById(R.id.small_singer);
        final ImageView imag=(ImageView)findViewById(R.id.imag);
        mediaSeekbar=(SeekBar)findViewById(R.id.mediaSeekBar);

        final TextView  time_end=(TextView)findViewById(R.id.time_end);
        //mediaSeekbar.setOnSeekBarChangeListener((SeekBar.OnSeekBarChangeListener) this);
        Button play=(Button)findViewById(R.id.playButton);
        Button pause=(Button)findViewById(R.id.pauseButton);
        Button stop=(Button)findViewById(R.id.stopButton);
        ListView listview = (ListView)findViewById(R.id.list_view);
        play.setOnClickListener(this);
        mediaSeekbar.setOnSeekBarChangeListener(sbLis);
        pause.setOnClickListener(this);
        stop.setOnClickListener(this);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                time=musiclist.get(position).getDuration();
                small_title.setText(musiclist.get(position).getTitle());
                small_singer.setText(musiclist.get(position).getSinger());
               circle_photo=getRoundedCornerBitmap(musiclist.get(position).getPhoto(),2);
                imag.setImageBitmap(circle_photo);
                System.out.println(musiclist.get(position).getDuration());
                time_end.setText(change_time(musiclist.get(position).getDuration()));
                small_title.setTextColor(Color.BLUE);
                mediaSeekbar.setMax(time);
                handler.post(updateseekbar);
                Uri uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, String.valueOf(musiclist.get(position).getId()));   //找到路径
                mediaPlayer.reset();
                try {

                    mediaPlayer.setDataSource(MainActivity.this, uri);
                    mediaPlayer.prepare();
                    mediaPlayer.start();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

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
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float ratio) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, bitmap.getWidth() / ratio,
                bitmap.getHeight() / ratio, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    Runnable updateseekbar =new Runnable(){      //实现多线程操作
        @Override
        public void run() {
            // TODO Auto-generated method stub
            handler.postDelayed(updateseekbar, 60);    //更新频率
            mediaSeekbar.setProgress(mediaPlayer.getCurrentPosition());
            TextView time_begin = (TextView) findViewById(R.id.time_begin);
            time_begin.setText(change_time(mediaPlayer.getCurrentPosition()));

        }

    };

    private Bitmap getAlbumArt(int albumID) {   //通过封面id找到图片
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[]{"album_art"};
        Cursor cur = getContentResolver().query(Uri.parse(mUriAlbums + "/" + Integer.toString(albumID)), projection, null, null, null);
        String album_art = null;
        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
            cur.moveToNext();
            album_art = cur.getString(0);
        }
        cur.close();
        Bitmap bm = null;
        if (album_art != null) {
            bm = BitmapFactory.decodeFile(album_art);
        }
        return bm;
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
                    Toast.makeText(this,"拒绝权限无法使用该程序！",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }
    public String change_size(long size)
    {
        long GB = 1024 * 1024 * 1024;
        long  MB = 1024 * 1024;
        long KB = 1024;
        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
        String resultSize = "";
        if (size / GB >= 1) {

            resultSize = df.format(size / (float) GB) + "GB   ";
        } else if (size / MB >= 1) {

            resultSize = df.format(size / (float) MB) + "MB   ";
        } else if (size / KB >= 1) {

            resultSize = df.format(size / (float) KB) + "KB   ";
        } else {
            resultSize = size + "B   ";
        }
      return resultSize;

    }
    public String change_time(int time)
    {
        String result_time;
        int new_time=time/1000;
        int min=new_time/60;
        int sec=new_time%60;
        result_time=format("%02d:%02d",min,sec);
        return result_time;
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
                int albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));   //专辑封面id
                String singer = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));  //歌手
                System.out.println(singer);
                String url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));   //路径
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));  //事件
                Long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));  //大小
                Bitmap photo=getAlbumArt(albumId);
                if (size >1024*800){//大于800K
                    Music music = new Music();
                    music.setId(id);
                    music.setTitle(title);
                    music.setSinger(singer);
                    music.setAlbum(album);
                    music.setDuration(duration);
                    music.setSize(size);
                    music.setUrl(url);
                    music.setAlbumId(albumId);
                    music.setPhoto(photo);
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
                    handler.post(updateseekbar);
                    click=false;
                }
                break;
            case R.id.pauseButton:
                if(true == click)
            {
                Toast.makeText(MainActivity.this, "您已暂停音乐！", Toast.LENGTH_SHORT).show();
                break;
            }
                if(mediaPlayer.isPlaying())
                {
                    //System.out.println("1111111");
                    mediaPlayer.pause();
                    click=true;
                }

                break;
            case R.id.stopButton:
                    mediaSeekbar.setProgress(0);
                    mediaPlayer.seekTo(0);
                    mediaPlayer.pause();
                break;
            default:
                break;
        }
    }
    private SeekBar.OnSeekBarChangeListener sbLis=new SeekBar.OnSeekBarChangeListener(){
        @Override
       public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    }

                @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub

                    }

               @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
                        mediaPlayer.seekTo(mediaSeekbar.getProgress());
                        //SeekBar确定位置后，跳到指定位置
                    }

            };

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

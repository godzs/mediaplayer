package com.example.zhousheng.mediaplayer;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.example.zhousheng.mediaplayer.Music;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getAlldata();
    }


    public void getAlldata() {
        ContentResolver cr = getApplication().getContentResolver();
        if (cr == null) {
            return;
        }
        // 获取所有歌曲
        Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (null == cursor) {
            return;
        }
        Music music;
        List<Music> list = new ArrayList<Music>();
        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));  //id号
                //歌曲名
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                System.out.println(title);

                //歌手
                String singer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

                //专辑
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));

                //长度
                long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));

                //时长
                int duration = cursor.getInt(cursor
                        .getColumnIndex(MediaStore.Audio.Media.DURATION));

                //路径
                String url = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media.DATA));

                //显示的文件名
                String _display_name = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));

                //类型
                String mime_type = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media.MIME_TYPE));

                music = new Music();
                music.setAlbum(album);
                music.setDuration(duration);
                music.setSinger(singer);
                music.setSize(size);
                music.setTitle(title);
                music.setUrl(url);
                music.set_display_name(_display_name);
                music.setMime_type(mime_type);
                list.add(music);
            }while (cursor.moveToNext());
            System.out.println(list.size());
        }
    }
}

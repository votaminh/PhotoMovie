package com.hw.photomovie.sample;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.hw.photomovie.PhotoMovie;
import com.hw.photomovie.PhotoMovieFactory;
import com.hw.photomovie.PhotoMoviePlayer;
import com.hw.photomovie.model.PhotoData;
import com.hw.photomovie.model.PhotoSource;
import com.hw.photomovie.model.SimplePhotoData;
import com.hw.photomovie.record.GLMovieRecorder;
import com.hw.photomovie.render.GLSurfaceMovieRenderer;
import com.hw.photomovie.render.GLTextureMovieRender;
import com.hw.photomovie.render.GLTextureView;
import com.hw.photomovie.render.MovieRenderer;
import com.hw.photomovie.sample.widget.FilterItem;
import com.hw.photomovie.sample.widget.FilterType;
import com.hw.photomovie.sample.widget.MovieFilterView;
import com.hw.photomovie.sample.widget.MovieTransferView;
import com.hw.photomovie.sample.widget.TransferItem;
import com.hw.photomovie.timer.IMovieTimer;
import com.hw.photomovie.util.MLog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by huangwei on 2018/9/9.
 */
public class DemoPresenter {
    private PhotoMovie mPhotoMovie;
    private PhotoMovieFactory.PhotoMovieType mMovieType = PhotoMovieFactory.PhotoMovieType.HORIZONTAL_TRANS;
    private Activity activity;

    public DemoPresenter(Activity activity){
        this.activity = activity;
    }

    public void saveVideo() {
        final ProgressDialog dialog = new ProgressDialog(activity);
        dialog.setMessage("saving video...");
        dialog.setMax(100);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(false);
        dialog.show();
        final long startRecodTime = System.currentTimeMillis();
        final GLMovieRecorder recorder = new GLMovieRecorder(activity);
        final File file = initVideoFile();

        int width = 1920;
        int height = 1920;

        int bitrate = width * height > 1000 * 1500 ? 8000000 : 4000000;
        recorder.configOutput(width, height, bitrate, 30, 1, file.getAbsolutePath());
        PhotoMovie newPhotoMovie = PhotoMovieFactory.generatePhotoMovie(mPhotoMovie.getPhotoSource(), mMovieType);
        GLSurfaceMovieRenderer newMovieRenderer = new GLSurfaceMovieRenderer();
        newMovieRenderer.setPhotoMovie(newPhotoMovie);

        recorder.setDataSource(newMovieRenderer);
        recorder.startRecord(new GLMovieRecorder.OnRecordListener() {
            @Override
            public void onRecordFinish(boolean success) {
                File outputFile = file;

                Log.i("lsdfdf", "onRecordFinish: " + outputFile.getAbsolutePath());

                long recordEndTime = System.currentTimeMillis();
                MLog.i("Record", "record:" + (recordEndTime - startRecodTime));
                dialog.dismiss();
                if (success) {
                    Toast.makeText(activity.getApplicationContext(), "Video save to path:" + outputFile.getAbsolutePath(), Toast.LENGTH_LONG).show();

                    activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));


                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(outputFile.getAbsolutePath()));
                    intent.setDataAndType(Uri.parse(outputFile.getAbsolutePath()), "video/mp4");
                    activity.startActivity(intent);
                } else {
                    Toast.makeText(activity.getApplicationContext(), "com.hw.photomovie.record error!", Toast.LENGTH_LONG).show();
                }
                if(recorder.getAudioRecordException()!=null){
                    Toast.makeText(activity.getApplicationContext(), "record audio failed:"+recorder.getAudioRecordException().toString(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onRecordProgress(int recordedDuration, int totalDuration) {
                dialog.setProgress((int) (recordedDuration / (float) totalDuration * 100));
            }
        });
    }

    private File initVideoFile() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!dir.exists()) {
            dir = activity.getCacheDir();
        }
        return new File(dir, String.format("photo_movie_%s.mp4",
                new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(System.currentTimeMillis())));
    }

    public void onPhotoPick(ArrayList<String> photos) {
        List<PhotoData> photoDataList = new ArrayList<PhotoData>(photos.size());
        for (String path : photos) {
            PhotoData photoData = new SimplePhotoData(activity, path, PhotoData.STATE_LOCAL);
            photoDataList.add(photoData);
        }
        PhotoSource photoSource = new PhotoSource(photoDataList);
        mPhotoMovie = PhotoMovieFactory.generatePhotoMovie(photoSource, PhotoMovieFactory.PhotoMovieType.HORIZONTAL_TRANS);
    }
}

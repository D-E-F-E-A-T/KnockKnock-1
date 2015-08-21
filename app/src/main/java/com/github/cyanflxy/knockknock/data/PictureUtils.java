package com.github.cyanflxy.knockknock.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.cyanflxy.dapenti.htmlparser.JokeBean;
import com.github.cyanflxy.knockknock.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.github.cyanflxy.knockknock.AppApplication.baseContext;

public class PictureUtils {
    // 创建在QQ上分享的图片
    public static String createJokePicture(Context c, JokeBean jokeBean) {
        View view = createJokeView(c, jokeBean);

        String parent = Utils.getAppLocalFolder();
        String fileName = String.valueOf(System.currentTimeMillis()) + ".png";
        File picFile = new File(parent, fileName);

        try {
            createViewPicture(view, picFile);
            return picFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    private static View createJokeView(Context c, JokeBean jokeBean) {
        View view = LayoutInflater.from(c).inflate(R.layout.share_joke_img, null);

        TextView title = (TextView) view.findViewById(R.id.joke_title);
        TextView content = (TextView) view.findViewById(R.id.joke_content);

        title.setText(jokeBean.title);
        content.setText(jokeBean.content);

        return view;
    }

    private static void createViewPicture(View view, File filePath) throws IOException {
        Bitmap bitmap = null;
        try {
            bitmap = getViewBitmap(view);
            saveBitmap(bitmap, filePath);
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
    }

    private static Bitmap getViewBitmap(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();

        return view.getDrawingCache();
    }

    private static void saveBitmap(Bitmap bitmap, File file) throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static String getLogoPicture() {
        String parent = Utils.getAppLocalFolder();
        File logoFile = new File(parent, "logo.png");

        if (!logoFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeResource(baseContext.getResources(), R.drawable.logo);
            try {
                saveBitmap(bitmap, logoFile);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        return logoFile.getAbsolutePath();
    }

    public static String getQRPicture() {
        String parent = Utils.getAppLocalFolder();
        File shareQrFile = new File(parent, "share_qr.png");

        if (!shareQrFile.exists()) {
            View view = getQrView();
            try {
                createViewPicture(view, shareQrFile);
            } catch (IOException e) {
                e.printStackTrace();
                shareQrFile.delete();
                return null;
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                shareQrFile.delete();
                return null;
            }
        }

        return shareQrFile.getAbsolutePath();
    }

    private static View getQrView() {
        View parent = LayoutInflater.from(baseContext).inflate(R.layout.dialog_share_app_qr, null);
        return parent.findViewById(R.id.qr_content);
    }
}

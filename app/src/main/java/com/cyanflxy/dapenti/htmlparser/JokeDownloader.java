package com.cyanflxy.dapenti.htmlparser;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class JokeDownloader {

    private RequestQueue requestQueue;

    private List<String> jokeHrefList;
    private List<JokeBean> jokeBeanList;

    private HrefResultListener hrefResultListener;
    private HrefErrorListener hrefErrorListener;
    private ContentResultListener contentResultListener;
    private ContentErrorListener contentErrorListener;

    private OnJokeDownloadListener listener;

    private int hrefDownloadCount;
    private int currentJokePage;
    private int jokeContentErrorNumber;
    private int currentProgress;
    private boolean isQuit;

    public JokeDownloader(Context c, OnJokeDownloadListener l) {
        listener = l;
        requestQueue = Volley.newRequestQueue(c);
    }

    public void download(int lastPage, int lastId) {
        isQuit = false;
        currentProgress = 0;

        currentJokePage = lastPage - 1;
        if (currentJokePage <= 0) {
            currentJokePage = 1;
        }

        jokeContentErrorNumber = 0;
        jokeHrefList = new LinkedList<String>();
        jokeBeanList = new LinkedList<JokeBean>();

        hrefResultListener = new HrefResultListener(lastId);
        hrefErrorListener = new HrefErrorListener();
        contentResultListener = new ContentResultListener(lastId);
        contentErrorListener = new ContentErrorListener();

        requestQueue.start();

        if (lastPage != currentJokePage) {
            downloadJokeHrefUntil(lastPage);
            hrefDownloadCount = 2;
        } else {
            hrefDownloadCount = 1;
        }
        downloadJokeHrefUntil(currentJokePage);

        sendProgress(5);
    }

    private void downloadJokeHrefUntil(int page) {
        JokeHrefRequest request = new JokeHrefRequest(page,
                hrefResultListener, hrefErrorListener);
        requestQueue.add(request);
    }

    private void checkJokeHref(List<String> hrefs) {

        if (hrefs != null && hrefs.size() > 0) {
            jokeHrefList.addAll(hrefs);

            // 逆序下载
            for (int i = hrefs.size() - 1; i >= 0; i--) {
                downloadJoke(hrefs.get(i));
            }
        }

        // 检查进度
        int resultCount = hrefResultListener.resultCount + hrefErrorListener.errorTimes;

        // 链接页面处理完毕
        if (resultCount == hrefDownloadCount) {

            if (jokeHrefList.size() == 0) {
                // 没有笑话链接，就是出错了或者没有了
                requestQueue.stop();
                sendProgress(100);

                if (hrefErrorListener.errorTimes > 0) {
                    listener.onDownloadError(currentJokePage, contentResultListener.maxJokeId);
                } else {
                    listener.onNoMoreContent();
                }
            } else {
                // 有笑话链接，正在下载
                calculateProgress();
            }

        } else {
            calculateProgress();
        }

    }

    private void downloadJoke(String url) {
        JokeContentRequest request = new JokeContentRequest(url,
                contentResultListener, contentErrorListener);
        requestQueue.add(request);
    }

    private void checkEnd() {
        if (isQuit) {
            return;
        }

        // 检查是否下载完毕
        int hrefResultCount = hrefResultListener.resultCount + hrefErrorListener.errorTimes;
        if (hrefResultCount != hrefDownloadCount) {
            calculateProgress();
            return;
        }
        if (jokeBeanList.size() + jokeContentErrorNumber != jokeHrefList.size()) {
            calculateProgress();
            return;
        }

        //下载完毕
        isQuit = true;
        requestQueue.stop();
        sendProgress(100);

        if (jokeBeanList.size() == 0) {
            listener.onDownloadError(currentJokePage, contentResultListener.maxJokeId);
        } else {
            listener.onDownload(jokeBeanList, currentJokePage, contentResultListener.maxJokeId);
        }

    }

    public void cancel() {
        if (isQuit) {
            return;
        }

        isQuit = true;
        requestQueue.stop();

        if (jokeBeanList.size() != 0) {
            listener.onDownload(jokeBeanList, currentJokePage, contentResultListener.maxJokeId);
        }

    }

    // 根据当前状态计算进度
    private void calculateProgress() {
        int progress = 5;

        int hrefResultCount = hrefResultListener.resultCount + hrefErrorListener.errorTimes;
        int jokeResult = jokeBeanList.size() + jokeContentErrorNumber;

        if (hrefResultCount != hrefDownloadCount) {
            progress += 5;
            progress += jokeResult;
        } else {
            progress += 10;
            progress += 85 * jokeResult / jokeHrefList.size();
        }

        sendProgress(progress);
    }

    private void sendProgress(int progress) {
        if (currentProgress < progress) {
            currentProgress = progress;
            if (listener != null) {
                listener.onProgress(progress);
            }
        }
    }

    private class HrefResultListener implements Listener<List<String>> {

        private int lastJokeId;
        public int resultCount = 0;

        public HrefResultListener(int lastJokeId) {
            this.lastJokeId = lastJokeId;
        }

        @Override
        public void onResponse(List<String> response) {
            resultCount++;

            if (response.size() > 0) {
                List<String> resultHref = new ArrayList<String>(response.size());

                for (String str : response) {
                    int id = HtmlParserUtils.getId(str);
                    if (id > lastJokeId) {
                        resultHref.add(str);
                    } else {
                        break;
                    }
                }

                checkJokeHref(resultHref);
            } else {
                checkJokeHref(null);
            }
        }
    }

    private class HrefErrorListener implements ErrorListener {

        public int errorTimes = 0;

        @Override
        public void onErrorResponse(VolleyError error) {
            errorTimes++;
            checkJokeHref(null);
        }
    }

    private class ContentResultListener implements Listener<JokeBean> {

        private int maxJokeId = 0;

        public ContentResultListener(int maxId) {
            maxJokeId = maxId;
        }

        @Override
        public void onResponse(JokeBean response) {

            if (TextUtils.isEmpty(response.content)) {
                jokeContentErrorNumber++;
            } else {
                jokeBeanList.add(response);

                if (maxJokeId < response.id) {
                    maxJokeId = response.id;
                }
            }

            checkEnd();
        }
    }

    private class ContentErrorListener implements ErrorListener {

        @Override
        public void onErrorResponse(VolleyError error) {
            error.printStackTrace();

            jokeContentErrorNumber++;
            checkEnd();
        }
    }

    public interface OnJokeDownloadListener {
        void onDownloadError(int lastPage, int maxJokeId);

        void onNoMoreContent();

        void onDownload(List<JokeBean> list, int lastPage, int maxJokeId);

        void onProgress(int progress);

    }
}

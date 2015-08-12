package com.cyanflxy.dapenti.htmlparser;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.util.LinkedList;
import java.util.List;

public class JokeDownloader {

    private int lastJokePage;
    private int maxJokeId;

    private RequestQueue requestQueue;

    private List<String> jokeHrefList;
    private List<JokeBean> jokeBeanList;
    private int jokeContentErrorNumber;

    private JokeHrefResultListener jokeHrefResultListener;
    private HrefDownloadErrorListener hrefDownloadErrorListener;

    private JokeContentDownloadListener jokeContentDownloadListener;

    private OnJokeDownloadListener listener;

    public JokeDownloader(Context c, OnJokeDownloadListener l) {
        listener = l;

        requestQueue = Volley.newRequestQueue(c);
    }

    public void download(int lastPage, int lastId) {

        jokeContentErrorNumber = 0;
        jokeHrefList = new LinkedList<String>();
        jokeBeanList = new LinkedList<JokeBean>();

        jokeHrefResultListener = new JokeHrefResultListener();
        hrefDownloadErrorListener = new HrefDownloadErrorListener();
        jokeContentDownloadListener = new JokeContentDownloadListener();


        lastJokePage = lastPage - 1;
        if (lastJokePage <= 0) {
            lastJokePage = 1;
        }

        maxJokeId = lastId;
        jokeHrefResultListener.setLastJokeId(lastId);

        requestQueue.start();
        downloadJokeHrefUntil(lastJokePage);

    }

    private void downloadJokeHrefUntil(int page) {
        jokeHrefResultListener.setCurrentPage(page);
        hrefDownloadErrorListener.setCurrentPage(page);

        JokeHrefRequest request = new JokeHrefRequest(page,
                jokeHrefResultListener, hrefDownloadErrorListener);

        requestQueue.add(request);
    }

    private void checkJokeHref() {
        if (jokeHrefList.size() == 0) {
            if (hrefDownloadErrorListener.getErrorTimes() > 0) {
                listener.onDownloadError();
            } else {
                listener.onNoMoreContent();
            }
            return;
        }

        for (String url : jokeHrefList) {
            downloadJoke(url, 0);
        }
    }

    private void downloadJoke(String url, int tryTimes) {
        JokeContentDownloadErrorListener errorListener =
                new JokeContentDownloadErrorListener(url, tryTimes);
        JokeContentRequest request = new JokeContentRequest(url,
                jokeContentDownloadListener, errorListener);
        requestQueue.add(request);
    }

    private void checkEnd() {
        // 检查是否下载完毕
        if (jokeBeanList.size() + jokeContentErrorNumber != jokeHrefList.size()) {
            return;
        }

        if (jokeBeanList.size() == 0) {
            listener.onDownloadError();
        } else {
            listener.onDownload(jokeBeanList, lastJokePage, maxJokeId);
        }
    }

    private class JokeHrefResultListener implements Listener<List<String>> {

        private int currentPage;
        private int lastJokeId;

        public void setCurrentPage(int page) {
            currentPage = page;
        }

        public void setLastJokeId(int jokeId) {
            lastJokeId = jokeId;
        }

        @Override
        public void onResponse(List<String> response) {

            boolean end = false;

            if (response.size() > 0) {
                for (String str : response) {
                    int id = HtmlParserUtils.getId(str);
                    if (id > lastJokeId) {
                        jokeHrefList.add(str);
                    } else {
                        end = true;
                    }
                }
            } else {
                end = true;
            }

            if (end) {
                checkJokeHref();
            } else {
                hrefDownloadErrorListener.clearTryTimes();
                downloadJokeHrefUntil(currentPage + 1);
            }
        }
    }

    private class HrefDownloadErrorListener implements ErrorListener {

        private int currentPage;
        private int tryTimes;
        private int errorTimes = 0;

        public void setCurrentPage(int page) {
            currentPage = page;
        }

        public void clearTryTimes() {
            tryTimes = 0;
        }

        public int getErrorTimes() {
            return errorTimes;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            error.printStackTrace();

            if (tryTimes < 3) {
                tryTimes++;
                downloadJokeHrefUntil(currentPage);
            } else {
                errorTimes++;
                checkJokeHref();
            }
        }
    }

    private class JokeContentDownloadListener implements Listener<JokeBean> {

        @Override
        public void onResponse(JokeBean response) {
            jokeBeanList.add(response);

            if (maxJokeId < response.id) {
                maxJokeId = response.id;
            }

            checkEnd();
        }
    }

    private class JokeContentDownloadErrorListener implements ErrorListener {

        private String jokeUrl;
        private int tryTimes;

        public JokeContentDownloadErrorListener(String url, int tryTimes) {
            jokeUrl = url;
            this.tryTimes = tryTimes;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            if (tryTimes < 3) {
                tryTimes++;
                downloadJoke(jokeUrl, tryTimes);
            } else {
                jokeContentErrorNumber++;
                checkEnd();
            }
        }
    }

    public interface OnJokeDownloadListener {
        void onDownloadError();

        void onNoMoreContent();

        void onDownload(List<JokeBean> list, int lastPage, int maxJokeId);
    }
}

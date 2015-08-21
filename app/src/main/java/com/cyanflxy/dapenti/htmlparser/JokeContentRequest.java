package com.cyanflxy.dapenti.htmlparser;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class JokeContentRequest extends Request<JokeBean> {
    private final Listener<JokeBean> mListener;

    public JokeContentRequest(String url, Listener<JokeBean> resultListener, ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        mListener = resultListener;
    }

    @Override
    protected Response<JokeBean> parseNetworkResponse(NetworkResponse response) {
        try {
            JokeBean jokeBean = getJoke(response.data);
            return Response.success(jokeBean, HttpHeaderParser.parseCacheHeaders(response));
        } catch (IOException e) {
            e.printStackTrace();
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(JokeBean response) {
        mListener.onResponse(response);
    }

    private JokeBean getJoke(byte[] data) throws IOException {
        JokeBean jokeBean = new JokeBean();
        jokeBean.id = HtmlParserUtils.getId(getUrl());

        StringConvertInputStream is = new StringConvertInputStream(new ByteArrayInputStream(data));

        try {
            String charset = HtmlParserUtils.getCharset(is);
            is.setCharset(charset);

            // 获取title
            int titleStart = is.indexOf("<title>");
            int titleEnd = is.indexOf("</title>", titleStart);
            String title = is.subString(titleStart, titleEnd);
            is.trunc(titleEnd);

            int indexLabel = title.indexOf("【段子】");
            if (indexLabel < 0) {
                indexLabel = title.indexOf("【喷嚏】");
            }
            jokeBean.title = title.substring(indexLabel + 4);

            while (true) {
                int indexStart = is.indexOf("<DIV");
                int indexEnd = is.indexOf("</DIV>", indexStart);
                if (indexStart < 0 || indexEnd < 0) {
                    break;
                }

                String contentPart = is.subString(indexStart, indexEnd);
                is.trunc(indexEnd);

                if (contentPart.contains("blog_text") && !contentPart.contains("发布于")) {
                    jokeBean.content = filterJoke(contentPart);
                    break;
                }
            }
        } finally {
            is.close();
        }

        return jokeBean;
    }


    private static String filterJoke(String content) {
        StringBuilder sb = new StringBuilder(content);

        int divStart = sb.indexOf("<div ");
        int divEnd = sb.indexOf(">", divStart);
        if (divStart >= 0 && divEnd >= 0) {
            sb.delete(divStart, divEnd + 1);
        }

        // 删除连接
        int hrefStart = sb.indexOf("<a ");
        while (hrefStart >= 0) {
            int hrefEnd = sb.indexOf("</a>", hrefStart);
            if (hrefEnd >= 0) {
                sb.delete(hrefStart, hrefEnd + 4);
            }

            hrefStart = sb.indexOf("<a ");
        }

        deleteStringBuilder(sb, "<a>");
        deleteStringBuilder(sb, "</a>");
        deleteStringBuilder(sb, "</div>");
        deleteStringBuilder(sb, "<p>");
        deleteStringBuilder(sb, "<span>");
        deleteStringBuilder(sb, "</span>");
        deleteStringBuilder(sb, "&nbsp;");

        deleteTAGWithOutChinese(sb, "span");

        replaceStringBuilder(sb, "<br>", "\n");
        replaceStringBuilder(sb, "</p>", "\n");
        replaceStringBuilder(sb, "\r", "\n");
        replaceStringBuilder(sb, "\n\n", "\n");

        int sourceIndex = sb.indexOf("来源：");
        if (sourceIndex > 0) {
            sb.delete(sourceIndex, sb.length());
        }

        String filterString = ":：\n \t";
        deleteHeaderChar(sb, filterString);
        deleteEndChar(sb, filterString);

        return sb.toString();
    }

    /**
     * tag里面没有中文的时候删除tag内容
     */
    private static void deleteTAGWithOutChinese(StringBuilder sb, String tag) {
        int indexStart = sb.indexOf("<" + tag + " ");
        if (indexStart < 0) {
            indexStart = sb.indexOf("<" + tag + ">");
            if (indexStart < 0) {
                return;
            }
        }

        int end = sb.indexOf("</" + tag + ">");
        if (end < 0) {
            end = sb.length();
        } else {
            end += tag.length() + 3;
        }

        for (int i = indexStart; i < end; i++) {
            char c = sb.charAt(i);
            if (c > 128) {
                return;
            }
        }

        sb.delete(indexStart, end);
    }

    private static void deleteStringBuilder(StringBuilder sb, String str) {
        int index;
        while (true) {
            index = sb.indexOf(str);
            if (index >= 0) {
                sb.delete(index, index + str.length());
            } else {
                break;
            }
        }
    }

    private static void replaceStringBuilder(StringBuilder sb, String src, String dst) {
        int index;
        while (true) {
            index = sb.indexOf(src);
            if (index >= 0) {
                sb.delete(index, index + src.length());
                sb.insert(index, dst);
            } else {
                break;
            }
        }
    }

    public static void deleteHeaderChar(StringBuilder sb, String removeChars) {

        while (true) {
            char c = sb.charAt(0);
            if (removeChars.contains(String.valueOf(c))) {
                sb.deleteCharAt(0);
            } else {
                break;
            }
        }

    }

    private static void deleteEndChar(StringBuilder sb, String removeChars) {
        while (true) {
            char c = sb.charAt(sb.length() - 1);
            if (removeChars.contains(String.valueOf(c))) {
                sb.deleteCharAt(sb.length() - 1);
            } else {
                break;
            }
        }
    }
}

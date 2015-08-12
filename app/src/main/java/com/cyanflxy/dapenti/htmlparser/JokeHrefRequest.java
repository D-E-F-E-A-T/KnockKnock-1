package com.cyanflxy.dapenti.htmlparser;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

public class JokeHrefRequest extends Request<List<String>> {

    public static final String JOKE_URL = "http://www.dapenti.com/blog/more.asp?name=xilei&id=";

    private final Listener<List<String>> mListener;

    public JokeHrefRequest(int pageNum, Listener<List<String>> responseListener, ErrorListener errorListener) {
        super(Method.GET, HtmlParserUtils.BASE_URL + pageNum, errorListener);
        mListener = responseListener;
    }

    @Override
    protected Response<List<String>> parseNetworkResponse(NetworkResponse response) {

        List<String> hrefStrings;
        try {
            hrefStrings = getAllHref(response.data);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error(new ParseError(e));
        }

        List<String> result = new LinkedList<String>();
        for (String href : hrefStrings) {
            if (href.contains("【段子】") || href.contains("【喷嚏】")) {
                String url = HtmlParserUtils.getArg(href, "href", HtmlParserUtils.HREF_SEPARATOR);
                result.add(HtmlParserUtils.getRelativeUrl(getUrl(), url));
            } else if (href.contains("首页")) {
                break;
            }
        }

        return Response.success(result, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(List<String> response) {
        mListener.onResponse(response);
    }

    private static List<String> getAllHref(byte[] data) throws IOException, ParserConfigurationException, SAXException {

        StringConvertInputStream is = new StringConvertInputStream(new ByteArrayInputStream(data));
        List<String> href = new LinkedList<String>();

        try {
            String charset = HtmlParserUtils.getCharset(is);
            is.setCharset(charset);

            String hrefStart = "<a ";
            String hrefEnd = "</a>";

            int indexStart = is.indexOf(hrefStart);
            int indexEnd = is.indexOf(hrefEnd, indexStart);

            while (indexStart >= 0 && indexEnd >= 0) {
                String hrefString = is.subString(indexStart, indexEnd + hrefEnd.length());
                href.add(hrefString);

                is.trunc(indexEnd + hrefEnd.length());

                indexStart = is.indexOf(hrefStart);
                indexEnd = is.indexOf(hrefEnd, indexStart);
            }
        } finally {
            is.close();
        }

        return href;
    }
}

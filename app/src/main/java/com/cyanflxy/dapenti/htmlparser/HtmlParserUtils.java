package com.cyanflxy.dapenti.htmlparser;

import java.io.IOException;

public class HtmlParserUtils {

    public static final String BASE_URL = "http://www.dapenti.com/blog/blog.asp?name=xilei&subjectid=137&page=";

    public static final String HREF_SEPARATOR = " \'\"/><";
    public static final String ARG_SEPARATOR = HREF_SEPARATOR + "&";

    public static String getCharset(StringConvertInputStream is) throws IOException {
        int indexStart = is.indexOf("charset=");
        int indexEnd = is.indexOf(">", indexStart);
        String sub = is.subString(indexStart, indexEnd + 1);
        is.trunc(indexEnd + 1);

        return getArg(sub, "charset", ARG_SEPARATOR);
    }

    public static int getId(String url) {
        String idStr = HtmlParserUtils.getArg(url, "id", HtmlParserUtils.ARG_SEPARATOR);
        if (idStr != null) {
            try {
                return Integer.valueOf(idStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public static String getArg(String src, String key, String separator) {

        key = key + "=";

        int keyStart = src.indexOf(key);
        if (keyStart < 0) {
            return null;
        }

        keyStart += key.length();

        char startChar = src.charAt(keyStart);

        if (startChar == '\'' || startChar == '\"') {

            int lastChar = src.indexOf(startChar, keyStart + 1);
            if (lastChar > 0) {
                return src.substring(keyStart + 1, lastChar);
            } else {
                keyStart++;
            }

        }

        StringBuilder sb = new StringBuilder();
        for (int i = keyStart; i < src.length(); i++) {
            char c = src.charAt(i);
            if (separator.indexOf(c) < 0) {
                sb.append(c);
            } else {
                break;
            }
        }
        return sb.toString();

    }

    public static String getRelativeUrl(String baseUrl, String relativeUrl) {
        if (relativeUrl.startsWith("http://")) {
            return relativeUrl;
        } else {
            int baseLen = baseUrl.lastIndexOf('/');
            String base = baseUrl.substring(0, baseLen + 1);
            return base + relativeUrl;
        }
    }

}

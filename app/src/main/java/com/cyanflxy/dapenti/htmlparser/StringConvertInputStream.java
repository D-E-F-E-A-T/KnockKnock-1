package com.cyanflxy.dapenti.htmlparser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 将InputStream实时转换成String供使用者解析
 * <p/>
 * 字符串比较忽略大小写。
 */
public class StringConvertInputStream {

    private InputStream inputStream;
    private String charset;

    private int currentIndex;
    private StringBuilder currentString;

    private byte[] buffer;

    public StringConvertInputStream(InputStream is) {
        inputStream = is;
        currentIndex = 0;
        currentString = new StringBuilder(512);
        buffer = new byte[128];
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public int indexOf(String sub) throws IOException {
        return indexOf(sub, currentIndex);
    }

    public int indexOf(String sub, int indexStart) throws IOException {

        sub = sub.toLowerCase();

        int start = 0;
        int index = currentString.indexOf(sub, start + indexStart - currentIndex);

        while (index < 0) {
            if (inputStream.available() <= 0) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    //ignore
                }
            }

            start = currentString.length();

            if (appendNext()) {
                index = currentString.indexOf(sub, start - sub.length());
            } else {
                break;
            }
        }

        if (index > 0) {
            return index + currentIndex;
        } else {
            return index;
        }
    }

    public String subString(int start, int end) {
        try {
            return currentString.substring(start - currentIndex, end - currentIndex);
        } catch (StringIndexOutOfBoundsException e) {
            System.out.println(currentString);
            System.out.println("start=" + start + "; end=" + end + "; currentIndex:" + currentIndex);
            throw e;
        }
    }

    public void trunc(int index) {
        currentString.delete(0, index - currentIndex);
        currentIndex = index;
    }

    public void close() throws IOException {
        inputStream.close();
        currentString.delete(0, currentString.length());
    }

    private boolean appendNext() throws IOException {
        int len = inputStream.read(buffer);
        if (len == -1) {
            return false;
        }

        // buffer边界不能赶上 ascii<128 的字符
        byte[] bufferNew = buffer;
        if (buffer[len - 1] < 0) {
            ByteArrayOutputStream bis = new ByteArrayOutputStream();
            bis.write(buffer, 0, len);

            while (buffer[len - 1] < 0) {
                len = inputStream.read(buffer);
                bis.write(buffer, 0, len);
            }

            bufferNew = bis.toByteArray();
            len = bufferNew.length;
        }

        String result;
        if (charset != null) {
            result = new String(bufferNew, 0, len, charset);
        } else {
            result = new String(bufferNew, 0, len);
        }

        currentString.append(result.toLowerCase());
        return true;
    }
}

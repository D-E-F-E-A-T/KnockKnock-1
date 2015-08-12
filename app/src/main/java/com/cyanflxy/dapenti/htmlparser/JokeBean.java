package com.cyanflxy.dapenti.htmlparser;

import java.io.Serializable;

public class JokeBean implements Serializable {

    private static final long serialVersionUID = 1L;

    public int id;
    public String title;
    public String content;

    public JokeBean() {

    }

    public JokeBean(int id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    @Override
    public String toString() {
        return "" + id + "\n" + title + "\n" + content;
    }
}

package com.github.cyanflxy.knockknock.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cyanflxy.dapenti.htmlparser.JokeBean;
import com.github.cyanflxy.knockknock.AppApplication;

public class JokeDataBase {
    public static JokeDataBase getInstance() {
        return DBHolder.INSTANCE;
    }

    private static class DBHolder {
        private static final JokeDataBase INSTANCE = new JokeDataBase();
    }

    public static final String DATABASE_NAME = "joke.db";
    private static final String TABLE_DPT = "joke_x1";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";

    public static final int COLUMN_ID_INDEX = 1;
    public static final int COLUMN_TITLE_INDEX = 2;
    public static final int COLUMN_CONTENT_INDEX = 3;

    private SQLiteDatabase mdb;

    private JokeDataBase() {
        prepare();
    }

    private void prepare() {
        mdb = AppApplication.baseContext.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);

        if (mdb.getVersion() < 1) {
            mdb.execSQL("create table if not exists "
                    + TABLE_DPT + " ("
                    + "_id integer  primary key autoincrement,"// 这一行是为了使用CursorAdapter必须加的
                    + COLUMN_ID + " integer,"
                    + COLUMN_TITLE + " varchar,"
                    + COLUMN_CONTENT + " varchar"
                    + ");");
            mdb.setVersion(1);
        }
    }

    public void insertJoke(JokeBean bean) {
        mdb.execSQL("insert into " + TABLE_DPT
                + " ("
                + COLUMN_ID + ", "
                + COLUMN_TITLE + ", "
                + COLUMN_CONTENT +
                ") values ('"
                + bean.id + "','"
                + bean.title + "','"
                + bean.content + "');");
    }

    public Cursor query() {
        return mdb.rawQuery("select * from " + TABLE_DPT+" order by id desc", null);
    }

    public void delete(int id) {
        mdb.execSQL("delete from " + TABLE_DPT + " where " + COLUMN_ID + "='" + id + "'");
    }
}

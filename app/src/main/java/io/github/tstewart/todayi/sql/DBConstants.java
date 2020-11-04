package io.github.tstewart.todayi.sql;

import java.text.SimpleDateFormat;

public class DBConstants {

    public static final String DB_NAME = "accomplishment_db";
    public static final int DB_VERSION = 1;

    public static final String ACCOMPLISHMENT_TABLE = "accomplishments";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_CONTENT = "content";

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static final String ACCOMPLISHMENT_QUERY = "select * from " + ACCOMPLISHMENT_TABLE + " where date = ?";


    private DBConstants(){}
}

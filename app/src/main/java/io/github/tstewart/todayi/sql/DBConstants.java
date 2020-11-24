package io.github.tstewart.todayi.sql;

public class DBConstants {

    public static final String DB_NAME = "todayi_db";
    public static final int DB_VERSION = 1;

    public static final String ACCOMPLISHMENT_TABLE = "accomplishments";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_CONTENT = "content";

    public static final String RATING_TABLE = "dayratings";
    public static final String COLUMN_RATING = "rating";

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static final String ACCOMPLISHMENT_QUERY = "select * from " + ACCOMPLISHMENT_TABLE + " where date = ?";
    public static final String ACCOMPLISHMENT_DATE_GROUP_QUERY = "select " + COLUMN_DATE + "  from " + ACCOMPLISHMENT_TABLE + " group by " + COLUMN_DATE;
    public static final String DAY_RATING_QUERY = "select " + COLUMN_RATING + " from " + RATING_TABLE + " where date = ?";
    public static final String DAY_RATING_ALL_RESULTS_QUERY = "select * from " + RATING_TABLE;

    private DBConstants() {
    }

}

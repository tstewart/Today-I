package io.github.tstewart.todayi.data;

/**
 * Constants used by the database and helper functions
 */
public class DBConstants {

    /* Meta table data */
    public static final String DB_NAME = "todayi_db";
    public static final int DB_VERSION = 7;

    /* Table names */
    public static final String ACCOMPLISHMENT_TABLE = "accomplishments";
    public static final String RATING_TABLE = "dayratings";

    /* Column names */
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_RATING = "rating";

    /*
     Default date format, all dates should be formatted to this to be accepted
    */
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";
    public static final String DATE_FORMAT_NO_TIME = "yyyy-MM-dd";
    /* Time format, used when date is not required to be shown */
    public static final String TIME_FORMAT = "HH:mm";


    /* Helper queries to fetch data */
    public static final String ACCOMPLISHMENT_QUERY = "select * from " + ACCOMPLISHMENT_TABLE + " where " + COLUMN_DATE + " like ? order by " + COLUMN_DATE;
    public static final String ACCOMPLISHMENT_DATE_GROUP_QUERY = "select " + COLUMN_DATE + "  from " + ACCOMPLISHMENT_TABLE + " group by " + COLUMN_DATE;
    public static final String DAY_RATING_QUERY = "select " + COLUMN_RATING + " from " + RATING_TABLE + " where " + COLUMN_DATE + " = ?";

    /* Private constructor prevents initialisation of helper class */
    private DBConstants() {
    }

}

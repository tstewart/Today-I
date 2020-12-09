package io.github.tstewart.todayi.data;

/**
 * Constants used by the database and helper functions
 */
public class DBConstants {

    /* Meta table data */
    public static final String DB_NAME = "todayi_db";
    public static final int DB_VERSION = 2;

    /* Table names */
    public static final String ACCOMPLISHMENT_TABLE = "accomplishments";
    public static final String RATING_TABLE = "dayratings";

    /* Column names */
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_RATING = "rating";

    /*
     Default date format, all dates should be formatted to this to be accepted
     This is used because dates are represented in SQLite as a millisecond-date, which makes querying specific dates difficult
    */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /* Time posted format, used when fetching Accomplishments to identify at what time they were posted */
    public static final String TIME_FORMAT = "HH:mm";

    /* Helper queries to fetch data */
    public static final String ACCOMPLISHMENT_QUERY = "select * from " + ACCOMPLISHMENT_TABLE + " where date = ?";
    public static final String ACCOMPLISHMENT_DATE_GROUP_QUERY = "select " + COLUMN_DATE + "  from " + ACCOMPLISHMENT_TABLE + " group by " + COLUMN_DATE;
    public static final String DAY_RATING_QUERY = "select " + COLUMN_RATING + " from " + RATING_TABLE + " where date = ?";
    public static final String DAY_RATING_ALL_RESULTS_QUERY = "select * from " + RATING_TABLE;

    /* Private constructor prevents initialisation of helper class */
    private DBConstants() {
    }

}

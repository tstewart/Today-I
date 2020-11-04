package io.github.tstewart.todayi;

import androidx.appcompat.app.AppCompatActivity;
import io.github.tstewart.todayi.sql.Database;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    SQLiteDatabase accomplishmentDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Database db = new Database(getApplicationContext());
        accomplishmentDb = db.getWritableDatabase();
    }
}

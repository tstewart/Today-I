package io.github.tstewart.todayi.ui.fragment;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.event.OnDateChanged;
import io.github.tstewart.todayi.event.OnDateChangedListener;
import io.github.tstewart.todayi.object.DayRating;
import io.github.tstewart.todayi.sql.DBConstants;
import io.github.tstewart.todayi.sql.Database;
import io.github.tstewart.todayi.sql.DatabaseHelper;
import io.github.tstewart.todayi.sql.DayRatingTableHelper;

public class DayRatingFragment extends Fragment implements OnDateChangedListener {

    Button[] buttons;
    int colors[] = new int[]{R.color.colorRatingRed, R.color.colorRatingOrange, R.color.colorRatingYellow, R.color.colorRatingLightGreen, R.color.colorRatingGreen};

    Date selectedDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_day_rater, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        buttons = new Button[5];
        LinearLayout ll = view.findViewById(R.id.linearLayoutDayRating);

        for (int i = 0; i < 5; i++) {

            buttons[i] = new Button(new ContextThemeWrapper(getContext(), R.style.AppTheme_DayRatingButton), null, R.style.Widget_AppCompat_Button_Borderless);
            buttons[i].setText(String.valueOf(i+1));
            buttons[i].setOnClickListener(this::onRatingButtonClicked);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);

            ll.addView(buttons[i], layoutParams);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        OnDateChanged.addListener(this);
    }

    private void onRatingButtonClicked(View v) {
        Context context = getContext();

        if(buttons != null && context != null) {
            List<Button> buttonsList = Arrays.asList(buttons);

            buttonsList.forEach(this::resetButtonBackground);

            if (v instanceof Button) {
                Button buttonClicked = (Button) v;

                int index = Arrays.asList(buttons).indexOf(buttonClicked);

                if (index >= 0 && index < colors.length) {
                    int color = colors[index];
                    setButtonBackground(buttonClicked, color);

                    setDayRating(index+1);
                }
            }
        }
    }

    private void setDayRating(int index) {
        DatabaseHelper helper = new DatabaseHelper(DBConstants.RATING_TABLE);

        if(selectedDate != null && getContext() != null) {
            DayRating dayRating = new DayRating(selectedDate, index);
            //TODO validate

            SQLiteDatabase db = helper.getDatabase(getContext());
            String dateFormatted = new SimpleDateFormat(DBConstants.DATE_FORMAT, Locale.getDefault()).format(selectedDate);
            Cursor existingRowCheck = db.rawQuery(DBConstants.DAY_RATING_QUERY, new String[]{dateFormatted});

            if(existingRowCheck.moveToFirst()) {
                helper.update(getContext(),dayRating, DBConstants.COLUMN_DATE + "=?", new String[]{dateFormatted});
            }
            else {
                helper.insert(getContext(), dayRating);
            }

            existingRowCheck.close();
        }
    }

    private void resetButtonBackground(Button button) {
        setButtonBackground(button, R.color.colorTransparent);
    }

    private void setButtonBackground(Button button, int color) {
        if(getContext() != null && button != null) {
            GradientDrawable drawable = (GradientDrawable) button.getBackground();
            drawable.setColor(ContextCompat.getColor(getContext(), color));
        }
    }

    @Override
    public void onDateChanged(Date date) {
        if(getContext() != null) {
            SQLiteDatabase db = new Database(getContext()).getReadableDatabase();
            int index = new DayRatingTableHelper(getContext()).getRatingOrDefault(date);
        }

        this.selectedDate = date;
    }
}

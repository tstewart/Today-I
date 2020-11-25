package io.github.tstewart.todayi.ui.fragment;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.event.OnDateChanged;
import io.github.tstewart.todayi.event.OnDateChangedListener;
import io.github.tstewart.todayi.object.DayRating;
import io.github.tstewart.todayi.sql.DBConstants;
import io.github.tstewart.todayi.sql.DatabaseHelper;
import io.github.tstewart.todayi.sql.DayRatingTableHelper;

public class DayRatingFragment extends Fragment implements OnDateChangedListener {

    Button[] buttons;
    final int[] colors = new int[]{R.color.colorRatingRed, R.color.colorRatingOrange, R.color.colorRatingYellow, R.color.colorRatingLightGreen, R.color.colorRatingGreen};

    Date selectedDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_day_rater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
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

        int index = getIndexOfRating(new Date());

        if(index >= 0) setSelectedButton(index);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        OnDateChanged.addListener(this);
    }

    private void onRatingButtonClicked(View v) {
        Context context = getContext();

        if(buttons != null && context != null) {

            resetAllButtonBackgrounds();

            if (v instanceof Button) {
                Button buttonClicked = (Button) v;

                int index = Arrays.asList(buttons).indexOf(buttonClicked);

                if (index >= 0 && index < buttons.length) {
                    int color = colors[index];
                    setButtonBackground(buttonClicked, color);

                    setSelectedButton(index);
                    updateDayRatingOnDB(index+1);
                }
            }
        }
    }

    private void setSelectedButton(int index) {
        resetAllButtonBackgrounds();

        if(index < colors.length) {

            Button button = buttons[index];
            int color = colors[index];

            setButtonBackground(button,color);
        }
    }

    private void updateDayRatingOnDB(int index) {
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

    private void resetAllButtonBackgrounds() {
        if(buttons != null) Arrays.asList(buttons).forEach(this::resetButtonBackground);
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

    private int getIndexOfRating(Date date) {
        Context context = getContext();

        if(context != null) {
            int index = new DayRatingTableHelper(context).getRatingOrDefault(date);
            return index-1;
        }

        return -1;
    }

    @Override
    public void onDateChanged(Date date) {
        int index = getIndexOfRating(date);

        if(index >= 0) setSelectedButton(index);
        else resetAllButtonBackgrounds();

        this.selectedDate = date;
    }
}

package io.github.tstewart.todayi.ui.fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.Arrays;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import io.github.tstewart.todayi.R;

public class DayRatingFragment extends Fragment {

    private GradientDrawable buttonDrawable;
    Button[] buttons;
    int colors[] = new int[]{R.color.colorRatingRed, R.color.colorRatingOrange, R.color.colorRatingYellow, R.color.colorRatingLightGreen, R.color.colorRatingGreen};


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
                }
            }
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
}

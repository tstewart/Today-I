package io.github.tstewart.todayi.ui.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.github.tstewart.todayi.R;

public class DayRatingFragment extends Fragment {

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

            buttons[i] = new Button(new ContextThemeWrapper(getContext(), R.style.Widget_AppCompat_Button_Borderless), null, R.style.Widget_AppCompat_Button_Borderless);
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
                    buttonClicked.setBackgroundColor(ContextCompat.getColor(context, color));
                }
            }
        }
    }

    private void resetButtonBackground(Button button) {
        if(button != null) button.setBackgroundColor(0x00000000);
    }
}

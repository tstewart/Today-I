package io.github.tstewart.todayi.ui.tutorials;

import android.graphics.Color;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.ui.activities.MainActivity;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;
import uk.co.deanwild.materialshowcaseview.shape.NoShape;

public class MainActivityTutorial {

    /**
     * Show tutorial for a provided MainActivity instance
     * @param tutorialActivity MainActivity instance to show tutorial on
     */
    public void showTutorial(MainActivity tutorialActivity) {

        try {
            MaterialShowcaseSequence sequence = getTutorialSequence(tutorialActivity);

            sequence.start();
        }
        /* If a view was not found and the tutorial could not be shown, don't show it. */
        catch(NullPointerException ignore) {}
    }

    public MaterialShowcaseSequence getTutorialSequence(MainActivity tutorialActivity) {

        /* Get Views focused on in the tutorial */
        View newAccomplishmentButton = tutorialActivity.findViewById(R.id.buttonNewAccomplishment);
        View dayLayout = tutorialActivity.findViewById(R.id.linearLayoutDayButtons);
        View previousDayButton = tutorialActivity.findViewById(R.id.buttonPrevDay);
        View nextDayButton = tutorialActivity.findViewById(R.id.buttonNextDay);
        View accomplishmentListFragment = tutorialActivity.findViewById(R.id.listFragment);
        View dayRatingFragment = tutorialActivity.findViewById(R.id.fragment);
        View calendarButton = tutorialActivity.findViewById(R.id.toolbar_calendar);

        /* The first view is initialised without a focus shape, so the overlay takes up the whole screen */
        MaterialShowcaseView startOverlayView = new MaterialShowcaseView.Builder(tutorialActivity)
                .setTarget(newAccomplishmentButton)
                .setShape(new NoShape())
                .build();


        MaterialShowcaseSequence showcase = new MaterialShowcaseSequence(tutorialActivity);

        showcase.addSequenceItem(startOverlayView, "Welcome to Today I, a mini application to track your accomplishments for the day, as well rate your overall feeling of each day.", "Next");
        showcase.addSequenceItem(newAccomplishmentButton, "To add new Accomplishments, select the New button.", "Next");
        showcase.addSequenceItem(dayLayout, "The current selected day is shown here.", "Next");
        showcase.addSequenceItem(previousDayButton, "You can change to the previous day by selecting this button.", "Next");
        showcase.addSequenceItem(nextDayButton, "You can change to the next day by selecting this button.", "Next");
        showcase.addSequenceItem(accomplishmentListFragment, "You can also swipe left and right to change days (If this is enabled in Settings.)", "Next");
        showcase.addSequenceItem(dayRatingFragment, "You can rate your day on a scale from 1-5 by selecting a rating.", "Next");
        showcase.addSequenceItem(calendarButton, "You can view past day ratings and quickly find Accomplishments from previous days in the calendar.", "Next");

        return showcase;
    }
}

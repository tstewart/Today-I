package io.github.tstewart.todayi.ui.tutorials;

import android.util.Log;
import android.view.View;

import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.ui.activities.MainActivity;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.shape.NoShape;

/*
User interactive tutorial of the features of MainActivity
 */
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
        catch(NullPointerException e) {
            Log.w(this.getClass().getSimpleName(), "Failed to show tutorial. One or more views were not instantiated.", e);
        }
    }

    public MaterialShowcaseSequence getTutorialSequence(MainActivity tutorialActivity) {

        /* Get Views focused on in the tutorial */
        View dayLayout = tutorialActivity.findViewById(R.id.linearLayoutDayButtons);
        View previousDayButton = tutorialActivity.findViewById(R.id.buttonPrevDay);
        View nextDayButton = tutorialActivity.findViewById(R.id.buttonNextDay);
        View accomplishmentListFragment = tutorialActivity.findViewById(R.id.listFragment);
        View dayRatingFragment = tutorialActivity.findViewById(R.id.fragment);
        View calendarButton = tutorialActivity.findViewById(R.id.toolbar_calendar);

        /* The first view is initialised without a focus shape, so the overlay takes up the whole screen */
        MaterialShowcaseView startOverlayView = new MaterialShowcaseView.Builder(tutorialActivity)
                .setTarget(dayLayout)
                .setShape(new NoShape())
                .build();


        MaterialShowcaseSequence showcase = new MaterialShowcaseSequence(tutorialActivity);

        String next = tutorialActivity.getString(R.string.tutorial_next);

        showcase.addSequenceItem(startOverlayView, tutorialActivity.getString(R.string.tutorial_welcome), next);
        showcase.addSequenceItem(accomplishmentListFragment, tutorialActivity.getString(R.string.tutorial_new_accomplishment), next);
        showcase.addSequenceItem(dayLayout, tutorialActivity.getString(R.string.tutorial_current_day), next);
        showcase.addSequenceItem(previousDayButton, tutorialActivity.getString(R.string.tutorial_previous_day), next);
        showcase.addSequenceItem(nextDayButton, tutorialActivity.getString(R.string.tutorial_next_day), next);
        showcase.addSequenceItem(accomplishmentListFragment, tutorialActivity.getString(R.string.tutorial_gesture), next);
        showcase.addSequenceItem(dayRatingFragment, tutorialActivity.getString(R.string.tutorial_day_rating), next);
        showcase.addSequenceItem(calendarButton, tutorialActivity.getString(R.string.tutorial_calendar), tutorialActivity.getString(R.string.tutorial_start));

        return showcase;
    }
}

package io.github.tstewart.todayi.helpers;

import android.graphics.Color;
import android.util.Log;

import java.text.DecimalFormat;

import androidx.core.graphics.ColorUtils;

/*
Helper class. Generates an array of colors blending two colors on a sliding scale
Example case:

User requests 5 colors that represent a scale from red to green
Results in an array of colors approximating [red, orange, yellow, light green, green]
 */
public class ColorBlendHelper {

    int[] mColors;
    int mNumColorsGenerated;

    int mColorStart;
    int mColorEnd;

    /**
     * @param numColorsGenerated Number of colors to generate
     * @param colorStart First color in scale
     * @param colorEnd Second color in scale
     */
    public ColorBlendHelper(int numColorsGenerated, int colorStart, int colorEnd) {
        this.mNumColorsGenerated = numColorsGenerated;
        this.mColorStart = colorStart;
        this.mColorEnd = colorEnd;
    }

    public int[] generateColors() throws IllegalArgumentException {

        if(mNumColorsGenerated < 0)
            throw new IllegalArgumentException("Cannot generate an array of negative value.");
        else if(mNumColorsGenerated == 0)
            return new int[]{};

        mColors = new int[mNumColorsGenerated];

        /* The percentage blend each color in the array takes up
        * E.g. an array of 5 colors each take 20% of the blend  */
        float percentageBlendPerColor = 1f / mNumColorsGenerated;
        /* Current position in the blend we are at. */
        float currentBlend = 0f;

        for (int i = 0; i < mNumColorsGenerated; i++) {
            /* Create a blended color with the current percentage blend of both provided colors */
            mColors[i] = ColorUtils.blendARGB(mColorStart,mColorEnd, currentBlend);
            /* Increase the blend */
            currentBlend += percentageBlendPerColor;
        }

        return mColors;
    }

    public int[] getColors() {
        return mColors;
    }
}

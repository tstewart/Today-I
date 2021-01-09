package io.github.tstewart.todayi.helpers;

import android.graphics.Color;

/*
Helper class. Generates an array of colors blending on a sliding scale
Example case:

User requests 5 colors that represent a scale from red to green
Results in an array of colors approximating [red, orange, yellow, light green, green]
 */
public class ColorBlendHelper {

    /* Maximum Hue value to reach */
    /* Of a maximum of 360 */
    private static final float HSV_HUE = 126;
    /* Saturation of the color (HSV) */
    private static final float HSV_SATURATION = 1;
    /* Brightness of the color (HSV) */
    private static final float HSV_BRIGHTNESS = 1;
    /* Alpha of the color */
    /* Of a maximum of 255 */
    private static final int ALPHA = 200;

    int[] mColors;
    int mNumColorsGenerated;

    /**
     * @param numColorsGenerated Number of colors to generate
     */
    public ColorBlendHelper(int numColorsGenerated) {
        this.mNumColorsGenerated = numColorsGenerated;
    }

    public int[] blendColors() {

        if(mNumColorsGenerated <= 0)
            return new int[]{};

        mColors = new int[mNumColorsGenerated];

        /* The percentage of the total Hue that each color takes up */
        float percentageBlendPerColor = HSV_HUE / mNumColorsGenerated;
        /* Current position in the blend we are at. */
        float currentBlend = 0f;

        float[] hsvValues = {currentBlend, HSV_SATURATION, HSV_BRIGHTNESS};

        for (int i = 0; i < mNumColorsGenerated; i++) {
            /* Create a blended color with the current percentage blend of both provided colors */
            mColors[i] = Color.HSVToColor(ALPHA, hsvValues);
            /* Increase the blend */
            hsvValues[0] += percentageBlendPerColor;
        }

        return mColors;
    }
}

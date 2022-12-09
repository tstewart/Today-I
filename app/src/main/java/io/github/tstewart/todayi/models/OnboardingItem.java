package io.github.tstewart.todayi.models;

public class OnboardingItem {

    private final int mPageTitleRes;
    private final int mImageRes;
    private final int mPageBodyRes;

    public OnboardingItem(int pageTitleRes, int pageBodyRes, int imageRes) {
        this.mPageTitleRes = pageTitleRes;
        this.mImageRes = imageRes;
        this.mPageBodyRes = pageBodyRes;
    }

    public int getPageTitleRes() {
        return mPageTitleRes;
    }

    public int getImageRes() {
        return mImageRes;
    }

    public int getPageBodyRes() {
        return mPageBodyRes;
    }
}

package io.github.tstewart.todayi.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.models.OnboardingItem;

public class OnboardingItemAdapter extends RecyclerView.Adapter<OnboardingItemAdapter.OnboardingViewHolder>{

    Context mContext;
    ArrayList<OnboardingItem> mItemList;

    public OnboardingItemAdapter(Context mContext, ArrayList<OnboardingItem> mItemList) {
        this.mContext = mContext;
        this.mItemList = mItemList;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_onboarding_item, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        OnboardingItem onboardingItem = mItemList.get(position);
        if(onboardingItem != null) {
            holder.mTitle.setText(onboardingItem.getPageTitleRes());
            holder.mBody.setText(onboardingItem.getPageBodyRes());
            holder.mImage.setImageResource(onboardingItem.getImageRes());
        }
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    protected class OnboardingViewHolder extends RecyclerView.ViewHolder {

        TextView mTitle;
        TextView mBody;
        ImageView mImage;

        public OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.textViewOnboardingPageTitle);
            mBody = itemView.findViewById(R.id.textViewOnboardingPageBody);
            mImage = itemView.findViewById(R.id.imageViewOnboardingPageImage);
        }
    }
}

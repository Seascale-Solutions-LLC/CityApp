/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * NOTICE: Following code has been modified from its original for the express purpose of CityApp
 * usage.
 */

package com.seascale.cityapp.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.seascale.cityapp.R;
import com.seascale.cityapp.data.UserPreferences;
import com.seascale.cityapp.models.Business;

/**
 * RecyclerView adapter for business overview in the MainActivity
 */
public class BusinessAdapter extends FirestoreAdapter<BusinessAdapter.ViewHolder> {

    private static final String LOG_TAG = BusinessAdapter.class.getSimpleName();

    /**
     * Listener interface - trigger on user selection of item from the RecyclerView
     */
    public interface OnBusinessSelectedListener {
        void onBusinessSelected(View view, DocumentSnapshot business);
    }

    private OnBusinessSelectedListener mListener;


    /**
     * Listener interface - trigger when first item is available from the Firestore query (only used
     * for two-pane mode)
     */
    public interface OnLayoutReadyListener {
        void onLayoutReady(DocumentSnapshot snapshot);
    }

    private OnLayoutReadyListener mOnLayoutReadyListener;


    public BusinessAdapter(Query query, OnBusinessSelectedListener listener,
                           OnLayoutReadyListener layoutReadyListener) {
        super(query);
        mListener = listener;
        mOnLayoutReadyListener = layoutReadyListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
        if (position == 0) {
            // Query results have been returned from Firestore. If we are in two-pane mode, notify
            // the MainActivity so the right pane (with the BusinessActivity information) can be
            // populated and displayed.
            mOnLayoutReadyListener.onLayoutReady(getSnapshot(position));
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView businessImage;
        TextView businessName;
        TextView businessDescription;
        FrameLayout favoriteIndicator;
        FrameLayout campaignIndicator;

        ViewHolder(View itemView) {
            super(itemView);
            businessImage = itemView.findViewById(R.id.business_image);
            businessName = itemView.findViewById(R.id.list_item_business_name);
            businessDescription =  itemView.findViewById(R.id.list_item_business_description);
            favoriteIndicator = itemView.findViewById(R.id.favorite_icon);
            campaignIndicator = itemView.findViewById(R.id.campaign_icon);
        }

        void bind(final DocumentSnapshot snapshot, final OnBusinessSelectedListener listener) {
            Business business = snapshot.toObject(Business.class);

            // Load business related image
            Glide.with(businessImage.getContext()).load(business.getImageId()).into(businessImage);

            try {
                businessName.setText(business.getName());
            } catch (NullPointerException e) {
                Log.e(LOG_TAG, "[ERROR] bind - business.getName() yielded NullPointerException");
            }
            businessDescription.setText(business.getDescription());

            // Set favorite and/or campaign icons as appropriate
            if (business.getCampaign()) {
                campaignIndicator.setVisibility(View.VISIBLE);
            } else {
                campaignIndicator.setVisibility(View.INVISIBLE);
            }
            if (UserPreferences.isFavorite(itemView.getContext(), business.getId())) {
                favoriteIndicator.setVisibility(View.VISIBLE);
            } else {
                favoriteIndicator.setVisibility(View.INVISIBLE);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onBusinessSelected(businessImage, snapshot);
                    }
                }
            });
        }
    }
}

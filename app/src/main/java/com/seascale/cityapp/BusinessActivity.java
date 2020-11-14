/**********************************************************************************************
 * Copyright Seascale Consulting LLC. All Rights Reserved.                                    *
 *                                                                                            *
 * NOTICE: This source code is protected by an End-User License Agreement (the "License").    *
 * You may not use this file or its executable version except in compliance with the License. *
 * Unless required by applicable law or agreed to in writing, software distributed under the  *
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,  *
 * either express or implied. See the License for the specific language governing permissions *
 * and limitations under the License.                                                         *
 **********************************************************************************************
 */

package com.seascale.cityapp;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.seascale.cityapp.data.UserPreferences;
import com.seascale.cityapp.models.Business;
import com.seascale.cityapp.models.Filters;
import com.seascale.cityapp.utils.CommonUtils;

import static com.seascale.cityapp.BusinessFragment.KEY_BUSINESS_PARCELABLE;
import static com.seascale.cityapp.MainActivity.FILTER_STATE;

public class BusinessActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String LOG_TAG = BusinessActivity.class.getSimpleName();

    static String KEY_CITY_ID = "key_city_id";

    private Button mFavoriteButton;
    private Button mNotFavoriteButton;
    private boolean mIsFavorite;

    private String mBusinessId;
    private String mCityName;

    private Filters mFilters;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business);

        // If device is a tablet and user changes from portrait to landscape orientation, then we
        // want to return to the MainActivity since the BusinessActivity will show up in the right
        // pane of a two-pane layout in that case
        Configuration configuration = getResources().getConfiguration();
        if (configuration.screenWidthDp >= 820) {
            finish();
        }

        // Setup TitleBar
        Toolbar toolbar = findViewById(R.id.business_toolbar);
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        toolbar.setTitleTextAppearance(this, R.style.TextAppearance_AppCompat_Title_Land);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        try {
            actionBar.setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "[ERROR] onCreate - setDisplayHomeAsUpEnabled threw NullPointerException");
        }

        // Get city name from calling Intent
        try {
            mCityName = getIntent().getExtras().getString(KEY_CITY_ID);
        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "[ERROR] onCreate - getString(KEY_CITY_ID) threw NullPointerException");
            throw new IllegalArgumentException("BusinessActivity called without Extra " + KEY_CITY_ID);
        }

        // Get the Filters POJO from MainActivity so it can be restored with the user's last query
        // when we return to it
        mFilters = getIntent().getParcelableExtra(FILTER_STATE);

        Business business = getIntent().getParcelableExtra(KEY_BUSINESS_PARCELABLE);
        mBusinessId = business.getId();

        Log.i(LOG_TAG, "[INFO] onCreate - business.getLocation()" + business.getLocation());

        // Setup BottomSheet
        View bottomsheet = findViewById(R.id.business_bottomsheet);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomsheet);
        behavior.setHideable(false);
        ImageView notification = bottomsheet.findViewById(R.id.buttonbar_campaign_icon);
        TextView textView = bottomsheet.findViewById(R.id.buttonbar_has_campaign);
        if (business.getCampaign()) {
            notification.setImageResource(R.drawable.ic_notifications_accent_24dp);
            textView.setText(R.string.buttonbar_campaign);
            textView.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        } else {
            notification.setImageResource(R.drawable.ic_notifications_primary_24dp);
            textView.setText(R.string.buttonbar_no_campaign);
            textView.setTextColor(ContextCompat.getColor(this, R.color.white));
        }

        // Initialize Favorite buttons
        mFavoriteButton = bottomsheet.findViewById(R.id.buttonbar_select_favorite);
        mNotFavoriteButton = bottomsheet.findViewById(R.id.buttonbar_deselect_favorite);
        mIsFavorite = UserPreferences.isFavorite(this, mBusinessId);
        if (mIsFavorite) {
            mFavoriteButton.setVisibility(View.VISIBLE);
            mNotFavoriteButton.setVisibility(View.GONE);
        } else {
            mFavoriteButton.setVisibility(View.GONE);
            mNotFavoriteButton.setVisibility(View.VISIBLE);
        }
        mFavoriteButton.setOnClickListener(this);
        mNotFavoriteButton.setOnClickListener(this);

        // Add BusinessFragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        BusinessFragment fragment = BusinessFragment.newInstance(business);
        transaction.replace(R.id.business_fragment_container, fragment);
        transaction.commit();
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(FILTER_STATE, mFilters);
            startActivity(intent);
        }
        return true;
    }


    /**
     * Method for handling user click on Favorite button. Toggles the state of whether the business
     * is selected as a favorite, sets the local database accordingly, and confirms user selection
     * through a Toast.
     * @param isFavorite boolean for target value of favorite status
     */
    private void setFavoriteBusiness(boolean isFavorite) {
        if (isFavorite) {
            // Mark selection on Cloud Firestore database and update SharedPreferences
            CommonUtils.setFavorite(this, true, mCityName, mBusinessId);
            mFavoriteButton.setVisibility(View.VISIBLE);
            mNotFavoriteButton.setVisibility(View.GONE);
            mIsFavorite = true;
            Toast.makeText(this, getString(R.string.toast_favorite_added),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Mark de-selection on Cloud Firestore database and update SharedPreferences
            CommonUtils.setFavorite(this, false, mCityName, mBusinessId);
            mFavoriteButton.setVisibility(View.GONE);
            mNotFavoriteButton.setVisibility(View.VISIBLE);
            mIsFavorite = false;
            Toast.makeText(this, getString(R.string.toast_favorite_removed),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonbar_select_favorite:
                // Toggle the current favorite status to false
                Log.i(LOG_TAG, "[INFO] onClick - mIsFavorite = " + mIsFavorite);
                setFavoriteBusiness(false);
                break;
            case R.id.buttonbar_deselect_favorite:
                // Toggle the current favorite status to true
                Log.i(LOG_TAG, "[INFO] onClick - mIsFavorite = " + mIsFavorite);
                setFavoriteBusiness(true);
                break;
            default:
                break;
        }
    }
}

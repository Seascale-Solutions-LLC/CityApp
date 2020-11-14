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
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.seascale.cityapp.models.Filters;

import static com.seascale.cityapp.MainActivity.FILTER_STATE;

public class UserInformationActivity extends AppCompatActivity {

    private final String LOG_TAG = UserInformationActivity.class.getSimpleName();

    private TextView mBigTextView;

    private Filters mFilters;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_information);

        // Setup TitleBar
        Toolbar toolbar = findViewById(R.id.user_information_toolbar);
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        toolbar.setTitleTextAppearance(this, R.style.TextAppearance_AppCompat_Title_Land);

        // Get the type of information this activity should show
        String type;
        try {
            type = getIntent().getExtras().getString("TYPE");
        } catch (NullPointerException e) {
            type = getString(R.string.about_cityapp_name);
            Log.e(LOG_TAG, "[ERROR] onCreate - Calling activity did not include \"TYPE\" key");
        }
        toolbar.setTitle(type);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        try {
            actionBar.setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "[ERROR] onCreate - setDisplayHomeAsUpEnabled threw NullPointerException");
        }

        // Get the Filters POJO from MainActivity so it can be restored with the user's last query
        // when we return to it
        mFilters = getIntent().getParcelableExtra(FILTER_STATE);

        mBigTextView = findViewById(R.id.large_notice_container);

        try {
            if (type.equals(getString(R.string.user_privacy_name))) {
                mBigTextView.setText(R.string.user_privacy_policy);
            } else if (type.equals(getString(R.string.eula_name))) {
                mBigTextView.setText(R.string.end_user_license_agreement);
            } else if (type.equals(getString(R.string.about_cityapp_name))) {
                showAboutInformation();
            } else {
                Log.e(LOG_TAG, "[ERROR] onCreate - Activity called with unrecognized type variable");
            }
        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "[ERROR] onCreate - type.equals() threw NullPointerException");
        }
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

    private void showAboutInformation() {
        // Setup informational sections
        Typeface typeface = Typeface.createFromAsset(this.getAssets(), "fonts/montserrat_bold.ttf");
        mBigTextView.setTypeface(typeface);
        mBigTextView.setTextSize(18);
        mBigTextView.setText(R.string.customer_teaser);

        TextView advertisement = findViewById(R.id.large_notice_container_2);
        advertisement.setText(R.string.cityapp_information_1);
        advertisement.setVisibility(View.VISIBLE);

        TextView userInfoHeader = findViewById(R.id.large_notice_container_3);
        userInfoHeader.setTypeface(typeface);
        userInfoHeader.setTextSize(18);
        userInfoHeader.setText(R.string.problem_reporting);
        userInfoHeader.setVisibility(View.VISIBLE);

        TextView userInfo = findViewById(R.id.large_notice_container_4);
        userInfo.setText(R.string.cityapp_information_2);
        userInfo.setVisibility(View.VISIBLE);

        // Setup problem reporting information sections
        for (int item = 0; item < 5; item++) {
            setupProblemLine(item);
        }

        // App version label and number
        LinearLayout linearLayout = findViewById(R.id.version_container);
        linearLayout.setVisibility(View.VISIBLE);
        TextView versionLabel = findViewById(R.id.version_label);
        versionLabel.setText(R.string.version_label);
        TextView versionNumber = findViewById(R.id.version_number);
        versionNumber.setText(R.string.app_version);

        // Email label and company address
        TextView emailLabel;
        TextView emailAddress;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            linearLayout = findViewById(R.id.email_container);
            linearLayout.setVisibility(View.VISIBLE);
            emailLabel = findViewById(R.id.email_us_label_alt);
            emailAddress = findViewById(R.id.cityapp_email_address_alt);
        } else {
            emailLabel = findViewById(R.id.email_us_label);
            emailLabel.setVisibility(View.VISIBLE);
            emailAddress = findViewById(R.id.cityapp_email_address);
            emailAddress.setVisibility(View.VISIBLE);
        }
        emailLabel.setText(R.string.email_invitation);
        // Show text as underlined so user will know it is a link
        emailAddress.setPaintFlags(emailAddress.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        emailAddress.setText(R.string.seascale_email_address);
        // Set onClickListener with implicit email intent
        emailAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] seascaleAddress = {getString(R.string.seascale_email_address)};
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, seascaleAddress);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * Helper function to setup the five lines describing the five different pieces of information
     * users should include when reporting app problems
     * @param item int corresponding to the required problem reporting item
     */
    private void setupProblemLine(int item) {
        int[] layoutResId = {R.id.problem_info_container_1, R.id.problem_info_container_2,
                R.id.problem_info_container_3, R.id.problem_info_container_4,
                R.id.problem_info_container_5};
        int[] enumStringId = {R.string.enum_1, R.string.enum_2, R.string.enum_3, R.string.enum_4,
                R.string.enum_5};
        int[] problemStringId = {R.string.problem_item_1, R.string.problem_item_2,
                R.string.problem_item_3, R.string.problem_item_4, R.string.problem_item_5};

        LinearLayout linearLayout = findViewById(layoutResId[item]);
        linearLayout.setVisibility(View.VISIBLE);
        TextView textView = linearLayout.findViewById(R.id.enumeration);
        textView.setText(getString(enumStringId[item]));
        textView = linearLayout.findViewById(R.id.problem_item_description);
        textView.setText(problemStringId[item]);
    }
}

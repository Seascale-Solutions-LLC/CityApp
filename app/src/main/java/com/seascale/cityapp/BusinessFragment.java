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
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.seascale.cityapp.data.UserPreferences;
import com.seascale.cityapp.models.Business;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class BusinessFragment extends Fragment implements View.OnClickListener {

    private static final String LOG_TAG = BusinessFragment.class.getSimpleName();

    static final String KEY_BUSINESS_PARCELABLE = "key_business_parcelable";

    private Button mSavedButton;
    private boolean mIsSaved;

    private Business mBusiness;
    private String mBusinessId;

    private View mView;

    /**
     * Following initializer method (not a constructor - Fragment constructors can take no
     * arguments) enables the calling Activity to communicate the Business POJO to this Fragment.
     * The POJO is retrieved in onCreate below.
     * @param business Business POJO
     * @return BusinessFragment (for use with FragmentTransaction#replace())
     */
    public static BusinessFragment newInstance(Business business) {
        Log.i(LOG_TAG, "[INFO] newInstance called");
        BusinessFragment fragment = new BusinessFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_BUSINESS_PARCELABLE, business);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the Business POJO from the calling Activity via the initializer above
        mBusiness = getArguments().getParcelable(KEY_BUSINESS_PARCELABLE);
        mBusinessId = mBusiness.getId();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_business, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mView = view;

        // Setup the UI using the Business POJO retrieved in onCreate
        onBusinessLoaded();
    }


    /**
     * Load selected Business into the UI
     */
    private void onBusinessLoaded() {
        TextView textView;

        // Setup header display
        textView = mView.findViewById(R.id.details_business_name);
        textView.setText(mBusiness.getName());
        ImageView bizImage = mView.findViewById(R.id.details_business_image);
        Glide.with(this).load(mBusiness.getImageId()).into(bizImage);
        textView = mView.findViewById(R.id.detail_business_description);
        textView.setText(mBusiness.getDescription());

        if (mBusiness.getCampaign()) {
            textView = mView.findViewById(R.id.detail_campaign_description);
            textView.setText(mBusiness.getCampaignDescription());
            textView.setVisibility(View.VISIBLE);
        } else {
            textView = mView.findViewById(R.id.detail_campaign_description);
            textView.setVisibility(View.GONE);
        }

        // Initialize Save button
        mSavedButton = mView.findViewById(R.id.button_save);
        // Get current saved status of this business (i.e. whether user add it to their saved list)
        mIsSaved = UserPreferences.isSaved(getContext(), mBusinessId);
        if (mIsSaved) {
            mSavedButton.setText(R.string.button_unsave_business);
        } else {
            mSavedButton.setText(R.string.button_save_business);
        }
        mSavedButton.setOnClickListener(this);

        // Setup lines of detailed business information
        //<Hours:>
        //Website:           Icon      Link
        //<Address:>         Icon      Link
        //<Phone:>           Icon      Link
        //Email:             Icon      Link
        //<Awards:>
        //Genre:
        //Special:
        //Atmosphere:
        //Price:

        // Hours - List<String> - (mandatory) - No Icon - No Link
        setupDetailLine(Business.FIELD_HOURS, -1, R.id.detail_line_hours,
                mBusiness.getHours(), null);

        // Website - String - (optional) - Icon - Link
        if (mBusiness.getUrl() != null && !mBusiness.getUrl().isEmpty()) {
            setupDetailLine(Business.FIELD_WEB_URL, R.drawable.ic_www_primary_24dp,
                    R.id.detail_line_url, mBusiness.getUrl(), Intent.ACTION_VIEW);
        }

        // Address - List<String> - (mandatory) - Icon - Link
        setupDetailLine(Business.FIELD_ADDRESS, R.drawable.ic_pin_drop_primary_24dp,
                R.id.detail_line_address, mBusiness.getAddress(), Intent.ACTION_VIEW);

        // Phone - List<String> - (optional) - Icon - Link
        if (mBusiness.getPhone() != null && !mBusiness.getPhone().isEmpty()) {
            setupDetailLine(Business.FIELD_PHONE, R.drawable.ic_phone_primary_24dp,
                    R.id.detail_line_phone, mBusiness.getPhone(), Intent.ACTION_DIAL);
        }

        // Email - String - (optional) - Icon - Link
        if (mBusiness.getEmail() != null && !mBusiness.getEmail().isEmpty()) {
            setupDetailLine(Business.FIELD_EMAIL, R.drawable.ic_email_primary_24dp,
                    R.id.detail_line_email, mBusiness.getEmail(), Intent.ACTION_SENDTO);
        }

        // Awards - List<String> - (optional) - No Icon - No Link
        if (mBusiness.getAwards() != null && !mBusiness.getAwards().isEmpty()) {
            setupDetailLine(Business.FIELD_AWARDS, -1, R.id.detail_line_awards,
                    mBusiness.getAwards(), null);
        }

        // Genre (a.k.a. SecondFilter) - String - (optional) - No Icon - No Link
        if (mBusiness.getSecondFilter() != null && !mBusiness.getSecondFilter().isEmpty()) {
            setupDetailLine(Business.FIELD_SECOND_FILTER, -1,
                    R.id.detail_line_secondfilter, mBusiness.getSecondFilter(), null);
        }

        // Special - String - (optional) - No Icon - No Link
        if (mBusiness.getSpecial() != null && !mBusiness.getSpecial().isEmpty()) {
            setupDetailLine(Business.FIELD_SPECIAL, -1, R.id.detail_Line_special,
                    mBusiness.getSpecial(), null);
        }

        // Atmosphere (a.k.a. Type) - String - (optional) - No Icon - No Link
        if (mBusiness.getType() != null && !mBusiness.getType().isEmpty()) {
            setupDetailLine(Business.FIELD_TYPE, -1, R.id.detail_line_type,
                    mBusiness.getType(), null);
        }

        // Price  - String - (optional) - No Icon - No Link
        if (mBusiness.getPrice() != 0) {
            String[] priceDecoder = {getString(R.string.price_code_1),
                    getString(R.string.price_code_2), getString(R.string.price_code_3),
                    getString(R.string.price_code_4)};

            setupDetailLine(Business.FIELD_PRICE, -1, R.id.detail_line_price,
                    priceDecoder[mBusiness.getPrice()], null);
        }
    }


    /**
     * Helper function to setup detail_line_item for a particular parameter
     * @param infoName String of parameter derived from Business POJO FIELD names
     * @param includeResId int for the <include> resource ID for the line to setup
     * @param iconImageResId int for the icon drawable (if applicable). If not applicable pass in -1
     * @param value List<String> of values for this parameter
     * @param intentType String for Intent ACTION type (pass null if no link is associated with this
     *                   parameter)
     */
    private void setupDetailLine(final String infoName, int iconImageResId,
                                 int includeResId, final List<String> value,
                                 final String intentType) {
        // Ensure <include> is visible
        LinearLayout detailLine = mView.findViewById(includeResId);
        detailLine.setVisibility(View.VISIBLE);

        // Add icon if relevant
        if (iconImageResId != -1) {
            ImageView iconImage = detailLine.findViewById(R.id.detail_line_icon);
            iconImage.setImageResource(iconImageResId);
        }

        // Add label
        TextView label = detailLine.findViewById(R.id.detail_line_label);
        String sLabelResId = "detail_label_" + infoName;
        int iLabelResId = getResources().getIdentifier(sLabelResId, "string",
                getContext().getPackageName());
        label.setText(iLabelResId);

        // Add business specific details
        int numEntries = value.size();
        if (numEntries > 3) {
            // detail_line_item has provisions for only three rows of data. If more than three are
            // encountered, log the encounter but proceed with populating the UI with the data that
            // will fit.
            Log.e(LOG_TAG, "[ERROR] setupDetailLine - " + infoName + " provided with > 3 rows");
        }

        int[] dataResId = {R.id.detail_line_info_0, R.id.detail_line_info_1, R.id.detail_line_info_2};
        TextView data;

        for (int item = 0; item < numEntries; item++) {
            data = detailLine.findViewById(dataResId[item]);
            data.setVisibility(View.VISIBLE);
            data.setText(value.get(item));

            // Setup link (if applicable)
            if (intentType != null) {
                final int itemNum = item;
                // Show text as underlined so user will know it is a link
                data.setPaintFlags(data.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                data.setTextColor(ContextCompat.getColor(getContext(), R.color.clickable_text_color));
                data.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        launchIntent(intentType, value.get(itemNum), itemNum, infoName);
                    }
                });
            }
        }
    }


    /**
     * Overloaded function call to handle cases where Business parameter is a single String (as
     * opposed to a List<String>)
     * @param infoName String of parameter derived from Business POJO FIELD names
     * @param iconImageResId int for the icon drawable (if applicable). If not applicable pass in -1
     * @param includeResId int for the <include> resource ID for the line to setup
     * @param value parameter value of type String
     * @param intentType String for Intent ACTION type (pass null if no link is associated with this
     *                   parameter)
     */
    private void setupDetailLine(String infoName, int iconImageResId, int includeResId,
                                 String value, String intentType) {
        List<String> valueList = new ArrayList<>(Arrays.asList(value));
        setupDetailLine(infoName, iconImageResId, includeResId, valueList, intentType);
    }


    /**
     * Helper function for the onClickListener, which launches implicit intents
     * @param intentType Intent type (e.g. ACTION_VIEW or ACTION_DIAL
     * @param value String of actual Intent data (e.g. URL string)
     * @param index int index into Business POJO (all items are cast into List<> by setupDetailLine
     *              method)
     * @param infoType String derived from Business POJO FILED name to determine what type of Intent
     *                 needs to be forumlated
     */
    private void launchIntent(String intentType, String value, int index, String infoType) {
        Intent intent = new Intent(intentType);

        if (intentType.equals(Intent.ACTION_VIEW)) {
            // Used to call web browser and mapping apps

            if (infoType.equals(Business.FIELD_ADDRESS)) {
                // Build Intent to launch a mapping app
                // GPS coordinates are stored as a single String in Firebase, so break up into
                // constituent components
                String coords = mBusiness.getCoordinates().get(index);
                String[] latLong = coords.split(",");

                // Construct Intent String from coordinates
                String location = String.format(Locale.US, "geo:0,0?q=") +
                        android.net.Uri.encode(String.format(Locale.US,"%s@%f,%f","",
                                Float.valueOf(latLong[0]), Float.valueOf(latLong[1])),"UTF-8");
                intent.setData(Uri.parse(location));
            } else {
                // Build Intent to launch a web page
                intent.setData(Uri.parse(value));
            }
        } else if (intentType.equals(Intent.ACTION_DIAL)) {
            //Used to place a phone call
            intent.setData(Uri.parse("tel:" + value));
        } else if (intentType.equals(Intent.ACTION_SENDTO)) {
            // Used to send email
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_EMAIL, value);
        }

        // Launch Intent
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivity(intent);
        }
    }


    /**
     * Method for handling user click on Save button. Toggles the state of whether the business
     * is included on the saved list, sets the local database accordingly, and confirms user
     * selection through a Toast.
     * @param isSaved boolean for target value of saved status
     */
    private void setSaveBusiness(boolean isSaved) {
        if (isSaved) {
            UserPreferences.addToSpSaved(getContext(), mBusinessId);
            Toast.makeText(getContext(), getString(R.string.toast_save_business),
                    Toast.LENGTH_SHORT).show();
            mSavedButton.setText(R.string.button_unsave_business);
            mIsSaved = true;
        } else {
            UserPreferences.removeFromSpSaved(getContext(), mBusinessId);
            Toast.makeText(getContext(), getString(R.string.toast_remove_saved),
                    Toast.LENGTH_SHORT).show();
            mSavedButton.setText(R.string.button_save_business);
            mIsSaved = false;
        }
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_save) {
            setSaveBusiness(!mIsSaved);
        }
    }
}

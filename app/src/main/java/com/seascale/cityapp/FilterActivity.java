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
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.seascale.cityapp.data.UserPreferences;
import com.seascale.cityapp.models.Filters;
import com.seascale.cityapp.viewmodels.FilterActivityViewModel;

import java.util.LinkedList;
import java.util.List;

import static com.seascale.cityapp.MainActivity.FILTER_STATE;

public class FilterActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener {

    private static final String LOG_TAG = FilterActivity.class.getSimpleName();

    public static final String FILTER_KEY = "filter_result";

    private Filters mFilters;

    private boolean mThirdRow;

    // == DEBUG GLOBALS - REMOVE FOR PRODUCTION ==
    private boolean mDebug = false;

    private Spinner mTopSpinner;
    private Spinner mSecondarySpinner;
    private Spinner mSpecialSpinner;
    private Spinner mAtmosphereSpinner;
    private Spinner mPriceSpinner;
    private Spinner mSortSpinner;

    private String mSpinnerDefault;

    // Member arrays to simplify form initialization and reset
    private final int[] mCheckboxResIds = {R.id.checkbox_1, R.id.checkbox_2, R.id.checkbox_3};
    private final int[] mPresetRowResIds = {R.id.filter_preset_row_one, R.id.filter_preset_row_two};
    private final int[] mLocationRowResIds = {R.id.filter_location_row_one,
                        R.id.filter_location_row_two, R.id.filter_location_row_three,
                        R.id.filter_location_row_four, R.id.filter_location_row_five,
                        R.id.filter_location_row_six};

    private FilterActivityViewModel mViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        // Setup TitleBar
        Toolbar toolbar = findViewById(R.id.filter_toolbar);
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        toolbar.setTitleTextAppearance(this, R.style.TextAppearance_AppCompat_Title_Land);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        try {
            actionBar.setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "[ERROR] onCreate - setDisplayHomeAsUpEnabled threw NullPointerException");
        }

        // Setup miscellaneous global variables
        mSpinnerDefault = getString(R.string.filter_prompt);

        // Detect device screen (whether landscape, portrait, or tablet)
        mThirdRow = getResources().getBoolean(R.bool.filter_block_row_three);

        // Initialize filter and sort Spinners
        mTopSpinner = findViewById(R.id.filter_spinner_top);
        mTopSpinner.setOnItemSelectedListener(this);
        mSecondarySpinner = findViewById(R.id.filter_spinner_second);
        mSpecialSpinner = findViewById(R.id.filter_spinner_food_special);
        mAtmosphereSpinner = findViewById(R.id.filter_spinner_food_atmosphere);
        mPriceSpinner = findViewById(R.id.filter_spinner_food_price);
        mSortSpinner = findViewById(R.id.sort_spinner);
        mSortSpinner.setOnItemSelectedListener(this);

        // Setup Standard Filters
        String[] filterNames = getResources().getStringArray(R.array.filter_preset_types);
        layoutCheckboxes(filterNames, mPresetRowResIds);

        // Setup Location Filters
        String cityName = UserPreferences.getSpCity(this);
        String[] locationNames;
        if(cityName.equals(getString(R.string.austin_name))) {
            locationNames = getResources().getStringArray(R.array.austin_locations);
        } else {
            Log.e(LOG_TAG, "[ERROR] regionName array is empty!");
            locationNames = new String[] {};
        }
        layoutCheckboxes(locationNames, mLocationRowResIds);

        // Setup ViewModel
        mViewModel = new ViewModelProvider(this).get(FilterActivityViewModel.class);

        mFilters = getIntent().getParcelableExtra(FILTER_STATE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Update member variable in case any configuration changes have occurred
        mThirdRow = getResources().getBoolean(R.bool.filter_block_row_three);
        if (mDebug) {
            Log.i(LOG_TAG, "[INFO] onStart - mThirdRow = " + mThirdRow);
            Log.i(LOG_TAG, "[INFO] onStart - mViewModel.getFilters().getSecondary() = "
                    + mViewModel.getFilters().getSecondary());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Restore UI (the state of all Spinners and CheckBoxes) to previous state before
        // configuration change
        restoreUi(mViewModel.getFilters());
        if (mDebug) {
            Log.i(LOG_TAG, "[INFO] onResume - mViewModel.getFilters().getSecondary() = "
                    + mViewModel.getFilters().getSecondary());
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

    /**
     * This method is reached either through onStart, in which case there has been an orientation
     * change, or through the CLEAR button on the UI. This method will configure the UI accordingly
     * based on the Filters POJO passed to it.
     * @param filters Filters POJO
     */
    private void restoreUi(Filters filters) {

        if (mDebug) {
            if (filters.getPresets() != null) {
                for (String preset : filters.getPresets()) {
                    Log.i(LOG_TAG, "[INFO] restoreUi - preset = " + preset);
                }
            } else {
                Log.i(LOG_TAG, "[INFO] restoreUi - preset is null");
            }
        }

        // Restore preset filters
        setCheckboxData(mPresetRowResIds, filters.getPresets());

        // Restore filter Spinners
        String category = filters.getCategory();

        if (mDebug) {
            if (category != null) {
                Log.i(LOG_TAG, "[INFO] restoreUi - category = " + category);
            } else {
                Log.i(LOG_TAG, "[INFO] restoreUi - category is null");
            }
        }

        if (category == null) {
            category = mSpinnerDefault;
        }
        setSpinner(R.array.filter_categories, category, mTopSpinner);
        if (!category.equals(mSpinnerDefault)) {
            // Category has been set - continue setting up spinners
            mSecondarySpinner.setVisibility(View.VISIBLE);
            String secondary = filters.getSecondary();

            int secondaryResId = getResources().getIdentifier("filter_second_" +
                            category.toLowerCase(),"array", getPackageName());

            if (secondaryResId == 0) {
                Log.e(LOG_TAG, "[ERROR] restoreUi - secondaryResId = 0");
            }
            if (secondary == null) {
                Log.e(LOG_TAG, "[ERROR] restoreUI - secondary == null");
                secondary = mSpinnerDefault;
            }

            setSpinner(secondaryResId, secondary, mSecondarySpinner);

            if (category.equals(getString(R.string.food_category_name))) {
                // The food category has been selected, so continue with setting up the tertiary
                // portion of the UI
                setSubCategories(true, View.VISIBLE);
                String special = filters.getSpecial();
                setSpinner(R.array.filter_food_special, special, mSpecialSpinner);
                String atmosphere = filters.getAtmosphere();
                setSpinner(R.array.filter_food_atmosphere, atmosphere, mAtmosphereSpinner);
                int price = filters.getPrice();
                mPriceSpinner.setSelection(price + 1);
            } else {
                // Something other than the food category has been selected - reset the tertiary
                // filters
                setSubCategories(false, View.GONE);
            }
        } else {
            // Category has NOT been set - reset all subcategory (secondary and tertiary) Spinners
            setSubCategories(true, View.GONE);
        }

        // Restore sort Spinner
        int sort = filters.getSortDirection();
        mSortSpinner.setSelection(sort);

        // Restore location filters
        setCheckboxData(mLocationRowResIds, filters.getLocations());
    }

    /**
     * Helper function to reset or setup subcategory (secondary and tertiary) filters in the UI
     *
     * case: reset (clear)      cascade     visibility
     * 1) Both 2nd and 3rd      true        GONE
     * 2) Only 3rd              false       GONE
     *
     * case: setup
     * 1) Only 2nd              false       VISIBLE
     * 2) Both 2nd and 3rd      true        VISIBLE
     *
     * @param cascade boolean (see truth table above)
     * @param visibility either GONE for reset or VISIBLE for setup
     */
    private void setSubCategories(boolean cascade, int visibility) {
        if (visibility == View.VISIBLE || cascade) {
            // Show or hide secondary filter section
            TextView secondaryHeader = findViewById(R.id.filter_secondary_header);
            secondaryHeader.setVisibility(visibility);
            mSecondarySpinner.setVisibility(visibility);
            if (visibility == View.GONE) {
                // Reset secondary filter Spinner
                mSecondarySpinner.setSelection(0);
            }
            mSecondarySpinner.setOnItemSelectedListener(this);
        }
        if (visibility == View.GONE || cascade) {
            // Show or hide the tertiary filter section
            TextView tertiaryHeader = findViewById(R.id.filter_tertiary_header);
            tertiaryHeader.setVisibility(visibility);
            TextView specialLabel = findViewById(R.id.filter_food_special_label);
            specialLabel.setVisibility(visibility);
            mSpecialSpinner.setVisibility(visibility);
            FrameLayout divider1 = findViewById(R.id.filter_divider_1);
            divider1.setVisibility(visibility);
            TextView atmosphereLabel = findViewById(R.id.filter_food_atmosphere_label);
            atmosphereLabel.setVisibility(visibility);
            mAtmosphereSpinner.setVisibility(visibility);
            FrameLayout divider2 = findViewById(R.id.filter_divider_2);
            divider2.setVisibility(visibility);
            TextView priceLabel = findViewById(R.id.filter_food_price_label);
            priceLabel.setVisibility(visibility);
            mPriceSpinner.setVisibility(visibility);
            if (visibility == View.GONE) {
                // Reset tertiary filter Spinners
                mSpecialSpinner.setSelection(0);
                mAtmosphereSpinner.setSelection(0);
                mPriceSpinner.setSelection(0);
            }
            mSpecialSpinner.setOnItemSelectedListener(this);
            mAtmosphereSpinner.setOnItemSelectedListener(this);
            mPriceSpinner.setOnItemSelectedListener(this);
        }
    }

    /**
     * Called in response to user click on UI button. Clears all filters by setting all checkboxes
     * and spinners to their default states and updating the FilterActivityViewModel.
     * @param view View that called this method
     */
    public void onClearFiltersClicked(View view) {
        // Reset the UI
        restoreUi(Filters.getDefault());
        // Update ViewModel
        mViewModel.setFilters(Filters.getDefault());
    }

    /**
     * Called in response to user click on UI button. Recalls a previously saved filter (if exists)
     * and sets up the UI with those values.
     * @param view View that called this method
     */
    public void onRecallFilterClicked(View view) {
        // Check if a filter has been saved in the local database
        Filters filters = UserPreferences.recallSpFilter(this);
        if (filters == null) {
            Toast.makeText(this, R.string.filter_none_exists_toast, Toast.LENGTH_SHORT).show();
            return;
        }
        mViewModel.setFilters(filters);
        restoreUi(filters);
    }

    /**
     * Called in response to user click on UI button. Saves the current filter configuration to the
     * local database for future recall by the user via the "Recall Filter" button (above).
     * @param view View that called this method
     */
    public void onSaveFilterClicked(View view) {
        UserPreferences.saveSpFilter(this, mViewModel.getFilters());
        Toast.makeText(this, R.string.filter_saved_toast, Toast.LENGTH_SHORT).show();
    }

    /**
     * Following is invoked when the "APPLY" button is clicked by the user. This method merely acts
     * as a gateway to the buildQueryFromFilters method to return a Query object back to the calling
     * Activity (MainActivity).
     * @param view View that called this method
     */
    public void onApplyFilterClicked(View view) {
        // Get filters from ViewModel (cache)
        Filters filters = mViewModel.getFilters();
        if (filters == null) {
            Log.i(LOG_TAG, "[INFO] onApplyFilter - filters = null");
        }

        // Return Filters POJO to calling Activity (MainActivity) via onActivityResult
        // Reference: https://stackoverflow.com/questions/10407159/how-to-manage-startactivityforresult-on-android
        Intent returnIntent = new Intent();
        returnIntent.putExtra(FILTER_KEY, filters);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    /**
     * Method for programmatically laying out checkboxes in UI based on device screen
     * characteristics (e.g. portrait, landscape, or tablet) and number of items to be laid out
     * @param checkboxNames Array of Strings associated with each CheckBox
     * @param rowNames Array of integers (filter_block layout resource IDs)
     */
    private void layoutCheckboxes(String[] checkboxNames, int[] rowNames) {
        int nameEntry = 0;
        //int numColumns = getResources().getInteger(R.integer.filter_grid_columns);
        int numColumns = mThirdRow ? 3 : 2;
        int numItems = checkboxNames.length;
        int numRows = (numItems / numColumns) + (numItems % numColumns);

        for (int row = 0; row < numRows; row++) {
            LinearLayout layout = findViewById(rowNames[row]);
            layout.setVisibility(View.VISIBLE);
            for (int column = 0; column < numColumns; column++) {
                if (nameEntry < numItems) {
                    CheckBox checkBox = layout.findViewById(mCheckboxResIds[column]);
                    checkBox.setText(checkboxNames[nameEntry]);
                    nameEntry++;
                    checkBox.setVisibility(View.VISIBLE);
                } else {
                    break;
                }
            }
        }
    }

    /**
     * Helper function that enables the display of the secondary filter in the UI and populates the
     * secondary Spinner. Also saves selection to the ViewModel.
     */
    private void onTopSpinnerSelected() {
        // Get user selection and save to cache
        String category = getUserSelection(mTopSpinner);

        if (category == null) {
            // Initialized state - nothing more to do
            return;
        }

        if (category.equals(mSpinnerDefault)) {
            // Set to default - nothing more to do
            return;
        }

        mViewModel.setCategory(category);

        // Enable secondary filter
        setSubCategories(false, View.VISIBLE);

        // Construct string array name that will be used to populate the secondary Spinner
        String secondaryArrayResourceId = "filter_second_" + category.toLowerCase();
        int stringArrayResourceId = getResources()
                .getIdentifier(secondaryArrayResourceId.replace(" ", ""),
                "array", getPackageName());

        // Set the array to the secondary filter/spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                stringArrayResourceId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSecondarySpinner.setAdapter(adapter);

        // In the event that we are restoring the UI at this point, set the Spinner to the saved value
        setSpinner(stringArrayResourceId, mViewModel.getFilters().getSecondary(), mSecondarySpinner);

        // Setup remaining filter if category has been set to "Food"
        if (category.equals(getString(R.string.food_category_name))) {
            addTertiaryFilters();
        } else {
            // Something other than the food category has been selected - reset the tertiary
            // filters
            setSubCategories(false, View.GONE);
        }
    }

    /**
     * Helper function to add the additional filters associated with the food category
     */
    private void addTertiaryFilters() {
        // Enable tertiary filter information in the case where the food category has been selected
        setSubCategories(true, View.VISIBLE);

        // Setup Spinners
        // Special considerations filter
        ArrayAdapter<CharSequence> specialAdapter = ArrayAdapter.createFromResource(this,
                R.array.filter_food_special, android.R.layout.simple_spinner_item);
        specialAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpecialSpinner.setAdapter(specialAdapter);

        // In the event that we are restoring the UI at this point, set the Spinner to the saved value
        setSpinner(R.array.filter_food_special, mViewModel.getFilters().getSpecial(), mSpecialSpinner);

        // Atmosphere filter
        ArrayAdapter<CharSequence> atmosphereAdapter = ArrayAdapter.createFromResource(this,
                R.array.filter_food_atmosphere, android.R.layout.simple_spinner_item);
        atmosphereAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAtmosphereSpinner.setAdapter(atmosphereAdapter);

        // In the event that we are restoring the UI at this point, set the Spinner to the saved value
        setSpinner(R.array.filter_food_atmosphere, mViewModel.getFilters().getAtmosphere(),
                mAtmosphereSpinner);

        // Price filter
        ArrayAdapter<CharSequence> priceAdapter = ArrayAdapter.createFromResource(this,
                R.array.filter_food_price, android.R.layout.simple_spinner_item);
        priceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPriceSpinner.setAdapter(priceAdapter);

        // In the event that we are restoring the UI at this point, set the Spinner to the saved value
        mPriceSpinner.setSelection(mViewModel.getFilters().getPrice());
    }

    /**
     * Helper function used in conjunction with onItemSelected to return the state of a given
     * Spinner
     * @param spinner Initialized Spinner object
     * @return User selection as a String
     */
    private String getUserSelection(Spinner spinner) {
        // Get user selection
        String selection = (String) spinner.getSelectedItem();

        if (mDebug) {
            Log.i(LOG_TAG, "[INFO] getUserSelection - selection = " + selection);
        }

        if (selection.equals(mSpinnerDefault) ||
                selection.equals(getString(R.string.filter_prompt_negative))) {
            // No filter applied (default)

            if (mDebug) {
                Log.i(LOG_TAG, "[INFO] getUserSelection - selection = " + mSpinnerDefault);
            }

            selection = null;
        }
        return selection;
    }

    /**
     * Following is a very inefficient mechanism, but it lends itself to a more adaptive design to
     * make future changes easier. Due to how checkboxes are implemented in activity_filter there is
     * no way to know which item was selected without querying all existing checkboxes. This method
     * performs the query and updates the ViewModel appropriately.
     * @param view the View that triggered this method
     */
    public void onCheckboxClicked(View view) {
        Filters filters = new Filters();
        filters.setPresets(getCheckboxData(mPresetRowResIds));
        filters.setLocations(getCheckboxData(mLocationRowResIds));

        // Save results to cache (ViewModel)
        mViewModel.setCheckboxes(filters);
    }

    /**
     * Helper function for onCheckboxClicked method.
     * @param rowResIds Array of filter_block resource IDs for a given filter area (e.g. presets or
     *                  locations)
     * @return List of string names for all items selected by the user (i.e. where
     * CheckBox.isChecked())
     */
    private List<String> getCheckboxData(int[] rowResIds) {
        CheckBox checkBox;
        LinearLayout layout;
        List<String> selectedItems = new LinkedList<>();

        for (int rowResId : rowResIds) {
            layout = findViewById(rowResId);
            for (int checkboxId : mCheckboxResIds) {
                if (checkboxId == mCheckboxResIds[2] && !mThirdRow) break;

                checkBox = layout.findViewById(checkboxId);
                if (mDebug) {
                    Log.i(LOG_TAG, "[INFO] getCheckboxData - checkBox.getText().toString() = "
                            + checkBox.getText().toString());
                }
                if (checkBox.isChecked()) {
                    selectedItems.add(checkBox.getText().toString());
                }
            }
        }
        return selectedItems;
    }

    /**
     * Helper function for restoreUi method.
     * @param rowResIds Array of filter_block resource IDs for a given filter area (e.g. presets or
     *                  locations)
     */
    private void setCheckboxData(int[] rowResIds, List<String> optionsSelected) {
        CheckBox checkBox;
        String checkboxName;
        LinearLayout layout;

        for (int rowResId : rowResIds) {
            layout = findViewById(rowResId);
            for (int checkboxId : mCheckboxResIds) {
                if (checkboxId == mCheckboxResIds[2] && !mThirdRow) break;
                checkBox = layout.findViewById(checkboxId);
                checkboxName = checkBox.getText().toString();

                if (optionsSelected == null) {
                    if (mDebug) {
                        Log.i(LOG_TAG, "[INFO] setCheckboxData - optionsSelected null"
                                + " for checkboxName = " + checkboxName);
                    }
                    checkBox.setChecked(false);
                } else if (optionsSelected.contains(checkboxName)) {
                    checkBox.setChecked(true);
                    if (mDebug) {
                        Log.i(LOG_TAG, "[INFO] setCheckboxData - SELECTED: checkboxName = "
                                + checkboxName);
                        Log.i(LOG_TAG, "[INFO] setCheckboxData - layout = "
                                + getResources().getResourceName(rowResId));
                        Log.i(LOG_TAG, "[INFO] setCheckboxData - checkbox = "
                                + getResources().getResourceName(checkboxId));
                    }
                } else {
                    checkBox.setChecked(false);
                }
            }
        }
    }

    /**
     * Helper function to set a Spinner to a particular value, usually from the POJO cached in the
     * ViewModel
     * @param arrayResId int associated with the String array with which the Spinner is populated
     * @param selection String of an entry to which the Spinner should be set
     * @param spinner Spinner that should be set
     */
    private void setSpinner(int arrayResId, String selection, Spinner spinner) {

        if (mDebug) {
            Log.i(LOG_TAG, "[INFO] setSpinner - arrayResId = " + arrayResId);
            Log.i(LOG_TAG, "[INFO] setSpinner - selection = " + selection);
        }

        if (selection == null) {
            selection = mSpinnerDefault;
        }
        String[] array = getResources().getStringArray(arrayResId);
        for (int index = 0; index < array.length; index++) {
            String item = array[index];

            if (mDebug) {
                Log.i(LOG_TAG, "[INFO] setSpinner - index = " + index);
                Log.i(LOG_TAG, "[INFO] setSpinner - item = " + item);
                Log.i(LOG_TAG, "[INFO] setSpinner - selection = " + selection);
            }

            if (selection.equals(item)) {
                spinner.setSelection(index);
                return;
            }
        }
        Log.e(LOG_TAG, "[ERROR] setSpinner - selection (" + selection + ") not found in Spinner");
    }

    /**
     * Spinner selected listener
     * @param parent Resource ID for the spinner interacted with
     * @param view Resource ID for the view within the AdapterView
     * @param position Position of the view in the adapter
     * @param id The row ID of the view that is selected
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.filter_spinner_top:
                onTopSpinnerSelected();
                break;
            case R.id.filter_spinner_second:
                String secondary = getUserSelection(mSecondarySpinner);
                mViewModel.setSecondaryFilter(secondary);
                break;
            case R.id.filter_spinner_food_special:
                String special = getUserSelection(mSpecialSpinner);
                mViewModel.setSpecialFilter(special);
                break;
            case R.id.filter_spinner_food_atmosphere:
                String atmosphere = getUserSelection(mAtmosphereSpinner);
                mViewModel.setAtmosphereFilter(atmosphere);
                break;
            case R.id.filter_spinner_food_price:
                String priceString = getUserSelection(mPriceSpinner);
                if (priceString == null) {
                    mViewModel.setPriceFilter(-1);
                } else if (priceString.equals(getString(R.string.price_code_1))) {
                    mViewModel.setPriceFilter(1);
                } else if (priceString.equals(getString(R.string.price_code_2))) {
                    mViewModel.setPriceFilter(2);
                } else if (priceString.equals(getString(R.string.price_code_3))) {
                    mViewModel.setPriceFilter(3);
                } else if (priceString.equals(getString(R.string.price_code_4))) {
                    mViewModel.setPriceFilter(4);
                } else {
                    // Spinner is set to default (no filter applied for this parameter)
                    mViewModel.setPriceFilter(-1);
                }
                break;
            case R.id.sort_spinner:
                String sort = getUserSelection(mSortSpinner);
                if (sort.equals(getString(R.string.sort_name_descending))) {
                    mViewModel.setSortDirection(Filters.DESCENDING);
                } else {
                    mViewModel.setSortDirection(Filters.ASCENDING);
                }
                break;
            default:
                Log.e(LOG_TAG, "[ERROR] onItemSelected - Unrecognized resource ID");
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.i(LOG_TAG, "[INFO] onNothingSelected");
    }
}

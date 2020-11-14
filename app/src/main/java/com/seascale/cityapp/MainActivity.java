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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityOptions;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.seascale.cityapp.adapters.BusinessAdapter;
import com.seascale.cityapp.data.UserPreferences;
import com.seascale.cityapp.models.Business;
import com.seascale.cityapp.models.Filters;
import com.seascale.cityapp.utils.CityAppSyncUtils;
import com.seascale.cityapp.utils.CommonUtils;
import com.seascale.cityapp.utils.NotificationUtils;
import com.seascale.cityapp.viewmodels.MainActivityViewModel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.seascale.cityapp.BusinessActivity.KEY_CITY_ID;
import static com.seascale.cityapp.BusinessFragment.KEY_BUSINESS_PARCELABLE;
import static com.seascale.cityapp.FilterActivity.FILTER_KEY;
import static com.seascale.cityapp.data.UserPreferences.SP_CITY_KEY;
import static com.seascale.cityapp.utils.CommonUtils.CF_CITY_IMAGE_LAND;
import static com.seascale.cityapp.utils.CommonUtils.CF_CITY_IMAGE_PORT;
import static com.seascale.cityapp.utils.CommonUtils.CF_CITY_PHOTO_ATTRIBUTION;
import static com.seascale.cityapp.utils.CommonUtils.CF_SECOND_COLLECTION_KEY;
import static com.seascale.cityapp.utils.CommonUtils.CF_TOP_COLLECTION_KEY;
import static com.seascale.cityapp.utils.CommonUtils.CITY_SLOGAN_KEY;

public class MainActivity extends AppCompatActivity implements
        BusinessAdapter.OnLayoutReadyListener,
        BusinessAdapter.OnBusinessSelectedListener,
        View.OnClickListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int FILTER_REQUEST_CODE = 100;
    public static final int QUERY_LIMIT = 20;

    public static final String FILTER_STATE = "filter_state";

    private String mCityName;

    private BusinessAdapter mAdapter;

    // Cloud Firestore related member variables
    private FirebaseFirestore mFirestore;
    private Query mQuery;

    private MainActivityViewModel mViewModel;

    private ImageView mCityIvPort;
    private ImageView mCityIvLand;
    private Button mFavoriteButton;
    private Button mNotFavoriteButton;
    private boolean mIsFavorite;
    private String mBusinessId;

    private Filters mFilters;

    private RecyclerView mRecyclerView;

    private Configuration mConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize member variables
        mConfiguration = getResources().getConfiguration();
        mViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        mCityIvPort = findViewById(R.id.appbar_city_image_port);
        mCityIvLand = findViewById(R.id.appbar_city_image_land);

        // Get the current city and slogan from SharedPreferences
        mCityName = UserPreferences.getSpCity(this).toLowerCase();
        String citySlogan = UserPreferences.getSpCitySlogan(this);

        // Check if this is the first time running the app on this device (in which case a city has
        // not been set to the SharedPreferences)
        if (mCityName.isEmpty() || citySlogan.isEmpty()) {
            // Use Austin as default
            UserPreferences.setSpCity(this, getString(R.string.austin_name));
            UserPreferences.setSpCitySlogan(this, getString(R.string.austin_slogan));
            mCityName = getString(R.string.austin_name);
            citySlogan = getString(R.string.austin_slogan);
        }

        // Check for network connectivity and whether the user should be notified based on previous
        // user interaction with the app
        boolean remindUserAgain = UserPreferences.spRemindUser(this);
        if (!CommonUtils.hasNetworkConnection(this) && remindUserAgain
                && mViewModel.getNotifyUser()) {
            // No network connectivity - notify user
            showNetworkConnectivityDialog();

            // "Latch" the user's acknowledgment in the ViewModel
            mViewModel.setNotifyUser(false);
        }

        // Setup Firebase Cloud Firestore
        FirebaseFirestore.setLoggingEnabled(true);
        mFirestore = FirebaseFirestore.getInstance();

        // Setup city image in AppBar - If the ViewModel copy is null, then the picture URL strings
        // need to be obtained from Cloud Firestore
        if (mViewModel.getCityPictures()[0] == null) {
            getPictureUrlStrings();
        } else {
            postPicture();
        }

        // Collapsing toolbar only exists with phone devices in portrait mode
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (mConfiguration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Set TitleBar font
            Typeface typeface = Typeface.createFromAsset(this.getAssets(),
                    "fonts/montserrat_regular.ttf");
            CollapsingToolbarLayout toolbarLayout = findViewById(R.id.collapsing_toolbar_layout);
            toolbarLayout.setCollapsedTitleTextAppearance(R.style.TextAppearance_AppCompat_Title_Land);
            toolbarLayout.setExpandedTitleTypeface(typeface);
            toolbarLayout.setCollapsedTitleTypeface(typeface);
        } else {
            toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorAccent));
            toolbar.setTitleTextAppearance(this, R.style.TextAppearance_AppCompat_Title_Land);
        }

        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);

        FloatingActionButton filterFab = findViewById(R.id.filter_fab);
        filterFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent filterIntent = new Intent(MainActivity.this, FilterActivity.class);
                filterIntent.putExtra(FILTER_STATE, mFilters);
                startActivityForResult(filterIntent, FILTER_REQUEST_CODE);
            }
        });

        TextView textView = findViewById(R.id.appbar_city_slogan);
        textView.setText(citySlogan);

        // Figure out what to display to the screen. The first step is to determine whether we are
        // reaching this point after a configuration change (e.g. orientation change) when there are
        // no results to show. In that case we need to re-display the empty view. Next, check
        // whether a previously destroyed (non-empty) instance is being re-created so we can restore
        // the RecyclerView with the user's latest Query. First check if anything has been saved in
        // the ViewModel. If not, see if the calling Intent passed a Bundle containing a Filters
        // POJO (note that this requires overriding what happens when the back button is clicked on
        // the other Activities). If all else fails, apply the standard Query.
        Intent intent = getIntent();
        if (intent.hasExtra(FILTER_STATE)) {
            mFilters = intent.getParcelableExtra(FILTER_STATE);
        }

        if (mViewModel.showEmptyView()) {
            Log.i(LOG_TAG, "[INFO] onCreate - ViewModel indicates empty view");
            if (mConfiguration.screenWidthDp >= 820) {
                CoordinatorLayout rightPane = findViewById(R.id.right_pane);
                rightPane.setVisibility(View.INVISIBLE);
            }
            setEmptyView(true);
        } else if (mViewModel.getCurrentFilter() != null) {
            Log.i(LOG_TAG, "[INFO] onCreate - retrieving filter from ViewModel");
            mFilters = mViewModel.getCurrentFilter();
            mQuery = CommonUtils.convertFilterToQuery(this, mCityName, mFilters);
        } else if (mFilters != null) {
            Log.i(LOG_TAG, "[INFO] onCreate - retrieving existing non-null filter");
            // Filter has been preserved from previous Activity
            mViewModel.setCurrentFilter(mFilters);
            mQuery = CommonUtils.convertFilterToQuery(this, mCityName, mFilters);
        } else {
            Log.i(LOG_TAG, "[INFO] onCreate - creating default filter");
            // No existing Filters/Query found - apply the default Query
            // Initial default filter is to return businesses with campaign showing at the top of
            // the list.
            mQuery = mFirestore.collection(CF_TOP_COLLECTION_KEY)
                    .document(mCityName)
                    .collection(CF_SECOND_COLLECTION_KEY)
                    .orderBy(Business.FIELD_CAMPAIGN, Query.Direction.DESCENDING)
                    .limit(QUERY_LIMIT);
        }

        // Setup RecyclerView
        mAdapter = new BusinessAdapter(mQuery, this, this) {
            @Override
            protected void onDataChanged() {
                if (getItemCount() == 0) {
                    // Notify user search returned no results (for some reason this never gets called)
                    Log.i(LOG_TAG, "[INFO] onDataChanged - no results");
                } else {
                    Log.i(LOG_TAG, "[INFO] onDataChanged - item count > 0");
                    // Invalidate the view and then set the adapter once more as otherwise the
                    // measurements will not be recomputed resulting in the business images in
                    // the list items showing up with the wrong dimensions.
                    mRecyclerView.invalidate();
                    mRecyclerView.setAdapter(mAdapter);
                    setEmptyView(false);
                    if (mConfiguration.screenWidthDp >= 820) {
                        CoordinatorLayout rightPane = findViewById(R.id.right_pane);
                        rightPane.setVisibility(View.VISIBLE);
                    }
                }
            }
        };

        mRecyclerView = findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration dividerDecorater = new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerDecorater);
        if (!mViewModel.showEmptyView()) {
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mRecyclerView.setVisibility(View.GONE);
        }

        // Initialize the periodic sync (if not already done) with Cloud Firestore to check for new
        // business campaigns
        CityAppSyncUtils.initialize(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Commence listening for Firestore updates
        mAdapter.startListening();
    }


    @Override
    protected void onStop() {
        super.onStop();
        // Detach listener
        mAdapter.stopListening();
    }


    /**
     * Callback from FilterActivity containing the Filters POJO reflecting the user's selections.
     *
     * NOTE: onActivityResult gets called after onStart and immediate before onResume
     *
     * @param requestCode - int corresponding to the code used to launch the Activity from this
     *                    Activity (MainActivity). That Activity returns with the same code for
     *                    identification purposes.
     * @param resultCode - int of code corresponding to whether launched Activity was successful
     *                   (will return RESULT_CANCELED otherwise)
     * @param data - Intent from which data (in this case the Filters POJO) can be unbundled
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Reference: https://developer.android.com/training/basics/intents/result
        if (requestCode == FILTER_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                try {
                    mFilters = data.getParcelableExtra(FILTER_KEY);
                    if (mFilters != null) {
                        Log.i(LOG_TAG, "[INFO] onActivityResult - mAdapter.getItemCount = " + mAdapter.getItemCount());
                        mQuery = CommonUtils.convertFilterToQuery(this, mCityName, mFilters);
                        // Update the ViewModel
                        mViewModel.setCurrentFilter(mFilters);
                        mAdapter.setQuery(mQuery);
                        // At this juncture the query is typically still running on another thread.
                        // Go ahead and set the empty view and then, if there are results to show,
                        // the onDataChanged callback (located in onCreate) will be invoked at which
                        // point the information can be displayed.
                        if (mAdapter.getItemCount() == 0) {
                            setEmptyView(true);
                            if (mConfiguration.screenWidthDp >= 820) {
                                CoordinatorLayout rightPane = findViewById(R.id.right_pane);
                                rightPane.setVisibility(View.INVISIBLE);
                            }
                        } else {
                            setEmptyView(false);
                        }
                    } else {
                        Log.e(LOG_TAG, "[ERROR] onActivityResult - Received empty Filters " +
                                "object with RESULT_OK resultCode");
                        Toast.makeText(this, getText(R.string.filter_failure_toast_2),
                                Toast.LENGTH_LONG).show();
                    }
                } catch (NullPointerException e) {
                    Log.e(LOG_TAG, "[ERROR] onActivityResult - Did not receive expected Parcelable");
                    Toast.makeText(this, getText(R.string.filter_failure_toast),
                            Toast.LENGTH_LONG).show();
                }
            }
        } else if (requestCode == RESULT_CANCELED) {
            Log.i(LOG_TAG, "[INFO] requestCode == RESULT_CANCELED");
            mFilters = getIntent().getParcelableExtra(FILTER_STATE);
            mViewModel.setCurrentFilter(mFilters);
        } else {
            Log.i(LOG_TAG, "[INFO] unrecognized requestCode");
        }
    }


    /**
     * Implementation of listener defined in BusinessAdapter and implemented by this Activity
     * @param snapshot Business POJO received from Cloud Firestore query
     */
    @Override
    public void onBusinessSelected(View view, DocumentSnapshot snapshot) {
        mBusinessId = snapshot.getId();
        if (snapshot != null && mConfiguration.screenWidthDp < 820) {
            // Open the BusinessActivity for item selected from RecyclerView
            Intent intent = new Intent(this, BusinessActivity.class);
            // Pass the Business POJO passed in from Firestore
            intent.putExtra(KEY_BUSINESS_PARCELABLE, snapshot.toObject(Business.class));
            intent.putExtra(KEY_CITY_ID, mCityName);
            intent.putExtra(FILTER_STATE, mFilters);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Configure the Bundle for a shared element transition animation
                Bundle bundle = ActivityOptions
                        .makeSceneTransitionAnimation(this,
                                view,
                                getString(R.string.shared_element_transition))
                        .toBundle();
                startActivity(intent, bundle);
            } else {
                startActivity(intent);
            }
        } else if (snapshot != null) {
            // Business selected in two-pane mode - show selected business in right pane
            onLayoutReady(snapshot);
        } else {
            Log.e(LOG_TAG, "[ERROR] onBusinessSelected - received null snapshot");
        }
    }


    /**
     * Callback is invoked when the user has entered search criteria in the search widget and
     * then initiated the search by pressing "Go". This Activity is then called with the
     * ACTION_SEARCH intent.
     * @param intent - calling Intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
            handleIntent(intent.getStringExtra(SearchManager.QUERY));
        }
        super.onNewIntent(intent);
    }


    /**
     * Method to take in the search text entered by the user and from it construct and execute a
     * Firestore Cloud query. The intention for the CityApp search feature is to look for business
     * names as well as any entries in the "tags" field matching the user's criteria. In order to
     * simplify this search, the business entry in Firestore should include the business name, in
     * all lowercase and without spaces, as an entry in the "tags" field.
     *
     * @param searchString - String of search text entered by user into the search widget
     */
    private void handleIntent(String searchString) {
        // In order to make search of tags case-insensitive, convert to lowercase. Also remove all
        // spaces. Note this "tags" field entries also be lower case and single words
        searchString = searchString.toLowerCase().replaceAll(" ", "");
        Log.i(LOG_TAG, "[INFO] handleIntent - searchString = " + searchString);

        mQuery = mFirestore.collection(CF_TOP_COLLECTION_KEY)
                .document(mCityName)
                .collection(CF_SECOND_COLLECTION_KEY)
                .whereArrayContainsAny(Business.FIELD_TAGS, Arrays.asList(searchString))
                .orderBy(Business.FIELD_CAMPAIGN, Query.Direction.DESCENDING)
                .limit(QUERY_LIMIT);

        // Setup RecyclerView
        mAdapter.setQuery(mQuery);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search_action).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        // Do not iconify the widget; expand it by default
        searchView.setIconifiedByDefault(true);
        return true;
    }


    /**
     * Method to get the city image URL Strings from Cloud Firestore
     */
    private void getPictureUrlStrings() {
        DocumentReference document = mFirestore.collection(CF_TOP_COLLECTION_KEY).document(mCityName);
        document.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        // Save image URL Strings to the ViewModel
                        mViewModel.setCityPictures(snapshot.getString(CF_CITY_IMAGE_PORT),
                                snapshot.getString(CF_CITY_IMAGE_LAND));
                        mViewModel.setCityAttribution(snapshot.getString(CF_CITY_PHOTO_ATTRIBUTION));
                        // Update the UI
                        postPicture();
                    } else {
                        Log.e(LOG_TAG, "[ERROR] onCreate - city document not found");
                    }
                } else {
                    Log.e(LOG_TAG, "[ERROR] onCreate - failed to obtain city document from CF");
                }
            }
        });
    }


    /**
     * Helper function to update member variables and setup the city image in the UI
     */
    private void postPicture() {
        if (mConfiguration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Glide.with(this).load(mViewModel.getCityPictures()[0]).into(mCityIvPort);
        } else {
            Glide.with(this).load(mViewModel.getCityPictures()[1]).into(mCityIvLand);
        }
    }

    private void setEmptyView(boolean showEmptyView) {
        TextView emptyView = findViewById(R.id.empty_view);
        if (showEmptyView) {
            if (mRecyclerView != null) {
                mRecyclerView.setVisibility(View.GONE);
            }
            emptyView.setVisibility(View.VISIBLE);
            mViewModel.setShowEmptyView(true);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            mViewModel.setShowEmptyView(false);
        }
    }


    public void showPhotoAttribution(View view) {
        Toast.makeText(this, mViewModel.getCityAttribution(), Toast.LENGTH_LONG).show();
    }


    /**
     * Method to instantiate dialog to let user know that there is currently no network connectivity
     * with option to not show this dialog again.
     */
    private void showNetworkConnectivityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.alert_no_connectivity);
        // PositiveButton = "GOT IT!" (dismiss dialog and continue)
        builder.setPositiveButton(R.string.alert_connectivity_dismiss,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // PositiveButton = "DO NOT REMIND ME AGAIN" (store input to SharedPreferences)
        builder.setNegativeButton(R.string.alert_connectivity_not_again,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Store user selection to SharedPreferences
                UserPreferences.setSpRemindUser(getApplicationContext(), false);
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    /**
     * Method to instantiate dialog to let user know that, although Austin is the only city
     * supported by CityApp, they try again later as cities are added.
     */
    private void showTryAgainLaterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.try_again_later);
        builder.setNegativeButton(R.string.dialog_dismiss, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    /**
     * As cities are added, this method should be used to obtain the city name and "slogan" and
     * store as a shared preference.
     */
    private void setCityContext() {
        // TODO: Remove the following as well as its method once more than one city is supported then
        //       enable the subsequent two lines
        showTryAgainLaterDialog();

        //DialogFragment fragment = new CitySelectDialogFragment(new FragmentHandler());
        //fragment.show(getSupportFragmentManager(), "city");
    }


    /**
     * Nested class enables this Activity to get the city selected by the user through the overflow
     * menu via the CitySelectDialogFragment
     */
    class FragmentHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            // Extract the selected city from the bundle (bundle contains index into
            // dialog_city_list array
            Bundle bundle = msg.getData();
            int index = bundle.getInt(SP_CITY_KEY);
            String[] cityArray = getResources().getStringArray(R.array.dialog_city_list);
            String city = cityArray[index];

            // Store the city in SharedPreferences
            UserPreferences.setSpCity(getApplicationContext(), city);

            // Get the city slogan from Firestore and store in SharedPreferences
            DocumentReference reference = mFirestore
                    .collection(CF_TOP_COLLECTION_KEY)
                    .document(city.toLowerCase());
            reference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        try {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String slogan = task.getResult().getString(CITY_SLOGAN_KEY);
                                UserPreferences.setSpCitySlogan(getApplicationContext(), slogan);
                            }
                        } catch (NullPointerException e) {
                            Log.e(LOG_TAG, "[ERROR] NullPointerError thrown within onComplete");
                        }
                    }
                }
            });

            // Update query with new selection
            mQuery = mFirestore.collection(CF_TOP_COLLECTION_KEY)
                    .document(city.toLowerCase())
                    .collection(CF_SECOND_COLLECTION_KEY)
                    .whereEqualTo(Business.FIELD_CAMPAIGN, true)
                    .orderBy(Business.FIELD_NAME, Query.Direction.ASCENDING)
                    .limit(QUERY_LIMIT);
            mAdapter.setQuery(mQuery);
        }
    }


    /**
     * Helper function to show dialog to give user the chance to cancel action that will wipe out
     * all of the businesses they have selected from their "favorites" list
     */
    private void showDeleteConfirmation(final int msgResId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msgResId);
        builder.setPositiveButton(R.string.delete_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    if (msgResId == R.string.delete_favorites_confirmation) {
                        CommonUtils.clearFavorites(getApplicationContext(), mCityName);
                    } else if (msgResId == R.string.delete_saved_confirmation) {
                        UserPreferences.clearSpSaved(getApplicationContext());
                    }
                    dialog.dismiss();
                }
            }
        });
        builder.setNegativeButton(R.string.delete_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        String selection;
        switch (item.getItemId()) {
            case R.id.action_select_city:
                setCityContext();
                return true;
            case R.id.action_clear_favorites:
                showDeleteConfirmation(R.string.delete_favorites_confirmation);
                return true;
            case R.id.action_clear_saved:
                showDeleteConfirmation(R.string.delete_saved_confirmation);
                return true;
            case R.id.action_customer_login:
                // TODO: Create customer interface
                return true;
            case R.id.action_about_cityapp:
                selection = getString(R.string.about_cityapp_name);
                break;
            case R.id.action_user_privacy:
                selection = getString(R.string.user_privacy_name);
                break;
            case R.id.action_user_agreement:
                selection = getString(R.string.eula_name);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        Intent intent = new Intent(this, UserInformationActivity.class);
        intent.putExtra("TYPE", selection);
        intent.putExtra(FILTER_STATE, mFilters);
        startActivity(intent);
        return true;
    }


    //**********************************************************************************************
    // TWO-PANE MODE SPECIFIC CODE (Replicated in BusinessActivity)
    //**********************************************************************************************

    /**
     * Implementation of listener defined in BusinessAdapter and implemented by this Activity.
     * Listener interface returns the DocumentSnapshot of the first item in the Query (and thus
     * RecyclerView) so it can be displayed in the right pane of a two-pane layout (where device
     * is a tablet in orientation mode).
     * @param snapshot Business POJO received from Cloud Firestore query
     */
    @Override
    public void onLayoutReady(DocumentSnapshot snapshot) {

        Log.i(LOG_TAG, "[INFO] onLayoutReady - mAdapter.getItemCount = " + mAdapter.getItemCount());
        Log.i(LOG_TAG, "[INFO] onLayoutReady - snapshot.exists() = " + snapshot.exists());

        if (mConfiguration.screenWidthDp < 820) {
            // This callback is only ever invoked when there are results to show
            setEmptyView(false);
            return;
        }

        Business business = snapshot.toObject(Business.class);
        mBusinessId = business.getId();

        // Just in case a previous search or filter yielded no results, be sure to re-enable the
        // right pane
        CoordinatorLayout rightPane = findViewById(R.id.right_pane);
        rightPane.setVisibility(View.VISIBLE);

        // Begin by setting up the BottomSheet
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
        mIsFavorite = UserPreferences.isFavorite(this, snapshot.getId());
        if (mIsFavorite) {
            mFavoriteButton.setVisibility(View.VISIBLE);
            mNotFavoriteButton.setVisibility(View.GONE);
        } else {
            mFavoriteButton.setVisibility(View.GONE);
            mNotFavoriteButton.setVisibility(View.VISIBLE);
        }
        mFavoriteButton.setOnClickListener(this);
        mNotFavoriteButton.setOnClickListener(this);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        BusinessFragment fragment = BusinessFragment.newInstance(business);
        transaction.replace(R.id.business_fragment_container, fragment);
        transaction.commit();
    }


    /**
     * Method for handling user click on Favorite button. Toggles the state of whether the business
     * is selected as a favorite, sets the local database accordingly, and confirms user selection
     * through a Toast.
     * @param isFavorite boolean for target value of favorite status
     */
    private void setFavoriteBusiness(boolean isFavorite) {
        if (isFavorite) {
            CommonUtils.setFavorite(this, true, mCityName, mBusinessId);
            mFavoriteButton.setVisibility(View.VISIBLE);
            mNotFavoriteButton.setVisibility(View.GONE);
            mIsFavorite = true;
            Toast.makeText(this, getString(R.string.toast_favorite_added),
                    Toast.LENGTH_SHORT).show();
        } else {
            CommonUtils.setFavorite(this, false, mCityName, mBusinessId);
            mFavoriteButton.setVisibility(View.GONE);
            mNotFavoriteButton.setVisibility(View.VISIBLE);
            mIsFavorite = false;
            Toast.makeText(this, getString(R.string.toast_favorite_removed),
                    Toast.LENGTH_SHORT).show();
        }
        Log.i(LOG_TAG, "[INFO] setFavoriteButton - mIsFavorite = " + mIsFavorite);
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

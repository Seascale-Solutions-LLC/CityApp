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

package com.seascale.cityapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.seascale.cityapp.R;
import com.seascale.cityapp.data.UserPreferences;
import com.seascale.cityapp.models.Business;
import com.seascale.cityapp.models.Filters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.seascale.cityapp.MainActivity.QUERY_LIMIT;

/**
 * Following is a catch-all utility class for offloading non-UI operations from activities and as a
 * repository for keys (e.g. SharedPreferences and Cloud Firestore) used by multiple Activities
 */
public class CommonUtils {

    private static final String LOG_TAG = CommonUtils.class.getSimpleName();

    // Cloud Firestore Keys
    public static final String CF_TOP_COLLECTION_KEY = "cities";
    public static final String CF_SECOND_COLLECTION_KEY = "businesses";
    public static final String CF_CITY_IMAGE_PORT = "cityimageport";
    public static final String CF_CITY_IMAGE_LAND = "cityimageland";
    public static final String CF_CITY_PHOTO_ATTRIBUTION = "cityphotoattribution";

    // Following Key is used for both SharedPreferences and Cloud Firestore
    public static final String CITY_SLOGAN_KEY = "cityslogan";

    /**
     * Helper function to convert a Filters POJO (e.g. from FilterActivity) to a Cloud Firestore
     * database query
     * @param context Context of calling Activity
     * @param city String of city name in the Cloud Firestore second level Collection
     * @param filters Filters POJO
     * @return Firestore Query
     */
    public static Query convertFilterToQuery(Context context, String city, Filters filters) {

        // Get reference to relevant Collection
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference reference = firestore.collection(CF_TOP_COLLECTION_KEY)
                .document(city)
                .collection(CF_SECOND_COLLECTION_KEY);

        // Begin "backwards" by setting the limit in order to initialize the Query
        Query query = reference.limit(QUERY_LIMIT);

        // --- PRESETS ---
        if (filters.hasPresets()) {
            // User lists
            List<String> finalList = processUserList(context, filters);
            Log.i(LOG_TAG, "[INFO] convertFilterToQuery - finalList = " + finalList);
            if (finalList == null) {
                // User requested a search of an empty list (Saved, Favorite, or both)
                return null;
            } else if (!finalList.isEmpty()) {
                // Either Favorites or Saved list (or both) have been selected and contain entries -
                // include in query
                Log.i(LOG_TAG, "[INFO] convertFilterToQuery - !finalList.isEmpty()");
                query = query.whereIn(Business.FIELD_ID, finalList);
            }

            // Check whether user selected special deals
            if (filters.getPresets().contains(context.getString(R.string.filter_preset_special))) {
                Log.i(LOG_TAG, "[INFO] convertFilterToQuery - filters.getPresets().contains(Special Deals)");
                query = query.whereEqualTo(Business.FIELD_CAMPAIGN, true);
            }
        }

        // --- TOP, SECOND, AND TERTIARY FILTERS ---
        if (filters.hasCategory()) {
            Log.i(LOG_TAG, "[INFO] convertFilterToQuery - filters.hasCategory()");
            Map<String, String> filterMap = processIntermediateFilters(context, filters);
            Set<String> filterSet = filterMap.keySet();
            for (String key : filterSet) {
                if (key.equals(Business.FIELD_PRICE)) {
                    Log.i(LOG_TAG, "[INFO] convertFilterToQuery - key = " + key);
                    Log.i(LOG_TAG, "[INFO] convertFilterToQuery - filterMap.get(key) = " + filterMap.get(key));
                    int price = Integer.parseInt(filterMap.get(key));
                    query = query.whereEqualTo(key, price);
                } else {
                    Log.i(LOG_TAG, "[INFO] convertFilterToQuery - key = " + key);
                    Log.i(LOG_TAG, "[INFO] convertFilterToQuery - filterMap.get(key) = " + filterMap.get(key));
                    query = query.whereEqualTo(key, filterMap.get(key));
                }
            }
        }

        // --- LOCATIONS ---
        if (filters.hasLocations()) {
            List<String> locationList = new ArrayList<>();
            locationList.addAll(filters.getLocations());
            Log.i(LOG_TAG, "[INFO] convertFilterToQuery - locationList = " + locationList);
            query = query.whereArrayContainsAny(Business.FIELD_LOCATION, locationList);
        }

        query = query.orderBy(Business.FIELD_CAMPAIGN, Query.Direction.DESCENDING);

        Query.Direction direction = Query.Direction.ASCENDING;
        if (filters.getSortDirection() == Filters.DESCENDING) {
            direction = Query.Direction.DESCENDING;
        }

        query = query.orderBy(filters.getSortBy(), direction);

        return query;
    }

    /**
     * Helper function to process user lists (Favorites and Saved). Method returns a list of all
     * business IDs, without any duplication, of any user favorite or saved businesses, depending
     * on criteria selected by the user via the Filter POJO.
     * @param context - Context of calling Activity
     * @param filters - Filters POJO containing output of FilterActivity
     * @return List<String> of business IDs to apply remaining filters to
     */
    private static List<String> processUserList(Context context, Filters filters) {
        List<String> favorites = new ArrayList<>();
        List<String> saved = new ArrayList<>();
        List<String> workingList = new ArrayList<>();

        if (filters.hasPresets()) {
            List<String> selectedPresets = filters.getPresets();
            Log.i(LOG_TAG, "[INFO] processUserList - selectedPresets = " + selectedPresets);

            // --- FAVORITES ---
            if (selectedPresets.contains(context.getString(R.string.filter_preset_favorites))) {
                // Get list of businesses marked as favorite by the user
                favorites = UserPreferences.getSpFavorites(context);
                if (selectedPresets.size() == 1 && favorites.get(0).equals("")) {
                    // User selected filter ONLY on Favorites, but there are no favorites
                    return null;
                } else {
                    workingList.addAll(favorites);
                }
            }

            // --- SAVED ---
            if (selectedPresets.contains(context.getString(R.string.filter_preset_saved))) {
                saved = UserPreferences.getSpSaved(context);
                if (selectedPresets.size() == 1 && saved.get(0).isEmpty()) {
                    // User selected filter ONLY on Saved, but there are no saved
                    return null;
                } else {
                    workingList.addAll(saved);
                }
            }

            // --- FAVORITES + SAVED ---
            if (selectedPresets.contains(context.getString(R.string.filter_preset_favorites)) &&
                    selectedPresets.contains(context.getString(R.string.filter_preset_saved))) {
                if (favorites.isEmpty() && saved.isEmpty()) {
                    // User selected filter ONLY on Saved, but there are no saved
                    return null;
                }
            }
        }

        // Remove any duplicates and return
        return new ArrayList<>(new HashSet<>(workingList));
    }


    /**
     * Helper function to process the top (category), secondary, and any tertiary (food category
     * only) filters. Method returns any such selections as a Map.
     * @param context - Context of calling Activity
     * @param filters - Filters POJO containing output of FilterActivity
     * @return Map of intermediate filter selections with the keys of the Map corresponding to the
     *         field names in both the Business POJO and Cloud Firestore business Documents
     */
    private static Map<String, String> processIntermediateFilters(Context context, Filters filters) {
        Map<String, String> filterMap = new HashMap<>();

        filterMap.put(Business.FIELD_TOP_FILTER, filters.getCategory());
        // Get sub-category
        if (filters.hasSecondary()) {
            filterMap.put(Business.FIELD_SECOND_FILTER, filters.getSecondary());

            // Get tertiary selections if Food category selected
            if (filters.getCategory().equals(context.getString(R.string.food_category_name))) {
                //if (!filters.getSpecial().equals(context.getString(R.string.filter_prompt_negative))) {
                if (filters.getSpecial() != null) {
                    filterMap.put(Business.FIELD_SPECIAL, filters.getSpecial());
                    Log.i(LOG_TAG, "[INFO] processIntermediateFilters - filterMap.put(" + Business.FIELD_SPECIAL + ", " + filters.getSpecial());
                }
                //if (!filters.getAtmosphere().equals(context.getString(R.string.filter_prompt))) {
                if (filters.getAtmosphere() != null) {
                    filterMap.put(Business.FIELD_TYPE, filters.getAtmosphere());
                    Log.i(LOG_TAG, "[INFO] processIntermediateFilters - filterMap.put(" + Business.FIELD_TYPE + ", " + filters.getAtmosphere());
                }
                if (filters.getPrice() != -1) {
                    filterMap.put(Business.FIELD_PRICE, Integer.toString(filters.getPrice()));
                    Log.i(LOG_TAG, "[INFO] processIntermediateFilters - filterMap.put(" + Business.FIELD_PRICE + ", " + filters.getPrice());
                }
            }
        }
        return filterMap;
    }


    /**
     * Function to notify associated Business Document on Cloud Firestore that a user has either
     * selected the business as a favorite or else de-selected a previously selected business. The
     * Document "favoritetally" field is incremented or decremented by this method as appropriate.
     *
     * This method additionally handles the updating of SharedPreferences.
     *
     * @param context - Context of calling Activity
     * @param setAsFavorite boolean - set true to add as favorite or false to remove as a favorite
     * @param cityName String - name of the city Document in the path to this business
     * @param businessId String - name of the ID for the business selected by the user
     */
    public static void setFavorite(Context context, boolean setAsFavorite, String cityName,
                                   String businessId) {
        // Get the business Document reference on Cloud Firestore
        FirebaseFirestore firestore  = FirebaseFirestore.getInstance();
        DocumentReference document = firestore.collection(CF_TOP_COLLECTION_KEY).document(cityName)
                .collection(CF_SECOND_COLLECTION_KEY).document(businessId);
        if (setAsFavorite) {
            document.update("favoritetally", FieldValue.increment(1));
            UserPreferences.addToSpFavorites(context, businessId);
        } else {
            document.update("favoritetally", FieldValue.increment(-1));
            UserPreferences.removeFromSpFavorites(context, businessId);
        }
    }


    /**
     * For each businessId in the Favorites list stored on SharedPreferences, the associated
     * Document on Cloud Firestore has its "favoritetally" field decremented by one. Then the
     * Favorites are cleared out from the SharedPreferences.
     * @param context Context of the calling Activity
     * @param cityName String of the name of the current city (must correspond to city name in the
     *                 cities collection on Cloud Firestore)
     */
    public static void clearFavorites(Context context, String cityName) {
        // Get list of favorites
        List<String> favorites = UserPreferences.getSpFavorites(context);

        if (favorites.size() == 1 && favorites.get(0).isEmpty()) {
            // No favorites have been saved (nothing to do)
            return;
        }

        for (String item : favorites) {
            // Get the business Document reference on Cloud Firestore
            FirebaseFirestore firestore  = FirebaseFirestore.getInstance();
            DocumentReference document = firestore.collection(CF_TOP_COLLECTION_KEY).document(cityName)
                    .collection(CF_SECOND_COLLECTION_KEY).document(item);
            // Decrement favorite count for each business on Cloud Firestore
            document.update("favoritetally", FieldValue.increment(-1));
        }

        // Finally, clear favorites from local SharedPreferences
        UserPreferences.clearSpFavorites(context);
    }


    /**
     * Simple utility function to read from SharedPreferences the last time the user was notified of
     * new business campaigns, and then return the elapsed time since that time
     * @param context - Context of calling class
     * @return long - The elapsed time, in milliseconds, since the last time the user was notified
     *                of current business campaigns
     */
    public static long getElapsedTimeSinceLastNotification(Context context) {
        long lastNotificationTime = UserPreferences.getLastNotification(context);
        return System.currentTimeMillis() - lastNotificationTime;
    }


    /**
     * Following utility was written with the intention of being accessed by the calling Activity
     * (rather than the ViewModel or Repository since it must have access to Context) as a means of
     * dealing with the deprecated NetworkInfo. The newer libraries, however, do not support APIs
     * lower than 23. As such, the deprecated libraries MUST be used so long as the minSdkVersion
     * is less than 23.
     * The following code was derived in part from the following Stackoverflow discussion:
     * https://stackoverflow.com/questions/32242384/android-getallnetworkinfo-is-deprecated-what-is-the-alternative
     * @param context Application context (required to instantiate the Connectivity Manager)
     * @return boolean - whether a network connection currently exists
     */
    @SuppressWarnings("deprecation")
    public static boolean hasNetworkConnection(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        // Note: VERSION_CODE.M = API 23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                return true;
            }
        } else {
            NetworkInfo[] networks = connectivityManager.getAllNetworkInfo();
            if (networks != null) {
                for (NetworkInfo currentNetwork : networks) {
                    if (currentNetwork.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

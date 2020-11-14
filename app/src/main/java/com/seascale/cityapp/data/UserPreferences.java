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

package com.seascale.cityapp.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.seascale.cityapp.R;
import com.seascale.cityapp.models.Filters;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.seascale.cityapp.utils.CommonUtils.CITY_SLOGAN_KEY;

/**
 * Class dedicated to SharedPreferences interactions. Any access of SharedPreferences should be
 * accomplished through the agency of this class with the exception of Saved and Favorite
 * preferences, which should generally be accessed through CommonUtils (in order to keep the Cloud
 * Firestore database current).
 */
public final class UserPreferences {

    private static final String LOG_TAG = UserPreferences.class.getSimpleName();

    // SharedPreference Keys - General
    public static final String SP_CITY_KEY = "cityname";
    private static final String SP_FAVORITES = "favorites";
    private static final String SP_SAVED = "saved";
    private static final String SP_REMIND_CONNECTIVITY = "remind_connectivity";
    private static final String SP_NOTIFIED_CAMPAIGNS = "notified_campaigns";
    private static final String SP_LAST_NOTIFICATION = "last_notification";

    // SharedPreference Keys - Filter Related
    private static final String SP_FILTER_PRESETS = "filter_presets";
    private static final String SP_FILTER_CATEGORY = "filter_category";
    private static final String SP_FILTER_SECONDARY = "filter_secondary";
    private static final String SP_FILTER_LOCATIONS = "filter_locations";
    private static final String SP_FILTER_SORTBY = "filter_sortby";
    private static final String SP_FILTER_SPECIAL = "filter_special";
    private static final String SP_FILTER_ATMOSPHERE = "filter_atmosphere";
    private static final String SP_FILTER_PRICE = "filter_price";
    private static final String SP_FILTER_SORT_DIRECTION = "filter_sort_direction";

    //**********************************************************************************************
    // GENERAL SHARED PREFERENCES METHODS
    //**********************************************************************************************

    public static String getSpCity(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(SP_CITY_KEY, Context.MODE_PRIVATE);
        return preferences.getString(SP_CITY_KEY, context.getString(R.string.austin_name));
    }


    public static void setSpCity(Context context, String cityName) {
        SharedPreferences preferences = context.getSharedPreferences(SP_CITY_KEY, Context.MODE_PRIVATE);
        preferences.edit().putString(SP_CITY_KEY, cityName).apply();
    }


    public static String getSpCitySlogan(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(CITY_SLOGAN_KEY,
                Context.MODE_PRIVATE);
        return preferences.getString(CITY_SLOGAN_KEY, context.getString(R.string.austin_slogan));
    }


    public static void setSpCitySlogan(Context context, String slogan) {
        SharedPreferences preferences = context.getSharedPreferences(CITY_SLOGAN_KEY,
                Context.MODE_PRIVATE);
        preferences.edit().putString(CITY_SLOGAN_KEY, slogan).apply();
    }


    public static boolean spRemindUser(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(SP_REMIND_CONNECTIVITY,
                Context.MODE_PRIVATE);
        return preferences.getBoolean(SP_REMIND_CONNECTIVITY, true);
    }


    public static void setSpRemindUser(Context context, boolean remindUserAgain) {
        SharedPreferences preferences = context.getSharedPreferences(SP_REMIND_CONNECTIVITY,
                Context.MODE_PRIVATE);
        preferences.edit().putBoolean(SP_REMIND_CONNECTIVITY, remindUserAgain).apply();
    }


    public static List<String> getNotifiedCampaigns(Context context) {
        return getSp(context, SP_NOTIFIED_CAMPAIGNS);
    }


    /**
     * IMPORTANT: businessId argument must assume the business ID from Cloud Firestore!
     */
    public static void addToNotifiedCampaigns(Context context, String businessId) {
        addToSp(context, businessId, SP_NOTIFIED_CAMPAIGNS);
    }


    /**
     * IMPORTANT: businessId argument must assume the business ID from Cloud Firestore!
     */
    public static void removeFromNotifiedCampaigns(Context context, String businessId) {
        removeFromSp(context, businessId, SP_NOTIFIED_CAMPAIGNS);
    }


    public static long getLastNotification(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(SP_LAST_NOTIFICATION,
                Context.MODE_PRIVATE);
        return preferences.getLong(SP_LAST_NOTIFICATION, 0);
    }


    public static void setLastNotification(Context context, long nowInMillis) {
        SharedPreferences preferences = context.getSharedPreferences(SP_LAST_NOTIFICATION,
                Context.MODE_PRIVATE);
        preferences.edit().putLong(SP_LAST_NOTIFICATION, nowInMillis).apply();
    }


    //**********************************************************************************************
    // FILTER RELATED METHODS
    //**********************************************************************************************

    /**
     * Save Filters POJO to SharedPreferences
     * @param context - Context of calling Activity
     * @param filters - Filters POJO
     */
    public static void saveSpFilter(Context context, Filters filters) {
        List<String> listParameter = filters.getPresets();
        writeSpParameter(context, listParameter, SP_FILTER_PRESETS);
        String stringParameter = filters.getCategory();
        writeSpParameter(context, stringParameter, SP_FILTER_CATEGORY);
        stringParameter = filters.getSecondary();
        writeSpParameter(context, stringParameter, SP_FILTER_SECONDARY);
        listParameter = filters.getLocations();
        writeSpParameter(context, listParameter, SP_FILTER_LOCATIONS);
        stringParameter = filters.getSortBy();
        writeSpParameter(context, stringParameter, SP_FILTER_SORTBY);
        stringParameter = filters.getSpecial();
        writeSpParameter(context, stringParameter, SP_FILTER_SPECIAL);
        stringParameter = filters.getAtmosphere();
        writeSpParameter(context, stringParameter, SP_FILTER_ATMOSPHERE);
        int intParameter = filters.getPrice();
        writeSpParameter(context, intParameter, SP_FILTER_PRICE);
        intParameter = filters.getSortDirection();
        writeSpParameter(context, intParameter, SP_FILTER_SORT_DIRECTION);
    }

    private static void writeSpParameter(Context context, List<String> values, String parameterKey) {
        StringBuilder stringValues = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            stringValues.append(values.get(i));
            if (i != (values.size() - 1)) {
                stringValues.append(";");
            }
        }
        writeSpParameter(context, stringValues.toString(), parameterKey);
    }

    private static void writeSpParameter(Context context, String value, String parameterKey) {
        SharedPreferences preferences = context.getSharedPreferences(parameterKey,
                Context.MODE_PRIVATE);
        preferences.edit().putString(parameterKey, value).apply();
    }

    private static void writeSpParameter(Context context, int value, String parameterKey) {
        SharedPreferences preferences = context.getSharedPreferences(parameterKey,
                Context.MODE_PRIVATE);
        preferences.edit().putInt(parameterKey, value).apply();
    }

    /**
     * Recalls a Filters POJO from SharedPreferences
     * @param context - Context of calling Activity
     * @return Filters POJO
     */
    public static Filters recallSpFilter(Context context) {
        Filters filters = new Filters();
        filters.setPresets(getSp(context, SP_FILTER_PRESETS));
        filters.setCategory(readSpParameter(context, SP_FILTER_CATEGORY));
        filters.setSecondary(readSpParameter(context, SP_FILTER_SECONDARY));
        filters.setLocations(getSp(context, SP_FILTER_LOCATIONS));
        filters.setSortBy(readSpParameter(context, SP_FILTER_SORTBY));
        filters.setSpecial(readSpParameter(context, SP_FILTER_SPECIAL));
        filters.setAtmosphere(readSpParameter(context, SP_FILTER_ATMOSPHERE));
        filters.setPrice(readSpIntParameter(context, SP_FILTER_PRICE));
        filters.setSortDirection(readSpIntParameter(context, SP_FILTER_SORT_DIRECTION));
        return filters;
    }

    private static String readSpParameter(Context context, String parameterKey) {
        SharedPreferences preferences = context.getSharedPreferences(parameterKey,
                Context.MODE_PRIVATE);
        return preferences.getString(parameterKey, "");
    }

    private static int readSpIntParameter(Context context, String parameterKey) {
        SharedPreferences preferences = context.getSharedPreferences(parameterKey,
                Context.MODE_PRIVATE);
        return preferences.getInt(parameterKey, -1);
    }

    //**********************************************************************************************
    // "FAVORITE" RELATED METHODS
    //**********************************************************************************************

    /**
     * IMPORTANT: businessId argument must assume the business ID from Cloud Firestore!
     */
    public static void addToSpFavorites(Context context, String businessId) {
        addToSp(context, businessId, SP_FAVORITES);
    }

    /**
     * IMPORTANT: businessId argument must assume the business ID from Cloud Firestore!
     */
    public static void removeFromSpFavorites(Context context, String businessId) {
        removeFromSp(context, businessId, SP_FAVORITES);
    }

    /**
     * IMPORTANT: businessId argument must assume the business ID from Cloud Firestore!
     */
    public static boolean isFavorite(Context context, String businessId) {
        List<String> favorites = getSp(context, SP_FAVORITES);
        return favorites.contains(businessId);
    }

    public static List<String> getSpFavorites(Context context) {
        return getSp(context, SP_FAVORITES);
    }

    public static void clearSpFavorites(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(SP_FAVORITES,
                Context.MODE_PRIVATE);
        preferences.edit().putString(SP_FAVORITES, "").apply();
    }

    //**********************************************************************************************
    // "SAVE" RELATED METHODS
    //**********************************************************************************************

    /**
     * IMPORTANT: businessId argument must assume the business ID from Cloud Firestore!
     */
    public static void addToSpSaved(Context context, String businessId) {
        addToSp(context, businessId, SP_SAVED);
    }

    /**
     * IMPORTANT: businessId argument must assume the business ID from Cloud Firestore!
     */
    public static void removeFromSpSaved(Context context, String businessId) {
        removeFromSp(context, businessId, SP_SAVED);
    }

    /**
     * IMPORTANT: businessId argument must assume the business ID from Cloud Firestore!
     */
    public static boolean isSaved(Context context, String businessId) {
        List<String> saved = getSp(context, SP_SAVED);
        return saved.contains(businessId);
    }

    public static List<String> getSpSaved(Context context) {
        return getSp(context, SP_SAVED);
    }

    public static void clearSpSaved(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(SP_SAVED,
                Context.MODE_PRIVATE);
        preferences.edit().putString(SP_SAVED, "").apply();
    }

    //**********************************************************************************************
    // "FAVORITE" AND "SAVE" HELPER METHODS
    //**********************************************************************************************

    private static void addToSp(Context context, String businessId, String parameterKey) {
        // Make sure entry is not somehow already included
        List<String> list = getSp(context, parameterKey);
        if (list.contains(businessId)) {
            Log.e(LOG_TAG, "[ERROR] addToSp - attempt to add duplicate entry");
            return;
        }

        SharedPreferences preferences = context.getSharedPreferences(parameterKey,
                Context.MODE_PRIVATE);
        // Append new entry and apply to SharedPreferences
        String value = preferences.getString(parameterKey, "");
        if (value.isEmpty()) {
            value = businessId;
        } else {
            value += ";" + businessId;
        }
        preferences.edit().putString(parameterKey, value).apply();
    }


    private static void removeFromSp(Context context, String businessId, String parameterKey) {
        SharedPreferences preferences = context.getSharedPreferences(parameterKey,
                Context.MODE_PRIVATE);
        // Favorites list is stored as a single, semi-colon delimited string - convert to an Array
        List<String> list = getSp(context, parameterKey);
        int index = list.indexOf(businessId);
        if (index != -1) {
            list.remove(index);
        }
        // Reconstruct string to re-write to SharedPreferences
        StringBuilder newList = new StringBuilder();
        for (index = 0; index < list.size(); index++) {
            if (index == 0) {
                newList.append(list.get(index));
            } else {
                newList.append(";").append(list.get(index));
            }
        }
        // Commit updated list to SharedPreferences
        preferences.edit().putString(parameterKey, newList.toString()).apply();
    }

    private static List<String> getSp(Context context, String parameterKey) {
        SharedPreferences preferences = context.getSharedPreferences(parameterKey,
                Context.MODE_PRIVATE);
        // Lists are stored as a single, semi-colon delimited string - convert to an Array
        String[] array = preferences.getString(parameterKey, "").split(";");
        // Convert Array to LinkedList<> (required in order to modify list (e.g. in
        // removeFromSp()). A plain List<String> is a fixed-sized list and cannot be modified
        return new LinkedList<>(Arrays.asList(array));
    }
}

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

package com.seascale.cityapp.workers;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.seascale.cityapp.data.UserPreferences;
import com.seascale.cityapp.models.Business;
import com.seascale.cityapp.utils.CommonUtils;
import com.seascale.cityapp.utils.NotificationUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.seascale.cityapp.utils.CommonUtils.CF_SECOND_COLLECTION_KEY;
import static com.seascale.cityapp.utils.CommonUtils.CF_TOP_COLLECTION_KEY;

/**
 * This class contains the code to sync the business campaign data on Cloud Firestore with the
 * businesses selected as favorites by the user
 */
public class CityAppSyncWorker extends Worker {

    private static final String LOG_TAG = CityAppSyncWorker.class.getSimpleName();

    // Constructor
    public CityAppSyncWorker(@NonNull Context context, @NonNull WorkerParameters parameters) {
        super(context, parameters);
    }


    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        long timeSinceLastNotification = CommonUtils.getElapsedTimeSinceLastNotification(context);
        // List of all business IDs that have active campaigns as of now
        final List<String> businessesWithCampaigns = new ArrayList<>();
        // Map of all business names that have active campaigns as of now, accessible by business ID
        final Map<String, String> businessNames = new HashMap<>();
        // Map of all active campaign descriptions, accessible by business ID
        final Map<String, String> campaignDescriptions = new HashMap<>();

        // 1) Get list of businessIds where campaign=true. Store the business name and campaign
        //    description so it can be displayed in a notification if appropriate.
        String cityName = UserPreferences.getSpCity(context);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(CF_TOP_COLLECTION_KEY)
                .document(cityName)
                .collection(CF_SECOND_COLLECTION_KEY)
                .whereEqualTo(Business.FIELD_CAMPAIGN, true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if ((boolean) document.get(Business.FIELD_CAMPAIGN)) {
                                    String businessId = (String) document.get(Business.FIELD_ID);
                                    businessesWithCampaigns.add(businessId);
                                    businessNames.put(businessId, (String) document
                                            .get(Business.FIELD_NAME));
                                    campaignDescriptions.put(businessId, (String) document
                                            .get(Business.FIELD_CAMPAIGN_DESCRIPTION));
                                }
                            }
                        } else {
                            Log.e(LOG_TAG, "[ERROR] doWork - Error getting documents: ",
                                    task.getException());
                        }
                    }
                });

        // 2)
        //   a) Check if any of the businessIds in the current campaign list
        //      (businessesWithCampaigns) match the user's favorites list stored in SharedPreferences
        //   b) Next, check if the user has already been notified by comparing the
        //      businessesWithCampaigns list with the list of user's favorites (favoritesList)
        List<String> favoritesList = UserPreferences.getSpFavorites(context);
        Set favoritesSet = new HashSet<>(favoritesList);
        List<String> notifiedList = UserPreferences.getNotifiedCampaigns(context);
        Set notifiedSet = new HashSet<>(notifiedList);
        // Map containing only those active campaigns where the user has not already been notified.
        // Unlike the other Maps in this method the business names (rather than ID) are used as the
        // keys.
        final Map<String, String> campaigns = new HashMap<>();

        int numNewCampaigns = 0;
        for (int i = 0; i < businessesWithCampaigns.size(); i++) {
            String currentBusinessId = businessesWithCampaigns.get(i);
            // Check whether business is a user favorite and, if so, whether user has already been
            // notified of this campaign
            if (favoritesSet.contains(currentBusinessId) && !notifiedSet.contains(currentBusinessId)) {
                // This is a new campaign (i.e. one for which the user has not been notified). Add
                // to the tally and "latch" the notification by adding to the notified List
                numNewCampaigns++;
                UserPreferences.addToNotifiedCampaigns(context, currentBusinessId);
                // Add to the Map that will be used to construct the expanded form of the notification
                campaigns.put(businessNames.get(currentBusinessId),
                        campaignDescriptions.get(currentBusinessId));
            }
        }

        // Send out the notification if appropriate
        if (numNewCampaigns > 0 && timeSinceLastNotification >= DateUtils.DAY_IN_MILLIS) {
            NotificationUtils.notifyOfNewCampaigns(context, campaigns);
        }

        // 4) Finally, see if there are any items in the notifiedList that do not appear in
        //    the businessesWithCampaigns list. Any such occurrences indicate an expired campaign
        //    and the businessId needs to be "reset" by removing it from the notifiedList. In this
        //    way, the next time that business has a campaign the user will be notified.
        Set campaignsSet = new HashSet<>(businessesWithCampaigns);
        for (int i = 0; i < notifiedList.size(); i++) {
            String campaignBusinessId = notifiedList.get(i);
            if (!campaignsSet.contains(campaignBusinessId)) {
                UserPreferences.removeFromNotifiedCampaigns(context, campaignBusinessId);
            }
        }

        return Result.success();
    }
}

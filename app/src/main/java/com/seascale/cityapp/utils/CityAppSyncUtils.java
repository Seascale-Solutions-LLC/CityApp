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
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.seascale.cityapp.sync.CityAppSyncIntentService;
import com.seascale.cityapp.workers.CityAppSyncWorker;

import java.util.concurrent.TimeUnit;

public class CityAppSyncUtils {

    // Interval at which to sync with Cloud Firestore regarding campaigns.
    private static final int SYNC_INTERVAL_HOURS = 24;
    private static final  int SYNC_INTERVAL_SECONDS = (int) TimeUnit.HOURS.toSeconds(SYNC_INTERVAL_HOURS);
    private static final int SYNC_FLEXTIME_SECONDS = SYNC_INTERVAL_SECONDS / 24;

    private static final String CITYAPP_SYNC_TAG = "cityapp_sync_tag";

    private static boolean sInitialized;


    /**
     * Method for forcing an immediate sync using an IntentService for asynchronous execution (used,
     * for example, upon initial install of the app on a device)
     * @param context - Context of calling class
     */
    public static void startImmediateSync(@NonNull final Context context) {
        Intent intent = new Intent(context, CityAppSyncIntentService.class);
        context.startService(intent);
    }


    /**
     * Schedule a repeating sync of business campaign data with Cloud Firestore using WorkManager
     * @param context - Context of the calling class
     */
    public static void scheduleWorkManagerSync(@NonNull final Context context) {
        // To save user money, do not sync while roaming
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_ROAMING)
                .build();

        // Create a periodic work request which will execute roughly once every 24 hours
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(CityAppSyncWorker.class,
                SYNC_INTERVAL_HOURS,
                TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();

        // Schedule the work
        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(CITYAPP_SYNC_TAG,
                        // If a job with this tag already exists, then replace with this new one
                        ExistingPeriodicWorkPolicy.REPLACE,
                        workRequest);
    }


    synchronized public static void initialize(@NonNull final Context context) {
        // Only perform initialization once. If this has already been performed then nothing remains
        // to be done here.
        if (sInitialized) {
            return;
        }

        sInitialized = true;

        // Create the task to periodically synchronize business campaign data
        scheduleWorkManagerSync(context);

        // This is where we would check if there are currently any campaigns to display and
        // start an immediate synch if needed. However since this whole method is something
        // only called once upon app installation, there will be no favorite businesses saved
        // off at this point with which to compare. The synch with Firestore will occur as a
        // matter of course when the RecyclerView is first populated.
        //startImmediateSync();
    }
}

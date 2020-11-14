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

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.seascale.cityapp.MainActivity;
import com.seascale.cityapp.R;
import com.seascale.cityapp.data.UserPreferences;
import com.seascale.cityapp.models.Filters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.seascale.cityapp.MainActivity.FILTER_STATE;

public class NotificationUtils {

    // Notification ID to enable access to the notification after it has been displayed
    private static final int CAMPAIGN_NOTIFICATION_ID = 1001;

    private static final String CAMPAIGN_NOTIFICATION_CHANNEL_ID = "campaign_notification_channel";

    /**
     * Method for constructing and displaying notifications of business campaigns for those
     * businesses marked by the user as one of their favorites
     * @param context - Context of the calling class
     * @param campaigns - Map<String, String> containing the business name (keys) and campaign
     *                  descriptions (values) of the new campaigns for which the user needs to be
     *                  notified
     */
    public static void notifyOfNewCampaigns(Context context, Map<String, String> campaigns) {
        Resources resources = context.getResources();
        int numOffers = campaigns.size();
        Bitmap notificationIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_notification);
        String notificationTitle;
        String notificationText;
        if (numOffers == 1) {
            notificationTitle = resources.getString(R.string.notification_title_one);
            notificationText = resources.getString(R.string.notification_text_one);
        } else {
            notificationTitle = resources.getString(R.string.notification_title_more);
            notificationText = resources.getString(R.string.notification_text_more, numOffers);
        }

        String details = "";
        int numEntries = 0;
        for (Map.Entry<String, String> entry : campaigns.entrySet()) {
            details = details.concat(entry.getKey() + ": " + entry.getValue() + "\n");
            numEntries++;
            // The notification in expanded form can convey at most two campaigns
            if (numEntries > 1) break;
        }

        // If there are more than two campaigns of which the user needs to be notified, then notify
        // of the number of additional campaigns not being shown in the expanded notification
        if (numOffers == 3) {
            details = details.concat(resources.getString(R.string.notification_one_more));
        } else if (numOffers > 3){
            details = details.concat(resources.getString(R.string.notification_more_campaigns,
                    (numOffers - 2)));
        }

        // Build the notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context,
                CAMPAIGN_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_accent_24dp)
                .setLargeIcon(notificationIcon)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(details))
                .setAutoCancel(true);

        // Following Intent will launch the MainActivity when the user clicks on the notification
        Intent intent  = new Intent(context, MainActivity.class);
        // Add a Filters POJO as a payload. This filters POJO should launch CityApp with a query
        // showing all favorites with campaigns (not just those for which the user has not already
        // been notified).
        Filters filters = Filters.getDefault();
        List<String> presets = new ArrayList<>();
        presets.add(resources.getString(R.string.filter_preset_favorites));
        presets.add(resources.getString(R.string.filter_preset_special));
        filters.setPresets(presets);
        intent.putExtra(FILTER_STATE, filters);

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CAMPAIGN_NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.main_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // Trigger the notification
        notificationManager.notify(CAMPAIGN_NOTIFICATION_ID, notificationBuilder.build());

        // Since notification was just launched, reset the timer
        UserPreferences.setLastNotification(context, System.currentTimeMillis());
    }
}

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

package com.seascale.cityapp.viewmodels;

import androidx.lifecycle.ViewModel;

import com.seascale.cityapp.models.Filters;

public class MainActivityViewModel extends ViewModel {

    private boolean mNotifyUserOfConnectivity;
    private boolean mShowEmptyView;

    private String mCityPicturePort;
    private String mCityPictureLand;
    private String mCityAttribution;

    private Filters mFilters;

    public MainActivityViewModel() {
        // Always enable network connectivity notice in the ViewModel initially. This acts as a
        // latch and after the first notification is set to false in the calling activity so the
        // user is not repeatedly reminded during a particular usage when, for example, changing
        // device orientation. Notice this mechanism does not kick in when the user navigates back
        // to the MainActivity from another Activity since the ViewModel is destroyed and then
        // re-created.
        mNotifyUserOfConnectivity = true;
        mShowEmptyView = false;
    }

    public void setNotifyUser(boolean notify) { mNotifyUserOfConnectivity = notify; }
    public boolean getNotifyUser() { return mNotifyUserOfConnectivity; }

    public void setCityPictures(String portPicture, String landPicture) {
        mCityPicturePort = portPicture;
        mCityPictureLand = landPicture;
    }

    public String[] getCityPictures() {
        String[] pictureUrlStrings = {mCityPicturePort, mCityPictureLand};
        return pictureUrlStrings;
    }

    public void setCityAttribution(String attribution) { mCityAttribution = attribution; }
    public String getCityAttribution() { return mCityAttribution; }

    public void setCurrentFilter(Filters filters) { mFilters = filters; }
    public Filters getCurrentFilter() { return mFilters; }

    public void setShowEmptyView(boolean showEmptyView) { mShowEmptyView = showEmptyView; }
    public boolean showEmptyView() { return mShowEmptyView; }
}

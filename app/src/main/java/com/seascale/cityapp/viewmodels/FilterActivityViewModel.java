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

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.seascale.cityapp.models.Filters;

/**
 * ViewModel for FilterActivity - used to cache a model of the user's Filters object
 */
public class FilterActivityViewModel extends ViewModel {

    private Filters mFilters;

    public FilterActivityViewModel() {
        mFilters = Filters.getDefault();
    }

    public Filters getFilters() {
        return mFilters;
    }

    public void setFilters(Filters filters) {
        this.mFilters = filters;
    }

    /**
     * Call to this function sets in cache only those parameters represented by checkboxes
     * @param filters Filters object with only the presets and locations set (any other data will
     *                be ignored)
     */
    public void setCheckboxes(Filters filters) {
        mFilters.setPresets(filters.getPresets());
        mFilters.setLocations(filters.getLocations());
    }

    /**
     * Call to this function sets in cache the state of the category filter
     * @param category String of category selected by user
     */
    public void setCategory(String category) {
        mFilters.setCategory(category);
    }

    /**
     * Call to this function sets in cache the state of the secondary filter
     * @param secondaryFilter String of secondary filter selected by the user
     */
    public void setSecondaryFilter(String secondaryFilter) {
        mFilters.setSecondary(secondaryFilter);
    }

    /**
     * Call to this function sets in cache the state of the special considerations filter
     * @param special String of the special consideration selected by the user
     */
    public void setSpecialFilter(String special) {
        mFilters.setSpecial(special);
    }

    /**
     * Call to this function sets in cache the state of the atmosphere filter
     * @param atmosphere String of the atmosphere selected by the user
     */
    public void setAtmosphereFilter(String atmosphere) {
        mFilters.setAtmosphere(atmosphere);
    }

    /**
     * Call to this function sets in cache the state of the price filter
     * @param price integer code pertaining to the price range selected by the user
     */
    public void setPriceFilter(int price) {
        mFilters.setPrice(price);
    }

    /**
     * Call to this function sets in cache the sort direction (ascending or descending) preference
     * @param sortBy Query.Direction object indicating either ASCENDING or DESCENDING
     */
    public void setSortDirection(int sortBy) {
        mFilters.setSortDirection(sortBy);
    }
}

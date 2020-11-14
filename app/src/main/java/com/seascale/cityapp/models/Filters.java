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

package com.seascale.cityapp.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO for easily working with filters between classes
 */
public class Filters implements Parcelable {

    private List<String> presets = new ArrayList<>();
    private String category = null;
    private String secondary = null;
    private List<String> locations = new ArrayList<>();
    private String sortBy = null;
    private String special = null;
    private String atmosphere = null;
    private int price = -1;
    private int sortDirection = -1;

    // In order to pass Filters POJOs between Activities, sort direction is stored as a primitive
    // here in order for the POJO to be passed as a Parcelable. The receiving activity then converts
    // the POJO to a Firestore Query. The following constants aid in this conversion.
    public static final int ASCENDING = 0;
    public static final int DESCENDING = 1;

    // Constructor
    public Filters() {}

    public static Filters getDefault() {
        Filters filters = new Filters();
        filters.setSortBy(Business.FIELD_NAME);
        filters.setSortDirection(0);
        return filters;
    }

    public boolean hasPresets() {
        if (presets == null || presets.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }
    public boolean hasCategory() { return !(TextUtils.isEmpty(category)); }
    public boolean hasSecondary() { return !(TextUtils.isEmpty(secondary)); }
    public boolean hasLocations() {
        if (locations == null || locations.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }
    public boolean hasSortBy() { return !(TextUtils.isEmpty(sortBy)); }
    public boolean hasSpecial() { return !(TextUtils.isEmpty(special)); }
    public boolean hasAtmosphere() { return !(TextUtils.isEmpty(atmosphere)); }
    public boolean hasPrice() { return (price > 0); }

    public List<String> getPresets() { return presets; }
    public void setPresets(List<String> presets) { this.presets = presets; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSecondary() { return  secondary; }
    public void setSecondary(String secondary) { this.secondary = secondary; }

    public List<String> getLocations() { return locations; }
    public void setLocations(List<String> locations) { this.locations = locations; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getSpecial() { return special; }
    public void setSpecial(String special) { this.special = special; }

    public String getAtmosphere() { return atmosphere; }
    public void setAtmosphere(String atmosphere) { this.atmosphere = atmosphere; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public int getSortDirection() { return sortDirection; }
    public void setSortDirection(int sortDirection) {
        this.sortDirection = sortDirection;
    }

    //===================================================
    // Parcelable Related Methods
    //===================================================

    protected Filters(Parcel in) {
        presets = new ArrayList<>();
        in.readStringList(presets);
        category = in.readString();
        secondary = in.readString();
        locations = new ArrayList<>();
        in.readStringList(locations);
        sortBy = in.readString();
        special = in.readString();
        atmosphere = in.readString();
        price = in.readInt();
        sortDirection = in.readInt();
    }

    public static final Creator<Filters> CREATOR = new Creator<Filters>() {
        @Override
        public Filters createFromParcel(Parcel source) {
            return new Filters(source);
        }

        @Override
        public Filters[] newArray(int size) {
            return new Filters[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(presets);
        dest.writeString(category);
        dest.writeString(secondary);
        dest.writeStringList(locations);
        dest.writeString(sortBy);
        dest.writeString(special);
        dest.writeString(atmosphere);
        dest.writeInt(price);
        dest.writeInt(sortDirection);
    }
}

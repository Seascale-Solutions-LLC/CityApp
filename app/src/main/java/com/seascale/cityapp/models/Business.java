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

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Business POJO
 */
@IgnoreExtraProperties
public class Business implements Parcelable {

    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_IMAGE_ID = "imageid";
    public static final String FIELD_DESCRPITION = "description";
    public static final String FIELD_HOURS = "hours";
    public static final String FIELD_WEB_URL = "url";
    public static final String FIELD_PHONE = "phone";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_ADDRESS = "address";
    public static final String FIELD_LOCATION = "location";
    public static final String FIELD_COORDINATES = "coordinates";
    public static final String FIELD_TOP_FILTER = "topfilter";
    public static final String FIELD_SECOND_FILTER = "secondfilter";
    public static final String FIELD_SPECIAL = "special";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_PRICE = "price";
    public static final String FIELD_AWARDS = "awards";
    public static final String FIELD_CAMPAIGN = "campaign";
    public static final String FIELD_CAMPAIGN_DESCRIPTION = "campaigndescription";
    public static final String FIELD_CAMPAIGN_START = "campaignstart";
    public static final String FIELD_CAMPAIGN_END = "campaignend";
    public static final String FIELD_TAGS = "tags";

    // Fields exclusive to client
    public static final String FIELD_FAVORITE = "favorite";
    public static final String FIELD_SAVED = "saved";

    private String id;
    private String name;
    private String imageid;
    private String description;
    private List<String> hours;
    private String url;
    private List<String> phone;
    private String email;
    private List<String> address;
    private List<String> location;
    private List<String> coordinates;
    private String topfilter;
    private String secondfilter;
    private String special;
    private String type;
    private int price;
    private List<String> awards;
    private boolean campaign;
    private String campaigndescription;
    private long campaignstart;
    private long campaignend;
    private List<String> tags;
    private boolean favorite;
    private boolean saved;

    /**
     * Empty constructor is required in order to retrieve items from Cloud Firestore as Business
     * Objects
     */
    public Business() {}

    /**
     * Regular constructor for Business Objects
     */
    public Business(String id, String name, String imageid, String description, List<String> hours,
                    String url, List<String> phone, String email, List<String> address,
                    List<String> location, List<String> coordinates, String topfilter,
                    String secondfilter, String special, String type, int price,
                    List<String> awards, boolean campaign, String campaigndescription,
                    long campaignstart, long campaignend, List<String> tags, boolean favorite,
                    boolean saved) {
        this.id = id;
        this.name = name;
        this.imageid = imageid;
        this.description = description;
        this.hours = hours;
        this.url = url;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.location = location;
        this.coordinates = coordinates;
        this.topfilter = topfilter;
        this.secondfilter = secondfilter;
        this.special = special;
        this.type = type;
        this.price = price;
        this.awards = awards;
        this.campaign = campaign;
        this.campaigndescription = campaigndescription;
        this.campaignstart = campaignstart;
        this.campaignend = campaignend;
        this.tags = tags;
        this.favorite = favorite;
        this.saved = saved;
    }

    public void setId(String id) { this.id = id; }
    public String getId() { return id; }

    public void setName(String name) { this.name = name; }
    public String getName() { return name; }

    public void setImageId(String imageid) {this.imageid = imageid; }
    public String getImageId() { return imageid; }

    public void setDescription(String description) { this.description = description; }
    public String getDescription() { return description; }

    public void setHours(List<String> hours) { this.hours = hours; }
    public List<String> getHours() { return hours; }

    public void setUrl(String url) { this.url = url; }
    public String getUrl() { return url; }

    public void setPhone(List<String> phone) { this.phone = phone; }
    public List<String> getPhone() { return phone; }

    public void setEmail(String email) { this.email = email; }
    public String getEmail() { return email; }

    public void setAddress(List<String> address) { this.address = address; }
    public List<String> getAddress() { return address; }

    public void setLocation(List<String> location) { this.location = location; }
    public List<String> getLocation() { return location; }

    public void setCoordinates(List<String> coordinates) { this.coordinates = coordinates; }
    public List<String> getCoordinates() { return coordinates; }

    public void setTopFilter(String topfilter) { this.topfilter = topfilter; }
    public String getTopFilter() { return topfilter; }

    public void setSecondFilter(String secondfilter) { this.secondfilter = secondfilter; }
    public String getSecondFilter() { return secondfilter; }

    public void setSpecial(String special) { this.special = special; }
    public String getSpecial() { return special; }

    public void setType(String type) { this.type = type; }
    public String getType() { return  type; }

    public void setPrice(int price) { this.price = price; }
    public int getPrice() { return price; }

    public void setAwards(List<String> awards) { this.awards = awards; }
    public List<String> getAwards() { return awards; }

    public void setCampaign(boolean campaign) { this.campaign = campaign; }
    public boolean getCampaign() { return campaign; }

    public void setCampaignDescription(String campaigndescription) {
        this.campaigndescription = campaigndescription;
    }
    public String getCampaignDescription() { return campaigndescription; }

    public void setCampaignStart(long campaignstart) { this.campaignstart = campaignstart; }
    public long getCampaignStart() { return campaignstart; }

    public void setCampaignEnd(long campaignend) { this.campaignend = campaignend; }
    public long getCampaignEnd() { return  campaignend; }

    public void setTags(List<String> tags) { this.tags = tags; }
    public List<String> getTags() { return tags; }

    public void setFavorite(boolean favorite) { this.favorite = favorite; }
    public boolean getFavorite() { return favorite; }

    public void setSaved(boolean saved) { this.saved = saved; }
    public boolean getSaved() { return  saved; }

    //===================================================
    // Parcelable Related Methods
    //===================================================

    protected Business(Parcel in) {
        id = in.readString();
        name = in.readString();
        imageid = in.readString();
        description = in.readString();
        hours = new ArrayList<>();
        in.readStringList(hours);
        url = in.readString();
        phone = new ArrayList<>();
        in.readStringList(phone);
        email = in.readString();
        address = new ArrayList<>();
        in.readStringList(address);
        location = new ArrayList<>();
        in.readStringList(location);
        coordinates = new ArrayList<>();
        in.readStringList(coordinates);
        topfilter = in.readString();
        secondfilter = in.readString();
        special = in.readString();
        type = in.readString();
        price = in.readInt();
        awards = new ArrayList<>();
        in.readStringList(awards);
        campaigndescription = in.readString();
        campaignstart = in.readLong();
        campaignend = in.readLong();
        tags = new ArrayList<>();
        in.readStringList(tags);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            campaign = in.readBoolean();
            favorite = in.readBoolean();
            saved = in.readBoolean();
        } else {
            campaign = in.readByte() != 0;
            favorite = in.readByte() != 0;
            saved = in.readByte() != 0;
        }
    }

    public static final Parcelable.Creator<Business> CREATOR = new Parcelable.Creator<Business>() {
        @Override
        public Business createFromParcel(Parcel source) {
            return new Business(source);
        }

        @Override
        public Business[] newArray(int size) {
            return new Business[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(imageid);
        dest.writeString(description);
        dest.writeStringList(hours);
        dest.writeString(url);
        dest.writeStringList(phone);
        dest.writeString(email);
        dest.writeStringList(address);
        dest.writeStringList(location);
        dest.writeStringList(coordinates);
        dest.writeString(topfilter);
        dest.writeString(secondfilter);
        dest.writeString(special);
        dest.writeString(type);
        dest.writeInt(price);
        dest.writeStringList(awards);
        dest.writeString(campaigndescription);
        dest.writeLong(campaignstart);
        dest.writeLong(campaignend);
        dest.writeStringList(tags);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            dest.writeBoolean(campaign);
            dest.writeBoolean(favorite);
            dest.writeBoolean(saved);
        } else {
            dest.writeByte((byte) (campaign ? 1 : 0));
            dest.writeByte((byte) (favorite ? 1 : 0));
            dest.writeByte((byte) (saved ? 1 : 0));
        }
    }
}

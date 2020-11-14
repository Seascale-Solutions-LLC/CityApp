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

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import static com.seascale.cityapp.data.UserPreferences.SP_CITY_KEY;

public class CitySelectDialogFragment extends DialogFragment {

    private Handler mHandler;

    public CitySelectDialogFragment(Handler handler) {
        mHandler = handler;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Construct dialog for selecting city
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_fragment_city_select_prompt)
                .setItems(R.array.dialog_city_list, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int index) {
                        // Bundle the index into the dialog_city_list string array and pass back to
                        // calling Activity (MainActivity)
                        Bundle bundle = new Bundle();
                        bundle.putInt(SP_CITY_KEY, index);
                        Message msg = new Message();
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
                    }
                });
        return builder.create();
    }
}

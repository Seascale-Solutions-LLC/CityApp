/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * NOTICE: Modifications have been made to the original to support logical OR queries of non-array
 * fields, accomplished by two sequential queries. The modifications required to support this
 * feature is overloading of the setQuery method, modifications to the onEvent callback, and
 * addition of associated member variables (mQuery2, and mHold).
 */

package com.seascale.cityapp.adapters;

import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * RecyclerView adapter for displaying the results of a Firestore {@link Query}.
 *
 * Note that this class forgoes some efficiency to gain simplicity. For example, the result of
 * {@link DocumentSnapshot#toObject(Class)} is not cached so the same object may be deserialized
 * many times as the user scrolls.
 */
public abstract class FirestoreAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH>
        implements EventListener<QuerySnapshot> {

    private static final String TAG = "FirestoreAdapter";

    private Query mQuery;
    private Query mQuery2;
    private ListenerRegistration mRegistration;

    // Latch for logical OR queries of Cloud Firestore (not supported natively for non-array fields)
    // Variable is used to wait for results of second sequential query before notifying of results
    // availability.
    private boolean mHold;

    private ArrayList<DocumentSnapshot> mSnapshots = new ArrayList<>();

    public FirestoreAdapter(Query query) {
        mQuery = query;
    }


    @Override
    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(TAG, "onEvent:error", e);
            onError(e);
            return;
        }

        // Dispatch the event
        Log.d(TAG, "onEvent:numChanges:" + documentSnapshots.getDocumentChanges().size());
        for (DocumentChange change : documentSnapshots.getDocumentChanges()) {
            switch (change.getType()) {
                case ADDED:
                    onDocumentAdded(change);
                    break;
                case MODIFIED:
                    onDocumentModified(change);
                    break;
                case REMOVED:
                    onDocumentRemoved(change);
                    break;
            }
        }

        if (!mHold) {
            // Either this is a single query, or the second of two queries composing a logical OR,
            // so there is no need to wait
            onDataChanged();
        } else {
            // Theses are the first results of a two-query logical OR - initiate the second query
            // and await the results.
            if (mRegistration != null) {
                mRegistration.remove();
                mRegistration = null;
            }
            mQuery = mQuery2;
            startListening();
            // Release the "latch"
            mHold = false;
        }
    }

    public void startListening() {
        if (mQuery != null && mRegistration == null) {
            mRegistration = mQuery.addSnapshotListener(this);
        }
    }

    public void stopListening() {
        if (mRegistration != null) {
            mRegistration.remove();
            mRegistration = null;
        }

        mSnapshots.clear();
        notifyDataSetChanged();
    }

    public void setQuery(Query query) {
        mHold = false;

        // Stop listening
        stopListening();

        // Clear existing data
        mSnapshots.clear();
        notifyDataSetChanged();

        // Listen to new query
        mQuery = query;
        startListening();
    }

    public void setQuery(Query query1, Query query2) {
        // IMPORTANT NOTE: If using this overloaded version of the setQuery method to perform a
        // logical OR query, the results may include duplicates. It is therefore incumbent upon the
        // calling method to perform the necessary dedup operation.
        mHold = true;
        mQuery2 = query2;

        // Stop listening
        stopListening();

        // Clear existing data
        mSnapshots.clear();
        notifyDataSetChanged();

        // Listen to new query
        mQuery = query1;
        startListening();
    }

    @Override
    public int getItemCount() {
        return mSnapshots.size();
    }

    protected DocumentSnapshot getSnapshot(int index) {
        return mSnapshots.get(index);
    }

    protected void onDocumentAdded(DocumentChange change) {
        mSnapshots.add(change.getNewIndex(), change.getDocument());
        notifyItemInserted(change.getNewIndex());
    }

    protected void onDocumentModified(DocumentChange change) {
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            mSnapshots.set(change.getOldIndex(), change.getDocument());
            notifyItemChanged(change.getOldIndex());
        } else {
            // Item changed and changed position
            mSnapshots.remove(change.getOldIndex());
            mSnapshots.add(change.getNewIndex(), change.getDocument());
            notifyItemMoved(change.getOldIndex(), change.getNewIndex());
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        mSnapshots.remove(change.getOldIndex());
        notifyItemRemoved(change.getOldIndex());
    }

    protected void onError(FirebaseFirestoreException e) {
        Log.w(TAG, "onError", e);
    }

    protected void onDataChanged() { }
}

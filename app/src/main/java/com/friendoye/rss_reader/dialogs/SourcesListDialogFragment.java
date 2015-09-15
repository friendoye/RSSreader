package com.friendoye.rss_reader.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.friendoye.rss_reader.R;
import com.friendoye.rss_reader.utils.Config;
import com.friendoye.rss_reader.utils.Packer;

import java.util.HashSet;
import java.util.Set;

/**
 * Dialog for picking active RSS sources.
 */
public class SourcesListDialogFragment extends DialogFragment
        implements DialogInterface.OnMultiChoiceClickListener,
        DialogInterface.OnClickListener {
    private Set<String> mSelectedItems;
    private String[] mSources;

    private OnSourcesChangedListener mCallback;

    public interface OnSourcesChangedListener {
        void onSourcesChanged();
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        try {
            mCallback = (OnSourcesChangedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must " +
                    "implement OnSourcesChangedListener!");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSelectedItems = new HashSet<>();
        mSources = getResources().getStringArray(R.array.rss_sources_array);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Choose RSS sources")
                .setMultiChoiceItems(R.array.rss_sources_array, null, this)
                .setPositiveButton("OK", this)
                .setNegativeButton("Cancel", null);

        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        if (isChecked) {
            mSelectedItems.add(mSources[which]);
        } else {
            mSelectedItems.remove(mSources[which]);
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        String savedPack = preferences.getString(Config.SOURCES_STRING_KEY,
                                                 null);
        String pack = Packer.packCollection(mSelectedItems);
        if (savedPack == null ||!savedPack.equals(pack)) {
            preferences.edit()
                    .putString(Config.SOURCES_STRING_KEY, pack)
                    .commit();
            if (mCallback != null) {
                mCallback.onSourcesChanged();
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }
}

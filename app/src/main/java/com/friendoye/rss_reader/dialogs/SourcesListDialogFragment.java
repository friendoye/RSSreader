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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.friendoye.rss_reader.R;
import com.friendoye.rss_reader.utils.Config;
import com.friendoye.rss_reader.utils.Packer;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 * Dialog for picking active RSS sources.
 */
public class SourcesListDialogFragment extends DialogFragment
        implements DialogInterface.OnMultiChoiceClickListener,
        DialogInterface.OnClickListener {
    public static final String SELECTED_SOURCES_KEY = "selected_s_key";

    private String[] mSources;
    private boolean[] mSelectedSources;

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

        mSources = getResources().getStringArray(R.array.rss_sources_array);

        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        String savedPack = preferences.getString(Config.SOURCES_STRING_KEY,
                                                 null);
        if (savedInstanceState == null) {
            mSelectedSources = new boolean[mSources.length];
            if (savedPack != null) {
                Set<String> activeSources = new TreeSet<>();
                activeSources.addAll(Arrays.asList(Packer.unpackAsStringArray(savedPack)));
                for (int i = 0; i < mSources.length; i++) {
                    mSelectedSources[i] = activeSources.contains(mSources[i]);
                }
            }
        } else {
            mSelectedSources = savedInstanceState
                    .getBooleanArray(SELECTED_SOURCES_KEY);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.sources_picker_title)
                .setMultiChoiceItems(mSources, mSelectedSources, this)
                .setPositiveButton(R.string.ok_text, this)
                .setNegativeButton(R.string.cancel_text, null)
                .create();

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Dialog shouldn't be dismissed, if there are no selected sources.
        // For that purpose here was added this listener.
        final AlertDialog dialog = (AlertDialog) getDialog();
        if(dialog != null)
        {
            Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences preferences = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    String savedPack = preferences.getString(Config.SOURCES_STRING_KEY,
                                                             null);
                    String pack = Packer.packCollection(mSources, mSelectedSources);
                    if (pack == null) {
                        Toast.makeText(getActivity(), R.string.no_sources_forbidden_text,
                                Toast.LENGTH_LONG).show();
                        return;
                    } else if (savedPack == null ||!savedPack.equals(pack)) {
                        preferences.edit()
                                .putString(Config.SOURCES_STRING_KEY, pack)
                                .commit();
                        if (mCallback != null) {
                            mCallback.onSourcesChanged();
                        }
                    }
                    dialog.dismiss();
                }
            });
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        mSelectedSources[which] = isChecked;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        // Implemented only for one purpose: positive button creation.
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBooleanArray(SELECTED_SOURCES_KEY, mSelectedSources);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }
}

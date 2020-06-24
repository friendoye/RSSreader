package com.friendoye.rss_reader.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.friendoye.rss_reader.R;
import com.friendoye.rss_reader.utils.Config;
import com.friendoye.rss_reader.utils.DataKeeper;
import com.friendoye.rss_reader.utils.Packer;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 * Dialog for picking active RSS sources.
 */
public class SourcesListDialogFragment extends AppCompatDialogFragment
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
        String savedPack = DataKeeper.restoreString(getActivity(), Config.SOURCES_STRING_KEY);

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
            mSelectedSources = savedInstanceState.getBooleanArray(SELECTED_SOURCES_KEY);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog);

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
                    Context context = getActivity();
                    String savedPack = DataKeeper.restoreString(context, Config.SOURCES_STRING_KEY);
                    String pack = Packer.packCollection(mSources, mSelectedSources);
                    if (pack == null) {
                        Toast.makeText(getActivity(), R.string.no_sources_forbidden_text,
                                Toast.LENGTH_LONG).show();
                        return;
                    } else if (savedPack == null ||!savedPack.equals(pack)) {
                        DataKeeper.saveString(context, Config.SOURCES_STRING_KEY, pack);
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

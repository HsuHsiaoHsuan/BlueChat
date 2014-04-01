package idv.funnybrain.bluechat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Freeman on 2014/4/1.
 */
public class ChooseTypeDialogFragment extends DialogFragment {

    public static ChooseTypeDialogFragment newInstance(int type) {
        ChooseTypeDialogFragment fragment = new ChooseTypeDialogFragment();
        Bundle args = new Bundle();
        args.putInt("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int type = getArguments().getInt("type");

        switch (type) {
            case Utils.DIALOG_CHOOSE_SERVER_OR_CLIENT_ID:
                return new AlertDialog.Builder(getActivity())
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle("Be Server or Client ...")
                        .setCancelable(false)
                        .setPositiveButton("Client", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((MainActivity) getActivity()).doPositiveClick();
                            }
                        })
                        .setNeutralButton("Server", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((MainActivity) getActivity()).doNeutralClick();
                            }
                        })
                        .setNegativeButton("Both", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((MainActivity) getActivity()).doNegativeClick();
                            }
                        })
                        .create();
            default:
                return super.onCreateDialog(savedInstanceState);
        }
    }
}

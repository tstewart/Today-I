package io.github.tstewart.todayi.fragments;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

import androidx.fragment.app.ListFragment;
import io.github.tstewart.todayi.R;

public class AccomplishmentListFragment extends ListFragment {

    private OnFragmentInteractionListener mListener;

    private ArrayAdapter<String> listAdapter;
    private ArrayList<String> items = new ArrayList<>();

    private Button addNewAccomplishmentButton;

    public AccomplishmentListFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_accomplishment_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, items);
        setListAdapter(listAdapter);

        addNewAccomplishmentButton = new Button(getActivity());
        addNewAccomplishmentButton.setText(getResources().getText(R.string.new_accomplishment));
        addNewAccomplishmentButton.setOnClickListener(this::onButtonPressed);
        getListView().addFooterView(addNewAccomplishmentButton);
    }

    private void onButtonPressed(View view) {
        AlertDialog dialog = getAccomplishmentDialog();

        // Change the input mode, so the keyboard pops up when the dialog opens
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        dialog.show();
    }

    AlertDialog getAccomplishmentDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(this.getResources().getString(R.string.new_accomplishment_dialog_title));

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.dialog_new_accomplishment, null);
        builder.setView(view);

        builder.setPositiveButton(R.string.button_confirm, ((dialog, which) -> {
            //TODO null check
            EditText input = view.findViewById(R.id.editTextNewAccomplishment);
            listAdapter.add(input.getText().toString());
        }));
        builder.setNegativeButton(R.string.button_cancel, null);

        return builder.create();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}

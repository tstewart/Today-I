package io.github.tstewart.todayi.fragments;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.fragment.app.ListFragment;
import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.dialog.AddAccomplishmentDialog;
import io.github.tstewart.todayi.util.OnDialogResponseListener;

public class AccomplishmentListFragment extends ListFragment {

    private OnFragmentInteractionListener mListener;

    private AddAccomplishmentDialog addAccomplishmentDialog;

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
        showNewAccomplishmentDialog();
    }

    void showNewAccomplishmentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle(this.getResources().getString(R.string.new_accomplishment_dialog_title));

        final EditText input = new EditText(this.getContext());

        builder.setView(input);
        builder.setPositiveButton(R.string.button_confirm, ((dialog, which) -> {
            listAdapter.add(input.getText().toString());
        }));
        builder.setNegativeButton(R.string.button_cancel, null);
        builder.show();

        input.requestFocus();
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

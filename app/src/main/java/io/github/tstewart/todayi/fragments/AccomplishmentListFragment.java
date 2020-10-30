package io.github.tstewart.todayi.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;

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
        addNewAccomplishmentButton.setText("New...");
        addNewAccomplishmentButton.setOnClickListener(this::onButtonPressed);
        getListView().addFooterView(addNewAccomplishmentButton);
    }

    private void onButtonPressed(View view) {
        listAdapter.add("Test " + listAdapter.getCount());
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

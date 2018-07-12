package com.xfinity.characterviewer.ui.characterlist;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.xfinity.characterviewer.CharacterViewerApp;
import com.xfinity.characterviewer.R;
import com.xfinity.characterviewer.di.component.DaggerCharacterListComponent;
import com.xfinity.characterviewer.di.module.CharacterListModule;
import com.xfinity.characterviewer.model.CharacterSet;
import com.xfinity.characterviewer.model.ShowCharacter;
import com.xfinity.characterviewer.ui.CharacterAdapter;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * CharacterListFragment is the fragment which actually resides in activity to show a list of characters
 */
public class CharacterListFragment extends Fragment implements CharacterAdapter.RecyclerViewClickListener, CharacterListContract.IView {
    private static final String TAG = "CharacterListFragment";
    List<ShowCharacter> dataSource;
    private RecyclerView recyclerView;
    private OnItemSelectedListener listener;

    @Inject
    CharacterListPresenter mCharacterListPresenter;

    @Inject
    CharacterAdapter adapter;

    @Inject
    LinearLayoutManager layoutManager;

//    @Override
//    public void recyclerViewListClicked(View v, int position) {
//        listener.onItemSelected(dataSource.get(position));
//    }

    @Subscribe
    public void onEvent(Boolean b) {
        for (ShowCharacter topic : dataSource) topic.setUseGrid(b);
        if (b) {
            this.recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        } else {
            this.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        }
    }

    @Override
    public void onAttach(Context context) {
        DaggerCharacterListComponent.builder()
                .appComponent(CharacterViewerApp.get(getContext()).getAppComponent())
                .characterListModule(new CharacterListModule(this, context))
                .build()
                .inject(this);

        super.onAttach(context);

        dataSource = new ArrayList<>();
        if (context instanceof OnItemSelectedListener) {
            listener = (OnItemSelectedListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + getString(R.string.class_cast_exception));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter.setListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_character_list, container, false);
        recyclerView = view.findViewById(R.id.rvItems);
        makeRequest();
        EventBus.getDefault().register(this);
        return view;
    }

    /**
     * Request Character Data from Presenter
     */
    void makeRequest() {
        mCharacterListPresenter.requestCharacterData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void setAdapter(CharacterSet characterData) {
        this.dataSource = characterData.getShowCharacters();
        adapter.setDataSource(this.dataSource);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public void onCharacterItemClicked(int position, ShowCharacter characterItem, /*ImageView*/View v) {
        listener.onItemSelected(characterItem, v);
    }

    public interface OnItemSelectedListener {
        void onItemSelected(Object item, /*ImageView*/View characterImg);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}

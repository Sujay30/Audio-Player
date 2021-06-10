package com.example.audioplayer2;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.audioplayer.R;

import static com.example.audioplayer2.MainActivity.albums;
import static com.example.audioplayer2.MainActivity.musicFiles;

/**
 * A simple {@link Fragment} subclass.

 */
public class AlbumFragment extends Fragment {

    RecyclerView recyclerView;
    albumAdapter albumadapter;

    public AlbumFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_album, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        if(!(albums.size() < 1))
        {
             albumadapter = new albumAdapter(getContext(),albums    );
            recyclerView.setAdapter(albumadapter);
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));
        }

        return view;
    }
}
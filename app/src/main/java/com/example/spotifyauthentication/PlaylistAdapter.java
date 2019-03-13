package com.example.spotifyauthentication;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.DateFormat;
import java.util.ArrayList;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistItemViewHolder> {

    private ArrayList<String> mPlaylistTrackNames;
    private ArrayList<String> mPlaylistTrackDur;
    private ArrayList<String> mPlaylistTrackArtist;
    private ArrayList<String> mPlaylistImageUrl;
    private OnPlaylistItemClickListener mPlaylistItemClickListener;

    public interface OnPlaylistItemClickListener {
        void onPlaylistItemClick(String track, String arist, String dur, String ImgUrl);
    }

    public PlaylistAdapter(OnPlaylistItemClickListener clickListener) {
        mPlaylistItemClickListener = clickListener;
    }

    public void updatePlaylistItems(ArrayList<String> TrackData, ArrayList<String> durData, ArrayList<String> artistData, ArrayList<String> ImgUrl) {
        mPlaylistTrackNames = TrackData;
        mPlaylistTrackDur = durData;
        mPlaylistTrackArtist = artistData;
        mPlaylistImageUrl = ImgUrl;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mPlaylistTrackNames != null) {
            return mPlaylistTrackNames.size();
        } else {
            return 0;
        }
    }

    @Override
    public PlaylistItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.playlist_list_item, parent, false);
        return new PlaylistItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PlaylistItemViewHolder holder, int position) {
        holder.bind(mPlaylistTrackNames.get(position), mPlaylistTrackArtist.get(position), mPlaylistTrackDur.get(position));
    }

    class PlaylistItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mNameTv;
        private TextView mArtistTv;
        private TextView mDurIV;

        public PlaylistItemViewHolder(View itemView) {
            super(itemView);
            mNameTv = itemView.findViewById(R.id.tv_track_name);
            mArtistTv = itemView.findViewById(R.id.tv_track_artist);
            mDurIV = itemView.findViewById(R.id.tv_track_duration);
            itemView.setOnClickListener(this);
        }

        public void bind(String track, String artist, String duration) {
            mNameTv.setText(track);
            mArtistTv.setText(artist);
            mDurIV.setText(duration);
        }

        @Override
        public void onClick(View v) {
            int x = getAdapterPosition();
            String TrackArtist = mPlaylistTrackArtist.get(x);
            String TrackDur = mPlaylistTrackDur.get(x);
            String TrackItem = mPlaylistTrackNames.get(getAdapterPosition());
            String TrackImage = mPlaylistImageUrl.get(x);
            mPlaylistItemClickListener.onPlaylistItemClick(TrackItem,TrackArtist,TrackDur, TrackImage);
        }
    }
}

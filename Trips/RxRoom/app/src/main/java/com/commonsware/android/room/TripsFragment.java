/***
  Copyright (c) 2008-2017 CommonsWare, LLC
  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain	a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS,	WITHOUT	WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.
  
  Covered in detail in the book _Android's Architecture Components_
    https://commonsware.com/AndroidArch
 */

package com.commonsware.android.room;

import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;
import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class TripsFragment extends RecyclerViewFragment {
  private Maybe<List<Trip>> trips;
  private Disposable sub;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    setRetainInstance(true);

    TripStore store=TripDatabase.get(getActivity()).tripStore();

    trips=store.maybeAllTrips()
      .subscribeOn(Schedulers.single())
      .cache()
      .observeOn(AndroidSchedulers.mainThread());
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    setLayoutManager(new LinearLayoutManager(getActivity()));
    getRecyclerView()
      .addItemDecoration(new DividerItemDecoration(getActivity(),
        LinearLayoutManager.VERTICAL));

    unsub();
    sub=trips.subscribe(this::setAdapter);
  }

  @Override
  public void onDestroy() {
    unsub();
    super.onDestroy();
  }

  private void unsub() {
    if (sub!=null && !sub.isDisposed()) {
      sub.dispose();
    }
  }

  private void setAdapter(List<Trip> trips) {
    setAdapter(new TripsAdapter(trips, getActivity().getLayoutInflater()));
  }

  private static class TripsAdapter extends RecyclerView.Adapter<RowHolder> {
    private final List<Trip> trips;
    private final LayoutInflater inflater;

    private TripsAdapter(List<Trip> trips, LayoutInflater inflater) {
      this.trips=trips;
      this.inflater=inflater;
    }

    @Override
    public RowHolder onCreateViewHolder(ViewGroup parent,
                                        int viewType) {
      return(new RowHolder(inflater.inflate(android.R.layout.simple_list_item_1,
        parent, false)));
    }

    @Override
    public void onBindViewHolder(RowHolder holder, int position) {
      holder.bind(trips.get(position));
    }

    @Override
    public int getItemCount() {
      return(trips==null ? 0 : trips.size());
    }
  }

  private static class RowHolder extends RecyclerView.ViewHolder {
    private final TextView title;

    RowHolder(View itemView) {
      super(itemView);

      title=itemView.findViewById(android.R.id.text1);
    }

    void bind(Trip trip) {
      title.setText(trip.title);
    }
  }
}

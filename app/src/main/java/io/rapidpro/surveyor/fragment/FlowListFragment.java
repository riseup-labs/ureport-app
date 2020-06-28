package io.rapidpro.surveyor.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;

import io.rapidpro.surveyor.R;
import io.rapidpro.surveyor.adapter.FlowListAdapter;
import io.rapidpro.surveyor.data.Flow;
import io.rapidpro.surveyor.data.Org;
import io.rapidpro.surveyor.extend.SurveyorActivity;
import io.rapidpro.surveyor.extend.fragment.BaseFragment;

import static io.rapidpro.surveyor.extend.StaticMethods.playNotification;

/**
 * A list of flows than can be selected from
 */
public class FlowListFragment extends BaseFragment implements AbsListView.OnItemClickListener {

    private Container containerx;
    private ListAdapter adapter;
    SwipeRefreshLayout flowlistRefresh;

    public FlowListFragment() {

    }

    public void stopRefresh() {
        flowlistRefresh.setRefreshing(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //containerx = (Container) getActivity();

        if(containerx.getOrg() == null){return;} // Not Logged In

        Org org = containerx.getOrg();
        List<Flow> items = containerx.getListItems();

        String firstUUID = "";
        if(items.size() > 0){
            firstUUID = items.get(0).getUuid();
        }

        adapter = new FlowListAdapter(getSurveyor(), getActivity(), R.layout.item_flow, org, items, firstUUID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_list, container, false);
        ListView m_listView = view.findViewById(android.R.id.list);
        m_listView.setAdapter(adapter);
        m_listView.setOnItemClickListener(this);


        flowlistRefresh = view.findViewById(R.id.flowRefreshLayout);
        flowlistRefresh.setOnRefreshListener(() -> {
            playNotification(getSurveyor(), getContext(), R.raw.swipe_sound);
            ((SurveyorActivity) containerx).refreshFlows();
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            containerx = (Container) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement FlowListFragment.Container");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        containerx = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        playNotification(getSurveyor(), getContext(), R.raw.button_click_yes);
        containerx.onItemClick((Flow) adapter.getItem(position));
    }

    /**
     * Container activity should implement this to be notified when a flow is clicked
     */
    public interface Container {
        Org getOrg();

        List<Flow> getListItems();

        void onItemClick(Flow flow);
    }

    @Override
    public boolean requireLogin() {
        return false;
    }

}

package io.rapidpro.surveyor.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.rapidpro.surveyor.R;
import io.rapidpro.surveyor.SurveyorApplication;
import io.rapidpro.surveyor.data.Flow;
import io.rapidpro.surveyor.data.Org;
import io.rapidpro.surveyor.extend.StaticMethods;

public class FlowListAdapter extends ArrayAdapter<Flow> {

    private Org org;
    private String firstUUID = "";
    SurveyorApplication surveyorApplication;

    public FlowListAdapter(SurveyorApplication surveyorApplication, Context context, int resourceId, Org org, List<Flow> flows, String firstUUID) {
        super(context, resourceId, flows);
        this.firstUUID = firstUUID;
        this.org = org;
        this.surveyorApplication = surveyorApplication;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        //ViewCache cache;

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        //if (row == null) {
        row = inflater.inflate(R.layout.item_flow, parent, false);
            //cache = new ViewCache();
        TextView titleView = row.findViewById(R.id.text_flow_name);
        TextView questionView = row.findViewById(R.id.text_flow_questions);
        TextView pendingSubmissions = row.findViewById(R.id.text_pending_submissions);
        TextView surveyor_last_updated = row.findViewById(R.id.surveyor_last_updated);
            //row.setTag(cache);

        //} else {
            //cache = (ViewCache) row.getTag();
        //}


        Flow flow = getItem(position);
        titleView.setText(flow.getName());

        if(flow.getUuid().equals(firstUUID)){
            String last_update = StaticMethods.getLocalUpdateDate(surveyorApplication, "surveyor_last_updated_local");
            if(last_update.equals("")){
                surveyor_last_updated.setText("Pull down to refresh");
            }else{
                surveyor_last_updated.setText("Last updated: " + last_update + "\n"  + "Pull down to refresh");
            }
            surveyor_last_updated.setVisibility(View.VISIBLE);
            //firstUUID = "-1";
        }

        int pending = SurveyorApplication.get().getSubmissionService().getCompletedCount(org, flow);

        NumberFormat nf = NumberFormat.getInstance();
        pendingSubmissions.setText(nf.format(pending));
        pendingSubmissions.setTag(flow);
        pendingSubmissions.setVisibility(pending > 0 ? View.VISIBLE : View.GONE);

        int numQuestions = flow.getQuestionCount();
        String questionsString = getContext().getResources().getQuantityString(R.plurals.questions, numQuestions, numQuestions);

        questionView.setText(questionsString + " (v" + nf.format(flow.getRevision()) + ")");
        return row;
    }


    @Override
    public long getItemId(int position) {
        return position; //return position here
    }

//    public static class ViewCache {
//        TextView surveyor_last_updated;
//        TextView titleView;
//        TextView questionView;
//        TextView pendingSubmissions;
//    }
}
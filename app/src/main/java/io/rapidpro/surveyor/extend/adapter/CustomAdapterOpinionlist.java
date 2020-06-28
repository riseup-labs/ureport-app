package io.rapidpro.surveyor.extend.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.rapidpro.surveyor.R;
import io.rapidpro.surveyor.extend.entity.model.categories;
import io.rapidpro.surveyor.extend.entity.model.questions;

/**
 * Inherited Adapter from Adolescent App
 * to display U-Report on the Application.
 * Scheduled for Deletion from RV version.
 */

public class CustomAdapterOpinionlist extends RecyclerView.Adapter<CustomAdapterOpinionlist.ViewHolder> {

    private List<questions> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    String resultDate;
    String resultFormatted;
    String previousDistrict="all";
    int delay = 600;
    Context context;
    TextView summaryText;

    public CustomAdapterOpinionlist(Context context, List<questions> data, String resultDate, TextView summaryText) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.resultDate=resultDate;
        this.context=context;
        this.summaryText=summaryText;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.v1_recycler_item_opinion_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.myTextView.setText(mData.get(position).getTitle());
        //holder.responded.setText(mData.get(position).getResults().getSet()+" responded out of "+(mData.get(position).getResults().getUnset()+mData.get(position).getResults().getSet())+" polled");
        int numSub = mData.get(position).getResults().getSet();
        int numSup = (mData.get(position).getResults().getUnset()+mData.get(position).getResults().getSet());
        holder.responded.setText(
                context.getString(R.string.v1_ureport_out_of)
                        .replace("%sup", String.valueOf(numSup))
                        .replace("%sub", String.valueOf(numSub))
        );
        if(position == 0) {
            float responseRate = 0f;
            DecimalFormat df = new DecimalFormat("#.00");

            if(numSub > 0 && numSup > 0){
                responseRate = (float)numSub / ((float)numSup / 100);
                responseRate = Float.valueOf(df.format(responseRate));
            }

            summaryText.setText(
                    context.getString(R.string.v1_ureport_poll_summary)
                            .replace("%sup", String.valueOf(numSub))
                            .replace("%sub", String.valueOf(responseRate))
            );
        }

        holder.dateT.setText(resultFormatted);

        setupStatistics(holder, position);


        if(mData.get(position).getResults_by_gender() != null) {
            List<categories> tempf = mData.get(position).getResults_by_gender().get(0).getCategories();
            setupFemale(holder, position, tempf);
        }


        if(mData.get(position).getResults_by_gender() != null) {
            List<categories> tempm = mData.get(position).getResults_by_gender().get(1).getCategories();
            setupMale(holder, position, tempm);
        }

        if(mData.get(position).getResults_by_age() != null) {
            List<categories> tempa0 = mData.get(position).getResults_by_age().get(0).getCategories();
            setupAge0(holder, position, tempa0);

            List<categories> tempa15 = mData.get(position).getResults_by_age().get(1).getCategories();
            setupA15(holder, position, tempa15);

            List<categories> tempa20 = mData.get(position).getResults_by_age().get(2).getCategories();
            setupAge20(holder, position, tempa20);

            List<categories> tempa25 = mData.get(position).getResults_by_age().get(3).getCategories();
            setupAge25(holder, position, tempa25);

            List<categories> tempa31 = mData.get(position).getResults_by_age().get(4).getCategories();
            setupAge31(holder, position, tempa31);

            List<categories> tempa35 = mData.get(position).getResults_by_age().get(5).getCategories();

            setupAge35(holder, position, tempa35);
        }

        int highest=0;
        holder.respondedMap.setText( ""+mData.get(position).getResults().getSet());
        holder.respondedAll.setText(""+ (mData.get(position).getResults().getSet()+ mData.get(position).getResults().getUnset()));

        for (int i=0;i< mData.get(position).getResults().getCategories().size();i++){
            if(mData.get(position).getResults().getCategories().get(i).getCount()>=highest){
                highest=mData.get(position).getResults().getCategories().get(i).getCount();
                int percentage = 0;
                if(mData.get(position).getResults().getSet() > 0){
                    percentage = (highest*100)/mData.get(position).getResults().getSet();
                }
                holder.respondedPercentage.setText(""+percentage+"%");
                holder.respondedLabel.setText(""+mData.get(position).getResults().getCategories().get(i).getLabel());
                holder.respondPercentRest.setText(""+(100-percentage)+"%");
            }
        }
        if(mData.get(position).getResults().getCategories().size()>=5){
            holder.cardViewx.findViewById(R.id.layout_pie_chart).setVisibility(View.VISIBLE);
            holder.cardViewx.findViewById(R.id.layout_statistics).setVisibility(View.GONE);
            holder.cardViewx.findViewById(R.id.textViewStatistics).setVisibility(View.GONE);
            holder.cardViewx.findViewById(R.id.textViewlocations).setVisibility(View.GONE);
            holder.cardViewx.findViewById(R.id.textViewGender).setVisibility(View.GONE);
            holder.cardViewx.findViewById(R.id.textViewAge).setVisibility(View.GONE);
            holder.cardViewx.findViewById(R.id.textViewPieChart).setVisibility(View.VISIBLE);




            ArrayList<PieEntry> entries = new ArrayList<PieEntry>();


            for (int i = 0; i < mData.get(position).getResults().getCategories().size(); i++) {
                if(i<25){
                    entries.add(new PieEntry(mData.get(position).getResults().getCategories().get(i).getCount(),mData.get(position).getResults().getCategories().get(i).getLabel()));
                }

            }

            PieDataSet dataSet = new PieDataSet(entries, "");

            dataSet.setDrawIcons(false);

            dataSet.setSliceSpace(3f);
            dataSet.setIconsOffset(new MPPointF(0, 40));
            dataSet.setSelectionShift(5f);

            // add a lot of colors

            ArrayList<Integer> colors = new ArrayList<Integer>();

            for (int c : ColorTemplate.VORDIPLOM_COLORS)
                colors.add(c);

            for (int c : ColorTemplate.JOYFUL_COLORS)
                colors.add(c);

            for (int c : ColorTemplate.COLORFUL_COLORS)
                colors.add(c);

            for (int c : ColorTemplate.LIBERTY_COLORS)
                colors.add(c);

            for (int c : ColorTemplate.PASTEL_COLORS)
                colors.add(c);

            colors.add(ColorTemplate.getHoloBlue());

            dataSet.setColors(colors);
            //dataSet.setSelectionShift(0f);

            PieData data = new PieData(dataSet);
            data.setValueFormatter(new PercentFormatter( holder.mChart));
            data.setValueTextSize(11f);
            data.setValueTextColor(Color.BLACK);

            holder.mChart.setData(data);

            // undo all highlights
            holder.mChart.highlightValues(null);


            holder.mChart.invalidate();
            holder.mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    PieEntry pe = (PieEntry) e;
                    holder.cardviewPie.setVisibility(View.VISIBLE);
                    holder.pieLabel.setText(""+pe.getLabel());
                    holder.pieValue.setText(""+((int)pe.getValue()));
                }

                @Override
                public void onNothingSelected() {
                    holder.cardviewPie.setVisibility(View.GONE);
                    holder.pieLabel.setText("");
                    holder.pieValue.setText("");
                }
            });


        }


    }




    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView,dateT;
        TextView responded;
        TextView date;
        TextView statistics,locations,gender,age,pie_chart_text;
        PieChart mChart;
        CardView cardViewx,cardviewPie;
        TextView pieLabel,pieValue;



        TextView label1,label2,label3,label4,label5;
        RoundCornerProgressBar option1,option2,option3,option4,option5;
        LinearLayout l3,l4,l5;
        TextView percentage1,percentage2,percentage3,percentage4,percentage5;


        TextView label1f,label2f,label3f,label4f,label5f;
        RoundCornerProgressBar option1f,option2f,option3f,option4f,option5f;
        LinearLayout l3f,l4f,l5f;
        TextView percentage1f,percentage2f,percentage3f,percentage4f,percentage5f;


        TextView label1m,label2m,label3m,label4m,label5m;
        RoundCornerProgressBar option1m,option2m,option3m,option4m,option5m;
        LinearLayout l3m,l4m,l5m;
        TextView percentage1m,percentage2m,percentage3m,percentage4m,percentage5m;


        TextView label1a0,label2a0,label3a0,label4a0,label5a0;
        RoundCornerProgressBar option1a0,option2a0,option3a0,option4a0,option5a0;
        LinearLayout l3a0,l4a0,l5a0;
        TextView percentage1a0,percentage2a0,percentage3a0,percentage4a0,percentage5a0;


        TextView label1a15,label2a15,label3a15,label4a15,label5a15;
        RoundCornerProgressBar option1a15,option2a15,option3a15,option4a15,option5a15;
        LinearLayout l3a15,l4a15,l5a15;
        TextView percentage1a15,percentage2a15,percentage3a15,percentage4a15,percentage5a15;



        TextView label1a20,label2a20,label3a20,label4a20,label5a20;
        RoundCornerProgressBar option1a20,option2a20,option3a20,option4a20,option5a20;
        LinearLayout l3a20,l4a20,l5a20;
        TextView percentage1a20,percentage2a20,percentage3a20,percentage4a20,percentage5a20;






        TextView label1a25,label2a25,label3a25,label4a25,label5a25;
        RoundCornerProgressBar option1a25,option2a25,option3a25,option4a25,option5a25;
        LinearLayout l3a25,l4a25,l5a25;
        TextView percentage1a25,percentage2a25,percentage3a25,percentage4a25,percentage5a25;



        TextView label1a31,label2a31,label3a31,label4a31,label5a31;
        RoundCornerProgressBar option1a31,option2a31,option3a31,option4a31,option5a31;
        LinearLayout l3a31,l4a31,l5a31;
        TextView percentage1a31,percentage2a31,percentage3a31,percentage4a31,percentage5a31;





        TextView label1a35,label2a35,label3a35,label4a35,label5a35;
        RoundCornerProgressBar option1a35,option2a35,option3a35,option4a35,option5a35;
        LinearLayout l3a35,l4a35,l5a35;
        TextView percentage1a35,percentage2a35,percentage3a35,percentage4a35,percentage5a35;


        ImageView bdmap;
        View v1,v2,v3,v4,v5,v6,v7,v8;
        TextView districtName,respondedMap,respondedAll,responseInZone,respondedPercentage,respondedLabel,respondPercentRest;






        ViewHolder(View itemView) {
            super(itemView);
            //resultFormatted=""+resultDate.charAt(0)+resultDate.charAt(1)+resultDate.charAt(2)+resultDate.charAt(3)+resultDate.charAt(4)+resultDate.charAt(5)+resultDate.charAt(6)+resultDate.charAt(7)+resultDate.charAt(8)+resultDate.charAt(9);
            //resultFormatted=""+resultDate.charAt(8)+resultDate.charAt(9)+resultDate.charAt(4)+resultDate.charAt(5)+resultDate.charAt(6)+resultDate.charAt(7)+resultDate.charAt(0)+resultDate.charAt(1)+resultDate.charAt(2)+resultDate.charAt(3);


            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);
            try {
                cal.setTime(sdf.parse(resultDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            SimpleDateFormat format = new SimpleDateFormat("MMM d, yyyy");
            resultFormatted = format.format(cal.getTime());//resultDate;

            dateT=itemView.findViewById(R.id.textViewDate);
            bdmap=itemView.findViewById(R.id.imageViewMapBd);

            cardViewx=itemView.findViewById(R.id.cardviewx);
            cardviewPie=itemView.findViewById(R.id.pieDisplay);
            pieLabel=itemView.findViewById(R.id.pieLabel);
            pieValue=itemView.findViewById(R.id.pieValue);

            v1=itemView.findViewById(R.id.rongpurClick);
            v2=itemView.findViewById(R.id.rajshahiClick);
            v3=itemView.findViewById(R.id.khulnaClick);
            v4=itemView.findViewById(R.id.dhakaClick);
            v5=itemView.findViewById(R.id.barisalClick);
            v6=itemView.findViewById(R.id.sylhetClick);
            v7=itemView.findViewById(R.id.chittagongClick);
            v8=itemView.findViewById(R.id.mymensinghClick);

            districtName=itemView.findViewById(R.id.districtName);
            respondedMap=itemView.findViewById(R.id.textViewRespondedMap);
            respondedAll=itemView.findViewById(R.id.textViewRespondedAllMap);
            responseInZone=itemView.findViewById(R.id.textViewResponseInZone);
            respondedPercentage=itemView.findViewById(R.id.respondedPercentageMap);
            respondedLabel=itemView.findViewById(R.id.respondedLabelMap);
            respondPercentRest=itemView.findViewById(R.id.textViewRest);



            mChart = (PieChart) itemView.findViewById(R.id.chart1);
            mChart.setUsePercentValues(true);
            mChart.getDescription().setEnabled(false);
            mChart.setExtraOffsets(5, 5, 5, 5);

            mChart.setDragDecelerationFrictionCoef(0.95f);
            mChart.getLegend().setWordWrapEnabled(true);


            Legend l = mChart.getLegend();
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            l.setDrawInside(false);
            l.setXEntrySpace(7f);
            l.setYEntrySpace(0f);
            l.setYOffset(0f);

            // entry label styling
            mChart.setEntryLabelColor(Color.DKGRAY);

            mChart.setEntryLabelTextSize(12f);

            mChart.setDrawHoleEnabled(true);
            mChart.setHoleColor(Color.WHITE);

            mChart.setTransparentCircleColor(Color.WHITE);
            mChart.setTransparentCircleAlpha(110);

            mChart.setHoleRadius(38f);
            mChart.setTransparentCircleRadius(45f);

            mChart.setDrawCenterText(false);

            mChart.setRotationAngle(0);
            // enable rotation of the chart by touch
            mChart.setRotationEnabled(true);
            mChart.setHighlightPerTapEnabled(true);
            mChart.setDrawSliceText(false);
            mChart.setDrawEntryLabels(false);





            v1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (previousDistrict.matches("রংপুর বিভাগ")){

                        int highest=0;
                        bdmap.setImageResource(R.drawable.v3_bd_0);
                        districtName.setText(R.string.v1_ureport_all);


                        respondedMap.setText( ""+mData.get(getAdapterPosition()).getResults().getSet());
                        respondedAll.setText(""+ (mData.get(getAdapterPosition()).getResults().getSet()+ mData.get(getAdapterPosition()).getResults().getUnset()));


                        for (int i=0;i< mData.get(getAdapterPosition()).getResults().getCategories().size();i++) {
                            if (mData.get(getAdapterPosition()).getResults().getCategories().get(i).getCount() >= highest) {
                                highest = mData.get(getAdapterPosition()).getResults().getCategories().get(i).getCount();
                                int percentage = 0;
                                if( mData.get(getAdapterPosition()).getResults().getSet() > 0){
                                    percentage = (highest * 100) / mData.get(getAdapterPosition()).getResults().getSet();
                                }
                                respondedPercentage.setText("" + percentage + "%");
                                respondedLabel.setText("" + mData.get(getAdapterPosition()).getResults().getCategories().get(i).getLabel());
                                respondPercentRest.setText("" + (100 - percentage) + "%");
                            }

                        }
                        previousDistrict="All";
                    }else{

                        int highest=0;
                        bdmap.setImageResource(R.drawable.v3_bd_1);
                        districtName.setText(R.string.v1_ureport_division_rangpur);

                        for (int j=0; j <mData.get(getAdapterPosition()).getResults_by_location().size();j++){
                            if(mData.get(getAdapterPosition()).getResults_by_location().get(j).getLabel().matches("রংপুর বিভাগ")){
                                respondedMap.setText( ""+mData.get(getAdapterPosition()).getResults_by_location().get(j).getSet());
                                respondedAll.setText(""+ (mData.get(getAdapterPosition()).getResults_by_location().get(j).getSet()+ mData.get(getAdapterPosition()).getResults_by_location().get(j).getUnset()));
                                responseInZone.setText(
                                        context.getResources().getString(R.string.v1_reporters_in_x)
                                                .replace("%div", context.getResources().getString(R.string.v1_ureport_division_rangpur)));
                                mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().size();
                                for (int i=0;i< mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().size();i++){
                                    if(mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().get(i).getCount()>=highest){
                                        highest=mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().get(i).getCount();
                                        int percentage = 0;
                                        if(mData.get(getAdapterPosition()).getResults_by_location().get(j).getSet() > 0){
                                            percentage = (highest*100)/mData.get(getAdapterPosition()).getResults_by_location().get(j).getSet();
                                        }
                                        respondedPercentage.setText(""+percentage+"%");
                                        respondedLabel.setText(""+mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().get(i).getLabel());
                                        respondPercentRest.setText(""+(100-percentage)+"%");
                                    }


                                }
                            }

                        }
                        previousDistrict="রংপুর বিভাগ";
                    }





                }
            });

            v2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (previousDistrict.matches("রাজশাহী বিভাগ")){

                        int highest=0;
                        bdmap.setImageResource(R.drawable.v3_bd_0);
                        districtName.setText(R.string.v1_ureport_all);


                        respondedMap.setText( ""+mData.get(getAdapterPosition()).getResults().getSet());
                        respondedAll.setText(""+ (mData.get(getAdapterPosition()).getResults().getSet()+ mData.get(getAdapterPosition()).getResults().getUnset()));


                        for (int i=0;i< mData.get(getAdapterPosition()).getResults().getCategories().size();i++) {
                            if (mData.get(getAdapterPosition()).getResults().getCategories().get(i).getCount() >= highest) {
                                highest = mData.get(getAdapterPosition()).getResults().getCategories().get(i).getCount();
                                int percentage = 0;
                                if( mData.get(getAdapterPosition()).getResults().getSet() > 0){
                                    percentage = (highest * 100) / mData.get(getAdapterPosition()).getResults().getSet();
                                }
                                respondedPercentage.setText("" + percentage + "%");
                                respondedLabel.setText("" + mData.get(getAdapterPosition()).getResults().getCategories().get(i).getLabel());
                                respondPercentRest.setText("" + (100 - percentage) + "%");
                            }

                        }
                        previousDistrict="All";
                    }else{

                        int highest=0;
                        bdmap.setImageResource(R.drawable.v3_bd_2);
                        districtName.setText(R.string.v1_ureport_division_rajshahi);

                        for (int j=0; j <mData.get(getAdapterPosition()).getResults_by_location().size();j++){
                            if(mData.get(getAdapterPosition()).getResults_by_location().get(j).getLabel().matches("রাজশাহী বিভাগ")){
                                respondedMap.setText( ""+mData.get(getAdapterPosition()).getResults_by_location().get(j).getSet());
                                respondedAll.setText(""+ (mData.get(getAdapterPosition()).getResults_by_location().get(j).getSet()+ mData.get(getAdapterPosition()).getResults_by_location().get(j).getUnset()));
                                responseInZone.setText(
                                        context.getResources().getString(R.string.v1_reporters_in_x)
                                                .replace("%div", context.getResources().getString(R.string.v1_ureport_division_rajshahi)));
                                mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().size();
                                for (int i=0;i< mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().size();i++){
                                    if(mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().get(i).getCount()>=highest){
                                        highest=mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().get(i).getCount();
                                        int percentage = 0;
                                        if(mData.get(getAdapterPosition()).getResults_by_location().get(j).getSet() > 0){
                                            percentage = (highest*100)/mData.get(getAdapterPosition()).getResults_by_location().get(j).getSet();
                                        }
                                        respondedPercentage.setText(""+percentage+"%");
                                        respondedLabel.setText(""+mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().get(i).getLabel());
                                        respondPercentRest.setText(""+(100-percentage)+"%");
                                    }


                                }
                            }

                        }
                        previousDistrict="রাজশাহী বিভাগ";
                    }








                }
            });

            v3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (previousDistrict.matches("খুলনা বিভাগ")){

                        int highest=0;
                        bdmap.setImageResource(R.drawable.v3_bd_0);
                        districtName.setText(R.string.v1_ureport_all);


                        respondedMap.setText( ""+mData.get(getAdapterPosition()).getResults().getSet());
                        respondedAll.setText(""+ (mData.get(getAdapterPosition()).getResults().getSet()+ mData.get(getAdapterPosition()).getResults().getUnset()));


                        for (int i=0;i< mData.get(getAdapterPosition()).getResults().getCategories().size();i++) {
                            if (mData.get(getAdapterPosition()).getResults().getCategories().get(i).getCount() >= highest) {
                                highest = mData.get(getAdapterPosition()).getResults().getCategories().get(i).getCount();
                                int percentage = 0;
                                if( mData.get(getAdapterPosition()).getResults().getSet() > 0){
                                    percentage = (highest * 100) / mData.get(getAdapterPosition()).getResults().getSet();
                                }
                                respondedPercentage.setText("" + percentage + "%");
                                respondedLabel.setText("" + mData.get(getAdapterPosition()).getResults().getCategories().get(i).getLabel());
                                respondPercentRest.setText("" + (100 - percentage) + "%");
                            }

                        }
                        previousDistrict="All";
                    }else{

                        int highest=0;
                        bdmap.setImageResource(R.drawable.v3_bd_3);
                        districtName.setText(R.string.v1_ureport_division_khulna);


                        for (int j=0; j <mData.get(getAdapterPosition()).getResults_by_location().size();j++){
                            if(mData.get(getAdapterPosition()).getResults_by_location().get(j).getLabel().matches("খুলনা বিভাগ")){
                                respondedMap.setText( ""+mData.get(getAdapterPosition()).getResults_by_location().get(j).getSet());
                                respondedAll.setText(""+ (mData.get(getAdapterPosition()).getResults_by_location().get(j).getSet()+ mData.get(getAdapterPosition()).getResults_by_location().get(j).getUnset()));
                                responseInZone.setText(
                                        context.getResources().getString(R.string.v1_reporters_in_x)
                                                .replace("%div", context.getResources().getString(R.string.v1_ureport_division_khulna)));
                                mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().size();
                                for (int i=0;i< mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().size();i++){
                                    if(mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().get(i).getCount()>=highest){
                                        highest=mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().get(i).getCount();
                                        int percentage = 0;
                                        if(mData.get(getAdapterPosition()).getResults_by_location().get(j).getSet() > 0){
                                            percentage = (highest*100)/mData.get(getAdapterPosition()).getResults_by_location().get(j).getSet();
                                        }
                                        respondedPercentage.setText(""+percentage+"%");
                                        respondedLabel.setText(""+mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().get(i).getLabel());
                                        respondPercentRest.setText(""+(100-percentage)+"%");
                                    }


                                }
                            }

                        }
                        previousDistrict="খুলনা বিভাগ";
                    }






                }
            });

            v4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (previousDistrict.matches("ঢাকা বিভাগ")){

                        int highest=0;
                        bdmap.setImageResource(R.drawable.v3_bd_0);
                        districtName.setText(R.string.v1_ureport_all);


                        respondedMap.setText( ""+mData.get(getAdapterPosition()).getResults().getSet());
                        respondedAll.setText(""+ (mData.get(getAdapterPosition()).getResults().getSet()+ mData.get(getAdapterPosition()).getResults().getUnset()));


                        for (int i=0;i< mData.get(getAdapterPosition()).getResults().getCategories().size();i++) {
                            if (mData.get(getAdapterPosition()).getResults().getCategories().get(i).getCount() >= highest) {
                                highest = mData.get(getAdapterPosition()).getResults().getCategories().get(i).getCount();
                                int percentage = 0;
                                if( mData.get(getAdapterPosition()).getResults().getSet() > 0){
                                    percentage = (highest * 100) / mData.get(getAdapterPosition()).getResults().getSet();
                                }
                                respondedPercentage.setText("" + percentage + "%");
                                respondedLabel.setText("" + mData.get(getAdapterPosition()).getResults().getCategories().get(i).getLabel());
                                respondPercentRest.setText("" + (100 - percentage) + "%");
                            }

                        }
                        previousDistrict="All";
                    }else{


                        int highest=0;
                        bdmap.setImageResource(R.drawable.v3_bd_4);
                        districtName.setText(R.string.v1_ureport_division_dhaka);


                        for (int j=0; j <mData.get(getAdapterPosition()).getResults_by_location().size();j++){
                            if(mData.get(getAdapterPosition()).getResults_by_location().get(j).getLabel().matches("ঢাকা বিভাগ")){
                                respondedMap.setText( ""+mData.get(getAdapterPosition()).getResults_by_location().get(j).getSet());
                                respondedAll.setText(""+ (mData.get(getAdapterPosition()).getResults_by_location().get(j).getSet()+ mData.get(getAdapterPosition()).getResults_by_location().get(j).getUnset()));
                                responseInZone.setText(
                                        context.getResources().getString(R.string.v1_reporters_in_x)
                                                .replace("%div", context.getResources().getString(R.string.v1_ureport_division_dhaka)));
                                mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().size();
                                for (int i=0;i< mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().size();i++){
                                    if(mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().get(i).getCount()>=highest){
                                        highest=mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().get(i).getCount();
                                        int percentage = 0;
                                        if(mData.get(getAdapterPosition()).getResults_by_location().get(j).getSet() > 0){
                                            percentage = (highest*100)/mData.get(getAdapterPosition()).getResults_by_location().get(j).getSet();
                                        }
                                        respondedPercentage.setText(""+percentage+"%");
                                        respondedLabel.setText(""+mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().get(i).getLabel());
                                        respondPercentRest.setText(""+(100-percentage)+"%");
                                    }


                                }
                            }

                        }
                        previousDistrict="ঢাকা বিভাগ";
                    }






                }
            });

            v5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (previousDistrict.matches("বরিশাল বিভাগ")){

                        int highest=0;
                        bdmap.setImageResource(R.drawable.v3_bd_0);
                        districtName.setText(R.string.v1_ureport_all);


                        respondedMap.setText( ""+mData.get(getAdapterPosition()).getResults().getSet());
                        respondedAll.setText(""+ (mData.get(getAdapterPosition()).getResults().getSet()+ mData.get(getAdapterPosition()).getResults().getUnset()));


                        for (int i=0;i< mData.get(getAdapterPosition()).getResults().getCategories().size();i++) {
                            if (mData.get(getAdapterPosition()).getResults().getCategories().get(i).getCount() >= highest) {
                                highest = mData.get(getAdapterPosition()).getResults().getCategories().get(i).getCount();
                                int percentage = 0;
                                if( mData.get(getAdapterPosition()).getResults().getSet() > 0){
                                    percentage = (highest * 100) / mData.get(getAdapterPosition()).getResults().getSet();
                                }
                                respondedPercentage.setText("" + percentage + "%");
                                respondedLabel.setText("" + mData.get(getAdapterPosition()).getResults().getCategories().get(i).getLabel());
                                respondPercentRest.setText("" + (100 - percentage) + "%");
                            }

                        }
                        previousDistrict="All";
                    }else{


                        int highest=0;
                        bdmap.setImageResource(R.drawable.v3_bd_5);
                        districtName.setText(R.string.v1_ureport_division_barisal);

                        for (int j=0; j <mData.get(getAdapterPosition()).getResults_by_location().size();j++){
                            if(mData.get(getAdapterPosition()).getResults_by_location().get(j).getLabel().matches("বরিশাল বিভাগ")){
                                respondedMap.setText( ""+mData.get(getAdapterPosition()).getResults_by_location().get(j).getSet());
                                respondedAll.setText(""+ (mData.get(getAdapterPosition()).getResults_by_location().get(j).getSet()+ mData.get(getAdapterPosition()).getResults_by_location().get(j).getUnset()));
                                responseInZone.setText(
                                        context.getResources().getString(R.string.v1_reporters_in_x)
                                                .replace("%div", context.getResources().getString(R.string.v1_ureport_division_barisal)));
                                mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().size();
                                for (int i=0;i< mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().size();i++){
                                    if(mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().get(i).getCount()>=highest){
                                        highest=mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().get(i).getCount();
                                        int percentage = 0;
                                        if(mData.get(getAdapterPosition()).getResults_by_location().get(j).getSet() > 0){
                                            percentage = (highest*100)/mData.get(getAdapterPosition()).getResults_by_location().get(j).getSet();
                                        }
                                        respondedPercentage.setText(""+percentage+"%");
                                        respondedLabel.setText(""+mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().get(i).getLabel());
                                        respondPercentRest.setText(""+(100-percentage)+"%");
                                    }


                                }
                            }

                        }
                        previousDistrict="বরিশাল বিভাগ";
                    }







                }
            });

            v6.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (previousDistrict.matches("সিলেট বিভাগ")){

                        int highest=0;
                        bdmap.setImageResource(R.drawable.v3_bd_0);
                        districtName.setText(R.string.v1_ureport_all);


                        respondedMap.setText( ""+mData.get(getAdapterPosition()).getResults().getSet());
                        respondedAll.setText(""+ (mData.get(getAdapterPosition()).getResults().getSet()+ mData.get(getAdapterPosition()).getResults().getUnset()));


                        for (int i=0;i< mData.get(getAdapterPosition()).getResults().getCategories().size();i++) {
                            if (mData.get(getAdapterPosition()).getResults().getCategories().get(i).getCount() >= highest) {
                                highest = mData.get(getAdapterPosition()).getResults().getCategories().get(i).getCount();
                                int percentage = 0;
                                if( mData.get(getAdapterPosition()).getResults().getSet() > 0){
                                    percentage = (highest * 100) / mData.get(getAdapterPosition()).getResults().getSet();
                                }
                                respondedPercentage.setText("" + percentage + "%");
                                respondedLabel.setText("" + mData.get(getAdapterPosition()).getResults().getCategories().get(i).getLabel());
                                respondPercentRest.setText("" + (100 - percentage) + "%");
                            }

                        }
                        previousDistrict="All";
                    }else{


                        int highest=0;
                        bdmap.setImageResource(R.drawable.v3_bd_6);
                        districtName.setText(R.string.v1_ureport_division_sylhet);
                        for (int j=0; j <mData.get(getAdapterPosition()).getResults_by_location().size();j++){
                            if(mData.get(getAdapterPosition()).getResults_by_location().get(j).getLabel().matches("সিলেট বিভাগ")){
                                respondedMap.setText( ""+mData.get(getAdapterPosition()).getResults_by_location().get(j).getSet());
                                respondedAll.setText(""+ (mData.get(getAdapterPosition()).getResults_by_location().get(j).getSet()+ mData.get(getAdapterPosition()).getResults_by_location().get(j).getUnset()));
                                responseInZone.setText(
                                        context.getResources().getString(R.string.v1_reporters_in_x)
                                                .replace("%div", context.getResources().getString(R.string.v1_ureport_division_sylhet)));
                                mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().size();
                                for (int i=0;i< mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().size();i++){
                                    if(mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().get(i).getCount()>=highest){
                                        highest=mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().get(i).getCount();
                                        int percentage = 0;
                                        if(mData.get(getAdapterPosition()).getResults_by_location().get(j).getSet() > 0){
                                            percentage = (highest*100)/mData.get(getAdapterPosition()).getResults_by_location().get(j).getSet();
                                        }
                                        respondedPercentage.setText(""+percentage+"%");
                                        respondedLabel.setText(""+mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().get(i).getLabel());
                                        respondPercentRest.setText(""+(100-percentage)+"%");
                                    }


                                }
                            }

                        }
                        previousDistrict="সিলেট বিভাগ";
                    }










                }
            });

            v7.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (previousDistrict.matches("চট্টগ্রাম বিভাগ")){

                        int highest=0;
                        bdmap.setImageResource(R.drawable.v3_bd_0);
                        districtName.setText(R.string.v1_ureport_all);


                        respondedMap.setText( ""+mData.get(getAdapterPosition()).getResults().getSet());
                        respondedAll.setText(""+ (mData.get(getAdapterPosition()).getResults().getSet()+ mData.get(getAdapterPosition()).getResults().getUnset()));


                        for (int i=0;i< mData.get(getAdapterPosition()).getResults().getCategories().size();i++) {
                            if (mData.get(getAdapterPosition()).getResults().getCategories().get(i).getCount() >= highest) {
                                highest = mData.get(getAdapterPosition()).getResults().getCategories().get(i).getCount();
                                int percentage = 0;
                                if( mData.get(getAdapterPosition()).getResults().getSet() > 0){
                                    percentage = (highest * 100) / mData.get(getAdapterPosition()).getResults().getSet();
                                }
                                respondedPercentage.setText("" + percentage + "%");
                                respondedLabel.setText("" + mData.get(getAdapterPosition()).getResults().getCategories().get(i).getLabel());
                                respondPercentRest.setText("" + (100 - percentage) + "%");
                            }

                        }
                        previousDistrict="All";
                    }else{


                        int highest=0;
                        bdmap.setImageResource(R.drawable.v3_bd_7);
                        districtName.setText(R.string.v1_ureport_division_chittagong);
                        for (int j=0; j <mData.get(getAdapterPosition()).getResults_by_location().size();j++){
                            if(mData.get(getAdapterPosition()).getResults_by_location().get(j).getLabel().matches("চট্টগ্রাম বিভাগ")){
                                respondedMap.setText( ""+mData.get(getAdapterPosition()).getResults_by_location().get(j).getSet());
                                respondedAll.setText(""+ (mData.get(getAdapterPosition()).getResults_by_location().get(j).getSet()+ mData.get(getAdapterPosition()).getResults_by_location().get(j).getUnset()));
                                responseInZone.setText(
                                        context.getResources().getString(R.string.v1_reporters_in_x)
                                                .replace("%div", context.getResources().getString(R.string.v1_ureport_division_chittagong)));
                                mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().size();
                                for (int i=0;i< mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().size();i++){
                                    if(mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().get(i).getCount()>=highest){
                                        highest=mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().get(i).getCount();
                                        int percentage = 0;
                                        if(mData.get(getAdapterPosition()).getResults_by_location().get(j).getSet() > 0){
                                            percentage = (highest*100)/mData.get(getAdapterPosition()).getResults_by_location().get(j).getSet();
                                        }
                                        respondedPercentage.setText(""+percentage+"%");
                                        respondedLabel.setText(""+mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().get(i).getLabel());
                                        respondPercentRest.setText(""+(100-percentage)+"%");
                                    }


                                }
                            }

                        }
                        previousDistrict="চট্টগ্রাম বিভাগ";
                    }










                }
            });



            v8.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (previousDistrict.matches("ময়মনসিংহ বিভাগ")){

                        int highest=0;
                        bdmap.setImageResource(R.drawable.v3_bd_0);
                        districtName.setText(R.string.v1_ureport_all);


                        respondedMap.setText( ""+mData.get(getAdapterPosition()).getResults().getSet());
                        respondedAll.setText(""+ (mData.get(getAdapterPosition()).getResults().getSet()+ mData.get(getAdapterPosition()).getResults().getUnset()));


                        for (int i=0;i< mData.get(getAdapterPosition()).getResults().getCategories().size();i++) {
                            if (mData.get(getAdapterPosition()).getResults().getCategories().get(i).getCount() >= highest) {
                                highest = mData.get(getAdapterPosition()).getResults().getCategories().get(i).getCount();
                                int percentage = 0;
                                if( mData.get(getAdapterPosition()).getResults().getSet() > 0){
                                    percentage = (highest * 100) / mData.get(getAdapterPosition()).getResults().getSet();
                                }
                                respondedPercentage.setText("" + percentage + "%");
                                respondedLabel.setText("" + mData.get(getAdapterPosition()).getResults().getCategories().get(i).getLabel());
                                respondPercentRest.setText("" + (100 - percentage) + "%");
                            }

                        }
                        previousDistrict="All";
                    }else{


                        int highest=0;
                        bdmap.setImageResource(R.drawable.v3_bd_8);
                        districtName.setText(R.string.v1_ureport_division_mymensing);
                        for (int j=0; j <mData.get(getAdapterPosition()).getResults_by_location().size();j++){
                            if(mData.get(getAdapterPosition()).getResults_by_location().get(j).getLabel().matches("ময়মনসিংহ বিভাগ")){
                                respondedMap.setText( ""+mData.get(getAdapterPosition()).getResults_by_location().get(j).getSet());
                                respondedAll.setText(""+ (mData.get(getAdapterPosition()).getResults_by_location().get(j).getSet()+ mData.get(getAdapterPosition()).getResults_by_location().get(j).getUnset()));
                                responseInZone.setText(
                                        context.getResources().getString(R.string.v1_reporters_in_x)
                                                .replace("%div", context.getResources().getString(R.string.v1_ureport_division_mymensing)));
                                mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().size();
                                for (int i=0;i< mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().size();i++){
                                    if(mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().get(i).getCount()>=highest){
                                        highest=mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().get(i).getCount();
                                        int percentage = 0;
                                        if(mData.get(getAdapterPosition()).getResults_by_location().get(j).getSet() > 0){
                                            percentage = (highest*100)/mData.get(getAdapterPosition()).getResults_by_location().get(j).getSet();
                                        }
                                        respondedPercentage.setText(""+percentage+"%");
                                        respondedLabel.setText(""+mData.get(getAdapterPosition()).getResults_by_location().get(j).getCategories().get(i).getLabel());
                                        respondPercentRest.setText(""+(100-percentage)+"%");
                                    }


                                }
                            }

                        }
                        previousDistrict="ময়মনসিংহ বিভাগ";
                    }









                }
            });



            myTextView = itemView.findViewById(R.id.textView14);
            responded=itemView.findViewById(R.id.textViewResponded);
            date=itemView.findViewById(R.id.textViewDate);

            percentage1 = itemView.findViewById(R.id.textViewPercentage);
            percentage2 = itemView.findViewById(R.id.textViewPercentage2);
            percentage3 = itemView.findViewById(R.id.textViewPercentage3);
            percentage4 = itemView.findViewById(R.id.textViewPercentage4);
            percentage5 = itemView.findViewById(R.id.textViewPercentage5);

            label1=itemView.findViewById(R.id.textViewLabel1);
            label2=itemView.findViewById(R.id.textViewLabel2);
            label3=itemView.findViewById(R.id.textViewLabel3);
            label4=itemView.findViewById(R.id.textViewLabel4);
            label5=itemView.findViewById(R.id.textViewLabel5);
            option1=itemView.findViewById(R.id.rounded1);
            option2=itemView.findViewById(R.id.rounded2);
            option3=itemView.findViewById(R.id.rounded3);
            option4=itemView.findViewById(R.id.rounded4);
            option5=itemView.findViewById(R.id.rounded5);
            l3=itemView.findViewById(R.id.layout_3);
            l4=itemView.findViewById(R.id.layout_4);
            l5=itemView.findViewById(R.id.layout_5);



            percentage1f = itemView.findViewById(R.id.textViewPercentagef);
            percentage2f = itemView.findViewById(R.id.textViewPercentage2f);
            percentage3f = itemView.findViewById(R.id.textViewPercentage3f);
            percentage4f = itemView.findViewById(R.id.textViewPercentage4f);
            percentage5f = itemView.findViewById(R.id.textViewPercentage5f);

            label1f=itemView.findViewById(R.id.textViewLabel1f);
            label2f=itemView.findViewById(R.id.textViewLabel2f);
            label3f=itemView.findViewById(R.id.textViewLabel3f);
            label4f=itemView.findViewById(R.id.textViewLabel4f);
            label5f=itemView.findViewById(R.id.textViewLabel5f);
            option1f=itemView.findViewById(R.id.rounded1f);
            option2f=itemView.findViewById(R.id.rounded2f);
            option3f=itemView.findViewById(R.id.rounded3f);
            option4f=itemView.findViewById(R.id.rounded4f);
            option5f=itemView.findViewById(R.id.rounded5f);
            l3f=itemView.findViewById(R.id.layout_f_3);
            l4f=itemView.findViewById(R.id.layout_f_4);
            l5f=itemView.findViewById(R.id.layout_f_5);



            percentage1m = itemView.findViewById(R.id.textViewPercentagem);
            percentage2m = itemView.findViewById(R.id.textViewPercentage2m);
            percentage3m = itemView.findViewById(R.id.textViewPercentage3m);
            percentage4m = itemView.findViewById(R.id.textViewPercentage4m);
            percentage5m = itemView.findViewById(R.id.textViewPercentage5m);

            label1m=itemView.findViewById(R.id.textViewLabel1m);
            label2m=itemView.findViewById(R.id.textViewLabel2m);
            label3m=itemView.findViewById(R.id.textViewLabel3m);
            label4m=itemView.findViewById(R.id.textViewLabel4m);
            label5m=itemView.findViewById(R.id.textViewLabel5m);
            option1m=itemView.findViewById(R.id.rounded1m);
            option2m=itemView.findViewById(R.id.rounded2m);
            option3m=itemView.findViewById(R.id.rounded3m);
            option4m=itemView.findViewById(R.id.rounded4m);
            option5m=itemView.findViewById(R.id.rounded5m);
            l3m=itemView.findViewById(R.id.layout_m_3);
            l4m=itemView.findViewById(R.id.layout_m_4);
            l5m=itemView.findViewById(R.id.layout_m_5);






            percentage1a0 = itemView.findViewById(R.id.textViewPercentage1a0);
            percentage2a0 = itemView.findViewById(R.id.textViewPercentage2a0);
            percentage3a0 = itemView.findViewById(R.id.textViewPercentage3a0);
            percentage4a0 = itemView.findViewById(R.id.textViewPercentage4a0);
            percentage5a0 = itemView.findViewById(R.id.textViewPercentage5a0);

            label1a0=itemView.findViewById(R.id.textViewLabel1a0);
            label2a0=itemView.findViewById(R.id.textViewLabel2a0);
            label3a0=itemView.findViewById(R.id.textViewLabel3a0);
            label4a0=itemView.findViewById(R.id.textViewLabel4a0);
            label5a0=itemView.findViewById(R.id.textViewLabel5a0);
            option1a0=itemView.findViewById(R.id.rounded1a0);
            option2a0=itemView.findViewById(R.id.rounded2a0);
            option3a0=itemView.findViewById(R.id.rounded3a0);
            option4a0=itemView.findViewById(R.id.rounded4a0);
            option5a0=itemView.findViewById(R.id.rounded5a0);
            l3a0=itemView.findViewById(R.id.layout_a0_3);
            l4a0=itemView.findViewById(R.id.layout_a0_4);
            l5a0=itemView.findViewById(R.id.layout_a0_5);







            percentage1a15 = itemView.findViewById(R.id.textViewPercentage1a15);
            percentage2a15 = itemView.findViewById(R.id.textViewPercentage2a15);
            percentage3a15 = itemView.findViewById(R.id.textViewPercentage3a15);
            percentage4a15 = itemView.findViewById(R.id.textViewPercentage4a15);
            percentage5a15 = itemView.findViewById(R.id.textViewPercentage5a15);

            label1a15=itemView.findViewById(R.id.textViewLabel1a15);
            label2a15=itemView.findViewById(R.id.textViewLabel2a15);
            label3a15=itemView.findViewById(R.id.textViewLabel3a15);
            label4a15=itemView.findViewById(R.id.textViewLabel4a15);
            label5a15=itemView.findViewById(R.id.textViewLabel5a15);
            option1a15=itemView.findViewById(R.id.rounded1a15);
            option2a15=itemView.findViewById(R.id.rounded2a15);
            option3a15=itemView.findViewById(R.id.rounded3a15);
            option4a15=itemView.findViewById(R.id.rounded4a15);
            option5a15=itemView.findViewById(R.id.rounded5a15);
            l3a15=itemView.findViewById(R.id.layout_a15_3);
            l4a15=itemView.findViewById(R.id.layout_a15_4);
            l5a15=itemView.findViewById(R.id.layout_a15_5);





            percentage1a20 = itemView.findViewById(R.id.textViewPercentage1a20);
            percentage2a20 = itemView.findViewById(R.id.textViewPercentage2a20);
            percentage3a20 = itemView.findViewById(R.id.textViewPercentage3a20);
            percentage4a20 = itemView.findViewById(R.id.textViewPercentage4a20);
            percentage5a20 = itemView.findViewById(R.id.textViewPercentage5a20);

            label1a20=itemView.findViewById(R.id.textViewLabel1a20);
            label2a20=itemView.findViewById(R.id.textViewLabel2a20);
            label3a20=itemView.findViewById(R.id.textViewLabel3a20);
            label4a20=itemView.findViewById(R.id.textViewLabel4a20);
            label5a20=itemView.findViewById(R.id.textViewLabel5a20);
            option1a20=itemView.findViewById(R.id.rounded1a20);
            option2a20=itemView.findViewById(R.id.rounded2a20);
            option3a20=itemView.findViewById(R.id.rounded3a20);
            option4a20=itemView.findViewById(R.id.rounded4a20);
            option5a20=itemView.findViewById(R.id.rounded5a20);
            l3a20=itemView.findViewById(R.id.layout_a20_3);
            l4a20=itemView.findViewById(R.id.layout_a20_4);
            l5a20=itemView.findViewById(R.id.layout_a20_5);





            percentage1a25 = itemView.findViewById(R.id.textViewPercentage1a25);
            percentage2a25 = itemView.findViewById(R.id.textViewPercentage2a25);
            percentage3a25 = itemView.findViewById(R.id.textViewPercentage3a25);
            percentage4a25 = itemView.findViewById(R.id.textViewPercentage4a25);
            percentage5a25 = itemView.findViewById(R.id.textViewPercentage5a25);

            label1a25=itemView.findViewById(R.id.textViewLabel1a25);
            label2a25=itemView.findViewById(R.id.textViewLabel2a25);
            label3a25=itemView.findViewById(R.id.textViewLabel3a25);
            label4a25=itemView.findViewById(R.id.textViewLabel4a25);
            label5a25=itemView.findViewById(R.id.textViewLabel5a25);
            option1a25=itemView.findViewById(R.id.rounded1a25);
            option2a25=itemView.findViewById(R.id.rounded2a25);
            option3a25=itemView.findViewById(R.id.rounded3a25);
            option4a25=itemView.findViewById(R.id.rounded4a25);
            option5a25=itemView.findViewById(R.id.rounded5a25);
            l3a25=itemView.findViewById(R.id.layout_a25_3);
            l4a25=itemView.findViewById(R.id.layout_a25_4);
            l5a25=itemView.findViewById(R.id.layout_a25_5);





            percentage1a31 = itemView.findViewById(R.id.textViewPercentage1a31);
            percentage2a31 = itemView.findViewById(R.id.textViewPercentage2a31);
            percentage3a31 = itemView.findViewById(R.id.textViewPercentage3a31);
            percentage4a31 = itemView.findViewById(R.id.textViewPercentage4a31);
            percentage5a31 = itemView.findViewById(R.id.textViewPercentage5a31);

            label1a31=itemView.findViewById(R.id.textViewLabel1a31);
            label2a31=itemView.findViewById(R.id.textViewLabel2a31);
            label3a31=itemView.findViewById(R.id.textViewLabel3a31);
            label4a31=itemView.findViewById(R.id.textViewLabel4a31);
            label5a31=itemView.findViewById(R.id.textViewLabel5a31);
            option1a31=itemView.findViewById(R.id.rounded1a31);
            option2a31=itemView.findViewById(R.id.rounded2a31);
            option3a31=itemView.findViewById(R.id.rounded3a31);
            option4a31=itemView.findViewById(R.id.rounded4a31);
            option5a31=itemView.findViewById(R.id.rounded5a31);
            l3a31=itemView.findViewById(R.id.layout_a31_3);
            l4a31=itemView.findViewById(R.id.layout_a31_4);
            l5a31=itemView.findViewById(R.id.layout_a31_5);





            percentage1a35 = itemView.findViewById(R.id.textViewPercentage1a35);
            percentage2a35 = itemView.findViewById(R.id.textViewPercentage2a35);
            percentage3a35 = itemView.findViewById(R.id.textViewPercentage3a35);
            percentage4a35 = itemView.findViewById(R.id.textViewPercentage4a35);
            percentage5a35 = itemView.findViewById(R.id.textViewPercentage5a35);

            label1a35=itemView.findViewById(R.id.textViewLabel1a35);
            label2a35=itemView.findViewById(R.id.textViewLabe2a35);
            label3a35=itemView.findViewById(R.id.textViewLabe3a35);
            label4a35=itemView.findViewById(R.id.textViewLabe4a35);
            label5a35=itemView.findViewById(R.id.textViewLabe5a35);
            option1a35=itemView.findViewById(R.id.rounded1a35);
            option2a35=itemView.findViewById(R.id.rounded2a35);
            option3a35=itemView.findViewById(R.id.rounded3a35);
            option4a35=itemView.findViewById(R.id.rounded4a35);
            option5a35=itemView.findViewById(R.id.rounded5a35);
            l3a35=itemView.findViewById(R.id.layout_a35_3);
            l4a35=itemView.findViewById(R.id.layout_a35_4);
            l5a35=itemView.findViewById(R.id.layout_a35_5);









            statistics=itemView.findViewById(R.id.textViewStatistics);
            locations=itemView.findViewById(R.id.textViewlocations);
            gender=itemView.findViewById(R.id.textViewGender);
            age=itemView.findViewById(R.id.textViewAge);


//            itemView.setOnClickListener(this);
            statistics.setOnClickListener(this);
            gender.setOnClickListener(this);
            locations.setOnClickListener(this);
            age.setOnClickListener(this);


        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public questions getItem(int id) {
        return mData.get(id);
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
    private void setupAge0(ViewHolder holder, int position, List<categories> tempa0) {
        if (tempa0.size() == 1) {
            holder.label1a0.setText(tempa0.get(0).getLabel());
            holder.option1a0.setMax(mData.get(position).getResults_by_age().get(0).getSet());
            holder.option1a0.setProgress(tempa0.get(0).getCount());
            int percentageTempa0 = 0;
            if( (mData.get(position).getResults_by_age().get(0).getSet()) > 0){
                percentageTempa0 = ((tempa0.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(0).getSet());
            }
            holder.percentage1a0.setText("" + percentageTempa0 + "%");
            holder.l3a0.setVisibility(View.GONE);
            holder.l4a0.setVisibility(View.GONE);
            holder.l5a0.setVisibility(View.GONE);
        }
        if (tempa0.size() == 2) {
            holder.label1a0.setText(tempa0.get(0).getLabel());
            holder.option1a0.setMax(mData.get(position).getResults_by_age().get(0).getSet());
            holder.option1a0.setProgress(tempa0.get(0).getCount());
            int percentageTempa0 = 0;
            if( (mData.get(position).getResults_by_age().get(0).getSet()) > 0){
                percentageTempa0 = ((tempa0.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(0).getSet());
            }
            holder.percentage1a0.setText("" + percentageTempa0 + "%");
            holder.label2a0.setText(tempa0.get(1).getLabel());
            holder.option2a0.setMax(mData.get(position).getResults_by_age().get(0).getSet());
            holder.option2a0.setProgress(tempa0.get(1).getCount());
            int percentageTemp2a0 = 0;
            if( (mData.get(position).getResults_by_age().get(0).getSet()) > 0){
                percentageTemp2a0 = ((tempa0.get(1).getCount()) * 100) / (mData.get(position).getResults_by_age().get(0).getSet());
            }
            holder.percentage2a0.setText("" + percentageTemp2a0 + "%");
            holder.l3a0.setVisibility(View.GONE);
            holder.l4a0.setVisibility(View.GONE);
            holder.l5a0.setVisibility(View.GONE);
        }
        if (tempa0.size() == 3) {
            holder.label1a0.setText(tempa0.get(0).getLabel());
            holder.option1a0.setMax(mData.get(position).getResults_by_age().get(0).getSet());
            holder.option1a0.setProgress(tempa0.get(0).getCount());
            int percentageTempa0 = 0;
            if( (mData.get(position).getResults_by_age().get(0).getSet()) > 0){
                percentageTempa0 = ((tempa0.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(0).getSet());
            }
            holder.percentage1a0.setText("" + percentageTempa0 + "%");
            holder.label2a0.setText(tempa0.get(1).getLabel());
            holder.option2a0.setMax(mData.get(position).getResults_by_age().get(0).getSet());
            holder.option2a0.setProgress(tempa0.get(1).getCount());
            int percentageTemp2a0 = 0;
            if( (mData.get(position).getResults_by_age().get(0).getSet()) > 0){
                percentageTemp2a0 = ((tempa0.get(1).getCount()) * 100) / (mData.get(position).getResults_by_age().get(0).getSet());
            }
            holder.percentage2a0.setText("" + percentageTemp2a0 + "%");
            holder.l3a0.setVisibility(View.VISIBLE);
            holder.label3a0.setText(tempa0.get(2).getLabel());
            holder.option3a0.setMax(mData.get(position).getResults_by_age().get(0).getSet());
            holder.option3a0.setProgress(tempa0.get(2).getCount());
            int percentageTemp3a0 = 0;
            if( (mData.get(position).getResults_by_age().get(0).getSet()) > 0){
                percentageTemp3a0 = ((tempa0.get(2).getCount()) * 100) / (mData.get(position).getResults_by_age().get(0).getSet());
            }
            holder.percentage3a0.setText("" + percentageTemp3a0 + "%");

            holder.l4a0.setVisibility(View.GONE);
            holder.l5a0.setVisibility(View.GONE);

        } else {
            holder.l3a0.setVisibility(View.GONE);
        }
        if (tempa0.size() == 4) {
            holder.label1a0.setText(tempa0.get(0).getLabel());
            holder.option1a0.setMax(mData.get(position).getResults_by_age().get(0).getSet());
            holder.option1a0.setProgress(tempa0.get(0).getCount());
            int percentageTempa0 = 0;
            if( (mData.get(position).getResults_by_age().get(0).getSet()) > 0){
                percentageTempa0 = ((tempa0.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(0).getSet());
            }
            holder.percentage1a0.setText("" + percentageTempa0 + "%");
            holder.label2a0.setText(tempa0.get(1).getLabel());
            holder.option2a0.setMax(mData.get(position).getResults_by_age().get(0).getSet());
            holder.option2a0.setProgress(tempa0.get(1).getCount());
            int percentageTemp2a0 = 0;
            if( (mData.get(position).getResults_by_age().get(0).getSet()) > 0){
                percentageTemp2a0 = ((tempa0.get(1).getCount()) * 100) / (mData.get(position).getResults_by_age().get(0).getSet());
            }
            holder.percentage2a0.setText("" + percentageTemp2a0 + "%");
            holder.l3a0.setVisibility(View.VISIBLE);
            holder.label3a0.setText(tempa0.get(2).getLabel());
            holder.option3a0.setMax(mData.get(position).getResults_by_age().get(0).getSet());
            holder.option3a0.setProgress(tempa0.get(2).getCount());
            holder.l4a0.setVisibility(View.VISIBLE);
            holder.label4a0.setText(tempa0.get(3).getLabel());
            holder.option4a0.setMax(mData.get(position).getResults_by_age().get(0).getSet());
            holder.option4a0.setProgress(tempa0.get(3).getCount());
            int percentageTemp3a0 = 0;
            if( (mData.get(position).getResults_by_age().get(0).getSet()) > 0){
                percentageTemp3a0 = ((tempa0.get(2).getCount()) * 100) / (mData.get(position).getResults_by_age().get(0).getSet());
            }
            holder.percentage3a0.setText("" + percentageTemp3a0 + "%");
            int percentageTemp4a0 = 0;
            if( (mData.get(position).getResults_by_age().get(0).getSet()) > 0){
                percentageTemp4a0 = ((tempa0.get(3).getCount()) * 100) / (mData.get(position).getResults_by_age().get(0).getSet());
            }
            holder.percentage4a0.setText("" + percentageTemp4a0 + "%");


        } else {
            holder.l4a0.setVisibility(View.GONE);
        }
        if (tempa0.size() == 5) {
            holder.label1a0.setText(tempa0.get(0).getLabel());
            holder.option1a0.setMax(mData.get(position).getResults_by_age().get(0).getSet());
            holder.option1a0.setProgress(tempa0.get(0).getCount());
            int percentagetempa0 = 0;
            if( (mData.get(position).getResults_by_age().get(0).getSet()) > 0){
                percentagetempa0 = ((tempa0.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(0).getSet());
            }
            holder.percentage1a0.setText("" + percentagetempa0 + "%");
            holder.label2a0.setText(tempa0.get(1).getLabel());
            holder.option2a0.setMax(mData.get(position).getResults_by_age().get(0).getSet());
            holder.option2a0.setProgress(tempa0.get(1).getCount());
            int percentageTemp2a0 = 0;
            if( (mData.get(position).getResults_by_age().get(0).getSet()) > 0){
                percentageTemp2a0 = ((tempa0.get(1).getCount()) * 100) / (mData.get(position).getResults_by_age().get(0).getSet());
            }
            holder.percentage2a0.setText("" + percentageTemp2a0 + "%");
            holder.l3a0.setVisibility(View.VISIBLE);
            holder.label3a0.setText(tempa0.get(2).getLabel());
            holder.option3a0.setMax(mData.get(position).getResults_by_age().get(0).getSet());
            holder.option3a0.setProgress(tempa0.get(2).getCount());
            holder.l4a0.setVisibility(View.VISIBLE);
            holder.label4a0.setText(tempa0.get(3).getLabel());
            holder.option4a0.setMax(mData.get(position).getResults_by_age().get(0).getSet());
            holder.option4a0.setProgress(tempa0.get(3).getCount());
            holder.l5a0.setVisibility(View.VISIBLE);
            holder.label5a0.setText(tempa0.get(4).getLabel());
            holder.option5a0.setMax(mData.get(position).getResults_by_age().get(0).getSet());
            holder.option5a0.setProgress(tempa0.get(4).getCount());
            int percentageTemp3a0 = 0;
            if( (mData.get(position).getResults_by_age().get(0).getSet()) > 0){
                percentageTemp3a0 = ((tempa0.get(2).getCount()) * 100) / (mData.get(position).getResults_by_age().get(0).getSet());
            }
            holder.percentage3a0.setText("" + percentageTemp3a0 + "%");
            int percentageTemp4a0 = 0;
            if( (mData.get(position).getResults_by_age().get(0).getSet()) > 0){
                percentageTemp4a0 = ((tempa0.get(3).getCount()) * 100) / (mData.get(position).getResults_by_age().get(0).getSet());
            }
            holder.percentage4a0.setText("" + percentageTemp4a0 + "%");
            int percentageTemp5a0 = 0;
            if( (mData.get(position).getResults_by_age().get(0).getSet()) > 0){
                percentageTemp5a0 = ((tempa0.get(4).getCount()) * 100) / (mData.get(position).getResults_by_age().get(0).getSet());
            }
            holder.percentage5a0.setText("" + percentageTemp5a0 + "%");

        } else {
            holder.l5a0.setVisibility(View.GONE);
        }
        if (tempa0.size() > 5) {
            holder.label1a0.setText(tempa0.get(0).getLabel());
            holder.option1a0.setMax(mData.get(position).getResults_by_age().get(0).getSet());
            holder.option1a0.setProgress(tempa0.get(0).getCount());
            int percentagetempa0 = 0;
            if( (mData.get(position).getResults_by_age().get(0).getSet()) > 0){
                percentagetempa0 = ((tempa0.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(0).getSet());
            }
            holder.percentage1a0.setText("" + percentagetempa0 + "%");
            holder.label2a0.setText(tempa0.get(1).getLabel());
            holder.option2a0.setMax(mData.get(position).getResults_by_age().get(0).getSet());
            holder.option2a0.setProgress(tempa0.get(1).getCount());
            int percentageTemp2a0 = 0;
            if( (mData.get(position).getResults_by_age().get(0).getSet()) > 0){
                percentageTemp2a0 = ((tempa0.get(1).getCount()) * 100) / (mData.get(position).getResults_by_age().get(0).getSet());
            }
            holder.percentage2a0.setText("" + percentageTemp2a0 + "%");
            holder.l3a0.setVisibility(View.GONE);
            holder.l4a0.setVisibility(View.GONE);
            holder.l5a0.setVisibility(View.GONE);
        }
    }


    private void setupA15(ViewHolder holder, int position, List<categories> tempa15) {
        if (tempa15.size() == 1) {
            holder.label1a15.setText(tempa15.get(0).getLabel());
            holder.option1a15.setMax(mData.get(position).getResults_by_age().get(1).getSet());
            holder.option1a15.setProgress(tempa15.get(0).getCount());
            int percentagetempa15 = 0;
            if( (mData.get(position).getResults_by_age().get(1).getSet()) > 0){
                percentagetempa15 = ((tempa15.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(1).getSet());
            }
            holder.percentage1a15.setText("" + percentagetempa15 + "%");
            holder.l3a15.setVisibility(View.GONE);
            holder.l4a15.setVisibility(View.GONE);
            holder.l5a15.setVisibility(View.GONE);
        }
        if (tempa15.size() == 2) {
            holder.label1a15.setText(tempa15.get(0).getLabel());
            holder.option1a15.setMax(mData.get(position).getResults_by_age().get(1).getSet());
            holder.option1a15.setProgress(tempa15.get(0).getCount());
            int percentagetempa15 = 0;
            if( (mData.get(position).getResults_by_age().get(1).getSet()) > 0){
                percentagetempa15 = ((tempa15.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(1).getSet());
            }
            holder.percentage1a15.setText("" + percentagetempa15 + "%");
            holder.label2a15.setText(tempa15.get(1).getLabel());
            holder.option2a15.setMax(mData.get(position).getResults_by_age().get(1).getSet());
            holder.option2a15.setProgress(tempa15.get(1).getCount());
            int percentageTemp2a15 = 0;
            if( (mData.get(position).getResults_by_age().get(1).getSet()) > 0){
                percentageTemp2a15 = ((tempa15.get(1).getCount()) * 100) / (mData.get(position).getResults_by_age().get(1).getSet());
            }
            holder.percentage2a15.setText("" + percentageTemp2a15 + "%");
            holder.l3a15.setVisibility(View.GONE);
            holder.l4a15.setVisibility(View.GONE);
            holder.l5a15.setVisibility(View.GONE);
        }
        if (tempa15.size() == 3) {
            holder.label1a15.setText(tempa15.get(0).getLabel());
            holder.option1a15.setMax(mData.get(position).getResults_by_age().get(1).getSet());
            holder.option1a15.setProgress(tempa15.get(0).getCount());
            int percentagetempa15 = 0;
            if( (mData.get(position).getResults_by_age().get(1).getSet()) > 0){
                percentagetempa15 = ((tempa15.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(1).getSet());
            }
            holder.percentage1a15.setText("" + percentagetempa15 + "%");
            holder.label2a15.setText(tempa15.get(1).getLabel());
            holder.option2a15.setMax(mData.get(position).getResults_by_age().get(1).getSet());
            holder.option2a15.setProgress(tempa15.get(1).getCount());
            int percentageTemp2a15 = 0;
            if( (mData.get(position).getResults_by_age().get(1).getSet()) > 0){
                percentageTemp2a15 = ((tempa15.get(1).getCount()) * 100) / (mData.get(position).getResults_by_age().get(1).getSet());
            }
            holder.percentage2a15.setText("" + percentageTemp2a15 + "%");
            holder.l3a15.setVisibility(View.VISIBLE);
            holder.label3a15.setText(tempa15.get(2).getLabel());
            holder.option3a15.setMax(mData.get(position).getResults_by_age().get(1).getSet());
            holder.option3a15.setProgress(tempa15.get(2).getCount());
            int percentageTemp3a15 = 0;
            if( (mData.get(position).getResults_by_age().get(1).getSet()) > 0){
                percentageTemp3a15 = ((tempa15.get(2).getCount()) * 100) / (mData.get(position).getResults_by_age().get(1).getSet());
            }
            holder.percentage3a15.setText("" + percentageTemp3a15 + "%");

            holder.l4a15.setVisibility(View.GONE);
            holder.l5a15.setVisibility(View.GONE);

        } else {
            holder.l3a15.setVisibility(View.GONE);
        }
        if (tempa15.size() == 4) {
            holder.label1a15.setText(tempa15.get(0).getLabel());
            holder.option1a15.setMax(mData.get(position).getResults_by_age().get(1).getSet());
            holder.option1a15.setProgress(tempa15.get(0).getCount());
            int percentagetempa15 = 0;
            if( (mData.get(position).getResults_by_age().get(1).getSet()) > 0){
                percentagetempa15 = ((tempa15.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(1).getSet());
            }
            holder.percentage1a15.setText("" + percentagetempa15 + "%");
            holder.label2a15.setText(tempa15.get(1).getLabel());
            holder.option2a15.setMax(mData.get(position).getResults_by_age().get(1).getSet());
            holder.option2a15.setProgress(tempa15.get(1).getCount());
            int percentageTemp2a15 = 0;
            if( (mData.get(position).getResults_by_age().get(1).getSet()) > 0){
                percentageTemp2a15 = ((tempa15.get(1).getCount()) * 100) / (mData.get(position).getResults_by_age().get(1).getSet());
            }
            holder.percentage2a15.setText("" + percentageTemp2a15 + "%");
            holder.l3a15.setVisibility(View.VISIBLE);
            holder.label3a15.setText(tempa15.get(2).getLabel());
            holder.option3a15.setMax(mData.get(position).getResults_by_age().get(1).getSet());
            holder.option3a15.setProgress(tempa15.get(2).getCount());
            holder.l4a15.setVisibility(View.VISIBLE);
            holder.label4a15.setText(tempa15.get(3).getLabel());
            holder.option4a15.setMax(mData.get(position).getResults_by_age().get(1).getSet());
            holder.option4a15.setProgress(tempa15.get(3).getCount());
            int percentageTemp3a15 = 0;
            if( (mData.get(position).getResults_by_age().get(1).getSet()) > 0){
                percentageTemp3a15 = ((tempa15.get(2).getCount()) * 100) / (mData.get(position).getResults_by_age().get(1).getSet());
            }
            holder.percentage3a15.setText("" + percentageTemp3a15 + "%");
            int percentageTemp4a15 = 0;
            if( (mData.get(position).getResults_by_age().get(1).getSet()) > 0){
                percentageTemp4a15 = ((tempa15.get(3).getCount()) * 100) / (mData.get(position).getResults_by_age().get(1).getSet());
            }
            holder.percentage4a15.setText("" + percentageTemp4a15 + "%");


        } else {
            holder.l4a15.setVisibility(View.GONE);
        }
        if (tempa15.size() == 5) {
            holder.label1a15.setText(tempa15.get(0).getLabel());
            holder.option1a15.setMax(mData.get(position).getResults_by_age().get(1).getSet());
            holder.option1a15.setProgress(tempa15.get(0).getCount());
            int percentagetempa15 = 0;
            if( (mData.get(position).getResults_by_age().get(1).getSet()) > 0){
                percentagetempa15 = ((tempa15.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(1).getSet());
            }
            holder.percentage1a15.setText("" + percentagetempa15 + "%");
            holder.label2a15.setText(tempa15.get(1).getLabel());
            holder.option2a15.setMax(mData.get(position).getResults_by_age().get(1).getSet());
            holder.option2a15.setProgress(tempa15.get(1).getCount());
            int percentageTemp2a15 = 0;
            if( (mData.get(position).getResults_by_age().get(1).getSet()) > 0){
                percentageTemp2a15 = ((tempa15.get(1).getCount()) * 100) / (mData.get(position).getResults_by_age().get(1).getSet());
            }
            holder.percentage2a15.setText("" + percentageTemp2a15 + "%");
            holder.l3a15.setVisibility(View.VISIBLE);
            holder.label3a15.setText(tempa15.get(2).getLabel());
            holder.option3a15.setMax(mData.get(position).getResults_by_age().get(1).getSet());
            holder.option3a15.setProgress(tempa15.get(2).getCount());
            holder.l4a15.setVisibility(View.VISIBLE);
            holder.label4a15.setText(tempa15.get(3).getLabel());
            holder.option4a15.setMax(mData.get(position).getResults_by_age().get(1).getSet());
            holder.option4a15.setProgress(tempa15.get(3).getCount());
            holder.l5a15.setVisibility(View.VISIBLE);
            holder.label5a15.setText(tempa15.get(4).getLabel());
            holder.option5a15.setMax(mData.get(position).getResults_by_age().get(1).getSet());
            holder.option5a15.setProgress(tempa15.get(4).getCount());
            int percentageTemp3a15 = 0;
            if( (mData.get(position).getResults_by_age().get(1).getSet()) > 0){
                percentageTemp3a15 = ((tempa15.get(2).getCount()) * 100) / (mData.get(position).getResults_by_age().get(1).getSet());
            }
            holder.percentage3a15.setText("" + percentageTemp3a15 + "%");
            int percentageTemp4a15 = 0;
            if( (mData.get(position).getResults_by_age().get(1).getSet()) > 0){
                percentageTemp4a15 = ((tempa15.get(3).getCount()) * 100) / (mData.get(position).getResults_by_age().get(1).getSet());
            }
            holder.percentage4a15.setText("" + percentageTemp4a15 + "%");
            int percentageTemp5a15 = 0;
            if( (mData.get(position).getResults_by_age().get(1).getSet()) > 0){
                percentageTemp5a15 = ((tempa15.get(4).getCount()) * 100) / (mData.get(position).getResults_by_age().get(1).getSet());
            }
            holder.percentage5a15.setText("" + percentageTemp5a15 + "%");

        } else {
            holder.l5a15.setVisibility(View.GONE);
        }
        if (tempa15.size() > 5) {
            holder.label1a15.setText(tempa15.get(0).getLabel());
            holder.option1a15.setMax(mData.get(position).getResults_by_age().get(1).getSet());
            holder.option1a15.setProgress(tempa15.get(0).getCount());
            int percentagetempa15 = 0;
            if( (mData.get(position).getResults_by_age().get(1).getSet()) > 0){
                percentagetempa15 = ((tempa15.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(1).getSet());
            }
            holder.percentage1a15.setText("" + percentagetempa15 + "%");
            holder.label2a15.setText(tempa15.get(1).getLabel());
            holder.option2a15.setMax(mData.get(position).getResults_by_age().get(1).getSet());
            holder.option2a15.setProgress(tempa15.get(1).getCount());
            int percentageTemp2a15 = 0;
            if( (mData.get(position).getResults_by_age().get(1).getSet()) > 0){
                percentageTemp2a15 = ((tempa15.get(1).getCount()) * 100) / (mData.get(position).getResults_by_age().get(1).getSet());
            }
            holder.percentage2a15.setText("" + percentageTemp2a15 + "%");
            holder.l3a15.setVisibility(View.GONE);
            holder.l4a15.setVisibility(View.GONE);
            holder.l5a15.setVisibility(View.GONE);
        }
    }

    private void setupAge35(ViewHolder holder, int position, List<categories> tempa35) {
        if (tempa35.size() == 1) {
            holder.label1a35.setText(tempa35.get(0).getLabel());
            holder.option1a35.setMax(mData.get(position).getResults_by_age().get(5).getSet());
            holder.option1a35.setProgress(tempa35.get(0).getCount());
            int percentagetempa35 =0;
            if((mData.get(position).getResults_by_age().get(5).getSet())!=0){

                percentagetempa35 = ((tempa35.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(5).getSet());

            }else{

            }
            holder.percentage1a35.setText("" + percentagetempa35 + "%");
            holder.l3a35.setVisibility(View.GONE);
            holder.l4a35.setVisibility(View.GONE);
            holder.l5a35.setVisibility(View.GONE);

        }
        if (tempa35.size() == 2) {
            holder.label1a35.setText(tempa35.get(0).getLabel());
            holder.option1a35.setMax(mData.get(position).getResults_by_age().get(5).getSet());
            holder.option1a35.setProgress(tempa35.get(0).getCount());
            int percentagetempa35 = 0;
            if( (mData.get(position).getResults_by_age().get(5).getSet()) > 0){
                percentagetempa35 = ((tempa35.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(5).getSet());
            }
            holder.percentage1a35.setText("" + percentagetempa35 + "%");
            holder.label2a35.setText(tempa35.get(1).getLabel());
            holder.option2a35.setMax(mData.get(position).getResults_by_age().get(5).getSet());
            holder.option2a35.setProgress(tempa35.get(1).getCount());
            int percentageTemp2a35 = 0;
            if( (mData.get(position).getResults_by_age().get(5).getSet()) > 0){
                percentageTemp2a35 = ((tempa35.get(1).getCount()) * 100) / (mData.get(position).getResults_by_age().get(5).getSet());
            }
            holder.percentage2a35.setText("" + percentageTemp2a35 + "%");
            holder.l3a35.setVisibility(View.GONE);
            holder.l4a35.setVisibility(View.GONE);
            holder.l5a35.setVisibility(View.GONE);
        }
        if (tempa35.size() == 3) {
            holder.label1a35.setText(tempa35.get(0).getLabel());
            holder.option1a35.setMax(mData.get(position).getResults_by_age().get(5).getSet());
            holder.option1a35.setProgress(tempa35.get(0).getCount());
            int percentagetempa35 = 0;
            if( (mData.get(position).getResults_by_age().get(5).getSet()) > 0){
                percentagetempa35 = ((tempa35.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(5).getSet());
            }
            holder.percentage1a35.setText("" + percentagetempa35 + "%");
            holder.label2a35.setText(tempa35.get(1).getLabel());
            holder.option2a35.setMax(mData.get(position).getResults_by_age().get(5).getSet());
            holder.option2a35.setProgress(tempa35.get(1).getCount());
            int percentageTemp2a35 = 0;
            if( (mData.get(position).getResults_by_age().get(5).getSet()) > 0){
                percentageTemp2a35 = ((tempa35.get(1).getCount()) * 100) / (mData.get(position).getResults_by_age().get(5).getSet());
            }
            holder.percentage2a35.setText("" + percentageTemp2a35 + "%");
            holder.l3a35.setVisibility(View.VISIBLE);
            holder.label3a35.setText(tempa35.get(2).getLabel());
            holder.option3a35.setMax(mData.get(position).getResults_by_age().get(5).getSet());
            holder.option3a35.setProgress(tempa35.get(2).getCount());
            int percentageTemp3a35 = 0;
            if( (mData.get(position).getResults_by_age().get(5).getSet()) > 0){
                percentageTemp3a35 = ((tempa35.get(2).getCount()) * 100) / (mData.get(position).getResults_by_age().get(5).getSet());
            }
            holder.percentage3a35.setText("" + percentageTemp3a35 + "%");
            holder.l4a35.setVisibility(View.GONE);
            holder.l5a35.setVisibility(View.GONE);

        } else {
            holder.l3a35.setVisibility(View.GONE);
        }
        if (tempa35.size() == 4) {
            holder.label1a35.setText(tempa35.get(0).getLabel());
            holder.option1a35.setMax(mData.get(position).getResults_by_age().get(5).getSet());
            holder.option1a35.setProgress(tempa35.get(0).getCount());
            int percentagetempa35 = 0;
            if( (mData.get(position).getResults_by_age().get(5).getSet()) > 0){
                percentagetempa35 = ((tempa35.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(5).getSet());
            }
            holder.percentage1a35.setText("" + percentagetempa35 + "%");
            holder.label2a35.setText(tempa35.get(1).getLabel());
            holder.option2a35.setMax(mData.get(position).getResults_by_age().get(5).getSet());
            holder.option2a35.setProgress(tempa35.get(1).getCount());
            int percentageTemp2a35 = 0;
            if( (mData.get(position).getResults_by_age().get(5).getSet()) > 0){
                percentageTemp2a35 = ((tempa35.get(1).getCount()) * 100) / (mData.get(position).getResults_by_age().get(5).getSet());
            }
            holder.percentage2a35.setText("" + percentageTemp2a35 + "%");
            holder.l3a35.setVisibility(View.VISIBLE);
            holder.label3a35.setText(tempa35.get(2).getLabel());
            holder.option3a35.setMax(mData.get(position).getResults_by_age().get(5).getSet());
            holder.option3a35.setProgress(tempa35.get(2).getCount());
            holder.l4a35.setVisibility(View.VISIBLE);
            holder.label4a35.setText(tempa35.get(3).getLabel());
            holder.option4a35.setMax(mData.get(position).getResults_by_age().get(5).getSet());
            holder.option4a35.setProgress(tempa35.get(3).getCount());
            int percentageTemp3a35 = 0;
            if( (mData.get(position).getResults_by_age().get(5).getSet()) > 0){
                percentageTemp3a35 = ((tempa35.get(2).getCount()) * 100) / (mData.get(position).getResults_by_age().get(5).getSet());
            }
            holder.percentage3a35.setText("" + percentageTemp3a35 + "%");
            int percentageTemp4a35 = 0;
            if( (mData.get(position).getResults_by_age().get(5).getSet()) > 0){
                percentageTemp4a35 = ((tempa35.get(3).getCount()) * 100) / (mData.get(position).getResults_by_age().get(5).getSet());
            }
            holder.percentage4a35.setText("" + percentageTemp4a35 + "%");


        } else {
            holder.l4a35.setVisibility(View.GONE);
        }
        if (tempa35.size() == 5) {
            holder.label1a35.setText(tempa35.get(0).getLabel());
            holder.option1a35.setMax(mData.get(position).getResults_by_age().get(5).getSet());
            holder.option1a35.setProgress(tempa35.get(0).getCount());
            int percentagetempa35 = 0;
            if( (mData.get(position).getResults_by_age().get(5).getSet()) > 0){
                percentagetempa35 = ((tempa35.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(5).getSet());
            }
            holder.percentage1a35.setText("" + percentagetempa35 + "%");
            holder.label2a35.setText(tempa35.get(1).getLabel());
            holder.option2a35.setMax(mData.get(position).getResults_by_age().get(5).getSet());
            holder.option2a35.setProgress(tempa35.get(1).getCount());
            int percentageTemp2a35 = 0;
            if( (mData.get(position).getResults_by_age().get(5).getSet()) > 0){
                percentageTemp2a35 = ((tempa35.get(1).getCount()) * 100) / (mData.get(position).getResults_by_age().get(5).getSet());
            }
            holder.percentage2a35.setText("" + percentageTemp2a35 + "%");
            holder.l3a35.setVisibility(View.VISIBLE);
            holder.label3a35.setText(tempa35.get(2).getLabel());
            holder.option3a35.setMax(mData.get(position).getResults_by_age().get(5).getSet());
            holder.option3a35.setProgress(tempa35.get(2).getCount());
            holder.l4a35.setVisibility(View.VISIBLE);
            holder.label4a35.setText(tempa35.get(3).getLabel());
            holder.option4a35.setMax(mData.get(position).getResults_by_age().get(5).getSet());
            holder.option4a35.setProgress(tempa35.get(3).getCount());
            holder.l5a35.setVisibility(View.VISIBLE);
            holder.label5a35.setText(tempa35.get(4).getLabel());
            holder.option5a35.setMax(mData.get(position).getResults_by_age().get(5).getSet());
            holder.option5a35.setProgress(tempa35.get(4).getCount());
            int percentageTemp3a35 = 0;
            if( (mData.get(position).getResults_by_age().get(5).getSet()) > 0){
                percentageTemp3a35 = ((tempa35.get(2).getCount()) * 100) / (mData.get(position).getResults_by_age().get(5).getSet());
            }
            holder.percentage3a35.setText("" + percentageTemp3a35 + "%");
            int percentageTemp4a35 = 0;
            if( (mData.get(position).getResults_by_age().get(5).getSet()) > 0){
                percentageTemp4a35 = ((tempa35.get(3).getCount()) * 100) / (mData.get(position).getResults_by_age().get(5).getSet());
            }
            holder.percentage4a35.setText("" + percentageTemp4a35 + "%");
            int percentageTemp5a35 = 0;
            if( (mData.get(position).getResults_by_age().get(5).getSet()) > 0){
                percentageTemp5a35 = ((tempa35.get(4).getCount()) * 100) / (mData.get(position).getResults_by_age().get(5).getSet());
            }
            holder.percentage5a35.setText("" + percentageTemp5a35 + "%");

        } else {
            holder.l5a35.setVisibility(View.GONE);
        }
        if (tempa35.size() > 5) {
            holder.label1a35.setText(tempa35.get(0).getLabel());
            holder.option1a35.setMax(mData.get(position).getResults_by_age().get(5).getSet());
            holder.option1a35.setProgress(tempa35.get(0).getCount());
            int percentagetempa35 = 0;
            if( (mData.get(position).getResults_by_age().get(5).getSet()) > 0){
                percentagetempa35 = ((tempa35.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(5).getSet());
            }
            holder.percentage1a35.setText("" + percentagetempa35 + "%");
            holder.label2a35.setText(tempa35.get(1).getLabel());
            holder.option2a35.setMax(mData.get(position).getResults_by_age().get(5).getSet());
            holder.option2a31.setProgress(tempa35.get(1).getCount());
            int percentageTemp2a31 = 0;
            if( (mData.get(position).getResults_by_age().get(5).getSet()) > 0){
                percentageTemp2a31 = ((tempa35.get(1).getCount()) * 100) / (mData.get(position).getResults_by_age().get(5).getSet());
            }
            holder.percentage2a31.setText("" + percentageTemp2a31 + "%");
            holder.l3a31.setVisibility(View.GONE);
            holder.l4a31.setVisibility(View.GONE);
            holder.l5a31.setVisibility(View.GONE);
        }
    }

    private void setupAge31(ViewHolder holder, int position, List<categories> tempa31) {
        if (tempa31.size() == 1) {
            holder.label1a31.setText(tempa31.get(0).getLabel());
            holder.option1a31.setMax(mData.get(position).getResults_by_age().get(4).getSet());
            holder.option1a31.setProgress(tempa31.get(0).getCount());
            int percentagetempa31 =0;
            if((mData.get(position).getResults_by_age().get(4).getSet())!=0){

                percentagetempa31 = ((tempa31.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(4).getSet());

            }else{

            }
            holder.percentage1a31.setText("" + percentagetempa31 + "%");
            holder.l3a31.setVisibility(View.GONE);
            holder.l4a31.setVisibility(View.GONE);
            holder.l5a31.setVisibility(View.GONE);

        }
        if (tempa31.size() == 2) {
            holder.label1a31.setText(tempa31.get(0).getLabel());
            holder.option1a31.setMax(mData.get(position).getResults_by_age().get(4).getSet());
            holder.option1a31.setProgress(tempa31.get(0).getCount());
            int percentagetempa31 = 0;
            if( (mData.get(position).getResults_by_age().get(4).getSet()) > 0){
                percentagetempa31 = ((tempa31.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(4).getSet());
            }
            holder.percentage1a31.setText("" + percentagetempa31 + "%");
            holder.label2a31.setText(tempa31.get(1).getLabel());
            holder.option2a31.setMax(mData.get(position).getResults_by_age().get(4).getSet());
            holder.option2a31.setProgress(tempa31.get(1).getCount());
            int percentageTemp2a31 = 0;
            if( (mData.get(position).getResults_by_age().get(4).getSet()) > 0){
                percentageTemp2a31 = ((tempa31.get(1).getCount()) * 100) / (mData.get(position).getResults_by_age().get(4).getSet());
            }
            holder.percentage2a31.setText("" + percentageTemp2a31 + "%");
            holder.l3a31.setVisibility(View.GONE);
            holder.l4a31.setVisibility(View.GONE);
            holder.l5a31.setVisibility(View.GONE);
        }
        if (tempa31.size() == 3) {
            holder.label1a31.setText(tempa31.get(0).getLabel());
            holder.option1a31.setMax(mData.get(position).getResults_by_age().get(4).getSet());
            holder.option1a31.setProgress(tempa31.get(0).getCount());
            int percentagetempa31 = 0;
            if( (mData.get(position).getResults_by_age().get(4).getSet()) > 0){
                percentagetempa31 = ((tempa31.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(4).getSet());
            }
            holder.percentage1a31.setText("" + percentagetempa31 + "%");
            holder.label2a31.setText(tempa31.get(1).getLabel());
            holder.option2a31.setMax(mData.get(position).getResults_by_age().get(4).getSet());
            holder.option2a31.setProgress(tempa31.get(1).getCount());
            int percentageTemp2a31 = 0;
            if( (mData.get(position).getResults_by_age().get(4).getSet()) > 0){
                percentageTemp2a31 = ((tempa31.get(1).getCount()) * 100) / (mData.get(position).getResults_by_age().get(4).getSet());
            }
            holder.percentage2a31.setText("" + percentageTemp2a31 + "%");
            holder.l3a31.setVisibility(View.VISIBLE);
            holder.label3a31.setText(tempa31.get(2).getLabel());
            holder.option3a31.setMax(mData.get(position).getResults_by_age().get(4).getSet());
            holder.option3a31.setProgress(tempa31.get(2).getCount());
            int percentageTemp3a31 = 0;
            if( (mData.get(position).getResults_by_age().get(4).getSet()) > 0){
                percentageTemp3a31 = ((tempa31.get(2).getCount()) * 100) / (mData.get(position).getResults_by_age().get(4).getSet());
            }
            holder.percentage3a31.setText("" + percentageTemp3a31 + "%");
            holder.l4a31.setVisibility(View.GONE);
            holder.l5a31.setVisibility(View.GONE);

        } else {
            holder.l3a31.setVisibility(View.GONE);
        }
        if (tempa31.size() == 4) {
            holder.label1a31.setText(tempa31.get(0).getLabel());
            holder.option1a31.setMax(mData.get(position).getResults_by_age().get(4).getSet());
            holder.option1a31.setProgress(tempa31.get(0).getCount());
            int percentagetempa31 = 0;
            if( (mData.get(position).getResults_by_age().get(4).getSet()) > 0){
                percentagetempa31 = ((tempa31.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(4).getSet());
            }
            holder.percentage1a31.setText("" + percentagetempa31 + "%");
            holder.label2a31.setText(tempa31.get(1).getLabel());
            holder.option2a31.setMax(mData.get(position).getResults_by_age().get(4).getSet());
            holder.option2a31.setProgress(tempa31.get(1).getCount());
            int percentageTemp2a31 = 0;
            if( (mData.get(position).getResults_by_age().get(4).getSet()) > 0){
                percentageTemp2a31 = ((tempa31.get(1).getCount()) * 100) / (mData.get(position).getResults_by_age().get(4).getSet());
            }
            holder.percentage2a31.setText("" + percentageTemp2a31 + "%");
            holder.l3a31.setVisibility(View.VISIBLE);
            holder.label3a31.setText(tempa31.get(2).getLabel());
            holder.option3a31.setMax(mData.get(position).getResults_by_age().get(4).getSet());
            holder.option3a31.setProgress(tempa31.get(2).getCount());
            holder.l4a31.setVisibility(View.VISIBLE);
            holder.label4a31.setText(tempa31.get(3).getLabel());
            holder.option4a31.setMax(mData.get(position).getResults_by_age().get(4).getSet());
            holder.option4a31.setProgress(tempa31.get(3).getCount());
            int percentageTemp3a31 = 0;
            if( (mData.get(position).getResults_by_age().get(4).getSet()) > 0){
                percentageTemp3a31 = ((tempa31.get(2).getCount()) * 100) / (mData.get(position).getResults_by_age().get(4).getSet());
            }
            holder.percentage3a31.setText("" + percentageTemp3a31 + "%");
            int percentageTemp4a31 = 0;
            if( (mData.get(position).getResults_by_age().get(4).getSet()) > 0){
                percentageTemp4a31 = ((tempa31.get(3).getCount()) * 100) / (mData.get(position).getResults_by_age().get(4).getSet());
            }
            holder.percentage4a31.setText("" + percentageTemp4a31 + "%");


        } else {
            holder.l4a31.setVisibility(View.GONE);
        }
        if (tempa31.size() == 5) {
            holder.label1a31.setText(tempa31.get(0).getLabel());
            holder.option1a31.setMax(mData.get(position).getResults_by_age().get(4).getSet());
            holder.option1a31.setProgress(tempa31.get(0).getCount());
            int percentagetempa31 = 0;
            if( (mData.get(position).getResults_by_age().get(4).getSet()) > 0){
                percentagetempa31 = ((tempa31.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(4).getSet());
            }
            holder.percentage1a31.setText("" + percentagetempa31 + "%");
            holder.label2a31.setText(tempa31.get(1).getLabel());
            holder.option2a31.setMax(mData.get(position).getResults_by_age().get(4).getSet());
            holder.option2a31.setProgress(tempa31.get(1).getCount());
            int percentageTemp2a31 = 0;
            if( (mData.get(position).getResults_by_age().get(4).getSet()) > 0){
                percentageTemp2a31 = ((tempa31.get(1).getCount()) * 100) / (mData.get(position).getResults_by_age().get(4).getSet());
            }
            holder.percentage2a31.setText("" + percentageTemp2a31 + "%");
            holder.l3a31.setVisibility(View.VISIBLE);
            holder.label3a31.setText(tempa31.get(2).getLabel());
            holder.option3a31.setMax(mData.get(position).getResults_by_age().get(4).getSet());
            holder.option3a31.setProgress(tempa31.get(2).getCount());
            holder.l4a31.setVisibility(View.VISIBLE);
            holder.label4a31.setText(tempa31.get(3).getLabel());
            holder.option4a31.setMax(mData.get(position).getResults_by_age().get(4).getSet());
            holder.option4a31.setProgress(tempa31.get(3).getCount());
            holder.l5a31.setVisibility(View.VISIBLE);
            holder.label5a31.setText(tempa31.get(4).getLabel());
            holder.option5a31.setMax(mData.get(position).getResults_by_age().get(4).getSet());
            holder.option5a31.setProgress(tempa31.get(4).getCount());
            int percentageTemp3a31 = 0;
            if( (mData.get(position).getResults_by_age().get(4).getSet()) > 0){
                percentageTemp3a31 = ((tempa31.get(2).getCount()) * 100) / (mData.get(position).getResults_by_age().get(4).getSet());
            }
            holder.percentage3a31.setText("" + percentageTemp3a31 + "%");
            int percentageTemp4a31 = 0;
            if( (mData.get(position).getResults_by_age().get(4).getSet()) > 0){
                percentageTemp4a31 = ((tempa31.get(3).getCount()) * 100) / (mData.get(position).getResults_by_age().get(4).getSet());
            }
            holder.percentage4a31.setText("" + percentageTemp4a31 + "%");
            int percentageTemp5a31 = 0;
            if( (mData.get(position).getResults_by_age().get(4).getSet()) > 0){
                percentageTemp5a31 = ((tempa31.get(4).getCount()) * 100) / (mData.get(position).getResults_by_age().get(4).getSet());
            }
            holder.percentage5a31.setText("" + percentageTemp5a31 + "%");

        } else {
            holder.l5a31.setVisibility(View.GONE);
        }
        if (tempa31.size() > 5) {
            holder.label1a31.setText(tempa31.get(0).getLabel());
            holder.option1a31.setMax(mData.get(position).getResults_by_age().get(4).getSet());
            holder.option1a31.setProgress(tempa31.get(0).getCount());
            int percentagetempa31 = 0;
            if( mData.get(position).getResults_by_age().get(4).getSet() > 0){
                percentagetempa31 = ((tempa31.get(0).getCount()) * 100) / mData.get(position).getResults_by_age().get(4).getSet();
            }
            holder.percentage1a31.setText("" + percentagetempa31 + "%");
            holder.label2a31.setText(tempa31.get(1).getLabel());
            holder.option2a31.setMax(mData.get(position).getResults_by_age().get(4).getSet());
            holder.option2a31.setProgress(tempa31.get(1).getCount());
            int percentageTemp2a31 = 0;
            if( (mData.get(position).getResults_by_age().get(4).getSet()) > 0){
                percentageTemp2a31 = ((tempa31.get(1).getCount()) * 100) / (mData.get(position).getResults_by_age().get(4).getSet());
            }
            holder.percentage2a31.setText("" + percentageTemp2a31 + "%");
            holder.l3a31.setVisibility(View.GONE);
            holder.l4a31.setVisibility(View.GONE);
            holder.l5a31.setVisibility(View.GONE);
        }
    }

    private void setupAge20(ViewHolder holder, int position, List<categories> tempa20) {
        if (tempa20.size() == 1) {
            holder.label1a20.setText(tempa20.get(0).getLabel());
            holder.option1a20.setMax(mData.get(position).getResults_by_age().get(2).getSet());
            holder.option1a20.setProgress(tempa20.get(0).getCount());
            int percentagetempa20 = 0;
            if( (mData.get(position).getResults_by_age().get(2).getSet()) > 0){
                percentagetempa20 = ((tempa20.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(2).getSet());
            }
            holder.percentage1a20.setText("" + percentagetempa20 + "%");
            holder.l3a20.setVisibility(View.GONE);
            holder.l4a20.setVisibility(View.GONE);
            holder.l5a20.setVisibility(View.GONE);
        }
        if (tempa20.size() == 2) {
            holder.label1a20.setText(tempa20.get(0).getLabel());
            holder.option1a20.setMax(mData.get(position).getResults_by_age().get(2).getSet());
            holder.option1a20.setProgress(tempa20.get(0).getCount());
            int percentagetempa20 = 0;
            if( (mData.get(position).getResults_by_age().get(2).getSet()) > 0){
                percentagetempa20 = ((tempa20.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(2).getSet());
            }
            holder.percentage1a20.setText("" + percentagetempa20 + "%");
            holder.label2a20.setText(tempa20.get(1).getLabel());
            holder.option2a20.setMax(mData.get(position).getResults_by_age().get(2).getSet());
            holder.option2a20.setProgress(tempa20.get(1).getCount());
            int percentageTemp2a20 = 0;
            if( (mData.get(position).getResults_by_age().get(2).getSet()) > 0){
                percentageTemp2a20 = ((tempa20.get(1).getCount()) * 100) / (mData.get(position).getResults_by_age().get(2).getSet());
            }
            holder.percentage2a20.setText("" + percentageTemp2a20 + "%");
            holder.l3a20.setVisibility(View.GONE);
            holder.l4a20.setVisibility(View.GONE);
            holder.l5a20.setVisibility(View.GONE);
        }
        if (tempa20.size() == 3) {
            holder.label1a20.setText(tempa20.get(0).getLabel());
            holder.option1a20.setMax(mData.get(position).getResults_by_age().get(2).getSet());
            holder.option1a20.setProgress(tempa20.get(0).getCount());
            int percentagetempa20 = 0;
            if( (mData.get(position).getResults_by_age().get(2).getSet()) > 0){
                percentagetempa20 = ((tempa20.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(2).getSet());
            }
            holder.percentage1a20.setText("" + percentagetempa20 + "%");
            holder.label2a20.setText(tempa20.get(1).getLabel());
            holder.option2a20.setMax(mData.get(position).getResults_by_age().get(2).getSet());
            holder.option2a20.setProgress(tempa20.get(1).getCount());
            int percentageTemp2a20 = 0;
            if( (mData.get(position).getResults_by_age().get(2).getSet()) > 0){
                percentageTemp2a20 = ((tempa20.get(1).getCount()) * 100) / (mData.get(position).getResults_by_age().get(2).getSet());
            }
            holder.percentage2a20.setText("" + percentageTemp2a20 + "%");
            holder.l3a20.setVisibility(View.VISIBLE);
            holder.label3a20.setText(tempa20.get(2).getLabel());
            holder.option3a20.setMax(mData.get(position).getResults_by_age().get(2).getSet());
            holder.option3a20.setProgress(tempa20.get(2).getCount());
            int percentageTemp3a20 = 0;
            if( (mData.get(position).getResults_by_age().get(2).getSet()) > 0){
                percentageTemp3a20 = ((tempa20.get(2).getCount()) * 100) / (mData.get(position).getResults_by_age().get(2).getSet());
            }
            holder.percentage3a20.setText("" + percentageTemp3a20 + "%");

            holder.l4a20.setVisibility(View.GONE);
            holder.l5a20.setVisibility(View.GONE);

        } else {
            holder.l3a20.setVisibility(View.GONE);
        }
        if (tempa20.size() == 4) {
            holder.label1a20.setText(tempa20.get(0).getLabel());
            holder.option1a20.setMax(mData.get(position).getResults_by_age().get(2).getSet());
            holder.option1a20.setProgress(tempa20.get(0).getCount());
            int percentagetempa20 = 0;
            if( (mData.get(position).getResults_by_age().get(2).getSet()) > 0){
                percentagetempa20 = ((tempa20.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(2).getSet());
            }
            holder.percentage1a20.setText("" + percentagetempa20 + "%");
            holder.label2a20.setText(tempa20.get(1).getLabel());
            holder.option2a20.setMax(mData.get(position).getResults_by_age().get(2).getSet());
            holder.option2a20.setProgress(tempa20.get(1).getCount());
            int percentageTemp2a20 = 0;
            if( (mData.get(position).getResults_by_age().get(2).getSet()) > 0){
                percentageTemp2a20 = ((tempa20.get(1).getCount()) * 100) / (mData.get(position).getResults_by_age().get(2).getSet());
            }
            holder.percentage2a20.setText("" + percentageTemp2a20 + "%");
            holder.l3a20.setVisibility(View.VISIBLE);
            holder.label3a20.setText(tempa20.get(2).getLabel());
            holder.option3a20.setMax(mData.get(position).getResults_by_age().get(2).getSet());
            holder.option3a20.setProgress(tempa20.get(2).getCount());
            holder.l4a20.setVisibility(View.VISIBLE);
            holder.label4a20.setText(tempa20.get(3).getLabel());
            holder.option4a20.setMax(mData.get(position).getResults_by_age().get(2).getSet());
            holder.option4a20.setProgress(tempa20.get(3).getCount());
            int percentageTemp3a20 = 0;
            if( (mData.get(position).getResults_by_age().get(2).getSet()) > 0){
                percentageTemp3a20 = ((tempa20.get(2).getCount()) * 100) / (mData.get(position).getResults_by_age().get(2).getSet());
            }
            holder.percentage3a20.setText("" + percentageTemp3a20 + "%");
            int percentageTemp4a20 = 0;
            if( (mData.get(position).getResults_by_age().get(2).getSet()) > 0){
                percentageTemp4a20 = ((tempa20.get(3).getCount()) * 100) / (mData.get(position).getResults_by_age().get(2).getSet());
            }
            holder.percentage4a20.setText("" + percentageTemp4a20 + "%");


        } else {
            holder.l4a20.setVisibility(View.GONE);
        }
        if (tempa20.size() == 5) {
            holder.label1a20.setText(tempa20.get(0).getLabel());
            holder.option1a20.setMax(mData.get(position).getResults_by_age().get(2).getSet());
            holder.option1a20.setProgress(tempa20.get(0).getCount());
            int percentagetempa20 = 0;
            if( (mData.get(position).getResults_by_age().get(2).getSet()) > 0){
                percentagetempa20 = ((tempa20.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(2).getSet());
            }
            holder.percentage1a20.setText("" + percentagetempa20 + "%");
            holder.label2a20.setText(tempa20.get(1).getLabel());
            holder.option2a20.setMax(mData.get(position).getResults_by_age().get(2).getSet());
            holder.option2a20.setProgress(tempa20.get(1).getCount());
            int percentageTemp2a20 = 0;
            if( (mData.get(position).getResults_by_age().get(2).getSet()) > 0){
                percentageTemp2a20 = ((tempa20.get(1).getCount()) * 100) / (mData.get(position).getResults_by_age().get(2).getSet());
            }
            holder.percentage2a20.setText("" + percentageTemp2a20 + "%");
            holder.l3a20.setVisibility(View.VISIBLE);
            holder.label3a20.setText(tempa20.get(2).getLabel());
            holder.option3a20.setMax(mData.get(position).getResults_by_age().get(2).getSet());
            holder.option3a20.setProgress(tempa20.get(2).getCount());
            holder.l4a20.setVisibility(View.VISIBLE);
            holder.label4a20.setText(tempa20.get(3).getLabel());
            holder.option4a20.setMax(mData.get(position).getResults_by_age().get(2).getSet());
            holder.option4a20.setProgress(tempa20.get(3).getCount());
            holder.l5a20.setVisibility(View.VISIBLE);
            holder.label5a20.setText(tempa20.get(4).getLabel());
            holder.option5a20.setMax(mData.get(position).getResults_by_age().get(2).getSet());
            holder.option5a20.setProgress(tempa20.get(4).getCount());
            int percentageTemp3a20 = 0;
            if( (mData.get(position).getResults_by_age().get(2).getSet()) > 0){
                percentageTemp3a20 = ((tempa20.get(2).getCount()) * 100) / (mData.get(position).getResults_by_age().get(2).getSet());
            }
            holder.percentage3a20.setText("" + percentageTemp3a20 + "%");
            int percentageTemp4a20 = 0;
            if( (mData.get(position).getResults_by_age().get(2).getSet()) > 0){
                percentageTemp4a20 = ((tempa20.get(3).getCount()) * 100) / (mData.get(position).getResults_by_age().get(2).getSet());
            }
            holder.percentage4a20.setText("" + percentageTemp4a20 + "%");
            int percentageTemp5a20 = 0;
            if( (mData.get(position).getResults_by_age().get(2).getSet()) > 0){
                percentageTemp5a20 = ((tempa20.get(4).getCount()) * 100) / (mData.get(position).getResults_by_age().get(2).getSet());
            }
            holder.percentage5a20.setText("" + percentageTemp5a20 + "%");

        } else {
            holder.l5a20.setVisibility(View.GONE);
        }
        if (tempa20.size() > 5) {
            holder.label1a20.setText(tempa20.get(0).getLabel());
            holder.option1a20.setMax(mData.get(position).getResults_by_age().get(2).getSet());
            holder.option1a20.setProgress(tempa20.get(0).getCount());
            int percentagetempa20 = 0;
            if( (mData.get(position).getResults_by_age().get(2).getSet()) > 0){
                percentagetempa20 = ((tempa20.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(2).getSet());
            }
            holder.percentage1a20.setText("" + percentagetempa20 + "%");
            holder.label2a20.setText(tempa20.get(1).getLabel());
            holder.option2a20.setMax(mData.get(position).getResults_by_age().get(2).getSet());
            holder.option2a20.setProgress(tempa20.get(1).getCount());
            int percentageTemp2a20 = 0;
            if( (mData.get(position).getResults_by_age().get(2).getSet()) > 0){
                percentageTemp2a20 = ((tempa20.get(1).getCount()) * 100) / (mData.get(position).getResults_by_age().get(2).getSet());
            }
            holder.percentage2a20.setText("" + percentageTemp2a20 + "%");
            holder.l3a20.setVisibility(View.GONE);
            holder.l4a20.setVisibility(View.GONE);
            holder.l5a20.setVisibility(View.GONE);
        }
    }

    private void setupAge25(ViewHolder holder, int position, List<categories> tempa25) {
        if (tempa25.size() == 1) {
            holder.label1a25.setText(tempa25.get(0).getLabel());
            holder.option1a25.setMax(mData.get(position).getResults_by_age().get(3).getSet());
            holder.option1a25.setProgress(tempa25.get(0).getCount());
            int percentagetempa25 = 0;
            if( (mData.get(position).getResults_by_age().get(3).getSet()) > 0){
                percentagetempa25 = ((tempa25.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(3).getSet());
            }
            holder.percentage1a25.setText("" + percentagetempa25 + "%");
            holder.l3a25.setVisibility(View.GONE);
            holder.l4a25.setVisibility(View.GONE);
            holder.l5a25.setVisibility(View.GONE);
        }
        if (tempa25.size() == 2) {
            holder.label1a25.setText(tempa25.get(0).getLabel());
            holder.option1a25.setMax(mData.get(position).getResults_by_age().get(3).getSet());
            holder.option1a25.setProgress(tempa25.get(0).getCount());
            int percentagetempa25 = 0;
            if( (mData.get(position).getResults_by_age().get(3).getSet()) > 0){
                percentagetempa25 = ((tempa25.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(3).getSet());
            }
            holder.percentage1a25.setText("" + percentagetempa25 + "%");
            holder.label2a25.setText(tempa25.get(1).getLabel());
            holder.option2a25.setMax(mData.get(position).getResults_by_age().get(3).getSet());
            holder.option2a25.setProgress(tempa25.get(1).getCount());
            int percentageTemp2a25 = 0;
            if( (mData.get(position).getResults_by_age().get(3).getSet()) > 0){
                percentageTemp2a25 = ((tempa25.get(1).getCount()) * 100) / (mData.get(position).getResults_by_age().get(3).getSet());
            }
            holder.percentage2a25.setText("" + percentageTemp2a25 + "%");
            holder.l3a25.setVisibility(View.GONE);
            holder.l4a25.setVisibility(View.GONE);
            holder.l5a25.setVisibility(View.GONE);
        }
        if (tempa25.size() == 3) {
            holder.label1a25.setText(tempa25.get(0).getLabel());
            holder.option1a25.setMax(mData.get(position).getResults_by_age().get(3).getSet());
            holder.option1a25.setProgress(tempa25.get(0).getCount());
            int percentagetempa25 = 0;
            if( (mData.get(position).getResults_by_age().get(3).getSet()) > 0){
                percentagetempa25 = ((tempa25.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(3).getSet());
            }
            holder.percentage1a25.setText("" + percentagetempa25 + "%");
            holder.label2a25.setText(tempa25.get(1).getLabel());
            holder.option2a25.setMax(mData.get(position).getResults_by_age().get(3).getSet());
            holder.option2a25.setProgress(tempa25.get(1).getCount());
            int percentageTemp2a25 = 0;
            if( (mData.get(position).getResults_by_age().get(3).getSet()) > 0){
                percentageTemp2a25 = ((tempa25.get(1).getCount()) * 100) / (mData.get(position).getResults_by_age().get(3).getSet());
            }
            holder.percentage2a25.setText("" + percentageTemp2a25 + "%");
            holder.l3a25.setVisibility(View.VISIBLE);
            holder.label3a25.setText(tempa25.get(2).getLabel());
            holder.option3a25.setMax(mData.get(position).getResults_by_age().get(3).getSet());
            holder.option3a25.setProgress(tempa25.get(2).getCount());
            int percentageTemp3a25 = 0;
            if( (mData.get(position).getResults_by_age().get(3).getSet()) > 0){
                percentageTemp3a25 = ((tempa25.get(2).getCount()) * 100) / (mData.get(position).getResults_by_age().get(3).getSet());
            }
            holder.percentage3a25.setText("" + percentageTemp3a25 + "%");
            holder.l4a25.setVisibility(View.GONE);
            holder.l5a25.setVisibility(View.GONE);

        } else {
            holder.l3a25.setVisibility(View.GONE);
        }
        if (tempa25.size() == 4) {
            holder.label1a25.setText(tempa25.get(0).getLabel());
            holder.option1a25.setMax(mData.get(position).getResults_by_age().get(3).getSet());
            holder.option1a25.setProgress(tempa25.get(0).getCount());
            int percentagetempa25 = 0;
            if( (mData.get(position).getResults_by_age().get(3).getSet()) > 0){
                percentagetempa25 = ((tempa25.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(3).getSet());
            }
            holder.percentage1a25.setText("" + percentagetempa25 + "%");
            holder.label2a25.setText(tempa25.get(1).getLabel());
            holder.option2a25.setMax(mData.get(position).getResults_by_age().get(3).getSet());
            holder.option2a25.setProgress(tempa25.get(1).getCount());
            int percentageTemp2a25 = 0;
            if( (mData.get(position).getResults_by_age().get(3).getSet()) > 0){
                percentageTemp2a25 = ((tempa25.get(1).getCount()) * 100) / (mData.get(position).getResults_by_age().get(3).getSet());
            }
            holder.percentage2a25.setText("" + percentageTemp2a25 + "%");
            holder.l3a25.setVisibility(View.VISIBLE);
            holder.label3a25.setText(tempa25.get(2).getLabel());
            holder.option3a25.setMax(mData.get(position).getResults_by_age().get(3).getSet());
            holder.option3a25.setProgress(tempa25.get(3).getCount());
            holder.l4a25.setVisibility(View.VISIBLE);
            holder.label4a25.setText(tempa25.get(3).getLabel());
            holder.option4a25.setMax(mData.get(position).getResults_by_age().get(3).getSet());
            holder.option4a25.setProgress(tempa25.get(3).getCount());
            int percentageTemp3a25 = 0;
            if( (mData.get(position).getResults_by_age().get(3).getSet()) > 0){
                percentageTemp3a25 = ((tempa25.get(2).getCount()) * 100) / (mData.get(position).getResults_by_age().get(3).getSet());
            }
            holder.percentage3a25.setText("" + percentageTemp3a25 + "%");
            int percentageTemp4a25 = 0;
            if( (mData.get(position).getResults_by_age().get(3).getSet()) > 0){
                percentageTemp4a25 = ((tempa25.get(3).getCount()) * 100) / (mData.get(position).getResults_by_age().get(3).getSet());
            }
            holder.percentage4a25.setText("" + percentageTemp4a25 + "%");


        } else {
            holder.l4a25.setVisibility(View.GONE);
        }
        if (tempa25.size() == 5) {
            holder.label1a25.setText(tempa25.get(0).getLabel());
            holder.option1a25.setMax(mData.get(position).getResults_by_age().get(3).getSet());
            holder.option1a25.setProgress(tempa25.get(0).getCount());
            int percentagetempa25 = 0;
            if( (mData.get(position).getResults_by_age().get(3).getSet()) > 0){
                percentagetempa25 = ((tempa25.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(3).getSet());
            }
            holder.percentage1a25.setText("" + percentagetempa25 + "%");
            holder.label2a25.setText(tempa25.get(1).getLabel());
            holder.option2a25.setMax(mData.get(position).getResults_by_age().get(3).getSet());
            holder.option2a25.setProgress(tempa25.get(1).getCount());
            int percentageTemp2a25 = 0;
            if( (mData.get(position).getResults_by_age().get(3).getSet()) > 0){
                percentageTemp2a25 = ((tempa25.get(1).getCount()) * 100) / (mData.get(position).getResults_by_age().get(3).getSet());
            }
            holder.percentage2a25.setText("" + percentageTemp2a25 + "%");
            holder.l3a25.setVisibility(View.VISIBLE);
            holder.label3a25.setText(tempa25.get(2).getLabel());
            holder.option3a25.setMax(mData.get(position).getResults_by_age().get(3).getSet());
            holder.option3a25.setProgress(tempa25.get(2).getCount());
            holder.l4a25.setVisibility(View.VISIBLE);
            holder.label4a25.setText(tempa25.get(3).getLabel());
            holder.option4a25.setMax(mData.get(position).getResults_by_age().get(3).getSet());
            holder.option4a25.setProgress(tempa25.get(3).getCount());
            holder.l5a25.setVisibility(View.VISIBLE);
            holder.label5a25.setText(tempa25.get(4).getLabel());
            holder.option5a25.setMax(mData.get(position).getResults_by_age().get(3).getSet());
            holder.option5a25.setProgress(tempa25.get(4).getCount());
            int percentageTemp3a25 = 0;
            if( (mData.get(position).getResults_by_age().get(3).getSet()) > 0){
                percentageTemp3a25 = ((tempa25.get(2).getCount()) * 100) / (mData.get(position).getResults_by_age().get(3).getSet());
            }
            holder.percentage3a25.setText("" + percentageTemp3a25 + "%");
            int percentageTemp4a25 = 0;
            if( (mData.get(position).getResults_by_age().get(3).getSet()) > 0){
                percentageTemp4a25 = ((tempa25.get(3).getCount()) * 100) / (mData.get(position).getResults_by_age().get(3).getSet());
            }
            holder.percentage4a25.setText("" + percentageTemp4a25 + "%");
            int percentageTemp5a25 = 0;
            if( (mData.get(position).getResults_by_age().get(3).getSet()) > 0){
                percentageTemp5a25 = ((tempa25.get(4).getCount()) * 100) / (mData.get(position).getResults_by_age().get(3).getSet());
            }
            holder.percentage5a25.setText("" + percentageTemp5a25 + "%");

        } else {
            holder.l5a25.setVisibility(View.GONE);
        }
        if (tempa25.size() > 5) {
            holder.label1a25.setText(tempa25.get(0).getLabel());
            holder.option1a25.setMax(mData.get(position).getResults_by_age().get(3).getSet());
            holder.option1a25.setProgress(tempa25.get(0).getCount());
            int percentagetempa25 = 0;
            if( (mData.get(position).getResults_by_age().get(3).getSet()) > 0){
                percentagetempa25 = ((tempa25.get(0).getCount()) * 100) / (mData.get(position).getResults_by_age().get(3).getSet());
            }
            holder.percentage1a25.setText("" + percentagetempa25 + "%");
            holder.label2a25.setText(tempa25.get(1).getLabel());
            holder.option2a25.setMax(mData.get(position).getResults_by_age().get(3).getSet());
            holder.option2a25.setProgress(tempa25.get(1).getCount());
            int percentageTemp2a25 = 0;
            if( (mData.get(position).getResults_by_age().get(3).getSet()) > 0){
                percentageTemp2a25 = ((tempa25.get(1).getCount()) * 100) / (mData.get(position).getResults_by_age().get(3).getSet());
            }
            holder.percentage2a25.setText("" + percentageTemp2a25 + "%");
            holder.l3a25.setVisibility(View.GONE);
            holder.l4a25.setVisibility(View.GONE);
            holder.l5a25.setVisibility(View.GONE);
        }
    }

    private void setupMale(ViewHolder holder, int position, List<categories> tempm) {
        if (tempm.size() == 1) {
            holder.label1m.setText(mData.get(position).getResults_by_gender().get(1).getCategories().get(0).getLabel());
            holder.option1m.setMax(mData.get(position).getResults_by_gender().get(1).getSet());
            holder.option1m.setProgress(mData.get(position).getResults_by_gender().get(1).getCategories().get(0).getCount());
            int percentageTempm = 0;
            if( (mData.get(position).getResults_by_gender().get(1).getSet()) > 0){
                percentageTempm = ((mData.get(position).getResults_by_gender().get(1).getCategories().get(0).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(1).getSet());
            }
            holder.percentage1m.setText("" + percentageTempm + "%");
        }
        if (tempm.size() == 2) {
            holder.label1m.setText(mData.get(position).getResults_by_gender().get(1).getCategories().get(0).getLabel());
            holder.option1m.setMax(mData.get(position).getResults_by_gender().get(1).getSet());
            holder.option1m.setProgress(mData.get(position).getResults_by_gender().get(1).getCategories().get(0).getCount());
            int percentageTempm = 0;
            if( (mData.get(position).getResults_by_gender().get(1).getSet()) > 0){
                percentageTempm = ((mData.get(position).getResults_by_gender().get(1).getCategories().get(0).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(1).getSet());
            }
            holder.percentage1m.setText("" + percentageTempm + "%");
            holder.label2m.setText(mData.get(position).getResults_by_age().get(1).getCategories().get(1).getLabel());
            holder.option2m.setMax(mData.get(position).getResults_by_gender().get(1).getSet());
            holder.option2m.setProgress(mData.get(position).getResults_by_gender().get(1).getCategories().get(1).getCount());
            int percentageTemp2m = 0;
            if( (mData.get(position).getResults_by_gender().get(1).getSet()) > 0){
                percentageTemp2m = ((mData.get(position).getResults_by_gender().get(1).getCategories().get(1).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(1).getSet());
            }
            holder.percentage2m.setText("" + percentageTemp2m + "%");
        }
        if (tempm.size() == 3) {
            holder.label1m.setText(mData.get(position).getResults_by_gender().get(1).getCategories().get(0).getLabel());
            holder.option1m.setMax(mData.get(position).getResults_by_gender().get(1).getSet());
            holder.option1m.setProgress(mData.get(position).getResults_by_gender().get(1).getCategories().get(0).getCount());
            int percentageTempm = 0;
            if( (mData.get(position).getResults_by_gender().get(1).getSet()) > 0){
                percentageTempm = ((mData.get(position).getResults_by_gender().get(1).getCategories().get(0).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(1).getSet());
            }
            holder.percentage1m.setText("" + percentageTempm + "%");
            holder.label2m.setText(mData.get(position).getResults_by_age().get(1).getCategories().get(1).getLabel());
            holder.option2m.setMax(mData.get(position).getResults_by_gender().get(1).getSet());
            holder.option2m.setProgress(mData.get(position).getResults_by_gender().get(1).getCategories().get(1).getCount());
            int percentageTemp2m = 0;
            if( (mData.get(position).getResults_by_gender().get(1).getSet()) > 0){
                percentageTemp2m = ((mData.get(position).getResults_by_gender().get(1).getCategories().get(1).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(1).getSet());
            }
            holder.percentage2m.setText("" + percentageTemp2m + "%");
            holder.l3m.setVisibility(View.VISIBLE);
            holder.label3m.setText(tempm.get(2).getLabel());
            holder.option3m.setMax(mData.get(position).getResults().getSet());
            holder.option3m.setProgress(tempm.get(2).getCount());
            int percentageTemp3m = 0;
            if( (mData.get(position).getResults_by_gender().get(1).getSet()) > 0){
                percentageTemp3m = ((mData.get(position).getResults_by_gender().get(1).getCategories().get(2).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(1).getSet());
            }
            holder.percentage3m.setText("" + percentageTemp3m + "%");

        } else {
            holder.l3m.setVisibility(View.GONE);
        }
        if (tempm.size() == 4) {
            holder.label1m.setText(mData.get(position).getResults_by_gender().get(1).getCategories().get(0).getLabel());
            holder.option1m.setMax(mData.get(position).getResults_by_gender().get(1).getSet());
            holder.option1m.setProgress(mData.get(position).getResults_by_gender().get(1).getCategories().get(0).getCount());
            int percentageTempm = 0;
            if( (mData.get(position).getResults_by_gender().get(1).getSet()) > 0){
                percentageTempm = ((mData.get(position).getResults_by_gender().get(1).getCategories().get(0).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(1).getSet());
            }
            holder.percentage1m.setText("" + percentageTempm + "%");
            holder.label2m.setText(mData.get(position).getResults_by_age().get(1).getCategories().get(1).getLabel());
            holder.option2m.setMax(mData.get(position).getResults_by_gender().get(1).getSet());
            holder.option2m.setProgress(mData.get(position).getResults_by_gender().get(1).getCategories().get(1).getCount());
            int percentageTemp2m = 0;
            if( (mData.get(position).getResults_by_gender().get(1).getSet()) > 0){
                percentageTemp2m = ((mData.get(position).getResults_by_gender().get(1).getCategories().get(1).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(1).getSet());
            }
            holder.percentage2m.setText("" + percentageTemp2m + "%");
            holder.l3m.setVisibility(View.VISIBLE);
            holder.label3m.setText(tempm.get(2).getLabel());
            holder.option3m.setMax(mData.get(position).getResults_by_gender().get(1).getSet());
            holder.option3m.setProgress(tempm.get(2).getCount());
            holder.l4m.setVisibility(View.VISIBLE);
            holder.label4m.setText(tempm.get(3).getLabel());
            holder.option4m.setMax(mData.get(position).getResults_by_gender().get(1).getSet());
            holder.option4m.setProgress(tempm.get(3).getCount());
            int percentageTemp3m = 0;
            if( (mData.get(position).getResults_by_gender().get(1).getSet()) > 0){
                percentageTemp3m = ((mData.get(position).getResults_by_gender().get(1).getCategories().get(2).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(1).getSet());
            }
            holder.percentage3m.setText("" + percentageTemp3m + "%");
            int percentageTemp4m = 0;
            if( (mData.get(position).getResults_by_gender().get(1).getSet()) > 0){
                percentageTemp4m = ((mData.get(position).getResults_by_gender().get(1).getCategories().get(3).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(1).getSet());
            }
            holder.percentage4m.setText("" + percentageTemp4m + "%");

        } else {
            holder.l4m.setVisibility(View.GONE);
        }
        if (tempm.size() == 5) {
            holder.label1m.setText(mData.get(position).getResults_by_gender().get(1).getCategories().get(0).getLabel());
            holder.option1m.setMax(mData.get(position).getResults_by_gender().get(1).getSet());
            holder.option1m.setProgress(mData.get(position).getResults_by_gender().get(1).getCategories().get(0).getCount());
            int percentageTempm = 0;
            if( (mData.get(position).getResults_by_gender().get(1).getSet()) > 0){
                percentageTempm = ((mData.get(position).getResults_by_gender().get(1).getCategories().get(0).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(1).getSet());
            }
            holder.percentage1m.setText("" + percentageTempm + "%");
            holder.label2m.setText(mData.get(position).getResults_by_age().get(1).getCategories().get(1).getLabel());
            holder.option2m.setMax(mData.get(position).getResults_by_gender().get(1).getSet());
            holder.option2m.setProgress(mData.get(position).getResults_by_gender().get(1).getCategories().get(1).getCount());
            int percentageTemp2m = 0;
            if( (mData.get(position).getResults_by_gender().get(1).getSet()) > 0){
                percentageTemp2m = ((mData.get(position).getResults_by_gender().get(1).getCategories().get(1).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(1).getSet());
            }
            holder.percentage2m.setText("" + percentageTemp2m + "%");
            holder.l3m.setVisibility(View.VISIBLE);
            holder.label3m.setText(tempm.get(2).getLabel());
            holder.option3m.setMax(mData.get(position).getResults_by_gender().get(1).getSet());
            holder.option3m.setProgress(tempm.get(2).getCount());
            holder.l4m.setVisibility(View.VISIBLE);
            holder.label4m.setText(tempm.get(3).getLabel());
            holder.option4m.setMax(mData.get(position).getResults_by_gender().get(1).getSet());
            holder.option4m.setProgress(tempm.get(3).getCount());
            holder.l5m.setVisibility(View.VISIBLE);
            holder.label5m.setText(tempm.get(4).getLabel());
            holder.option5m.setMax(mData.get(position).getResults_by_gender().get(1).getSet());
            holder.option5m.setProgress(tempm.get(4).getCount());
            int percentageTemp3m = 0;
            if( (mData.get(position).getResults_by_gender().get(1).getSet()) > 0){
                percentageTemp3m = ((mData.get(position).getResults_by_gender().get(1).getCategories().get(2).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(1).getSet());
            }
            holder.percentage3m.setText("" + percentageTemp3m + "%");
            int percentageTemp4m = 0;
            if( (mData.get(position).getResults_by_gender().get(1).getSet()) > 0){
                percentageTemp4m = ((mData.get(position).getResults_by_gender().get(1).getCategories().get(3).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(1).getSet());
            }
            holder.percentage4m.setText("" + percentageTemp4m + "%");
            int percentageTemp5m = 0;
            if( (mData.get(position).getResults_by_gender().get(1).getSet()) > 0){
                percentageTemp5m = ((mData.get(position).getResults_by_gender().get(1).getCategories().get(4).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(1).getSet());
            }
            holder.percentage5m.setText("" + percentageTemp5m + "%");

        } else {
            holder.l5m.setVisibility(View.GONE);
        }
        if(tempm.size() > 5){
            holder.label1m.setText(mData.get(position).getResults_by_gender().get(1).getCategories().get(0).getLabel());
            holder.option1m.setMax(mData.get(position).getResults_by_gender().get(1).getSet());
            holder.option1m.setProgress(mData.get(position).getResults_by_gender().get(1).getCategories().get(0).getCount());
            int percentageTempm = 0;
            if( (mData.get(position).getResults_by_gender().get(1).getSet()) > 0){
                percentageTempm = ((mData.get(position).getResults_by_gender().get(1).getCategories().get(0).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(1).getSet());
            }
            holder.percentage1m.setText("" + percentageTempm + "%");
            holder.label2m.setText(mData.get(position).getResults_by_age().get(1).getCategories().get(1).getLabel());
            holder.option2m.setMax(mData.get(position).getResults_by_gender().get(1).getSet());
            holder.option2m.setProgress(mData.get(position).getResults_by_gender().get(1).getCategories().get(1).getCount());
            int percentageTemp2m = 0;
            if( (mData.get(position).getResults_by_gender().get(1).getSet()) > 0){
                percentageTemp2m = ((mData.get(position).getResults_by_gender().get(1).getCategories().get(1).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(1).getSet());
            }
            holder.percentage2m.setText("" + percentageTemp2m + "%");

        }
    }

    private void setupFemale(ViewHolder holder, int position, List<categories> tempf) {
        if (tempf.size() == 1) {
            holder.label1f.setText(mData.get(position).getResults_by_gender().get(0).getCategories().get(0).getLabel());
            holder.option1f.setMax(mData.get(position).getResults_by_gender().get(0).getSet());
            holder.option1f.setProgress(mData.get(position).getResults_by_gender().get(0).getCategories().get(0).getCount());
            int percentageTempf = 0;
            if( (mData.get(position).getResults_by_gender().get(0).getSet()) > 0){
                percentageTempf = ((mData.get(position).getResults_by_gender().get(0).getCategories().get(0).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(0).getSet());
            }
            holder.percentage1f.setText("" + percentageTempf + "%");
        }
        if (tempf.size() == 2) {
            holder.label1f.setText(mData.get(position).getResults_by_gender().get(0).getCategories().get(0).getLabel());
            holder.option1f.setMax(mData.get(position).getResults_by_gender().get(0).getSet());
            holder.option1f.setProgress(mData.get(position).getResults_by_gender().get(0).getCategories().get(0).getCount());
            int percentageTempf = 0;
            if( (mData.get(position).getResults_by_gender().get(0).getSet()) > 0){
                percentageTempf = ((mData.get(position).getResults_by_gender().get(0).getCategories().get(0).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(0).getSet());
            }
            holder.percentage1f.setText("" + percentageTempf + "%");
            holder.label2f.setText(mData.get(position).getResults_by_gender().get(0).getCategories().get(1).getLabel());
            holder.option2f.setMax(mData.get(position).getResults_by_gender().get(0).getSet());
            holder.option2f.setProgress(mData.get(position).getResults_by_gender().get(0).getCategories().get(1).getCount());
            int percentageTemp2f = 0;
            if( (mData.get(position).getResults_by_gender().get(0).getSet()) > 0){
                percentageTemp2f = ((mData.get(position).getResults_by_gender().get(0).getCategories().get(1).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(0).getSet());
            }
            holder.percentage2f.setText("" + percentageTemp2f + "%");
        }
        if (tempf.size() == 3) {
            holder.label1f.setText(mData.get(position).getResults_by_gender().get(0).getCategories().get(0).getLabel());
            holder.option1f.setMax(mData.get(position).getResults_by_gender().get(0).getSet());
            holder.option1f.setProgress(mData.get(position).getResults_by_gender().get(0).getCategories().get(0).getCount());
            int percentageTempf = 0;
            if( (mData.get(position).getResults_by_gender().get(0).getSet()) > 0){
                percentageTempf = ((mData.get(position).getResults_by_gender().get(0).getCategories().get(0).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(0).getSet());
            }
            holder.percentage1f.setText("" + percentageTempf + "%");
            holder.label2f.setText(mData.get(position).getResults_by_gender().get(0).getCategories().get(1).getLabel());
            holder.option2f.setMax(mData.get(position).getResults_by_gender().get(0).getSet());
            holder.option2f.setProgress(mData.get(position).getResults_by_gender().get(0).getCategories().get(1).getCount());
            int percentageTemp2f = 0;
            if( (mData.get(position).getResults_by_gender().get(0).getSet()) > 0){
                percentageTemp2f = ((mData.get(position).getResults_by_gender().get(0).getCategories().get(1).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(0).getSet());
            }
            holder.percentage2f.setText("" + percentageTemp2f + "%");
            holder.l3f.setVisibility(View.VISIBLE);
            holder.label3f.setText(tempf.get(2).getLabel());
            holder.option3f.setMax(mData.get(position).getResults().getSet());
            holder.option3f.setProgress(tempf.get(2).getCount());
            int percentageTemp3f = 0;
            if( (mData.get(position).getResults_by_gender().get(0).getSet()) > 0){
                percentageTemp3f = ((mData.get(position).getResults_by_gender().get(0).getCategories().get(2).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(0).getSet());
            }
            holder.percentage3f.setText("" + percentageTemp3f + "%");

        } else {
            holder.l3f.setVisibility(View.GONE);
        }
        if (tempf.size() == 4) {
            holder.label1f.setText(mData.get(position).getResults_by_gender().get(0).getCategories().get(0).getLabel());
            holder.option1f.setMax(mData.get(position).getResults_by_gender().get(0).getSet());
            holder.option1f.setProgress(mData.get(position).getResults_by_gender().get(0).getCategories().get(0).getCount());
            int percentageTempf = 0;
            if( (mData.get(position).getResults_by_gender().get(0).getSet()) > 0){
                percentageTempf = ((mData.get(position).getResults_by_gender().get(0).getCategories().get(0).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(0).getSet());
            }
            holder.percentage1f.setText("" + percentageTempf + "%");
            holder.label2f.setText(mData.get(position).getResults_by_gender().get(0).getCategories().get(1).getLabel());
            holder.option2f.setMax(mData.get(position).getResults_by_gender().get(0).getSet());
            holder.option2f.setProgress(mData.get(position).getResults_by_gender().get(0).getCategories().get(1).getCount());
            int percentageTemp2f = 0;
            if( (mData.get(position).getResults_by_gender().get(0).getSet()) > 0){
                percentageTemp2f = ((mData.get(position).getResults_by_gender().get(0).getCategories().get(1).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(0).getSet());
            }
            holder.percentage2f.setText("" + percentageTemp2f + "%");
            holder.l3f.setVisibility(View.VISIBLE);
            holder.label3f.setText(tempf.get(2).getLabel());
            holder.option3f.setMax(mData.get(position).getResults_by_gender().get(0).getSet());
            holder.option3f.setProgress(tempf.get(2).getCount());
            holder.l4f.setVisibility(View.VISIBLE);
            holder.label4f.setText(tempf.get(3).getLabel());
            holder.option4f.setMax(mData.get(position).getResults_by_gender().get(0).getSet());
            holder.option4f.setProgress(tempf.get(3).getCount());
            int percentageTemp3f = 0;
            if( (mData.get(position).getResults_by_gender().get(0).getSet()) > 0){
                percentageTemp3f = ((mData.get(position).getResults_by_gender().get(0).getCategories().get(2).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(0).getSet());
            }
            holder.percentage3f.setText("" + percentageTemp3f + "%");
            int percentageTemp4f = 0;
            if( (mData.get(position).getResults_by_gender().get(0).getSet()) > 0){
                percentageTemp4f = ((mData.get(position).getResults_by_gender().get(0).getCategories().get(3).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(0).getSet());
            }
            holder.percentage4f.setText("" + percentageTemp4f + "%");

        } else {
            holder.l4f.setVisibility(View.GONE);
        }
        if (tempf.size() == 5) {
            holder.label1f.setText(mData.get(position).getResults_by_gender().get(0).getCategories().get(0).getLabel());
            holder.option1f.setMax(mData.get(position).getResults_by_gender().get(0).getSet());
            holder.option1f.setProgress(mData.get(position).getResults_by_gender().get(0).getCategories().get(0).getCount());
            int percentageTempf = 0;
            if( (mData.get(position).getResults_by_gender().get(0).getSet()) > 0){
                percentageTempf = ((mData.get(position).getResults_by_gender().get(0).getCategories().get(0).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(0).getSet());
            }
            holder.percentage1f.setText("" + percentageTempf + "%");
            holder.label2f.setText(mData.get(position).getResults_by_gender().get(0).getCategories().get(1).getLabel());
            holder.option2f.setMax(mData.get(position).getResults_by_gender().get(0).getSet());
            holder.option2f.setProgress(mData.get(position).getResults_by_gender().get(0).getCategories().get(1).getCount());
            int percentageTemp2f = 0;
            if( (mData.get(position).getResults_by_gender().get(0).getSet()) > 0){
                percentageTemp2f = ((mData.get(position).getResults_by_gender().get(0).getCategories().get(1).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(0).getSet());
            }
            holder.percentage2f.setText("" + percentageTemp2f + "%");
            holder.l3f.setVisibility(View.VISIBLE);
            holder.label3f.setText(tempf.get(2).getLabel());
            holder.option3f.setMax(mData.get(position).getResults_by_gender().get(0).getSet());
            holder.option3f.setProgress(tempf.get(2).getCount());
            holder.l4f.setVisibility(View.VISIBLE);
            holder.label4f.setText(tempf.get(3).getLabel());
            holder.option4f.setMax(mData.get(position).getResults_by_gender().get(0).getSet());
            holder.option4f.setProgress(tempf.get(3).getCount());
            holder.l5f.setVisibility(View.VISIBLE);
            holder.label5f.setText(tempf.get(4).getLabel());
            holder.option5f.setMax(mData.get(position).getResults_by_gender().get(0).getSet());
            holder.option5f.setProgress(tempf.get(4).getCount());
            int percentageTemp3f = 0;
            if( (mData.get(position).getResults_by_gender().get(0).getSet()) > 0){
                percentageTemp3f = ((mData.get(position).getResults_by_gender().get(0).getCategories().get(2).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(0).getSet());
            }
            holder.percentage3f.setText("" + percentageTemp3f + "%");
            int percentageTemp4f = 0;
            if( (mData.get(position).getResults_by_gender().get(0).getSet()) > 0){
                percentageTemp4f = ((mData.get(position).getResults_by_gender().get(0).getCategories().get(3).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(0).getSet());
            }
            holder.percentage4f.setText("" + percentageTemp4f + "%");
            int percentageTemp5f = 0;
            if( (mData.get(position).getResults_by_gender().get(0).getSet()) > 0){
                percentageTemp5f = ((mData.get(position).getResults_by_gender().get(0).getCategories().get(4).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(0).getSet());
            }
            holder.percentage5f.setText("" + percentageTemp5f + "%");

        } else {
            holder.l5f.setVisibility(View.GONE);
        }
        if (tempf.size() > 5) {
            holder.label1f.setText(mData.get(position).getResults_by_gender().get(0).getCategories().get(0).getLabel());
            holder.option1f.setMax(mData.get(position).getResults_by_gender().get(0).getSet());
            holder.option1f.setProgress(mData.get(position).getResults_by_gender().get(0).getCategories().get(0).getCount());
            int percentageTempf = 0;
            if( (mData.get(position).getResults_by_gender().get(0).getSet()) > 0){
                percentageTempf = ((mData.get(position).getResults_by_gender().get(0).getCategories().get(0).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(0).getSet());
            }
            holder.percentage1f.setText("" + percentageTempf + "%");
            holder.label2f.setText(mData.get(position).getResults_by_gender().get(0).getCategories().get(1).getLabel());
            holder.option2f.setMax(mData.get(position).getResults_by_gender().get(0).getSet());
            holder.option2f.setProgress(mData.get(position).getResults_by_gender().get(0).getCategories().get(1).getCount());
            int percentageTemp2f = 0;
            if( (mData.get(position).getResults_by_gender().get(0).getSet()) > 0){
                percentageTemp2f = ((mData.get(position).getResults_by_gender().get(0).getCategories().get(1).getCount()) * 100) / (mData.get(position).getResults_by_gender().get(0).getSet());
            }
            holder.percentage2f.setText("" + percentageTemp2f + "%");
        }
    }

    private void setupStatistics(ViewHolder holder, int position) {
        List<categories> temp = mData.get(position).getResults().getCategories();
        if (temp.size() == 1) {
            holder.label1.setText(mData.get(position).getResults().getCategories().get(0).getLabel());
            holder.option1.setMax(mData.get(position).getResults().getSet());
            holder.option1.setProgress(mData.get(position).getResults().getCategories().get(0).getCount());
            int percentageTemp = 0;
            if( (mData.get(position).getResults().getSet()) > 0){
                percentageTemp = ((mData.get(position).getResults().getCategories().get(0).getCount()) * 100) / (mData.get(position).getResults().getSet());
            }
            holder.percentage1.setText("" + percentageTemp + "%");
        }
        if (temp.size() == 2) {
            holder.label1.setText(mData.get(position).getResults().getCategories().get(0).getLabel());
            holder.option1.setMax(mData.get(position).getResults().getSet());
            holder.option1.setProgress(mData.get(position).getResults().getCategories().get(0).getCount());
            int percentageTemp = 0;
            if( (mData.get(position).getResults().getSet()) > 0){
                percentageTemp = ((mData.get(position).getResults().getCategories().get(0).getCount()) * 100) / (mData.get(position).getResults().getSet());
            }
            holder.percentage1.setText("" + percentageTemp + "%");
            holder.label2.setText(mData.get(position).getResults().getCategories().get(1).getLabel());
            holder.option2.setMax(mData.get(position).getResults().getSet());
            holder.option2.setProgress(mData.get(position).getResults().getCategories().get(1).getCount());
            int percentageTemp2 = 0;
            if( (mData.get(position).getResults().getSet()) > 0){
                percentageTemp2 = ((mData.get(position).getResults().getCategories().get(1).getCount()) * 100) / (mData.get(position).getResults().getSet());
            }
            holder.percentage2.setText("" + percentageTemp2 + "%");


        } else {
        }
        if (temp.size() == 3) {
            holder.label1.setText(mData.get(position).getResults().getCategories().get(0).getLabel());
            holder.option1.setMax(mData.get(position).getResults().getSet());
            holder.option1.setProgress(mData.get(position).getResults().getCategories().get(0).getCount());
            int percentageTemp = 0;
            if( (mData.get(position).getResults().getSet()) > 0){
                percentageTemp = ((mData.get(position).getResults().getCategories().get(0).getCount()) * 100) / (mData.get(position).getResults().getSet());
            }
            holder.percentage1.setText("" + percentageTemp + "%");
            holder.label2.setText(mData.get(position).getResults().getCategories().get(1).getLabel());
            holder.option2.setMax(mData.get(position).getResults().getSet());
            holder.option2.setProgress(mData.get(position).getResults().getCategories().get(1).getCount());
            int percentageTemp2 = 0;
            if( (mData.get(position).getResults().getSet()) > 0){
                percentageTemp2 = ((mData.get(position).getResults().getCategories().get(1).getCount()) * 100) / (mData.get(position).getResults().getSet());
            }
            holder.percentage2.setText("" + percentageTemp2 + "%");
            holder.l3.setVisibility(View.VISIBLE);
            Log.d("LENGTH CHECK", "" + temp.size() + temp.get(2).getLabel());
            holder.label3.setText(temp.get(2).getLabel());
            holder.option3.setMax(mData.get(position).getResults().getSet());
            holder.option3.setProgress(temp.get(2).getCount());
            int percentageTemp3 = 0;
            if( (mData.get(position).getResults().getSet()) > 0){
                percentageTemp3 = ((mData.get(position).getResults().getCategories().get(2).getCount()) * 100) / (mData.get(position).getResults().getSet());
            }
            holder.percentage3.setText("" + percentageTemp3 + "%");

        } else {
            holder.l3.setVisibility(View.GONE);
        }
        if (temp.size() == 4) {
            holder.label1.setText(mData.get(position).getResults().getCategories().get(0).getLabel());
            holder.option1.setMax(mData.get(position).getResults().getSet());
            holder.option1.setProgress(mData.get(position).getResults().getCategories().get(0).getCount());
            int percentageTemp = 0;
            if( (mData.get(position).getResults().getSet()) > 0){
                percentageTemp = ((mData.get(position).getResults().getCategories().get(0).getCount()) * 100) / (mData.get(position).getResults().getSet());
            }
            holder.percentage1.setText("" + percentageTemp + "%");
            holder.label2.setText(mData.get(position).getResults().getCategories().get(1).getLabel());
            holder.option2.setMax(mData.get(position).getResults().getSet());
            holder.option2.setProgress(mData.get(position).getResults().getCategories().get(1).getCount());
            int percentageTemp2 = 0;
            if( (mData.get(position).getResults().getSet()) > 0){
                percentageTemp2 = ((mData.get(position).getResults().getCategories().get(1).getCount()) * 100) / (mData.get(position).getResults().getSet());
            }
            holder.percentage2.setText("" + percentageTemp2 + "%");
            holder.l3.setVisibility(View.VISIBLE);
            holder.label3.setText(temp.get(2).getLabel());
            holder.option3.setMax(mData.get(position).getResults().getSet());
            holder.option3.setProgress(temp.get(2).getCount());
            holder.l4.setVisibility(View.VISIBLE);
            holder.label4.setText(temp.get(3).getLabel());
            holder.option4.setMax(mData.get(position).getResults().getSet());
            holder.option4.setProgress(temp.get(3).getCount());
            int percentageTemp3 = 0;
            if( (mData.get(position).getResults().getSet()) > 0){
                percentageTemp3 = ((mData.get(position).getResults().getCategories().get(2).getCount()) * 100) / (mData.get(position).getResults().getSet());
            }
            holder.percentage3.setText("" + percentageTemp3 + "%");
            int percentageTemp4 = 0;
            if( (mData.get(position).getResults().getSet()) > 0){
                percentageTemp4 = ((mData.get(position).getResults().getCategories().get(3).getCount()) * 100) / (mData.get(position).getResults().getSet());
            }
            holder.percentage4.setText("" + percentageTemp4 + "%");

        } else {
            holder.l4.setVisibility(View.GONE);
        }
        if (temp.size() == 5) {
            holder.label1.setText(mData.get(position).getResults().getCategories().get(0).getLabel());
            holder.option1.setMax(mData.get(position).getResults().getSet());
            holder.option1.setProgress(mData.get(position).getResults().getCategories().get(0).getCount());
            int percentageTemp = 0;
            if( (mData.get(position).getResults().getSet()) > 0){
                percentageTemp = ((mData.get(position).getResults().getCategories().get(0).getCount()) * 100) / (mData.get(position).getResults().getSet());
            }
            holder.percentage1.setText("" + percentageTemp + "%");
            holder.label2.setText(mData.get(position).getResults().getCategories().get(1).getLabel());
            holder.option2.setMax(mData.get(position).getResults().getSet());
            holder.option2.setProgress(mData.get(position).getResults().getCategories().get(1).getCount());
            int percentageTemp2 = 0;
            if( (mData.get(position).getResults().getSet()) > 0){
                percentageTemp2 = ((mData.get(position).getResults().getCategories().get(1).getCount()) * 100) / (mData.get(position).getResults().getSet());
            }
            holder.percentage2.setText("" + percentageTemp2 + "%");
            holder.l3.setVisibility(View.VISIBLE);
            holder.label3.setText(temp.get(2).getLabel());
            holder.option3.setMax(mData.get(position).getResults().getSet());
            holder.option3.setProgress(temp.get(2).getCount());
            holder.l4.setVisibility(View.VISIBLE);
            holder.label4.setText(temp.get(3).getLabel());
            holder.option4.setMax(mData.get(position).getResults().getSet());
            holder.option4.setProgress(temp.get(3).getCount());
            holder.l5.setVisibility(View.VISIBLE);
            holder.label5.setText(temp.get(4).getLabel());
            holder.option5.setMax(mData.get(position).getResults().getSet());
            holder.option5.setProgress(temp.get(4).getCount());
            int percentageTemp3 = 0;
            if( (mData.get(position).getResults().getSet()) > 0){
                percentageTemp3 = ((mData.get(position).getResults().getCategories().get(2).getCount()) * 100) / (mData.get(position).getResults().getSet());
            }
            holder.percentage3.setText("" + percentageTemp3 + "%");
            int percentageTemp4 = 0;
            if( (mData.get(position).getResults().getSet()) > 0){
                percentageTemp4 = ((mData.get(position).getResults().getCategories().get(3).getCount()) * 100) / (mData.get(position).getResults().getSet());
            }
            holder.percentage4.setText("" + percentageTemp4 + "%");
            int percentageTemp5 = 0;
            if( (mData.get(position).getResults().getSet()) > 0){
                percentageTemp5 = ((mData.get(position).getResults().getCategories().get(4).getCount()) * 100) / (mData.get(position).getResults().getSet());
            }
            holder.percentage5.setText("" + percentageTemp5 + "%");

        } else {
            holder.l5.setVisibility(View.GONE);
        }
        if(temp.size() > 5){
            holder.label1.setText(mData.get(position).getResults().getCategories().get(0).getLabel());
            holder.option1.setMax(mData.get(position).getResults().getSet());
            holder.option1.setProgress(mData.get(position).getResults().getCategories().get(0).getCount());
            int percentageTemp = 0;
            if( (mData.get(position).getResults().getSet()) > 0){
                percentageTemp = ((mData.get(position).getResults().getCategories().get(0).getCount()) * 100) / (mData.get(position).getResults().getSet());
            }
            holder.percentage1.setText("" + percentageTemp + "%");
            holder.label2.setText(mData.get(position).getResults().getCategories().get(1).getLabel());
            holder.option2.setMax(mData.get(position).getResults().getSet());
            holder.option2.setProgress(mData.get(position).getResults().getCategories().get(1).getCount());
            int percentageTemp2 = 0;
            if( (mData.get(position).getResults().getSet()) > 0){
                percentageTemp2 = ((mData.get(position).getResults().getCategories().get(1).getCount()) * 100) / (mData.get(position).getResults().getSet());
            }
            holder.percentage2.setText("" + percentageTemp2 + "%");

        }
    }
}

package com.atakmap.android.plugintemplate.araaftorPlugin.services;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.atakmap.android.plugintemplate.araaftorPlugin.DetectObject;
import com.atakmap.android.plugintemplate.araaftorPlugin.interfaces.UnitResultListener;
import com.atakmap.android.plugintemplate.plugin.R;

public class UnitService {
    private LinearLayout l1, l2, l3, l4, l5, l6;
    private int currentLayout = 1;
    private ImageButton arrow_left, rank;
    private String nameOfUnit = "s";
    String[] l1_table = {"Friendly", "Neutral", "Hostile", "Unknown"};
    String[] rank_table = {"rank_a", "rank_b","rank_c","rank_d","rank_e","rank_f","rank_g","rank_h","rank_i","rank_j","rank_k","rank_l","rank_m"};
    String[] l1_table_photo = {"side_friendly", "side_neutral", "side_hostile", "side_unknown"};
    String[] l1_table_name = {"f", "n", "h", "u"};
    String[] l2_table = {"Air track", "Ground track", "Sea surface track", "Space track", "Subsurface track", "SOF Unit"};
    String[] l2_table_photo = {"ap", "gp", "sp", "pp", "up", "fp"};
    String[] l3_table = {"Equipment", "Installation", "Unit"};
    String[] l3_table_photo = {"___________", "i_____h____", "___________"};
    String[] l4_table = {"Combat", "Combat service support", "Combat support"};
    String[] l4_table_photo = {"uc", "us", "uu"};
    String[] l5_table = {"Air defense", "Anti armor", "Armor", "Aviation", "Engineer", "Field artillery", "Infantry", "Missile (SSM)", "Reconnaissance"};
    String[] l5_table_photo = {"d", "aa", "a","v","e","f","i","m","r"};
    Context context;
    View detailsView;
    private UnitResultListener listener;

    String rankNameU = "";
    public UnitService(Context context, UnitResultListener listener) {
        this.context = context;
        this.listener = listener;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        detailsView = inflater.inflate(R.layout.change_unit, null);
        l1 = detailsView.findViewById(R.id.l1);
        l2= detailsView.findViewById(R.id.l2);
        l3= detailsView.findViewById(R.id.l3);
        l4= detailsView.findViewById(R.id.l4);
        l5= detailsView.findViewById(R.id.l5);
        l6= detailsView.findViewById(R.id.l6);
        arrow_left = detailsView.findViewById(R.id.arrow_left);
        rank = detailsView.findViewById(R.id.rank);
    }
    public void showDialog(DetectObject item, LayoutInflater inflater, String rankName) {
        FrameLayout detailsContainer = ((Activity) context).findViewById(R.id.container3);
        rankNameU = rankName;
        if (detailsContainer.getChildCount() == 0) {
            generateLayout1(item,detailsContainer);
            currentLayout=1;
            l1.setVisibility(View.VISIBLE);
        }
        int leftImageRes = context.getResources().getIdentifier(rankName, "drawable", context.getPackageName());
        rank.setImageResource(leftImageRes);
        rank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLayout!=6) {
                    generateLayout6(item,detailsContainer);
                    l2.setVisibility(View.GONE);
                    l2.removeAllViews();
                    l1.setVisibility(View.GONE);
                    l1.removeAllViews();
                    l3.setVisibility(View.GONE);
                    l3.removeAllViews();
                    l4.setVisibility(View.GONE);
                    l4.removeAllViews();
                    l5.setVisibility(View.GONE);
                    l5.removeAllViews();
                    currentLayout=6;
                    l6.setVisibility(View.VISIBLE);
                }
            }
        });
        arrow_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLayout==1) {
                    if (listener != null) {
                        listener.onUnitResult(false,item.id,nameOfUnit, rankNameU);
                        detailsContainer.removeAllViews();
                        detailsContainer.setVisibility(View.GONE);
                    }
                } else if (currentLayout==2) {
                    currentLayout=1;
                    nameOfUnit = nameOfUnit.substring(0, nameOfUnit.length() - 1);
                    generateLayout1(item,detailsContainer);
                    l2.setVisibility(View.GONE);
                    l2.removeAllViews();
                    l1.setVisibility(View.VISIBLE);
                } else if (currentLayout==3) {
                    currentLayout=2;
                    nameOfUnit = nameOfUnit.substring(0, nameOfUnit.length() - 2);
                    generateLayout2(item,detailsContainer);
                    l3.setVisibility(View.GONE);
                    l3.removeAllViews();
                    l2.setVisibility(View.VISIBLE);
                } else if (currentLayout==4) {
                    currentLayout=3;
                    generateLayout3(item,detailsContainer);
                    l4.setVisibility(View.GONE);
                    l4.removeAllViews();
                    l3.setVisibility(View.VISIBLE);
                } else if (currentLayout==5) {
                    currentLayout=4;
                    nameOfUnit = nameOfUnit.substring(0, nameOfUnit.length() - 2);
                    generateLayout4(item,detailsContainer);
                    l5.removeAllViews();
                    l5.setVisibility(View.GONE);
                    l4.setVisibility(View.VISIBLE);
                }
            }
        });
        detailsContainer.addView(detailsView);
        detailsContainer.setVisibility(View.VISIBLE);
    }
    public void generateLayout6(DetectObject item, FrameLayout detailsContainer){
        for (int i = 0; i < rank_table.length; i++) {
            LinearLayout itemLayout = new LinearLayout(context);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f));

            ImageButton rightImageButton = new ImageButton(context);
            rightImageButton.setId(View.generateViewId());
            rightImageButton.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            int leftImageRes = context.getResources().getIdentifier(rank_table[i], "drawable", context.getPackageName());
            rightImageButton.setImageResource(leftImageRes);
            rightImageButton.setPadding(5, 5, 5, 5);
            rightImageButton.setScaleType(ImageView.ScaleType.CENTER);
            int finalI = i;
            rightImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentLayout=1;
                    generateLayout1(item,detailsContainer);
                    l6.removeAllViews();
                    l6.setVisibility(View.GONE);
                    l1.setVisibility(View.VISIBLE);
                    int leftImageRese = context.getResources().getIdentifier(rank_table[finalI], "drawable", context.getPackageName());
                    rank.setImageResource(leftImageRese);
                    rankNameU = rank_table[finalI];
                }
            });
            itemLayout.addView(rightImageButton);

            l6.addView(itemLayout);
        }
    }
    public void generateLayout1(DetectObject item, FrameLayout detailsContainer){
        for (int i = 0; i < l1_table.length; i++) {
            LinearLayout itemLayout = new LinearLayout(context);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f));

            ImageButton leftImageButton = new ImageButton(context);
            leftImageButton.setId(View.generateViewId());
            leftImageButton.setLayoutParams(new LinearLayout.LayoutParams(
                    250, LinearLayout.LayoutParams.MATCH_PARENT));
            int leftImageRes = context.getResources().getIdentifier(l1_table_photo[i], "drawable", context.getPackageName());
            leftImageButton.setImageResource(leftImageRes);
            leftImageButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            Drawable drawable = leftImageButton.getDrawable();
            leftImageButton.setPadding(5, 5, 5, 5);

            Button middleButton = new Button(context);
            middleButton.setId(View.generateViewId());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    600, LinearLayout.LayoutParams.MATCH_PARENT);
            params.setMargins(-30, 0, 0, 0); // Ustawiamy margines lewy na -10dp, pozostałe marginesy na 0

            middleButton.setLayoutParams(params);
            middleButton.setText(l1_table[i]); // Set the button text based on the table array
            middleButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            middleButton.setTextSize(10);
            middleButton.setPadding(20, 20, 20, 20);
            middleButton.setCompoundDrawablePadding(10);
            int finalI = i;
            middleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onUnitResult(true, item.id, nameOfUnit+l1_table_name[finalI]+"_____________",rankNameU);
                        detailsContainer.removeAllViews();
                        detailsContainer.setVisibility(View.GONE);
                    }
                }
            });
            ImageButton rightImageButton = new ImageButton(context);
            rightImageButton.setId(View.generateViewId());
            rightImageButton.setLayoutParams(new LinearLayout.LayoutParams(
                    250, LinearLayout.LayoutParams.MATCH_PARENT));
            rightImageButton.setImageResource(R.drawable.arrow_right); // Change image source dynamically
            rightImageButton.setPadding(5, 5, 5, 5);
            rightImageButton.setScaleType(ImageView.ScaleType.CENTER);

            rightImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentLayout=2;
                    nameOfUnit = nameOfUnit.concat(l1_table_name[finalI]);
                    generateLayout2(item,detailsContainer);
                    l1.removeAllViews();
                    l1.setVisibility(View.GONE);
                    l2.setVisibility(View.VISIBLE);
                }
            });
            itemLayout.addView(leftImageButton);
            itemLayout.addView(middleButton);
            itemLayout.addView(rightImageButton);

            l1.addView(itemLayout);
        }
    }
    public void generateLayout2(DetectObject item, FrameLayout detailsContainer){
        for (int i = 0; i < l2_table.length; i++) {
            LinearLayout itemLayout = new LinearLayout(context);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f));

            ImageButton leftImageButton = new ImageButton(context);
            leftImageButton.setId(View.generateViewId());
            leftImageButton.setLayoutParams(new LinearLayout.LayoutParams(
                    250, LinearLayout.LayoutParams.MATCH_PARENT));
            int leftImageRes = context.getResources().getIdentifier((nameOfUnit+l2_table_photo[i]+"___________"), "drawable", context.getPackageName());
            leftImageButton.setImageResource(leftImageRes);
            leftImageButton.setPadding(5, 5, 5, 5);

            Button middleButton = new Button(context);
            middleButton.setId(View.generateViewId());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    600, LinearLayout.LayoutParams.MATCH_PARENT);
            params.setMargins(-30, 0, 0, 0); // Ustawiamy margines lewy na -10dp, pozostałe marginesy na 0

            middleButton.setLayoutParams(params);
            middleButton.setText(l2_table[i]); // Set the button text based on the table array
            middleButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            middleButton.setTextSize(10);
            middleButton.setPadding(20, 20, 20, 20);
            middleButton.setCompoundDrawablePadding(10);
            int finalI = i;
            middleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onUnitResult(true, item.id, nameOfUnit+l2_table_photo[finalI]+"___________",rankNameU);
                        detailsContainer.removeAllViews();
                        detailsContainer.setVisibility(View.GONE);
                    }
                }
            });
            ImageButton rightImageButton = new ImageButton(context);
            rightImageButton.setId(View.generateViewId());
            rightImageButton.setLayoutParams(new LinearLayout.LayoutParams(
                    150, LinearLayout.LayoutParams.MATCH_PARENT));
            rightImageButton.setImageResource(R.drawable.arrow_right); // Change image source dynamically
            rightImageButton.setPadding(5, 5, 5, 5);
            rightImageButton.setScaleType(ImageView.ScaleType.CENTER);

            rightImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentLayout=3;
                    nameOfUnit = nameOfUnit.concat(l2_table_photo[finalI]);
                    generateLayout3(item,detailsContainer);
                    l2.removeAllViews();
                    l2.setVisibility(View.GONE);
                    l3.setVisibility(View.VISIBLE);
                }
            });
            itemLayout.addView(leftImageButton);
            itemLayout.addView(middleButton);
            itemLayout.addView(rightImageButton);

            l2.addView(itemLayout);
        }
    }
    public void generateLayout3(DetectObject item, FrameLayout detailsContainer){
        for (int i = 0; i < l3_table.length; i++) {
            LinearLayout itemLayout = new LinearLayout(context);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f));

            ImageButton leftImageButton = new ImageButton(context);
            leftImageButton.setId(View.generateViewId());
            leftImageButton.setLayoutParams(new LinearLayout.LayoutParams(
                    250, LinearLayout.LayoutParams.MATCH_PARENT));
            int leftImageRes = context.getResources().getIdentifier((nameOfUnit+l3_table_photo[i]), "drawable", context.getPackageName());
            leftImageButton.setImageResource(leftImageRes);
            leftImageButton.setPadding(5, 5, 5, 5);

            Button middleButton = new Button(context);
            middleButton.setId(View.generateViewId());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    600, LinearLayout.LayoutParams.MATCH_PARENT);
            params.setMargins(-30, 0, 0, 0); // Ustawiamy margines lewy na -10dp, pozostałe marginesy na 0

            middleButton.setLayoutParams(params);
            middleButton.setText(l3_table[i]); // Set the button text based on the table array
            middleButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            middleButton.setTextSize(10);
            middleButton.setPadding(20, 20, 20, 20);
            middleButton.setCompoundDrawablePadding(10);
            int finalI = i;
            middleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onUnitResult(true, item.id, nameOfUnit+l3_table_photo[finalI],rankNameU);
                        detailsContainer.removeAllViews();
                        detailsContainer.setVisibility(View.GONE);
                    }
                }
            });

            ImageButton rightImageButton = new ImageButton(context);
            rightImageButton.setId(View.generateViewId());
            rightImageButton.setLayoutParams(new LinearLayout.LayoutParams(
                    150, LinearLayout.LayoutParams.MATCH_PARENT));
            rightImageButton.setImageResource(R.drawable.arrow_right); // Change image source dynamically
            rightImageButton.setPadding(5, 5, 5, 5);
            rightImageButton.setScaleType(ImageView.ScaleType.CENTER);

            rightImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentLayout=4;
                    generateLayout4(item,detailsContainer);
                    l3.removeAllViews();
                    l3.setVisibility(View.GONE);
                    l4.setVisibility(View.VISIBLE);
                }
            });
            itemLayout.addView(leftImageButton);
            itemLayout.addView(middleButton);
            itemLayout.addView(rightImageButton);

            l3.addView(itemLayout);
        }
    }
    public void generateLayout4(DetectObject item, FrameLayout detailsContainer){
        for (int i = 0; i < l4_table.length; i++) {
            LinearLayout itemLayout = new LinearLayout(context);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f));

            ImageButton leftImageButton = new ImageButton(context);
            leftImageButton.setId(View.generateViewId());
            leftImageButton.setLayoutParams(new LinearLayout.LayoutParams(
                    250, LinearLayout.LayoutParams.MATCH_PARENT));
            int leftImageRes = context.getResources().getIdentifier((nameOfUnit + l4_table_photo[i] + "_________"), "drawable", context.getPackageName());
            leftImageButton.setImageResource(leftImageRes);
            leftImageButton.setPadding(5, 5, 5, 5);

            Button middleButton = new Button(context);
            middleButton.setId(View.generateViewId());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    600, LinearLayout.LayoutParams.MATCH_PARENT);
            params.setMargins(-30, 0, 0, 0); // Ustawiamy margines lewy na -10dp, pozostałe marginesy na 0

            middleButton.setLayoutParams(params);
            middleButton.setText(l4_table[i]); // Set the button text based on the table array
            middleButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            middleButton.setTextSize(10);
            middleButton.setPadding(20, 20, 20, 20);
            middleButton.setCompoundDrawablePadding(10);
            int finalI = i;
            middleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onUnitResult(true, item.id, nameOfUnit+l4_table_photo[finalI]+"_________",rankNameU);
                        detailsContainer.removeAllViews();
                        detailsContainer.setVisibility(View.GONE);
                    }
                }
            });
            ImageButton rightImageButton = new ImageButton(context);
            rightImageButton.setId(View.generateViewId());
            rightImageButton.setLayoutParams(new LinearLayout.LayoutParams(
                    150, LinearLayout.LayoutParams.MATCH_PARENT));
            rightImageButton.setImageResource(R.drawable.arrow_right); // Change image source dynamically
            rightImageButton.setPadding(5, 5, 5, 5);
            rightImageButton.setScaleType(ImageView.ScaleType.CENTER);

            rightImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentLayout=5;
                    nameOfUnit = nameOfUnit.concat(l4_table_photo[finalI]);
                    generateLayout5(item,detailsContainer);
                    l4.removeAllViews();
                    l4.setVisibility(View.GONE);
                    l5.setVisibility(View.VISIBLE);
                }
            });
            itemLayout.addView(leftImageButton);
            itemLayout.addView(middleButton);
            itemLayout.addView(rightImageButton);

            l4.addView(itemLayout);
        }
    }
    public void generateLayout5(DetectObject item, FrameLayout detailsContainer){
        for (int i = 0; i < l5_table.length; i++) {
            LinearLayout itemLayout = new LinearLayout(context);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f));

            ImageButton leftImageButton = new ImageButton(context);
            leftImageButton.setId(View.generateViewId());
            leftImageButton.setLayoutParams(new LinearLayout.LayoutParams(
                    150, LinearLayout.LayoutParams.MATCH_PARENT));

            int leftImageRes;
            if(l5_table_photo[i].length()==2){
                leftImageRes = context.getResources().getIdentifier((nameOfUnit+l5_table_photo[i]+"_______"), "drawable", context.getPackageName());
            }else {
                leftImageRes = context.getResources().getIdentifier((nameOfUnit +l5_table_photo[i] + "________"), "drawable", context.getPackageName());
            }
            leftImageButton.setImageResource(leftImageRes);
            leftImageButton.setPadding(5, 5, 5, 5);

            Button middleButton = new Button(context);
            middleButton.setId(View.generateViewId());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    600, LinearLayout.LayoutParams.MATCH_PARENT);
            params.setMargins(-30, 0, 0, 0); // Ustawiamy margines lewy na -10dp, pozostałe marginesy na 0

            middleButton.setLayoutParams(params);
            middleButton.setText(l5_table[i]); // Set the button text based on the table array
            middleButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            middleButton.setTextSize(10);
            middleButton.setPadding(20, 20, 20, 20);
            middleButton.setCompoundDrawablePadding(10);
            int finalI = i;
            middleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        if(l5_table_photo[finalI].length()==2){
                            listener.onUnitResult(true, item.id, nameOfUnit+l5_table_photo[finalI]+"_______",rankNameU);
                        }else {
                            listener.onUnitResult(true, item.id, nameOfUnit+l5_table_photo[finalI]+"________",rankNameU);
                        }
                        detailsContainer.removeAllViews();
                        detailsContainer.setVisibility(View.GONE);
                    }
                }
            });
            itemLayout.addView(leftImageButton);
            itemLayout.addView(middleButton);

            l5.addView(itemLayout);
        }
    }
}
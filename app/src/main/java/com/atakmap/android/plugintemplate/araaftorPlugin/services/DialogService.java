package com.atakmap.android.plugintemplate.araaftorPlugin.services;

import com.atakmap.android.plugintemplate.detection.BoundingBox;
import com.atakmap.android.plugintemplate.araaftorPlugin.DetectObject;
import com.atakmap.android.plugintemplate.araaftorPlugin.interfaces.DialogResultListener;
import com.atakmap.android.plugintemplate.plugin.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DialogService {
    private Context context;
    ImageButton zoom_out_image;
    ImageButton arrow_left;
    ImageButton arrow_right;
    ImageButton zoom_in_image;
    EditText editTextClsName;
    ImageButton compas;
    ImageButton add_box;
    ImageButton delete_box;
    TextView azmt;
    Button doneButton;
    Button backButton;
    SeekBar seekBarZoom;
    ImageView photo;

    TextView x1_val;
    TextView y1_val;
    TextView x2_val;
    TextView y2_val;
    LinearLayout conf_lay;
    RelativeLayout main_lay;
    private int currentIndex = 0;
    private float dX, dY;
    View detailsView;
    private float rotationAngle = 0;
    private Bitmap imagePhoto;
    private float scaleFactor = 1.0f;
    FrameLayout boundingBoxLayout;
    ScaleGestureDetector scaleGestureDetector;
    private float scaleFactorGesture = 1.f;
    private List<FrameLayout> boundingBoxContainers = new ArrayList<>();
    private View lastSelectedBoundingBox = null;
    private int lastSelectedBoundingBoxIndex = -1;
    private DialogResultListener listener;
    public DialogService(Context context, DialogResultListener listener) {
        this.context = context;
        this.listener = listener;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        detailsView = inflater.inflate(R.layout.change_imgae, null);
        compas = detailsView.findViewById(R.id.compas);
        add_box= detailsView.findViewById(R.id.add_box);
        delete_box= detailsView.findViewById(R.id.delete_box);
        azmt= detailsView.findViewById(R.id.azmt);
        doneButton= detailsView.findViewById(R.id.doneButton);
        backButton= detailsView.findViewById(R.id.backButton);
        zoom_out_image = detailsView.findViewById(R.id.zoom_out_image);
        arrow_left = detailsView.findViewById(R.id.arrow_left);
        arrow_right = detailsView.findViewById(R.id.arrow_right);
        zoom_in_image = detailsView.findViewById(R.id.zoom_in_image);
        editTextClsName = detailsView.findViewById(R.id.editTextClsName);
        seekBarZoom = detailsView.findViewById(R.id.seekBarZoom);
        photo = detailsView.findViewById(R.id.photo);
        x1_val = detailsView.findViewById(R.id.x1_val);
        y1_val = detailsView.findViewById(R.id.y1_val);
        x2_val = detailsView.findViewById(R.id.x2_val);
        y2_val = detailsView.findViewById(R.id.y2_val);
        main_lay = detailsView.findViewById(R.id.main_lay);
        boundingBoxLayout = detailsView.findViewById(R.id.boundingBoxLayout);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void showDialog(DetectObject item, LayoutInflater inflater) {
        FrameLayout detailsContainer = ((Activity) context).findViewById(R.id.container2);

        if (detailsContainer.getChildCount() == 0) {

            DecimalFormat dec = new DecimalFormat("0.00");
            editTextClsName.setText(item.result.get(currentIndex).clsName);
            File picturesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File file = new File(picturesDirectory, item.photoPath);
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            imagePhoto = bitmap;
            LinearLayout confLay = detailsView.findViewById(R.id.layoutConf);
            TextView newTextView = new TextView(context);
            newTextView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            newTextView.setText(item.result.get(currentIndex).clsName + " - " + dec.format(item.result.get(currentIndex).cnf) + "%");
            newTextView.setTextSize(12);
            compas.setRotation((float) item.azimuth); // Ustawianie nowego kąta przycisku
            azmt.setText(String.valueOf((int) item.azimuth));
            confLay.addView(newTextView);

            photo.setImageBitmap(bitmap);
            if(item.result.size()==1){
                arrow_left.setBackgroundColor(Color.GRAY);
                arrow_right.setBackgroundColor(Color.GRAY);
            } else if (item.result.size()>1) {
                arrow_left.setBackgroundColor(Color.GRAY);
                arrow_right.setBackgroundColor(Color.WHITE);
            }
            arrow_left.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentIndex > 0) {
                        currentIndex--;
                        arrow_right.setBackgroundColor(Color.WHITE);
                        if(currentIndex==0){
                            arrow_left.setBackgroundColor(Color.GRAY);
                        }
                        editTextClsName.setText(item.result.get(currentIndex).clsName );
                        newTextView.setText(item.result.get(currentIndex).clsName + " - " + dec.format(item.result.get(currentIndex).cnf) + "%");
                        checkBoundingBox(boundingBoxContainers.get(currentIndex).getChildAt(0), currentIndex);
                        focusOnBoundingBox(item,currentIndex);
                        calculateBoundingBox(bitmap, currentIndex);
                    }
                }
            });
            editTextClsName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable editable) {
                    BoundingBox box = item.result.get(currentIndex);
                    box.clsName = editable.toString();
                    item.result.set(currentIndex,box);
                }
            });
            add_box.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addBoundingBox(item);
                    refreshBoundingBoxDisplay(item);
                }
            });
            delete_box.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(item.result.size()>1) {
                        removeBoundingBox(item, currentIndex);
                    }
                }
            });
            zoom_out_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    photo.setX(0);
                    photo.setY(0);
                    boundingBoxLayout.setX(0);
                    boundingBoxLayout.setY(0);
                    scaleFactor=1f;
                    seekBarZoom.setProgress(0);
                }
            });
            zoom_in_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    focusOnBoundingBox(item, currentIndex);
                }
            });
            arrow_right.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentIndex < item.result.size() - 1) {
                        currentIndex++;
                        arrow_left.setBackgroundColor(Color.WHITE);
                        if(currentIndex==item.result.size() - 1)
                        {
                            arrow_right.setBackgroundColor(Color.GRAY);
                        }
                        editTextClsName.setText(item.result.get(currentIndex).clsName);
                        newTextView.setText(item.result.get(currentIndex).clsName + " - " + dec.format(item.result.get(currentIndex).cnf) + "%");
                        checkBoundingBox(boundingBoxContainers.get(currentIndex).getChildAt(0), currentIndex);
                        focusOnBoundingBox(item,currentIndex);
                        calculateBoundingBox(bitmap, currentIndex);
                    }
                }
            });

            photo.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int action = event.getAction();
                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            dX = v.getX() - event.getRawX();
                            dY = v.getY() - event.getRawY();
                            return true;

                        case MotionEvent.ACTION_MOVE:
                            float newX = event.getRawX() + dX;
                            float newY = event.getRawY() + dY;

                            RelativeLayout parentLayout = (RelativeLayout) v.getParent();
                            int parentWidth = parentLayout.getWidth();
                            int parentHeight = parentLayout.getHeight();

                            float  minX = (0 - (photo.getWidth()*scaleFactor - parentWidth))/2;;
                            float maxX = (photo.getWidth()*scaleFactor - parentWidth)/2;
                            float  minY = (0 - (photo.getHeight()*scaleFactor - parentHeight))/2;;
                            float maxY = (photo.getHeight()*scaleFactor - parentHeight)/2;
                            if(parentWidth>(photo.getWidth()*scaleFactor))
                            {
                                minX = -minX;
                                maxX = -maxX;
                            }
                            if(parentHeight>(photo.getHeight()*scaleFactor))
                            {
                                minY = -minY;
                                maxY = -maxY;
                            }

                            if(newX>=minX && newX<=maxX)
                            {
                                v.setX(newX);
                                boundingBoxLayout.setX(newX);
                            }else if (parentWidth==(photo.getWidth()*scaleFactor)) {
                                v.setX(0);
                                boundingBoxLayout.setX(0);
                            }
                            if(newY>=minY && newY<=maxY)
                            {
                                v.setY(newY);
                                boundingBoxLayout.setY(newY);
                            } else if (parentHeight==(photo.getHeight()*scaleFactor)) {
                                v.setY(0);
                                boundingBoxLayout.setY(0);
                            }
                            return true;
                        default:
                            return false;
                    }
                }
            });

            seekBarZoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    scaleFactor = 1 + (progress / 100f);
                    photo.setScaleX(scaleFactor);
                    photo.setScaleY(scaleFactor);
                    boundingBoxLayout.setScaleX(scaleFactor);
                    boundingBoxLayout.setScaleY(scaleFactor);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    photo.setX(0);
                    photo.setY(0);
                    boundingBoxLayout.setX(0);
                    boundingBoxLayout.setY(0);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    photo.setX(0);
                    photo.setY(0);
                    boundingBoxLayout.setX(0);
                    boundingBoxLayout.setY(0);
                }
            });

            photo.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    photo.getViewTreeObserver().removeOnPreDrawListener(this);
                    for (BoundingBox box : item.result) {
                        FrameLayout boundingBoxContainer = new FrameLayout(context);
                        View boundingBox = new View(context);
                        GradientDrawable drawable = new GradientDrawable();
                        drawable.setShape(GradientDrawable.RECTANGLE);
                        drawable.setStroke(2, Color.RED);
                        drawable.setColor(Color.TRANSPARENT);

                        float scalePhotoX = (float) photo.getWidth() / 256;
                        float scalePhotoY = (float) photo.getHeight() / 256;

                        boundingBox.setBackground(drawable);
                        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                                (int) ((box.x2 - box.x1) * scalePhotoX),
                                (int) ((box.y2 - box.y1) * scalePhotoY)
                        );

                        params.leftMargin = (int) (box.x1 * scalePhotoX);
                        params.topMargin = (int) (box.y1 * scalePhotoY);
                        boundingBox.setLayoutParams(params);
                        boundingBoxContainer.addView(boundingBox);
                        addResizeHandles(boundingBoxContainer, params.leftMargin, params.topMargin, boundingBox, box, item);
                        boundingBox.setOnTouchListener(new View.OnTouchListener() {
                            private float dx, dy;

                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                if (lastSelectedBoundingBox != null) {
                                    GradientDrawable prevDrawable = (GradientDrawable) lastSelectedBoundingBox.getBackground();
                                    prevDrawable.setStroke(2, Color.RED);
                                    lastSelectedBoundingBox.setBackground(prevDrawable);
                                }

                                GradientDrawable currentDrawable = (GradientDrawable) v.getBackground();
                                currentDrawable.setStroke(4, Color.GREEN);
                                v.setBackground(currentDrawable);

                                lastSelectedBoundingBox = v;
                                lastSelectedBoundingBoxIndex = box.getIndex();
                                currentIndex = box.getIndex();
                                if(currentIndex==item.result.size()-1)
                                {
                                    arrow_right.setBackgroundColor(Color.GRAY);
                                    arrow_left.setBackgroundColor(Color.WHITE);
                                }
                                if(currentIndex==0)
                                {
                                    arrow_right.setBackgroundColor(Color.WHITE);
                                    arrow_left.setBackgroundColor(Color.GRAY);
                                }
                                if(item.result.size()==1)
                                {
                                    arrow_right.setBackgroundColor(Color.GRAY);
                                    arrow_left.setBackgroundColor(Color.GRAY);
                                }

                                BoundingBox selectedBox = item.result.get(currentIndex);
                                editTextClsName.setText(selectedBox.clsName);
                                newTextView.setText(item.result.get(currentIndex).clsName + " - " + dec.format(item.result.get(currentIndex).cnf) + "%");
                                calculateBoundingBox(bitmap, currentIndex);
                                int action = event.getAction();
                                switch (action) {
                                    case MotionEvent.ACTION_DOWN:
                                        dx = v.getX() - event.getRawX();
                                        dy = v.getY() - event.getRawY();
                                        return true;

                                    case MotionEvent.ACTION_MOVE:
                                        float newX = event.getRawX() + dx;
                                        float newY = event.getRawY() + dy;

                                        newX = Math.max(0, Math.min(newX, photo.getWidth() - v.getWidth()));
                                        newY = Math.max(0, Math.min(newY, photo.getHeight() - v.getHeight()));
                                        v.setX(newX);
                                        updateResizeHandles(boundingBoxContainer, newX, newY);
                                        v.setY(newY);
                                        return true;

                                    case MotionEvent.ACTION_UP:
                                        int[] results = calculateBoundingBox256(v.getX(), v.getY(),v.getWidth(), v.getHeight());
                                        BoundingBox newBox = new BoundingBox(
                                                results[0], results[1], results[2], results[3],
                                                (results[0] + ((results[2] - results[0]) / 2F)),
                                                (results[1] + ((results[3] - results[1]) / 2F)),
                                                (results[2] - results[0]), (results[3] - results[1]),
                                                selectedBox.cnf, selectedBox.getIndex(), selectedBox.clsName
                                        );
                                        item.result.set(currentIndex, newBox);
                                        calculateBoundingBox(bitmap, currentIndex);
                                        return true;

                                    default:
                                        return false;
                                }
                            }
                        });

                        boundingBoxContainers.add(boundingBoxContainer);
                        boundingBoxLayout.addView(boundingBoxContainer);
                        if(box.getIndex()==0)
                        {
                            GradientDrawable currentDrawable = (GradientDrawable) boundingBox.getBackground();
                            currentDrawable.setStroke(4, Color.GREEN);
                            boundingBox.setBackground(currentDrawable);
                            focusOnBoundingBox(item ,0);
                            calculateBoundingBox(bitmap, 0);
                            lastSelectedBoundingBox = boundingBox;
                            lastSelectedBoundingBoxIndex = 0;
                        }
                    }
                    return true;
                }
            });

            compas.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onDoubleTap(MotionEvent e) {
                            rotationAngle = 0;
                            compas.setRotation(rotationAngle);
                            return super.onDoubleTap(e);
                        }
                    });
                    gestureDetector.onTouchEvent(event);

                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        float dx = event.getX();
                        float dy = event.getY();
                        rotationAngle += 1;

                        if (rotationAngle >= 360) {
                            rotationAngle = 0;
                        }

                        compas.setRotation(rotationAngle);
                        azmt.setText(String.valueOf((int) rotationAngle));
                        item.setAzimuth(rotationAngle);
                    }
                    return true;
                }
            });
            doneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onDialogResult(true,item);
                        detailsContainer.removeAllViews();

                        detailsContainer.setVisibility(View.GONE);
                    }
                }
            });
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onDialogResult(false, item);
                        detailsContainer.removeAllViews();
                        detailsContainer.setVisibility(View.GONE);
                    }
                }
            });
            detailsContainer.addView(detailsView);
            detailsContainer.setVisibility(View.VISIBLE);
        }
    }
    private void checkBoundingBox(View v, int finalI){
        if (lastSelectedBoundingBox != null) {
            GradientDrawable prevDrawable = (GradientDrawable) lastSelectedBoundingBox.getBackground();
            prevDrawable.setStroke(2, Color.RED);
            lastSelectedBoundingBox.setBackground(prevDrawable);
        }

        GradientDrawable currentDrawable = (GradientDrawable) v.getBackground();
        currentDrawable.setStroke(4, Color.GREEN);
        v.setBackground(currentDrawable);

        lastSelectedBoundingBox = v;
        lastSelectedBoundingBoxIndex = finalI;
    }
    private void focusOnBoundingBox(DetectObject item, int currentIndex) {
        BoundingBox box = item.result.get(currentIndex);
        float scalePhotoX = (float) photo.getWidth() / 256;
        float scalePhotoY = (float) photo.getHeight() / 256;
        float boxWidth = (box.x2 - box.x1)*scalePhotoX;
        float boxHeight = (box.y2 - box.y1)*scalePhotoY;

        float scaleX = photo.getWidth() / (float) (boxWidth + 20);
        float scaleY = photo.getHeight() / (float) (boxHeight + 20);
        float zoomFactor = 3;
        if(scaleX<3&&scaleY<3&&scaleX>1&&scaleY>1){
            zoomFactor = Math.max(scaleX, scaleY);
        } else if (scaleY<3&&scaleX>=3&&scaleY>1) {
            zoomFactor = scaleY;
        } else if (scaleY>=3&&scaleX<3&&scaleX>1) {
            zoomFactor = scaleX;
        }

        if(zoomFactor==3)
        {
            seekBarZoom.setProgress(200);
            scaleFactor=3;
            photo.setScaleX(3);
            photo.setScaleY(3);
            boundingBoxLayout.setScaleX(3);
            boundingBoxLayout.setScaleY(3);
        }else
        {
            seekBarZoom.setProgress((int) ((zoomFactor-1)*100f));
            scaleFactor=(int) zoomFactor;
            photo.setScaleX(zoomFactor);
            photo.setScaleY(zoomFactor);
            boundingBoxLayout.setScaleX(zoomFactor);
            boundingBoxLayout.setScaleY(zoomFactor);
        }

        float centerX = ((box.x1 + box.x2) / 2);
        float centerY = ((box.y1 + box.y2) / 2);
        float offsetX = (((centerX*scalePhotoX)-((float) photo.getWidth() /2))*(scaleFactor))+20;
        float offsetY = (((centerY * scalePhotoY)-((float) photo.getHeight() /2))*(scaleFactor))+20;
        photo.setX(-offsetX);
        boundingBoxLayout.setX(-offsetX);
        photo.setY(-offsetY);
        boundingBoxLayout.setY(-offsetY);
    }
    private void updateResizeHandles(FrameLayout boundingBoxContainer, float newX, float newY) {
        View topLeftHandle = boundingBoxContainer.getChildAt(1);
        int handleOffset = 7;
        topLeftHandle.setX(newX - handleOffset);
        topLeftHandle.setY(newY - handleOffset);
    }
    private void addResizeHandles(FrameLayout boundingBoxContainer, int left, int top, View boundingBox, BoundingBox box, DetectObject item) {
        View topLeftHandle = createResizeHandle();
        int handleSize = 14;
        FrameLayout.LayoutParams paramsTopLeft = new FrameLayout.LayoutParams(handleSize, handleSize);
        paramsTopLeft.leftMargin = left - handleSize / 2;
        paramsTopLeft.topMargin = top - handleSize / 2;
        boundingBoxContainer.addView(topLeftHandle, paramsTopLeft);
        handleResize(topLeftHandle, "topLeft", boundingBox, box, item);
    }
    private View createResizeHandle() {
        View handle = new View(context);
        handle.setBackgroundColor(Color.BLUE);
        return handle;
    }
    private void handleResize(View handle, String direction, View boundingBox, BoundingBox box, DetectObject item) {
        handle.setOnTouchListener(new View.OnTouchListener() {
            private float startX, startY;
            private int originalWidth, originalHeight;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                handle.setBackgroundColor(Color.RED);
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getRawX();
                        startY = event.getRawY();
                        originalWidth = boundingBox.getWidth();
                        originalHeight = boundingBox.getHeight();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float deltaX = event.getRawX() - startX;
                        float deltaY = event.getRawY() - startY;

                        if (direction.equals("topLeft")) {
                            boundingBox.getLayoutParams().width = (int) (originalWidth - deltaX);
                            boundingBox.getLayoutParams().height = (int) (originalHeight - deltaY);
                            boundingBox.requestLayout();
                            calculateBoundingBox(imagePhoto,currentIndex);
                        } else if (direction.equals("topRight")) {
                            boundingBox.getLayoutParams().width = (int) (originalWidth + deltaX);
                            boundingBox.getLayoutParams().height = (int) (originalHeight - deltaY);
                            boundingBox.requestLayout();
                            calculateBoundingBox(imagePhoto,currentIndex);
                        } else if (direction.equals("bottomLeft")) {
                            boundingBox.getLayoutParams().width = (int) (originalWidth - deltaX);
                            boundingBox.getLayoutParams().height = (int) (originalHeight + deltaY);
                            boundingBox.requestLayout();
                            calculateBoundingBox(imagePhoto,currentIndex);
                        } else if (direction.equals("bottomRight")) {
                            boundingBox.getLayoutParams().width = (int) (originalWidth + deltaX);
                            boundingBox.getLayoutParams().height = (int) (originalHeight + deltaY);
                            boundingBox.requestLayout();
                            calculateBoundingBox(imagePhoto,currentIndex);
                        }

                        return true;

                    case MotionEvent.ACTION_UP:
                        handle.setBackgroundColor(Color.BLUE);
                        View x = boundingBoxContainers.get(currentIndex).getChildAt(0);
                        int[] results = calculateBoundingBox256(x.getX(), x.getY(),x.getWidth(), x.getHeight());
                        BoundingBox newBox = new BoundingBox(
                                results[0], results[1], results[2], results[3],
                                (results[0] + ((results[2] - results[0]) / 2F)),
                                (results[1] + ((results[3] - results[1]) / 2F)),
                                (results[2] - results[0]), (results[3] - results[1]),
                                box.cnf, box.getIndex(), box.clsName
                        );
                        item.result.set(currentIndex, newBox);
                    default:
                        return false;
                }
            }
        });
    }
    private void calculateBoundingBox(Bitmap bitmap, int currentIndex) {
        float scalePhotoX = (float) bitmap.getWidth()/photo.getWidth();
        float scalePhotoY = (float) bitmap.getHeight()/photo.getHeight();
        View box = boundingBoxContainers.get(currentIndex).getChildAt(0);
        int x1 = (int) (box.getX() * scalePhotoX);
        int y1 = (int) (box.getY() * scalePhotoY);
        int x2 = (int) ((box.getX()+box.getWidth()) * scalePhotoX);
        int y2 = (int) ((box.getY()+box.getHeight()) * scalePhotoY);
        setBoundingBox(x1,x2,y1,y2);
    }
    private int[] calculateBoundingBox256(float left, float top, float width, float height) {
        float scalePhotoX = (float) 256/photo.getWidth();
        float scalePhotoY = (float) 256/photo.getHeight();
        int x1 = (int) ((int) (left * scalePhotoX));
        int y1 = (int) ((int) (top * scalePhotoY));
        int x2 = (int) ((int) ((left+width) * scalePhotoX));
        int y2 = (int) ((int) ((top+height) * scalePhotoY));
        int[] x;
        return x = new int[]{x1, y1, x2, y2};
    }
    private void setBoundingBox(int x1, int x2, int y1, int y2) {
        x1_val.setText(String.valueOf(x1));
        x2_val.setText(String.valueOf(x2));
        y1_val.setText(String.valueOf(y1));
        y2_val.setText(String.valueOf(y2));
    }
    private void addBoundingBox(DetectObject item) {
        FrameLayout boundingBoxContainer = new FrameLayout(context);
        View boundingBox = new View(context);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setStroke(2, Color.RED);
        drawable.setColor(Color.TRANSPARENT);
        boundingBox.setBackground(drawable);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(100,100);
        params.leftMargin = (int) ((photo.getWidth()/2)-50);
        params.topMargin = (int) ((photo.getHeight()/2)-50);

        boundingBox.setLayoutParams(params);
        boundingBoxContainer.addView(boundingBox);
        boundingBoxContainers.add(boundingBoxContainer);
        boundingBoxLayout.addView(boundingBoxContainer);
        int[] results = calculateBoundingBox256(((photo.getWidth()/2)-50), ((photo.getHeight()/2)-50),100, 100);
        BoundingBox newBox = new BoundingBox(
                results[0], results[1], results[2], results[3],
                (results[0] + ((results[2] - results[0]) / 2F)),
                (results[1] + ((results[3] - results[1]) / 2F)),
                (results[2] - results[0]), (results[3] - results[1]),
                100, item.result.size(), "New bound box"
        );
        item.result.add(newBox);
        addResizeHandles(boundingBoxContainer, params.leftMargin, params.topMargin, boundingBox, newBox, item);
        boundingBox.setOnTouchListener(new View.OnTouchListener() {
            private float dx, dy;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (lastSelectedBoundingBox != null) {
                    GradientDrawable prevDrawable = (GradientDrawable) lastSelectedBoundingBox.getBackground();
                    prevDrawable.setStroke(2, Color.RED);
                    lastSelectedBoundingBox.setBackground(prevDrawable);
                }

                GradientDrawable currentDrawable = (GradientDrawable) v.getBackground();
                currentDrawable.setStroke(4, Color.GREEN);
                v.setBackground(currentDrawable);

                lastSelectedBoundingBox = v;
                lastSelectedBoundingBoxIndex = newBox.getIndex();
                currentIndex = newBox.getIndex();
                if(currentIndex==item.result.size()-1)
                {
                    arrow_right.setBackgroundColor(Color.GRAY);
                    arrow_left.setBackgroundColor(Color.WHITE);
                }
                if(currentIndex==0)
                {
                    arrow_right.setBackgroundColor(Color.WHITE);
                    arrow_left.setBackgroundColor(Color.GRAY);
                }
                if(item.result.size()==1)
                {
                    arrow_right.setBackgroundColor(Color.GRAY);
                    arrow_left.setBackgroundColor(Color.GRAY);
                }
                editTextClsName.setText(newBox.clsName);
                calculateBoundingBox(imagePhoto, currentIndex);
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        dx = v.getX() - event.getRawX();
                        dy = v.getY() - event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float newX = event.getRawX() + dx;
                        float newY = event.getRawY() + dy;

                        newX = Math.max(0, Math.min(newX, photo.getWidth() - v.getWidth()));
                        newY = Math.max(0, Math.min(newY, photo.getHeight() - v.getHeight()));
                        v.setX(newX);
                        updateResizeHandles(boundingBoxContainer, newX, newY);
                        v.setY(newY);
                        return true;

                    case MotionEvent.ACTION_UP:
                        int[] results = calculateBoundingBox256(v.getX(), v.getY(),v.getWidth(), v.getHeight());
                        BoundingBox newBox2 = new BoundingBox(
                                results[0], results[1], results[2], results[3],
                                (results[0] + ((results[2] - results[0]) / 2F)),
                                (results[1] + ((results[3] - results[1]) / 2F)),
                                (results[2] - results[0]), (results[3] - results[1]),
                                newBox.cnf, newBox.getIndex(), newBox.clsName
                        );
                        item.result.set(currentIndex, newBox2);
                        calculateBoundingBox(imagePhoto, currentIndex);
                        return true;

                    default:
                        return false;
                }
            }
        });

    }
    private void removeBoundingBox(DetectObject item, int indexToRemove) {
        item.result.remove(indexToRemove);
        boundingBoxContainers.remove(indexToRemove);
        currentIndex=0;
        if(item.result.size()==1){
            arrow_left.setBackgroundColor(Color.GRAY);
            arrow_right.setBackgroundColor(Color.GRAY);
        } else if (item.result.size()>1) {
            arrow_left.setBackgroundColor(Color.GRAY);
            arrow_right.setBackgroundColor(Color.WHITE);
        }
        for (int i = 0; i < item.result.size(); i++) {
            BoundingBox box = item.result.get(i);
            box.setIndex(i);
        }
        refreshBoundingBoxDisplay(item);
        calculateBoundingBox(imagePhoto, currentIndex);
        focusOnBoundingBox(item, currentIndex);
        if (lastSelectedBoundingBox != null) {
            GradientDrawable prevDrawable = (GradientDrawable) lastSelectedBoundingBox.getBackground();
            prevDrawable.setStroke(2, Color.RED);
            lastSelectedBoundingBox.setBackground(prevDrawable);
        }
        View v = boundingBoxContainers.get(0).getChildAt(0);
        GradientDrawable currentDrawable = (GradientDrawable) v.getBackground();
        currentDrawable.setStroke(4, Color.GREEN);
        v.setBackground(currentDrawable);

        lastSelectedBoundingBox = v;
        lastSelectedBoundingBoxIndex = item.result.get(0).getIndex();
        editTextClsName.setText(item.result.get(currentIndex).clsName);
    }
    private void refreshBoundingBoxDisplay(DetectObject item) {
        boundingBoxLayout.removeAllViews();
        for (int i = 0; i < item.result.size(); i++) {
            boundingBoxLayout.addView(boundingBoxContainers.get(i));
        }
    }
}

package com.atakmap.android.plugintemplate.detection;

import static com.atakmap.android.plugintemplate.detection.MetaData.extractNamesFromLabelFile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.util.Pair;


import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import org.tensorflow.lite.support.tensorbuffer.TensorBufferFloat;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DetectorObject {
    public String MODEL_NAME = "";
    private static final String Label_NAME = "labels.txt";
    private static final int NUM_THREADS = 4;
    private long inferenceTime;
    private Interpreter interpreter;
    private static final float IOU_THRESHOLD = 0.5f;
    private static final float CONFIDENCE_THRESHOLD = 40f;

    private ImageProcessor inputTensorProcessor;
    private TensorProcessor outputTensorProcessor;
    private int tensorWidth = 0;
    private int tensorHeight = 0;
    private int numChannel = 0;
    private int numElements = 0;
    private List<String> labels = new ArrayList<>();
    public Context contextActivity;

    public DetectorObject(Context contextActivity, String model) throws IOException {

        this.MODEL_NAME = model;
        this.contextActivity = contextActivity;
        labels.addAll(extractNamesFromLabelFile(contextActivity, Label_NAME));
        Interpreter.Options interpreterOptions = new Interpreter.Options();
        interpreterOptions.setNumThreads(NUM_THREADS);
        try {
            interpreter = new Interpreter(FileUtil.loadMappedFile(contextActivity, MODEL_NAME), interpreterOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int[] inputShape = interpreter.getInputTensor(0).shape();
        int[] outputShape = interpreter.getOutputTensor(0).shape();

        tensorWidth = inputShape[1];
        tensorHeight=inputShape[2];
        numChannel=outputShape[1];
        numElements=outputShape[2];

        inputTensorProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(tensorHeight, tensorWidth, ResizeOp.ResizeMethod.BILINEAR))
                .add(new NormalizeOp(100, 100))
                .build();

        outputTensorProcessor = new TensorProcessor.Builder()
                .add(new DepthScalingOp())
                .build();
    }
    public static class DepthScalingOp implements TensorOperator {

        @Override
        public TensorBuffer apply(TensorBuffer input) {
            float[] values = input.getFloatArray();

            float max = values[0];
            float min = values[0];
            for (float value : values) {
                if (value > max) max = value;
                if (value < min) min = value;
            }

            if (max - min > Float.MIN_VALUE) {
                for (int i = 0; i < values.length; i++) {
                    int p = (int) (((values[i] - min) / (max - min)) * 255);
                    if (p < 0) {
                        p += 255;
                    }
                    values[i] = p;
                }
            } else {
                Arrays.fill(values, 0.0f);
            }

            TensorBuffer output = TensorBufferFloat.createFrom(input, DataType.FLOAT32);
            output.loadArray(values);
            return output;
        }
    }
    public Pair<Long, List<BoundingBox>> Detect (Bitmap inputImage) {
        TensorImage inputTensor = TensorImage.fromBitmap(inputImage);
        long startTime = System.currentTimeMillis();
        inputTensor = inputTensorProcessor.process(inputTensor);
        TensorBuffer outputTensor = TensorBufferFloat.createFixedSize(new int[]{1, numChannel, numElements}, DataType.FLOAT32);
        interpreter.run(inputTensor.getBuffer(), outputTensor.getBuffer());
        outputTensor = outputTensorProcessor.process(outputTensor);
        List<BoundingBox> bestBoxes = bestBox(outputTensor.getFloatArray());
        inferenceTime = System.currentTimeMillis() - startTime;
        return new Pair<>(inferenceTime, bestBoxes);
    }

    private List<BoundingBox> bestBox(float[] array) {
        List<BoundingBox> boundingBoxes = new ArrayList<>();

        for (int c = 0; c < numElements; c++) {
            float maxConf = CONFIDENCE_THRESHOLD;
            int maxIdx = -1;
            int j = 4;
            int arrayIdx = c + numElements * j;
            while (j < numChannel) {
                if (array[arrayIdx] > maxConf) {
                    maxConf = array[arrayIdx];
                    maxIdx = j - 4;
                }
                j++;
                arrayIdx += numElements;
            }

            if (maxConf > CONFIDENCE_THRESHOLD) {
                String clsName = labels.get(maxIdx);
                float cx = array[c]; // 0
                float cy = array[c + numElements]; // 1
                float w = array[c + numElements * 2];
                float h = array[c + numElements * 3];
                float x1 = cx - (w / 2F);
                float y1 = cy - (h / 2F);
                float x2 = cx + (w / 2F);
                float y2 = cy + (h / 2F);
                float width = tensorWidth;
                float height = tensorHeight;
                if (x1 < 0F || x1 > width) continue;
                if (y1 < 0F || y1 > height) continue;
                if (x2 < 0F || x2 > width) continue;
                if (y2 < 0F || y2 > height) continue;
                if(maxIdx==0) {
                    boundingBoxes.add(new BoundingBox(
                            x1, y1, x2, y2,
                            cx, cy, w, h,
                            ((int)((maxConf/256)*100)),boundingBoxes.size(), clsName
                    ));
                }
            }
        }
        return applyNMS(boundingBoxes);
    }

    private List<BoundingBox> applyNMS(List<BoundingBox> boxes) {
        List<BoundingBox> sortedBoxes = new ArrayList<>(boxes);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sortedBoxes.sort((box1, box2) -> Float.compare(box2.cnf, box1.cnf));
        }
        List<BoundingBox> selectedBoxes = new ArrayList<>();
        while (!sortedBoxes.isEmpty()) {
            BoundingBox first = sortedBoxes.get(0);
            selectedBoxes.add(first);
            sortedBoxes.remove(first);

            for (int i = 0; i < sortedBoxes.size(); i++) {
                BoundingBox nextBox = sortedBoxes.get(i);
                float iou = calculateIoU(first, nextBox);
                if (iou >= IOU_THRESHOLD) {
                    sortedBoxes.remove(i);
                    i--;
                }
            }
        }
        for (int i = 0; i < selectedBoxes.size(); i++) {
            selectedBoxes.get(i).setIndex(i);
        }
        return selectedBoxes;
    }

    private float calculateIoU(BoundingBox box1, BoundingBox box2) {
        float x1 = Math.max(box1.x1, box2.x1);
        float y1 = Math.max(box1.y1, box2.y1);
        float x2 = Math.min(box1.x2, box2.x2);
        float y2 = Math.min(box1.y2, box2.y2);
        float intersectionArea = Math.max(0F, x2 - x1) * Math.max(0F, y2 - y1);
        float box1Area = box1.w * box1.h;
        float box2Area = box2.w * box2.h;
        return intersectionArea / (box1Area + box2Area - intersectionArea);
    }

    public Bitmap drawBoundingBoxes(Bitmap bitmap, List<BoundingBox> boxes) {
        Bitmap mutableBitmap = Bitmap.createScaledBitmap(bitmap, 640, 640, true);
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.rgb(220, 244, 0));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1f);
        Paint textPaint = new Paint();
        textPaint.setColor(Color.rgb(220, 244, 0));
        textPaint.setTextSize(20f);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        int i = 1;
        for (BoundingBox box : boxes) {
            float x1 = (float) (box.x1 * 2.5);
            float y1 = (float) (box.y1 * 2.5);
            float x2 = (float) (box.x2 * 2.5);
            float y2 = (float) (box.y2 * 2.5);
            canvas.drawRect(x1, y1, x2, y2, paint);
            canvas.drawText(box.clsName + " " + i, x1, y2, textPaint);
            i++;
        }
        return mutableBitmap;
    }
    public Bitmap drawDistance(Bitmap bitmap, List<BoundingBox> boxes, List<Double> distance) {
        DecimalFormat df = new DecimalFormat("0.00");
        Canvas canvas = new Canvas(bitmap);
        Paint textPaint = new Paint();
        textPaint.setColor(Color.rgb(220, 244, 0));
        textPaint.setTextSize(20f);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        int i = 0;
        for (BoundingBox box : boxes) {
            double x = distance.get(i);
            String formattedValue = df.format(x);
            float x1 = (float) (box.x1 * 2.5);
            float y1 = (float) (box.y1*2.5);
            float x2 = (float) (box.x2 *2.5);
            float y2 = (float) (box.y2 *2.5);
            canvas.drawText("Distance: " + formattedValue, x1, y1, textPaint);
            i++;
        }
        return bitmap;
    }
}



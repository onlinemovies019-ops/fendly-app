package com.example.fendly;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;

import java.util.List;

final class CommunityPosterPhotoFocus {
    interface Callback {
        void onFocusDetected(Rect bounds);

        void onDetectionFailed(Exception error);
    }

    private interface FailureHandler {
        void onFailure(Exception error);
    }

    private CommunityPosterPhotoFocus() {}

    static void detect(Bitmap photo, boolean preferFace, Callback callback) {
        if (photo == null || photo.isRecycled()) {
            callback.onFocusDetected(null);
            return;
        }

        try {
            InputImage image = InputImage.fromBitmap(photo, 0);
            if (preferFace) {
                detectFace(
                        image,
                        callback,
                        () -> detectObject(image, callback, () -> callback.onFocusDetected(null), error -> callback.onDetectionFailed(error)),
                        error -> detectObject(image, callback, () -> callback.onDetectionFailed(error), callback::onDetectionFailed)
                );
            } else {
                detectObject(
                        image,
                        callback,
                        () -> detectFace(image, callback, () -> callback.onFocusDetected(null), callback::onDetectionFailed),
                        error -> detectFace(
                                image,
                                callback,
                                () -> callback.onDetectionFailed(error),
                                callback::onDetectionFailed
                        )
                );
            }
        } catch (RuntimeException error) {
            callback.onDetectionFailed(error);
        }
    }

    private static void detectFace(
            InputImage image,
            Callback callback,
            Runnable onNoFace,
            FailureHandler onFailure
    ) {
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .build();
        FaceDetector detector = FaceDetection.getClient(options);
        detector.process(image)
                .addOnSuccessListener(faces -> {
                    detector.close();
                    Rect focus = largestFaceBounds(faces);
                    if (focus != null) {
                        callback.onFocusDetected(focus);
                    } else {
                        onNoFace.run();
                    }
                })
                .addOnFailureListener(error -> {
                    detector.close();
                    onFailure.onFailure(error);
                });
    }

    private static void detectObject(
            InputImage image,
            Callback callback,
            Runnable onNoObject,
            FailureHandler onFailure
    ) {
        ObjectDetectorOptions options = new ObjectDetectorOptions.Builder()
                .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                .enableClassification()
                .build();
        ObjectDetector detector = ObjectDetection.getClient(options);
        detector.process(image)
                .addOnSuccessListener(objects -> {
                    detector.close();
                    Rect focus = largestObjectBounds(objects);
                    if (focus != null) {
                        callback.onFocusDetected(focus);
                    } else {
                        onNoObject.run();
                    }
                })
                .addOnFailureListener(error -> {
                    detector.close();
                    onFailure.onFailure(error);
                });
    }

    private static Rect largestFaceBounds(List<Face> faces) {
        Rect largest = null;
        for (Face face : faces) {
            Rect bounds = face.getBoundingBox();
            if (largest == null || area(bounds) > area(largest)) largest = bounds;
        }
        return largest;
    }

    private static Rect largestObjectBounds(List<DetectedObject> objects) {
        Rect largest = null;
        for (DetectedObject object : objects) {
            Rect bounds = object.getBoundingBox();
            if (largest == null || area(bounds) > area(largest)) largest = bounds;
        }
        return largest;
    }

    private static long area(Rect bounds) {
        return (long) Math.max(0, bounds.width()) * Math.max(0, bounds.height());
    }
}

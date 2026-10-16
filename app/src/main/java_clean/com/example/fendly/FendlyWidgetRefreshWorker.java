package com.example.fendly;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public final class FendlyWidgetRefreshWorker extends Worker {
    public FendlyWidgetRefreshWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParameters
    ) {
        super(context, workerParameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            FendlyWidgetProvider.refreshWidgetsInBackground(getApplicationContext());
            return Result.success();
        } catch (Exception error) {
            Log.e("FendlyWidget", "Unable to refresh widget after a notification", error);
            return Result.retry();
        }
    }
}

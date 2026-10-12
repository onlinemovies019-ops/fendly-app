package com.example.fendly;

import android.content.Context;

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
        FendlyWidgetProvider.refreshWidgetsInBackground(getApplicationContext());
        return Result.success();
    }
}

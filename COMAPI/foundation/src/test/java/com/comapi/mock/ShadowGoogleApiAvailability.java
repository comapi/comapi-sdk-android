package com.comapi.mock;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
@Implements(GoogleApiAvailability.class)
public class ShadowGoogleApiAvailability {

    private static int availabilityCode = ConnectionResult.SERVICE_MISSING;

    @Implementation
    public static int isGooglePlayServicesAvailable(Context context) {
        return availabilityCode;
    }

    public static void setIsGooglePlayServicesAvailable(int availabilityCode) {
        ShadowGoogleApiAvailability.availabilityCode = availabilityCode;
    }

}

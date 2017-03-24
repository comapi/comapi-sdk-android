package com.comapi.internal.network.api;

import com.comapi.ServiceAccessor;

/**
 * Public interface to access ComapiImplementation services.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface ComapiService extends ServiceAccessor.MessagingService, ServiceAccessor.ProfileService, ServiceAccessor.SessionService, ServiceAccessor.ChannelsService {

}

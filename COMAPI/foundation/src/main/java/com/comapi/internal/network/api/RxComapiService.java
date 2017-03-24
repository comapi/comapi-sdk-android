package com.comapi.internal.network.api;

import com.comapi.RxServiceAccessor;

/**
 * Public interface to access ComapiImplementation services.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface RxComapiService extends RxServiceAccessor.MessagingService, RxServiceAccessor.ProfileService, RxServiceAccessor.SessionService, RxServiceAccessor.ChannelsService {

}

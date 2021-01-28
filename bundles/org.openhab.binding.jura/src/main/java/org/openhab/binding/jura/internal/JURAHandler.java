/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 * <p>
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 * <p>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 * <p>
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.jura.internal;

import static org.openhab.binding.jura.internal.JURABindingConstants.*;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link JURAHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Daniel Kerkmann - Initial contribution
 */
@NonNullByDefault
public class JURAHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(JURAHandler.class);
    private final HttpClient httpClient;

    private final JURAConfiguration config;
    private @Nullable ScheduledFuture<?> pollingJob;

    public JURAHandler(Thing thing, HttpClient httpClient) {
        super(thing);

        this.httpClient = httpClient;
        this.config = getConfigAs(JURAConfiguration.class);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        pollingJob = scheduler.scheduleWithFixedDelay(this::updateCoffeeData, 5, 60, TimeUnit.SECONDS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateCoffeeData();
        }

        logger.debug("handleCommand {} {}", channelUID.getId(), command.toFullString());

        try {
            switch (channelUID.getId()) {
                case CHANNEL_POWER:
                    if (command.equals(OnOffType.ON))
                        sendData("AN:02", "ok:");
                    break;
                case CHANNEL_BREW_COFFEE:
                    if (command.equals(OnOffType.ON))
                        sendData("FA:07", "ok:");
                    break;
            }
        } catch (Exception e) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.UNKNOWN.NONE, e.getMessage());
        }
    }

    @Override
    public void dispose() {
        Objects.requireNonNull(pollingJob).cancel(true);
    }

    private void updateCoffeeData() {
        try {
            String data = sendData("RT:0000", "rt:");
            updateState(CHANNEL_CUPS_COFFEE, new DecimalType(convertData(data, 8, 4)));
            updateState(CHANNEL_CUPS_ESPRESSO, new DecimalType(convertData(data, 0, 4)));
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private int convertData(String raw, int begin, int size) {
        return Integer.valueOf(raw.substring(begin, begin + size), 16);
    }

    private String sendData(String command, String expect) throws Exception {
        String data = receiveData(command);
        if (!data.startsWith(expect))
            throw new InvalidResponseException("The data '" + data + "' has not the right response '" + expect + "'!");
        return data.substring(expect.length());
    }

    private String receiveData(String command) throws InterruptedException, ExecutionException, TimeoutException {
        logger.debug("Send data with command '{}'.", command);
        String data = httpClient.POST(config.host).content(new StringContentProvider(command))
                .timeout(2000, TimeUnit.MILLISECONDS).send().getContentAsString();
        logger.debug("Send data was successful, command: '{}', raw response: {}", command, data);
        return data;
    }
}

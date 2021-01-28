/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.jura.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link JURABindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Daniel Kerkmann - Initial contribution
 */
@NonNullByDefault
public class JURABindingConstants {

    private static final String BINDING_ID = "jura";

    public static final ThingTypeUID THING_TYPE_E60 = new ThingTypeUID(BINDING_ID, "e60");

    public static final String CHANNEL_CUPS_COFFEE = "cupsCoffee";
    public static final String CHANNEL_CUPS_ESPRESSO = "cupsEspresso";

    public static final String CHANNEL_BREW_COFFEE = "brewCoffee";

    public static final String CHANNEL_POWER = "power";
}

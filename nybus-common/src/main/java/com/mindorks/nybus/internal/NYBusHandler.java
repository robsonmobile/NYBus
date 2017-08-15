/*
 *    Copyright (C) 2017 MINDORKS NEXTGEN PRIVATE LIMITED
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.mindorks.nybus.internal;

import com.mindorks.nybus.Scheduler.SchedulerProvider;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Created by Jyoti on 14/08/17.
 */

public class NYBusHandler {
    private SchedulerProvider mSchedulerProvider;
    private ConcurrentHashMap<String, Set<Object>> mChannelToTargetsMap;

    public NYBusHandler() {
        mChannelToTargetsMap = new ConcurrentHashMap<>();
    }

    public void setSchedulerProvider(SchedulerProvider mSchedulerProvider) {
        this.mSchedulerProvider = mSchedulerProvider;
    }

    public void register(Object object, String channelId) {
        if (mChannelToTargetsMap.containsKey(channelId)) {
            Set<Object> targets = mChannelToTargetsMap.get(channelId);
            if (targets.contains(object)) {
                Logger.getLogger("", object.getClass() + " already registered for receiving events. Did you forget to unregister?");
            } else {
                targets.add(object);
                mChannelToTargetsMap.put(channelId, targets);
            }
        } else {
            Set<Object> targets = new HashSet<>();
            targets.add(object);
            mChannelToTargetsMap.put(channelId, targets);
        }
    }


    public void unregister(Object object, String channelId) {
        Set<Object> targets = mChannelToTargetsMap.get(channelId);
        try {
            targets.remove(object);
            mChannelToTargetsMap.put(channelId, targets);
        } catch (NullPointerException e) {
            Logger.getLogger("", "Unregistering subscriber that  was not registered before: " + object.getClass());
            e.printStackTrace();
        }


    }
}

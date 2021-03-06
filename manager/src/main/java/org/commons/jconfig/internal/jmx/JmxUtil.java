/*
 * Copyright 2011 Yahoo! Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.commons.jconfig.internal.jmx;

import java.lang.management.ManagementFactory;
import java.util.Random;

import javax.management.DynamicMBean;
import javax.management.JMException;
import javax.management.ObjectName;
import javax.management.modelmbean.InvalidTargetObjectTypeException;

import org.commons.jconfig.config.ConfigManager;
import org.commons.jconfig.internal.ConfigMBean;


public class JmxUtil {
    public static void registerConfigObject(final ConfigManager manager, final Class<?> clazz, String appName)
            throws JMException, InvalidTargetObjectTypeException {
        Random random = new Random();

        StringBuilder sb = new StringBuilder(ConfigManagerJmx.CONFIG_MBEANS_DOMAIN_PREFIX);
        sb.append(clazz.getName());
        sb.append(",id=");
        sb.append(random.nextInt(1000));
        sb.append(",appName=");
        sb.append(appName);

        DynamicMBean dynBean = new ConfigMBean(manager, clazz);
        if (!ManagementFactory.getPlatformMBeanServer().isRegistered(new ObjectName(sb.toString()))) {
            ManagementFactory.getPlatformMBeanServer().registerMBean(dynBean, new ObjectName(sb.toString()));
        }
    }

}

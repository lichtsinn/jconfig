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

package org.commons.jconfig.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.commons.jconfig.config.ConfigManager;


/**
 * {@link ConfigManager} use the method names by default, to load config values
 * from config files. For example if the method name is getFgRetries,
 * setFgRetries on the config class, {@link ConfigManager} will try to load a
 * property with name "FgRetries" by default. if present on the method @ConfigResourceId
 * annotations will instruct {@link ConfigManager} to use this name first,
 * instead of the method name.
 * 
 * ConfigResourceId was introduced for backward compatibility, for config files
 * that cannot be changed or where the usage of long names is required and these
 * names would not conform to java syntax.
 * 
 * @author lafa
 * 
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ConfigResourceId {

    String value();

}

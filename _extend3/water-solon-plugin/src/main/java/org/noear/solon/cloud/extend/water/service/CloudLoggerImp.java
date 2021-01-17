package org.noear.solon.cloud.extend.water.service;

import org.noear.solon.cloud.CloudLogger;
import org.noear.water.log.WaterLogger;

/**
 * @author noear 2021/1/17 created
 */
public class CloudLoggerImp extends WaterLogger implements CloudLogger {
    public CloudLoggerImp(String name) {
        super(name);
    }

    public CloudLoggerImp(String name, Class<?> clz) {
        super(name, clz);
    }
}

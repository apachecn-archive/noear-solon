package org.noear.solon.cloud.extend.mqtt;

import org.noear.solon.SolonApp;
import org.noear.solon.Utils;
import org.noear.solon.cloud.CloudManager;
import org.noear.solon.cloud.extend.mqtt.service.CloudEventServiceImp;
import org.noear.solon.core.Aop;
import org.noear.solon.core.Plugin;

public class XPluginImp implements Plugin {

    @Override
    public void start(SolonApp app) {
        String server = MqttProps.instance.getEventServer();

        if (Utils.isNotEmpty(server)) {
            if (MqttProps.instance.getEventEnable()) {
                CloudEventServiceImp eventServiceImp = new CloudEventServiceImp(server);
                CloudManager.register(eventServiceImp);

                Aop.beanOnloaded(eventServiceImp::subscribe);
            }
        }
    }
}
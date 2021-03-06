/*************************GO-LICENSE-START*********************************
 * Copyright 2015 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *************************GO-LICENSE-END***********************************/

package com.thoughtworks.go.server.web;

import com.thoughtworks.go.service.ConfigRepository;
import com.thoughtworks.go.util.ReflectionUtil;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.Method;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:WEB-INF/applicationContext-global.xml",
        "classpath:WEB-INF/applicationContext-dataLocalAccess.xml",
        "classpath:WEB-INF/applicationContext-acegi-security.xml",
        "classpath:WEB-INF/spring-cruise-remoting-servlet.xml"
})

public class ScheduledTasksIntegrationTest {
    @Autowired
    private ScheduledTaskRegistrar scheduledTaskRegistrar;
    @Autowired
    private ConfigRepository configRepository;

    @Test
    public void shouldSetupASchedulerForConfigRepoGarbageCollection(){
        Map<Runnable, String> cronTasks = (Map<Runnable, String>) ReflectionUtil.getField(scheduledTaskRegistrar, "cronTasks");
        boolean matchFound = false;
        for (Runnable runnable : cronTasks.keySet()) {
            if (runnable instanceof ScheduledMethodRunnable) {
                ScheduledMethodRunnable scheduledMethodRunnable = (ScheduledMethodRunnable) runnable;
                Object target = scheduledMethodRunnable.getTarget();
                if (target.equals(configRepository)) {
                    matchFound = true;
                    Method method = scheduledMethodRunnable.getMethod();
                    assertThat(method.getName(), Is.is("garbageCollect"));
                    assertThat(cronTasks.get(runnable), is("0 0 1 * * ?"));
                }
            }
        }
        assertThat("Could not find a scheduled job for ConfigRepository.garbageCollect", matchFound, Is.is(true));
    }
}

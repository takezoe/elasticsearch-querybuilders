/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.codelibs.elasticsearch.monitor;

import org.codelibs.elasticsearch.common.component.AbstractLifecycleComponent;
import org.codelibs.elasticsearch.common.settings.Settings;
import org.codelibs.elasticsearch.env.NodeEnvironment;
import org.codelibs.elasticsearch.monitor.fs.FsService;
import org.codelibs.elasticsearch.monitor.jvm.JvmGcMonitorService;
import org.codelibs.elasticsearch.monitor.jvm.JvmService;
import org.codelibs.elasticsearch.monitor.os.OsService;
import org.codelibs.elasticsearch.monitor.process.ProcessService;
import org.codelibs.elasticsearch.threadpool.ThreadPool;

import java.io.IOException;

public class MonitorService extends AbstractLifecycleComponent {

    private final JvmGcMonitorService jvmGcMonitorService;
    private final OsService osService;
    private final ProcessService processService;
    private final JvmService jvmService;
    private final FsService fsService;

    public MonitorService(Settings settings, NodeEnvironment nodeEnvironment, ThreadPool threadPool) throws IOException {
        super(settings);
        this.jvmGcMonitorService = new JvmGcMonitorService(settings, threadPool);
        this.osService = new OsService(settings);
        this.processService = new ProcessService(settings);
        this.jvmService = new JvmService(settings);
        this.fsService = new FsService(settings, nodeEnvironment);
    }

    public OsService osService() {
        return this.osService;
    }

    public ProcessService processService() {
        return this.processService;
    }

    public JvmService jvmService() {
        return this.jvmService;
    }

    public FsService fsService() {
        return this.fsService;
    }

    @Override
    protected void doStart() {
        jvmGcMonitorService.start();
    }

    @Override
    protected void doStop() {
        jvmGcMonitorService.stop();
    }

    @Override
    protected void doClose() {
        jvmGcMonitorService.close();
    }

}
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hdds.scm.ha;

import org.apache.hadoop.hdds.conf.ConfigurationException;
import org.apache.hadoop.hdds.conf.DefaultConfigManager;
import org.apache.hadoop.hdds.conf.OzoneConfiguration;
import org.apache.hadoop.hdds.scm.ScmConfigKeys;
import org.apache.hadoop.hdds.scm.server.SCMStorageConfig;
import org.apache.hadoop.hdds.scm.server.StorageContainerManager;
import org.apache.hadoop.hdds.utils.HddsServerUtil;
import org.apache.hadoop.net.NetUtils;
import org.apache.hadoop.ozone.common.Storage;
import org.apache.hadoop.ozone.ha.ConfUtils;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.UUID;

import static org.apache.hadoop.hdds.scm.ScmConfigKeys.OZONE_SCM_ADDRESS_KEY;
import static org.apache.hadoop.hdds.scm.ScmConfigKeys.OZONE_SCM_BLOCK_CLIENT_ADDRESS_KEY;
import static org.apache.hadoop.hdds.scm.ScmConfigKeys.OZONE_SCM_BLOCK_CLIENT_BIND_HOST_KEY;
import static org.apache.hadoop.hdds.scm.ScmConfigKeys.OZONE_SCM_BLOCK_CLIENT_PORT_KEY;
import static org.apache.hadoop.hdds.scm.ScmConfigKeys.OZONE_SCM_CLIENT_ADDRESS_KEY;
import static org.apache.hadoop.hdds.scm.ScmConfigKeys.OZONE_SCM_CLIENT_BIND_HOST_KEY;
import static org.apache.hadoop.hdds.scm.ScmConfigKeys.OZONE_SCM_CLIENT_PORT_KEY;
import static org.apache.hadoop.hdds.scm.ScmConfigKeys.OZONE_SCM_DATANODE_ADDRESS_KEY;
import static org.apache.hadoop.hdds.scm.ScmConfigKeys.OZONE_SCM_DATANODE_BIND_HOST_KEY;
import static org.apache.hadoop.hdds.scm.ScmConfigKeys.OZONE_SCM_DATANODE_PORT_KEY;
import static org.apache.hadoop.hdds.scm.ScmConfigKeys.OZONE_SCM_DB_DIRS;
import static org.apache.hadoop.hdds.scm.ScmConfigKeys.OZONE_SCM_GRPC_PORT_KEY;
import static org.apache.hadoop.hdds.scm.ScmConfigKeys.OZONE_SCM_HTTP_ADDRESS_KEY;
import static org.apache.hadoop.hdds.scm.ScmConfigKeys.OZONE_SCM_HTTP_BIND_HOST_KEY;
import static org.apache.hadoop.hdds.scm.ScmConfigKeys.OZONE_SCM_RATIS_PORT_KEY;
import static org.apache.hadoop.hdds.scm.ScmConfigKeys.OZONE_SCM_SECURITY_SERVICE_ADDRESS_KEY;
import static org.apache.hadoop.hdds.scm.ScmConfigKeys.OZONE_SCM_SECURITY_SERVICE_BIND_HOST_KEY;
import static org.apache.hadoop.hdds.scm.ScmConfigKeys.OZONE_SCM_SECURITY_SERVICE_PORT_KEY;
import static org.apache.hadoop.ozone.OzoneConfigKeys.OZONE_METADATA_DIRS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for SCM HA-related configuration.
 */
class TestSCMHAConfiguration {
  private OzoneConfiguration conf;

  @BeforeEach
  void setup(@TempDir File tempDir) {
    conf = new OzoneConfiguration();
    conf.set(OZONE_METADATA_DIRS, tempDir.getAbsolutePath());
    DefaultConfigManager.clearDefaultConfigs();
  }

  @Test
  public void testSCMHAConfig() throws Exception {
    String scmServiceId = "scmserviceId";
    conf.set(ScmConfigKeys.OZONE_SCM_SERVICE_IDS_KEY, scmServiceId);

    String[] nodes = new String[] {"scm1", "scm2", "scm3"};
    conf.set(ScmConfigKeys.OZONE_SCM_NODES_KEY + "." + scmServiceId,
        "scm1,scm2,scm3");
    conf.set(ScmConfigKeys.OZONE_SCM_NODE_ID_KEY, "scm1");

    int port = 9880;
    int i = 1;
    for (String nodeId : nodes) {
      conf.set(ConfUtils.addKeySuffixes(OZONE_SCM_BLOCK_CLIENT_ADDRESS_KEY,
          scmServiceId, nodeId), "localhost:" + port++);
      conf.setInt(ConfUtils.addKeySuffixes(OZONE_SCM_BLOCK_CLIENT_PORT_KEY,
          scmServiceId, nodeId), port);
      conf.set(ConfUtils.addKeySuffixes(OZONE_SCM_BLOCK_CLIENT_BIND_HOST_KEY,
          scmServiceId, nodeId), "172.28.9.1");

      conf.set(ConfUtils.addKeySuffixes(OZONE_SCM_SECURITY_SERVICE_ADDRESS_KEY,
          scmServiceId, nodeId), "localhost:" + port++);
      conf.setInt(ConfUtils.addKeySuffixes(OZONE_SCM_SECURITY_SERVICE_PORT_KEY,
          scmServiceId, nodeId), port);
      conf.set(ConfUtils.addKeySuffixes(
          OZONE_SCM_SECURITY_SERVICE_BIND_HOST_KEY, scmServiceId, nodeId),
          "172.28.9.1");

      conf.set(ConfUtils.addKeySuffixes(OZONE_SCM_CLIENT_ADDRESS_KEY,
          scmServiceId, nodeId), "localhost:" + port++);
      conf.setInt(ConfUtils.addKeySuffixes(OZONE_SCM_CLIENT_PORT_KEY,
          scmServiceId, nodeId), port);
      conf.set(ConfUtils.addKeySuffixes(OZONE_SCM_CLIENT_BIND_HOST_KEY,
          scmServiceId, nodeId), "172.28.9.1");

      conf.set(ConfUtils.addKeySuffixes(OZONE_SCM_DATANODE_ADDRESS_KEY,
          scmServiceId, nodeId), "localhost:" + port++);
      conf.setInt(ConfUtils.addKeySuffixes(OZONE_SCM_DATANODE_PORT_KEY,
          scmServiceId, nodeId), port);
      conf.set(ConfUtils.addKeySuffixes(OZONE_SCM_DATANODE_BIND_HOST_KEY,
          scmServiceId, nodeId), "172.28.9.1");

      conf.set(ConfUtils.addKeySuffixes(OZONE_SCM_HTTP_ADDRESS_KEY,
          scmServiceId, nodeId), "localhost:" + port++);
      conf.set(ConfUtils.addKeySuffixes(OZONE_SCM_HTTP_BIND_HOST_KEY,
          scmServiceId, nodeId), "172.28.9.1");

      conf.set(ConfUtils.addKeySuffixes(OZONE_SCM_DB_DIRS,
          scmServiceId, nodeId), "/var/scm-metadata" + i++);

      conf.set(ConfUtils.addKeySuffixes(OZONE_SCM_ADDRESS_KEY,
          scmServiceId, nodeId), "localhost");

      conf.setInt(ConfUtils.addKeySuffixes(OZONE_SCM_RATIS_PORT_KEY,
          scmServiceId, nodeId), port++);
    }

    SCMStorageConfig scmStorageConfig = Mockito.mock(SCMStorageConfig.class);
    Mockito.when(scmStorageConfig.getState())
        .thenReturn(Storage.StorageState.NOT_INITIALIZED);
    SCMHANodeDetails.loadSCMHAConfig(conf, scmStorageConfig);

    port = 9880;

    // Validate configs.
    assertEquals("localhost:" + port++,
        conf.get(ConfUtils.addKeySuffixes(OZONE_SCM_BLOCK_CLIENT_ADDRESS_KEY,
        scmServiceId, "scm1")));
    assertEquals(port,
        conf.getInt(ConfUtils.addKeySuffixes(OZONE_SCM_BLOCK_CLIENT_PORT_KEY,
        scmServiceId, "scm1"), 9999));
    assertEquals("172.28.9.1",
        conf.get(ConfUtils.addKeySuffixes(OZONE_SCM_BLOCK_CLIENT_BIND_HOST_KEY,
            scmServiceId, "scm1")));


    assertEquals("localhost:" + port++,
        conf.get(ConfUtils.addKeySuffixes(
            OZONE_SCM_SECURITY_SERVICE_ADDRESS_KEY, scmServiceId, "scm1")));
    assertEquals(port, conf.getInt(ConfUtils.addKeySuffixes(
        OZONE_SCM_SECURITY_SERVICE_PORT_KEY, scmServiceId, "scm1"), 9999));
    assertEquals("172.28.9.1",
        conf.get(ConfUtils.addKeySuffixes(
            OZONE_SCM_SECURITY_SERVICE_BIND_HOST_KEY, scmServiceId, "scm1")));


    assertEquals("localhost:" + port++,
        conf.get(ConfUtils.addKeySuffixes(OZONE_SCM_CLIENT_ADDRESS_KEY,
            scmServiceId, "scm1")));
    assertEquals(port,
        conf.getInt(ConfUtils.addKeySuffixes(OZONE_SCM_CLIENT_PORT_KEY,
            scmServiceId, "scm1"), 9999));
    assertEquals("172.28.9.1", conf.get(
        ConfUtils.addKeySuffixes(OZONE_SCM_CLIENT_BIND_HOST_KEY, scmServiceId,
        "scm1")));

    assertEquals("localhost:" + port++,
        conf.get(ConfUtils.addKeySuffixes(OZONE_SCM_DATANODE_ADDRESS_KEY,
            scmServiceId, "scm1")));
    assertEquals(port,
        conf.getInt(ConfUtils.addKeySuffixes(OZONE_SCM_DATANODE_PORT_KEY,
            scmServiceId, "scm1"), 9999));
    assertEquals("172.28.9.1", conf.get(
        ConfUtils.addKeySuffixes(OZONE_SCM_DATANODE_BIND_HOST_KEY, scmServiceId,
        "scm1")));


    assertEquals("localhost:" + port++,
        conf.get(ConfUtils.addKeySuffixes(OZONE_SCM_HTTP_ADDRESS_KEY,
        scmServiceId, "scm1")));
    assertEquals("172.28.9.1",
        conf.get(ConfUtils.addKeySuffixes(OZONE_SCM_HTTP_BIND_HOST_KEY,
        scmServiceId, "scm1")));

    assertEquals("localhost", conf.get(ConfUtils.addKeySuffixes(
        OZONE_SCM_ADDRESS_KEY, scmServiceId,
        "scm1")));

    assertEquals("/var/scm-metadata1",
        conf.get(ConfUtils.addKeySuffixes(OZONE_SCM_DB_DIRS, scmServiceId,
        "scm1")));

    assertEquals(port++,
        conf.getInt(ConfUtils.addKeySuffixes(OZONE_SCM_RATIS_PORT_KEY,
        scmServiceId, "scm1"), 9999));


  }


  @Test
  public void testHAWithSamePortConfig() throws Exception {
    String scmServiceId = "scmserviceId";
    conf.set(ScmConfigKeys.OZONE_SCM_SERVICE_IDS_KEY, scmServiceId);

    String[] nodes = new String[] {"scm1", "scm2", "scm3"};
    conf.set(ScmConfigKeys.OZONE_SCM_NODES_KEY + "." + scmServiceId,
        "scm1,scm2,scm3");
    conf.set(ScmConfigKeys.OZONE_SCM_NODE_ID_KEY, "scm1");


    for (String node : nodes) {
      conf.set(ConfUtils.addKeySuffixes(OZONE_SCM_ADDRESS_KEY, scmServiceId,
          node), "localhost");
    }

    conf.set(OZONE_SCM_RATIS_PORT_KEY, "10000");
    conf.set(OZONE_SCM_GRPC_PORT_KEY, "10001");
    conf.set(OZONE_SCM_BLOCK_CLIENT_PORT_KEY, "9896");
    conf.set(OZONE_SCM_CLIENT_PORT_KEY, "9897");
    conf.set(OZONE_SCM_DATANODE_PORT_KEY, "9898");
    conf.set(OZONE_SCM_SECURITY_SERVICE_PORT_KEY, "9899");

    SCMStorageConfig scmStorageConfig = Mockito.mock(SCMStorageConfig.class);
    Mockito.when(scmStorageConfig.getState())
        .thenReturn(Storage.StorageState.NOT_INITIALIZED);
    SCMHANodeDetails scmhaNodeDetails =
        SCMHANodeDetails.loadSCMHAConfig(conf, scmStorageConfig);

    assertEquals("10000", conf.get(OZONE_SCM_RATIS_PORT_KEY));
    assertEquals("10001", conf.get(OZONE_SCM_GRPC_PORT_KEY));


    InetSocketAddress clientAddress =
        NetUtils.createSocketAddr("0.0.0.0",
        9897);
    InetSocketAddress blockAddress =
        NetUtils.createSocketAddr("0.0.0.0", 9896);
    InetSocketAddress datanodeAddress =
        NetUtils.createSocketAddr("0.0.0.0", 9898);
    assertEquals(clientAddress,
        scmhaNodeDetails.getLocalNodeDetails()
            .getClientProtocolServerAddress());
    assertEquals(blockAddress, scmhaNodeDetails.getLocalNodeDetails()
        .getBlockProtocolServerAddress());
    assertEquals(datanodeAddress,
        scmhaNodeDetails.getLocalNodeDetails()
            .getDatanodeProtocolServerAddress());

    assertEquals(10000,
        scmhaNodeDetails.getLocalNodeDetails().getRatisPort());
    assertEquals(10001,
        scmhaNodeDetails.getLocalNodeDetails().getGrpcPort());

    for (SCMNodeDetails peer : scmhaNodeDetails.getPeerNodeDetails()) {
      assertEquals(clientAddress,
          peer.getClientProtocolServerAddress());
      assertEquals(blockAddress,
          peer.getBlockProtocolServerAddress());
      assertEquals(datanodeAddress,
          peer.getDatanodeProtocolServerAddress());

      assertEquals(10000, peer.getRatisPort());
      assertEquals(10001,
          peer.getGrpcPort());
    }


    // Security protocol address is not set in SCMHANode Details.
    // Check conf is properly set with expected port.
    assertEquals(
        NetUtils.createSocketAddr("0.0.0.0", 9899),
        HddsServerUtil.getScmSecurityInetAddress(conf));


  }

  @Test
  public void testRatisEnabledDefaultConfigWithoutInitializedSCM()
      throws IOException, NoSuchFieldException, IllegalAccessException {
    SCMStorageConfig scmStorageConfig = Mockito.mock(SCMStorageConfig.class);
    Mockito.when(scmStorageConfig.getState())
        .thenReturn(Storage.StorageState.NOT_INITIALIZED);
    SCMHANodeDetails.loadSCMHAConfig(conf, scmStorageConfig);
    Assert.assertEquals(SCMHAUtils.isSCMHAEnabled(conf),
        ScmConfigKeys.OZONE_SCM_HA_ENABLE_DEFAULT);
    DefaultConfigManager.clearDefaultConfigs();
    conf.setBoolean(ScmConfigKeys.OZONE_SCM_HA_ENABLE_KEY, false);
    SCMHANodeDetails.loadSCMHAConfig(conf, scmStorageConfig);
    Assert.assertEquals(SCMHAUtils.isSCMHAEnabled(conf), false);
    DefaultConfigManager.clearDefaultConfigs();
    conf.setBoolean(ScmConfigKeys.OZONE_SCM_HA_ENABLE_KEY, true);
    SCMHANodeDetails.loadSCMHAConfig(conf, scmStorageConfig);
    Assert.assertEquals(SCMHAUtils.isSCMHAEnabled(conf), true);
  }

  @Test
  public void testRatisEnabledDefaultConfigWithInitializedSCM()
      throws IOException, NoSuchFieldException, IllegalAccessException {
    SCMStorageConfig scmStorageConfig = Mockito.mock(SCMStorageConfig.class);
    Mockito.when(scmStorageConfig.getState())
        .thenReturn(Storage.StorageState.INITIALIZED);
    Mockito.when(scmStorageConfig.isSCMHAEnabled()).thenReturn(false);
    DefaultConfigManager.clearDefaultConfigs();
    SCMHANodeDetails.loadSCMHAConfig(conf, scmStorageConfig);
    Assert.assertEquals(SCMHAUtils.isSCMHAEnabled(conf),
        scmStorageConfig.isSCMHAEnabled());
    Mockito.when(scmStorageConfig.isSCMHAEnabled()).thenReturn(false);
    DefaultConfigManager.clearDefaultConfigs();
    Assert.assertEquals(SCMHAUtils.isSCMHAEnabled(conf), true);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void testRatisEnabledDefaultConflictConfigWithInitializedSCM(
      boolean isRatisEnabled) {
    SCMStorageConfig scmStorageConfig = Mockito.mock(SCMStorageConfig.class);
    Mockito.when(scmStorageConfig.getState())
        .thenReturn(Storage.StorageState.INITIALIZED);
    Mockito.when(scmStorageConfig.isSCMHAEnabled()).thenReturn(isRatisEnabled);
    conf.setBoolean(ScmConfigKeys.OZONE_SCM_HA_ENABLE_KEY, !isRatisEnabled);
    assertThrows(ConfigurationException.class,
            () -> SCMHANodeDetails.loadSCMHAConfig(conf, scmStorageConfig));
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void testHAConfig(boolean ratisEnabled) throws IOException {
    conf.setBoolean(ScmConfigKeys.OZONE_SCM_HA_ENABLE_KEY, ratisEnabled);
    SCMStorageConfig scmStorageConfig = newStorageConfig(ratisEnabled);
    StorageContainerManager.scmInit(conf, scmStorageConfig.getClusterID());
    assertEquals(ratisEnabled, DefaultConfigManager.getValue(
        ScmConfigKeys.OZONE_SCM_HA_ENABLE_KEY, !ratisEnabled));
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void testInvalidHAConfig(boolean ratisEnabled) throws IOException {
    conf.setBoolean(ScmConfigKeys.OZONE_SCM_HA_ENABLE_KEY, ratisEnabled);
    SCMStorageConfig scmStorageConfig = newStorageConfig(!ratisEnabled);
    String clusterID = scmStorageConfig.getClusterID();
    assertThrows(ConfigurationException.class,
        () -> StorageContainerManager.scmInit(conf, clusterID));
  }

  private SCMStorageConfig newStorageConfig(
      boolean ratisEnabled) throws IOException {
    final SCMStorageConfig scmStorageConfig = new SCMStorageConfig(conf);
    scmStorageConfig.setClusterId(UUID.randomUUID().toString());
    scmStorageConfig.setSCMHAFlag(ratisEnabled);
    scmStorageConfig.initialize();
    return scmStorageConfig;
  }

}

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.hadoop.fs.ozone.contract.rooted;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.contract.AbstractContractRootDirectoryTest;
import org.apache.hadoop.fs.contract.AbstractFSContract;

import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Ozone contract test for ROOT directory operations.
 */
public class ITestRootedOzoneContractRootDir extends
    AbstractContractRootDirectoryTest {

  @BeforeClass
  public static void createCluster() throws IOException {
    RootedOzoneContract.createCluster();
  }

  @AfterClass
  public static void teardownCluster() {
    RootedOzoneContract.destroyCluster();
  }

  @Override
  protected AbstractFSContract createContract(Configuration conf) {
    return new RootedOzoneContract(conf);
  }

  @Override
  public void testRmRootRecursive() {
    // OFS doesn't support creating files directly under root
  }

  @Override
  public void testRmNonEmptyRootDirNonRecursive() {
    // OFS doesn't support creating files directly under root
  }

  @Override
  public void testRmEmptyRootDirNonRecursive() {
    // Internally test deletes volume recursively
    // Which is not supported
  }

  @Override
  public void testListEmptyRootDirectory() {
    // Internally test deletes volume recursively
    // Which is not supported
  }

  @Override
  public void testSimpleRootListing() {
    // Recursive list is not supported
  }

  @Override
  public void testMkDirDepth1() {
    // Internally test deletes volume recursively
    // Which is not supported
  }
}

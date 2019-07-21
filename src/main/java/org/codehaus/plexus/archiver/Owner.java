/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.codehaus.plexus.archiver;

public class Owner
{

  private String userName;
  private Integer userId;
  private String groupName;
  private Integer groupId;

  public String getUserName()
  {
    return userName;
  }

  public void setUserName( String userName )
  {
    this.userName = userName;
  }

  public Integer getUserId()
  {
    return userId;
  }

  public void setUserId( Integer userId )
  {
    this.userId = userId;
  }

  public String getGroupName()
  {
    return groupName;
  }

  public void setGroupName( String groupName )
  {
    this.groupName = groupName;
  }

  public Integer getGroupId()
  {
    return groupId;
  }

  public void setGroupId( Integer groupId )
  {
    this.groupId = groupId;
  }

}

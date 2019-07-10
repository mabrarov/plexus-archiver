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

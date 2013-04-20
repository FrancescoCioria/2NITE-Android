package com.mosquitolabs.tonight;

import java.io.Serializable;
import java.net.URL;



public class PageData
  implements Serializable
{
  private static final long serialVersionUID = 1L;
  public String _ID;
  public String name;
  public int number_of_likes;
  public String address="";
  public String category="";
  public String desc="";
  public String website="";
  public int checkins;
  public String phone="";
  public boolean you_like_it;
  public URL picURL;
  public URL coverURL;
  public boolean coverChecked=false;

  
  

  public String toString()
  {
    return this.name;
  }
}

/* Location:           C:\Users\VAIO\Desktop\facebookevents.apk\classes_dex2jar.jar
 * Qualified Name:     com.francescocioria.fbevents.PageData
 * JD-Core Version:    0.6.0
 */
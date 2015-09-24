package com.stoko.mappenger;

import com.google.android.gms.maps.model.Marker;

import java.util.Date;

/**
 * Created by Daniel on 14.9.2015 г..
 */
public class UserMessage {
    public double Latitude;
    public double Longitude;
    public String Message;
    public int MessageStatus;
    public int MessageType;
    public Date MessageLife;
    public String ParentMessageID;
    public String PartitionKey;
    public String RowKey;
    public Date Timestamp;

    public Marker messageMarker;
    public String IconType;
}
